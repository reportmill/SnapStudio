package studio.app;
import snap.geom.Polygon;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * This class is responsible for the UI associated with the inspector window.
 */
public class InspectorPanel extends EditorPane.SupportPane {
    
    // The selection path view
    ChildView            _selPathView;
    
    // The child inspector current installed in inspector panel
    ViewOwner            _childInspector;
    
    // The inspector for paint/fill shape attributes
    ShapeFills           _shapeFills = new ShapeFills(getEditorPane());
    
    // The inspector for shape placement attributes (location, size, roll, scale, skew, autosizing)
    ShapePlacement       _shapePlacement = new ShapePlacement(getEditorPane());
    
    // The inspector for shape general attributes (name, url, text wrap around)
    ShapeGeneral         _shapeGeneral = new ShapeGeneral(getEditorPane());
    
    // The inspector for shape animation
    Animation            _animation = new Animation(getEditorPane());
    
    // The inspector to show view hierarchy
    ViewTree             _viewTree = new ViewTree(getEditorPane());
    
    // The inspector for Undo
    //UndoInspector        _undoInspector;
    
    // Used for managing selection path
    View              _deepestShape;
    
    // Used for managing selection path
    View              _selectedShape;

/**
 * Creates a new InspectorPanel for EditorPane.
 */
public InspectorPanel(EditorPane anEP)  { super(anEP); }

/**
 * Initializes UI panel for the inspector.
 */
public void initUI()
{
    // Get SelPathView
    _selPathView = getView("SelectionPathPanel", ChildView.class);
    enableEvents(_selPathView, MouseRelease);
    
    // Create the Action that redispatches the event and add the action to the action map
    addKeyActionHandler("UndoAction", "meta Z");
    //getView("OffscreenButton").setPickable(false);
    
    // Configure Window
    //getWindow().setType(WindowView.TYPE_UTILITY); getWindow().setAlwaysOnTop(true);
    //getWindow().setHideOnDeactivate(true);getWindow().setResizable(false);getWindow().setSaveName("InspectorPanel");
}

/**
 * Refreshes the inspector for the current editor selection.
 */
public void resetUI()
{
    // Get editor (and just return if null) and tool for selected shapes
    Editor editor = getEditor(); if(editor==null) return;
    ViewTool tool = editor.getTool(editor.getSelectedOrSuperSelectedViews());
    
    // If ShapeSpecificButton is selected, instal inspector for current selection
    if(getViewBoolValue("ShapeSpecificButton"))
        setInspector(tool);
    
    // If FillsButton is selected, install fill inspector
    if(getViewBoolValue("ShapeFillsButton"))
        setInspector(_shapeFills);

    // Get the inspector (owner)
    ViewOwner owner = getInspector();
    
    // Get window title from owner and set
    //String title = RMKey.getStringValue(owner, "getWindowTitle"); getWindow().setTitle(title);

    // If owner non-null, tell it to reset
    if(owner!=null)
        owner.resetLater();
    
    // Reset the selection path matrix
    resetSelPathView();
    
    // Get image for current tool and set in ShapeSpecificButton
    Image timage = tool.getImage();
    getView("ShapeSpecificButton", ButtonBase.class).setImage(timage);
}

/**
 * Handles changes to the inspector UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle ShapePlacementButton
    if(anEvent.equals("ShapePlacementButton"))
        setInspector(_shapePlacement);
    
    // Handle ShapeGeneralButton
    if(anEvent.equals("ShapeGeneralButton"))
        setInspector(_shapeGeneral);
    
    // Handle AnimationButton
    if(anEvent.equals("AnimationButton")) setInspector(_animation);
    
    // Handle UndoAction
    if(anEvent.equals("UndoAction"))
        getEditor().undo();
        
    // Handle SelPath
    if(anEvent.getName().startsWith("SelPath"))
        popSelection(SnapUtils.intValue(anEvent.getName()));
    
    // Handle SelectionPathPanel
    if(anEvent.equals("SelectionPathPanel") && anEvent.isMouseRelease())
        setVisible(9);
    
    // Reset ui
    resetUI();
}

/**
 * Returns whether the inspector is visible.
 */
public boolean isVisible()  { return isUISet() && getUI().isShowing(); }

/**
 * Sets whether the inspector is visible.
 */
public void setVisible(boolean aValue)
{
    // If requested visible and inspector is not visible, make visible
    if(aValue && !isVisible())
        setVisible(-1);
    
    // If requested invisible and inspector is visible, set window not visible
    //else if(!aValue && isVisible()) setWindowVisible(false);
}

/**
 * Sets the inspector to be visible, showing the specific sub-inspector at the given index.
 */
public void setVisible(int anIndex)
{
    // If index 0, 1 or 3, set appropriate toggle button true
    if(anIndex==0) setViewValue("ShapeSpecificButton", true);
    if(anIndex==1) setViewValue("ShapeFillsButton", true);
    if(anIndex==3) setViewValue("ShapeGeneralButton", true);
    
    // If index is 6, show _undoInspector
    /*if(anIndex==6) {
        setInspector(_undoInspector!=null? _undoInspector : (_undoInspector = new UndoInspector(getEditorPane())));
        setViewValue("OffscreenButton", true);
    }*/
    
    // If index is 9, show ShapeTree Inspector
    if(anIndex==9) {
        setInspector(_viewTree);
        getView("ShapeSpecificButton", ToggleButton.class).getToggleGroup().setSelected(null);
    }
}

/**
 * Returns whether the inspector is showing the datasource inspector.
 */
public boolean isShowingDataSource()  { return isUISet() && getViewBoolValue("OffscreenButton"); }

/**
 * Returns the inspector (owner) of the inspector pane.
 */
protected ViewOwner getInspector()  { return _childInspector; }

/**
 * Sets the inspector in the inspector pane.
 */
protected void setInspector(ViewOwner anOwner)
{
    _childInspector = anOwner;
    getView("InspectorPanel", BorderView.class).setCenter(anOwner.getUI());
}

/**
 * Updates the selection path UI.
 */
public void resetSelPathView() 
{
    // Get main editor, Selected/SuperSelected shape and shape that should be selected in selection path
    Editor editor = getEditor();
    View selectedShape = editor.getSelectedOrSuperSelectedView();
    View shape = _deepestShape!=null && _deepestShape.isAncestor(selectedShape)? _deepestShape : selectedShape;
    
    // If the selectedShape has changed because of external forces, reset selectionPath to point to it
    if(selectedShape != _selectedShape)
        shape = selectedShape;
    
    // Set new DeepestShape to be shape
    _deepestShape = shape; _selectedShape = selectedShape;

    // Remove current buttons
    for(int i=_selPathView.getChildCount()-1; i>=0; i--) {
        View button = _selPathView.removeChild(i);
        if(button instanceof ToggleButton) getToggleGroup("SelectionPath").remove((ToggleButton)button);
    }
    
    // Add buttons for DeepestShape and its ancestors
    View contentBox = editor.getContent().getParent();
    for(View shp=_deepestShape; shp!=contentBox && shp!=null; shp=shp.getParent()) {
        
        // Create new button and configure action
        ToggleButton button = new ToggleButton(); button.setName("SelPath " + getParentCount(shp));
        button.setShowArea(false);
        button.setPrefSize(40,40);
        button.setMinSize(40,40);
        
        // Set button images
        Image img2 = getImage(shp, 40, 40, Color.CLEAR); button.setImage(img2);
        //Image img3 = getImage(img, 40, 40, Color.WHITE); button.setSelImage(img3);
        button.setToolTip(shp.getClass().getSimpleName()); // Tooltip
        if(shp==selectedShape) button.setSelected(true);  // Whether selected
        
        // Add button to selection path panel and button group
        _selPathView.addChild(button, 0); button.setOwner(this);
        getToggleGroup("SelectionPath").add(button);
        if(shp!=_deepestShape) _selPathView.addChild(new Sep(), 1);
    }
}

/** Returns an image as a new size, centered, with given background color. */
private Image getImage(View aView, int aW, int aH, Color aColor)
{
    // Get image for shape
    //Image img = getEditor().getTool(aView).getImage();
    Image img = Image.get(aW, aH, true);
    double vw = aView.getWidth(), vh = aView.getHeight(), sc = Math.min((aW-8)/vw, (aH-8)/vh);
    Painter pnt = img.getPainter(); pnt.translate(4,4); pnt.scale(sc,sc);
    ViewUtils.paintAll(aView, pnt); pnt.flush();

    // Get image at size, centered, with background color
    Image img2 = Image.get(aW, aH, aColor.getAlpha()<1);
    Painter pntr = img2.getPainter(); pntr.setColor(aColor); pntr.fillRect(0,0,aW,aH);
    pntr.drawImage(img, (aW-img.getWidth())/2, (aH-img.getHeight())/2); pntr.flush();
    return img2;
}

/**
 * Changes the selection path selection to the level of the string index in the action event.
 */
public void popSelection(int selIndex) 
{
    // Get main editor (just return if editor or deepest shape is null)
    Editor editor = getEditor(); if(editor==null || _deepestShape==null) return;
    
    // If user selected descendant of current selected shape, select on down to it
    if(selIndex > getParentCount(editor.getSelectedOrSuperSelectedView())) {
        
        // Get current deepest shape
        View shape = _deepestShape;

        // Find shape that was clicked on
        while(selIndex != getParentCount(shape))
            shape = shape.getParent();

        // If shape parent's childrenSuperSelectImmediately, superSelect shape
        View par = shape.getParent(); ViewTool tool = editor.getTool(par);
        if(tool.childrenSuperSelectImmediately(par))
            editor.setSuperSelectedView(shape);

        // If shape shouldn't superSelect, just select it
        editor.setSelectedView(shape);
    }

    // If user selected ancestor of current shape, pop selection up to it
    else while(selIndex != getParentCount(editor.getSelectedOrSuperSelectedView()))
        editor.popSelection();

    // Set selected shape to new editor selected shape
    _selectedShape = editor.getSelectedOrSuperSelectedView();
    
    // Make sure shape specific inspector is selected
    if(!getViewBoolValue("ShapeSpecificButton"))
        getView("ShapeSpecificButton", ToggleButton.class).fire();
}

/**
 * Makes the inspector panel show the document inspector.
 */
public void showDocumentInspector()
{
    setVisible(0); // Select the shape specific inspector
    resetSelPathView(); // Reset selection path matrix
    popSelection(0); // Pop selection
}

/**
 * Returns the parent count of a view relative to editor content.
 */
private int getParentCount(View aView)
{
    if(aView==getEditor().getContent() || aView==null) return 0;
    return getParentCount(aView.getParent()) + 1;
}

/** View to render SelectionPath separator. */
private static class Sep extends View {
    protected double getPrefWidthImpl(double aH)  { return 5; }
    protected double getPrefHeightImpl(double aW)  { return 40; }
    protected void paintFront(Painter aPntr)  { aPntr.setColor(Color.DARKGRAY); aPntr.fill(_arrow); }
    static Polygon _arrow = new Polygon(0, 15, 5, 20, 0, 25);
}

}