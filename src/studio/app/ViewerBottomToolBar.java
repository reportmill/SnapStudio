package studio.app;
import snap.geom.*;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * UI controls for RMViewerPane bottom.
 */
public class ViewerBottomToolBar extends ViewOwner {

    // The viewer associated with this tool bar
    ViewerPane    _viewerPane;

/**
 * Creates a new bottom ui.
 */
public ViewerBottomToolBar(ViewerPane aViewerPane)  { _viewerPane = aViewerPane; }

/**
 * Returns the viewer pane.
 */
public ViewerPane getViewerPane()  { return _viewerPane; }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Set right arrow in PageForwardButton
    Polygon p1 = new Polygon(4, 5, 10, 11, 4, 17);
    getView("PageForwardButton", Button.class).setImage(getImage(p1));
    
    // Set left arrow in PageBackButton
    Polygon p2 = new Polygon(10, 5, 4, 11, 10, 17);
    getView("PageBackButton", Button.class).setImage(getImage(p2));
    
    // Set left arrow plus stop bar in PageBackAllButton
    Path p3 = new Path(); p3.append(p2.getPathIter(new Transform(2, 0)));
    p3.append(new Rect(2, 6, 2, 10));
    getView("PageBackAllButton", Button.class).setImage(getImage(p3));
    
    // Set right arrow plus stop bar in PageForwardAllButton
    Path p4 = new Path(); p4.append(p1.getPathIter(new Transform(-2, 0)));
    p4.append(new Rect(10, 6, 2, 10));
    getView("PageForwardAllButton", Button.class).setImage(getImage(p4));
}

/**
 * Resets UI.
 */
protected void resetUI()
{
    // Get viewer pane
    ViewerPane viewerPane = getViewerPane();
    Viewer viewer = viewerPane.getViewer();
    
    // Reset ZoomText
    setViewValue("ZoomText", Math.round(viewer.getZoomFactor()*100) + "%");
    
    // Reset PageText field
    //String pageText = "" + (viewer.getSelectedPageIndex()+1) + " of " + viewer.getPageCount();
    //setViewValue("PageText", pageText);
    
    // Reset pageforward enabled
    //setViewEnabled("PageBackButton", viewer.getSelectedPageIndex()>0);
    //setViewEnabled("PageBackAllButton", viewer.getSelectedPageIndex()>0);
    //setViewEnabled("PageForwardButton", viewer.getSelectedPageIndex()<viewer.getPageCount()-1);
    //setViewEnabled("PageForwardAllButton", viewer.getSelectedPageIndex()<viewer.getPageCount()-1);
    
    getView("PageControls").setVisible(false); //viewer.getDoc().getPageCount()>1
}

/**
 * Responds to UI changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get viewer pane and viewer
    ViewerPane viewerPane = getViewerPane();
    Viewer viewer = viewerPane.getViewer();
    
    // Handle ZoomComboBox
    if(anEvent.equals("ZoomText"))
        viewer.setZoomFactor(anEvent.getFloatValue()/100);
    
    // Handle ZoomMenuButton
    if(anEvent.equals("ZoomMenuItem"))
        viewer.setZoomFactor(SnapUtils.floatValue(anEvent.getText())/100);
    
    // Handle ZoomToActualSizeMenuItem - use screen resolution to figure out zooming for actual size
    if(anEvent.equals("ZoomToActualSizeMenuItem"))
        viewer.setZoomFactor(viewer.getZoomToActualSizeFactor());
    
    // Handle ZoomToFitMenuItem
    //if(anEvent.equals("ZoomToFitMenuItem"))
    //    viewer.setZoomMode(RMViewer.ZoomMode.ZoomToFit);
    
    // Handle ZoomAsNeededMenuItem
    //if(anEvent.equals("ZoomAsNeededMenuItem"))
    //    viewer.setZoomMode(RMViewer.ZoomMode.ZoomAsNeeded);
    
    // Handle PageText
    //if(anEvent.equals("PageText"))
    //    viewer.setSelectedPageIndex(anEvent.getIntValue()-1);
    
    // Handle PageBackButton
    //if(anEvent.equals("PageBackButton"))
    //    viewer.pageBack();
    
    // Handle PageBackAllButton
    //if(anEvent.equals("PageBackAllButton"))
    //    viewer.setSelectedPageIndex(0);
    
    // Handle PageForwardButton
    //if(anEvent.equals("PageForwardButton"))
    //    viewer.pageForward();
    
    // Handle PageForwardAllButton
    //if(anEvent.equals("PageForwardAllButton"))
    //    viewer.setSelectedPageIndex(viewer.getPageCount()-1);
    
    // Have viewer pane reset
    viewerPane.resetLater();
}

/**
 * Returns an image for given shape.
 */
private static Image getImage(Shape aShape)
{
    Image img = Image.get(14,22,true); Painter pntr = img.getPainter(); pntr.setColor(Color.DARKGRAY);
    pntr.fill(aShape); pntr.flush(); return img;
}

}