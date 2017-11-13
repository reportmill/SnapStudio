package studio.html;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML paragraph tag.
 */
public class HTMLParagraph extends HTMLElement {

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