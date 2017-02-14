/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import snap.gfx.Color;
import studio.app.*;
import java.util.*;
import snap.view.*;

/**
 * This class handles editing of RectViews.
 */
public class RectViewTool <T extends RectView> extends ViewTool <T> {
    
/**
 * Returns a new instance of the shape class that this tool is responsible for.
 */
protected T newInstance()  { T rect = super.newInstance(); rect.setBorder(Color.BLACK, 1); return rect; }

protected void initUI()  { }

/**
 * Updates the UI controls from the currently selected rectangle.
 */
public void resetUI()
{
    // Get selected rectangle (just return if null)
    RectView rect = getSelectedView(); if(rect==null) return;
    
    // Update RoundingThumb and RoundingText
    setViewValue("RoundingThumb", rect.getRadius());
    setViewValue("RoundingText", rect.getRadius());
}

/**
 * Updates the currently selected rectangle from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current rect view and rects list (just return if null)
    RectView rect = getSelectedView(); if(rect==null) return;
    List <RectView> rects = (List)getSelectedViews();

    // Handle Rounding Radius Thumb & Text
    if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText")) {
        //rect.undoerSetUndoTitle("Rounding Change");
        float value = anEvent.getFloatValue();
        for(RectView r : rects) {
            r.setRadius(value);
            if(r.getBorder()==null)
                r.setBorder(Color.BLACK, 1);
        }
    }
}

/**
 * Event handling - overridden to install cross-hair cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

/**
 * Returns the class that this tool is responsible for.
 */
public Class getViewClass()  { return RectView.class; }

/**
 * Returns the name to be presented to user.
 */
public String getWindowTitle()  { return "Rectangle Tool"; }

}