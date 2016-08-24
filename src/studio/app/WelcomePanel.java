package studio.app;
import java.util.*;
import snap.util.*;
import snap.view.*;
import snap.web.*;

/**
 * An implementation of a panel to manage/open user Snap sites (projects).
 */
public class WelcomePanel extends ViewOwner {

    // The list of files
    List <WebFile>          _files;

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
    PrefsUtils.flush();    // Flush preferences
    if(_exit) quitApp(); // If exit requested, quit app
}

/**
 * Returns the number of files.
 */
public int getFileCount()  { return getFiles().size(); }

/**
 * Returns the file at given index.
 */
public WebFile getFile(int anIndex)  { return getFiles().get(anIndex); }

/**
 * Returns the list of files.
 */
public List <WebFile> getFiles()  { return _files!=null? _files : (_files=new ArrayList()); }

/**
 * Returns the selected file.
 */
public WebFile getSelectedFile()  { return _selFile; }

/**
 * Sets the selected file.
 */
public void setSelectedFile(WebFile aFile)  { _selFile = aFile; }

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
    // Add WelcomePaneAnim node
    WelcomePanelAnim anim = new WelcomePanelAnim();
    getUI(ChildView.class).addChild(anim.getUI(), 0); anim.getUI().getAnimator(true).play();
    
    // Enable SitesTable MouseClicked
    TableView sitesTable = getView("SitesTable", TableView.class);
    sitesTable.setRowHeight(24); //sitesTable.setStyle(new Style().setFontSize(10).toString());
    enableEvents(sitesTable, MouseClicked);
    
    // Set preferred size
    getUI().setPrefSize(400,480);
    
    // Configure Window: Add WindowListener to indicate app should exit when close button clicked
    WindowView win = getWindow(); win.setTitle("Welcome"); win.setResizable(false);
    enableEvents(win, WinClosing);
    getView("OpenButton", Button.class).setDefaultButton(true);
}

/**
 * Resets UI.
 */
public void resetUI()
{
    setViewEnabled("OpenButton", getSelectedFile()!=null);
}

/**
 * Responds to UI changes.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle SitesTable double-click
    if(anEvent.equals("SitesTable") && anEvent.getClickCount()>1)
        if(getView("OpenButton", Button.class).isEnabled()) {
            hide();
            openFile();
        }
    
    // Handle NewButton
    if(anEvent.equals("NewButton")) {
        newFile();
    }
    
    // Handle OpenButton
    if(anEvent.equals("OpenButton")) {
        hide();
        openFile();
    }
    
    // Handle QuitButton
    if(anEvent.equals("QuitButton")) {
        _exit = true; hide(); }
        
    // Handle WinClosing
    if(anEvent.isWinClosing()) {
        _exit = true; hide(); }
}

/**
 * Creates a new file.
 */
protected void newFile()
{
    EditorPane epane = new EditorPane().newDocument();
    epane.setWindowVisible(true);
}

/**
 * Opens selected file.
 */
public void openFile()
{
    WebFile file = getSelectedFile();
    //Editor ed = new Editor(file);
    //ed.show();
}

/**
 * A viewer owner to load/view WelcomePanel animation from WelcomePanelAnim.rpt.
 */
private static class WelcomePanelAnim extends ViewOwner {  //RMViewerOwner {

    /** Needed since file is rpt instead of jfx. */
    public Object getUISource() { return WebURL.getURL(getClass(), "WelcomePanelAnim.rpt"); }

    /** Initialize some fields. */
    protected void initUI()
    {
        setViewText("BuildText", "Build: " + SnapUtils.getBuildInfo());
        setViewText("JVMText", "JVM: " + System.getProperty("java.runtime.version"));
        DocView doc = getUI(DocView.class);
        PageView page = (PageView)doc.getPage();
        page.setEffect(null); page.setBorder(null);
    }
}

}