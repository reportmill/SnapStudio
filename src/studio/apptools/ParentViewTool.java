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
 * Override to return ParentView class.
 */
public Class <T> getViewClass()  { return (Class<T>)ParentView.class; }

/**
 * Returns the string to be used for the inspector window title.
 */
public String getWindowTitle()  { return "Group Shape Inspector"; }

}