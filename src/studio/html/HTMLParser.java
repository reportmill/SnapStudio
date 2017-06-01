/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.html;
import snap.parse.*;
import snap.util.*;

/**
 * A class to load an RXElement from aSource.
 */
public class HTMLParser extends Parser {
    
/**
 * Called when parse fails.
 */
protected void parseFailed(ParseRule aRule, ParseHandler aHandler)
{
    if(aHandler!=null) aHandler.reset();
    System.err.println("HTMLParser: Parse failed for " + aRule.getName() + " at line " + getTokenizer().getLineNum());
    //throw new ParseException(this, aRule);
}

/**
 * Creates a new HTMLParser.
 */
public HTMLParser()
{
    // Install handlers: ParseUtils.installHandlers(getClass(), getRule());
    getRule("Document").setHandler(new DocumentHandler());
    getRule("Prolog").setHandler(new PrologHandler());
    getRule("Element").setHandler(new ElementHandler());
    getRule("Attribute").setHandler(new AttributeHandler());
    getRule("Comment").setHandler(new CommentHandler());
}
    
/**
 * Kicks off xml parsing from given source and builds on this parser's element.
 */
public XMLElement parseXML(Object aSource) throws Exception
{
    String str = SnapUtils.getText(aSource);
    //WebURL url = WebURL.getURL(aSource);
    //String str = url!=null? url.getText() : null;
    //if(str==null && aSource instanceof byte[]) str = new String((byte[])aSource);
    return (XMLElement)parse(str).getCustomNode();
}

/**
 * Override to return XMLTokenizer.
 */
protected Tokenizer createTokenizerImpl()  { return new HTMLTokenizer(); }

/**
 * Document Handler.
 */
public static class DocumentHandler extends ParseHandler <XMLElement> {
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Element
        if(anId=="Element")
            _part = (XMLElement)aNode.getCustomNode();
    }
}

/**
 * Prolog Handler.
 */
public static class PrologHandler extends ParseHandler <XMLElement> {
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Attribute
        if(anId=="Attribute")
            getPart().addAttribute((XMLAttribute)aNode.getCustomNode());
    }
}

/**
 * Element Handler.
 */
public static class ElementHandler extends ParseHandler <XMLElement> {
    
    // Whether element has checked content
    boolean   _checkedContent;
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name") {
            
            // Handle open tag name: create part for name
            String name = aNode.getString();
            if(_part==null) { _part = new XMLElement(name); _checkedContent = false; }
            
            // Handle close tag name: if wrong name, bypass final processing (to close this tag) and rewind
            else if(!_part.getName().equals(name)) {
                
                // Print error message
                int lineNum = aNode.getStartToken().getLineIndex() + 1;
                System.err.println("XMLParser: Expected close tag " + _part.getName() + " at line " + lineNum);
                
                // Tell parser to bypass rest of this element and rewind to close char
                bypass();
                aNode.getParser().setCharIndex(aNode.getStart()-2);
            }
        }
            
        // Handle Attribute
        else if(anId=="Attribute") {
            
            // If attribute is valid, add it
            XMLAttribute attr = aNode.getCustomNode(XMLAttribute.class);
            if(attr!=null && attr.getValue()!=null)
                _part.addAttribute(attr);
                
            // Otherwise, skip to next whitespace
            else {
                Parser parser = aNode.getParser(); parser.clearTokens();
                Tokenizer toker = aNode.getParser().getTokenizer();
                while(toker.hasChar()) { char c = toker.getChar();
                    if(Character.isWhitespace(c) || c=='>')
                        break;
                    if(c=='<') {
                        bypass(); break; }
                    else toker.eatChar();
                }
            }
        }
            
        // Handle Element
        else if(anId=="Element") {
            XMLElement child = aNode.getCustomNode(XMLElement.class);
            if(child!=null) _part.addElement(child);
            
            // Read more content
            XMLElement htext = getHTMLText(aNode);
            if(htext!=null) _part.addElement(htext);
        }
            
        // Handle close: On first close, check for content
        else if(anId==">" && !_checkedContent) { _checkedContent = true;
        
            // Handle Empty tag
            String name = _part.getName();
            if(HTMLUtils.isEmptyTag(name))
                bypass();
        
            // Handle Script: Read script chars into part
            else if(name.equalsIgnoreCase("script"))
                getScriptText(aNode);
            
            // Handle normal element
            else {
                XMLElement htext = getHTMLText(aNode);
                if(htext!=null) _part.addElement(htext);
            }
        }
    }
    
    /** Returns an XML element for extra text. */
    XMLElement getHTMLText(ParseNode aNode)
    {
        HTMLTokenizer xt = (HTMLTokenizer)aNode.getParser().getTokenizer();
        String content = xt.getContent(); if(content==null) return null;
        content = content.trim(); if(content.length()==0) return null;
        content = content.replaceAll("\\s+", " ");
        content = decodeXMLString(content);
        XMLElement txml = new XMLElement("html_text"); txml.setValue(content);
        return txml;
    }
    
    /** Returns an XML element for extra text. */
    void getScriptText(ParseNode aNode)
    {
        HTMLTokenizer xt = (HTMLTokenizer)aNode.getParser().getTokenizer();
        String script = xt.getScript();
        _part.setValue(script);
    }
}

