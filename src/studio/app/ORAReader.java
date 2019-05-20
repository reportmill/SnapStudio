package studio.app;
import java.util.*;
import snap.gfx.*;
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
    String visibility = aXML.getAttributeValue("visibility");
    stack.visible = visibility!=null && visibility.equals("visible");
    
    //for(int i=0;i<_indent;i++) System.out.print("    "); System.out.println(stack);
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
    
    //for(int i=0;i<_indent;i++) System.out.print("    "); System.out.println(layer);
    return layer;
}

/**
 * A layer entry.
 */
public static class Layer {
    
    public String name;
    
    public String src;
    
    public boolean visible;
    
    public double x = Float.MAX_VALUE, y = Float.MAX_VALUE;
    
    public Stack stack;
    
    public Object view;
    
    Image  _img;
    
    /** Creates an ORA Layer. */
    public Layer(String aName)  { name = aName; }
    
    /** Creates an ORA Layer. */
    public Layer(String aName, String aSrc, boolean isVis, double aX, double aY)
    {
        name = aName; src = aSrc; visible = isVis; x = aX; y = aY;
    }
    
    /** Returns the image. */
    public Image getImage()
    {
        if(_img!=null) return _img;
        return _img = Image.get(src);
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
    
    /** Returns the image. */
    public Image getImage()
    {
        // If already set, just return
        if(_img!=null) return _img;
        
        // Iterate over entries and find x, y, maxX, maxY
        double mx = 0, my = 0;
        for(Layer entry : entries) {
            if(!entry.visible) continue;
            Image img = entry.getImage(); if(img==null) continue;
            x = Math.min(x, entry.x);
            y = Math.min(y, entry.y);
            mx = Math.max(mx, entry.x + img.getPixWidth());
            my = Math.max(my, entry.y + img.getPixHeight());
        }
        
        // Get pixels wide/tall
        int px = (int)Math.ceil(mx - x);
        int py = (int)Math.ceil(my - y);
        if(px<1 || py<1 || px>5000 || py>5000) {
            System.out.println("Stack.getImage: No image for layer: " + name); return null; }
        
        // Create image and render layer images in it
        Image img = Image.get(px, py, true);
        Painter pntr = img.getPainter();
        for(int i=entries.size()-1; i>=0; i--) { Layer entry = entries.get(i);
            if(!entry.visible) continue;
            Image im = entry.getImage(); if(im==null) continue;
            pntr.drawImage(im, entry.x - x, entry.y - y);
        }
        
        // Return image
        return _img = img;
    }
    
    /** Returns the layer with given name. */
    public Layer getLayer(String aName)
    {
        if(aName.equals(name)) return this;
        for(Layer entry : entries) {
            if(aName.equals(entry.name))
                return entry;
            if(entry instanceof Stack) { Stack stack = (Stack)entry;
                Layer match = stack.getLayer(aName);
                if(match!=null)
                    return match;
            }
        }
        return null;
    }
    
    public String toString()
    {
        return "Stack: name=" + name;
    }
}

}