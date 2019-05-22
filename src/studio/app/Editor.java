package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.WebFile;

/**
 * This class subclasses Viewer to support snp file editing.
 */
public class Editor extends Viewer implements DeepChangeListener {

    // Whether we're really editing
    boolean            _editing = true;
    
    // List of currently selected views
    List <View>        _selectedViews = new ArrayList();
    
    // List of super selected views (all ancestors of selected views)
    List <View>        _superSelectedViews = new ArrayList();
    
    // The last view that was copied to the clipboard (used for smart paste)
    View               _lastCopyView;
    
    // The last view that was pasted from the clipboard (used for smart paste)
    View               _lastPasteView;
    
    // An object to handle events
    EditorEvents       _events = new EditorEvents(this);
    
    // A helper class to handle drag and drop
    EditorDnD          _dragHelper = createDragHelper();
    
    // A shape to be drawn if set to drag-over shape during drag and drop
    Shape              _dragView;
    
    // Whether editor is in mouse loop
    boolean            _isMouseDown;
    
    // The select tool
    SelectTool         _selectTool;
    
    // Map of tool instances by view class
    Map <Class, ViewTool> _tools = new HashMap();
    
    // The current editor tool
    ViewTool              _currentTool = getSelectTool();
    
    // The undoer
    Undoer                _undoer = new Undoer();
    
    // The current time
    int                   _time;
    
    // A ViewUpdater.Listener to expand repaint bounds
    ViewUpdater.Listener  _updaterLsnr = (rv,r) -> rootViewWillPaint(rv,r);

