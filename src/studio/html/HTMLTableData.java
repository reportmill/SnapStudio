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
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new ColView.VBoxLayout(this); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Do normal version
    super.readHTML(aXML, aDoc);
}

}