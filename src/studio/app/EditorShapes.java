package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * Handles editor methods specific to clipboard operations (cut, copy paste).
 */
public class EditorShapes {

    // The editor
    Editor          _editor;

    // The last color set by or returned to the color panel
    static Color    _lastColor = Color.BLACK;

/**
 * Creates a new editor shapes helper.
 */
public EditorShapes(Editor anEditor) { _editor = anEditor; }

/**
 * Returns the editor.
 */
public Editor getEditor() { return _editor; }

/**
 * Groups the given shape list to the given group shape.
 * If given shapes list is null, use editor selected shapes.
 * If given group shape is null, create new generic group shape.
 */
public static void groupShapes(Editor anEditor, List <? extends View> theShapes, ParentView aGroupShape)
{
    // If shapes not provided, use editor selected shapes
    if(theShapes==null)
        theShapes = anEditor.getSelectedViews();
    
    // If there are less than 2 selected shapes play a beep (the user really should know better)
    if(theShapes.size()==0) { anEditor.beep(); return; }
    
    // Set undo title
    anEditor.undoerSetUndoTitle("Group");

    // Group shapes
    //aGroupShape = RMShapeUtils.groupShapes(theShapes, aGroupShape);
    
    // Select group shape
    anEditor.setSelectedView(aGroupShape);
}

/**
 * Ungroups any currently selected group shapes.
 */
public static void ungroupShapes(Editor anEditor)
{
    // Get currently super selected shape and create list to hold ungrouped shapes
    List <View> ungroupedShapes = new Vector();
    
    // Register undo title for ungrouping
    anEditor.undoerSetUndoTitle("Ungroup");

    // See if any of the selected shapes can be ungrouped
    for(View shape : anEditor.getSelectedViews()) {
        
        // If shape cann't be ungrouped, skip
        if(!anEditor.getTool(shape).isUngroupable(shape)) continue;
        ParentView groupShape = (ParentView)shape;
        ParentView parent = groupShape.getParent();
            
        // Iterate over children, remove from groupShape, add to groupShape parent and add to ungroupedShapes list
        /*for(View child : groupShape.getChildArray()) {
            
            // Convert child to world coords
            child.convertToShape(null);
            
            // Remove from group shape & add to group shape parent
            groupShape.removeChild(child);
            parent.addChild(child);
            ungroupedShapes.add(child);
            
            // Convert back from world coords
            child.convertFromShape(null);
        }*/

        // Remove groupShape from parent
        ((ChildView)parent).removeChild(groupShape);
    }

    // If were some ungroupedShapes, select them (set selected objects for undo/redo)
    if(ungroupedShapes.size()>0)
        anEditor.setSelectedViews(ungroupedShapes);

    // If no ungroupedShapes, beep at silly user
    else anEditor.beep();
}

/**
 * Orders all currently selected shapes to the front.
 */
public static void bringToFront(Editor anEditor)
{
    ChildView parent = anEditor.getSuperSelectedChildView();
    if(parent==null || anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Bring to Front");
    parent.bringViewsToFront(anEditor.getSelectedViews());
}

/**
 * Orders all currently selected shapes to the back.
 */
public static void sendToBack(Editor anEditor)
{
    ChildView parent = anEditor.getSuperSelectedChildView();
    if(parent==null || anEditor.getSelectedViewCount()==0) { anEditor.beep(); return; }
    anEditor.undoerSetUndoTitle("Send to Back");
    parent.sendViewsToBack(anEditor.getSelectedViews());
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
    
    // Get selected shapes and create CombinedShape
    /*List <RMShape> selectedShapes = ListUtils.clone(anEditor.getSelectedShapes());
    RMPolygonShape combinedShape = RMShapeUtils.getCombinedPathsShape(selectedShapes);
    
    // Remove original children and replace with CombinedShape
    anEditor.undoerSetUndoTitle("Add Paths");
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    for(RMShape shape : selectedShapes) parent.removeChild(shape);
    parent.addChild(combinedShape);
    
    // Select CombinedShape
    anEditor.setSelectedShape(combinedShape);*/
}

/**
 * Create new shape by coalescing the outer perimeters of the currently selected shapes.
 */
public static void subtractPaths(Editor anEditor)
{
    // If shapes less than 2, just beep and return
    if(anEditor.getSelectedViewCount()<2) { anEditor.beep(); return; }
    
    // Get selected shapes and create SubtractedShape
    /*List <RMShape> selectedShapes = ListUtils.clone(anEditor.getSelectedShapes());
    RMPolygonShape subtractedShape = RMShapeUtils.getSubtractedPathsShape(selectedShapes, 0);
    
    // Remove original children and replace with SubtractedShape
    anEditor.undoerSetUndoTitle("Subtract Paths");
    RMParentShape parent = anEditor.getSuperSelectedParentShape();
    for(RMShape shape : selectedShapes) parent.removeChild(shape);
    parent.addChild(subtractedShape);
    
    // Select SubtractedShape
    anEditor.setSelectedShape(subtractedShape);*/
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
        Paint orig = view.getFill();
        view.setFill(aColor);
        if(time!=0 || (view.getAnim(-1)!=null && !view.getAnim(0).isEmpty())) {
            ViewAnim anim = Animation.getAnim(view, time);
            if(!anim.isStartValSet(View.Fill_Prop))
                anim.setStartVal(View.Fill_Prop, orig!=null? orig : Color.CLEAR);
            anim.setFill(aColor);
        }
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

}