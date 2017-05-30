package studio.html;
import snap.view.ImageView;
import snap.gfx.Image;
import snap.util.XMLElement;
import snap.view.ViewLayout;
import snap.web.WebURL;

/**
 * A HTMLElement subclass for HTML image.
 */
public class HTMLImage extends HTMLElement {

    // The image source
    String          _src;
    
    // The image
    Image           _img;

    // The layout
    ViewLayout  _layout = new ViewLayout.BoxLayout(this);

/**
 * Returns the image source.
 */
public String getSource()  { return _src; }

/**
 * Sets the image source.
 */
public void setSource(String aSrc)
{
    _src = aSrc;
}

/**
 * Returns the image.
 */
public Image getImage()
{
    if(_img!=null || _src==null) return _img;
    HTMLDoc doc = getDoc(); if(doc==null) return null;
    WebURL surl = doc.getSourceURL(_src); if(surl==null) return null;
    Image img = Image.get(surl);
    return _img = img;
}

/**
 * Override to set image.
 */
protected void setShowing(boolean aValue)
{
    super.setShowing(aValue);
    if(aValue && getChildCount()==0) {
        Image img = getImage();
        if(img!=null)
            addChild(new ImageView(img));
    }
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
public void readHTML(XMLElement aXML)
{
    // Read src
    String src = aXML.getAttributeValue("src");
    if(src!=null)
        setSource(src);

    // Do normal version
    super.readHTML(aXML);
}



}