package studio.app;
import java.util.List;
import snap.gfx.Rect;
import snap.view.*;
import studio.app.ORAReader.*;

/**
 * A custom class.
 */
public class PuppetView extends ParentView {
    
    // The stack of layers
    Stack      _stack;
    
    Rect       _layerBnds;
    

/**
 * Creates a PuppetView.
 */
public PuppetView()
{
    ORAReader rdr = new ORAReader();
    _stack = rdr.readFile();
    rebuildLayers(_stack);
    resize();
}

/**
 * Returns the stack.
 */
public Stack getStack()  { return _stack; }

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
 * Adds children for stack.
 */
public void rebuildLayers(Stack aStack)
{
    List <Layer> entries = aStack.entries;
    for(int i=entries.size()-1; i>=0; i--) { Layer entry = entries.get(i);
        if(entry instanceof Stack) {
            rebuildLayers((Stack)entry); continue; }
    
        ImageView iview = new ImageView(entry.src); iview.setName(entry.name);
        iview.setXY(entry.x, entry.y);
        iview.setSize(iview.getPrefSize());
        addChild(iview); entry.view = iview;
        expandLayerBounds(entry.x, entry.y, iview.getWidth(), iview.getHeight());
    }
    
    if(getWidth()<_layerBnds.getMaxX()) setWidth(_layerBnds.getMaxX());
    if(getHeight()<_layerBnds.getMaxY()) setHeight(_layerBnds.getMaxY());
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

void resize()
{
    for(View c : getChildren())
        c.setBounds(c.getX()/4, c.getY()/4, c.getWidth()/4, c.getHeight()/4);
    setSize(getWidth()/4, getHeight()/4);
}

}