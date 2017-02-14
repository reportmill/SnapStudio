package studio.app;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * This is the base class for tools in SnapStudio - the objects that provide UI editing for Views.
 */
public class ViewTool <T extends View> extends ViewOwner {
    
    // The Editor that owns this tool
    Editor                _editor;
    
    // The Editor pane
    EditorPane            _editorPane;

    // The newly created view instance
    protected T           _view;
    
    // The mouse down point that initiated last tool mouse loop
    protected Point       _downPoint;
    
    // The image for a view handle
    static Image          _handle = Image.get(Editor.class, "Handle8x8.png");
    
    // Constants
    public static final byte HandleWidth = 8;

/**
 * Returns the View class that this tool handles.
 */
public Class <T> getViewClass()  { return (Class<T>)View.class; }

/**
 * Returns a new instance of the view class that this tool is responsible for.
 */
protected T newInstance()  { return ClassUtils.newInstance(getViewClass()); }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "View Inspector"; }

/**
 * Create Node.
 */
protected View createUI()
{
    View ui = super.createUI();
    if(ui!=null)
        return ui;
        
    TableView table = new TableView(); table.setName("PropTable");
    TableCol c1 = new TableCol(); c1.setHeaderValue("Key"); c1.setItemKey("Key"); c1.setPrefWidth(120);
    TableCol c2 = new TableCol(); c2.setHeaderValue("Value"); c2.setItemKey("ValueString"); c2.setGrowWidth(true);
    table.addCol(c1); table.addCol(c2);
    ScrollView sview = new ScrollView(table); sview.setPrefHeight(300);
    VBox box = new VBox(); box.setFillWidth(true); box.setPadding(8,4,4,4); box.setSpacing(4);
    box.setChildren(new Label("Properties:"), sview);
    return box; //return getClass()==ViewTool.class? new Label() : super.createUI();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Get/configure PropTable
    TableView propTable = getView("PropTable", TableView.class); if(propTable==null) return;
    enableEvents(propTable, MouseRelease);
}

/**
 * Resets the UI.
 */
protected void resetUI()
{
    TableView propTable = getView("PropTable", TableView.class); if(propTable==null) return;
    int sind = propTable.getSelectedIndex(); if(sind<0) sind = 0;
    propTable.setItems(getSelNodeItems());
    propTable.setSelectedIndex(sind);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle NodeTable double click
    if(anEvent.equals("PropTable") && anEvent.isMouseRelease() && anEvent.getClickCount()==2) {
        TableView <PropItem> propTable = getView("PropTable", TableView.class);
        PropItem prop = propTable.getSelectedItem(); View pview = prop._view; String pkey = prop._key;
        DialogBox dbox = new DialogBox("Set Property Panel"); dbox.setQuestionMessage("Enter " + pkey + " value:");
        String str = dbox.showInputDialog(pview, prop.getValueString()); if(str==null) return;
        pview.setValue(pkey,str);
        //_editor.relayout();
        //getFile().setUpdater(this);
    }
}

/**
 * Returns the selected node prop items.
 */
public List <PropItem> getSelNodeItems()
{
    List props = new ArrayList();
    View selNode = getEditor().getSelectedOrSuperSelectedView();
    props.add(new PropItem(selNode, View.Name_Prop));
    props.add(new PropItem(selNode, View.GrowWidth_Prop));
    props.add(new PropItem(selNode, View.GrowHeight_Prop));
    props.add(new PropItem(selNode, View.LeanX_Prop));
    props.add(new PropItem(selNode, View.LeanY_Prop));
    props.add(new PropItem(selNode, View.Fill_Prop));
    
    if(selNode instanceof ButtonBase) props.add(new PropItem(selNode, ButtonBase.Text_Prop));
    
    if(selNode instanceof Label) props.add(new PropItem(selNode, Label.Text_Prop));
    
    return props;
}

/**
 * A class to hold a property key/value.
 */
public class PropItem {
    
    // The view, key
    View   _view; String _key;
    
    /** Creates a new PropItem for Node and Key. */
    public PropItem(View aView, String aKey)  { _view = aView; _key = aKey; }
    
    /** Returns the key. */
    public String getKey()  { return _key; }
    
    /** Returns the value. */
    public Object getValue()  { return Key.getValue(_view, _key); }
    
    /** Returns the value as string. */
    public String getValueString()
    {
        Object val = getValue(); if(val==null) return "";
        if(val instanceof Number) return _fmt.format(val);
        if(val instanceof Color) return ((Color)val).toHexString();
        return val.toString();
    }
}

