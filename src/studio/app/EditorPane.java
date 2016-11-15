package studio.app;
import java.io.File;
import java.util.*;
import snap.gfx.*;
import snap.view.*;
import snap.web.*;
import snap.util.*;

/**
 * This class is a container for an Editor in an enclosing ScrollView with tool bars for editing.
 */
public class EditorPane extends ViewerPane {

    // The menu bar owner
    EditorPaneMenuBar    _menuBar;

    // The editor ruler nodes
    EditorRuler          _hruler, _vruler;
    
    // The original editor, if in preview mode
    Editor               _realEditor;
    
    // The shared editor inspector
    InspectorPanel         _inspPanel = createInspectorPanel();
    
    // The shared attributes inspector (go ahead and create to get RMColorPanel created)
    AttributesPanel        _attrsPanel = createAttributesPanel();
    
    // The ViewTree
    TreeView <View>        _viewTree;
    
    // The ViewTree box
    BorderView             _inspBox;
    
    // The ViewTree box
    BorderView             _viewTreeBox;
    
    // The image for a window frame icon
    static Image           _frameIcon = Image.get(EditorPane.class, "ReportMill16x16.png");

/**
 * Creates a new EditorPane.
 */
public EditorPane()  { }

/**
 * Returns the viewer as an editor.
 */
public Editor getEditor()  { return (Editor)getViewer(); }

/**
 * Overridden to return an Editor.
 */
protected Viewer createViewer()  { return new Editor(); }

/**
 * Override to return as EditorPaneToolBar.
 */
public EditorPaneToolBar getTopToolBar()  { return (EditorPaneToolBar)super.getTopToolBar(); }

/**
 * Creates the top tool bar.
 */
public ViewOwner createTopToolBar()  { return new EditorPaneToolBar(this); }

/**
 * Returns the SwingOwner for the menu bar.
 */
public EditorPaneMenuBar getMenuBar()  { return _menuBar!=null? _menuBar : (_menuBar = createMenuBar()); }

/**
 * Creates the EditorPaneMenuBar for the menu bar.
 */
public EditorPaneMenuBar createMenuBar()  { return new EditorPaneMenuBar(this); }

/**
 * Returns whether editor pane shows rulers.
 */
public boolean getShowRulers()  { return _hruler!=null; }

/**
 * Sets whether editor pane shows rulers.
 */
public void setShowRulers(boolean aValue)
{
    // If showing rulers is already equal value, just return
    if(aValue==getShowRulers()) return;
    
    // Determine if we should resize window after toggle (depending on whether window is at preferred size)
    WindowView win = getWindow();
    boolean doPack = win.getSize().equals(win.getPrefSize());
    
    // If no rulers, create and add them
    if(_hruler==null) {
        getScrollBorderView().setTop(_hruler = new EditorRuler(getEditor(), EditorRuler.HORIZONTAL));
        getScrollBorderView().setLeft(_vruler = new EditorRuler(getEditor(), EditorRuler.VERTICAL));
    }
    
    // Otherwise, remove and clear them
    else {
        getScrollBorderView().setTop(null);
        getScrollBorderView().setLeft(null);
        _hruler = _vruler = null;
    }
    
    // Resize window if window was previously at preferred size
    if(doPack)
        getWindow().pack();
}

/**
 * Initializes the UI.
 */
protected View createUI()
{
    BorderView bpane = (BorderView)super.createUI(); //bpane.setGrowWidth(true);
    VBox vbox = new VBox(); vbox.setFillWidth(true); vbox.setPrefWidth(275);
    vbox.setChildren(getAttributesPanel().getUI(), getInspectorBox(), getViewTreeBox());
    vbox.setBorder(Border.createLineBorder(Color.LIGHTGRAY, 1));
    //HBox hbox = new HBox(); hbox.setAlignment(Pos.TOP_LEFT); hbox.setFillHeight(true); hbox.setChildren(bpane, vbox);
    bpane.setRight(vbox);
    return bpane;
}

/**
 * Override to configure Window.
 */
protected void initUI()
{
    // Do normal version
    super.initUI();
    
    // Enable Mouse Events for editor
    enableEvents(getEditor(), MousePress, MouseRelease);
    
    // Configure Window ClassName, MenuBar, Image and enable window events
    WindowView win = getWindow();
    win.setType(WindowView.TYPE_MAIN);
    win.setMenuBar(getMenuBar().getUI());
    win.setImage(getFrameIcon());
    enableEvents(win, WinClose);
}

/**
 * Updates the editor's UI panels.
 */
protected void resetUI()
{
    // Do normal update
    super.resetUI();
    
    // If title has changed, update window title
    if(isWindowVisible()) {
        String title = getWindowTitle();
        WindowView win = getWindow();
        if(!SnapUtils.equals(title, win.getTitle())) {
            win.setTitle(title);
            WebFile dfile = getSourceURL()!=null? getSourceURL().getFile() : null;
            File file = dfile!=null? dfile.getStandardFile() : null;
            if(file!=null && file.exists()) win.setDocFile(file);
        }
    }
    
    // Update the rulers if visible
    if(_hruler!=null) { _hruler.repaint(); _vruler.repaint(); }
    
    // Reset MenuBar, InspectorPanel and AttributesPanel
    getMenuBar().resetLater();
    if(getInspectorPanel().isVisible()) getInspectorPanel().resetLater();
    if(getAttributesPanel().isVisible()) getAttributesPanel().resetLater();
    
    // ResetViewTree
    View content = getContent();
    _viewTree.setItems(content);
    _viewTree.expandItem(content);
    _viewTree.setSelectedItem(getEditor().getSelectedOrSuperSelectedShape());
}

/**
 * Handles changes to the editor's UI controls.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Forward on to menu bar
    getMenuBar().fireEvent(anEvent);
    
    // Do normal version
    super.respondUI(anEvent);
    
    // Handle PopupTrigger
    if(anEvent.isPopupTrigger())
        runPopupMenu(anEvent);
    
    // Handle WinClosing
    if(anEvent.isWinClose()) {
        close(); anEvent.consume(); }
    //else if(anEvent.isWinResized()) { //Dimension wsize=getWindow().getSize(), psize=getWindow().getPreferredSize();
        //if(Math.abs(wsize.width-psize.width)<=10) wsize.width = psize.width;
        //if(Math.abs(wsize.height-psize.height)<=10) wsize.height = psize.height;
        //if(getWindow().getWidth()!=wsize.width || getWindow().getHeight()!=wsize.height) getWindow().setSize(wsize);
        
    // Handle ViewTree
    if(anEvent.equals("ViewTree"))
        getEditor().setSelectedShape(_viewTree.getSelectedItem());
}

/**
 * Returns the inspector panel (shared).
 */
public InspectorPanel getInspectorPanel()  { return _inspPanel; }

/**
 * Returns the Inspector box.
 */
public BorderView getInspectorBox()
{
    if(_inspBox!=null) return _inspBox;
    BorderView bview = new BorderView();
    Label label = new Label("View Attributes"); label.setPadding(4,4,4,5); label.setFill(new Color("#E1E1E1"));
    label.setBorder(Border.createCompoundBorder(Border.createEmptyBorder(3,3,0,3),Border.createLoweredBevelBorder()));
    bview.setTop(label); bview.setCenter(getInspectorPanel().getUI());
    return _inspBox = bview;
}

/**
 * Creates the InspectorPanel.
 */
protected InspectorPanel createInspectorPanel()  { return new InspectorPanel(this); }

/**
 * Returns the attributes panel (shared).
 */
public AttributesPanel getAttributesPanel()  { return _attrsPanel; }

/**
 * Creates the AttributesPanel.
 */
protected AttributesPanel createAttributesPanel()  { return new AttributesPanel(this); }

/**
 * Returns the ViewTree.
 */
public TreeView <View> getViewTree()
{
    if(_viewTree!=null) return _viewTree;
    TreeView tview = new TreeView(); tview.setName("ViewTree"); tview.setGrowHeight(true);
    tview.setResolver(new ViewTreeResolver()); tview.setBorder(Color.GRAY, 1);
    return _viewTree = tview;
}

/**
 * Returns the ViewTree box.
 */
public BorderView getViewTreeBox()
{
    if(_viewTreeBox!=null) return _viewTreeBox;
    BorderView bview = new BorderView(); bview.setPadding(3,3,3,3); bview.setGrowHeight(true); bview.setPrefHeight(240);
    Label label = new Label("View Hierarchy"); label.setPadding(4,4,4,5); label.setFill(new Color("#E1E1E1"));
    label.setBorder(Border.createLoweredBevelBorder());
    bview.setTop(label); bview.setCenter(new ScrollView(getViewTree()));
    return _viewTreeBox = bview;
}

/**
 * Returns extension for editor document.
 */
public String[] getFileExtensions()  { return new String[] { ".snp", ".rpt", ".rib" }; }

/**
 * Returns the description for the editor document for use in open/save panels.
 */
public String getFileDescription()  { return "SnapStudio files (.snp)"; }

/**
 * Returns the window title.
 */
public String getWindowTitle()
{
    // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
    String title = getSourceURL()!=null? getSourceURL().getPath() : null; if(title==null) title = "Untitled";

    // If has undos, add asterisk. If zoomed, add ZoomFactor
    if(getEditor().getUndoer()!=null && getEditor().getUndoer().hasUndos()) title = "* " + title;
    if(!MathUtils.equals(getEditor().getZoomFactor(), 1f))
        title += " @ " + Math.round(getEditor().getZoomFactor()*100) + "%";
    return title;
}

/**
 * Returns the icon for the editor window frame.
 */
public static Image getFrameIcon()  { return _frameIcon; }

/**
 * Creates a new default editor pane.
 */
public EditorPane newDocument()  { return open("/Temp/RM14/src/com/reportmill/app/FontPanel.rib"); }
//return open(new RMDocument(612, 792)); }

/**
 * Creates a new editor window from an open panel.
 */
public EditorPane open(View aView)
{
    // Get path from open panel for supported file extensions
    FileChooser fc = getEnv().getFileChooser(); fc.setDesc(getFileDescription()); fc.setExts(getFileExtensions());
    String path = fc.showOpenPanel(aView);
    return open(path);
}

/**
 * Creates a new editor window by opening the document from the given source.
 */
public EditorPane open(Object aSource)
{
    // If document source is null, just return null
    if(aSource==null) return null;
    
    // Get Source URL
    WebURL url = null; try { url = WebURL.getURL(aSource); } catch(Exception e) { }
    
    // If source is already opened, return editor pane
    if(!SnapUtils.equals(url, getSourceURL())) {
        EditorPane epanes[] = WindowView.getOpenWindowOwners(EditorPane.class);
        for(EditorPane epane : epanes)
            if(SnapUtils.equals(url, epane.getSourceURL()))
                return epane;
    }
    
    // Load document
    ViewArchiver archiver = new ViewArchiver(); ViewArchiver.setUseRealClass(false);
    ParentView view = null; try { view = archiver.getParentView(aSource); }
    
    // If there was an XML parse error loading aSource, show error dialog
    catch(Exception e) {
        e.printStackTrace();
        String msg = StringUtils.wrap("Error reading file:\n" + e.getMessage(), 40);
        runLater(() -> {
            DialogBox dbox = new DialogBox("Error Reading File"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI()); });
    }
    ViewArchiver.setUseRealClass(true);
    
    // If no document, just return null
    if(view==null) return null;

    // If old version, warn user that saving document will make it unreadable by RM7
    /*if(shape instanceof RMDocument && ((RMDocument)shape).getVersion()<7.0) {
        String msg = "This document has been upgraded from an older version.\n" +
            "If saved, it will not open in earlier versions.";
        DialogBox dbox = new DialogBox("Warning: Document Upgrade"); dbox.setWarningMessage(msg);
        dbox.showMessageDialog(getUI());
    }*/
    
    // Set document
    getViewer().setContent(view);
    getViewer()._url = url;
    
    // If source is string, add to recent files menu
    //if(url!=null) RecentFilesPanel.addRecentFile(url.getString());
    
    // Return the editor
    return this;
}

/**
 * Saves the current editor document, running the save panel.
 */
public void saveAs()
{
    // Make sure editor isn't previewing
    //setEditing(true);
    
    // Get extensions - if there is an existing extension, make sure it's first in the exts array
    String exts[] = getFileExtensions();
    if(getSourceURL()!=null && FilePathUtils.getExtension(getSourceURL().getPath())!=null) {
        List ex = new ArrayList(Arrays.asList(exts));
        ex.add(0, "." + FilePathUtils.getExtension(getSourceURL().getPath()));
        exts = (String[])ex.toArray(new String[ex.size()]);
    }
    
    // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
    FileChooser fc = getEnv().getFileChooser(); fc.setDesc(getFileDescription()); fc.setExts(exts);
    String path = fc.showSavePanel(getEditor()); if(path==null) return;
    //getViewerShape().setSourceURL(WebURL.getURL(path));
    save();
}

/**
 * Saves the current editor document, running the save panel if needed.
 */
public void save()
{
    // If can't save to current source, do SaveAs instead
    if(getSourceURL()==null) { saveAs(); return; }
    
    // Make sure editor isn't previewing
    //setEditing(true);
    
    // Do actual save - if exception, print stack trace and set error string
    try { saveImpl(); }
    catch(Throwable e) {
        e.printStackTrace();
        String msg = "The file " + getSourceURL().getPath() + " could not be saved (" + e + ").";
        DialogBox dbox = new DialogBox("Error on Save"); dbox.setErrorMessage(msg);
        dbox.showMessageDialog(getUI());
        return;
    }
    
    // Add URL.String to RecentFilesMenu, clear undoer and reset UI
    //if(getSourceURL()!=null) RecentFilesPanel.addRecentFile(getSourceURL().getString());
    getEditor().getUndoer().reset();
    resetLater();
}

/**
 * The real save method.
 */
protected void saveImpl() throws Exception
{
    WebURL url = getSourceURL();
    WebFile file = url.getFile();
    if(file==null) file = url.createFile(false);
    XMLElement xml = getEditor().getContentXML();
    byte bytes[] = xml.getBytes();
    file.setBytes(bytes);
    file.save();
}

/**
 * Reloads the current editor document from the last saved version.
 */
public void revert()
{
    // Get filename (just return if null)
    WebURL surl = getSourceURL(); if(surl==null) return;

    // Run option panel for revert confirmation (just return if denied)
    String msg = "Revert to saved version of " + surl.getPathName() + "?";
    DialogBox dbox = new DialogBox("Revert to Saved"); dbox.setQuestionMessage(msg);
    if(!dbox.showConfirmDialog(getUI())) return;
        
    // Re-open filename
    getSourceURL().getFile().reload();
    open(getSourceURL());
}

/**
 * Closes this editor pane
 */
public boolean close()
{
    // Make sure editor isn't previewing
    //setEditing(true);
    
    // If unsaved changes, run panel to request save
    if(getEditor().undoerHasUndos()) {
        String filename = getSourceURL()==null? "untitled document" : getSourceURL().getPathName();
        DialogBox dbox = new DialogBox("Unsaved Changes");
        dbox.setWarningMessage("Save changes to " + filename + "?"); dbox.setOptions("Save", "Don't Save", "Cancel");
        switch(dbox.showOptionDialog(getUI(), "Save")) {
            case 0: save();
            case 1: break;
            default: return false;
        }
    }
    
    // Deactive current tool, so it doesn't reference this editor
    getEditor().getCurrentTool().deactivateTool();
    
    // Close window, called EditorClosed and return true to indicate we closed the window
    getWindow().hide();
    editorClosed();
    return true;
}

/**
 * Called when editor is closed.
 */
protected void editorClosed()
{
    // If another open editor is available focus on it, otherwise run WelcomePanel
    EditorPane epane = WindowView.getOpenWindowOwner(EditorPane.class);
    if(epane!=null)
        epane.getEditor().requestFocus();
    else WelcomePanel.getShared().showPanel();
}

/**
 * Catch Editor DeepChanges to register resetUI.
 */
public void deepChange(PropChangeListener aShape, PropChange anEvent)  { resetLater(); }

/**
 * Called when the app is about to exit to gracefully handle any open documents.
 */
public void quit()  { App.quitApp(); }

/**
 * Returns a popup menu for the editor.
 */
public void runPopupMenu(ViewEvent anEvent)
{
    // Get selected shape (just return if page is selected)
    Menu pmenu = new Menu();
    View shape = getEditor().getSelectedOrSuperSelectedShape();
    //if(shape instanceof RMPage) return;

    // If RMTextShape, get copy of Format menu
    /*if(shape instanceof RMTextShape) { RMTextShape text = (RMTextShape)shape;

        // Get editor pane format menu and add menu items to popup
        Menu formatMenu = getMenuBar().getView("FormatMenu", Menu.class);
        Menu formatMenuCopy = (Menu)formatMenu.clone();
        for(MenuItem m : formatMenuCopy.getItems()) pmenu.addItem(m);

        // If structured tablerow, add AddColumnMenuItem and SplitColumnMenuItem
        if(text.isStructured()) { MenuItem mi;
            mi = new MenuItem(); mi.setText("Add Column"); mi.setName("AddColumnMenuItem"); pmenu.addItem(mi);
            mi = new MenuItem(); mi.setText("Split Column"); mi.setName("SplitColumnMenuItem"); pmenu.addItem(mi);
        }
    }*/
    
    // Get copy of shapes menu and add menu items to popup
    Menu shapesMenu = getMenuBar().getView("ShapesMenu", Menu.class);
    Menu shapesMenuCopy = (Menu)shapesMenu.clone();
    for(MenuItem m : shapesMenuCopy.getItems()) pmenu.addItem(m);
    
    // Initialize popup menu items to send Events to menu bar
    pmenu.setOwner(getMenuBar());
    pmenu.getPopup().show(getEditor(), anEvent.getX(), anEvent.getY());
    anEvent.consume();
}

/**
 * A resolver for Views.
 */
public class ViewTreeResolver extends TreeResolver <View> {
    
