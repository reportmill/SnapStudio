package studio.html;
import snap.util.XMLElement;
import snap.view.*;

/**
 * A HTMLElement subclass for HTML text.
 */
public class HTMLText extends HTMLElement {

/**
 * Returns the text.
 */
public String getText()
{
    TextView tview = getTextView();
    return tview!=null? tview.getText() : null;
}

/**
 * Sets the text.
 */
public void setText(String aStr)
{
    TextView tview = getTextView();
    if(tview!=null) tview.setText(aStr);
}

/**
 * Returns the text view.
 */
public TextView getTextView()  { return getChildCount()>0? (TextView)getChild(0) : null; }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Do normal version
    super.readHTML(aXML, aDoc);
    
    // Read text and create text view
    String text = aXML.getValue();
    TextView tview = new TextView(); tview.setFill(null); tview.setText(text);
    addChild(tview);
}

}