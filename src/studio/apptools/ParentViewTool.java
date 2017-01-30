/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import studio.app.*;
import snap.view.*;

/**
 * A tool class for ParentView.
 */
public class ParentViewTool <T extends ParentView> extends ViewTool <T> {

/**
 * Override to return shape class.
 */
public Class<T> getShapeClass()  { return (Class<T>)ParentView.class; }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "Group Shape Inspector"; }

/**
 * Called to handle dropping a string.
 */
public void dropString(T aShape, ViewEvent anEvent)
{
    Clipboard cb = anEvent.getDragboard(); //Transferable transferable = anEvent.getTransferable();
    getEditor().undoerSetUndoTitle("Drag and Drop Key");
    EditorClipboard.paste(getEditor(), cb, aShape, anEvent.getPoint());    
}

}