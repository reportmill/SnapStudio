package studio.app;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.web.WebURL;

/**
 * The RMViewer class is a JComponent subclass that can be used in Java client applications to display and/or print an
 * RMDocument.
 *
 * You might use it like this to simply print a document:
 * <p><blockquote><pre>
 *   new RMViewer(aDocument).print();
 * </pre></blockquote><p>
 * Or you might want to allocate one and add it to a Swing component hierarchy:
 * <p><blockquote><pre>
 *   RMViewer viewer = new RMViewer(); viewer.setContent(new RMDocument(aSource));
 *   myFrame.getContentPane().add(new JScrollPane(viewer));
 * </pre></blockquote>
 */
public class Viewer extends Box { //implements PropChangeListener {

    // The Source URL
    WebURL                   _url;

    // The view used to manage real root of views
    Box                      _cbox = new Box();
    
    // The Zoom mode
    ZoomMode                 _zoomMode = ZoomMode.ZoomAsNeeded;
    
    // Zoom factor
    double                   _zoomFactor = 1;
    
    // The previous zoom factor (for toggle zoom)
    double                   _lastZoomFactor = 1;

    // Zoom modes
    public enum ZoomMode { ZoomToFit, ZoomAsNeeded, ZoomToFactor };
    
    // Constants for PropertyChanges
    public static final String Content_Prop = "Content";
        
/**
 * Creates a new RMViewer with an empty document in it.
 */
public Viewer()
{
    enableEvents(MouseEvents); enableEvents(KeyEvents);
    setFocusable(true); setFocusWhenPressed(true);
    setFill(Color.LIGHTGRAY);
    super.setContent(_cbox);
    _cbox.setFill(ViewUtils.getBackFill()); _cbox.setFillWidth(true); _cbox.setFillHeight(true);
    _cbox.setEffect(new ShadowEffect());
    _cbox.setPickable(false);
}

/**
 * Returns the viewer view.
 */
public Box getContentBox()  { return _cbox; }

/**
 * Returns the root view that is the content of this viewer.
 */
public ParentView getContent()  { return (ParentView)_cbox.getContent(); }

/**
 * Sets the root view that is the content of this viewer.
 */
public void setContent(View aView)
{
    // If already set, just return
    if(aView==getContent()) return;
    
    // Set new document and fire property change
    ParentView pview = getContent(); _cbox.setContent(aView);
    firePropChange(Content_Prop, pview, aView);
    
    // If content not DocView or SpringView, set min size
    if(aView instanceof DocView || aView instanceof SpringView) _cbox.setMinSize(-1,-1);
    else _cbox.setMinSize(500,500);
    
    // Set ZoomToFitFactor and relayout/repaint (for possible size change)
    setZoomToFitFactor();
    relayout(); repaint();
}

/**
 * Sets the content from any source.
 */
public void setContent(Object aSource)
{
    ViewArchiver varch = new ViewArchiver();
    ParentView pview = varch.getParentView(aSource);
    _url = varch.getSourceURL();
    setContent(pview);
}

/**
 * Returns the source URL.
 */
public WebURL getSourceURL()  { return _url; }

/**
 * Returns a point converted from viewer coords to the coordinate space of the given view.
 */
public Point localToView(View aView, double aX, double aY)
{
    View view = aView!=null? aView : getContent().getParent();
    return view.parentToLocal(this, aX, aY);
}

/**
 * Returns a point converted from viewer coords to the coordinate space of the given view.
 */
public Shape localToView(View aView, Shape aShape)
{
    View view = aView!=null? aView : getContent().getParent();
    return view.parentToLocal(this, aShape);
}

/**
 * Returns a point converted from viewer coords to the coordinate space of the given view.
 */
public Point viewToLocal(View aView, double aX, double aY)
{
    View view = aView!=null? aView : getContent().getParent();
    return view.localToParent(this, aX, aY);
}

/**
 * Returns a point converted from viewer coords to the coordinate space of the given view.
 */
public Shape viewToLocal(View aView, Shape aShape)
{
    View view = aView!=null? aView : getContent().getParent();
    return view.localToParent(this, aShape);
}

/**
 * Returns the viewer's zoom factor (1 by default).
 */
public double getZoomFactor()  { return _zoomFactor; }

/**
 * Sets the viewer's zoom factor (1 for 100%).
 */
public void setZoomFactor(double aFactor)
{
    setZoomMode(ZoomMode.ZoomToFactor);
    setZoomFactorImpl(aFactor);
}

/**
 * Sets the viewer's zoom factor (1 for 100%) and mode.
 */
protected void setZoomFactorImpl(double aFactor)
{    
    // Constrain zoom factor to valid range (ZoomToFactor: 20%...10000%, ZoomAsNeed: Max of 1)
    ZoomMode zmode = getZoomMode();
    if(zmode==ZoomMode.ZoomToFactor) aFactor = Math.min(Math.max(.2f, aFactor), 100);
    else if(zmode==ZoomMode.ZoomAsNeeded) aFactor = Math.min(aFactor, 1);

    // If already at given factor, just return
    if(aFactor==_zoomFactor) return;

    // Set last zoom factor and new zoom factor and fire property change
    firePropChange("ZoomFactor", _lastZoomFactor = _zoomFactor, _zoomFactor = aFactor);
    
    // If ZoomToFactor and parent is viewport, resize and scroll to center of previous zoom
    if(isZoomToFactor()) {
        Rect vr = getZoomFocusRect(), vr2 = vr.clone();
        setSize(getPrefWidth(), getPrefHeight());
        vr2.scale(_zoomFactor/_lastZoomFactor);
        vr2.inset((vr2.getWidth() - vr.getWidth())/2, (vr2.getHeight() - vr.getHeight())/2);
        setVisRect(vr2);
    }
    
    // Relayout and repaint
    relayout(); repaint();
}

/**
 * Returns the ZoomMode (ZoomToFit, ZoomIfNeeded, ZoomToFactor).
 */
public ZoomMode getZoomMode()  { return _zoomMode; }

/**
 * Sets the ZoomMode.
 */
public void setZoomMode(ZoomMode aZoomMode)
{
    if(aZoomMode==getZoomMode()) return;
    firePropChange("ZoomMode", _zoomMode, _zoomMode = aZoomMode);
    setZoomToFitFactor(); // Reset ZoomFactor
}

/**
 * Returns whether viewer is set to ZoomToFactor.
 */
public boolean isZoomToFactor()  { return getZoomMode()==ZoomMode.ZoomToFactor; }

/**
 * Returns the zoom factor for the given mode at the current viewer size.
 */
public double getZoomFactor(ZoomMode aMode)
{
    // If ZoomToFactor, just return ZoomFactor
    if(aMode==ZoomMode.ZoomToFactor) return getZoomFactor();
    
    // Get ideal size and current size (if size is zero, return 1)
    double pw = 1, ph = 1; //getPrefWidthBase(), ph = getPrefHeightBase();
    double width = getWidth(), height = getHeight(); if(width==0 || height==0) return 1;
    
    // If ZoomAsNeeded and IdealSize is less than size, return
    if(aMode==ZoomMode.ZoomAsNeeded && pw<=width && ph<=height) return 1;
    if(aMode==ZoomMode.ZoomToFit && pw==width && ph==height) return 1;
    
    // Otherwise get ratio of parent size to ideal size (with some gutter added in) and return smaller axis
    double zw = width/(pw + 8f), zh = height/(ph + 8f);
    return Math.min(zw, zh);
}

/**
 * Sets the zoom to fit factor, based on the current zoom mode.
 */
public void setZoomToFitFactor()  { setZoomFactorImpl(getZoomFactor(getZoomMode())); }

/**
 * Returns zoom focus rect (just the visible rect by default, but overriden by editor to return selected views rect).
 */
public Rect getZoomFocusRect()  { return getVisRect(); }

/**
 * Returns the zoom factor to view the document at actual size taking into account the current screen resolution.
 */
public double getZoomToActualSizeFactor()  { return GFXEnv.getEnv().getScreenResolution()/72; }

/**
 * Sets the viewer's zoom to its previous value.
 */
public void zoomToggleLast()  { setZoomFactor(_lastZoomFactor); }

/**
 * Overrides to update ZoomFactor if dynamic.
 */
public void setWidth(double aValue)  { super.setWidth(aValue); setZoomToFitFactor(); }

/**
 * Overrides to update ZoomFactor if dynamic.
 */
public void setHeight(double aValue)  { super.setHeight(aValue); setZoomToFitFactor(); }

/**
 * Returns the content view's X location in viewer.
 */
public int getContentX()
{
    float align = .5f; //ViewUtils.getAutosizeAlignmentX(getContent());
    return (int)Math.round(Math.max((getWidth()-getContent().getWidth()*getZoomFactor())*align, 0));
}

/**
 * Returns the content view's Y location in viewer.
 */
public int getContentY()
{
    float align = .5f; //ViewUtils.getAutosizeAlignmentY(getContent());
    return (int)Math.round(Math.max((getHeight()-getContent().getHeight()*getZoomFactor())*align, 0));
}

/**
 * Override to paint viewer views and page, margin, grid, etc.
 */
public void paintFront(Painter aPntr)
{
    //Rect bnds = new Rect(0, 0, getWidth(), getHeight()); double scale = getZoomFactor();
    //RMShapePaintProps props = createShapePaintProps(); if(props!=null) aPntr.setProps(props);
    //RMShapeUtils.paintShape(aPntr, _vshape, bnds, scale);
    //if(props!=null) aPntr.setProps(null); //RMShapePainter spntr = getShapePainter(aPntr); spntr.paintShape(_vshape);
}

/**
 * Handle mouse events.
 */
protected void processEvent(ViewEvent anEvent)
{
    super.processEvent(anEvent); // Do normal version
    //getInputAdapter().processEvent(anEvent); // Forward to input adapter
}

/**
 * Returns the undoer associated with the viewer's document.
 */
public Undoer getUndoer()  { return null; }

/**
 * Sets the title of the next registered undo in the viewer's documents's undoer (convenience).
 */
public void undoerSetUndoTitle(String aTitle)
{
    if(getUndoer()!=null)
        getUndoer().setUndoTitle(aTitle);
}

/**
 * Returns whether undos exist in the viewer's documents's undoer (convenience).
 */
public boolean undoerHasUndos()  { return getUndoer()!=null && getUndoer().hasUndos(); }

}