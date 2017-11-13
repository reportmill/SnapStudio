package studio.html;
import snap.gfx.Color;
import snap.util.XMLElement;
import snap.view.*;
import snap.web.WebURL;

/**
 * A HTMLElement subclass for HTML link tag.
 */
public class HTMLLink extends HTMLElement {
    
    // The HRef
    String           _href;

/**
 * Creates a new HTMLLink.
 */
public HTMLLink()
{
    enableEvents(MouseRelease);
    setCursor(Cursor.HAND);
}

/**
 * Handle event.
 */
protected void processEvent(ViewEvent anEvent)
{
    if(anEvent.isMouseRelease()) {
        HTMLDoc doc = getDoc();
        View viewerUI = doc.getParent();
        HTMLViewer viewer = (HTMLViewer)viewerUI.getOwner();
        WebURL url = doc.getSourceURL(getHRef());
        HTMLDoc doc2 = HTMLDoc.getDoc(url);
        viewer.setDoc(doc2);
    }
}

/**
 * Returns the HRef.
 */
public String getHRef()  { return _href; }

/**
 * Sets the HRef.
 */
public void setHRef(String aValue)  { _href = aValue; }

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, getChildren(), 0, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, getChildren(), aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { RowView.layout(this, getChildren(), null, false, 0); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Do normal version
    super.readHTML(aXML, aDoc);
    
    // Set HRef
    String href = aXML.getAttributeValue("href");
    setHRef(href);
    
    // Set all child text to red
    for(View child : getChildren()) {
        child.setPickable(false);
        if(child instanceof HTMLText) { HTMLText text = (HTMLText)child;
            text.setTextFill(Color.RED);
            text.setTextUnderlined(true);
        }
    }
}

}