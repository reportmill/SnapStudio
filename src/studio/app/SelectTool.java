package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.util.ListUtils;
import snap.view.*;

/**
 * This class handles mouse selection and manipulation of shapes, including:
 *   - Click on a shape selects a shape
 *   - Double click on a shape super-selects a shape
 *   - Drag a rect selects shapes
 *   - Shift click or shift drag XORs selection
 *   - Click and drag handle resizes shape
 */
public class SelectTool extends ViewTool {

    // The mode of current even loop (Move, Resize, etc.)
    DragMode      _dragMode = DragMode.None;
    
    // The point of last mouse
    Point         _lastMousePoint;
    
    // A construct representing a view whose handle was hit and the handle
    ViewHandle    _viewHandle;
    
    // The shape handling mouse events
    View          _eventShape;

    // The current selection rect (during DragModeSelect)
    Rect          _selectionRect = new Rect();
    
    // The list of shapes currently selected while selecting
    List <View>   _whileSelectingSelectedShapes = new Vector();
    
    // Whether to re-enter mouse pressed
    boolean       _redoMousePressed;

    // Drag mode constants
    public enum DragMode { None, Move, Rotate, Resize, Select, EventDispatch };

/**
 * Handles mouse pressed for the select tool.
 */
public void processEvent(ViewEvent anEvent)
{
    switch(anEvent.getType()) {
        case MousePress: mousePressed(anEvent); break;
        case MouseDrag: mouseDragged(anEvent); break;
        case MouseRelease: mouseReleased(anEvent); break;
        default: break;
    }
}

/**
 * Handles mouse pressed for the select tool.
 */
public void mousePressed(ViewEvent anEvent)
{
    // Get current editor
    Editor editor = getEditor();

    // Call setNeedsRepaint on superSelectedShapes to wipe out handles
    editor.getSuperSelectedViews().forEach(i -> i.repaint());

    // See if tool wants to handle this one
    ViewTool toolShared = editor.getTool(editor.getSelectedOrSuperSelectedViews());
    if(toolShared!=null && toolShared.mousePressedSelection(anEvent)) {
        _dragMode = DragMode.None; return; }
    
    // Reset re-enter flag
    _redoMousePressed = false;

    // Set downPoint to event location.
    _downPoint = getEventPointInDoc();
    
    // Get view handle for event point
    _viewHandle = getViewHandleAtPoint(anEvent.getPoint());

    // If view handle was found for event point, set mode to resize.
    if(_viewHandle!=null) {
        
        // Set DragMode to Resize
        _dragMode = DragMode.Resize;
        
        // Register view handle view for repaint
        _viewHandle.view.repaint();

        // If _selectedShape is superSelected, select it instead
        if(isSuperSelected(_viewHandle.view))
            editor.setSelectedView(_viewHandle.view);

        // Just return
        return;
    }
    
    // Get selected shape at event point
    View selectedShape = editor.getViewAtPoint(anEvent.getX(), anEvent.getY());
    
    // If hit shape is super selected, then forward the event
    if(isSuperSelected(selectedShape)) {

        // If selectedShape isn't editor superSelectedShape, superSelect it (ie., pop the selection)
        if(selectedShape != editor.getSuperSelectedView())
            editor.setSuperSelectedView(selectedShape);
        
        // Set drag mode to select
        _dragMode = DragMode.Select;
    }

    // If Multi-click and SelectedShape is super-selectable, super-select shape and redo with reduced clicks
    else if(anEvent.getClickCount()>1 && getTool(selectedShape).isSuperSelectable(selectedShape)) {
        editor.setSuperSelectedView(selectedShape);                               // Super select selectedShape
        ViewEvent event = anEvent.copyForClickCount(anEvent.getClickCount()-1);  // Get event with reduced clicks
        mousePressed(event); return;                                               // Re-enter and return
    }

    // If event was shift click, either add or remove hit shape from list
    else if(anEvent.isShiftDown()) {
            
        // If mouse pressed shape is already selected, remove it and reset drag mode to none
        if(isSelected(selectedShape)) {
            editor.removeSelectedView(selectedShape); _dragMode = DragMode.None; }
        
        // If shape wasn't yet selected, add it to selected shapes
        else { editor.addSelectedView(selectedShape); _dragMode = DragMode.Move; }
    }
        
    // Otherwise, handle normal mouse press on shape
    else {
        if(!isSelected(selectedShape))                                    // If hit shape isn't selected then select it
            editor.setSelectedView(selectedShape);
        _dragMode = !anEvent.isAltDown()? DragMode.Move : DragMode.Rotate;  // Set drag mode to move
    }
    
    // If a shape was selected whose parent childrenSuperSelectImmediately, go ahead and super select it
    if(editor.getSelectedView()!=null && editor.getSuperSelectedViewTool().childrenSuperSelectImmediately()) {
        editor.setSuperSelectedView(editor.getSelectedView());     // Super select selected shape
        mousePressed(anEvent); return;                               // Re-enter mouse pressed and return
    }
    
    // Set last point to event point in super selected shape coords
    _lastMousePoint = getEventPointInSuperSelectedView(false);
    
    // Get editor super selected shape
    View superSelectedShape = editor.getSuperSelectedView();
        
    // Call mouse pressed for superSelectedShape's tool
    getTool(superSelectedShape).processEvent(superSelectedShape, anEvent);
    
    // If redo mouse pressed was requested, do redo
    if(getRedoMousePressed()) {
        mousePressed(anEvent); return; }
        
    // If event was consumed, set event shape and DragMode to event dispatch and return
    if(anEvent.isConsumed()) {
        _eventShape = superSelectedShape; _dragMode = DragMode.EventDispatch; return; }
    
    // Get the shape at the event point
    View mousePressedShape = editor.getViewAtPoint(anEvent.getX(), anEvent.getY());
    
    // If mousePressedShape is the editor's selected shape, call mouse pressed on mousePressedShape's tool
    if(isSelected(mousePressedShape)) {
        
        // Call mouse pressed on mousePressedShape's tool
        getTool(mousePressedShape).processEvent(mousePressedShape, anEvent);
        
        // If redo mouse pressed was requested, do redo
        if(getRedoMousePressed()) {
            mousePressed(anEvent); return; }
            
        // If event was consumed, set event shape and drag mode to event dispatch and return
        if(anEvent.isConsumed()) {
            _eventShape = mousePressedShape; _dragMode = DragMode.EventDispatch; return; }
    }
}

/**
 * Handles mouse dragged for the select tool.
 */
public void mouseDragged(ViewEvent anEvent)
{
    // Get current editor
    Editor editor = getEditor(); View content = editor.getContent();
    
    // Holding ctrl down at any point during a drag prevents snapping 
    boolean shouldSnap = !anEvent.isControlDown();

    // Handle specific drag modes
    switch(_dragMode) {

        // Handle DragModeMove
        case Move:
            
            // Set undo title
            editor.undoerSetUndoTitle("Move");
            
            // Get SuperSelectedShape and disable ParentTracksBoundsOfChildren
            ParentView parent = editor.getSuperSelectedParentView();
            
            // Get event point in super selected shape coords
            Point point = getEventPointInSuperSelectedView(false);

            // Move shapes once to event point without SnapToGrid
            moveShapes(_lastMousePoint, point); _lastMousePoint = point;
            
            // Get event point snapped to grid & edges, since SnapEdges will now be valid
            //Point pointSnapped = getEventPointInShape(shouldSnap, shouldSnap);
            //Point pointSnappedDoc = parent.localToParent(content, pointSnapped.x, pointSnapped.y);
            
            // Move shapes again to snapped point and reset LastMousePoint again
            //moveShapes(point, pointSnapped);
            //_lastMousePoint = parent.parentToLocal(content, pointSnappedDoc.x, pointSnappedDoc.y);
            break;
            
        // Handle Rotate
        case Rotate:

            // Set Undo title
            editor.undoerSetUndoTitle("Rotate");
            Point point2 = getEventPointInSuperSelectedView(false);
            
            // Iterate over selected shapes and update roll
            for(View shape : editor.getSelectedViews()) { //if(shape.isLocked()) continue;
                shape.setRotate(shape.getRotate() + point2.getY() - _lastMousePoint.getY()); }

            // Reset last point and break
            _lastMousePoint = point2;
            break;

        // Handle DragModeResize
        case Resize:
            
            // Register undo title "Resize"
            editor.undoerSetUndoTitle("Resize");
            
            // Get event point in super selected shape coords snapped to grid 
            Point resizePoint = getEventPointInSuperSelectedView(shouldSnap);
            
            // Move handle to current point and break
            _viewHandle.tool.moveViewHandle(_viewHandle, resizePoint);
            if(_viewHandle.view.getParent() instanceof SpringView)
                ((SpringView)_viewHandle.view.getParent()).resetSpringInfo();
            break;

        // Handle DragModeSelect
        case Select:

            // Get current hit shapes
            List <View> newShapes = getHitShapes();
            
            // Repaint selected views and SelectionRect
            _whileSelectingSelectedShapes.forEach(i -> i.repaint());
            editor.repaint(editor.getContent().localToParent(editor,_selectionRect.getInsetRect(-2)).getBounds());
            
            // Get new _selectionRect and clear _whileSelectingSelectedShapes
            _selectionRect = Rect.get(_downPoint, editor.localToView(null, anEvent.getX(), anEvent.getY()));
            _whileSelectingSelectedShapes.clear();

            // If shift key was down, exclusive OR (xor) newShapes with selectedShapes
            if(anEvent.isShiftDown()) {
                List xor = ListUtils.clone(editor.getSelectedViews());
                ListUtils.xor(xor, newShapes);
                _whileSelectingSelectedShapes.addAll(xor);
            }
            
            // If shit key not down, select all new shapes
            else _whileSelectingSelectedShapes.addAll(newShapes);

            // Repaint selected views and SelectionRect
            _whileSelectingSelectedShapes.forEach(i -> i.repaint());
            editor.repaint(editor.getContent().localToParent(editor,_selectionRect.getInsetRect(-2)).getBounds());

            // break
            break;

        // Handle DragModeSuperSelect: Forward mouse drag on to super selected shape's mouse dragged and break
        case EventDispatch: getTool(_eventShape).processEvent(_eventShape, anEvent); break;

        // Handle DragModeNone
        case None: break;
    }
    
    // Create guidelines
    EditorProxGuide.createGuidelines(editor);
}

/**
 * Handles mouse released for the select tool.
 */
public void mouseReleased(ViewEvent anEvent)
{
    Editor editor = getEditor();
    
    // Handle DragModes
    switch(_dragMode) {

        // Handle Select
        case Select:
            
            // Get hit shapes
            List newShapes = getHitShapes();
            
            // If shift key was down, exclusive OR (xor) newShapes with selectedShapes. Else select new shapes
            if(newShapes.size()>0) {
                if(anEvent.isShiftDown()) {
                    List xor = ListUtils.clone(editor.getSelectedViews());
                    ListUtils.xor(xor, newShapes);
                    editor.setSelectedViews(xor);
                }
                else editor.setSelectedViews(newShapes);
            }
            
            // If no shapes were selected, clear selectedShapes
            else editor.setSuperSelectedView(editor.getSuperSelectedView());

            // Reset _whileSelectingSelectedShapes and _selectionRect since we don't need them anymore
            _whileSelectingSelectedShapes.clear();
            _selectionRect.setRect(0,0,0,0);
            break;

        // Handle EventDispatch
        case EventDispatch:
            getTool(_eventShape).processEvent(_eventShape, anEvent);
            _eventShape = null;
            break;
            
        // Handle others
        default: break;
    }
    
    // Clear proximity guidelines
    EditorProxGuide.clearGuidelines(editor);

    // Repaint editor
    editor.repaint();
    
    // Reset drag mode
    _dragMode = DragMode.None;
}

/**
 * Handles mouse moved - forward on to super selected shape tool.
 */
public void mouseMoved(ViewEvent anEvent)
{
    // Iterate over super selected shapes and forward mouseMoved for each shape
    Editor editor = getEditor();
    for(int i=1, iMax=editor.getSuperSelectedViewCount(); i<iMax && !anEvent.isConsumed(); i++) {
        View shape = editor.getSuperSelectedView(i);
        getTool(shape).mouseMoved(shape, anEvent);
    }
}

/**
 * Moves the currently selected shapes from a point to a point.
 */
private void moveShapes(Point fromPoint, Point toPoint)
{
    // Iterate over selected shapes
    for(int i=0, iMax=getEditor().getSelectedViewCount(); i<iMax; i++) {
        View shape = getEditor().getSelectedView(i); //if(shape.isLocked()) continue;
        double fx = fromPoint.getX(), fy = fromPoint.getY(), tx = toPoint.getX(), ty = toPoint.getY();
        shape.setXY(shape.getX() + tx - fx, shape.getY() + ty - fy); // Was setFrameXY, getFrameX/Y
    }
    View par = getEditor().getSuperSelectedView();
    if(par instanceof SpringView)
        ((SpringView)par).resetSpringInfo();
}

/**
 * Returns the list of shapes hit by the selection rect formed by the down point and current point.
 */
private List <View> getHitShapes()
{
    // Get selection path from rect around currentPoint and _downPoint
    Editor editor = getEditor(); View content = editor.getContent();
    ParentView superShape = editor.getSuperSelectedParentView(); if(superShape==null) return Collections.emptyList();
    Point curPoint = getEventPointInDoc();
    Rect selRect = Rect.get(curPoint, _downPoint);
    Shape path = superShape.parentToLocal(editor, selRect);

    // If selection rect is outside super selected shape, move up shape hierarchy
    while(superShape!=content &&
        !path.getBounds().intersectsEvenIfEmpty(getTool(superShape).getBoundsSuperSelected(superShape))) {
        ParentView parent = superShape.getParent();
        editor.setSuperSelectedView(parent);
        path = superShape.parentToLocal(path);
        superShape = parent;
    }

    // Make sure page is worst case
    if(superShape==content && editor.getContentPage()!=null) { superShape = editor.getContentPage();
        path = superShape.parentToLocal(selRect); editor.setSuperSelectedView(superShape); }

    // Returns the children of the super-selected shape that intersect selection path
    return superShape.getChildrenAt(path);
}

/**
 * Returns the last drag mode handled by the select tool.
 */
public DragMode getDragMode()  { return _dragMode; }

/**
 * Returns whether select tool should redo current mouse down.
 */
public boolean getRedoMousePressed()  { return _redoMousePressed; }

/**
 * Sets whether select tool should redo current mouse dwon.
 */
public void setRedoMousePressed(boolean aFlag)  { _redoMousePressed = aFlag; }

/**
 * Paints tool specific things, like handles.
 */
public void paintTool(Painter aPntr)
{
    // Iterate over super selected shapes and have tool paint SuperSelected
    Editor editor = getEditor();
    for(int i=1, iMax=editor.getSuperSelectedViewCount(); i<iMax; i++) {
        View shape = editor.getSuperSelectedView(i); ViewTool tool = getTool(shape);
        tool.paintHandles(shape, aPntr, true);
    }
    
    // Get selected shapes
    List <View> selectedShapes = editor.getSelectedViews();
    
    // If in mouse loop, substitute "while selecting shapes"
    if(editor.isMouseDown())
        selectedShapes = _whileSelectingSelectedShapes;

    // Iterate over SelectedShapes and have tool paint Selected
    for(int i=0, iMax=selectedShapes.size(); i<iMax; i++) { View shape = selectedShapes.get(i);
        ViewTool tool = getTool(shape);
        tool.paintHandles(shape, aPntr, false);
    }

    // Draw SelectionRect: light gray rect with darker border (semi transparent)
    if(!_selectionRect.isEmpty()) {
        Rect rect = editor.viewToLocal(null, _selectionRect).getBounds();
        aPntr.setColor(new Color(.9,.5)); aPntr.fill(rect);
        aPntr.setStroke(Stroke.Stroke1); aPntr.setColor(new Color(.6,.6)); aPntr.draw(rect);
    }
}

/**
 * Tool callback selects parent of selected shapes (or just shape, if it's super-selected).
 */
public void reactivateTool()  { getEditor().popSelection(); }

}