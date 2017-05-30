package studio.html;
import snap.gfx.Color;
import snap.view.ViewLayout;

/**
 * A HTMLElement subclass for HTML table row.
 */
public class HTMLTableRow extends HTMLElement {

    // The layout
    ViewLayout  _layout = new ViewLayout.HBoxLayout(this);

/**
 * Creates a new HTMLTableRow.
 */
public HTMLTableRow()
{
    setBorder(Color.GREEN,1);
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