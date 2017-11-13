package studio.html;
import snap.view.*;

/**
 * A HTMLElement for HTML body.
 */
public class HTMLBody extends HTMLElement {

/**
 * Creates the layout.
 */
protected ViewLayout createLayout()  { return new ColView.VBoxLayout(this); }

}