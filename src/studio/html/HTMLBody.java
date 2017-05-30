package studio.html;
import snap.view.ViewLayout;

/**
 * A HTMLElement for HTML body.
 */
public class HTMLBody extends HTMLElement {

    // The layout
    ViewLayout  _layout = new ViewLayout.VBoxLayout(this);

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