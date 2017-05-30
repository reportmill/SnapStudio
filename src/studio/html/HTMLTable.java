package studio.html;
import snap.gfx.Color;
import snap.view.ViewLayout;

/**
 * A HTMLElement subclass for HTML table.
 */
public class HTMLTable extends HTMLElement {

    // The layout
    ViewLayout  _layout = new ViewLayout.VBoxLayout(this);

/**
 * Creates a new HTMLTable.
 */
public HTMLTable()
{
    setBorder(Color.LIGHTGRAY,1);
    setFill(Color.CYAN);
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

}