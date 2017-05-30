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
public Image getImage()  { return _img; }

/**
 * Sets the image.
 */
public void setImage(Image anImage)
{
    _img = anImage;
    ImageView iview = getChildCount()>0? (ImageView)getChild(0) : null;
    if(iview==null)
        addChild(iview = new ImageView(anImage));
    else iview.setImage(anImage);
}

/**
 * Loads the image.
 */
protected void loadImage(HTMLDoc aDoc)
{
    WebURL surl = aDoc.getSourceURL(_src);
    if(surl==null) { System.err.println("HTMLImage.loadImage: Can't find image for source: " + _src); return; }
    Image img = Image.get(surl);
    if(img!=null)
        setImage(img);
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
    // Read src
    String src = aXML.getAttributeValue("src");
    if(src!=null) {
        setSource(src);
        loadImage(aDoc);
    }

    // Do normal version
    super.readHTML(aXML, aDoc);
}

}