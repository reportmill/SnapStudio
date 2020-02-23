package studio.apptools;
import snap.geom.Point;
import snap.gfx.*;
import studio.app.*;
import snap.view.*;

/**
 * A custom class.
 */
public class PuppetTool <T extends PuppetView> extends ViewTool <T> {
    
    // The current puppet view
    PuppetView         _pupView;
    
    // The ListView
    ListView <String>  _partsList;
    
    // The selected part name
    String             _selName;

    // Constants
    static Color SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Returns the selected part.
 */
public String getSelName()  { return _selName; }

/**
 * Sets the selected layer.
 */
public void setSelName(String aName)
{
    if(getSelView()!=null) getSelView().setEffect(null);
    _selName = aName;
    if(getSelView()!=null) getSelView().setEffect(SELECT_EFFECT);
}

/**
 * Sets the selected layer for given name.
 */
public void setSelPart(String aName)
{
    PuppetView pview = getSelectedView(); if(pview==null) return;
    setSelName(aName);
}

View getSelView()
{
    if(_selName==null) return null;
    PuppetView pview = getSelectedView(); if(pview==null) return null;
    return pview.getChild(_selName);
}

/**
 * Create UI.
 */
protected View createUI()
{
    // Create/configure TreeView
    _partsList = new ListView(); _partsList.setGrowWidth(true); _partsList.setGrowHeight(true);
        
    ColView colView = new ColView(); colView.setPadding(20,8,8,8); colView.setSpacing(5); colView.setFillWidth(true);
    colView.addChild(_partsList);
    return colView;
}

/**
 * InitUI.
 */
protected void initUI()
{
    //_treeView.setBorder(Border.createLineBorder(Color.LIGHTGRAY,1));
}

/**
 * Updates the UI controls from currently selected page.
 */
public void resetUI()
{
    // Get currently selected page (just return if null)
    PuppetView pview = getSelectedView(); if(pview==null) return;
    Puppet puppet = pview.getPuppet();
    boolean pviewChanged = pview!=_pupView; _pupView = pview;
    
    // Update TreeView Items
    if(pviewChanged) {
        _partsList.setItems(puppet.getPartNames());
    }
    _partsList.setSelItem(getSelName());
}

/**
 * Responds to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get currently selected page (just return if null)
    PuppetView pview = getSelectedView(); if(pview==null) return;
    
    // Handle PartsList
    if(anEvent.equals(_partsList)) {
        setSelName(_partsList.getSelItem());
    }
}

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aView, ViewEvent anEvent)
{
    PuppetView pview = getSelectedView(); if(pview==null) return;
    Point pnt = pview.parentToLocal(anEvent.getX(), anEvent.getY(), anEvent.getView());
    View hitView = ViewUtils.getChildAt(pview, pnt.x, pnt.y);
    if(hitView!=null)
        setSelPart(hitView.getName());
}

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseDragged(T aView, ViewEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseReleased(T aView, ViewEvent anEvent)  { }

/**
 * Returns whether a given view can be ungrouped.
 */
public boolean isUngroupable(View aView)  { return true; }

}