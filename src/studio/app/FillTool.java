package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * Provides a tool for editing RMFills.
 */
public class FillTool extends EditorPane.SupportPane {

    // Map of tool instances by shape class
    Map                 _tools = new Hashtable();
    
    // List of known strokes
    static Border     _strokes[] = { Border.createLineBorder(Color.BLACK,1)  };
    
    // List of known fills
    static Image       _img = Image.get(FillTool.class.getResource("pkg.images/Clouds.jpg"));
    static ImagePaint  _imageFill = new ImagePaint(_img);
    static Paint       _fills[] = { Color.BLACK, new GradientPaint(), _imageFill };

/**
 * Creates a new RMFillTool panel.
 */
public FillTool()  { super(null); }

/**
 * Called to reset UI controls.
 */
protected void resetUI()
{
    // Get currently selected shape
    View shape = getEditor().getSelectedOrSuperSelectedView();
    
    // Update FillColorWell
    setViewValue("FillColorWell", shape.getFillColor());    
}

/**
 * Called to respond to UI controls
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get the current editor and currently selected shape (just return if null)
    Editor editor = getEditor(); if(editor==null) return;
    View shape = editor.getSelectedOrSuperSelectedView(); if(shape==null) return;
    
    // Handle FillColorWell
    if(anEvent.equals("FillColorWell")) {
        
        // Get RMColor from color well
        ColorWell cwell = getView("FillColorWell", ColorWell.class);
        Color color = cwell.getColor();
        
        // Iterate over selected shapes and set color
        for(View s : editor.getSelectedOrSuperSelectedViews()) {
            
            // If command-click, set gradient fill
            if(ViewUtils.isMetaDown()) {
                Color c1 = shape.getFill()!=null? shape.getFillColor() : Color.CLEARWHITE;
                Paint f = new GradientPaint(0, 0, c1, 1, 0, color);
                s.setFill(f);
            }

            // If not command-click, just set color
            else s.setFill(color);
        }
    }
}

/**
 * Returns the number of known strokes.
 */
public int getStrokeCount()  { return _strokes.length; }

/**
 * Returns an individual stroke at given index.
 */
public Border getStroke(int anIndex)  { return _strokes[anIndex]; }

/**
 * Returns the number of known fills.
 */
public int getFillCount()  { return _fills.length; }

/**
 * Returns an individual fill at given index.
 */
public Paint getFill(int anIndex)  { return _fills[anIndex]; }

/**
 * Returns the currently selected shape's stroke.
 */
public Border getSelectedStroke()
{
    View shape = getEditor().getSelectedOrSuperSelectedView();
    return shape.getBorder();
}

/**
 * Iterate over editor selected shapes and set stroke.
 */
public void setSelectedStroke(Border aStroke)
{
    Editor editor = getEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = editor.getSelectedOrSuperSelectedView(i);
        shape.setBorder(aStroke);
    }
}

/**
 * Returns the currently selected shape's fill.
 */
public Paint getSelectedFill()
{
    View shape = getEditor().getSelectedOrSuperSelectedView();
    return shape.getFill();
}

/**
 * Iterate over editor selected shapes and set fill.
 */
public void setSelectedFill(Paint aFill)
{
    Editor editor = getEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = editor.getSelectedOrSuperSelectedView(i);
        shape.setFill(aFill);
    }
}

/**
 * Returns the specific tool for a given fill.
 */
public FillTool getTool(Object anObj)
{
    if(_imgPaintTool==null) {
        _gradPaintTool=new GradientPaintTool(); _gradPaintTool.setEditorPane(getEditorPane());
        _imgPaintTool=new ImagePaintTool(); _imgPaintTool.setEditorPane(getEditorPane());
    }
    
    // Get tool from tools map - just return if present
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
    if(cls==GradientPaint.class) return _gradPaintTool;
    if(cls==ImagePaint.class) return _imgPaintTool;
    return this;
}

GradientPaintTool _gradPaintTool;
ImagePaintTool _imgPaintTool;

}