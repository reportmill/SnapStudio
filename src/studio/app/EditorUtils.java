package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.util.ListUtils;
import snap.view.*;

/**
 * Some utility methods for Editor.
 */
public class EditorUtils {

    // The last color set by or returned to the color panel
    static Color    _lastColor = Color.BLACK;

/**
 * Groups the given view list to the given group view.
 * If given view list is null, use editor selected views.
 * If given group view is null, create new generic group view.
 */
public static void groupViews(Editor anEditor, List <? extends View> theViews, ChildView aGroupView)
{
    // If shapes not provided, use editor selected shapes
    if(theViews==null) theViews = anEditor.getSelectedViews();
    
    // If there are less than 2 selected shapes play a beep (the user really should know better)
    if(theViews.size()==0) { anEditor.beep(); return; }
    
    // Set undo title
    anEditor.undoerSetUndoTitle("Group");

    // Get copy of shapes, sorted by their original index in parent
    List <? extends View> shapes = theViews; //Sort.sortedList(theShapes, "indexInParent");
    
    // Get parent
    ChildView parent = (ChildView)shapes.get(0).getParent();
    
    // If no group shape, create one
    if(aGroupView==null) {
        aGroupView = new SpringView();
        aGroupView.setBounds(ViewUtils.getBoundsOfViews(parent, shapes));
    }

    // Add groupShape to the current parent (with no transform)
    parent.addChild(aGroupView);

    // Iterate over children and group to GroupShape
    for(View child : shapes)
        groupView(child, aGroupView);
    
    // Select group shape
    anEditor.setSelectedView(aGroupView);
}

/**
 * Adds child shape to group shape.
 */
private static void groupView(View child, ChildView gshape)
{
    // Get center point in parent coords and store as child x/y
    ChildView parent = (ChildView)child.getParent();
    Point cp = child.localToParent(child.getWidth()/2, child.getHeight()/2);
    child.setXY(cp.x, cp.y);
    
    // Move child to GroupShape
    parent.removeChild(child);
    gshape.addChild(child);
        
    // Undo transforms of group shape
    child.setRotate(child.getRotate() - gshape.getRotate());
    child.setScaleX(child.getScaleX()/gshape.getScaleX()); child.setScaleY(child.getScaleY()/gshape.getScaleY());
    //child.setSkewX(child.getSkewX() - gshape.getSkewX()); child.setSkewY(child.getSkewY() - gshape.getSkewY());
    
    // Reset center point: Get old center point in GroupShape coords and offset child by new center in GroupShape coords
    cp = gshape.parentToLocal(cp.x, cp.y);
    Point cp2 = child.localToParent(child.getWidth()/2, child.getHeight()/2);
    child.setXY(child.getX() + cp.x - cp2.x, child.getY() + cp.y - cp2.y);
}

/**
 * Ungroups any currently selected group shapes.
 */
public static void ungroupViews(Editor anEditor)
{
    // Get currently super selected shape and create list to hold ungrouped shapes
    List <View> ungroupedShapes = new ArrayList();
    
    // Register undo title for ungrouping
    anEditor.undoerSetUndoTitle("Ungroup");

    // See if any of the selected shapes can be ungrouped
    for(View shape : anEditor.getSelectedViews()) {
        
        // If shape cann't be ungrouped, skip
        if(!anEditor.getTool(shape).isUngroupable(shape)) continue;
        ChildView groupShape = (ChildView)shape;
        ChildView parent = (ChildView)groupShape.getParent();
            
        // Iterate over children and ungroup from GroupShape
        for(View child : groupShape.getChildren().clone()) {
            ungroupView(child);
            ungroupedShapes.add(child);
        }

        // Remove groupShape from parent
        parent.removeChild(groupShape);
    }

    // If were some ungroupedShapes, select them (set selected objects for undo/redo)
    if(ungroupedShapes.size()>0)
        anEditor.setSelectedViews(ungroupedShapes);

    // If no ungroupedShapes, beep at silly user
    else anEditor.beep();
}

/**
 * Transforms given shape to world coords.
 */
private static void ungroupView(View child)
{
    // Get center point in parent coords and store as child x/y
    ChildView gshape = (ChildView)child.getParent(), parent = (ChildView)gshape.getParent();
    Point cp = child.localToParent(child.getWidth()/2, child.getHeight()/2, parent);
    child.setXY(cp.x, cp.y);
    
    // Coalesce transforms up the parent chain
    child.setRotate(child.getRotate() + gshape.getRotate());
    child.setScaleX(child.getScaleX() * gshape.getScaleX()); child.setScaleY(child.getScaleY() * gshape.getScaleY());
    //child.setSkewX(child.getSkewX() + gshape.getSkewX()); child.setSkewY(child.getSkewY() + gshape.getSkewY());

    // Remove from group shape & add to group shape parent
    gshape.removeChild(child);
    parent.addChild(child);
    
    // Reset center point: Get new center in parent coords and offset child by change
    Point cp2 = child.localToParent(child.getWidth()/2, child.getHeight()/2);
    child.setXY(child.getX() + cp.x - cp2.x, child.getY() + cp.y - cp2.y);
}

/**
 * Orders all currently selected shapes to the front.
 */
public static void bringToFront(Editor anEditor)
{
    ChildView par = anEditor.getSuperSelectedChildView();
    if(par==null || anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Bring to Front");
    
    // Iterate over selected views and remove/add to move to front
    List <View> views = anEditor.getSelectedViews();
    for(View view : views) {
        par.removeChild(view); par.addChild(view); }
}

/**
 * Orders all currently selected shapes to the back.
 */
public static void sendToBack(Editor anEditor)
{
    ChildView par = anEditor.getSuperSelectedChildView();
    if(par==null || anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Send to Back");
    
    // Iterate over selected views and remove/add to move to back
    List <View> views = anEditor.getSelectedViews();
    for(int i=0, iMax=views.size(); i<iMax; i++) { View view = views.get(i);
        par.removeChild(view); par.addChild(view, i); }
}

/**
 * Arranges currently selected shapes in a row relative to their top.
 */
public static void makeRowTop(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Top");
    //double minY = anEditor.getSelectedShape().getFrameY();
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setFrameY(minY);
}

/**
 * Arranges currently selected shapes in a row relative to their center.
 */
public static void makeRowCenter(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Center");
    //double midY = anEditor.getSelectedShape().getFrame().getMidY();
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setFrameY(midY - shape.getHeight()/2);
}

/**
 * Arranges currently selected shapes in a row relative to their bottom.
 */
public static void makeRowBottom(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Row Bottom");
    //double maxY = anEditor.getSelectedShape().getFrameMaxY();
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setFrameY(maxY - shape.getHeight());
}

/**
 * Arranges currently selected shapes in a column relative to their left border.
 */
public static void makeColumnLeft(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Left");
    //double minX = anEditor.getSelectedShape().getFrameX();
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setFrameX(minX);
}

/**
 * Arranges currently selected shapes in a column relative to their center.
 */
public static void makeColumnCenter(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Center");
    //double midX = anEditor.getSelectedShape().getFrame().getMidX();
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setFrameX(midX - shape.getWidth()/2);
}

/**
 * Arranges currently selected shapes in a column relative to their right border.
 */
public static void makeColumnRight(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Column Right");
    //double maxX = anEditor.getSelectedShape().getFrameMaxX();    
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setFrameX(maxX - shape.getWidth());
}

/**
 * Makes currently selected shapes all have the same width and height as the first selected shape.
 */
public static void makeSameSize(Editor anEditor)
{
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Make Same Size");
    Size size = anEditor.getSelectedView().getSize();
    //for(RMShape shape : anEditor.getSelectedShapes()) shape.setSize(size.getWidth(), size.getHeight());
}

/**
 * Makes currently selected shapes all have the same width as the first selected shape.
 */
public static void makeSameWidth(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Make Same Width");
    
    // Get first selected shape width
    double width = anEditor.getSelectedView().getWidth();
    
    // Iterate over selected shapes and set width
    for(View shape : anEditor.getSelectedViews())
        shape.setWidth(width);
}

/**
 * Makes currently selected shapes all have the same height as the first selected shape.
 */
public static void makeSameHeight(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Make Same Height");
    
    // Get first selected shape height
    double height = anEditor.getSelectedView().getHeight();
    
    // Iterate over selected shapes and set height
    for(View shape : anEditor.getSelectedViews())
        shape.setHeight(height);
}

/**
 * Makes currently selected shapes size to fit content.
 */
public static void setSizeToFit(Editor anEditor)
{
    // If no shapes, beep and return
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    
    // Register undo title
    anEditor.undoerSetUndoTitle("Size to Fit");
    
    // Iterate over shapes and size to fit
    //for(View shape : anEditor.getSelectedShapes()) shape.setBestSize();
}

/**
 * Arranges currently selected shapes such that they have the same horizontal distance between them.
 */
public static void equallySpaceRow(Editor anEditor)
{
    // If no selected shapes, beep and return
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    
    // Get selectedShapes sorted by minXInParentBounds
    /*List <View> shapes = RMSort.sortedList(anEditor.getSelectedShapes(), "getFrameX");
    float spaceBetweenShapes = 0;

    // Calculate average space between shapes
    for(int i=1, iMax=shapes.size(); i<iMax; i++)
        spaceBetweenShapes += shapes.get(i).getFrameX() - shapes.get(i-1).getFrameMaxX();
    if(shapes.size()>1)
        spaceBetweenShapes = spaceBetweenShapes/(shapes.size()-1);
    
    // Reset average space between shapes
    anEditor.undoerSetUndoTitle("Equally Space Row");
    for(int i=1, iMax=shapes.size(); i<iMax; i++) {
        View shape = shapes.get(i);
        View lastShape = shapes.get(i-1);
        double tx = lastShape.getFrameMaxX() + spaceBetweenShapes;
        shape.setFrameX(tx);
    }*/
}

/**
 * Arranges currently selected shapes such that they have the same vertical distance between them.
 */
public static void equallySpaceColumn(Editor anEditor)
{
    // If no selected shapes, beep and return
    if(anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    
    // Get selectedShapes sorted by minXInParentBounds
    /*List <View> shapes = RMSort.sortedList(anEditor.getSelectedShapes(), "getFrameY");
    float spaceBetweenShapes = 0;

    // Calculate average space between shapes
    for(int i=1, iMax=shapes.size(); i<iMax; i++)
        spaceBetweenShapes += shapes.get(i).getFrameY() - shapes.get(i-1).getFrameMaxY();
    if(shapes.size()>1)
        spaceBetweenShapes = spaceBetweenShapes/(shapes.size()-1);

    // Reset average space between shapes
    anEditor.undoerSetUndoTitle("Equally Space Column");
    for(int i=1, iMax=shapes.size(); i<iMax; i++) {
        View shape = shapes.get(i);
        View lastShape = shapes.get(i-1);
        double ty = lastShape.getFrameMaxY() + spaceBetweenShapes;
        shape.setFrameY(ty);
    }*/
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void combinePaths(Editor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedViewCount()<2) { anEditor.beep(); return; }
    
    // Get selected views and create CombinedView
    List <View> selViews = ListUtils.clone(anEditor.getSelectedViews());
    PathView combinedView = getCombinedPathsView(selViews);
    
    // Remove original children and replace with CombinedShape
    anEditor.undoerSetUndoTitle("Add Paths");
    ChildView parent = (ChildView)anEditor.getSuperSelectedParentView();
    for(View shape : selViews) parent.removeChild(shape);
    parent.addChild(combinedView);
    
    // Select CombinedShape
    anEditor.setSelectedView(combinedView);
}

/**
 * Returns a PathView by combining paths of given views.
 */
public static PathView getCombinedPathsView(List <View> theViews)
{
    // Get first view and the path of the combined views
    View view0 = theViews.size()>0? theViews.get(0) : null; if(view0==null) return null;
    Shape newPath = getCombinedPath(theViews);

    // Create combined view, configure and return
    PathView view = new PathView(); view.setBounds(newPath.getBounds());
    view.setFill(view0.getFill()); view.setBorder(view0.getBorder()); //view.copyShape(shape0); view._rss = null;
    view.resetPath(newPath);
    return view;
}

/**
 * Returns the combined path from given views.
 */
public static Shape getCombinedPath(List <View> theViews)
{
    List <Shape> paths = getPathsFromViews(theViews, 0); Shape s1 = paths.get(0);
    for(int i=1, iMax=paths.size(); i<iMax; i++) { Shape s2 = paths.get(i);
        s1 = Shape.add(s1, s2); }
    return s1;
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void subtractPaths(Editor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedViewCount()<2) { anEditor.beep(); return; }
    
    // Get selected shapes and create SubtractedShape
    List <View> selViews = ListUtils.clone(anEditor.getSelectedViews());
    PathView newView = getSubtractedPathsView(selViews, 0);
    
    // Remove original children and replace with SubtractedShape
    anEditor.undoerSetUndoTitle("Subtract Paths");
    ChildView parent = (ChildView)anEditor.getSuperSelectedParentView();
    for(View view : selViews) parent.removeChild(view);
    parent.addChild(newView);
    
    // Select SubtractedShape
    anEditor.setSelectedView(newView);
}

/**
 * Returns a PathView by subtracting paths of given views.
 */
public static PathView getSubtractedPathsView(List <View> theViews, int anInset)
{
    // Get first view and path for subtracted views
    View view0 = theViews.get(0);
    Shape newPath = getSubtractedPath(theViews, 0);

    // Create subtracted view, configure and return
    PathView view = new PathView(); view.setBounds(newPath.getBounds());
    view.setFill(view0.getFill()); view.setBorder(view0.getBorder()); //view.copyShape(view0); view._rss = null;
    view.resetPath(newPath);
    return view;
}

/**
 * Returns the combined path from given views.
 */
public static Shape getSubtractedPath(List <View> theViews, int anInset)
{
    // Eliminate view that don't intersect first shape frame
    View shape0 = theViews.get(0);
    Rect shape0Frame = shape0.localToParent(shape0.getBoundsLocal()).getBounds();
    List <View> shapes = theViews;
    for(int i=shapes.size()-1; i>=0; i--) { View view = shapes.get(i);
        Rect frame = view.localToParent(view.getBoundsLocal()).getBounds();
        if(!frame.intersects(shape0Frame)) {
            if(shapes==theViews) shapes = new ArrayList(theViews); shapes.remove(i); }}
    
    // Get shape paths, iterate over them, successively subtract them and return final
    List <Shape> paths = getPathsFromViews(shapes, anInset); Shape s1 = paths.get(0);
    for(int i=1, iMax=paths.size(); i<iMax; i++) { Shape s2 = paths.get(i);
        s1 = Shape.subtract(s1, s2); }
    return s1;
}

/**
 * Returns the list of paths from the given shapes list.
 */
private static List <Shape> getPathsFromViews(List <View> theViews, int anInset)
{
    // Iterate over shapes, get bounds of each (inset), path of each (in parent coords) and add to list
    List paths = new ArrayList(theViews.size());
    for(int i=0, iMax=theViews.size(); i<iMax; i++) { View shape = theViews.get(i);
        Rect bounds = shape.getBoundsLocal(); if(anInset!=0 && i>0) bounds.inset(anInset);
        Shape path = shape.getBoundsShape();
        path = shape.localToParent(path);
        paths.add(path);
    }
    
    // Return paths list
    return paths;
}

/**
 * Converts currently selected shape to image.
 */
public static void convertToImage(Editor anEditor)
{
    // Get currently selected shape (if shape is null, just return)
    /*RMShape shape = anEditor.getSelectedShape(); if(shape==null) return;
    
    // Get image for shape, get PNG bytes for image and create new RMImageShape for bytes
    Image image = RMShapeUtils.createImage(shape, null);
    byte imageBytes[] = image.getBytesPNG();
    RMImageShape imageShape = new RMImageShape(imageBytes);
    
    // Set ImageShape XY and add to parent
    imageShape.setXY(shape.getX() + shape.getBoundsMarked().getX(), shape.getY() + shape.getBoundsMarked().getY());
    shape.getParent().addChild(imageShape, shape.indexOf());
    
    // Replace old selectedShape with image and remove original shape
    anEditor.setSelectedShape(imageShape);
    shape.removeFromParent();*/
}

/**
 * Moves all the currently selected shapes one point to the right.
 */
public static void moveRightOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Right One Point");
    double offset = 1; //anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    anEditor.getSelectedViews().forEach(i -> i.setX(i.getX() + offset));
}

/**
 * Moves all the currently selected shapes one point to the left.
 */
public static void moveLeftOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Left One Point");
    double offset = 1; //anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    anEditor.getSelectedViews().forEach(i -> i.setX(i.getX() - offset));
}

/**
 * Moves all the currently selected shapes one point up.
 */
public static void moveUpOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Up One Point");
    double offset = 1; //anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    anEditor.getSelectedViews().forEach(i -> i.setY(i.getY() - offset));
}

/**
 * Moves all the currently selected shapes one point down.
 */
public static void moveDownOnePoint(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Move Down One Point");
    double offset = 1; //anEditor.getViewerShape().getSnapGrid()? anEditor.getViewerShape().getGridSpacing() : 1;
    anEditor.getSelectedViews().forEach(i -> i.setX(i.getX() + offset));
}

/**
 * Returns the specified type of color (text, stroke or fill) of editor's selected shape.
 */
public static Color getSelectedColor(Editor anEditor)
{
    // Get selected or super selected view
    View view = anEditor.getSelectedOrSuperSelectedView();
    
    // If selected or super selected view is page that doesn't draw color, return "last color" (otherwise, reset it)
    if((view instanceof PageView || view instanceof DocView) && view.getFill()==null)
        return _lastColor;
    else _lastColor = Color.BLACK;
        
    // If text color and text editing, return color of text editor
    //if(anEditor.getTextEditor()!=null) return anEditor.getTextEditor().getColor();
        
    // Return selected shape's color
    Paint p = anEditor.getSelectedOrSuperSelectedView().getFill();
    return p instanceof Color? (Color)p : Color.BLACK;
}

/**
 * Sets the specified type of color (text, stroke or fill) of editor's selected shape.
 */
public static void setSelectedColor(Editor anEditor, Color aColor)
{
    // Get selected or super selected view
    View view = anEditor.getSelectedOrSuperSelectedView();
        
    // If editor selected or super selected view is doc or page, set "last color" and return
    if(view instanceof PageView || view instanceof DocView) { _lastColor = aColor; return; }

    // If text color and text editing, return color of text editor
    /*if(anEditor.getTextEditor()!=null) { RMTextEditor ted = anEditor.getTextEditor();
        if(ViewUtils.isMetaDown() && ted.getTextBorder()!=null) { Border lbrdr = ted.getTextBorder();
            ted.setTextBorder(Border.createLineBorder(aColor, lbrdr.getWidth())); }
        else ted.setColor(aColor); } else*/
    
    // If command-click, set gradient fill
    if(ViewUtils.isMetaDown()) {
        Paint p = view.getFill();
        Color c1 = p instanceof Color? (Color)p : Color.CLEARWHITE;
        view.setFill(new GradientPaint(c1, aColor, 0));
    }
        
    // If not command click, just set the color of all the selected shapes
    else setColor(anEditor, aColor);
}

/**
 * Sets the fill color of the editor's selected shapes.
 */
public static void setColor(Editor anEditor, Color aColor)
{
    int time = anEditor.getTime();
    for(View view : anEditor.getSelectedOrSuperSelectedViews()) {

        // Set Anim value (do this first so Anim.StartVal can be initialized if needed)
        if(time!=0 || (view.getAnim(-1)!=null && !view.getAnim(0).isEmpty())) {
            ViewAnim anim = Animation.getAnim(view, time);
            anim.setFill(aColor); }
            
        // Set view fill
        view.setFill(aColor);
    }
}

/**
 * Sets the stroke color of the editor's selected shapes.
 */
public static void setStrokeColor(Editor anEditor, Color aColor)
{
    Border bdr = anEditor.getSelectedOrSuperSelectedView().getBorder();
    double w = bdr!=null? bdr.getWidth() : 1;
    anEditor.getSelectedOrSuperSelectedViews().forEach(i -> i.setBorder(aColor,w));
}

/**
 * Sets the text color of the editor's selected shapes.
 */
public static void setTextColor(Editor anEditor, Color aColor)
{
    for(View view : anEditor.getSelectedOrSuperSelectedViews())
        if(view instanceof TextView) ((TextView)view).setTextFill(aColor);
}

/**
 * Returns the font of editor's selected shape.
 */
public static Font getFont(Editor anEditor)
{
    Font font = null;
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax && font==null; i++) {
        View view = anEditor.getSelectedOrSuperSelectedView(i); ViewTool tool = anEditor.getTool(view);
        font = tool.getFont(anEditor, view);
    }
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax && font==null; i++) {
        View view = anEditor.getSelectedOrSuperSelectedView(i); ViewTool tool = anEditor.getTool(view);
        font = tool.getFontDeep(anEditor, view);
    }
    return font!=null? font : new Font();//.getDefaultFont();
}

