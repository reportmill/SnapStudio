package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.ColorButton;

/**
 * Tool bar for EditorPane.
 */
public class EditorPaneToolBar extends EditorPane.SupportPane {

    // The toolbar tools
    ViewTool           _toolBarTools[];

/**
 * Creates a new EditorPaneToolBar.
 */
public EditorPaneToolBar(EditorPane anEP)
{
    super(anEP);
    _toolBarTools = createToolBarTools();
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    // Get/configure FontFaceComboBox
    ComboBox fontFaceComboBox = getView("FontFaceComboBox", ComboBox.class);
    fontFaceComboBox.setItems((Object[])Font.getFamilyNames());
    
    // Get/configure FontSizeComboBox
    ComboBox fontSizeComboBox = getView("FontSizeComboBox", ComboBox.class);
    Object sizes[] = { 6, 8, 9, 10, 11, 12, 14, 16, 18, 22, 24, 36, 48, 64, 72, 96, 128, 144 };
    fontSizeComboBox.setItems(sizes);
}

/**
 * Updates the UI panel controls.
 */
protected void resetUI()
{
    // Get the editor
    Editor editor = getEditor();
    Font font = EditorShapes.getFont(editor);
    
    // Update UndoButton, RedoButton
    Undoer undoer = editor.getUndoer();
    setViewDisabled("UndoButton", undoer==null || undoer.getUndoSetLast()==null);
    setViewEnabled("RedoButton", undoer==null || undoer.getRedoSetLast()==null);
    
    // Get selected tool button name and button - if found and not selected, select it
    String toolButtonName = editor.getCurrentTool().getClass().getSimpleName() + "Button";
    ToggleButton toolButton = getView(toolButtonName, ToggleButton.class);
    if(toolButton!=null && !toolButton.isSelected())
        toolButton.setSelected(true);
        
    // Reset FontFaceComboBox, FontSizeComboBox
    setViewSelectedItem("FontFaceComboBox", font.getFamily());
    setViewText("FontSizeComboBox", StringUtils.toString(font.getSize()) + " pt");
        
    // Reset BoldButton, ItalicButton, UnderlineButton
    setViewValue("BoldButton", font.isBold());
    setViewEnabled("BoldButton", font.getBold()!=null);
    setViewValue("ItalicButton", font.isItalic());
    setViewEnabled("ItalicButton", font.getItalic()!=null);
    setViewValue("UnderlineButton", EditorShapes.isUnderlined(editor));
    
    // Update AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    HPos lineAlign = EditorShapes.getAlignX(editor); boolean isJustify = EditorShapes.isJustify(editor);
    setViewValue("AlignLeftButton", lineAlign==HPos.LEFT && !isJustify);
    setViewValue("AlignCenterButton", lineAlign==HPos.CENTER && !isJustify);
    setViewValue("AlignRightButton", lineAlign==HPos.RIGHT && !isJustify);
    setViewValue("AlignFullButton", isJustify);
}

/**
 * Responds to UI panel control changes.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get the editor
    EditorPane epane = getEditorPane();
    Editor editor = getEditor();
    
    // Handle File NewButton, OpenButton, SaveButton, PreviewPDFButton, PreviewHTMLButton, PrintButton
    if(anEvent.equals("NewButton")) epane.respondUI(anEvent);
    if(anEvent.equals("OpenButton")) epane.respondUI(anEvent);
    if(anEvent.equals("SaveButton")) epane.respondUI(anEvent);
    if(anEvent.equals("PreviewPDFButton")) epane.respondUI(anEvent);
    if(anEvent.equals("PreviewHTMLButton")) epane.respondUI(anEvent);
    if(anEvent.equals("PrintButton")) epane.respondUI(anEvent);
        
    // Handle Edit CutButton, CopyButton, PasteButton, DeleteButton
    if(anEvent.equals("CutButton")) epane.respondUI(anEvent);
    if(anEvent.equals("CopyButton")) epane.respondUI(anEvent);
    if(anEvent.equals("PasteButton")) epane.respondUI(anEvent);
    if(anEvent.equals("DeleteButton")) editor.delete();
        
    // Handle Edit UndoButton, RedoButton
    if(anEvent.equals("UndoButton")) epane.respondUI(anEvent);
    if(anEvent.equals("RedoButton")) epane.respondUI(anEvent);
    
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
    if(anEvent.equals("AlignLeftButton")) EditorShapes.setAlignX(editor, HPos.LEFT);
    if(anEvent.equals("AlignCenterButton")) EditorShapes.setAlignX(editor, HPos.CENTER);
    if(anEvent.equals("AlignRightButton")) EditorShapes.setAlignX(editor, HPos.RIGHT);
    if(anEvent.equals("AlignFullButton")) EditorShapes.setJustify(editor, true);
    
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