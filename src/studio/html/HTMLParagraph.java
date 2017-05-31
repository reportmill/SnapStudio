package studio.html;
import snap.view.ViewLayout;

/**
 * A HTMLElement subclass for HTML paragraph tag.
 */
public class HTMLParagraph extends HTMLElement {

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new ViewLayout.HBoxLayout(this); }

}