/**
 * Sets the font family of editor's selected shape(s).
 */
public static void setFontFamily(Editor anEditor, Font aFont)
{
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = anEditor.getSelectedOrSuperSelectedView(i);
        ViewTool tool = anEditor.getTool(shape);
        tool.setFontFamilyDeep(anEditor, shape, aFont);
    }
}

/**
 * Sets the font name of editor's selected shape(s).
 */
public static void setFontName(Editor anEditor, Font aFont)
{
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = anEditor.getSelectedOrSuperSelectedView(i);
        ViewTool tool = anEditor.getTool(shape);
        tool.setFontNameDeep(anEditor, shape, aFont);
    }
}

/**
 * Sets the font size of editor's selected shape(s).
 */
public static void setFontSize(Editor anEditor, float aSize, boolean isRelative)
{
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = anEditor.getSelectedOrSuperSelectedView(i);
        ViewTool tool = anEditor.getTool(shape);
        tool.setFontSizeDeep(anEditor, shape, aSize, isRelative);
    }
}

/**
 * Sets the "boldness" of text in the currently selected shapes.
 */
public static void setFontBold(Editor anEditor, boolean aFlag)
{
    anEditor.undoerSetUndoTitle("Make Bold");
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = anEditor.getSelectedOrSuperSelectedView(i);
        ViewTool tool = anEditor.getTool(shape);
        tool.setFontBoldDeep(anEditor, shape, aFlag);
    }
}

