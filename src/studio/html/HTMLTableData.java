package studio.html;
import snap.gfx.Color;
import snap.util.XMLElement;
import snap.view.ViewLayout;

/**
 * A HTMLElement subclass for HTML table data.
 */
public class HTMLTableData extends HTMLElement {

/**
 * Creates a new HTMLTableData.
 */
public HTMLTableData()
{
    setBorder(Color.PINK,1);
}

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new ViewLayout.VBoxLayout(this); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Do normal version
    super.readHTML(aXML, aDoc);
    
    System.out.println("TableData: " + aXML.getValue());
}

}