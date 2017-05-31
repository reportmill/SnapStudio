package studio.html;
import snap.util.XMLElement;
import snap.view.View;

/**
 * A HTMLElement subclass for HTML table.
 */
public class HTMLListItem extends HTMLElement {


/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Do normal version
    super.readHTML(aXML, aDoc);
    
    // Add bullet
    for(View child : getChildren()) {
        if(child instanceof HTMLText) { HTMLText text = (HTMLText)child;
            text.setText("- " + text.getText()); return; }
    }
}

}