/**
 * Sets the italic state of text in the currently selected shapes.
 */
public static void setFontItalic(Editor anEditor, boolean aFlag)
{
    anEditor.undoerSetUndoTitle("Make Italic");
    for(int i=0, iMax=anEditor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = anEditor.getSelectedOrSuperSelectedView(i);
        ViewTool tool = anEditor.getTool(shape);
        tool.setFontItalicDeep(anEditor, shape, aFlag);
    }
}

/**
 * Returns whether the currently selected shape is underlined.
 */
public static boolean isUnderlined(Editor anEdtr)  { return false; }//anEdtr.getSelectedOrSuperSelectedShape().isUnderlined(); }

/**
 * Sets the currently selected shapes to be underlined.
 */
public static void setUnderlined(Editor anEditor)
{
    anEditor.undoerSetUndoTitle("Make Underlined");
    for(View view : anEditor.getSelectedOrSuperSelectedViews())
        if(view instanceof TextView) ((TextView)view).setUnderlined(!((TextView)view).isUnderlined());
}

/**
 * Returns the outline state of the currently selected shape (null if none).
 */
public static Border getTextBorder(Editor anEditor)
{
    //RMShape shp = anEditor.getSelectedOrSuperSelectedShape();
    //RMTextShape tshp = shp instanceof RMTextShape? (RMTextShape)shp : null; if(tshp==null) return null;
    return null;//tshp.getTextBorder();
}

