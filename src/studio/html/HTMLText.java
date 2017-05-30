package studio.html;
import snap.util.XMLElement;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML text.
 */
public class HTMLText extends HTMLElement {

    // The layout
    ViewLayout  _layout = new ViewLayout.BoxLayout(this);

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
    // Do normal version
    super.readHTML(aXML, aDoc);
    
    // Read text and create text view
    String text = aXML.getValue();
    TextView tview = new TextView(); tview.setFill(null); tview.setText(text);
    addChild(tview);
}

}