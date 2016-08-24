package studio.app;
import java.util.*;
import snap.util.*;
import snap.view.*;

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
 * Initializes UI panel.
 */
protected void initUI()
{
    // Modify items in Row1
    HBox hbox = getUI(HBox.class); 
    for(View child : hbox.getChildren()) child.setPrefSize(child.getSize());
        
    // Modify items in Row2, Row3
    //hbox = getView("Row2", HBox.class); for(Node child : hbox.getChildren()) child.setPrefSize(child.getSize());
    //hbox = getView("Row3", HBox.class); for(Node child : hbox.getChildren()) child.setPrefSize(child.getSize());
        
    // Get FontFaceComboBox, FontFaceText and FontFaceComboBox
    //_fontComboBox = getView("FontFaceComboBox", ComboBox.class);
    //_fontComboBox.setCellConfigure(c -> configureFontComboBox((ListCell)c));
    //setNodeItems(_fontComboBox, RMFont.getFamilyNames());
    //TextField fontText = _fontComboBox.getTextField();
    //enableEvents(fontText, KeyPressed, KeyReleased);
    //fontText.setName("FontFaceComboBoxText");
    //fontText.addPropChangeListener(e -> selectAllOnFocus(fontText), Node.Focused_Prop);
    
    // Configure FontSizeComboBox
    //_fontSizeComboBox = getView("FontSizeComboBox", ComboBox.class);
    //Object sizes[] = { 6, 8, 9, 10, 11, 12, 14, 16, 18, 22, 24, 36, 48, 64, 72, 96, 128, 144 };
    //setNodeItems(_fontSizeComboBox, sizes);
    //TextField fontSizeText = _fontSizeComboBox.getTextField();
    //fontSizeText.addPropChangeListener(e -> selectAllOnFocus(fontSizeText), Node.Focused_Prop);
}

/**
 * Updates the UI panel controls.
 */
protected void resetUI()
{
    // Get the editor
    Editor editor = getEditor();
    
    // Update UndoButton, RedoButton
    Undoer undoer = editor.getUndoer();
    setViewEnabled("UndoButton", undoer!=null && undoer.getUndoSetLast()!=null);
    setViewEnabled("RedoButton", undoer!=null && undoer.getRedoSetLast()!=null);
    
    // Update BoldButton, PercentButton, CommaButton, DecimalAddButton, DecimalRemoveButton
    //RMFormat format = EditorShapes.getFormat(editor);
    //setNodeValue("MoneyButton", format instanceof RMNumberFormat && ((RMNumberFormat)format).isLocalCurrencySymbolUsed());
    //setNodeValue("PercentButton", format instanceof RMNumberFormat && ((RMNumberFormat)format).isPercentSymbolUsed());
    //setNodeValue("CommaButton", format instanceof RMNumberFormat && ((RMNumberFormat)format).isGroupingUsed());
    //setNodeEnabled("DecimalAddButton", format instanceof RMNumberFormat);
    //setNodeEnabled("DecimalRemoveButton", format instanceof RMNumberFormat);    

    // Update FontFaceComboBox, FontSizeComboBox, BoldButton, ItalicButton, UnderlineButton, FontSizeDownButton.Enabled
    //RMFont font = EditorShapes.getFont(editor); double size = font.getSize(); Number snum = size;
    //setNodeValue("FontFaceComboBox", font.getFamily()); if(size==(int)size) snum = snum.intValue();
    //setNodeValue("FontSizeComboBox", snum); // if is whole number, reset to integer
    //setNodeValue("BoldButton", font.isBold());
    //setNodeValue("ItalicButton", font.isItalic());
    //setNodeValue("UnderlineButton", EditorShapes.isUnderlined(editor));
    //setNodeEnabled("FontSizeDownButton", font.getSize()>6);

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
    
    // Handle FillColorButton, StrokeColorButton
    //if(anEvent.equals("FillColorButton"))
    //    EditorShapes.setColor(editor, anEvent.getView(ColorButton.class).getColor());
    //if(anEvent.equals("StrokeColorButton"))
    //    EditorShapes.setStrokeColor(editor, anEvent.getView(ColorButton.class).getColor());
    
    // Handle Format BoldButton, ItalicButton, UnderlineButton
    if(anEvent.equals("BoldButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("ItalicButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("UnderlineButton")) _editorPane.respondUI(anEvent);
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton
    if(anEvent.equals("AlignLeftButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("AlignCenterButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("AlignRightButton")) _editorPane.respondUI(anEvent);
    if(anEvent.equals("AlignFullButton")) _editorPane.respondUI(anEvent);
    
    // Handle FontSizeUpButton, FontSizeDownButton, TextColorButton
    //if(anEvent.equals("TextColorButton"))
    //    EditorShapes.setTextColor(editor, anEvent.getView(ColorButton.class).getColor());

    // Handle Preview/Edit button and PreviewMenuItem
    if(anEvent.equals("PreviewEditButton") || anEvent.equals("PreviewMenuItem")) {
        
    }
    
    // Handle PreviewXMLMenuItem
    if(anEvent.equals("PreviewXMLMenuItem"))
        EditorPaneUtils.previewXML(getEditorPane());

    // Handle ToolButton(s)
    if(anEvent.getName().endsWith("ToolButton")) {
        for(ViewTool tool : _toolBarTools)
            if(anEvent.getName().startsWith(tool.getClass().getSimpleName())) {
                getEditor().setCurrentTool(tool); break; }
    }
    
    // Handle AddTableButton, AddGraphButton, AddLabelsButton, AddCrossTabFrameButton
    //if(anEvent.equals("AddTableButton")) RMTableTool.addTable(getEditor(), null);
    //if(anEvent.equals("AddGraphButton")) RMGraphTool.addGraph(getEditor(), null);
    //if(anEvent.equals("AddLabelsButton")) RMLabelsTool.addLabels(getEditor(), null);
    //if(anEvent.equals("AddCrossTabFrameButton")) RMCrossTabTool.addCrossTab(getEditor(), null);
}

/**
 * Creates the list of tool instances for tool bar.
 */
protected ViewTool[] createToolBarTools()
{
    List <ViewTool> tools = new ArrayList();
    Editor editor = getEditor();
    tools.add(editor.getSelectTool());
    //tools.add(editor.getTool(RMLineShape.class));
    //tools.add(editor.getTool(RMRectShape.class));
    //tools.add(editor.getTool(RMOvalShape.class));
    //tools.add(editor.getTool(RMTextShape.class));
    //tools.add(editor.getTool(RMPolygonShape.class));
    //tools.add(new RMPolygonShapeTool.PencilTool());
    return tools.toArray(new ViewTool[0]);
}

}