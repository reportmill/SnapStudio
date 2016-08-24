package studio.app;
import snap.gfx.Font;
import snap.view.*;

/**
 * This class is a Swing/Ribs Font panel for selecting fonts. It lets the user easily select a font family,
 * font size and font style (bold, italic, underline, outline). It also has a convenient slider for interactively
 * changing the size and a text field for specifically setting a size. In addition, there is a pick list that
 * shows all the individual fonts available for a given family.
 */
public class FontPanel extends ViewOwner {
    
    // The EditorPane
    EditorPane    _editorPane;

/**
 * Creates a new FontPanel for EditorPane.
 */
public FontPanel(EditorPane anEP)  { _editorPane = anEP; }

/**
 * Returns the Editor.
 */
public Editor getEditor()  { return _editorPane.getEditor(); }

/**
 * Returns the EditorPane.
 */
public EditorPane getEditorPane()  { return _editorPane; }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    setViewItems("FamilyList", Font.getFamilyNames());
    setViewItems("SizesList", new Object[] { 6,8,9,10,11,12,14,16,18,22,24,36,48,64,72,96,128,144 }); 
}

/**
 * Reset UI from the current selection.
 */
public void resetUI()
{
    // Get current font
    Editor editor = getEditor();
    Font font = EditorShapes.getFont(editor);
    
    // Get family name and size
    String familyName = font.getFamily();
    double size = font.getSize();
    
    // Reset FamilyList, SizesList, SizeText, SizeThumb, and Bold, Italic, Underline and Outline buttons
    setViewValue("FamilyList", familyName);
    setViewValue("FamilyText", familyName);
    setViewValue("SizesList", (int)size);
    setViewValue("SizeText", "" + size + " pt"); //setNodeValue("SizeThumb", size);
    setViewValue("BoldButton", font.isBold());
    setViewEnabled("BoldButton", font.getBold()!=null);
    setViewValue("ItalicButton", font.isItalic());
    setViewEnabled("ItalicButton", font.getItalic()!=null);
    setViewValue("UnderlineButton", EditorShapes.isUnderlined(editor));
    setViewValue("OutlineButton", EditorShapes.getTextBorder(editor)!=null);
    
    // Get font names in currently selected font's family
    String familyNames[] = Font.getFontNames(font.getFamily());
    
    // Reset FontNameComboBox Items, SelectedItem and Enabled
    setViewItems("FontNameComboBox", familyNames);
    String fn = font.getFontFile().getNativeName(); setViewSelectedItem("FontNameComboBox", fn);
    setViewEnabled("FontNameComboBox", familyNames.length>1);
}

/**
 * Respond to UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get current editor
    Editor editor = getEditor();
    
    // Handle FontSizeUpButton
    if(anEvent.equals("FontSizeUpButton")) { Font font = EditorShapes.getFont(editor);
        EditorShapes.setFontSize(editor, font.getSize()<16? 1 : 2, true); }
    
    // Handle FontSizeDownButton
    if(anEvent.equals("FontSizeDownButton")) { Font font = EditorShapes.getFont(editor);
        EditorShapes.setFontSize(editor, font.getSize()<16? -1 : -2, true); }
    
    // Handle BoldButton
    if(anEvent.equals("BoldButton"))
        EditorShapes.setFontBold(editor, anEvent.getBoolValue());
    
    // Handle ItalicButton
    if(anEvent.equals("ItalicButton"))
        EditorShapes.setFontItalic(editor, anEvent.getBoolValue());
    
    // Handle UnderlineButton
    if(anEvent.equals("UnderlineButton"))
        EditorShapes.setUnderlined(editor);
    
    // Handle OutlineButton
    if(anEvent.equals("OutlineButton"))
        EditorShapes.setTextBorder(editor);
    
    // Handle SizeThumbwheel
    //if(anEvent.equals("SizeThumb")) EditorShapes.setFontSize(editor, anEvent.getIntValue(), false);
    
    // Handle SizesList
    if(anEvent.equals("SizesList") && anEvent.getValue()!=null)
        EditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
    
    // Handle SizeText
    if(anEvent.equals("SizeText")) {
        EditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
        editor.requestFocus();
    }

    // Handle FamilyList
    if(anEvent.equals("FamilyList")) {
        String familyName = getViewStringValue("FamilyList");
        String fontName = Font.getFontNames(familyName)[0];
        Font font = new Font(fontName, 12);
        EditorShapes.setFontFamily(editor, font);
    }
    
    // Handle FontNameComboBox
    if(anEvent.equals("FontNameComboBox")) {
        Font font = new Font(anEvent.getStringValue(), 12);
        EditorShapes.setFontName(editor, font);
    }
}
    
/** Returns the name for the inspector window. */
public String getWindowTitle()  { return "Font Panel"; }

}