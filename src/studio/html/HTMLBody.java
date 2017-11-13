package studio.html;
import snap.view.*;

/**
 * A HTMLElement for HTML body.
 */
public class HTMLBody extends HTMLElement {

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