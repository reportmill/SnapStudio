package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This class subclasses RMViewer to support RMDocument editing.
 */
public class Editor extends Viewer implements DeepChangeListener {

    // Whether we're really editing
    boolean             _editing = true;
    
    // List of currently selected shapes
    List <View>      _selectedShapes = new ArrayList();
    
    // List of super selected shapes (all ancestors of selected shapes)
    List <ParentView>  _superSelectedShapes = new ArrayList();
    
    // The last shape that was copied to the clipboard (used for smart paste)
    View             _lastCopyShape;
    
    // The last shape that was pasted from the clipboard (used for smart paste)
    View             _lastPasteShape;
    
    // A helper class providing utilities for shape
    //EditorShapes      _shapesHelper = createShapesHelper();
    
    // A helper class to handle drag and drop
    EditorDnD         _dragHelper = createDragHelper();
    
    // A shape to be drawn if set to drag-over shape during drag and drop
    Shape               _dragShape;
    
    // Whether editor is in mouse loop
    boolean             _isMouseDown;
    
    // The select tool
    SelectTool        _selectTool;
    
    // Map of tool instances by shape class
    Map <Class, ViewTool> _tools = new HashMap();
    
    // The current editor tool
    ViewTool              _currentTool = getSelectTool();
    
    // The undoer
    Undoer                _undoer = new Undoer();

