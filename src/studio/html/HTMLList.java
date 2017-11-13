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

}