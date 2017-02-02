package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.ColorButton;

/**
 * Tool bar for EditorPane.
 */
public class EditorPaneToolBar extends ViewOwner {

    // The editor pane that this tool bar works for
    EditorPane      _editorPane;
    
    // The font face combobox
    ComboBox          _fontComboBox;
    
    // The font size combobox
    ComboBox          _fontSizeComboBox;
    
    // The toolbar tools
    ViewTool            _toolBarTools[];

/**
 * Creates a new editor pane tool bar.
 */
public EditorPaneToolBar(EditorPane anEditorPane)
{
    _editorPane = anEditorPane;
    _toolBarTools = createToolBarTools();
}

/**
 * Returns the editor pane.
 */
public EditorPane getEditorPane()  { return _editorPane; }

/**
 * Returns the editor pane editor.
 */
public Editor getEditor()  { return getEditorPane().getEditor(); }

/**
 * Updates the UI panel controls.
 */
protected void resetUI()
{
    // Get the editor
    Editor editor = getEditor();
    
    // Update UndoButton, RedoButton
    Undoer undoer = editor.getUndoer();
    setViewDisabled("UndoButton", undoer==null || undoer.getUndoSetLast()==null);
    setViewEnabled("RedoButton", undoer==null || undoer.getRedoSetLast()==null);
    
    // Reset PreviewEditButton state if out of sync
    //if(getViewBoolValue("PreviewEditButton")==getEditorPane().isEditing())
    //    setViewValue("PreviewEditButton", !getEditorPane().isEditing());

    // Get selected tool button name and button - if found and not selected, select it
    String toolButtonName = editor.getCurrentTool().getClass().getSimpleName() + "Button";
    ToggleButton toolButton = getView(toolButtonName, ToggleButton.class);
    if(toolButton!=null && !toolButton.isSelected())
        toolButton.setSelected(true);
}

/**
 * Responds to UI panel control changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get the editor
    Editor editor = getEditor();
    
    // Handle File NewButton, OpenButton, SaveButton, PreviewPDFButton, PreviewHTMLButton, PrintButton
    if(anEvent.equals("NewButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("OpenButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("SaveButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PreviewPDFButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PreviewHTMLButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PrintButton")) _editorPane.respondUI(anEvent);
        
    // Handle Edit CutButton, CopyButton, PasteButton, DeleteButton
    if(anEvent.equals("CutButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("CopyButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("PasteButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("DeleteButton")) editor.delete();
        
    // Handle Edit UndoButton, RedoButton
    if(anEvent.equals("UndoButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("RedoButton")) _editorPane.respondUI(anEvent);
    
    // Handle FillColorButton, StrokeColorButton, TextColorButton
    if(anEvent.equals("FillColorButton"))
        EditorShapes.setColor(editor, anEvent.getView(ColorButton.class).getColor());
    if(anEvent.equals("StrokeColorButton"))
        EditorShapes.setStrokeColor(editor, anEvent.getView(ColorButton.class).getColor());
    if(anEvent.equals("TextColorButton"))
        EditorShapes.setTextColor(editor, anEvent.getView(ColorButton.class).getColor());
    
    // Handle FontFaceComboBox
    if(anEvent.equals("FontFaceComboBox")) {
        String familyName = anEvent.getText();
        String fontNames[] = Font.getFontNames(familyName); if(fontNames==null || fontNames.length==0) return;
        String fontName = fontNames[0];
        Font font = Font.get(fontName, 12);
        EditorShapes.setFontFamily(editor, font);
        editor.requestFocus();
    }
    
    // Handle FontSizeComboBox
    if(anEvent.equals("FontSizeComboBox")) {
        EditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
        editor.requestFocus();
    }
    
    // Handle FontSizeUpButton, FontSizeDownButton
    if(anEvent.equals("FontSizeUpButton")) { Font font = EditorShapes.getFont(editor);
        EditorShapes.setFontSize(editor, font.getSize()<16? 1 : 2, true); }
    if(anEvent.equals("FontSizeDownButton")) { Font font = EditorShapes.getFont(editor);
        EditorShapes.setFontSize(editor, font.getSize()<16? -1 : -2, true); }
    
    // Handle Format BoldButton, ItalicButton, UnderlineButton
    if(anEvent.equals("BoldButton")) EditorShapes.setFontBold(editor, anEvent.getBoolValue());
    if(anEvent.equals("ItalicButton")) EditorShapes.setFontItalic(editor, anEvent.getBoolValue());
    if(anEvent.equals("UnderlineButton")) EditorShapes.setUnderlined(editor);
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
    if(anEvent.equals("AlignLeftButton")) EditorShapes.setAlignmentX(editor, HPos.LEFT);
    if(anEvent.equals("AlignCenterButton")) EditorShapes.setAlignmentX(editor, HPos.CENTER);
    if(anEvent.equals("AlignRightButton")) EditorShapes.setAlignmentX(editor, HPos.RIGHT);
    if(anEvent.equals("AlignFullButton")) _editorPane.respondUI(anEvent);
    
    // Handle PreviewXMLMenuItem
    if(anEvent.equals("PreviewXMLMenuItem"))
        EditorPaneUtils.previewXML(getEditorPane());

    // Handle ToolButton(s)
    if(anEvent.getName().endsWith("ToolButton")) {
        for(ViewTool tool : _toolBarTools)
            if(anEvent.getName().startsWith(tool.getClass().getSimpleName())) {
                getEditor().setCurrentTool(tool); break; }
    }
}

/**
 * Creates the list of tool instances for tool bar.
 */
protected ViewTool[] createToolBarTools()
{
    List <ViewTool> tools = new ArrayList();
    Editor editor = getEditor();
    tools.add(editor.getSelectTool());
    tools.add(editor.getTool(LineView.class));
    tools.add(editor.getTool(RectView.class));
    tools.add(editor.getTool(ArcView.class));
    tools.add(editor.getTool(TextView.class));
    //tools.add(editor.getTool(RMPolygonShape.class));
    //tools.add(new RMPolygonShapeTool.PencilTool());
    return tools.toArray(new ViewTool[0]);
}

}