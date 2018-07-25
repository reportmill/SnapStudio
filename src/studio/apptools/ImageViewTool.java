package studio.apptools;
import studio.app.*;
import snap.view.*;

/**
 * A tool subclass for editing ImageView.
 */
public class ImageViewTool <T extends ImageView> extends ViewTool <T> {
    
/**
 * Updates the UI controls from the currently selected oval.
 */
public void resetUI()
{    
    ImageView iview = getSelectedView(); if(iview==null) return;
    
    Slider slider = getView("FrameSlider", Slider.class);
    slider.setMax(iview.getFrameMax());
    
    setViewValue("FrameSlider", iview.getFrame());
    setViewValue("FrameText", iview.getFrame());
}

/**
 * Updates the currently selected oval from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    ImageView iview = getSelectedView(); if(iview==null) return;
    
    // Handle FrameSlider, FrameText
    if(anEvent.equals("FrameSlider") || anEvent.equals("FrameText")) {
        int frame = anEvent.getIntValue();
        iview.setFrame(frame);
    }
}

/**
 * Event handling - overridden to install crosshair cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

/**
 * Returns the view class this tool is responsible for.
 */
public Class <T> getViewClass()  { return (Class<T>)ImageView.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "ImageView Tool"; }

}