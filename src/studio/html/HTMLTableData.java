package studio.html;
import snap.gfx.Color;
import snap.view.ViewLayout;

/**
 * A HTMLElement subclass for HTML table data.
 */
public class HTMLTableData extends HTMLElement {

    // The layout
    ViewLayout  _layout = new ViewLayout.VBoxLayout(this);

/**
 * Creates a new HTMLTableData.
 */
public HTMLTableData()
{
    setBorder(Color.PINK,1);
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