// The formatter for numbers
DecimalFormat         _fmt = new DecimalFormat("#.##");

/**
 * Returns the currently active editor.
 */
public Editor getEditor()  { return _editor; }

/**
 * Sets the currently active editor.
 */
public void setEditor(Editor anEditor)  { _editor = anEditor; }

/**
 * Returns the currently active editor pane.
 */
public EditorPane getEditorPane()
{
    if(_editorPane!=null) return _editorPane;
    return _editorPane = _editor.getEditorPane();
}

/**
 * Returns the EditorEvents.
 */
public EditorEvents getEditorEvents()  { return _editor.getEvents(); }

/**
 * Returns the event point in editor conent coords.
 */
public Point getEventPointInDoc()  { return getEditorEvents().getEventPointInDoc(); }

/**
 * Returns the event point editor super selected view coords.
 */
public Point getEventPointInSuperSelectedView(boolean shouldSnap)
{
    return getEditorEvents().getEventPointInShape(shouldSnap);
}

/**
 * Returns the event point editor super selected view coords.
 */
public Point getEventPointInSuperSelectedView(boolean snapToGrid, boolean snapEdges)
{
    return getEditorEvents().getEventPointInShape(snapToGrid, snapEdges);
}

/**
 * Returns the current selected view for the current editor.
 */
public T getSelectedView()
{
    Editor e = getEditor(); if(e==null) return null;
    View view = e.getSelectedOrSuperSelectedView();
    return ClassUtils.getInstance(view, getViewClass());
}

/**
 * Returns the current selected views for the current editor.
 */
public List <? extends View> getSelectedViews()  { return getEditor().getSelectedOrSuperSelectedViews(); }

/**
 * Returns a tool for given object.
 */
public ViewTool getTool(Object anObj)  { return getEditor().getTool(anObj); }

/**
 * Called when a tool is selected.
 */
public void activateTool()  { }

/**
 * Called when a tool is deselected (when another tool is selected).
 */
public void deactivateTool()  { }

/**
 * Called when a tool is selected even when it's already the current tool.
 */
public void reactivateTool()  { }

/**
 * Called when a tool is deselected to give an opportunity to finalize changes in progress.
 */
public void flushChanges(Editor anEditor, View aView)  { }

/**
 * Returns whether a given view is selected in the editor.
 */
public boolean isSelected(View aView)  { return getEditor().isSelected(aView); }

/**
 * Returns whether a given view is superselected in the editor.
 */
public boolean isSuperSelected(View aView)  { return getEditor().isSuperSelected(aView); }

/**
 * Returns whether a given view is super-selectable.
 */
public boolean isSuperSelectable(T aView)  { return aView instanceof ParentView; }

/**
 * Returns whether a given view accepts children.
 */
public boolean getAcceptsChildren(T aView)  { return aView instanceof ParentView; }

/**
 * Returns whether a given view accepts children.
 */
public boolean childrenSuperSelectImmediately()
{
    return childrenSuperSelectImmediately(getEditor().getSuperSelectedView());
}

/**
 * Returns whether a given view accepts children.
 */
public boolean childrenSuperSelectImmediately(View aView)  { return false; }

/**
 * Returns whether a given view can be ungrouped.
 */
public boolean isUngroupable(View aView)  { return false; }//aView.getChildCount()>0; }

/**
 * Adds a child to given view.
 */
public void addChild(T aView, View aChild)
{
    int index = aView instanceof ParentView? ((ParentView)aView).getChildCount() : -1;
    addChild(aView, aChild, index);
}

/**
 * Adds a child to given view.
 */
public void addChild(T aView, View aChild, int anIndex)
{
    if(aView instanceof ChildView)
        ((ChildView)aView).addChild(aChild, anIndex);
    else System.err.println(getClass().getSimpleName() + ".addChild: Not supported");
}

/**
 * Removes a child from view.
 */
public void removeChild(T aView, View aChild)
{
    if(aView instanceof ChildView)
        ((ChildView)aView).removeChild(aChild);
    else System.err.println(getClass().getSimpleName() + ".removeChild: Not supported");
}

/**
 * Editor method - called when an instance of this tool's view is super selected.
 */
public void didBecomeSuperSelected(T aView)  { }

/**
 * Editor method - called when an instance of this tool's view in de-super-selected.
 */
public void willLoseSuperSelected(T aView)  { }

/**
 * Returns the bounds of the view in parent coords when super selected (same as getBoundsMarkedDeep by default).
 */
