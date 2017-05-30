package studio.html;
import java.util.List;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import snap.view.*;
import snap.web.WebURL;

/**
 * A view to represent an HTMLElement.
 */
public class HTMLElement extends ChildView {

/**
 * Returns the doc.
 */
public HTMLDoc getDoc()  { return getParent(HTMLDoc.class); }

/**
 * Reads HTML.
 */
public void readHTML(XMLElement aXML)
{
    // Read attributes
    List <XMLAttribute> attrs = aXML.getAttributes();
    if(attrs!=null)
    for(XMLAttribute attr : aXML.getAttributes()) {
        String name = attr.getName().toLowerCase();
        switch(name) {
            case "width": setPrefWidth(attr.getDoubleValue()); break;
            case "height": setPrefHeight(attr.getDoubleValue()); break;
        }
        System.out.println(getClass().getSimpleName() + " read " + name + " = " + attr.getValue());
    }
    
    // Read children
    for(XMLElement cxml : aXML.getElements()) {
        HTMLElement child = createHTML(cxml);
        if(child!=null)
            addChild(child);
    }
}

/**
 * Creates an HTML element for given XML.
 */
public static HTMLElement createHTML(XMLElement aXML)  { return createHTML(aXML, null); }

/**
 * Creates an HTML element for given XML.
 */
public static HTMLElement createHTML(XMLElement aXML, WebURL aSourceURL)
{
    String name = aXML.getName().toLowerCase();
    HTMLElement hview = null;
    switch(name) {
        case "html": hview = new HTMLDoc(); break;
        case "body": hview = new HTMLBody(); break;
        case "table": hview = new HTMLTable(); break;
        case "tr": hview = new HTMLTableRow(); break;
        case "td": hview = new HTMLTableData(); break;
        case "img": hview = new HTMLImage(); break;
        default:
            for(XMLElement child : aXML.getElements())
                if((hview = createHTML(child))!=null)
                    return hview;
    }

    if(hview!=null)
        hview.readHTML(aXML);
    return hview;
}

}