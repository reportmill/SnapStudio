package studio.html;
import snap.gfx.Color;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * A view for displaying an HTML document.
 */
public class HTMLDoc extends HTMLElement {

    // The source URL
    WebURL      _srcURL;
    
    // The title
    String      _title;
    
/**
 * Creates a new HTMLDoc.
 */
public HTMLDoc()
{
    setFill(Color.WHITE);
}

/**
 * Creates a new HTMLDoc for given source.
 */
public HTMLDoc(Object aSource)
{
    WebURL surl = _srcURL = WebURL.getURL(aSource);
    
    XMLElement xml = null;
    try { xml = new HTMLParser().parseXML(surl); }
    catch(Exception e) { throw new RuntimeException(e); }
    readHTML(xml, this);
    
    setPrefSize(800,800);
    setFill(Color.WHITE);
}

/**
 * Returns the document source URL.
 */
public WebURL getSourceURL()  { return _srcURL; }

/**
 * Sets the document source URL.
 */
public void setSourceURL(WebURL aURL)  { _srcURL = aURL; }

/**
 * Returns the title.
 */
public String getTitle()  { return _title; }

/**
 * Sets the title.
 */
public void setTitle(String aTitle)  { _title = aTitle; }

/**
 * Returns the source URL for given string path.
 */
public WebURL getSourceURL(String aPath)
{
    if(_srcURL==null) return null;
    if(StringUtils.startsWithIC(aPath,"http"))
        return WebURL.getURL(aPath);
    String spath = _srcURL.getPath();
    String path = PathUtils.getRelative(spath, aPath);
    WebSite site = _srcURL.getSite();
    return site.getURL(path);
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return ColView.getPrefWidth(this, null, -1); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return ColView.getPrefHeight(this, null, 0, -1); }

/**
 * Layout children.
 */
protected void layoutImpl()  { ColView.layout(this, null, null, true, 0); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Get Head and Title
    aXML.setIgnoreCase(true);
    XMLElement head = aXML.get("head");
    XMLElement title = head!=null? head.get("title") : null;
    if(title!=null) {
        XMLElement titleText = title.get("html_text");
        String titleStr = titleText!=null? titleText.getValue() : null;
        setTitle(titleStr); head.removeElement(title);
    }
    
    // Do normal version
    super.readHTML(aXML, aDoc);
}

/**
 * Returns an HTML doc for given source object.
 */
public static HTMLDoc getDoc(Object aSource)
{
    if(aSource instanceof HTMLDoc)
        return (HTMLDoc)aSource;
    try { return new HTMLDoc(aSource); }
    catch(Exception e) { System.err.println("HTMLDoc.getDoc: Error reading source: " + e); return null; }
}

}