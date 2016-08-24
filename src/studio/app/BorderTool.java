package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.ColorWell;

/**
 * UI editing for RMStroke.
 */
public class BorderTool extends FillTool {

    // The last list of strokes provided to UI
    List <Border>  _strokes;

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
public List <Border> getStrokes()  { return _strokes; }

/**
 * Returns a list of strokes for all MainEditor selected shapes (creating stand-ins for selected shapes with no stroke).
 */
private List <Border> createStrokes()
{
    Editor editor = getEditor();
    List <Border> strokes = new ArrayList();
    for(View shape : editor.getSelectedOrSuperSelectedShapes())
        strokes.add(shape.getBorder()!=null? shape.getBorder() : Border.createLineBorder(Color.BLACK,1));
    return _strokes = strokes;
}

/**
 * Override to load Strokes list.
 */
public void processResetUI()
{
    _strokes = createStrokes();
    super.processResetUI();
}

/**
 * Reset UI controls.
 */
public void resetUI()
{
    // Get currently selected shape
    View shape = getEditor().getSelectedOrSuperSelectedShape();
    Border stroke = shape.getBorder(); if(stroke==null) stroke = Border.createLineBorder(Color.BLACK,1);
    
    // Update StrokeColorWell, StrokeWidthText, StrokeWidthThumb, DashArrayText, DashPhaseSpinner
    setViewValue("StrokeColorWell", stroke.getColor());
    setViewValue("StrokeWidthText", stroke.getWidth());
    setViewValue("StrokeWidthThumb", stroke.getWidth());
    //setViewValue("DashArrayText", stroke.getDashArrayString());
    //setViewValue("DashPhaseSpinner", stroke.getDashPhase());
}

/**
 * Respond to UI changes
 */
public void respondUI(ViewEvent anEvent)
{
    // Get editor selected shapes and selected shape
    Editor editor = getEditor();
    List <View> shapes = editor.getSelectedOrSuperSelectedShapes();
    View shape = editor.getSelectedOrSuperSelectedShape();
    
    // Handle StrokeColorWell - get color and set in selected shapes
    if(anEvent.equals("StrokeColorWell")) {
        ColorWell cwell = getView("StrokeColorWell", ColorWell.class);
        Color color = cwell.getColor();
        //for(View s : shapes)
        //    s.setStrokeColor(color);
    }
    
    // Handle StrokeWidthText, StrokeWidthThumb
    if(anEvent.equals("StrokeWidthText") || anEvent.equals("StrokeWidthThumb")) {
        float width = anEvent.getFloatValue();
        //for(View s : shapes)
        //    s.setStrokeWidth(width);
    }
    
    // Handle DashArrayText
    if(anEvent.equals("DashArrayText")) {
        //float darray[] = RMStroke.getDashArray(anEvent.getStringValue(), ",");
        //for(View shp : shapes) { RMStroke stroke = shp.getStroke(); if(stroke==null) stroke = new RMStroke();
        //    shp.setStroke(stroke.deriveDashArray(darray)); }
    }

    // Handle DashPhaseSpinner
    if(anEvent.equals("DashPhaseSpinner")) {
        float dphase = anEvent.getFloatValue();
        //for(View shp : shapes) { Border stroke = shp.getBorder(); if(stroke==null) stroke = new RMStroke();
        //    shp.setStroke(stroke.deriveDashPhase(dphase)); }
    }

}

}