/**
 * Sets the currently selected shapes to be outlined.
 */
public static void setTextBorder(Editor anEditor)
{
    /*if(getTextBorder(anEditor)==null) {
        setTextBorder(anEditor, Border.createLineBorder(Color.BLACK,1));
        setTextColor(anEditor, RMColor.white);
    }
    else {
        setTextBorder(anEditor, null);
        setTextColor(anEditor, RMColor.black);
    }*/
}

/**
 * Sets the outline state of the currently selected shapes.
 */
public static void setTextBorder(Editor anEditor, Border aBorder)
{
    /*anEditor.undoerSetUndoTitle("Make Outlined");
    for(RMShape shp : anEditor.getSelectedOrSuperSelectedShapes()) {
        if(shp instanceof RMTextShape)
            ((RMTextShape)shp).setTextBorder(aBorder);
    }*/
}

/**
 * Returns the horizontal alignment of the text of the currently selected shapes.
 */
public static HPos getAlignX(Editor anEditor)
{
    View view = anEditor.getSelectedOrSuperSelectedView();
    if(view instanceof TextView)
        return ((TextView)view).getLineAlign();
    return view.getAlign().getHPos();
}

/**
 * Sets the horizontal alignment of the text of the currently selected shapes.
 */
public static void setAlignX(Editor anEditor, HPos anAlign)
{
    anEditor.undoerSetUndoTitle("Alignment Change");
    for(View view : anEditor.getSelectedOrSuperSelectedViews()) {
        if(view instanceof TextView)
            ((TextView)view).setLineAlign(anAlign);
        else view.setAlign(anAlign);
    }
}