public Rect getBoundsSuperSelected(T aView)  { return aView.getBoundsLocal(); } //getBoundsMarkedDeep(); }

/**
 * Converts from view units to tool units.
 */
public double getUnitsFromPoints(double aValue)
{
    //Editor editor = getEditor(); RMDocument doc = editor.getDocument();
    //return doc!=null? doc.getUnitsFromPoints(aValue) : aValue;
    return aValue;
}

/**
 * Converts from tool units to view units.
 */
public double getPointsFromUnits(double aValue)
{
    //Editor editor = getEditor(); RMDocument doc = editor.getDocument();
    //return doc!=null? doc.getPointsFromUnits(aValue) : aValue;
    return aValue;
}

/**
 * Returns the font for the given view.
 */
public Font getFont(Editor anEditor, View aView)  { return aView.getFont(); }

/**
 * Sets the font for the given view.
 */
public void setFont(Editor anEditor, View aView, Font aFont)  { aView.setFont(aFont); }

/**
 * Returns the font for the given view.
 */
public Font getFontDeep(Editor anEditor, View aView)
{
    Font font = getFont(anEditor, aView);
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return font;
    for(int i=0, iMax=par.getChildCount(); i<iMax && font==null; i++) font = par.getChild(i).getFont();
    for(int i=0, iMax=par.getChildCount(); i<iMax && font==null; i++) {
        View child = par.getChild(i); ViewTool tool = getTool(child);
        font = tool.getFontDeep(anEditor, child);
    }
    return font;
}

/**
 * Sets the font family for given view.
 */
public void setFontFamily(Editor anEditor, View aView, Font aFont)
{
    // Get new font for given font family font and current view font size/style and set
    Font font = getFont(anEditor, aView), font2 = aFont;
    if(font!=null) {
        if(font.isBold()!=font2.isBold() && font2.getBold()!=null) font2 = font2.getBold();
        if(font.isItalic()!=font2.isItalic() && font2.getItalic()!=null) font2 = font2.getItalic();
        font2 = font2.deriveFont(font.getSize());
    }
    setFont(anEditor, aView, font2);
}

/**
 * Sets the font family for given view.
 */
public void setFontFamilyDeep(Editor anEditor, View aView, Font aFont)
{
    // Set FontFamily for view and recurse for children
    setFontFamily(anEditor, aView, aFont);
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    for(int i=0, iMax=par.getChildCount(); i<iMax; i++) { View child = par.getChild(i);
        ViewTool tool = getTool(child); tool.setFontFamilyDeep(anEditor, child, aFont); }
}

/**
 * Sets the font name for given view.
 */
public void setFontName(Editor anEditor, View aView, Font aFont)
{
    // Get new font for name and current view size and set
    Font font = getFont(anEditor, aView);
    Font font2 = font!=null? aFont.deriveFont(font.getSize()) : aFont;
    setFont(anEditor, aView, font2);
}

/**
 * Sets the font name for given view.
 */
public void setFontNameDeep(Editor anEditor, View aView, Font aFont)
{
    // Set Font name for view and recurse for children
    setFontName(anEditor, aView, aFont);
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    for(int i=0, iMax=par.getChildCount(); i<iMax; i++) { View child = par.getChild(i);
        ViewTool tool = getTool(child); tool.setFontNameDeep(anEditor, child, aFont); }
}

/**
 * Sets the font size for given shape.
 */
public void setFontSize(Editor anEditor, View aView, double aSize, boolean isRelative)
{
    // Get new font for current shape font at new size and set
    Font font = getFont(anEditor, aView); if(font==null) return;
    Font font2 = isRelative? font.deriveFont(font.getSize() + aSize) : font.deriveFont(aSize);
    setFont(anEditor, aView, font2);
}

/**
 * Sets the font size for given shape.
 */
public void setFontSizeDeep(Editor anEditor, View aView, double aSize, boolean isRelative)
{
    setFontSize(anEditor, aView, aSize, isRelative);
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    for(int i=0, iMax=par.getChildCount(); i<iMax; i++) { View child = par.getChild(i);
        ViewTool tool = getTool(child); tool.setFontSizeDeep(anEditor, child, aSize, isRelative); }    
}

/**
 * Sets the font to bold or not bold for given shape.
 */
public void setFontBold(Editor anEditor, View aView, boolean aFlag)
{
    Font font = getFont(anEditor, aView); if(font==null || font.isBold()==aFlag) return;
    Font font2 = font.getBold(); if(font2==null) return;
    setFont(anEditor, aView, font2);
}

/**
 * Sets the font to bold or not bold for given shape and its children.
 */
