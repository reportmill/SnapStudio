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
}

/**
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return ColView.getPrefWidth(this, getChildren(), aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return ColView.getPrefHeight(this, getChildren(), 0, aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { ColView.layout(this, getChildren(), null, true, 0); }

}