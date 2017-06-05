package studio.apptools;
import java.util.List;
import snap.view.*;
import studio.app.ViewTool;

/**
 * A ViewTool for Button.
 */
public class ButtonTool <T extends Button> extends ViewTool <T> {

/**
 * Returns the class that this tool is responsible for.
 */
public Class getViewClass()  { return Button.class; }

/**
 * Updates the UI controls from the currently selected rectangle.
 */
public void resetUI()
{
    // Get selected button (just return if null)
    Button btn = getSelectedView(); if(btn==null) return;
    
    // Update TitleText
    setViewValue("TitleText", btn.getText());
}

/**
 * Updates the currently selected rectangle from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current rect view and rects list (just return if null)
    Button btn = getSelectedView(); if(btn==null) return;
    List <Button> btns = (List)getSelectedViews();

    // Handle TitleText
    if(anEvent.equals("TitleText")) {
        for(Button b : btns) {
            b.setText(anEvent.getStringValue());
        }
    }
}



}