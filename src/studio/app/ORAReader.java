package studio.app;
import java.util.*;
import snap.util.XMLElement;
import snap.web.WebURL;

/**
 * A class to read an ORA file.
 */
public class ORAReader {
    
    // The source path
    String      _srcPath;
    
    int _indent = -1;

/**
 * Read file.
 */
public Stack readFile()
{
    _srcPath = "/tmp/CTLady";
        
    WebURL url = WebURL.getURL(_srcPath + "/stack.xml");

    XMLElement imgXML = XMLElement.getElement(url);
    XMLElement stackXML = imgXML.getElement(0);
    
    Stack stack = readStack(stackXML);
    
    // Return view
    return stack;
}

Stack readStack(XMLElement aXML)
{
    String name = aXML.getAttributeValue("name");
    if(name!=null && name.equals("HeadBone")) return null;
    
    Stack stack = new Stack(name);
    
    for(int i=0;i<_indent;i++) System.out.print("    "); System.out.println(stack);
    _indent++;
    
    for(XMLElement xml : aXML.getElements()) {
        String type = xml.getName();
        Layer entry = null;
        if(type.equals("stack"))
            entry = readStack(xml);
        else if(type.equals("layer"))
             entry = readLayer(xml);
        else System.out.println("Unknown type: " + type);
        if(entry!=null) {
            stack.entries.add(entry);
            entry.stack = stack;
        }
    }
    
    _indent--;
    return stack;
}

Layer readLayer(XMLElement aXML)
{
    String name = aXML.getAttributeValue("name");
    if(name==null || name.startsWith("Mask")) return null;
    
    String src = _srcPath + '/' + aXML.getAttributeValue("src");
    String visibility = aXML.getAttributeValue("visibility");
    boolean isVis = visibility.equals("visible");
    if(!isVis) return null;
    
    // Get X/Y
    double x = aXML.getAttributeDoubleValue("x");
    double y = aXML.getAttributeDoubleValue("y");
    Layer layer = new Layer(name, src, isVis, x, y);
    
    for(int i=0;i<_indent;i++) System.out.print("    "); System.out.println(layer);
    return layer;
}

/**
 * A layer entry.
 */
public static class Layer {
    
    public String name;
    
    public String src;
    
    public boolean visible;
    
    public double x, y;
    
    public Stack stack;
    
    public Object view;
    
    /** Creates an ORA Layer. */
    public Layer(String aName)  { name = aName; }
    
    /** Creates an ORA Layer. */
    public Layer(String aName, String aSrc, boolean isVis, double aX, double aY)
    {
        name = aName; src = aSrc; visible = isVis; x = aX; y = aY;
    }
    
    public String toString()
    {
        return "Layer: name=" + name + ", src=" + src + ", x=" + x + ", y=" + y;
    }
}

/**
 * A stack entry.
 */
public static class Stack extends Layer {
    
    public List <Layer> entries = new ArrayList();
    
    /** Creates an ORA Stack. */
    public Stack(String aName)  { super(aName); }
    
    public String toString()
    {
        return "Stack: name=" + name;
    }
}

}