public void setFontBoldDeep(Editor anEditor, View aView, boolean aFlag)
{
    setFontBold(anEditor, aView, aFlag);
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    for(int i=0, iMax=par.getChildCount(); i<iMax; i++) { View child = par.getChild(i);
        ViewTool tool = getTool(child); tool.setFontBoldDeep(anEditor, child, aFlag); }    
}

/**
 * Sets the font to italic or not italic for given shape.
 */
public void setFontItalic(Editor anEditor, View aView, boolean aFlag)
{
    Font font = getFont(anEditor, aView); if(font==null || font.isItalic()==aFlag) return;
    Font font2 = font.getItalic(); if(font2==null) return;
    setFont(anEditor, aView, font2);
}

/**
 * Sets the font to italic or not italic for given shape and its children.
 */
public void setFontItalicDeep(Editor anEditor, View aView, boolean aFlag)
{
    setFontItalic(anEditor, aView, aFlag);
    ParentView par = aView instanceof ParentView? (ParentView)aView : null; if(par==null) return;
    for(int i=0, iMax=par.getChildCount(); i<iMax; i++) { View child = par.getChild(i);
        ViewTool tool = getTool(child); tool.setFontItalicDeep(anEditor, child, aFlag); }    
}

/**
 * Event handling - called on mouse move when this tool is active.
 */
public void mouseMoved(ViewEvent anEvent)  { }

/**
 * Event handling for shape creation.
 */
public void mousePressed(ViewEvent anEvent)
{
    // Set undo title
    getEditor().undoerSetUndoTitle("Add Shape");

    // Save the mouse down point
    _downPoint = getEventPointInSuperSelectedView(true);

    // Create shape and move to downPoint
    _view = newInstance();
    _view.setXY(_downPoint.x, _downPoint.y);
    
    // Add shape to superSelectedShape and select shape
    ParentView parent = getEditor().getSuperSelectedParentView(); ViewTool ptool = getTool(parent);
    ptool.addChild(parent, _view);
    getEditor().setSelectedView(_view);
}

/**
 * Event handling for shape creation.
 */
public void mouseDragged(ViewEvent anEvent)
{
    _view.repaint();
    Point currentPoint = getEventPointInSuperSelectedView(true);
    double x = Math.min(_downPoint.getX(), currentPoint.getX());
    double y = Math.min(_downPoint.getY(), currentPoint.getY());
    double w = Math.abs(currentPoint.getX() - _downPoint.getX());
    double h = Math.abs(currentPoint.getY() - _downPoint.getY());
    _view.setBounds(x, y, w, h);
}

/**
 * Event handling for shape creation.
 */
public void mouseReleased(ViewEvent anEvent)  { getEditor().setCurrentToolToSelectTool(); _view = null; }

/**
 * Event handling from SelectTool for super selected shapes.
 */
public void processEvent(T aView, ViewEvent anEvent)
{
    switch(anEvent.getType()) {
        case MousePress: mousePressed(aView, anEvent); break;
        case MouseDrag: mouseDragged(aView, anEvent); break;
        case MouseRelease: mouseReleased(aView, anEvent); break;
        case MouseMove: mouseMoved(aView, anEvent); break;
        default: if(anEvent.isKeyEvent()) processKeyEvent(aView, anEvent);
    }
}

/**
 * Event handling from select tool for super selected shapes.
 */
