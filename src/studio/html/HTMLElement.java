package studio.html;
import java.util.List;
import snap.util.XMLAttribute;
import snap.util.XMLElement;
import snap.view.*;

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
public void readHTML(XMLElement aXML, HTMLDoc aDoc)
{
    // Read attributes
    List <XMLAttribute> attrs = aXML.getAttributes();
    if(attrs!=null)
    for(XMLAttribute attr : aXML.getAttributes()) {
        String name = attr.getName().toLowerCase();
        String value = attr.getValue();
        if(value.contains("%"))
            continue;
        switch(name) {
            case "width": setPrefWidth(attr.getDoubleValue()); break;
            case "height": setPrefHeight(attr.getDoubleValue()); break;
        }
        //System.out.println(getClass().getSimpleName() + " read " + name + " = " + attr.getValue());
    }
    
    // Read children
    for(XMLElement cxml : aXML.getElements()) {
        HTMLElement child = createHTML(cxml, aDoc);
        if(child!=null)
            addChild(child);
    }
}

/**
 * Creates an HTML element for given XML.
 */
public static HTMLElement createHTML(XMLElement aXML, HTMLDoc aDoc)
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
        case "html_text": hview = new HTMLText(); break;
        default:
            for(XMLElement child : aXML.getElements())
                if((hview = createHTML(child, aDoc))!=null)
                    return hview;
    }

    if(hview!=null)
        hview.readHTML(aXML, aDoc);
    return hview;
}

}