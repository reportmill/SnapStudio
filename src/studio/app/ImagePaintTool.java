package studio.app;
import snap.gfx.*;
import snap.view.*;

/**
 * UI editing for RMImageFill.
 */
public class ImagePaintTool extends FillTool {

/**
 * Updates the UI controls from the currently selected shape.
 */
public void resetUI()
{
    // Get currently selected shape (just return if null) and image fill (if none, use default instance)
    View shape = getEditor().getSelectedOrSuperSelectedView(); if(shape==null) return;
    ImagePaint fill = shape.getFill() instanceof ImagePaint? (ImagePaint)shape.getFill() : _imageFill;
    
    // Update TiledCheckBox
    setViewValue("TiledCheckBox", !fill.isAbsolute());
    
    // Update XSpinner, YSpinner, ScaleXSpinner and ScaleYSpinner
    setViewValue("XSpinner", fill.getX());
    setViewValue("YSpinner", fill.getY());
    //setViewValue("ScaleXSpinner", fill.getScaleX());
    //setViewValue("ScaleYSpinner", fill.getScaleY());
}

/**
 * Updates the currently selected shape from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get currently selected shape (just return if null) and image fill (if none, use default instance)
    View shape = getEditor().getSelectedOrSuperSelectedView(); if(shape==null) return;
    ImagePaint fill = shape.getFill() instanceof ImagePaint? (ImagePaint)shape.getFill() : _imageFill;
    
    // Handle TiledCheckBox
    //if(anEvent.equals("TiledCheckBox"))
    //    fill = new ImagePaint(fill.getImage(), anEvent.getBooleanValue());
    
    // Handle XSpinner, YSpinner, ScaleXSpinner, ScaleYSpinner
    //if(anEvent.equals("XSpinner"))
    //    fill = fill.copyFor(anEvent.getFloatValue(), fill.getY(), fill.getWidth(), fill.getHeight(), fill.isAbsolute());
    //if(anEvent.equals("YSpinner"))
    //    fill = fill.copyFor(fill.getX(), anEvent.getFloatValue(), fill.getWidth(), fill.getHeight(), fill.isAbsolute());
    //if(anEvent.equals("ScaleXSpinner"))
    //    fill = fill.copyForScale(anEvent.getFloatValue(), fill.getScaleY());
    //if(anEvent.equals("ScaleYSpinner"))
    //    fill = fill.copyForScale(fill.getScaleX(), anEvent.getFloatValue());
    
    // Handle ChooseButton
    if(anEvent.equals("ChooseButton")) {
        FileChooser fc = getEnv().getFileChooser(); fc.setDesc("Image File"); fc.setExts(".png", ".jpg", ".gif");
        String path = fc.showOpenPanel(getUI());
        if(path!=null) {
            //RMImageData idata = RMImageData.getImageData(path);
            //if(idata!=null)
            //    fill = new RMImageFill(idata, true);
        }
    }

    // Set new fill
    setSelectedFill(fill);
}

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Fill Inspector (Texture)"; }

}