public void mousePressed(T aView, ViewEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseDragged(T aView, ViewEvent anEvent)  { }

/**
 * Event handling from select tool for super selected shapes.
 */
public void mouseReleased(T aView, ViewEvent anEvent)  { }

/**
 * Event handling from select tool - called on mouse move when tool shape is super selected.
 * MouseMoved is useful for setting a custom cursor.
 */
public void mouseMoved(T aView, ViewEvent anEvent)
{
    // Just return if view isn't the super-selected view
    if(aView!=getEditor().getSuperSelectedView()) return;
    
    // Get ViewHandle
    ViewHandle viewHandle = getViewHandleAtPoint(anEvent.getPoint());
    
    // Declare variable for cursor
    Cursor cursor = null;
    
    // If view handle is non-null, set cursor and return
    if(viewHandle!=null)
        cursor = viewHandle.tool.getHandleCursor(viewHandle.view, viewHandle.handle);
    
    // If mouse not on handle, check for mouse over a view
    else {
        
        // Get mouse over view
        View view = getEditor().getViewAtPoint(anEvent.getX(),anEvent.getY());
        View parent = view.getParent();
        
        // If shape isn't super selected and it's parent doesn't superselect children immediately, choose move cursor
        if(!isSuperSelected(view) && !getTool(parent).childrenSuperSelectImmediately(parent)) cursor = Cursor.MOVE;
        
        // If shape is text and either super-selected or child of a super-select-immediately, choose text cursor
        if(view instanceof TextView && (isSuperSelected(view)||getTool(parent).childrenSuperSelectImmediately(parent)))
            cursor = Cursor.TEXT;
    }
    
    // Set cursor if it differs
    getEditor().setCursor(cursor);
}

/**
 * Event hook during selection.
 */
public boolean mousePressedSelection(ViewEvent anEvent)  { return false; }

/**
 * Returns a tool tip string for given view and event.
 */
public String getToolTip(T aView, ViewEvent anEvent)  { return null; }

/**
 * Editor method.
 */
public void processKeyEvent(T aView, ViewEvent anEvent)  { }

/**
 * Paints when tool is active for things like SelectTool's handles & selection rect or polygon's in-progress path.
 */
public void paintTool(Painter aPntr)  { }

/**
 * Handles painting view handles (or any indication that a shape is selected/super-selected).
 */
public void paintHandles(T aView, Painter aPntr, boolean isSuperSelected)
{
    // If no handles, just return
    if(getHandleCount(aView)==0) return;
    
    // Turn off antialiasing and cache current composite
    aPntr.setAntialiasing(false);
    double opacity = aPntr.getOpacity();
    
    // If super-selected, set composite to make drawing semi-transparent
    if(isSuperSelected)
        aPntr.setOpacity(.64);
    
    // Determine if rect should be reduced if the shape is especially small
    boolean mini = aView.getWidth()<16 || aView.getHeight()<16;
        
    // Iterate over view handles, get rect (reduce if needed) and draw
    for(int i=0, iMax=getHandleCount(aView); i<iMax; i++) { Pos hpos = getHandlePos(aView, i);
        Rect hr = getHandleRect(aView, hpos, isSuperSelected); if(mini) hr.inset(1, 1);
        aPntr.drawImage(_handle, hr.getX(), hr.getY(), hr.getWidth(), hr.getHeight());
    }
        
    // Restore composite and turn on antialiasing
    aPntr.setOpacity(opacity);
    aPntr.setAntialiasing(true);
}

/**
 * Returns the number of handles for given view.
 */
public int getHandleCount(T aView)  { return 8; }

/**
 * Returns the handle position for given index.
 */
public Pos getHandlePos(T aView, int anIndex)
{
    switch(anIndex) {
        case 0: return Pos.TOP_LEFT; case 1: return Pos.TOP_RIGHT;
        case 2: return Pos.BOTTOM_LEFT; case 3: return Pos.BOTTOM_RIGHT;
        case 4: return Pos.CENTER_LEFT; case 5: return Pos.CENTER_RIGHT;
        case 6: return Pos.TOP_CENTER; case 7: return Pos.BOTTOM_CENTER;
        default: throw new RuntimeException("ViewTool: getHandlePos: Unsupported: " + anIndex);
    }
}

/**
 * Returns the point for the handle of the given shape at the given handle index in the given shape's coords.
 */
public Point getHandlePoint(T aView, Pos aHandle, boolean isSuperSelected)
{
    // Get bounds of given shape
    Rect bnds = isSuperSelected? getBoundsSuperSelected(aView).getInsetRect(-HandleWidth/2) : aView.getBoundsLocal();
    
    // Get point for given handle
    switch(aHandle) {
        case TOP_LEFT: return new Point(bnds.getX(), bnds.getY());
        case TOP_RIGHT: return new Point(bnds.getMaxX(), bnds.getY());
        case BOTTOM_LEFT: return new Point(bnds.getX(), bnds.getMaxY());
        case BOTTOM_RIGHT: return new Point(bnds.getMaxX(), bnds.getMaxY());
        case CENTER_LEFT: return new Point(bnds.getX(), bnds.getMidY());
        case CENTER_RIGHT: return new Point(bnds.getMaxX(), bnds.getMidY());
        case TOP_CENTER: return new Point(bnds.getMidX(), bnds.getY());
        case BOTTOM_CENTER: return new Point(bnds.getMidX(), bnds.getMaxY());
        default: return null;
    }
}

/**
 * Returns the rect for the handle at the given index in editor coords.
 */
public Rect getHandleRect(T aView, Pos aHandle, boolean isSuperSelected)
{
    // Get handle point for given handle index in shape coords and editor coords
    Point hp = getHandlePoint(aView, aHandle, isSuperSelected);
    Point hpEd = aView.localToParent(getEditor(), hp.getX(), hp.getY());
    
    // Get handle rect at handle point, outset rect by handle width and return
    Rect hr = new Rect(Math.round(hpEd.getX()), Math.round(hpEd.getY()), 0, 0);
    hr.inset(-HandleWidth/2);
    return hr;
}

/**
 * Returns the handle hit by the given editor coord point.
 */
public Pos getHandleAtPoint(T aView, Point aPoint, boolean isSuperSelected)
{
    // Iterate over view handles, get handle rect for current loop handle and return index if rect contains point
    for(int i=0, iMax=getHandleCount(aView); i<iMax; i++) { Pos hpos = getHandlePos(aView, i);
        Rect hr = getHandleRect(aView, hpos, isSuperSelected);
        if(hr.contains(aPoint.getX(), aPoint.getY()))
            return hpos; }
    return null; // Return null since no handle at given point
}

/**
 * Returns the cursor for given handle.
 */
public Cursor getHandleCursor(T aView, Pos aHandle)  { return Cursor.get(aHandle); }

/**
 * Moves the handle at the given index to the given point.
 */
public void moveViewHandle(ViewHandle <T> aViewHandle, Point toPoint)
{
    // Get handle point in shape coords and shape parent coords
    T view = aViewHandle.view;
    Pos handle = aViewHandle.handle;
    Point anchor = aViewHandle.anchor;
    
    // Get anchor/drag points in local and calculate new bounds
    Point p0 = view.parentToLocal(anchor.x,anchor.y);
    Point p1 = view.parentToLocal(toPoint.x,toPoint.y);
    double x = Math.min(p0.x,p1.x), w = Math.max(p0.x,p1.x) - x; w = Math.round(w);
    double y = Math.min(p0.y,p1.y), h = Math.max(p0.y,p1.y) - y; h = Math.round(h);
    
    // If middle handles, constrain
    switch(handle) {
        case CENTER_LEFT: case CENTER_RIGHT: y = 0; h = view.getHeight(); break;
        case BOTTOM_CENTER: case TOP_CENTER: x = 0; w = view.getWidth(); break; }
        
    // Set view bounds in local coords
    view.setBoundsLocal(x, y, w, h);
}

/**
 * Returns the view handle for the given editor point.
 */
public ViewHandle getViewHandleAtPoint(Point aPoint)
{
    // Declare variables for view, handle, tool
    Editor editor = getEditor();
    View view = null; Pos handle = null; ViewTool tool = null;

    // Check selected shapes for a selected handle index
    for(int i=0, iMax=editor.getSelectedViewCount(); handle==null && i<iMax; i++) {
        view = editor.getSelectedView(i); tool = getTool(view);
        handle = tool.getHandleAtPoint(view, aPoint, false);
    }

    // Check super selected shapes for a selected handle index
    for(int i=0, iMax=editor.getSuperSelectedViewCount(); handle==null && i<iMax; i++) {
        view = editor.getSuperSelectedView(i); tool = getTool(view);
        handle = tool.getHandleAtPoint(view, aPoint, true);
    }

    // Return view handle
    return handle!=null? new ViewHandle(view, handle, tool) : null;
}

/**
 * Implemented by tools that can handle drag & drop.
 */
public boolean acceptsDrag(T aView, ViewEvent anEvent)
{
    // Bogus, but currently the page accepts everything
    //if(aView.isRoot()) return true;
    
    // Return true for Color drag or File drag
    if(anEvent.hasDragContent(Clipboard.COLOR)) return true;
    
    // Handle file drag - really just want to check for images here, but can't ask for transferable contents yet
    if(anEvent.hasDragFiles())
        return true;
    
    // Return true in any case if accepts children
    return getTool(aView).getAcceptsChildren(aView);
}

/**
 * Notifies tool that a something was dragged into of one of it's views with drag and drop.
 */
public void dragEnter(View aView, ViewEvent anEvent)  { }

/**
 * Notifies tool that a something was dragged out of one of it's views with drag and drop.
 */
public void dragExit(View aView, ViewEvent anEvent)  { }

/**
 * Notifies tool that something was dragged over one of it's views with drag and drop.
 */
public void dragOver(View aView, ViewEvent anEvent)  { }

/**
 * Notifies tool that something was dropped on one of it's views with drag and drop.
 */
public void drop(T aView, ViewEvent anEvent)
{
    // If a binding key drop, apply binding
    //if(KeysPanel.getDragKey()!=null) KeysPanel.dropDragKey(aView, anEvent);

    // Handle String drop
    /*else*/ if(anEvent.hasDragString())
        dropString(aView, anEvent);

    // Handle color panel drop
    else if(anEvent.hasDragContent(Clipboard.COLOR))
        dropColor(aView, anEvent);

    // Handle File drop - get list of dropped files and add individually
    else if(anEvent.hasDragFiles())
        dropFiles(aView, anEvent);
}

/**
 * Called to handle dropping a string.
 */
public void dropString(T aView, ViewEvent anEvent)
{
    String str = anEvent.getDropString();
    System.out.println("DropString: " + str);
    if(!str.startsWith("GalleryPane: ")) return;
    String cname = str.substring("GalleryPane: ".length());
    Class cls = ClassUtils.getClass(cname);
    View view = (View)ClassUtils.newInstance(cls);
    Image img = Clipboard.getDrag().getDragImage();
    Point pnt = aView.parentToLocal(anEvent.getView(), anEvent.getX(), anEvent.getY());
    double w = img.getWidth(), h = img.getHeight();
    double x = Math.round(pnt.getX() - w/2), y = Math.round(pnt.getY() - h/2);
    view.setBounds(x, y, w, h);
    if(view instanceof ButtonBase)
        view.setText(view.getClass().getSimpleName());
    if(view instanceof RectView) {
       view.setPrefSize(w,h); view.setFill(Color.PINK); view.setBorder(Color.BLACK,1); }
   if(view instanceof Label)
       view.setText("Label");
    ((ChildView)aView).addChild(view);
    getEditor().setSelectedView(view);
}

/**
 * Called to handle dropping a color.
 */
public void dropColor(T aView, ViewEvent anEvent)
{
    Color color = anEvent.getDragboard().getColor();
    getEditor().undoerSetUndoTitle("Set Fill Color");
    aView.setFill(color);
}

/**
 * Called to handle dropping a file.
 */
public void dropFiles(T aView, ViewEvent anEvent)
{
    List <File> filesList = anEvent.getDropFiles(); Point point = anEvent.getPoint();
    for(File file : filesList)
        point = dropFile(aView, file, anEvent);
}

/**
 * Called to handle a file drop on the editor.
 */
private Point dropFile(T aView, File aFile, ViewEvent anEvent)
{
    // If directory, recurse and return
    if(aFile.isDirectory()) {
        for(File file : aFile.listFiles())
            dropFile(aView, file, anEvent);
        return null;
    }
    
    // Get path and extension (set to empty string if null)
    String path = aFile.getPath();
    String fname = FilePathUtils.getFileName(path);
    String ext = FilePathUtils.getExtension(path); if(ext==null) ext = ""; ext = ext.toLowerCase();
    
    // If image, add image to project
    if(ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("gif")) {
        WebFile file = getEditor().getSourceURL().getFile();
        WebFile dir = file.getParent(); if(dir.getFile("pkg.images")!=null) dir = dir.getFile("pkg.images");
        WebFile ifile1 = WebURL.getURL(path).getFile();
        WebFile ifile2 = dir.getSite().createFile(dir.getDirPath() + fname, false);
        ifile2.setBytes(ifile1.getBytes());
        ifile2.save();
        
        Point pnt = aView.parentToLocal(anEvent.getView(), anEvent.getX(), anEvent.getY());
        ImageView iview = new ImageView(ifile2); iview.setImageName(fname); iview.setSize(iview.getPrefSize());
        double w = iview.getWidth(), h = iview.getHeight();
        double x = Math.round(pnt.getX() - w/2), y = Math.round(pnt.getY() - h/2);
        iview.setBounds(x,y,w,h);
        ((ChildView)aView).addChild(iview);
        getEditor().setSelectedView(iview);
    }

    // If xml file, pass it to setDataSource()
    //if(ext.equalsIgnoreCase("xml") || ext.equalsIgnoreCase("json"))
    //    getEditorPane().setDataSource(WebURL.getURL(aFile), aPoint.getX(), aPoint.getY());

    // If image file, add image view
    //else if(RMImageData.canRead(ext))
    //    runLater(() -> dropImageFile(aView, path, aPoint));

    // If reportmill file, addReportFile
    //else if(ext.equalsIgnoreCase("rpt"))
    //    dropReportFile(aView, path, aPoint);
    
    // Return point offset by 10
    return null;//aPoint.offset(10, 10); return aPoint;
}

/**
 * Called to handle an image drop on the editor.
 */
public void dropImageFile(View aView, String aPath, Point aPoint) //private
{
    // If image hit a real view, see if user wants it to be a texture
    Editor editor = getEditor();
    if(aView!=editor.getContent()) {
        
        // Create drop image file options array
        String options[] = { "ImageView", "Texture", "Cancel" };
        
        // Run drop image file options panel
        String msg = "Image can be either image view or texture", title = "Image import";
        DialogBox dbox = new DialogBox(title); dbox.setQuestionMessage(msg); dbox.setOptions(options);
        switch(dbox.showOptionDialog(null, options[0])) {
        
            // Handle Create ImageView
            case 0: while(!getTool(aView).getAcceptsChildren(aView)) aView = aView.getParent(); break;
            
            // Handle Create Texture
            //case 1: aView.setFill(new RMImageFill(aPath, true));
            
            // Handle Cancel
            case 2: return;
        }
    }
    
    // Get parent to add image view to and drop point in parent coords
    ParentView parent = aView instanceof ParentView? (ParentView)aView : aView.getParent();
    Point point = editor.localToView(parent, aPoint.x, aPoint.y);
    
    // Create new image view
    //ImageView imageView = new ImageView(aPath);
    
    // If image not PDF and is bigger than hit view, shrink down
    /*if(!imageView.getImageData().getType().equals("pdf"))
        if(imageView.getWidth()>parent.getWidth() || imageView.getHeight()>parent.getHeight()) {
            double w = imageView.getWidth();
            double h = imageView.getHeight();
            double w2 = w>h? 320 : 320/h*w;
            double h2 = h>w? 320 : 320/w*h;
            imageView.setSize(w2, h2);
        }

    // Set bounds centered around point (or centered on page if image covers 75% of page or more)
    if(imageView.getWidth()/editor.getWidth()>.75f || imageView.getHeight()/editor.getHeight()>.75)
        imageView.setXY(0, 0);
    else imageView.setXY(point.x - imageView.getWidth()/2, point.y - imageView.getHeight()/2);

    // Add imageView with undo
    editor.undoerSetUndoTitle("Add Image");
    parent.addChild(imageView);
    
    // Select imageView and SelectTool
    editor.setSelectedShape(imageView);*/
    editor.setCurrentToolToSelectTool();
}

/**
 * Returns a clone of a gallery view (hook to allow extra configuration for subclasses).
 */
public View getGalleryClone(T aView)  { return null; }//return aView.cloneDeep(); }

/**
 * Returns the image used to represent views that this tool represents.
 */
public Image getImage()
{
    for(Class c=getClass(); c!=ViewTool.class; c=c.getSuperclass()) {
        String name = c.getSimpleName().replace("Tool", "") + ".png";
        Image image = Image.get(c, name);
        if(image!=null) return image;
    }
    return Image.get(ViewTool.class, "RMShape.png");
}

/**
 * Returns the specific tool for a given view.
 */
public static ViewTool createTool(Class aClass)
{
    // Handle root
    if(aClass==null)
        aClass = View.class;
    if(aClass==View.class) return new ViewTool();
    
    // Check tool package for built-in View tools
    String cname = aClass.getSimpleName();
    Class tclass = ClassUtils.getClass("studio.apptools." + cname + "Tool");
    if(tclass==null && cname.endsWith("View"))
        tclass = ClassUtils.getClass("studio.apptools." + cname.replace("View", "Tool"));

    // If not found, try looking for inner class named "Tool"
    if(tclass==null)
        tclass = ClassUtils.getClass(aClass.getName() + "$" + "Tool", aClass);
    
    // If tool class found, instantiate tool class
    if(tclass!=null)
        try { return (ViewTool)tclass.newInstance(); }
        catch(Exception e) { throw new RuntimeException(e); }
        
    // Otherwise, get tool for super class
    return createTool(aClass.getSuperclass());
}

/**
 * A class describing a view handle for resizing operations.
 */
public static class ViewHandle <T extends View> {

    // The view, handle index, handle position, anchor point and shape tool
    public T         view;
    public int       index;
    public Pos       handle;
    public Point     anchor;
    public ViewTool  tool;
    
    /** Creates a new shape-handle. */
    public ViewHandle(T aView, Pos aHndl, ViewTool aTool)
    {
        view = aView; handle = aHndl; tool = aTool;
        for(int i=0;i<tool.getHandleCount(view);i++)
            if(tool.getHandlePos(view,i)==handle) { index = i; break; }
        anchor = tool.getHandlePoint(view, handle.getOpposing(), false);
        anchor = view.localToParent(anchor.x, anchor.y);
    }
}

}