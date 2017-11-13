package studio.html;
import snap.gfx.Color;
import snap.util.XMLElement;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML table data.
 */
public class HTMLTableData extends HTMLElement {

/**
 * Creates a new HTMLTableData.
 */
public HTMLTableData()
{
    setBorder(Color.PINK.brighter().brighter(),1);
    setGrowWidth(true);
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
    // Do normal version
    super.readHTML(aXML, aDoc);
}

}