    // Constants for PropertyChanges
    public static final String CurrentTool_Prop = "CurrentTool";
    public static final String SelectedViews_Prop = "SelectedViews";
    public static final String SuperSelectedView_Prop = "SuperSelectedView";
    
/**
 * Creates a new editor.
 */
public Editor()
{
    // Enable Drag events
    enableEvents(DragEvents);
    
    // Enable ToolTips so getToolTip gets called and disable FocusKeys so tab doesn't leave editor
    setToolTipEnabled(true);
    setFocusKeysEnabled(false);
    
    // Listen for deep changes on ContentBox
    getContentBox().addDeepChangeListener(this);
}

/**
 * Returns the editor pane for this editor, if there is one.
 */
public EditorPane getEditorPane()  { return _ep!=null? _ep : (_ep=getOwner(EditorPane.class)); } EditorPane _ep;

/**
 * Returns the Editor events helper.
 */
public EditorEvents getEvents()  { return _events; }

/**
 * Sets the root view that is the content of this viewer.
 */
public void setContent(View aView)
{
    // If already set, just return
    if(aView==getContent()) return; super.setContent(aView);
    
    // Super-select new content
    setSuperSelectedView(aView);
    
    // If SnapScene, show CastPane
    if(aView instanceof snap.viewx.SnapScene)
        getEditorPane().getAttributesPanel().setVisibleName(AttributesPanel.CAST);
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
 * Returns the "Page" if content is DocView such that childrenSuperSelectImmediately.
 */
public ParentView getContentPage()
{
    ParentView content = getContent();
    if(getTool(content).childrenSuperSelectImmediately(content) && content.getChildCount()>0)
        return (ParentView)content.getChild(0);
    return null;
}

/**
 * The real save method.
 */
public void save() throws Exception
{
    // Get source file and save (update file might get called from here)
    updateFile();
    WebFile file = getSourceFile(true);
    file.save();
    
    // Clear undoer
    getUndoer().reset();
}

/**
 * Updates the source file from editor.
 */
public void updateFile()
{
    WebFile file = getSourceFile(true);
    XMLElement xml = getContentXML();
    byte bytes[] = xml.getBytes();
    file.setBytes(bytes);
}

/**
 * Returns whether the editor is in mouse drag loop.
 */
public boolean isMouseDown()  { return _isMouseDown; }

/**
 * Creates the drag helper.
 */
protected EditorDnD createDragHelper()  { return new EditorDnD(this); }

/**
 * Returns the first selected view.
 */
public View getSelectedView()  { return getSelectedViewCount()==0? null : getSelectedView(0); }

/**
 * Selects the given view.
 */
public void setSelectedView(View aView)  { setSelectedViews(aView==null? null : Arrays.asList(aView)); }

/**
 * Returns the number of selected views.
 */
public int getSelectedViewCount()  { return _selectedViews.size(); }

/**
 * Returns the selected view at the given index.
 */
public View getSelectedView(int anIndex)  { return ListUtils.get(_selectedViews, anIndex); }

/**
 * Returns the selected views list.
 */
public List <View> getSelectedViews()  { return _selectedViews; }

/**
 * Selects the views in given list.
 */
public void setSelectedViews(List <View> theViews)
{
    // If views already set, just return
    if(ListUtils.equalsId(theViews, _selectedViews)) return;
    
    // If views is null or empty super-select the selected page and return
    if(theViews==null || theViews.size()==0) {
        setSuperSelectedView(getContent()); return; }
    
    // Get the first view in given views list
    View view = theViews.get(0);
    
    // If views contains superSelectedViews, superSelect last and return (hidden trick for undoSelectedObjects)
    if(theViews.size()>1 && view==getContent()) {
        setSuperSelectedView(theViews.get(theViews.size()-1)); return; }
    
    // Get the view parent
    View parent = view.getParent();
    
    // If parent is the content, super select view instead
    if(view==getContent()) {
        setSuperSelectedView(view); return; }
    
    // Super select parent
    setSuperSelectedView(parent);
    
    // Add views to selected list
    _selectedViews.addAll(theViews);
    
    // Fire PropertyChange
    firePropChange(SelectedViews_Prop, null, theViews);
}

/**
 * Add a view to the selected views list.
 */
public void addSelectedView(View aView)
{
    List list = new ArrayList(getSelectedViews()); list.add(aView);
    setSelectedViews(list);
}

/**
 * Remove a view from the selected views list.
 */
public void removeSelectedView(View aView)
{
    List list = new ArrayList(getSelectedViews()); list.remove(aView);
    setSelectedViews(list);
}

/**
 * Returns the first super-selected view.
 */
public View getSuperSelectedView()
{
    return getSuperSelectedViewCount()==0? null : getSuperSelectedView(getSuperSelectedViewCount()-1);
}

/**
 * Returns the first super selected view, if parent view.
 */
public ParentView getSuperSelectedParentView()
{
    View ss = getSuperSelectedView(); return ss instanceof ParentView? (ParentView)ss : null;
}

/**
 * Returns the first super selected view, if child view.
 */
public ChildView getSuperSelectedChildView()
{
    View ss = getSuperSelectedView(); return ss instanceof ChildView? (ChildView)ss : null;
}

/**
 * Super select a view.
 */
public void setSuperSelectedView(View aView)
{
    // If given view is null, reset to selected page
    View view = aView!=null? aView : getContent();
    
    // Unselect selected views
    _selectedViews.clear();

    // Remove current super-selected views that aren't an ancestor of given view   
    while(view!=getSuperSelectedView() && getSuperSelectedView()!=null && !view.isAncestor(getSuperSelectedView())) {
        View ssView = getSuperSelectedView();
        getTool(ssView).willLoseSuperSelected(ssView);
        ListUtils.removeLast(_superSelectedViews);
    }

    // Add super selected view (recursively adds parents if missing)
    if(view!=getSuperSelectedView())
        addSuperSelectedView(view);
    
    // Fire PropertyChange and repaint
    firePropChange(SuperSelectedView_Prop, null, view);
    repaint();
}

/**
 * Adds a super selected view.
 */
private void addSuperSelectedView(View aView)
{
    // If parent isn't super selected, add parent first
    View contentBox = getContent().getParent();
    if(aView.getParent()!=contentBox && !isSuperSelected(aView.getParent()))
        addSuperSelectedView(aView.getParent());

    // Add ancestor to super selected list
    _superSelectedViews.add(aView);
    
    // Notify tool
    getTool(aView).didBecomeSuperSelected(aView);

    // If ancestor is page but not document's selected page, make it the selected page
    //if(aView instanceof RMPage && aView!=getDocument().getSelectedPage())
    //    getDocument().setSelectedPage((RMPage)aView);
}

/**
 * Returns whether a given view is selected in the editor.
 */
public boolean isSelected(View aView)  { return ListUtils.containsId(_selectedViews, aView); }

/**
 * Returns whether a given view is super-selected in the editor.
 */
public boolean isSuperSelected(View aView)  { return ListUtils.containsId(_superSelectedViews, aView); }

/**
 * Returns the number of super-selected views.
 */
public int getSuperSelectedViewCount()  { return _superSelectedViews.size(); }

/**
 * Returns the super-selected view at the given index.
 */
public View getSuperSelectedView(int anIndex)  { return _superSelectedViews.get(anIndex); }

/**
 * Returns the super selected view list.
 */
public List <View> getSuperSelectedViews()  { return _superSelectedViews; }

/**
 * Returns the number of currently selected views or simply 1, if a view is super-selected.
 */
public int getSelectedOrSuperSelectedViewCount()
{
    return getSelectedViewCount()>0? getSelectedViewCount() : 1;
}

/**
 * Returns the currently selected view at the given index, or the super-selected view.
 */
public View getSelectedOrSuperSelectedView(int anIndex)
{
    return getSelectedViewCount()>0? getSelectedView(anIndex) : getSuperSelectedView();
}

/**
 * Returns the currently selected view or, if none, the super-selected view.
 */
public View getSelectedOrSuperSelectedView()
{
    return getSelectedViewCount()>0? getSelectedView() : getSuperSelectedView();
}
    
/**
 * Returns the currently selected views or, if none, the super-selected view in a list.
 */
public List <View> getSelectedOrSuperSelectedViews()
{
    return getSelectedViewCount()>0? _selectedViews : Arrays.asList(getSuperSelectedView());
}

/**
 * Returns the super selected view tool.
 */
public ViewTool getSuperSelectedViewTool()
{
    View view = getSuperSelectedView();
    return getTool(view);
}
    
/**
 * Un-SuperSelect currently super selected view.
 */
public void popSelection()
{
    // If there are selected views, empty current selection
    if(getSelectedViewCount()>0)
        setSuperSelectedView(getSelectedView().getParent());

    // Otherwise select super-selected view (or its parent if it has childrenSuperSelectImmediately)
    else if(getSuperSelectedViewCount()>1) {
        View view = getSuperSelectedView(), parent = view.getParent();
        if(view instanceof TextView)
            setSelectedView(view);
        else if(getTool(parent).childrenSuperSelectImmediately(parent))
            setSuperSelectedView(parent);
        else setSelectedView(view);
    }
}

/**
 * Returns first view hit by point given in View coords.
 */
public View getViewAtPoint(double aX, double aY)  { return getViewAtPoint(new Point(aX,aY)); }

/**
 * Returns first view hit by point given in View coords.
 */
public View getViewAtPoint(Point aPoint)
{
    // Get superSelectedView
    View superSelView = getSuperSelectedView();
    
    // If superSelectedView is document, start with page instead (maybe should go)
    if(superSelView==getContent() && getContentPage()!=null)
        superSelView = getContentPage();

    // Get the point in superSelectedView's coords
    Point point = superSelView.parentToLocal(aPoint.x, aPoint.y, this);

    // Get child of superSelectedView hit by point
    View viewAtPoint = getChildViewAtPoint(superSelView, point);
    
    // If no superSelectedView child hit by point, find first superSelectedView that is hit & set to viewAtPoint
    while(superSelView!=getContent() && viewAtPoint==null) {
        point = superSelView.localToParent(point.x, point.y);
        superSelView = superSelView.getParent();
        viewAtPoint = getChildViewAtPoint(superSelView, point);
    }

    // See if point really hits an upper level view that overlaps viewAtPoint
    if(viewAtPoint!=null && viewAtPoint!=getContent()) {
        
        // Declare view/point variables used to iterate up view hierarchy
        View ssView = viewAtPoint;
        Point pnt = point;

        // Iterate up view hierarchy
        while(ssView!=getContent() && ssView.getParent()!=null) {
            
            // Get child of parent hit point point
            View hitChild = getChildViewAtPoint(ssView.getParent(), pnt);
            
            // If child not equal to original view, change viewAtPoint
            if(hitChild != ssView) {
                superSelView = ssView.getParent();
                viewAtPoint = hitChild; point = pnt;
            }
            
            // Update loop view/point variables
            ssView = ssView.getParent();
            pnt = ssView.localToParent(pnt.x, pnt.y);
        }
    }

    // Make sure page is worst case
    if(viewAtPoint==null) viewAtPoint = getContent();
    if(viewAtPoint==getContent() && getContentPage()!=null)
        viewAtPoint = getContentPage();

    // Return view at point
    return viewAtPoint;
}

/**
 * Returns the child of the given view hit by the given point.
 */
public View getChildViewAtPoint(View aView, Point aPoint)
{
    // If given view is null, return null
    if(aView==null) return null;
    ParentView parView = aView instanceof ParentView? (ParentView)aView : null; if(parView==null) return null;
    
    // Iterate over view children
    for(int i=parView.getChildCount(); i>0; i--) { View child = parView.getChild(i-1);
        
        // If not hittable, continue
        //if(!child.isHittable()) continue;
        
        // Get given point in child view coords
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
 * Returns the first SuperSelected view that accepts children.
 */
public ParentView firstSuperSelectedViewThatAcceptsChildren()
{
    // Get super selected view
    View view = getSuperSelectedView();
    ParentView parent = view instanceof ChildView? (ChildView)view : view.getParent();

    // Iterate up hierarchy until we find a view that acceptsChildren
    while(!getTool(parent).getAcceptsChildren(parent))
        parent = parent.getParent();

    // Make sure page is worst case
    if(parent==getContent() && getContentPage()!=null)
        parent = getContentPage();

    // Return parent
    return parent;
}

/**
 * Returns the first SuperSelected view that accepts children at a given point.
 */
public ParentView firstSuperSelectedViewThatAcceptsChildrenAtPoint(Point aPoint)
{
    // Go up chain of superSelectedViews until one acceptsChildren and is hit by aPoint
    View selView = getSuperSelectedView();
    ParentView parent = selView instanceof ParentView? (ParentView)selView : selView.getParent();

    // Iterate up view hierarchy until we find a view that is hit and accepts children
    while(!getTool(parent).getAcceptsChildren(parent) ||
        !parent.contains(parent.parentToLocal(aPoint.x, aPoint.y, this))) {

        // If view childrenSuperSelImmd and view hitByPt, see if any view children qualify (otherwise use parent)
        if(getTool(parent).childrenSuperSelectImmediately(parent) &&
            parent.contains(parent.parentToLocal(aPoint.x, aPoint.y, this))) {
            View childView = parent.getChildAt(parent.parentToLocal(aPoint.x,aPoint.y,this));
            if(childView!=null && getTool(childView).getAcceptsChildren(childView))
                parent = (ParentView)childView;
            else parent = parent.getParent();
        }

        // If view's children don't superSelectImmediately or it is not hit by aPoint, just go up parent chain
        else parent = parent.getParent();
    }

    // Make sure page is worst case
    if(parent==getContent() && getContentPage()!=null)
        parent = getContentPage();

    // Return view
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
 * Causes all the children of the current super selected view to become selected.
 */
public void selectAll()
{
    View selView = getSuperSelectedView();
    ParentView par = selView instanceof ParentView? (ParentView)selView : null; if(par==null) return;
    if(par.getChildCount()==0) return;
    
    // Get list of all hittable children of super-selected view
    List views = new ArrayList();
    for(View view : par.getChildren())
        views.add(view); //if(view.isHittable())
        
    // Select views
    setSelectedViews(views);
}

/**
 * Deletes all the currently selected views.
 */
public void delete()
{
    // Get copy of selected views (just beep and return if no selected views)
    View views[] = _selectedViews.toArray(new View[0]);
    if(views.length==0) { beep(); return; }

    // Get/superSelect parent of selected views
    ParentView parent = getSelectedView().getParent(); if(parent==null) return;
    setSuperSelectedView(parent);

    // Set undo title
    undoerSetUndoTitle(getSelectedViewCount()>1? "Delete Views" : "Delete View");
    
    // Remove all views from their parent
    for(View view : views) {
        ((ChildView)parent).removeChild(view);
        if(_lastPasteView==view) _lastPasteView = null;
        if(_lastCopyView==view) _lastCopyView = null;
    }
}

/**
 * Adds views as children to given view.
 */
public void addViewsToView(List <? extends View> theViews, ParentView aView, boolean withCorrection)
{
    // If no views, just return
    if(theViews.size()==0) return;
    
    // Declare variables for dx, dy, dr
    double dx = 0, dy = 0, dr = 0;

    // Smart paste
    if(withCorrection) {

        // If there is an last-copy-view and new views will be it's peer, set offset
        if(_lastCopyView!=null && _lastCopyView.getParent()==aView) {

            if(_lastPasteView!=null) {
                View firstView = theViews.get(0);
                dx = 2*_lastPasteView.getX() - _lastCopyView.getX() - firstView.getX(); // was x()
                dy = 2*_lastPasteView.getY() - _lastCopyView.getY() - firstView.getY(); // was y()
                dr = 2*_lastPasteView.getRotate() - _lastCopyView.getRotate() - firstView.getRotate();
            }

            else dx = dy = 9;//getViewerView().getGridSpacing();
        }
    }

    // Get each individual view and add it to the superSelectedView
    for(int i=0, iMax=theViews.size(); i<iMax; i++) { View view = theViews.get(i);
        
        // Add current loop view to given parent view
        ((ChildView)aView).addChild(view);

        // Smart paste
        if(withCorrection) {
            Rect parentViewRect = aView.getBoundsLocal();
            view.setXY(view.getX() + dx, view.getY() + dy); // was x(), y()
            view.setRotate(view.getRotate() + dr);
            Rect rect = view.getBounds(); // was getFrame()
            rect.width = Math.max(1, rect.width);
            rect.height = Math.max(1, rect.height);
            if(!parentViewRect.intersectsRect(rect))
                view.setXY(0, 0);
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
 * Returns the specific tool for a list of views (if they have the same tool).
 */
public ViewTool getTool(List aList)
{
    Class commonClass = ClassUtils.getCommonClass(aList); // Get class for first object
    return getTool(commonClass); // Return tool for common class
}

/**
 * Returns the specific tool for a given view.
 */
public ViewTool getTool(Object anObj)
{
    // Get the view class and tool from tools map - if not there, find and set
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
 * Scrolls selected views to visible.
 */
public Rect getSelectedViewsBounds()
{
    // Get selected/super-selected view(s) and parent (just return if parent is null or document)
    List <? extends View> views = getSelectedOrSuperSelectedViews();
    View parent = views.get(0).getParent();
    //if(parent==null || parent instanceof RMDocument)
    //    return getDocBounds();
    
    // Get select views rect in viewer coords and return
    Rect sbounds = views.get(0).getBounds(); //ViewUtils.getBoundsOfChildren(parent, views);
    sbounds = parent.localToParent(sbounds, this).getBounds();
    return sbounds;
}

/**
 * Override to have zoom focus on selected views rect.
 */
public Rect getZoomFocusRect()
{
    Rect sbounds = getSelectedViewsBounds();
    Rect vrect = getVisRect();
    sbounds.inset((sbounds.getWidth() - vrect.getWidth())/2, (sbounds.getHeight() - vrect.getHeight())/2);
    return sbounds;
}

/**
 * Override to return Undoer.
 */
public Undoer getUndoer()  { return _undoer; }

/**
 * Returns the current time (in milliseconds).
 */
public int getTime()  { return _time; }

/**
 * Sets the time of the current animator to the given time.
 */
public void setTime(int aTime)  //{ setTimeForScopedKeyFrame(aTime, null); }
{
    if(aTime==_time) return;
    firePropChange("Time", _time, _time = aTime);
    setTimeDeep(getContent(), _time);
}

/**
 * Sets the time on all anims.
 */
protected void setTimeDeep(View aView, int aTime)
{
    ViewAnim anim = aView.getAnim(-1);
    if(anim!=null) anim.setTime(aTime);
    if(aView instanceof ParentView) { ParentView par = (ParentView)aView;
        for(View child : par.getChildren())
            setTimeDeep(child, aTime); }
}

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
    if(_dragView!=null) {
        aPntr.setColor(new Color(0,.6,1,.5)); aPntr.setStrokeWidth(3); aPntr.draw(_dragView); }
}

/**
 * Override to revalidate when ideal size changes.
 */
protected void processEvent(ViewEvent anEvent)
{
    // Do normal version
    super.processEvent(anEvent);
    
    getEvents().processEvent(anEvent);
    
    // Handle DragEvent
    if(anEvent.isDragEvent())
        _dragHelper.processEvent(anEvent);
        
    // See if zoom needs to be reset for any input events
    else if(anEvent.isMouseDrag() || anEvent.isMouseRelease() || anEvent.isKeyRelease()) {
        
        // If zoom to factor, revalidate when preferred size changes
        if(isZoomToFactor()) {
            if(!getSize().equals(getPrefSize()))
                relayout();
            if(!getVisRect().contains(getSelectedViewsBounds()) &&
                getSelectTool().getDragMode()==SelectTool.DragMode.Move)
                scrollToVisible(getSelectedViewsBounds());
        }
        
        // If zoom to fit, update zoom to fit factor (just returns if unchanged)
        else setZoomToFitFactor();
    }        
}

/**
 * Override to install RootView.Listener.
 */
protected void setShowing(boolean aValue)
{
    if(aValue==isShowing()) return; super.setShowing(aValue);
    if(aValue) getUpdater().addListener(_updaterLsnr);
    else if(getUpdater()!=null) getUpdater().removeListener(_updaterLsnr);
}

/**
 * Called to undo the last edit operation in the editor.
 */
public void undo()
{
    // If undoer exists, do undo, select views and repaint
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
    // If undoer exists, do undo, select views and repaint
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
    // Handle List <View>
    if(aSelection instanceof List)
        setSelectedViews((List)aSelection);
}

/**
 * Property change.
 */
public void deepChange(Object aView, PropChange anEvent)
{
    // Get source and prop name (if not View, just return)
    Object src = anEvent.getSource();
    View view = (View)aView, sview = src instanceof View? (View)src : null; if(view==null) return;
    String pname = anEvent.getPropertyName();
    
    // Ignore properties: Showing, NeedsLayout
    if(pname==Showing_Prop) return;
    if(pname==NeedsLayout_Prop) return;
    if(pname==Parent_Prop) return;
    
    // Ignore layout changes
    if(view instanceof ParentView && ((ParentView)view).isInLayout()) return;
    if(sview instanceof ParentView && ((ParentView)sview).isInLayout()) return;
    
    // If undoer exists, set selected objects and add property change
    Undoer undoer = getUndoer();
    if(undoer!=null) {
        
        // If no changes yet, set selected objects
        if(undoer.getActiveUndoSet().getChangeCount()==0)
            undoer.setUndoSelection(new ArrayList(getSelectedOrSuperSelectedViews()));
        
        // Add property change
        undoer.addPropertyChange(anEvent);
        
        // If adding child, add to child animator newborns
        //String pname = anEvent.getPropertyName(); if(pname.equals("Child") && anEvent.getNewValue()!=null) {
        //    View parent = (View)src, child = (View)anEvent.getNewValue();
        //    if(parent.getChildAnimator()!=null) parent.getChildAnimator().addNewborn(child); }
        
        // Save UndoerChanges after delay
        saveUndoerChangesLater();
        
        // Set updator
        WebFile file = getSourceFile(false);
        if(file!=null) file.setUpdater(undoer.hasUndos()? _updr : null);
    }
    
    // Reset EditorPane
    EditorPane ep = getEditorPane(); if(ep!=null) ep.resetLater();
}

// A Shared updater to kick off save
private WebFile.Updater _updr = file -> updateFile();

/**
 * ViewUpdater.Listener method.
 */
public Rect rootViewWillPaint(RootView aRV, Rect aRect)
{
    Rect rect = parentToLocal(aRect, aRV).getBounds();
    if(rect.intersects(getBoundsLocal())) { rect.inset(-4); aRect = localToParent(rect, aRV).getBounds(); }
    return aRect;
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
    
    // Set undo selected-views
    List views = getSelectedViewCount()>0? getSelectedViews() : getSuperSelectedViews();
    if(undoer.getRedoSelection()==null)
        undoer.setRedoSelection(new ArrayList(views));
    
    // Save undo changes
    undoer.saveChanges();
    
    // Reset EditorPane
    EditorPane ep = getEditorPane(); if(ep!=null) ep.resetLater();
}

/**
 * Saves undo changes after a delay.
 */
protected void saveUndoerChangesLater()
{
    // If runnable already set, just return
    if(_saveChangesRun!=null) return; _saveChangesRun = _scrShared;
    
    // If MouseDown, run on mouse up, otherwise run later
    if(ViewUtils.isMouseDown()) ViewUtils.runOnMouseUp(_saveChangesRun);
    else getEnv().runLater(_saveChangesRun);
}

// A Runnable for runLater(saveUndoerChanges())
private Runnable _saveChangesRun, _scrShared = () -> { saveUndoerChanges(); _saveChangesRun = null; };

/** Override to make editor want to be 600x600. */
protected double getPrefWidthImpl(double aH)  { return Math.max(super.getPrefWidthImpl(aH),500); }

/** Play beep. */
public void beep()  { ViewUtils.beep(); }

}