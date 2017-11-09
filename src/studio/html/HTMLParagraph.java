package studio.html;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML paragraph tag.
 */
public class HTMLParagraph extends HTMLElement {

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new HBox.HBoxLayout(this); }

}