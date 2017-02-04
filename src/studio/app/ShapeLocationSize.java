package studio.app;
import java.util.List;
import snap.util.MathUtils;
import snap.view.*;

/**
 * This class provides UI editing for the currently selected shapes location and size.
 */
public class ShapeLocationSize extends EditorPane.SupportPane {
    
/**
 * Creates a new ShapeLocationSize pane.
 */
public ShapeLocationSize(EditorPane anEP)  { super(anEP); }

/**
 * Updates UI controls from currently selected shape.
 */
public void resetUI()
{
    // Get currently selected shape
    View shape = getEditor().getSelectedOrSuperSelectedView();
    
    // Update XThumb & XText
    setViewValue("XThumb", getUnitsFromPoints(shape.getX()));
    setViewValue("XText", getUnitsFromPoints(shape.getX()));
    
    // Update YThumb & YText
    setViewValue("YThumb", getUnitsFromPoints(shape.getY()));
    setViewValue("YText", getUnitsFromPoints(shape.getY()));
    
    // Update WThumb & WText
    setViewValue("WThumb", getUnitsFromPoints(shape.getWidth()));
    setViewValue("WText", getUnitsFromPoints(shape.getWidth()));
    
    // Update HThumb & HText
    setViewValue("HThumb", getUnitsFromPoints(shape.getHeight()));
    setViewValue("HText", getUnitsFromPoints(shape.getHeight()));
    
    // Update MinWText and MinHText
    setViewValue("MinWText", shape.isMinWidthSet()? shape.getMinWidth() : "-");
    setViewValue("MinHText", shape.isMinHeightSet()? shape.getMinHeight() : "-");
    
    // Update PrefWText and PrefHText
    setViewValue("PrefWText", shape.isPrefWidthSet()? shape.getPrefWidth() : "-");
    setViewValue("PrefHText", shape.isPrefHeightSet()? shape.getPrefHeight() : "-");
    
    // Disable if document or page
    //getUI().setEnabled(!(shape instanceof RMDocument || shape instanceof RMPage));
}

/**
 * Updates currently selected shape from UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get currently selected editor, document and shapes
    Editor editor = getEditor();
    View view = editor.getSelectedOrSuperSelectedView();
    List <View> views = editor.getSelectedOrSuperSelectedViews();
    
    // Handle X ThumbWheel and Text
    if(anEvent.equals("XThumb") || anEvent.equals("XText")) {
        editor.undoerSetUndoTitle("Location Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        for(View v : views) v.setX(value);
    }
    
    // Handle Y ThumbWheel and Text
    if(anEvent.equals("YThumb") || anEvent.equals("YText")) {
        editor.undoerSetUndoTitle("Location Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        for(View v : views) v.setY(value);
    }
    
    // Handle Width ThumbWheel and Text
    if(anEvent.equals("WThumb") || anEvent.equals("WText")) {
        editor.undoerSetUndoTitle("Size Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        if(Math.abs(value)<.1) value = MathUtils.sign(value)*.1f;
        for(View v : views) v.setWidth(value);
        if(view==editor.getContent()) view.setPrefWidth(value);
    }
    
    // Handle Height ThumbWheel and Text
    if(anEvent.equals("HThumb") || anEvent.equals("HText")) {
        editor.undoerSetUndoTitle("Size Change");
        double value = anEvent.getFloatValue(); value = getPointsFromUnits(value);
        if(Math.abs(value)<.1) value = MathUtils.sign(value)*.1f;
        for(View v : views) v.setHeight(value);
        if(view==editor.getContent()) view.setPrefHeight(value);
    }
    
    // Handle MinWText & MinHText
    if(anEvent.equals("MinWText"))
        for(View v : views) v.setMinWidth(anEvent.getFloatValue());
    if(anEvent.equals("MinHText"))
        for(View v : views) v.setMinHeight(anEvent.getFloatValue());
    
    // Handle MinWSyncButton & MinHSyncButton
    if(anEvent.equals("MinWSyncButton"))
        for(View v : views) v.setMinWidth(v.getWidth());
    if(anEvent.equals("MinHSyncButton"))
        for(View v : views) v.setMinHeight(v.getHeight());

    // Handle PrefWText & PrefHText
    if(anEvent.equals("PrefWText"))
        for(View v : views) v.setPrefWidth(anEvent.getFloatValue());
    if(anEvent.equals("PrefHText"))
        for(View v : views) v.setPrefHeight(anEvent.getFloatValue());
    
    // Handle PrefWSyncButton & PrefHSyncButton
    if(anEvent.equals("PrefWSyncButton"))
        for(View v : views) v.setPrefWidth(v.getWidth());
    if(anEvent.equals("PrefHSyncButton"))
        for(View v : views) v.setPrefHeight(v.getHeight());
}

/**
 * Converts from shape units to tool units.
 */
public double getUnitsFromPoints(double aValue)
{
    //RMEditor editor = getEditor(); RMDocument doc = editor.getDocument();
    return aValue; //return doc!=null? doc.getUnitsFromPoints(aValue) : aValue;
}

/**
 * Converts from tool units to shape units.
 */
public double getPointsFromUnits(double aValue)
{
    //RMEditor editor = getEditor(); RMDocument doc = editor.getDocument();
    return aValue; //return doc!=null? doc.getPointsFromUnits(aValue) : aValue;
}

/** Returns the name to be used in the inspector's window title. */
public String getWindowTitle()  { return "Location/Size Inspector"; }

}