package studio.app;
import java.util.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.RecentFiles;
import snap.web.*;

/**
 * An implementation of a panel to manage/open user Snap sites (projects).
 */
public class WelcomePanel extends ViewOwner {

    // The selected file
    WebFile                 _selFile;
    
    // Whether welcome panel should exit on hide
    boolean                 _exit;
    
    // The Runnable to be called when app quits
    Runnable                _onQuit;

    // The shared instance
    static WelcomePanel     _shared;

/**
 * Returns the shared instance.
 */
public static WelcomePanel getShared()
{
    if(_shared!=null) return _shared;
    return _shared!=null? _shared : (_shared = new WelcomePanel());
}

/**
 * Shows the welcome panel.
 */
public void showPanel()
{
    getUI(); // This is bogus - if this isn't called, Window node get reset
    getWindow().setVisible(true); //getTimeline().play();
    resetLater();
}

/**
 * Hides the welcome panel.
 */
public void hide()
{
    // Hide window and stop animation
    getWindow().setVisible(false); //getTimeline().stop();
    
    // Write current list of sites, flush prefs and mayb exit
    //writeSites();         // Write data file for open/selected sites
    Prefs.get().flush();    // Flush preferences
    if(_exit) quitApp(); // If exit requested, quit app
}

/**
 * Returns the selected file.
 */
public WebFile getSelFile()  { return _selFile; }

/**
 * Sets the selected file.
 */
public void setSelFile(WebFile aFile)  { _selFile = aFile; }

/**
 * Returns the Runnable to be called to quit app.
 */
public Runnable getOnQuit()  { return _onQuit; }

/**
 * Sets the Runnable to be called to quit app.
 */
public void setOnQuit(Runnable aRunnable)  { _onQuit = aRunnable; }

/**
 * Called to quit app.
 */
public void quitApp()  { _onQuit.run(); }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Add WelcomePaneAnim.snp view
    View anim = getAnimView();
    getUI(ChildView.class).addChild(anim, 0); anim.playAnimDeep();
    
    // Enable SitesTable MouseReleased
    TableView sitesTable = getView("SitesTable", TableView.class);
    sitesTable.setRowHeight(24);
    List <WebFile> rfiles = getRecentFiles(); if(rfiles.size()>0) _selFile = rfiles.get(0);
    enableEvents(sitesTable, MouseRelease);
    
    // Set preferred size
    getUI().setPrefSize(400,480);
    
    // Configure Window: Add WindowListener to indicate app should exit when close button clicked
    WindowView win = getWindow(); win.setTitle("Welcome"); win.setResizable(false);
    enableEvents(win, WinClose);
    getView("OpenButton", Button.class).setDefaultButton(true);
}

/**
 * Resets UI.
 */
public void resetUI()
{
    //setViewEnabled("OpenButton", getSelectedFile()!=null);
}

/**
 * Responds to UI changes.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle SitesTable double-click
    if(anEvent.equals("SitesTable") && anEvent.getClickCount()>1) {
        WebFile file = (WebFile)getViewSelItem("SitesTable");
        openFile(file);
    }
    
    // Handle NewButton
    if(anEvent.equals("NewButton")) {
        newFile();
    }
    
    // Handle OpenButton
    if(anEvent.equals("OpenButton"))
        openFile(null);
    
    // Handle QuitButton
    if(anEvent.equals("QuitButton")) {
        _exit = true; hide(); }
        
    // Handle WinClosing
    if(anEvent.isWinClose()) {
        _exit = true; hide(); }
}

/**
 * Creates a new file.
 */
protected void newFile()
{
    EditorPane epane = new EditorPane().newDocument();
    epane.setWindowVisible(true);
    hide();
}

/**
 * Opens selected file.
 */
public void openFile(Object aSource)
{
    // Have editor run open panel (if no document opened, just return)
    EditorPane epane = new EditorPane();
    if(aSource!=null) epane = epane.open(aSource);
    else epane = epane.open(getUI());
    if(epane==null) return;
    
    // Make editor window visible and hide welcome
    epane.setWindowVisible(true);
    hide();
    
    // Register file with RecentFiles
    RecentFiles.addPath("RecentDocuments", epane.getSourceURL().getPath(), 99);
}

/**
 * Returns the list of the recent documents as a list of strings.
 */
public static List <WebFile> getRecentFiles()  { return RecentFiles.getFiles("RecentDocuments"); }

/** Loads the WelcomePaneAnim.snp DocView. */
DocView getAnimView()
{
    // Unarchive WelcomePaneAnim.snp as DocView
    WebURL url = WebURL.getURL(getClass(), "WelcomePanelAnim.snp");
    DocView doc = (DocView)new ViewArchiver().getView(url);
    
    // Set BuildText and JavaText
    View bt = doc.getChild("BuildText"); bt.setText("Build: " + SnapUtils.getBuildInfo());
    View jt = doc.getChild("JVMText"); jt.setText("JVM: " + System.getProperty("java.runtime.version"));
    
    // Clear Effects and return doc
    PageView page = doc.getPage(); page.setEffect(null); page.setBorder(null);
    return doc;
}

}