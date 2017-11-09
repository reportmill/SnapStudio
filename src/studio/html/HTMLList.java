package studio.html;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML table.
 */
public class HTMLList extends HTMLElement {

/**
 * Creates a new HTMLList.
 */
public HTMLList()
{
    setPadding(0,0,0,12);
}

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new VBox.VBoxLayout(this); }


}