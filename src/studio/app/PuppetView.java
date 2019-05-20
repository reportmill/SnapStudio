package studio.app;
import java.util.List;
import snap.gfx.Rect;
import snap.util.*;
import snap.view.*;
import studio.app.ORAReader.*;

/**
 * A custom class.
 */
public class PuppetView extends ParentView {
    
    // The stack of layers
    Stack      _stack;
    
    Rect       _layerBnds;
    
    // Whether puppet children are just doll parts
    boolean    _dollMode;
    

/**
 * Creates a PuppetView.
 */
public PuppetView()
{
    ORAReader rdr = new ORAReader();
    _stack = rdr.readFile();
    addAllLayers();

    setSize(_layerBnds.getMaxX()/4, _layerBnds.getMaxY()/4);
}

/**
 * Returns the stack.
 */
public Stack getStack()  { return _stack; }

/**
 * Returns whether puppet is in doll mode.
 */
public boolean isDollMode()  { return _dollMode; }

/**
 * Sets whether puppet is in doll mode.
 */
public void setDollMode(boolean aValue)
{
    if(aValue==_dollMode) return;
    
    _dollMode = aValue;
    if(aValue) addDollLayers();
    else addAllLayers();
    
}

/**
 * Returns the layer for given name.
 */
public Layer getLayer(String aName)  { return getLayer(_stack, aName); }

/**
 * Returns the layer for given name.
 */
Layer getLayer(Layer aLayer, String aName)
{
    if(aLayer.name!=null && aLayer.name.equals(aName))
        return aLayer;
    if(aLayer instanceof Stack) { Stack stack = (Stack)aLayer;
        for(Layer l : stack.entries)
            if(getLayer(l, aName)!=null)
                return getLayer(l, aName);
    }
    return null;
}

/**
 * Adds all layers.
 */
void addAllLayers()
{
    removeChildren();
    rebuildLayers(_stack);
    resizeChildren();
}

/**
 * Adds children for doll layers.
 */
void addDollLayers()
{
    removeChildren();
    addImageViewForLayerName("+RArm").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("RHand").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+RThigh").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+RFoot").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+Hip").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("RL_TalkingHead").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+LArm").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("LHand").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+LThigh").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+LFoot").getPhysics(true).setGroupIndex(-1);

    addImageViewForLayerName("Head").setName("joint");
    addImageViewForLayerName("RArm").setName("joint");
    //addImageViewForLayerName("RForearm").setName("joint");
    addImageViewForLayerName("RHand #1").setName("joint");
    addImageViewForLayerName("LArm").setName("joint");
    //addImageViewForLayerName("LForearm").setName("joint");
    addImageViewForLayerName("LHand #1").setName("joint");
    addImageViewForLayerName("RThigh").setName("joint");
    //addImageViewForLayerName("RShank").setName("joint");
    addImageViewForLayerName("RFoot").setName("joint");
    addImageViewForLayerName("LThigh").setName("joint");
    //addImageViewForLayerName("LShank").setName("joint");
    addImageViewForLayerName("LFoot").setName("joint");

    resizeChildren();
}

/**
 * Adds children for stack.
 */
void rebuildLayers(Stack aStack)
{
    // Iterate over stack entries
    List <Layer> entries = aStack.entries;
    for(int i=entries.size()-1; i>=0; i--) { Layer entry = entries.get(i);
    
        // Handle Stack: Recurse into rebuildLayers with Stack
        if(entry instanceof Stack) {
            rebuildLayers((Stack)entry); continue; }
    
        // Handle layer: Create ImageView for Layer.Image
        ImageView iview = addImageViewForLayer(entry);
        expandLayerBounds(entry.x, entry.y, iview.getWidth(), iview.getHeight());
    }
}

/**
 * Adds an image shape for given layer.
 */
ImageView addImageViewForLayer(Layer aLayer)
{
    ImageView iview = new ImageView(aLayer.getImage()); iview.setName(aLayer.name);
    iview.setXY(aLayer.x, aLayer.y);
    iview.setSize(iview.getPrefSize());
    addChild(iview); aLayer.view = iview;
    return iview;
}

/**
 * Adds an image shape for given layer.
 */
ImageView addImageViewForLayerName(String aName)
{
    Layer layer = _stack.getLayer(aName);
    if(layer==null) {
        System.out.println("PuppetView.addImageViewForLayerName: Layer not found: " + aName); return null; }
    return addImageViewForLayer(layer);
}

/**
 * Expands layer bounds.
 */
void expandLayerBounds(double aX, double aY, double aW, double aH)
{
    if(_layerBnds==null) _layerBnds = new Rect(aX, aY, aW, aH);
    else {
        if(aX<_layerBnds.x) { _layerBnds.width += _layerBnds.x - aX; _layerBnds.x = aX; }
        if(aY<_layerBnds.y) { _layerBnds.height += _layerBnds.y - aY; _layerBnds.y = aY; }
        if(aX+aW>_layerBnds.getMaxX()) _layerBnds.width = aX + aW - _layerBnds.x;
        if(aY+aH>_layerBnds.getMaxY()) _layerBnds.height = aY + aH - _layerBnds.y;
    }
}

void resizeChildren()
{
    for(View c : getChildren())
        c.setBounds(c.getX()/4, c.getY()/4, c.getWidth()/4, c.getHeight()/4);
}

public void convertToPreview()
{
    setDollMode(true);
    
    View children[] = getChildren();
    double x = getX(), y = getY();
    ChildView par = (ChildView)getParent();
    for(View c : children) {
        par.addChild(c); c.setXY(c.getX() + x, c.getY() + y);
    }
    par.removeChild(this);
    
}

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    XMLElement xml = super.toXML(anArchiver); xml.setName("PuppetView");
    return xml;
}

}