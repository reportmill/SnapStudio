/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import snap.gfx.Color;
import studio.app.*;
import java.util.*;
import snap.view.*;

/**
 * This class handles editing of rectangle shapes.
 */
public class RectViewTool <T extends RectView> extends ViewTool <T> {
    
/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()
{
    T view = super.newInstance();
    view.setBorder(Color.BLACK, 1); //shape.setStroke(new RMStroke());
    return view;
}

protected void initUI()  { }

/**
 * Updates the UI controls from the currently selected rectangle.
 */
public void resetUI()
{
    // Get selected rectangle (just return if null)
    RectView rect = getSelectedShape(); if(rect==null) return;
    
    // Update RoundingThumb and RoundingText
    //setViewValue("RoundingThumb", rect.getRadius());
    //setViewValue("RoundingText", rect.getRadius());
}

/**
 * Updates the currently selected rectangle from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current rectangle (just return if null)
    RectView rect = getSelectedShape(); if(rect==null) return;
    
    // Get selected rectangles
    List <RectView> rects = (List)getSelectedShapes();
    
    // Register rects for repaint (and thus undo)
    //RMShapeUtils.repaint(rects);

    // Handle Rounding Radius Thumb & Text
    /*if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText")) {
        rect.undoerSetUndoTitle("Rounding Change");
        float value = anEvent.getFloatValue();
        for(RMRectShape r : rects) {
            r.setRadius(value);
            if(r.getStroke()==null)
                r.setStroke(new RMStroke());
        }
    }*/
}

/**
 * Event handling - overridden to install cross-hair cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

/**
 * Returns the class that this tool is responsible for.
 */
public Class getShapeClass()  { return RectView.class; }

/**
 * Returns the name to be presented to user.
 */
public String getWindowTitle()  { return "Rectangle Tool"; }

}