    // Constants for PropertyChanges
    public static final String CurrentTool_Prop = "CurrentTool";
    public static final String SelectedShapes_Prop = "SelectedShapes";
    public static final String SuperSelectedShape_Prop = "SuperSelectedShape";
    
/**
 * Creates a new editor.
 */
public Editor()
{
    // SuperSelect ViewerShape
    //setSuperSelectedShape(getViewerShape());
    
    // Enable Drag events
    enableEvents(DragEvents);
    
    // Enable ToolTips so getToolTip gets called and disable FocusKeys so tab doesn't leave editor
    setToolTipEnabled(true);
    setFocusKeysEnabled(false);
}

/**
 * Returns the editor pane for this editor, if there is one.
 */
public EditorPane getEditorPane()  { return _ep!=null? _ep : (_ep=getOwner(EditorPane.class)); } EditorPane _ep;

/**
 * Sets the root shape that is the content of this viewer.
 */
public void setContent(View aView)
{
    // If already set, just return
    if(aView==getContent()) return; super.setContent(aView);
    setSuperSelectedShape(aView);
}

/**
 * Returns the content XML.
 */
public XMLElement getContentXML()
{
    ViewArchiver varch = new ViewArchiver();
    return varch.writeObject(getContent());
}

/**
 * Returns whether the editor is in mouse drag loop.
 */
public boolean isMouseDown()  { return _isMouseDown; }

/**
 * Returns the text editor (or null if not editing).
 */
public View getTextEditor()  { return null; }
/*{
    RMShape shp = getSuperSelectedShape();
    return shp instanceof RMTextShape? ((RMTextShape)shp).getTextEditor() : null;
}*/

/**
 * Returns the shapes helper.
 */
//public EditorShapes getShapesHelper()  { return _shapesHelper; }

/**
 * Creates the shapes helper.
 */
//protected EditorShapes createShapesHelper()  { return new EditorShapes(this); }

/**
 * Creates the shapes helper.
 */
protected EditorDnD createDragHelper()  { return new EditorDnD(this); }

/**
 * Returns the first selected shape.
 */
public View getSelectedShape()  { return getSelectedShapeCount()==0? null : getSelectedShape(0); }

/**
 * Selects the given shape.
 */
public void setSelectedShape(View aShape)  { setSelectedShapes(aShape==null? null : Arrays.asList(aShape)); }

/**
 * Returns the number of selected shapes.
 */
public int getSelectedShapeCount()  { return _selectedShapes.size(); }

/**
 * Returns the selected shape at the given index.
 */
public View getSelectedShape(int anIndex)  { return ListUtils.get(_selectedShapes, anIndex); }

/**
 * Returns the selected shapes list.
 */
public List <View> getSelectedShapes()  { return _selectedShapes; }

/**
 * Selects the shapes in the given list.
 */
public void setSelectedShapes(List <View> theShapes)
{
    // If shapes already set, just return
    if(ListUtils.equalsId(theShapes, _selectedShapes)) return;
    
    // If shapes is null or empty super-select the selected page and return
    if(theShapes==null || theShapes.size()==0) {
        setSuperSelectedShape(getContent()); return; }
    
    // Get the first shape in given shapes list
    View shape = theShapes.get(0);
    
    // If shapes contains superSelectedShapes, superSelect last and return (hidden trick for undoSelectedObjects)
    if(theShapes.size()>1 && shape==getContent()) {
        setSuperSelectedShape(theShapes.get(theShapes.size()-1)); return; }
    
    // Get the shape's parent
    View shapesParent = shape.getParent();
    
    // If shapes parent is the document, super select shape instead
    //if(shapesParent==getDocument()) {
    //    setSuperSelectedShape(shape); return; }
    
    // Super select shapes parent
    setSuperSelectedShape(shapesParent);
    
    // Add shapes to selected list
    _selectedShapes.addAll(theShapes);
    
    // Fire PropertyChange
    firePropChange(SelectedShapes_Prop, null, theShapes);
}

/**
 * Add a shape to the selected shapes list.
 */
public void addSelectedShape(View aShape)
{
    List list = new ArrayList(getSelectedShapes()); list.add(aShape);
    setSelectedShapes(list);
}

/**
 * Remove a shape from the selected shapes list.
 */
public void removeSelectedShape(View aShape)
{
    List list = new ArrayList(getSelectedShapes()); list.remove(aShape);
    setSelectedShapes(list);
}

/**
 * Returns the first super-selected shape.
 */
public ParentView getSuperSelectedShape()
{
    return getSuperSelectedShapeCount()==0? null : getSuperSelectedShape(getSuperSelectedShapeCount()-1);
}

/**
 * Returns the first super selected shape, if parent shape.
 */
public ParentView getSuperSelectedParentShape()
{
    View ss = getSuperSelectedShape(); return ss instanceof ParentView? (ParentView)ss : null;
}

/**
 * Super select a shape.
 */
public void setSuperSelectedShape(View aShape)
{
    // If given shape is null, reset to selected page
    View shape = aShape!=null? aShape : getContent();
    
    // Unselect selected shapes
    _selectedShapes.clear();

    // Remove current super-selected shapes that aren't an ancestor of given shape   
    if(getSuperSelectedShape()!=null)
    while(shape!=getSuperSelectedShape() && !shape.isAncestor(getSuperSelectedShape())) {
        View ssShape = getSuperSelectedShape();
        getTool(ssShape).willLoseSuperSelected(ssShape);
        ListUtils.removeLast(_superSelectedShapes);
    }

    // Add super selected shape (recursively adds parents if missing)
    if(shape!=getSuperSelectedShape())
        addSuperSelectedShape(shape);
    
    // Fire PropertyChange and repaint
    firePropChange(SuperSelectedShape_Prop, null, aShape);
    repaint();
}

/**
 * Adds a super selected shape.
 */
private void addSuperSelectedShape(View aShape)
{
    // If parent isn't super selected, add parent first
    View contentBox = getContent().getParent();
    if(aShape.getParent()!=contentBox && !isSuperSelected(aShape.getParent()))
        addSuperSelectedShape(aShape.getParent());

    // Add ancestor to super selected list
    _superSelectedShapes.add((ParentView)aShape);
    
    // Notify tool
    getTool(aShape).didBecomeSuperSelected(aShape);

    // If ancestor is page but not document's selected page, make it the selected page
    //if(aShape instanceof RMPage && aShape!=getDocument().getSelectedPage())
    //    getDocument().setSelectedPage((RMPage)aShape);
}

/**
 * Returns whether a given shape is selected in the editor.
 */
public boolean isSelected(View aShape)  { return ListUtils.containsId(_selectedShapes, aShape); }

/**
 * Returns whether a given shape is super-selected in the editor.
 */
public boolean isSuperSelected(View aShape)  { return ListUtils.containsId(_superSelectedShapes, aShape); }

/**
 * Returns the number of super-selected shapes.
 */
public int getSuperSelectedShapeCount()  { return _superSelectedShapes.size(); }

/**
 * Returns the super-selected shape at the given index.
 */
public ParentView getSuperSelectedShape(int anIndex)  { return _superSelectedShapes.get(anIndex); }

/**
 * Returns the super selected shape list.
 */
public List <ParentView> getSuperSelectedShapes()  { return _superSelectedShapes; }

/**
 * Returns the number of currently selected shapes or simply 1, if a shape is super-selected.
 */
public int getSelectedOrSuperSelectedShapeCount()
{
    return getSelectedShapeCount()>0? getSelectedShapeCount() : 1;
}

/**
 * Returns the currently selected shape at the given index, or the super-selected shape.
 */
public View getSelectedOrSuperSelectedShape(int anIndex)
{
    return getSelectedShapeCount()>0? getSelectedShape(anIndex) : getSuperSelectedShape();
}

/**
 * Returns the currently selected shape or, if none, the super-selected shape.
 */
public View getSelectedOrSuperSelectedShape()
{
    return getSelectedShapeCount()>0? getSelectedShape() : getSuperSelectedShape();
}
    
/**
 * Returns the currently selected shapes or, if none, the super-selected shape in a list.
 */
public List <View> getSelectedOrSuperSelectedShapes()
{
    return getSelectedShapeCount()>0? _selectedShapes : Arrays.asList(getSuperSelectedShape());
}
    
/**
 * Un-SuperSelect currently super selected shape.
 */
public void popSelection()
{
    // If there are selected shapes, empty current selection
    if(getSelectedShapeCount()>0)
        setSuperSelectedShape(getSelectedShape().getParent());

    // Otherwise select super-selected shape (or its parent if it has childrenSuperSelectImmediately)
    else if(getSuperSelectedShapeCount()>1) {
        //if(getSuperSelectedShape() instanceof RMTextShape)
        //    setSelectedShape(getSuperSelectedShape());
        //else if(getSuperSelectedShape().getParent().childrenSuperSelectImmediately())
        //    setSuperSelectedShape(getSuperSelectedShape().getParent());
        //else setSelectedShape(getSuperSelectedShape());
    }
}

/**
 * Returns first shape hit by point given in View coords.
 */
public View getShapeAtPoint(double aX, double aY)  { return getShapeAtPoint(Point.get(aX,aY)); }

/**
 * Returns first shape hit by point given in View coords.
 */
public View getShapeAtPoint(Point aPoint)
{
    // Get superSelectedShape
    ParentView superSelectedShape = getSuperSelectedShape();
    
    // If superSelectedShape is document, start with the selected page instead (maybe should go)
    //if(superSelectedShape==getDocument())  superSelectedShape = getSelectedPage();

    // Get the point in superSelectedShape's coords
    Point point = convertToShape(superSelectedShape, aPoint.x, aPoint.y);

    // Get child of superSelectedShape hit by point
    View shapeAtPoint = getChildShapeAtPoint(superSelectedShape, point);
    
    // If no superSelectedShape child hit by point, find first superSelectedShape that is hit & set to shapeAtPoint
    while(superSelectedShape!=null && shapeAtPoint==null) {
        superSelectedShape.localToParent(point.x, point.y);
        superSelectedShape = superSelectedShape.getParent();
        shapeAtPoint = getChildShapeAtPoint(superSelectedShape, point);
    }

    // See if point really hits an upper level shape that overlaps shapeAtPoint
    if(shapeAtPoint!=null && shapeAtPoint!=getContent()) {
        
        // Declare shape/point variables used to iterate up shape hierarchy
        View ssShape = shapeAtPoint;
        Point pnt = point;

        // Iterate up shape hierarchy
        while(ssShape!=getContent() && ssShape.getParent()!=null) {
            
            // Get child of parent hit point point
            View hitChild = getChildShapeAtPoint(ssShape.getParent(), pnt);
            
            // If child not equal to original shape, change shapeAtPoint
            if(hitChild != ssShape) {
                superSelectedShape = ssShape.getParent();
                shapeAtPoint = hitChild;
                point = pnt;
            }
            
            // Update loop shape/point variables
            ssShape = ssShape.getParent();
            pnt = ssShape.localToParent(pnt.x, pnt.y);
        }
    }

    // Make sure page is worst case
    //if(shapeAtPoint==null || shapeAtPoint==getDocument()) shapeAtPoint = getSelectedPage();

    // Return shape at point
    return shapeAtPoint;
}

/**
 * Returns the child of the given shape hit by the given point.
 */
public View getChildShapeAtPoint(ParentView aShape, Point aPoint)
{
    // If given shape is null, return null
    if(aShape==null) return null;
    
    // Iterate over shape children
    for(int i=aShape.getChildCount(); i>0; i--) { View child = aShape.getChild(i-1);
        
        // If not hittable, continue
        //if(!child.isHittable()) continue;
        
        // Get given point in child shape coords
        Point point = child.parentToLocal(aPoint.x, aPoint.y);

        // If child is super selected and point is in child super selected bounds, return child
        if(isSuperSelected(child) &&
            getTool(child).getBoundsSuperSelected(child).contains(point.getX(), point.getY()))
            return child;
        
        // If child isn't super selected and contains point, return child
        else if(child.contains(point.x, point.y))
            return child;
    }
    
    // Return null if no children hit by point
    return null;
}

/**
 * Returns the first SuperSelectedShape that accepts children.
 */
public ParentView firstSuperSelectedShapeThatAcceptsChildren()
{
    // Get super selected shape
    View shape = getSuperSelectedShape();
    ParentView parent = shape instanceof ChildView? (ChildView)shape : shape.getParent();

    // Iterate up hierarchy until we find a shape that acceptsChildren
    while(!getTool(parent).getAcceptsChildren(parent))
        parent = parent.getParent();

    // Make sure page is worst case
    //if(parent==getDocument())
    //    parent = getSelectedPage();

    // Return parent
    return parent;
}

/**
 * Returns the first SuperSelected shape that accepts children at a given point.
 */
public ParentView firstSuperSelectedShapeThatAcceptsChildrenAtPoint(Point aPoint)
{
    // Go up chain of superSelectedShapes until one acceptsChildren and is hit by aPoint
    View shape = getSuperSelectedShape();
    ParentView parent = shape instanceof ParentView? (ParentView)shape : shape.getParent();

    // Iterate up shape hierarchy until we find a shape that is hit and accepts children
    while(!getTool(parent).getAcceptsChildren(parent) ||
        !parent.contains(parent.parentToLocal(this, aPoint.x, aPoint.y))) {

        // If shape childrenSuperSelImmd and shape hitByPt, see if any shape children qualify (otherwise use parent)
        if(getTool(parent).childrenSuperSelectImmediately(parent) && parent.contains(parent.parentToLocal(this, aPoint.x, aPoint.y))) {
            View childShape = parent.getChildAt(parent.parentToLocal(this,aPoint.x,aPoint.y));
            if(childShape!=null && getTool(childShape).getAcceptsChildren(childShape))
                parent = (ParentView)childShape;
            else parent = parent.getParent();
        }

        // If shape's children don't superSelectImmediately or it is not hit by aPoint, just go up parent chain
        else parent = parent.getParent();

        //if(parent==null)
        //    return getSelectedPage();
    }

    // Make sure page is worst case
    //if(parent==getDocument())
    //    parent = getSelectedPage();

    // Return shape
    return parent;
}

/**
 * Standard clipboard cut functionality.
 */
public void cut()  { EditorClipboard.cut(this); }

/**
 * Standard clipboard copy functionality.
 */
public void copy()  { EditorClipboard.copy(this); }

/**
 * Standard clipbard paste functionality.
 */
public void paste()  { EditorClipboard.paste(this); }

/**
 * Causes all the children of the current super selected shape to become selected.
 */
public void selectAll()
{
    // If text editing, forward to text editor
    //if(getTextEditor()!=null)
    //    getTextEditor().selectAll();
    
    // Otherwise, select all children
    /*else*/ if(getSuperSelectedShape().getChildCount()>0) {
        
        // Get list of all hittable children of super-selected shape
        List shapes = new ArrayList();
        for(View shape : getSuperSelectedShape().getChildren())
                shapes.add(shape); //if(shape.isHittable())
        
        // Select shapes
        setSelectedShapes(shapes);
    }
}

/**
 * Deletes all the currently selected shapes.
 */
public void delete()
{
    // Get copy of selected shapes (just beep and return if no selected shapes)
    View shapes[] = _selectedShapes.toArray(new View[0]);
    if(shapes.length==0) { if(getTextEditor()==null) beep(); return; }

    // Get/superSelect parent of selected shapes
    ParentView parent = getSelectedShape().getParent(); if(parent==null) return;
    setSuperSelectedShape(parent);

    // Set undo title
    undoerSetUndoTitle(getSelectedShapeCount()>1? "Delete Shapes" : "Delete Shape");
    
    // Remove all shapes from their parent
    for(View shape : shapes) {
        ((ChildView)parent).removeChild(shape);
        if(_lastPasteShape==shape) _lastPasteShape = null;
        if(_lastCopyShape==shape) _lastCopyShape = null;
    }
}

/**
 * Adds shapes as children to given shape.
 */
public void addShapesToShape(List <? extends View> theShapes, ParentView aShape, boolean withCorrection)
{
    // If no shapes, just return
    if(theShapes.size()==0) return;
    
    // Declare variables for dx, dy, dr
    double dx = 0, dy = 0, dr = 0;

    // Smart paste
    if(withCorrection) {

        // If there is an last-copy-shape and new shapes will be it's peer, set offset
        if(_lastCopyShape!=null && _lastCopyShape.getParent()==aShape) {

            if(_lastPasteShape!=null) {
                View firstShape = theShapes.get(0);
                dx = 2*_lastPasteShape.getX() - _lastCopyShape.getX() - firstShape.getX(); // was x()
                dy = 2*_lastPasteShape.getY() - _lastCopyShape.getY() - firstShape.getY(); // was y()
                dr = 2*_lastPasteShape.getRotate() - _lastCopyShape.getRotate() - firstShape.getRotate();
            }

            else dx = dy = 9;//getViewerShape().getGridSpacing();
        }
    }

    // Get each individual shape and add it to the superSelectedShape
    for(int i=0, iMax=theShapes.size(); i<iMax; i++) { View shape = theShapes.get(i);
        
        // Add current loop shape to given parent shape
        ((ChildView)aShape).addChild(shape);

        // Smart paste
        if(withCorrection) {
            Rect parentShapeRect = aShape.getBoundsInside();
            shape.setXY(shape.getX() + dx, shape.getY() + dy); // was x(), y()
            shape.setRotate(shape.getRotate() + dr);
            Rect rect = shape.getBounds(); // was getFrame()
            rect.width = Math.max(1, rect.width);
            rect.height = Math.max(1, rect.height);
            if(!parentShapeRect.intersectsRect(rect))
                shape.setXY(0, 0);
        }
    }
}

/**
 * Returns the SelectTool.
 */
public SelectTool getSelectTool()
{
    if(_selectTool!=null) return _selectTool;
    _selectTool = new SelectTool(); _selectTool.setEditor(this);
    return _selectTool;
}

/**
 * Returns the specific tool for a list of shapes (if they have the same tool).
 */
public ViewTool getTool(List aList)
{
    Class commonClass = ClassUtils.getCommonClass(aList); // Get class for first object
    return getTool(commonClass); // Return tool for common class
}

/**
 * Returns the specific tool for a given shape.
 */
public ViewTool getTool(Object anObj)
{
    // Get the shape class and tool from tools map - if not there, find and set
    Class sclass = ClassUtils.getClass(anObj);
    ViewTool tool = _tools.get(sclass);
    if(tool==null) {
        _tools.put(sclass, tool = ViewTool.createTool(sclass));
        tool.setEditor(this);
    }
    return tool;
}

/**
 * Tool method - returns the currently selected tool.
 */
public ViewTool getCurrentTool()  { return _currentTool; }

/**
 * Tool method - sets the currently select tool to the given tool.
 */
public void setCurrentTool(ViewTool aTool)
{
    // If tool is already current tool, just reactivate and return
    if(aTool==_currentTool) {
        aTool.reactivateTool(); return; }

    // Deactivate current tool and reset to new tool
    _currentTool.deactivateTool();
    
    // Set new current tool
    firePropChange(CurrentTool_Prop, _currentTool, _currentTool = aTool);
        
    // Activate new tool and have editor repaint
    _currentTool.activateTool();
        
    // Repaint editor
    repaint();
}

/**
 * Returns whether the select tool is currently selected.
 */
public boolean isCurrentToolSelectTool()  { return _currentTool==getSelectTool(); }

/**
 * Sets the current tool to the select tool.
 */
public void setCurrentToolToSelectTool()
{
    if(getCurrentTool()!=getSelectTool())
        setCurrentTool(getSelectTool());
}

/**
 * Tool method - Returns whether the select tool is currently selected and if it's currently being used to select.
 */
public boolean isCurrentToolSelectToolAndSelecting()
{
    return isCurrentToolSelectTool() && getSelectTool().getDragMode()==SelectTool.DragMode.Select;
}

/**
 * Resets the currently selected tool.
 */
public void resetCurrentTool()
{
    _currentTool.deactivateTool();
    _currentTool.activateTool();
}

/**
 * Scrolls selected shapes to visible.
 */
public Rect getSelectedShapesBounds()
{
    // Get selected/super-selected shape(s) and parent (just return if parent is null or document)
    List <? extends View> shapes = getSelectedOrSuperSelectedShapes();
    View parent = shapes.get(0).getParent();
    //if(parent==null || parent instanceof RMDocument)
    //    return getDocBounds();
    
    // Get select shapes rect in viewer coords and return
    Rect sbounds = shapes.get(0).getBounds(); //RMShapeUtils.getBoundsOfChildren(parent, shapes);
    sbounds = parent.localToParent(this, sbounds).getBounds();
    return sbounds;
}

/**
 * Override to have zoom focus on selected shapes rect.
 */
public Rect getZoomFocusRect()
{
    Rect sbounds = getSelectedShapesBounds();
    Rect vrect = getVisRect();
    sbounds.inset((sbounds.getWidth() - vrect.getWidth())/2, (sbounds.getHeight() - vrect.getHeight())/2);
    return sbounds;
}

/**
 * Override to return Undoer.
 */
public Undoer getUndoer()  { return _undoer; }

/**
 * Override to paint handles, margin, grid, etc.
 */
public void paintAbove(Painter aPntr)
{
    // Do normal paint
    super.paintAbove(aPntr);
    
    // Draw selection handles and current tool
    if(getCurrentTool()!=getSelectTool())
        getSelectTool().paintTool(aPntr);
    getCurrentTool().paintTool(aPntr);
   
    // Paint proximity guides
    EditorProxGuide.paintProximityGuides(this, aPntr);
    
    // Paint DragShape, if set
    if(_dragShape!=null) {
        aPntr.setColor(new Color(0,.6,1,.5)); aPntr.setStrokeWidth(3); aPntr.draw(_dragShape); }
}

/**
 * Override to revalidate when ideal size changes.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Do normal version
    super.processEvent(anEvent);
    
    getSelectTool().processEvent(anEvent);
    
    // Handle DragEvent
    if(anEvent.isDragEvent())
        _dragHelper.processEvent(anEvent);
        
    // See if zoom needs to be reset for any input events
    else if(anEvent.isMouseDragged() || anEvent.isMouseReleased() || anEvent.isKeyReleased()) {
        
        // If zoom to factor, revalidate when preferred size changes
        if(isZoomToFactor()) {
            if(!getSize().equals(getPrefSize()))
                relayout();
            if(!getVisRect().contains(getSelectedShapesBounds()) &&
                getSelectTool().getDragMode()==SelectTool.DragMode.Move)
                setVisRect(getSelectedShapesBounds());
        }
        
        // If zoom to fit, update zoom to fit factor (just returns if unchanged)
        else setZoomToFitFactor();
    }        
}

/**
 * Called to undo the last edit operation in the editor.
 */
public void undo()
{
    // If undoer exists, do undo, select shapes and repaint
    if(getUndoer()!=null && getUndoer().getUndoSetLast()!=null) {
        UndoSet undoSet = getUndoer().undo();
        setUndoSelection(undoSet.getUndoSelection());
        repaint();
    }

    // Otherwise beep
    else beep();
}

/**
 * Called to redo the last undo operation in the editor.
 */
public void redo()
{
    // If undoer exists, do undo, select shapes and repaint
    if(getUndoer()!=null && getUndoer().getRedoSetLast()!=null) {
        UndoSet redoSet = getUndoer().redo();
        setUndoSelection(redoSet.getRedoSelection());
        repaint();
    }

    // Otherwise beep
    else beep();
}

/**
 * Sets the undo selection.
 */
protected void setUndoSelection(Object aSelection)
{
    // Handle List <RMShape>
    if(aSelection instanceof List)
        setSelectedShapes((List)aSelection);
}

/**
 * Property change.
 */
public void deepChange(PropChangeListener aShape, PropChange anEvent)
{
    // If deep change for EditorTextEditor, just return since it registers Undo itself (with better coalesce)
    //if(getTextEditor()!=null && getTextEditor().getTextShape()==aShape &&
    //    (anEvent.getSource() instanceof RMXString || anEvent.getSource() instanceof RMXStringRun)) return;
    
    // If undoer exists, set selected objects and add property change
    Undoer undoer = getUndoer();
    if(undoer!=null) {
        
        // If no changes yet, set selected objects
        if(undoer.getActiveUndoSet().getChangeCount()==0)
            undoer.setUndoSelection(new ArrayList(getSelectedOrSuperSelectedShapes()));
        
        // Add property change
        undoer.addPropertyChange(anEvent);
        
        // If adding child, add to child animator newborns
        String pname = anEvent.getPropertyName();
        if(pname.equals("Child") && anEvent.getNewValue()!=null) {
            View parent = (View)anEvent.getSource(), child = (View)anEvent.getNewValue();
            //if(parent.getChildAnimator()!=null) parent.getChildAnimator().addNewborn(child);
        }
        
        // Save UndoerChanges after delay
        saveUndoerChangesLater();
    }
    
    // Forward DeepChanges to EditorPane. Should have add/removeDeepChagneLister methods for this.
    //RMEditorPane ep = getEditorPane(); if(ep!=null) ep.deepChange(this, anEvent);
}

/**
 * Saves Undo Changes.
 */
protected void saveUndoerChanges()
{
    // If MouseIsDown, come back later
    if(ViewUtils.isMouseDown()) {
        saveUndoerChangesLater(); return; }

    // Get undoer
    Undoer undoer = getUndoer(); if(undoer==null || !undoer.isEnabled()) return;
    
    // Set undo selected-shapes
    List shapes = getSelectedShapeCount()>0? getSelectedShapes() : getSuperSelectedShapes();
    if(undoer.getRedoSelection()==null)
        undoer.setRedoSelection(new ArrayList(shapes));
    
    // Save undo changes
    undoer.saveChanges();
    
    // Re-enable animator
    //View shape = getSelectedOrSuperSelectedShape();
    //if(shape.getAnimator()!=null) shape.getAnimator().setEnabled(true);
}

/**
 * Saves undo changes after a delay.
 */
protected void saveUndoerChangesLater()  { getEnv().runLaterOnce("SaveChangesLater", _saveChangesRunnable); }
private Runnable _saveChangesRunnable = () -> saveUndoerChanges();

/** Override to make editor want to be 600x600. */
protected double getPrefWidthImpl(double aH)  { return Math.max(super.getPrefWidthImpl(aH),500); }

/** Play beep. */
public void beep()  { ViewUtils.beep(); }

}