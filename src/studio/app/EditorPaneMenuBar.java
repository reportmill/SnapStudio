package studio.app;
import snap.util.*;
import snap.view.*;

/**
 * Menu bar for Editor pane.
 */
public class EditorPaneMenuBar extends EditorPane.SupportPane {

/**
 * Creates a new editor pane menu bar.
 */
public EditorPaneMenuBar(EditorPane anEP)  { super(anEP); }

/**
 * Override to return node as MenuBar.
 */
public MenuBar getUI()  { return (MenuBar)super.getUI(); }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Configure CheckSpellingAsYouTypeMenuItem and HyphenateTextMenuItem
    //setViewValue("CheckSpellingAsYouTypeMenuItem", RMTextEditor.isSpellChecking);
    //setViewValue("HyphenateTextMenuItem", RMTextEditor.isHyphenating());
}

/**
 * Updates the editor's UI.
 */
protected void resetUI()
{
    // Get the editor undoer
    Undoer undoer = getEditor().getUndoer();

    // Update UndoMenuItem
    String uTitle = undoer==null || undoer.getUndoSetLast()==null? "Undo" : undoer.getUndoSetLast().getFullUndoTitle();
    setViewValue("UndoMenuItem", uTitle);
    setViewDisabled("UndoMenuItem", undoer==null || undoer.getUndoSetLast()==null);

    // Update RedoMenuItem
    String rTitle = undoer==null || undoer.getRedoSetLast()==null? "Redo" : undoer.getRedoSetLast().getFullRedoTitle();
    setViewValue("RedoMenuItem", rTitle);
    setViewDisabled("RedoMenuItem", undoer==null || undoer.getRedoSetLast()==null);
    
    // Update ShowRulersMenuItem
    setViewValue("ShowRulersMenuItem", getEditorPane().getShowRulers());
}