    /** Returns the parent of given item. */
    public View getParent(View anItem)  { return anItem!=getEditor().getContent()? anItem.getParent() : null; }

    /** Whether given object is a parent (has children). */
    public boolean isParent(View anItem)
    {
        if(!(anItem instanceof ParentView)) return false;
        if(anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner) return false;
        if(anItem instanceof ComboBox || anItem instanceof ListView) return false;
        return ((ParentView)anItem).getChildCount()>0;
    }

    /** Returns the children. */
    public View[] getChildren(View aParent)
    {
        ParentView par = (ParentView)aParent;
        if(par instanceof ScrollView) { ScrollView sp = (ScrollView)par;
            return sp.getContent()!=null? new View[] { sp.getContent() } : new View[0]; }
        return par.getChildren();
    }

    /** Returns the text to be used for given item. */
    public String getText(View anItem)
    {
        String str = anItem.getClass().getSimpleName();
        String name = anItem.getName(); if(name!=null) str += " - " + name;
        String text = anItem.getText(); if(text!=null) str += " \"" + text + "\" ";
        return str;
    }

    /** Return the image to be used for given item. */
    public View getGraphic(View anItem)  { return null; }
}

/**
 * A class for any editor pane support panes.
 */
public static class SupportPane extends ViewOwner {
    
    // The editor pane
    EditorPane         _editorPane;
    
    /** Creates a new SupportPane with given editor pane. */
    public SupportPane(EditorPane anEP)  { _editorPane = anEP; }
    
    /** Returns the EditorPane. */
    public EditorPane getEditorPane()  { return _editorPane; }
    
    /** Sets the EditorPane. */
    public void setEditorPane(EditorPane anEP)  { _editorPane = anEP; }
    
    /** Returns the editor. */
    public Editor getEditor()  { return _editorPane.getEditor(); }
}

}