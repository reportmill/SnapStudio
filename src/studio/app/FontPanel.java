package studio.app;
import snap.gfx.Font;
import snap.util.StringUtils;
import snap.view.*;

/**
 * This class is a Font panel for selecting fonts. It lets the user easily select a font family,
 * font size and font style (bold, italic, underline, outline). It also has a convenient slider for interactively
 * changing the size and a text field for specifically setting a size. In addition, there is a pick list that
 * shows all the individual fonts available for a given family.
 */
public class FontPanel extends EditorPane.SupportPane {
    
/**
 * Creates a new FontPanel.
 */
public FontPanel(EditorPane anEP)  { super(anEP); }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get/configure FamilyList
    ListView <String> familyList = getView("FamilyList", ListView.class);
    familyList.setItems(Font.getFamilyNames());
    
    // Get/configure FamilyComboBox
    ComboBox familyComboBox = getView("FamilyComboBox", ComboBox.class);
    familyComboBox.setListView(familyList);
    
    // Configure SizesList
    setViewItems("SizesList", new Object[] { 6,8,9,10,11,12,14,16,18,22,24,36,48,64,72,96,128,144 });
    
    // Configure SizeText
    TextField sizeText = getView("SizeText", TextField.class);
    sizeText.addPropChangeListener(pce -> { if(sizeText.isFocused()) sizeText.selectAll(); }, View.Focused_Prop);
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
    setViewValue("SizesList", (int)size);
    setViewValue("SizeText", "" + StringUtils.toString(size) + " pt"); //setNodeValue("SizeThumb", size);
    setViewValue("BoldButton", font.isBold());
    setViewDisabled("BoldButton", font.getBold()==null);
    setViewValue("ItalicButton", font.isItalic());
    setViewDisabled("ItalicButton", font.getItalic()==null);
    setViewValue("UnderlineButton", EditorShapes.isUnderlined(editor));
    setViewValue("OutlineButton", EditorShapes.getTextBorder(editor)!=null);
    
    // Get font names in currently selected font's family
    String familyNames[] = Font.getFontNames(font.getFamily());
    
    // Reset FontNameComboBox Items, SelectedItem and Enabled
    setViewItems("FontNameComboBox", familyNames);
    String fn = font.getFontFile().getNativeName(); setViewSelectedItem("FontNameComboBox", fn);
    setViewDisabled("FontNameComboBox", familyNames.length<=1);
}

/**
 * Respond to UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get current editor
    Editor editor = getEditor();
    
    // Handle FontSizeUpButton, FontSizeDownButton
    if(anEvent.equals("FontSizeUpButton")) { Font font = EditorShapes.getFont(editor);
        EditorShapes.setFontSize(editor, font.getSize()<16? 1 : 2, true); }
    if(anEvent.equals("FontSizeDownButton")) { Font font = EditorShapes.getFont(editor);
        EditorShapes.setFontSize(editor, font.getSize()<16? -1 : -2, true); }
    
    // Handle BoldButton, ItalicButton, UnderlineButton, OutlineButton
    if(anEvent.equals("BoldButton"))
        EditorShapes.setFontBold(editor, anEvent.getBoolValue());
    if(anEvent.equals("ItalicButton"))
        EditorShapes.setFontItalic(editor, anEvent.getBoolValue());
    if(anEvent.equals("UnderlineButton"))
        EditorShapes.setUnderlined(editor);
    if(anEvent.equals("OutlineButton"))
        EditorShapes.setTextBorder(editor);
    
    // Handle SizesList
    if(anEvent.equals("SizesList") && anEvent.getValue()!=null)
        EditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);
    
    // Handle SizeText
    if(anEvent.equals("SizeText"))
        EditorShapes.setFontSize(editor, anEvent.getFloatValue(), false);

    // Handle FamilyList, FamilyComboBox
    if(anEvent.equals("FamilyList") || (anEvent.equals("FamilyComboBox") && anEvent.isActionEvent())) {
        String familyName = getViewStringValue("FamilyList");
        String fontNames[] = Font.getFontNames(familyName); if(fontNames.length==0) return;
        String fontName = fontNames[0];
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