/**
 * Returns whether currently selected view is text justify.
 */
public static boolean isJustify(Editor anEditor)
{
    View view = anEditor.getSelectedOrSuperSelectedView();
    return view instanceof TextView && ((TextView)view).isLineJustify();
}

/**
 * Returns whether currently selected view is text justify.
 */
public static void setJustify(Editor anEditor, boolean aValue)
{
    anEditor.undoerSetUndoTitle("Alignment Change");
    for(View view : anEditor.getSelectedOrSuperSelectedViews())
        if(view instanceof TextView) ((TextView)view).setLineJustify(aValue);
}

/**
 * Sets the currently selected shapes to show text as superscript.
 */
public static void setSuperscript(Editor anEditor)
{
    //anEditor.undoerSetUndoTitle("Make Superscript");
    //RMTextEditor ted = anEditor.getTextEditor();
    //if(ted!=null) ted.setSuperscript();
}

/**
 * Sets the currently selected shapes to show text as subscript.
 */
public static void setSubscript(Editor anEditor)
{
    //anEditor.undoerSetUndoTitle("Make Subscript");
    //RMTextEditor ted = anEditor.getTextEditor();
    //if(ted!=null) ted.setSubscript();
}

/**
 * Returns the format of the editor's selected shape.
 */
public static TextFormat getFormat(Editor anEditor)  { return null; }//anEditor.getSelectedOrSuperSelectedShape().getFormat(); }

/**
 * Sets the format of editor's selected shape(s).
 */
public static void setFormat(Editor anEditor, TextFormat aFormat)
{
    //for(RMShape shape : anEditor.getSelectedOrSuperSelectedShapes()) shape.setFormat(aFormat);
}

/**
 * Preview PDF.
 */
public static void previewPDF(EditorPane anEP)  { }

/**
 * Generate report, save as HTML in temp file and open.
 */
public static void previewHTML(EditorPane anEP)  { }

/**
 * Generate report, save as CSV in temp file and open.
 */
public static void previewCSV(EditorPane anEP)  { }

/**
 * Generate report, save as JPG in temp file and open.
 */
public static void previewJPG(EditorPane anEP)  { }

/**
 * Generate report, save as PNG in temp file and open.
 */
public static void previewPNG(EditorPane anEP)  { }

/**
 * Preview XLS.
 */
public static void previewXLS(EditorPane anEP)  { }

/**
 * Preview RTF.
 */
public static void previewRTF(EditorPane anEP)  { }

/**
 * Preview XML.
 */
public static void previewXML(EditorPane anEP)  { }

/**
 * Save document as PDF to given path.
 */
public static void saveAsPDF(EditorPane anEP)  { }

}