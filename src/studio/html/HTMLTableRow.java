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
 * Returns the preferred width.
 */
protected double getPrefWidthImpl(double aH)  { return RowView.getPrefWidth(this, getChildren(), 0, aH); }

/**
 * Returns the preferred height.
 */
protected double getPrefHeightImpl(double aW)  { return RowView.getPrefHeight(this, getChildren(), aW); }

/**
 * Layout children.
 */
protected void layoutImpl()  { RowView.layout(this, getChildren(), null, false, 0); }

}