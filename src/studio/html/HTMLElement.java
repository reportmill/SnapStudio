package studio.html;
import java.util.List;
import snap.gfx.Color;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import snap.view.*;

/**
 * A view to represent an HTMLElement.
 */
public class HTMLElement extends ChildView {

/**
 * Returns the doc.
 */
public HTMLDoc getDoc()  { return getParent(HTMLDoc.class); }

/**
 * Returns the first child.
 */
protected View getChild()  { return getChildCount()>0? getChild(0) : null; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return BoxView.getPrefWidth(this, getChild(), aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return BoxView.getPrefHeight(this, getChild(), aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { BoxView.layout(this, getChild(), null, false, false); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Read attributes
    List <XMLAttribute> attrs = aXML.getAttributes();
    if(attrs!=null)
    for(XMLAttribute attr : aXML.getAttributes()) {
        String name = attr.getName().toLowerCase();
        String value = attr.getValue();
        if(value.contains("%"))
            continue;
        switch(name) {
            case "width": setPrefWidth(attr.getDoubleValue()); break;
            case "height": setPrefHeight(attr.getDoubleValue()); break;
            case "bgcolor": setFill(Color.get(attr.getValue())); break;
        }
        //System.out.println(getClass().getSimpleName() + " read " + name + " = " + attr.getValue());
    }
    
    // Read children
    readHTMLChildren(aXML, aDoc);
}

/**
 * Reads HTML.
 */
public void readHTMLChildren(XMLElement aXML, HTMLDoc aDoc)
{
    for(XMLElement cxml : aXML.getElements()) {
        
        // Handle Font
        if(cxml.getName().equals("font"))
            readHTMLChildren(cxml, aDoc);
        
        // Handle anything else
        else {
            HTMLElement child = createHTML(cxml, aDoc);
            if(child!=null)
                addChild(child);
        }
    }
}

/**
 * Creates an HTML element for given XML.
 */
public static HTMLElement createHTML(XMLElement aXML, HTMLDoc aDoc)
{
    String name = aXML.getName().toLowerCase();
    HTMLElement hview = null;
    switch(name) {
        case "a": hview = new HTMLLink(); break;
        case "body": hview = new HTMLBody(); break;
        case "html": hview = new HTMLDoc(); break;
        case "html_text": hview = new HTMLText(); break;
        case "img": hview = new HTMLImage(); break;
        case "li": hview = new HTMLListItem(); break;
        case "ol": hview = new HTMLList(); break;
        case "p": hview = new HTMLParagraph(); break;
        case "table": hview = new HTMLTable(); break;
        case "td": hview = new HTMLTableData(); break;
        case "tr": hview = new HTMLTableRow(); break;
        case "ul": hview = new HTMLList(); break;
        default:
            for(XMLElement child : aXML.getElements())
                if((hview = createHTML(child, aDoc))!=null)
                    return hview;
    }

    if(hview!=null)
        hview.readHTML(aXML, aDoc);
    return hview;
}

}