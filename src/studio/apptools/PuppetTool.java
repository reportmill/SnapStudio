package studio.apptools;
import snap.gfx.*;
import studio.app.*;
import studio.app.ORAReader.*;
import snap.view.*;

/**
 * A custom class.
 */
public class PuppetTool <T extends PuppetView> extends ViewTool <T> {
    
    // The current puppet view
    PuppetView        _pupView;
    // The TreeView
    TreeView <Layer>  _treeView;
    
    // The TreeResolver
    PuppetViewTreeResolver  _treeRes = new PuppetViewTreeResolver();
    
    // The selected layer
    Layer             _selLayer;

    // Constants
    static Color SELECT_COLOR = Color.get("#039ed3");
    static Effect SELECT_EFFECT = new ShadowEffect(8, SELECT_COLOR, 0, 0);

/**
 * Returns the selected layer.
 */
public Layer getSelLayer()  { return _selLayer; }

/**
 * Sets the selected layer.
 */
public void setSelLayer(Layer aLayer)
{
    if(getSelView()!=null) getSelView().setEffect(null);
    _selLayer = aLayer;
    if(getSelView()!=null) getSelView().setEffect(SELECT_EFFECT);
}

/**
 * Sets the selected layer for given name.
 */
public void setSelLayer(String aName)
{
    PuppetView pview = getSelectedView(); if(pview==null) return;
    Layer layer = pview.getLayer(aName);
    setSelLayer(layer);
}

View getSelView()  { return _selLayer!=null? (View)_selLayer.view : null; }

/**
 * Create UI.
 */
protected View createUI()
{
    // Create DollModeCheckBox
    CheckBox dollCheckBox = new CheckBox("Doll Mode"); dollCheckBox.setName("DollModeCheckBox");
    
    // Create/configure TreeView
    _treeView = new TreeView(); _treeView.setGrowWidth(true); _treeView.setGrowHeight(true);
        
    ColView colView = new ColView(); colView.setPadding(20,8,8,8); colView.setSpacing(5); colView.setFillWidth(true);
    colView.addChild(dollCheckBox);
    colView.addChild(_treeView);
    return colView;
}

/**
 * InitUI.
 */
protected void initUI()
{
    _treeView.setResolver(_treeRes);
    _treeView.getCol(0).setAltPaint(new Color("#F8"));
    _treeView.setBorder(Border.createLineBorder(Color.LIGHTGRAY,1));
    _treeView.setCellConfigure(c -> configureCell(c));
}

/**
 * Updates the UI controls from currently selected page.
 */
public void resetUI()
{
    // Get currently selected page (just return if null)
    PuppetView pview = getSelectedView(); if(pview==null) return;
    boolean pviewChanged = pview!=_pupView; _pupView = pview;
    
    // Update DollModeCheckBox
    setViewValue("DollModeCheckBox", pview.isDollMode());
    
    // Update TreeView Items
    if(pviewChanged) {
        _treeView.setItems(_treeRes.getChildren(pview.getStack()));
        _treeView.expandAll();
    }
    _treeView.setSelItem(getSelLayer());
}

/**
 * Responds to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get currently selected page (just return if null)
    PuppetView pview = getSelectedView(); if(pview==null) return;
    
    // Handle DollModeCheckBox
    if(anEvent.equals("DollModeCheckBox")) {
        pview.setDollMode(anEvent.getBoolValue());
    }

    // Handle TreeView
    if(anEvent.equals(_treeView)) {
        setSelLayer(_treeView.getSelItem());
    }
}

/**
 * Paints when tool is active for things like SelectTool's handles & selection rect or polygon's in-progress path.
 */
/*public void paintTool(Painter aPntr)
{
    if(_selLayer!=null && _selLayer.view!=null) {
        View view = _selLayer.view;
        aPntr.
    }
}*/

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aView, ViewEvent anEvent)
{
    PuppetView pview = getSelectedView(); if(pview==null) return;
    Point pnt = pview.parentToLocal(anEvent.getX(), anEvent.getY(), anEvent.getView());
    View hitView = ViewUtils.getChildAt(pview, pnt.x, pnt.y);
    if(hitView!=null)
        setSelLayer(hitView.getName());
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

/**
 * Configures a TreeView cell.
 */
void configureCell(ListCell <Layer> aCell)
{
    Layer layer = aCell.getItem(); if(layer==null) return;
    CheckBox cbox = new CheckBox(); if(layer.visible) cbox.setSelected(true); cbox.setScale(.8);
    cbox.addEventHandler(e -> cellCheckBoxClicked(aCell, cbox), Action);
    aCell.setGraphicAfter(cbox);
}

/**
 * Called when cell checkbox clicked.
 */
void cellCheckBoxClicked(ListCell <Layer> aCell, CheckBox aCBox)
{
    Layer layer = aCell.getItem();
    if(layer.view!=null)
        ((View)layer.view).setVisible(aCBox.isSelected());
}

/**
 * A TreeResolver for PuppetView.
 */
private static class PuppetViewTreeResolver extends TreeResolver <Layer> {
    
    /** Returns the parent of given item. */
    public Layer getParent(Layer anItem)  { return anItem.stack; }
    
    // Return whether file is directory
    public boolean isParent(Layer anObj)  { return anObj instanceof Stack; }

    // Return child files
    public Layer[] getChildren(Layer aPar)  { return ((Stack)aPar).entries.toArray(new Layer[0]); }

    // Return child file name
    public String getText(Layer anItem)  { return anItem.name; }

    // Return child file icon
    //public Image getImage(Layer aFile)  { return ViewUtils.getFileIconImage(aFile); }
}
        
}