/**
 * Handles changes to the editor's UI controls.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get editor pane
    EditorPane epane = getEditorPane();
    Editor editor = getEditor();
    
    // Handle NewMenuItem, NewButton: Get new editor pane and make visible
    if(anEvent.equals("NewMenuItem") || anEvent.equals("NewButton")) {
        EditorPane editorPane = ClassUtils.newInstance(epane).newDocument();
        editorPane.setWindowVisible(true);
    }
    
    // Handle OpenMenuItem, OpenButton: Get new editor pane from open panel and make visible (if created)
    if(anEvent.equals("OpenMenuItem") || anEvent.equals("OpenButton")) {
        EditorPane editorPane = ClassUtils.newInstance(epane).open(getUI());
        if(editorPane!=null)
            editorPane.setWindowVisible(true);
    }
    
    // Handle OpenRecentMenuItem
    //if(anEvent.equals("OpenRecentMenuItem")) new RecentFilesPanel().showPanel();

    // Handle CloseMenuItem
    if(anEvent.equals("CloseMenuItem")) epane.close();
    
    // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, SaveAsPDFMenuItem, RevertMenuItem
    if(anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton")) epane.save();
    if(anEvent.equals("SaveAsMenuItem")) epane.saveAs();
    if(anEvent.equals("SaveAsPDFMenuItem")) EditorUtils.saveAsPDF(epane);
    if(anEvent.equals("RevertMenuItem")) epane.revert();
    
    // Handle PrintMenuItem, QuitMenuItem
    //if(anEvent.equals("PrintMenuItem") || anEvent.equals("PrintButton")) editor.print(null, !anEvent.isAltDown());
    if(anEvent.equals("QuitMenuItem")) epane.quit();
        
    // Handle File -> Preview Reports menu items
    if(anEvent.equals("PreviewPDFMenuItem") || anEvent.equals("PreviewPDFButton")) EditorUtils.previewPDF(epane);
    if(anEvent.equals("PreviewHTMLMenuItem") || anEvent.equals("PreviewHTMLButton"))
        EditorUtils.previewHTML(epane);
    if(anEvent.equals("PreviewCSVMenuItem")) EditorUtils.previewCSV(epane);
    if(anEvent.equals("PreviewExcelMenuItem")) EditorUtils.previewXLS(epane);
    if(anEvent.equals("PreviewRTFMenuItem")) EditorUtils.previewRTF(epane);
    if(anEvent.equals("PreviewJPEGMenuItem")) EditorUtils.previewJPG(epane);
    if(anEvent.equals("PreviewPNGMenuItem")) EditorUtils.previewPNG(epane);
        
    // Handle Edit menu items
    if(anEvent.equals("UndoMenuItem") || anEvent.equals("UndoButton")) editor.undo();
    if(anEvent.equals("RedoMenuItem") || anEvent.equals("RedoButton")) editor.redo();
    if(anEvent.equals("CutMenuItem") || anEvent.equals("CutButton")) editor.cut();
    if(anEvent.equals("CopyMenuItem") || anEvent.equals("CopyButton")) editor.copy();
    if(anEvent.equals("PasteMenuItem") || anEvent.equals("PasteButton")) editor.paste();
    if(anEvent.equals("SelectAllMenuItem")) editor.selectAll();
    //if(anEvent.equals("CheckSpellingMenuItem")) SpellCheckPanel.getShared().show(editor);
    
    // Edit -> CheckSpellingAsYouTypeMenuItem
    /*if(anEvent.equals("CheckSpellingAsYouTypeMenuItem")) {
        RMTextEditor.isSpellChecking = anEvent.getBooleanValue();
        PrefsUtils.prefsPut("SpellChecking", RMTextEditor.isSpellChecking? Boolean.TRUE : Boolean.FALSE);
        editor.repaint();
    }*/
    
    // Edit -> HyphenateTextMenuItem
    /*if(anEvent.equals("HyphenateTextMenuItem")) {
        RMTextEditor.setHyphenating(anEvent.getBooleanValue()); editor.repaint(); }*/
        
    // Handle Format menu items (use name because anObj may come from popup menu)
    if(anEvent.equals("FontPanelMenuItem")) epane.getAttributesPanel().setVisibleName(AttributesPanel.FONT);
    if(anEvent.equals("BoldMenuItem") || anEvent.equals("BoldButton"))
        EditorUtils.setFontBold(editor, !EditorUtils.getFont(editor).isBold());
    if(anEvent.equals("ItalicMenuItem") || anEvent.equals("ItalicButton"))
        EditorUtils.setFontItalic(editor, !EditorUtils.getFont(editor).isItalic());
    if(anEvent.equals("UnderlineMenuItem") || anEvent.equals("UnderlineButton"))
        EditorUtils.setUnderlined(editor);
    if(anEvent.equals("OutlineMenuItem")) EditorUtils.setTextBorder(editor);
    //if(anEvent.equals("AlignLeftMenuItem") || anEvent.equals("AlignLeftButton"))
    //    EditorUtils.setAlignmentX(editor, RMTypes.AlignX.Left);
    //if(anEvent.equals("AlignCenterMenuItem") || anEvent.equals("AlignCenterButton"))
    //    EditorUtils.setAlignmentX(editor, RMTypes.AlignX.Center);
    //if(anEvent.equals("AlignRightMenuItem") || anEvent.equals("AlignRightButton"))
    //    EditorUtils.setAlignmentX(editor, RMTypes.AlignX.Right);
    //if(anEvent.equals("AlignFullMenuItem") || anEvent.equals("AlignFullButton"))
    //    EditorUtils.setAlignmentX(editor, RMTypes.AlignX.Full);
    if(anEvent.equals("SuperscriptMenuItem")) EditorUtils.setSuperscript(editor);
    if(anEvent.equals("SubscriptMenuItem")) EditorUtils.setSubscript(editor);
        
    // Handle Pages menu items
    //if(anEvent.equals("AddPageMenuItem")) editor.addPage();
    //if(anEvent.equals("AddPagePreviousMenuItem")) editor.addPagePrevious();
    //if(anEvent.equals("RemovePageMenuItem")) editor.removePage();
    if(anEvent.equals("ZoomInMenuItem")) editor.setZoomFactor(editor.getZoomFactor() + .1f);
    if(anEvent.equals("ZoomOutMenuItem")) editor.setZoomFactor(editor.getZoomFactor() - .1f);
    if(anEvent.equals("Zoom100MenuItem")) editor.setZoomFactor(1);
    if(anEvent.equals("Zoom200MenuItem")) editor.setZoomFactor(2);
    if(anEvent.equals("ZoomToggleLastMenuItem")) editor.zoomToggleLast();
    if(anEvent.equals("ZoomToMenuItem")) epane.runZoomPanel();
        
    // Handle Shapes menu items (use name because anObj may come from popup menu)
    String name = anEvent.getName();
    if(name.equals("GroupMenuItem")) EditorUtils.groupViews(editor, null, null);
    if(name.equals("UngroupMenuItem")) EditorUtils.ungroupViews(editor);
    if(name.equals("BringToFrontMenuItem")) EditorUtils.bringToFront(editor);
    if(name.equals("SendToBackMenuItem")) EditorUtils.sendToBack(editor);
    if(name.equals("MakeRowTopMenuItem")) EditorUtils.makeRowTop(editor);
    if(name.equals("MakeRowCenterMenuItem")) EditorUtils.makeRowCenter(editor);
    if(name.equals("MakeRowBottomMenuItem")) EditorUtils.makeRowBottom(editor);
    if(name.equals("MakeColumnLeftMenuItem")) EditorUtils.makeColumnLeft(editor);
    if(name.equals("MakeColumnCenterMenuItem")) EditorUtils.makeColumnCenter(editor);
    if(name.equals("MakeColumnRightMenuItem")) EditorUtils.makeColumnRight(editor);
    if(name.equals("MakeSameSizeMenuItem")) EditorUtils.makeSameSize(editor);
    if(name.equals("MakeSameWidthMenuItem")) EditorUtils.makeSameWidth(editor);
    if(name.equals("MakeSameHeightMenuItem")) EditorUtils.makeSameHeight(editor);
    if(name.equals("SizeToFitMenuItem")) EditorUtils.setSizeToFit(editor);
    if(name.equals("EquallySpaceRowMenuItem")) EditorUtils.equallySpaceRow(editor);
    if(name.equals("EquallySpaceColumnMenuItem")) EditorUtils.equallySpaceColumn(editor);
    if(name.equals("CombinePathsMenuItem")) EditorUtils.combinePaths(editor);
    if(name.equals("SubtractPathsMenuItem")) EditorUtils.subtractPaths(editor);
    if(name.equals("ConvertToImageMenuItem")) EditorUtils.convertToImage(editor);
    
    // Handle Tools menu items
    if(anEvent.equals("InspectorMenuItem")) epane.getInspectorPanel().setVisible(-1);
    if(anEvent.equals("ColorPanelMenuItem")) epane.getAttributesPanel().setVisibleName(AttributesPanel.COLOR);
    if(anEvent.equals("FormatPanelMenuItem")) epane.getAttributesPanel().setVisibleName(AttributesPanel.FORMAT);
    if(anEvent.equals("KeysPanelMenuItem")) epane.getAttributesPanel().setVisibleName(AttributesPanel.KEYS);
    
    // Handle ShowRulersMenuItem, FeedbackMenuItem, PrefsMenuItem
    if(anEvent.equals("ShowRulersMenuItem")) epane.setShowRulers(!epane.getShowRulers());
    //if(anEvent.equals("FeedbackMenuItem")) new FeedbackPanel().showPanel(epane.getUI());
    //if(anEvent.equals("PrefsMenuItem")) new PreferencesPanel().showPanel(epane.getUI());
    
    // Handle SupportPageMenuItem, TutorialMenuItem, BasicAPIMenuItem, TablesMenuItem
    if(anEvent.equals("SupportPageMenuItem")) URLUtils.openURL("http://reportmill.com/support");
    if(anEvent.equals("TutorialMenuItem")) URLUtils.openURL("http://reportmill.com/support/tutorial.pdf");
    if(anEvent.equals("BasicAPIMenuItem")) URLUtils.openURL("http://reportmill.com/support/BasicApi.pdf");
    if(anEvent.equals("TablesMenuItem")) URLUtils.openURL("http://reportmill.com/support/tables.pdf");
}

}