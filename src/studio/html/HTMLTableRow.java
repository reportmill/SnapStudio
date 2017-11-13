package studio.html;
import snap.gfx.Color;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML table row.
 */
public class HTMLTableRow extends HTMLElement {

/**
 * Creates a new HTMLTableRow.
 */
public HTMLTableRow()
{
    setBorder(Color.GREEN.brighter().brighter().brighter().brighter(),1);
}

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new RowView.HBoxLayout(this); }

}