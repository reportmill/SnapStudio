/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.app;
import snap.util.*;
import snap.viewx.ExceptionReporter;

/**
 * SnapCode Application entry point.
 */
public class App {

/**
 * Main method to run panel.
 */
//public static void main(String args[])  { snaptea.TV.set(); new EditorPane().newDocument().setWindowVisible(true); }

/**
 * Main method to run panel.
 */
public static void main(String args[])
{
    // Set App Prefs class
    Prefs.setPrefsDefault(Prefs.getPrefs(App.class));
    
    // Install Exception reporter
    ExceptionReporter er = new ExceptionReporter("SnapStudio"); er.setToAddress("support@reportmill.com");
    er.setInfo("SnapCode Version 1, Build Date: " + SnapUtils.getBuildInfo());
    Thread.setDefaultUncaughtExceptionHandler(er);

    // Show open data source panel
    WelcomePanel.getShared().setOnQuit(() -> quitApp());
    WelcomePanel.getShared().showPanel();
}

/**
 * Exits the application.
 */
public static void quitApp()
{
    //if(AppPane.getOpenAppPane()!=null) AppPane.getOpenAppPane().hide();
    Prefs.get().flush();
    System.exit(0);
}

}