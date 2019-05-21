package studio.app;
import java.util.List;
import snap.gfx.*;
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
    // Remove children
    removeChildren();
    
    // Add views for Hip, head, arms, hands, thighs, feet
    addImageViewForLayerName("+RArm").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("RHand").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+RThigh").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+RFoot").getPhysics(true).setGroupIndex(-1);
    View hip = addImageViewForLayerName("+Hip"); hip.getPhysics(true).setGroupIndex(-1);
    hip.getPhysics().setDensity(1000);
    addImageViewForLayerName("RL_TalkingHead").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+LThigh").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+LFoot").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("+LArm").getPhysics(true).setGroupIndex(-1);
    addImageViewForLayerName("LHand").getPhysics(true).setGroupIndex(-1);

    // Add joints for head, shoulders, elbows, hands, thighs, feet
    addImageViewForLayerName("Head").getPhysics(true).setJoint(true);
    addImageViewForLayerName("RArm").getPhysics(true).setJoint(true);
    addImageViewForLayerName("RForearm").getPhysics(true).setJoint(true);
    addImageViewForLayerName("RHand #1").getPhysics(true).setJoint(true);
    addImageViewForLayerName("RThigh").getPhysics(true).setJoint(true);
    addImageViewForLayerName("RShank").getPhysics(true).setJoint(true);
    addImageViewForLayerName("RFoot").getPhysics(true).setJoint(true);
    addImageViewForLayerName("LArm").getPhysics(true).setJoint(true);
    addImageViewForLayerName("LForearm").getPhysics(true).setJoint(true);
    addImageViewForLayerName("LHand #1").getPhysics(true).setJoint(true);
    addImageViewForLayerName("LThigh").getPhysics(true).setJoint(true);
    addImageViewForLayerName("LShank").getPhysics(true).setJoint(true);
    addImageViewForLayerName("LFoot").getPhysics(true).setJoint(true);
    
    // Split arms around elbow joint
    splitViewAroundJoint("+RArm", "RForearm");
    splitViewAroundJoint("+LArm", "LForearm");
    splitViewAroundJoint("+RThigh", "RShank");
    splitViewAroundJoint("+LThigh", "LShank");

    // Resize children
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

void splitViewAroundJoint(String aViewName, String aJointName)
{
    ImageView view = (ImageView)getChild(aViewName);
    if(view==null) { System.err.println("PuppetView.splitView: View not found " + aViewName); return; }
    View joint = getChild(aJointName);
    if(joint==null) { System.err.println("PuppetView.splitView: Joint not found " + aJointName); return; }
    
    Rect v1bnds = getSplitBoundsForView(view, joint, true);
    Rect v2bnds = getSplitBoundsForView(view, joint, false);
    Rect i1bnds = new Rect(v1bnds.x - view.getX(), v1bnds.y - view.getY(), v1bnds.width, v1bnds.height);
    Rect i2bnds = new Rect(v2bnds.x - view.getX(), v2bnds.y - view.getY(), v2bnds.width, v2bnds.height);
    
    Image img = view.getImage();
    Image img1 = img.getSubimage(i1bnds.x, i1bnds.y, i1bnds.width, i1bnds.height);
    Image img2 = img.getSubimage(i2bnds.x, i2bnds.y, i2bnds.width, i2bnds.height);
    ImageView view1 = new ImageView(img1); view1.setBounds(v1bnds); view1.getPhysics(true).setGroupIndex(-1);
    ImageView view2 = new ImageView(img2); view2.setBounds(v2bnds); view2.getPhysics(true).setGroupIndex(-1);
    addChild(view1, view.indexInParent());
    addChild(view2, view.indexInParent());
    removeChild(view);
}

Rect getSplitBoundsForView(View aView, View aJoint, boolean doLeftTop)
{
    Rect vbnds = aView.getBounds();
    Rect jbnds = aJoint.getBounds();
    double asp = vbnds.width/vbnds.height;
    double x = vbnds.x, y = vbnds.y, w = 0, h = 0;
    
    // Handle horizontal arm/let
    if(asp<.3333) {
        w = vbnds.width;
        if(doLeftTop) h = jbnds.getMaxY() - y;
        else { y = jbnds.y; h = vbnds.getMaxY() - y; }
    }
    
    // Handle diagonal arm/leg
    else if(asp<3) {
        
        // Handle Right arm/leg
        if(aView.getName().startsWith("+R")) {
            if(doLeftTop) { y = jbnds.y; w = jbnds.getMaxX() - x; h = vbnds.getMaxY() - y; }
            else { x = jbnds.x; w = vbnds.getMaxX() - x; h = jbnds.getMaxY() - y; }
        }
        
        // Handle Left arm/leg
        else {
            if(doLeftTop) { w = jbnds.getMaxX() - x; h = jbnds.getMaxY() - y; }
            else { x = jbnds.x; y = jbnds.y; w = vbnds.getMaxX() - x; h = vbnds.getMaxY() - y; }
        }
    }
    
    // Handle vertial arm/leg
    else {
        h = vbnds.height;
        if(doLeftTop) w = jbnds.getMaxX() - x;
        else { x = jbnds.x; w = vbnds.getMaxX() - x; }
    }
    
    return new Rect(x, y, w, h);
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