package studio.html;
import snap.gfx.Color;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML table.
 */
public class HTMLTable extends HTMLElement {

/**
 * Creates a new HTMLTable.
 */
public HTMLTable()
{
    setBorder(Color.LIGHTBLUE.brighter().brighter().brighter(),1);
    getLayout(ColView.VBoxLayout.class).setFillWidth(true);
}

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new ColView.VBoxLayout(this); }

}