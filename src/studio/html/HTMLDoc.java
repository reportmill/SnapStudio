package studio.html;
import snap.gfx.Color;
import snap.util.XMLElement;
import snap.view.ViewLayout;
import snap.web.PathUtils;
import snap.web.WebSite;
import snap.web.WebURL;

/**
 * A view for displaying an HTML document.
 */
public class HTMLDoc extends HTMLElement {

    // The source URL
    WebURL      _srcURL;
    
    // The layout
    ViewLayout  _layout = new ViewLayout.VBoxLayout(this);

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
 * Returns the source URL for given string path.
 */
public WebURL getSourceURL(String aPath)
{
    if(_srcURL==null) return null;
    String spath = _srcURL.getPath();
    String path = PathUtils.getRelative(spath, aPath);
    WebSite site = _srcURL.getSite();
    return site.getURL(path);
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return _layout.getPrefWidth(aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return _layout.getPrefHeight(aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { _layout.layoutChildren(); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    super.readHTML(aXML, aDoc);
}

}