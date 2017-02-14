/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import studio.app.*;
import snap.gfx.*;
import snap.view.*;
import static studio.apptools.PathViewUtils.*;

/**
 * This class manages creation and editing of PathViews.
 */
public class PathViewTool <T extends PathView> extends ViewTool <T> {
    
    // The current path being added
    Path         _path;
    
    // Whether path should be smoothed on mouse up
    boolean      _smoothPathOnMouseUp;
    
    // Used to determine which path element to start smoothing from
    int          _pointCountOnMouseDown;
    
    // The point (in path coords) for new control point additions
    Point        _newPoint;
    
    // The current selected point index
    int          _selectedPointIndex;

/**
 * Override to return empty panel.
 */
protected View createUI()  { return new Label(); }

/**
 * Handles the pop-up menu
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle DeletePointMenuItem
    if(anEvent.equals("DeletePointMenuItem"))
        deleteSelectedPoint();
    
    // Handle AddPointMenuItem
    if(anEvent.equals("AddPointMenuItem"))
        addNewPoint();
}

/**
 * Returns the class that this tool is responsible for.
 */
public Class getViewClass()  { return PathView.class; }

/**
 * Returns a new instance of the view class that this tool is responsible for.
 */
protected T newInstance()  { T view = super.newInstance(); view.setBorder(Color.BLACK,1); return view; }

/**
 * Returns whether tool should smooth path segments during creation.
 */
public boolean getSmoothPath()  { return false; }

/**
 * Handles mouse pressed for polygon creation.
 */
public void mousePressed(ViewEvent anEvent)
{
    boolean smoothPath = getSmoothPath(); if(anEvent.isAltDown()) smoothPath = !smoothPath;
    Point point = getEditorEvents().getEventPointInDoc(!smoothPath);

    // Register all selectedViews dirty because their handles will probably need to be wiped out
    getEditor().getSelectedViews().forEach(i -> i.repaint());

    // If this is the first mouseDown of a new path, create path and add moveTo. Otherwise add lineTo to current path
    if(_path==null) { _path = new Path(); _path.moveTo(point.x, point.y); }
    else _path.lineTo(point.x, point.y);    

    // Get the value of _shouldSmoothPathOnMouseUp for the mouseDrag and store current pointCount
    _smoothPathOnMouseUp = smoothPath;
    _pointCountOnMouseDown = _path.getPointCount();

    Rect rect = _path.getBounds().getInsetRect(-10);
    rect = getEditor().viewToLocal(null, rect).getBounds();
    getEditor().repaint(rect);
}

/**
 * Handles mouse dragged for polygon creation.
 */
public void mouseDragged(ViewEvent anEvent)
{
    Point point = getEditorEvents().getEventPointInDoc(!_smoothPathOnMouseUp);
    Rect rect = _path.getBounds();

    if(_smoothPathOnMouseUp || _path.getPointCount()==1) _path.lineTo(point.x, point.y);
    else _path.setPoint(_path.getPointCount()-1, point.x, point.y);

    rect.union(_path.getBounds()); rect.inset(-10, -10);
    rect = getEditor().viewToLocal(null, rect).getBounds();
    getEditor().repaint(rect);
}

/**
 * Handles mouse released for polygon creation.
 */
public void mouseReleased(ViewEvent anEvent)
{
    if(_smoothPathOnMouseUp && _pointCountOnMouseDown<_path.getPointCount()) {
        getEditor().repaint();
        PathFitCurves.fitCurveFromPointIndex(_path, _pointCountOnMouseDown);
    }

    // Check to see if point landed in first point
    if(_path.getPointCount() > 2) {
        PathIter.Seg lastElmnt = _path.getSegLast();
        int lastPointIndex = _path.getPointCount() - (lastElmnt==Path.LineTo? 2 : 4);
        Point beginPoint = _path.getPoint(0);
        Point lastPoint = _path.getPoint(lastPointIndex);
        Point thisPoint = _path.getPointLast();
        Rect firstHandleRect = new Rect(beginPoint.x - 3, beginPoint.y - 3, 6f, 6f);
        Rect lastHandleRect = new Rect(lastPoint.x - 3, lastPoint.y - 3, 6f, 6f);
        Rect currentHandleRect = new Rect(thisPoint.x - 3, thisPoint.y - 3, 6f, 6f);
        boolean createPath = false;

        // If mouseUp is in startPoint, create poly and surrender to selectTool
        if(currentHandleRect.intersectsRect(firstHandleRect)) {
            if(lastElmnt==Path.LineTo) _path.removeLastSeg();
            _path.close();
            createPath = true;
        }

        // If mouseUp is in startPoint, create poly and surrender to selectTool
        if(currentHandleRect.intersectsRect(lastHandleRect)) {
            if(_path.getSegLast() == Path.LineTo) _path.removeLastSeg();
            createPath = true;
        }
        
        // Create poly, register for redisplay and surrender to selectTool
        if(createPath) {
            createPoly();
            getEditor().repaint();
            getEditor().setCurrentToolToSelectTool();
        }
    }
}

/**
 * Event handling - overridden to maintain default cursor.
 */
public void mouseMoved(T aPathView, ViewEvent anEvent)
{
    // Get the mouse down point in view coords
    Point point = getEditor().localToView(aPathView, anEvent.getX(), anEvent.getY());
    
    // If control point is hit, change cursor to move
    Path path = aPathView.getPath(); Size size = new Size(9,9);
    if(handleAtPointForBounds(path, point, aPathView.getBoundsLocal(), _selectedPointIndex, size)>=0) {
        getEditor().setCursor(Cursor.MOVE);
        anEvent.consume();
    }
    
    // Otherwise, do normal mouse moved
    else super.mouseMoved(aPathView, anEvent);
}

/**
 * Event handling for view editing.
 */
public void mousePressed(T aPathView, ViewEvent anEvent)
{
    // If view isn't super selected, just return
    if(!isSuperSelected(aPathView)) return;
    
    // Get mouse down point in view coords (but don't snap to the grid)
    Point point = getEditorEvents().getEventPointInShape(false);
    
    // Register view for repaint
    aPathView.repaint();
    
    // check for degenerate path
    if(aPathView.getPath().getPointCount() < 2) 
        _selectedPointIndex = -1;
    
    // Otherwise, figure out the size of a handle in path coordinates and set index of path point hit by mouse down
    else {
        Size handles = new Size(9,9);
        int oldSelectedPt = _selectedPointIndex;
        int hp = handleAtPointForBounds(aPathView.getPath(), point, aPathView.getBoundsLocal(), oldSelectedPt, handles);
        _selectedPointIndex = hp;
    
        if(anEvent.isPopupTrigger())
            runContextMenu(aPathView, anEvent);
    }
    
    // Consume event
    anEvent.consume();
}

/**
 * Event handling for view editing.
 */
public void mouseDragged(T aPathView, ViewEvent anEvent)
{
    aPathView.repaint();
    if(_selectedPointIndex>=0) {
        Point point = getEditorEvents().getEventPointInShape(true);
        Path path = aPathView.getPath();
        point = pointInPathCoordsFromPoint(path, point, aPathView.getBoundsLocal());
        
        // Clone path, move control point & do all the other path funny business, reset path
        Path newPath = path.clone();
        setPointStructured(newPath, _selectedPointIndex, point);
        aPathView.resetPath(newPath);
    } 
}

/**
 * Actually creates a new path view from the path tool's current path.
 */
private void createPoly()
{
    if(_path!=null && _path.getPointCount()>2) {
        ParentView parent = getEditor().getSuperSelectedParentView();
        PathView pview = new PathView();
        Rect pbounds = parent.parentToLocal(getEditor(), _path.getBounds()).getBounds();
        pview.setBounds(pbounds); //pview.setFrame(pbounds);
        pview.setBorder(Color.BLACK, 1);
        pview.setPath(_path);

        // Add view to superSelectedView (within an undo grouping) and select
        getEditor().undoerSetUndoTitle("Add Polygon");
        getTool(parent).addChild(parent, pview);
        getEditor().setSelectedView(pview);
    }

    // Reset path
    _path = null;
}

/**
 * Overrides standard tool method to trigger polygon creation when the tool is deactivated.
 */
public void deactivateTool()  { createPoly(); }

/**
 * Overrides standard tool method to trigger polygon creation when the tool is reactivated.
 */
public void reactivateTool()  { createPoly(); }

/**
 * Editor method - called when an instance of this tool's view in de-super-selected.
 */
public void willLoseSuperSelected(T aView)
{
    super.willLoseSuperSelected(aView);
    _selectedPointIndex = -1;
}

/**
 * Draws the polygon tool's path durring path creation.
 */
public void paintTool(Painter aPntr)
{
    if(_path!=null) {
        View page = getEditor().getContentPage(); if(page==null) page = getEditor().getContent();
        Rect pbounds = page.localToParent(getEditor(), page.getBoundsLocal()).getBounds();
        aPntr.translate(pbounds.getX(), pbounds.getY());
        aPntr.scale(getEditor().getZoomFactor(), getEditor().getZoomFactor());
        aPntr.setColor(Color.BLACK); aPntr.setStroke(Stroke.Stroke1); aPntr.draw(_path);
        aPntr.scale(1/getEditor().getZoomFactor(), 1/getEditor().getZoomFactor());
        aPntr.translate(-pbounds.getX(), -pbounds.getY());
    }
}

/**
 * Returns the bounds for this view when it's super-selected.
 */
public Rect getBoundsSuperSelected(T aView) 
{
    // Get view bounds and view path bounds
    Rect bounds = aView.getBoundsLocal();
    Rect pathBounds = aView.getPath().getBounds();

    // Get 
    double mx1 = pathBounds.getMidX(), my1 = pathBounds.getMidY();
    double mx2 = bounds.getMidX(), my2 = bounds.getMidY();
    double sx = pathBounds.width==0? 1f : bounds.width/pathBounds.width;
    double sy = pathBounds.height==0? 1f : bounds.height/pathBounds.height;

    // Scale pathSSBounds.origin by sx and sy and translate it to the bounding rect's origin
    Rect pathSSBounds = getControlPointBounds(aView.getPath());
    double x = (pathSSBounds.x-mx1)*sx + mx2;
    double y = (pathSSBounds.y-my1)*sy + my2;
    double w = bounds.width*pathSSBounds.width/pathBounds.width;
    double h = bounds.height*pathSSBounds.height/pathBounds.height;
    
    // Get super selected bounds, outset a bit and return
    Rect ssbounds = new Rect(x,y,w,h); ssbounds.inset(-3, -3); return ssbounds;
}

/**
 * Returns the bounds for all the control points.
 */
private Rect getControlPointBounds(Path path)
{
    // Get segment index for selected control point handle
    int mouseDownSegIndex = getSegIndexForPointIndex(path, _selectedPointIndex);
    if(mouseDownSegIndex>=0 && path.getSeg(mouseDownSegIndex)==Path.CubicTo &&
        path.getSegPointIndex(mouseDownSegIndex)==_selectedPointIndex)
        mouseDownSegIndex--;

    // Iterate over path elements
    Point p0 = path.getPointCount()>0? new Point(path.getPoint(0)) : new Point();
    double p1x = p0.x, p1y = p0.y, p2x = p1x, p2y = p1y;
    PathIter piter = path.getPathIter(null); double pts[] = new double[6];
    for(int i=0; piter.hasNext(); i++) switch(piter.getNext(pts)) {
        
        // Handle MoveTo
        case MoveTo:
            
        // Handle LineTo
        case LineTo: {
            p1x = Math.min(p1x, pts[0]); p1y = Math.min(p1y, pts[1]);
            p2x = Math.max(p2x, pts[0]); p2y = Math.max(p2y, pts[1]);
        } break;
        
        // Handle CubicTo
        case CubicTo: {
            if((i-1)==mouseDownSegIndex) {
                p1x = Math.min(p1x, pts[0]); p1y = Math.min(p1y, pts[1]);
                p2x = Math.max(p2x, pts[0]); p2y = Math.max(p2y, pts[1]);
            }
            if(i==mouseDownSegIndex) {
                p1x = Math.min(p1x, pts[2]); p1y = Math.min(p1y, pts[3]);
                p2x = Math.max(p2x, pts[2]); p2y = Math.max(p2y, pts[3]);
            }
            p1x = Math.min(p1x, pts[4]); p1y = Math.min(p1y, pts[5]);
            p2x = Math.max(p2x, pts[4]); p2y = Math.max(p2y, pts[5]);
        } break;
        
        // Handle default
        default: break;
    }
    
    // Create control point bounds rect, union with path bounds and return
    Rect cpbounds = new Rect(p1x, p1y, Math.max(1, p2x - p1x), Math.max(1, p2y - p1y));
    cpbounds.union(path.getBounds()); return cpbounds;
}

/**
 * Runs a context menu for the given event.
 */
public void runContextMenu(PathView aPathView, ViewEvent anEvent)
{
    // Get the handle that was clicked on
    Path path = aPathView.getPath();
    int pindex = _selectedPointIndex;
    String mtitle = null, mname = null;
    
    // If clicked on a valid handle, add 'delete point' to menu, 
    if(pindex>=0) {
        if(pointOnPath(path, pindex)) { // Only on-path points can be deleted
            mtitle = "Delete Anchor Point"; mname ="DeletePointMenuItem"; }
    }
    
    // Otherwise if the path itself was hit, use 'add point'
    else {
        // Convert event point to view coords
        _newPoint = getEditor().localToView(aPathView, anEvent.getX(), anEvent.getY());
        
        // linewidth is probably in view coords, and might need to get transformed to path coords here
        if(path.intersects(_newPoint.getX(), _newPoint.getY(), Math.max(aPathView.getBorder().getWidth(),8))) {
            mtitle = "Add Anchor Point"; mname = "AddPointMenuItem"; }
    }
    
    // return if there's nothing to be done
    if(mname==null) return;
    
    // Create new PopupMenu
    Menu pmenu = new Menu();
    MenuItem mitem = new MenuItem(); mitem.setText(mtitle); mitem.setName(mname); pmenu.addItem(mitem);
    pmenu.setOwner(this);
    pmenu.show(anEvent.getView(), anEvent.getX(), anEvent.getY());
}

/**
 * Delete the selected control point and readjust view bounds
 */
public void deleteSelectedPoint()
{
    // Make changes to a clone of the path so deletions can be undone
    PathView pview = getSelectedView();
    Path path = pview.getPath().clone();

    // get the index of the path element corresponding to the selected control point
    int elementIndex = getSegIndexForPointIndex(path, _selectedPointIndex);

    // mark for repaint & undo
    pview.repaint();

    // delete the point from path in parent coords
    path.removeSeg(elementIndex);

    // if all points have been removed, delete the view itself
    if (path.getSegCount()==0) {
        getEditor().undoerSetUndoTitle("Delete PathView");
        pview.getParent().repaint();
        getTool(pview.getParent()).removeChild(pview.getParent(), pview); //pview.removeFromParent();
        getEditor().setSelectedView(null);
    }
    
    // otherwise update path and bounds and deselect the deleted point
    else {
        getEditor().undoerSetUndoTitle("Delete Control Point");
        pview.resetPath(path);
        _selectedPointIndex = -1;
    }
}

/**
 * Add a point to the curve by subdividing the path segment at the hit point.
 */
public void addNewPoint()
{
    // Get all the segments as a list of subpaths
    /*PathView pview = getSelectedView();
    List <List<Line>> subpaths = (List)pview.getPath().getSubpathsSegments();
    
    // Find hitInfo of segment by intersecting with either horizontal or vertial line segment
    Line hor = new Line(_newPoint.getX()-2, _newPoint.getY(), _newPoint.getX()+2, _newPoint.getY());
    Line vert = new Line(_newPoint.getX(), _newPoint.getY()-2, _newPoint.getX(), _newPoint.getY()+2);
    
    // Iterate over subpaths
    for(int i=0, iMax=subpaths.size(); i<iMax; i++) { List <Line> subpath = subpaths.get(i);
    
        // Iterate over subpath segments
        for(int j=0, jMax=subpath.size(); j<jMax; j++) { Line segment = subpath.get(j);
        
            // Get hit info for segment
            RMHitInfo hit = segment.getHitInfo(hor);
            if (hit==null)
                hit = segment.getHitInfo(vert);
            
            // If hit found, subdivide segment at hit point and create new path
            if(hit != null) {
                
                // get parametric hit point for segment
                double hitPoint = hit.getR();
                
                // readjust the hit segment's endpoint
                Line tailSeg = segment.clone();
                segment.setEnd(hitPoint);
                
                // Set the start of the new tail to the hit point & insert into the list
                tailSeg.setStart(hitPoint);
                subpath.add(j+1, tailSeg);

                // Create new path and add subpaths
                Path newPath = new Path();
                for(int k=0, kMax=subpaths.size(); k<kMax; k++)
                    newPath.addSegments(subpaths.get(k));
                
                pview.repaint();
                pview.resetPath(newPath); //p._mouseDownPointIndex = ??
                return;
            }
        }
    }*/
}

/**
 * This inner class defines a polygon tool subclass for drawing freehand pencil sketches instead.
 */
public static class PencilTool extends PathViewTool {

    /** Creates a new PencilTool. */
    public PencilTool(Editor anEd)  { setEditor(anEd); }
    
    /** Overrides polygon tool method to flip default smoothing. */
    public boolean getSmoothPath()  { return true; }
}

}