/**
 * Comment Handler.
 */
public static class CommentHandler extends ParseHandler <XMLElement> {
    
    /** Returns the part class. */
    protected Class <XMLElement> getPartClass()  { return XMLElement.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle prefix: Gobble content of comment and terminator
        if(anId=="Comment") {     // Not sure why id isn't: "<!--"
            _part = new XMLElement("COMMENT");
            HTMLTokenizer xt = (HTMLTokenizer)aNode.getParser().getTokenizer();
            String str = xt.getComment();
            _part.setValue(str);
        }
    }
}

/**
 * Attribute Handler.
 */
public static class AttributeHandler extends ParseHandler <XMLAttribute> {
    
    // The attribute name
    String _name;
    
    /** Returns the part class. */
    protected Class <XMLAttribute> getPartClass()  { return XMLAttribute.class; }

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            _name = aNode.getString();
            
        // Handle String
        else if(anId=="String") { String str = aNode.getString(); str = str.substring(1, str.length()-1);
            str = decodeXMLString(str);
            _part = new XMLAttribute(_name, str);
        }
    }
}

/** Converts an XML string to plain. This implementation is a bit bogus. */
private static String decodeXMLString(String aStr)
{
    // If no entity refs, just return
    if(aStr.indexOf('&')<0) return aStr;
    
    // Do common entity ref replacements
    aStr = aStr.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
    aStr = aStr.replace("&quot;", "\"").replace("&apos;", "'").replace("&nbsp;", " ");
    aStr = aStr.replace("&copy;", "\u00A9");
    
    // Iterate over string to find numeric/hex references and replace with char
    for(int start=aStr.indexOf("&#"); start>=0;start=aStr.indexOf("&#",start)) {
        int end = aStr.indexOf(";", start); if(end<0) continue;
        String str0 = aStr.substring(start, end+1), str1 = str0.substring(2,str0.length()-1);
        int val = Integer.valueOf(str1); String str2 = String.valueOf((char)val);
        aStr = aStr.replace(str0, str2);
    }
    
    // Return string
    return aStr;
}

/**
 * A Tokenizer subclass to read HTML contents.
 */
private static class HTMLTokenizer extends Tokenizer {
    
    /** Called to return the value of an element and update the char index. */
    protected String getContent()
    {
        // Mark content start and skip to next element-start char
        int start = _charIndex;
        while(!isNext("<") && _charIndex<length())
            eatChar();
        
        // Handle CDATA: Gobble until close and return string
        if(isNext("<![CDATA[")) {
            _charIndex += "<![CDATA[".length(); if(Character.isWhitespace(_charIndex)) eatChar();
            start = _charIndex;
            while(!isNext("]]>")) eatChar();
            String str = getInput().subSequence(start, _charIndex).toString();
            _charIndex += "]]>".length();
            return str;
        }
        
        // If next char isn't close tag, return null (assumes we hit child element instead of text content)
        //if(!isNext("</")) return null;
        
        // Return string for content
        String str = getInput().subSequence(start, _charIndex).toString();
        if(str.trim().length()==0)
            return null;
        return decodeXMLString(str);
    }

    /** Called to return the content of a comment and update char index. */
    protected String getComment()
    {
        // Mark content start and skip to next element-start char
        int start = _charIndex;
        while(!isNext("-->") && hasChar())
            eatChar();
        
        // Return string for content
        String str = getInput().subSequence(start, _charIndex).toString(); _charIndex += 3;
        return str.trim();
    }

    /** Called to return the content of a script and update char index. */
    protected String getScript()
    {
        // Mark content start and skip to next element-start char
        int start = _charIndex;
        while(!isNext("</script>", true) && hasChar())
            eatChar();
        
        // Return string for content
        String str = getInput().subSequence(start, _charIndex).toString();
        return str.trim();
    }

    /** Returns whether the given string is up next. */
    public boolean isNext(String aStr)  { return isNext(aStr, false); }

    /** Returns whether the given string is up next. */
    public boolean isNext(String aStr, boolean ignoreCase)
    {
        if(_charIndex+aStr.length()>length()) return false;
        for(int i=0,iMax=aStr.length();i<iMax;i++) { char c1 = charAt(_charIndex+i), c2 = aStr.charAt(i);
            if(ignoreCase) c1 = Character.toLowerCase(c1);
            if(c1!=c2)
                return false; }
        return true;
    }
}

/**
 * Test.
 */
public static void main(String args[]) throws Exception
{
    XMLParser parser = new XMLParser();
    XMLElement xml = parser.parseXML("/Temp/SnapCode/src/snap/app/AppPane.snp");
    System.err.println(xml);
}

}