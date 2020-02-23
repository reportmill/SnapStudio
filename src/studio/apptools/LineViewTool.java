/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import java.util.List;

import snap.geom.Point;
import snap.geom.Pos;
import studio.app.*;
import snap.gfx.*;
import snap.view.*;

/**
 * This class handles creation of lines.
 */
public class LineViewTool <T extends LineView> extends ViewTool <T> {
    
    // Indicates whether line should try to be strictly horizontal or vertical
    boolean               _hysteresis = false;
    
    // The list of arrow head shapes
    List <LineView>       _arrowShapes;

    // Constants for line segment points
    public static final byte HandleStartPoint = 0;
    public static final byte HandleEndPoint = 1;

/**
 * Returns the shape class that this tool is responsible for.
 */
public Class getViewClass()  { return LineView.class; }

/**
 * Returns the name of this tool to be displayed by inspector.
 */
public String getWindowTitle()  { return "Line Inspector"; }

/**
 * Event handling - overridden to install cross-hair cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

/**
 * Handles mouse press for line creation.
 */
public void mousePressed(ViewEvent anEvent)  { super.mousePressed(anEvent); _hysteresis = true; }

/**
 * Handles mouse drag for line creation.
 */
public void mouseDragged(ViewEvent anEvent)
{
    Point dragPoint = getEditorEvents().getEventPointInShape(true);
    double dx = Math.abs(dragPoint.x - _downPoint.x);
    double dy = Math.abs(dragPoint.y - _downPoint.y);
    double breakAmt = 20f;
    
    if(_hysteresis) {
        if(dx>dy) {
            if(dy<breakAmt) dragPoint.y = _downPoint.y;
            else _hysteresis = false;
        }
        
        else if(dx<breakAmt) dragPoint.x = _downPoint.x;
        else _hysteresis = false;
    }
    
    // Set adjusted bounds
    _view.setLineInParent(_downPoint.x, _downPoint.y, dragPoint.x, dragPoint.y);
}

/**
 * Editor method (returns the number of handles).
 */
public int getHandleCount(T aShape)  { return 2; }

/**
 * Returns the handle position for given index.
 */
public Pos getHandlePos(T aShape, int anIndex)
{
    Pos pos = aShape.getOriginPos();
    return anIndex==0? pos : pos.getOpposing();
}

/**
 * Editor method.
 */
public void moveViewHandle(ViewHandle <T> aViewHandle, Point aPoint)
{
    // Get line view and handle
    T lview = aViewHandle.view;
    Pos handle = getHandlePos(lview, aViewHandle.index); // Re-evaluate since handle/index might have flipped
    
    // Get opposite point from line view handle
    Point p0 = getHandlePoint(lview, handle.getOpposing(), false);
    p0 = lview.localToParent(p0.x, p0.y);
    
    // Get ends points and set (flip if moving handle 0)
    Point p1 = aPoint; if(aViewHandle.index==0) { Point p = p0; p0 = p1; p1 = p; }
    lview.setLineInParent(p0.x, p0.y, p1.x, p1.y);
}

/**
 * Loads the list of arrow shapes from a .rpt file.
 */
public List <LineView> getArrows()
{
    if(_arrowShapes!=null) return _arrowShapes;
    //RMDocument doc = new RMDocument(getClass().getResource("RMLineShapeToolArrowHeads.rpt"));
    return _arrowShapes;// = doc.getChildrenWithClass(getShapeClass());
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get arrows menu button
    MenuButton menuButton = getView("ArrowsMenuButton", MenuButton.class);
        
    // Add arrows menu button
    /*for(int i=0; i<getArrows().size(); i++) { LineView line = getArrows().get(i);
        Image image = RMShapeUtils.createImage(line.getArrowHead(), null);
        MenuItem mitem = new MenuItem(); mitem.setImage(image);
        mitem.setName("ArrowsMenuButtonMenuItem" + i);
        menuButton.addItem(mitem);
    }*/
    
    // Add "None" menu item
    MenuItem mitem = new MenuItem(); mitem.setText("None"); mitem.setName("ArrowsMenuButtonMenuItem 999");
    menuButton.addItem(mitem);
}

/**
 * Update UI panel.
 */
public void resetUI()
{
    // Get selected line and arrow head
    LineView line = getSelectedView(); if(line==null) return;
    //LineView.ArrowHead ahead = line.getArrowHead();
    
    // Update ArrowsMenuButton
    //Image image = ahead!=null? RMShapeUtils.createImage(line.getArrowHead(), null) : null;
    //getView("ArrowsMenuButton", MenuButton.class).setImage(image);

    // Update ScaleText and ScaleThumbWheel
    //setViewValue("ScaleText", ahead!=null? ahead.getScaleX() : 0);
    //setViewValue("ScaleThumbWheel", ahead!=null? ahead.getScaleX() : 0);
    //setViewEnabled("ScaleText", ahead!=null);
    //setViewEnabled("ScaleThumbWheel", ahead!=null);
}

/**
 * Respond to UI change.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get selected shape and arrow head
    LineView line = getSelectedView();
    //LineView.ArrowHead arrowHead = line.getArrowHead();

    // Handle ScaleText and ScaleThumbWheel
    //if(anEvent.equals("ScaleText") || anEvent.equals("ScaleThumbWheel"))
    //    arrowHead.setScaleXY(anEvent.getFloatValue(), anEvent.getFloatValue());

    // Handle ArrowsMenuButtonMenuItem
    /*if(anEvent.getName().startsWith("ArrowsMenuButtonMenuItem")) {
        int index = SnapUtils.intValue(anEvent.getName());
        RMLineShape.ArrowHead ahead = index<getArrows().size()? getArrows().get(index).getArrowHead() : null;
        line.setArrowHead(ahead!=null? (RMLineShape.ArrowHead)ahead.clone() : null);
    }*/
}

}