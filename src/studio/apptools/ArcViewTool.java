/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import snap.gfx.Color;
import studio.app.*;
import java.util.*;
import snap.view.*;

/**
 * A tool subclass for editing ArcView.
 */
public class ArcViewTool <T extends ArcView> extends ViewTool <T> {
    
/**
 * Returns a new instance of the view class that this tool is responsible for.
 */
protected T newInstance()
{
    T arc = super.newInstance(); arc.setBorder(Color.BLACK, 1); return arc;
}

/**
 * Updates the UI controls from the currently selected oval.
 */
public void resetUI()
{    
    ArcView arc = getSelectedShape(); if(arc==null) return;
    setViewValue("StartThumb", arc.getStartAngle());
    setViewValue("StartText", arc.getStartAngle());
    setViewValue("SweepThumb", arc.getSweepAngle());
    setViewValue("SweepText", arc.getSweepAngle());
}

/**
 * Updates the currently selected oval from the UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    ArcView arc = getSelectedShape(); if(arc==null) return;
    List <ArcView> arcs = (List)getSelectedShapes();
    
    // Handle StartThumb, StartText
    if(anEvent.equals("StartThumb") || anEvent.equals("StartText")) {
        //arc.undoerSetUndoTitle("Start Angle Change");
        double angle = anEvent.getFloatValue();
        arcs.forEach(i -> i.setStartAngle(angle));
    }

    // Handle SweepThumb, SweepText
    if(anEvent.equals("SweepThumb") || anEvent.equals("SweepText")) {
        //arc.undoerSetUndoTitle("Sweep Angle Change");
        double angle = anEvent.getFloatValue();
        arcs.forEach(i -> i.setSweepAngle(angle));
    }
}

/**
 * Event handling - overridden to install crosshair cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.CROSSHAIR); }

/**
 * Returns the view class this tool is responsible for.
 */
public Class <T> getViewClass()  { return (Class<T>)ArcView.class; }

/**
 * Returns the string used for the inspector window title.
 */
public String getWindowTitle()  { return "Oval Tool"; }

}