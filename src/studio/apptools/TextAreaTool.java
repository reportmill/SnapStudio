/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.apptools;
import studio.app.*;
import java.util.List;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;

/**
 * This class provides UI editing for TextArea.
 */
public class TextAreaTool <T extends TextArea> extends ViewTool <T> {
    
    // The inspector TextView
    TextView            _textView;
    
    // The view hit by text tool on mouse down
    View                _downView;
    
    // Whether editor should resize TextArea whenever text changes
    boolean             _updatingSize = false;
    
    // The minimum height of the RMText when editor text editor is updating size
    double              _updatingMinHeight = 0;
    
    // A PropChange listener to listen to selected TextArea Selection changes
    PropChangeListener  _textAreaSelLsnr = pce -> textAreaSelChange(pce);

    // A PropChange listener to listen to selected TextArea.RichText property changes
    PropChangeListener  _richTextPropLsnr = pc -> richTextPropChange(pc);

/**
 * Returns whether a given view is super-selectable.
 */
public boolean isSuperSelectable(T aView)  { return true; }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get TextView (in inspector) and register to catch Selection changes
    _textView = getView("TextView", TextView.class);
    _textView.getTextArea().addPropChangeListener(pce -> textViewSelChange(pce), TextArea.Selection_Prop);
    
    // Get editor and register for focus changes
    getEditor().addPropChangeListener(pce -> editorFocusedChange(), View.Focused_Prop);
}

/**
 * Refreshes UI controls from currently selected text view.
 */
public void resetUI()
{
    // Get editor and currently selected text
    Editor editor = getEditor();
    TextArea text = getSelectedView(); if(text==null) return;
    
    // Get text style and line style
    TextStyle style = text.getRichText().getStyleAt(0);
    TextLineStyle lstyle = text.getRichText().getLineStyleAt(0);
    HPos lineAlign = lstyle.getAlign(); boolean isJustify = lstyle.isJustify();
    
    // Update AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    setViewValue("AlignLeftButton", lineAlign==HPos.LEFT && !isJustify);
    setViewValue("AlignCenterButton", lineAlign==HPos.CENTER && !isJustify);
    setViewValue("AlignRightButton", lineAlign==HPos.RIGHT && !isJustify);
    setViewValue("AlignFullButton", isJustify);
    setViewValue("AlignTopButton", text.getTextBox().getAlignY()==VPos.TOP);
    setViewValue("AlignMiddleButton", text.getTextBox().getAlignY()==VPos.CENTER);
    setViewValue("AlignBottomButton", text.getTextBox().getAlignY()==VPos.BOTTOM); // Update AlignBottomButton
    
    // Reset TextView from (potentially) updated TextArea
    _textView.getTextBox().setText(text.getRichText());
    _textView.setSel(text.getSelStart(),text.getSelEnd());
    
    // Reset PaddingText
    setViewValue("PaddingText", text.getPadding().getStringLong());
    
    // Update RoundingThumb, RoundingText
    setViewValue("RoundingThumb", 0);
    setViewValue("RoundingText", 0);
    
    // Get text's background color and set in TextArea if found
    //Color color = null; for(View view=text; color==null && view!=null;) {
    //    if(view.getFill()==null) view = view.getParent(); else color = view.getFill().getColor(); }
    //_textView.setBackground(color==null? Color.white : color);
    // Get xstring font size and scale up to 12pt if any string run is smaller
    //RMXString xstring = text.getXString(); double fsize = 12;
    //for(int i=0,iMax=xstring.getRunCount();i<iMax;i++) fsize = Math.min(fsize, xstring.getRun(i).getFont().getSize());
    //_textView.setFontScale(fsize<12? 12/fsize : 1);

    // Update CharSpacingThumb and CharSpacingText
    setViewValue("CharSpacingThumb", style.getCharSpacing());
    setViewValue("CharSpacingText", style.getCharSpacing());
    
    // Update LineSpacingThumb and LineSpacingText
    setViewValue("LineSpacingThumb", lstyle.getSpacing());
    setViewValue("LineSpacingText", lstyle.getSpacing());
    
    // Update LineGapThumb and LineGapText
    setViewValue("LineGapThumb", lstyle.getNewlineSpacing());
    setViewValue("LineGapText", lstyle.getNewlineSpacing());
    
    // If line height min not set (0), update LineHeightMinSpinner with current font size
    // If valid line height min, update LineHeightMinSpinner with line height
    double lineHtMin = lstyle.getMinHeight();
    boolean lineHtMinSet = lineHtMin!=0; if(!lineHtMinSet) lineHtMin = EditorUtils.getFont(editor).getSize();
    setViewValue("LineHeightMinSpinner", lineHtMin);
    
    // If line height max not set, update LineHeightMaxSpinner with current font size
    // If line height max is set, update LineHeightMaxSpinner with line height max
    double lineHtMax = lstyle.getMaxHeight();
    boolean lineHtMaxSet = lineHtMax>999; if(!lineHtMaxSet) lineHtMax = EditorUtils.getFont(editor).getSize();
    setViewValue("LineHeightMaxSpinner", lineHtMax);
    
    // Update PerformWrapCheckBox
    setViewValue("PerformWrapCheckBox", text.isWrapLines()); // Really was for old wrap text stuff
}

/**
 * Handles changes from UI panel controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get editor, currently selected text view and text views (just return if null)
    Editor editor = getEditor();
    TextArea text = getSelectedView(); if(text==null) return;
    List <TextArea> texts = (List)getSelectedViews();
    
    // Register repaint for texts
    for(TextArea t : texts) t.repaint();
    
    // Handle TextArea: Send KeyEvents to Editor.TextEditor (and update its selection after MouseEvents)
    /*if(anEvent.equals(_textView)) {
        
        // Get Editor TextEditor (if not yet installed, SuperSelect text and try again)
        RMEditorTextEditor ted = editor.getTextEditor();
        if(ted==null) {
            getEditor().setSuperSelectedView(text);
            ted = editor.getTextEditor(); if(ted==null) return;
        }
        
        // If KeyEvent, reroute to Editor.TextEditor
        if(anEvent.isKeyEvent()) {
            ted.processKeyEvent(anEvent.getEvent(KeyEvent.class)); anEvent.consume();
            if(anEvent.isKeyPressed()) _textView.hideCursor();
            _textView.setSel(ted.getSelStart(), ted.getSelEnd());
        }
        
        // If MouseEvent, update Editor.TextEditor selection
        if(anEvent.isMouseReleased())
            ted.setSel(_textView.getSelStart(), _textView.getSelEnd(), _textView.getSelAnchor());
    }*/
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    if(anEvent.equals("AlignLeftButton")) EditorUtils.setAlignX(editor, HPos.LEFT);
    if(anEvent.equals("AlignCenterButton")) EditorUtils.setAlignX(editor, HPos.CENTER);
    if(anEvent.equals("AlignRightButton")) EditorUtils.setAlignX(editor, HPos.RIGHT);
    if(anEvent.equals("AlignFullButton")) EditorUtils.setJustify(editor, true);
    if(anEvent.equals("AlignTopButton")) for(TextArea t : texts) t.getTextBox().setAlignY(VPos.TOP);
    if(anEvent.equals("AlignMiddleButton")) for(TextArea t : texts) t.getTextBox().setAlignY(VPos.CENTER);
    if(anEvent.equals("AlignBottomButton")) for(TextArea t : texts) t.getTextBox().setAlignY(VPos.BOTTOM);
    
    // Handle PaddingText
    if(anEvent.equals("PaddingText")) {
        Insets ins = Insets.get(anEvent.getStringValue());
        for(TextArea t : texts) t.setPadding(ins);
    }
    
    // Handle RoundingThumb, RoundingText
    //if(anEvent.equals("RoundingThumb")) text.setRadius(anEvent.getFloatValue());
    //if(anEvent.equals("RoundingText")) text.setRadius(anEvent.getFloatValue());
    
    // Handle RoundingThumb, RoundingText: make sure views have stroke
    if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText"))
        for(TextArea t : texts) t.setBorder(Color.BLACK, 1);

    // Handle PaginateRadio, ShrinkRadio, GrowRadio
    //if(anEvent.equals("PaginateRadio")) texts.forEach(i -> i.setWraps(RMTextShape.WRAP_BASIC));
    //if(anEvent.equals("ShrinkRadio")) texts.forEach(i -> i.setWraps(RMTextShape.WRAP_SCALE));
    //if(anEvent.equals("GrowRadio")) texts.forEach(i -> i.setWraps(RMTextShape.WRAP_NONE));
    
    // Handle CharSpacingThumb/CharSpacingText - have RMEditor set char spacing on currently selected texts
    if(anEvent.equals("CharSpacingThumb") || anEvent.equals("CharSpacingText"))
        setCharSpacing(editor, anEvent.getFloatValue());
    
    // Handle LineSpacingThumb/LineSpacingText - have RMEditor set line spacing on currently selected texts
    if(anEvent.equals("LineSpacingThumb") || anEvent.equals("LineSpacingText"))
        setLineSpacing(editor, anEvent.getFloatValue());

    // Handle LineSpacingSingleButton, LineSpacingDoubleButton
    if(anEvent.equals("LineSpacingSingleButton")) setLineSpacing(editor, 1);
    if(anEvent.equals("LineSpacingDoubleButton")) setLineSpacing(editor, 2);

    // Handle LineGapThumb/LineGapText - have RMEditor set line gap on currently selected texts
    if(anEvent.equals("LineGapThumb") || anEvent.equals("LineGapText"))
        setLineGap(editor, anEvent.getFloatValue());

    // Handle LineHeightMinSpinner - set line height
    if(anEvent.equals("LineHeightMinSpinner"))
        setLineHeightMin(editor, Math.max(anEvent.getFloatValue(), 0));

    // Handle LineHeightMaxSpinner - set line height max to value
    if(anEvent.equals("LineHeightMaxSpinner")) {
        float value = anEvent.getFloatValue(); if(value>=999) value = Float.MAX_VALUE;
        setLineHeightMax(editor, value);
    }
    
    // Handle MakeMinWidthMenuItem, MakeMinHeightMenuItem
    if(anEvent.equals("MakeMinWidthMenuItem")) for(TextArea t : texts) t.setWidth(t.getBestWidth(-1));
    if(anEvent.equals("MakeMinHeightMenuItem")) for(TextArea t : texts) t.setHeight(t.getBestHeight(-1));
    
    // Handle PerformWrapCheckBox
    if(anEvent.equals("PerformWrapCheckBox")) text.setWrapLines(anEvent.getBoolValue());
}

/**
 * Overrides standard tool method to deselect any currently editing text.
 */
public void activateTool()
{
    if(getEditor().getSuperSelectedView() instanceof TextArea)
        getEditor().setSuperSelectedView(getEditor().getSuperSelectedView().getParent());
}

/**
 * Event handling - overridden to install text cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.TEXT); }

/**
 * Event handling - overridden to install text cursor.
 */
public void mouseMoved(T aText, ViewEvent anEvent)
{
    if(getEditor().getViewAtPoint(anEvent.getPoint()) instanceof TextArea) {
        getEditor().setCursor(Cursor.TEXT); anEvent.consume(); }
}

/**
 * Handles mouse pressed for text tool. Special support to super select any text hit by tool mouse pressed.
 */
public void mousePressed(ViewEvent anEvent)
{
    // Register all selectedViews dirty because their handles will probably need to be wiped out
    Editor editor = getEditor();
    for(View v : editor.getSelectedViews()) v.repaint();

    // Get view hit by down point
    _downView = editor.getViewAtPoint(anEvent.getX(),anEvent.getY());
    
    // Get _downPoint from editor
    _downPoint = getEditorEvents().getEventPointInShape(true);
    
    // Create default text instance and set initial bounds to reasonable value
    _view = (T)new TextArea();
    _view.setPlainText(false);
    _view.setWrapLines(true);
    _view.setEditable(true);
    _view.setBounds(getDefaultBounds((TextArea)_view, _downPoint)); // Was setFrame()
    
    // Add text to superSelectedView (within an undo grouping) and superSelect
    editor.undoerSetUndoTitle("Add Text");
    ParentView parent = editor.getSuperSelectedParentView(); ViewTool ptool = getTool(parent);
    ptool.addChild(parent, _view);
    editor.setSuperSelectedView(_view);
    _updatingSize = true;
}

/**
 * Handles mouse dragged for tool. If user doesn't really drag, then default text box should align the base line
 * of the text about the pressed point. If they do really drag, then text box should be the rect they drag out.
 */
public void mouseDragged(ViewEvent anEvent)
{
    // If view wasn't created in mouse down, just return
    if(_view==null) return;
    _view.repaint();
    
    // Get event point in view parent coords
    Point point = getEditorEvents().getEventPointInShape(true);
    point = _view.localToParent(point.x, point.y);
    
    // Get new bounds rect from down point and drag point
    Rect rect = Rect.get(point, _downPoint);
    
    // Get text default bounds
    Rect defaultBounds = getDefaultBounds(_view, _downPoint);

    // If drag rect less than default bounds, reset, otherwise set text bounds to drag rect
    if(rect.getWidth()<defaultBounds.getWidth() || rect.getHeight()<defaultBounds.getHeight()) {
        rect = defaultBounds; _updatingMinHeight = 0; }
    else _updatingMinHeight = rect.getHeight();
    
    // Set new view bounds
    _view.setBounds(rect);  // was setFrame()
}

/**
 * Event handling for text tool mouse loop.
 */
public void mouseReleased(ViewEvent anEvent)
{
    // Get event point in view parent coords
    Point upPoint = getEditorEvents().getEventPointInShape(true);
    upPoint = _view.localToParent(upPoint.x, upPoint.y);
    
    // If upRect is really small, see if the user meant to convert a view to text instead
    if(Math.abs(_downPoint.x - upPoint.x)<=3 && Math.abs(_downPoint.y - upPoint.y)<=3) {
        
        // If hit view is text, just super-select that text and return
        if(_downView instanceof TextArea) {
            ParentView pview = _view.getParent(); ViewTool ptool = getTool(pview);
            ptool.removeChild(pview, _view);
            getEditor().setSuperSelectedView(_downView);
        }
        
        // If hit view is Rectangle, Oval or Polygon, swap for RMText and return
        else if(shouldConvertToText(_downView)) {
            ParentView pview = _view.getParent(); ViewTool ptool = getTool(pview);
            ptool.removeChild(pview, _view);
            convertToText(_downView, null);
        }
    }
    
    // Set editor current tool to select tool and reset tool view
    getEditor().setCurrentToolToSelectTool(); _view = null;
}

/**
 * Event handling for view editing.
 */
public void processEvent(T aText, ViewEvent anEvent)
{
    // Handle KeyEvent: Forward to TextArea and return
    if(anEvent.isKeyEvent()) {
        ViewUtils.processEvent(aText, anEvent);
        aText.repaint(); return;
    }
        
    // If view isn't super selected, just return
    if(!isSuperSelected(aText)) return;
    
    // If mouse event, convert event to text view coords and consume
    if(anEvent.isMouseEvent()) { anEvent.consume();
        anEvent = anEvent.copyForView(aText); }
        
    // Forward to TextArea
    ViewUtils.processEvent(aText, anEvent); aText.repaint();
    if(anEvent.isMouseRelease()) aText.setCaretAnim(isCaretAnimNeeded(aText));
}

/**
 * Event handling for view editing (just forwards to text editor).
 */
public void processKeyEvent(T aText, ViewEvent anEvent)
{
    ViewUtils.processEvent(aText, anEvent);
    if(anEvent.isKeyRelease()) aText.setCaretAnim(isCaretAnimNeeded(aText));
}

/**
 * Returns whether caret anim is needed.
 */
protected boolean isCaretAnimNeeded(TextArea aText)
{
    Editor editor = getEditor();
    return editor.isFocused() && editor.isSuperSelected(aText) && aText.getSel().isEmpty();
}

/**
 * Editor method - installs this text in Editor's text editor.
 */
public void didBecomeSuperSelected(T aText)
{
    // Start listening to changes to TextArea and RichText
    aText.addPropChangeListener(_textAreaSelLsnr, TextArea.Selection_Prop);
    aText.getRichText().addPropChangeListener(_richTextPropLsnr);
    aText.setCaretAnim(isCaretAnimNeeded(aText));
}

/**
 * Editor method - uninstalls this text from RMEditor's text editor and removes new text if empty.
 */
public void willLoseSuperSelected(T aText)
{
    // If text editor was really just an insertion point and ending text length is zero, remove text
    if(_updatingSize && aText.length()==0 && getEditor().getSelectTool().getDragMode()==SelectTool.DragMode.None) {
        ParentView pview = aText.getParent(); ViewTool ptool = getTool(pview);
        ptool.removeChild(pview, aText);
    }

    // Stop listening to changes to TextArea RichText
    aText.removePropChangeListener(_textAreaSelLsnr, TextArea.Selection_Prop);
    aText.getRichText().removePropChangeListener(_richTextPropLsnr);
    aText.setSel(aText.length(), aText.length());
    aText.setCaretAnim(false);
    _updatingSize = false; _updatingMinHeight = 0;
}

/**
 * Called when TextView (in inspector) has selection change to sync with TextView in inspector.
 */
protected void textViewSelChange(PropChange aPC)
{
    TextArea text = getSelectedView(); if(text==null) return;
    text.setSel(_textView.getSelStart(), _textView.getSelEnd());
}

/**
 * Called when selected TextArea has selection change to sync with selected TextArea.
 */
public void textAreaSelChange(PropChange aPC)
{
    TextArea text = getSelectedView(); if(text==null) return;
    _textView.setSel(text.getSelStart(), text.getSelEnd());
}

/**
 * Handle changes to Selected TextArea.RichText
 */
protected void richTextPropChange(PropChange aPC)
{
    // If not UpdatingSize, just return
    if(!_updatingSize) return;
    
    // Get Selected TextArea
    TextArea text = getSelectedView(); if(text==null) return;
    
    // Get preferred text view width
    double maxWidth = _updatingMinHeight==0? text.getParent().getWidth() - text.getX() : text.getWidth();
    double prefWidth = text.getPrefWidth(-1); if(prefWidth>maxWidth) prefWidth = maxWidth;

    // If width gets updated, get & set desired width (make sure it doesn't go beyond page border)
    if(_updatingMinHeight==0)
        text.setWidth(prefWidth);

    // If PrefHeight or current height is greater than UpdatingMinHeight (which won't be zero if user drew a
    //  text box to enter text), set Height to PrefHeight
    double prefHeight = text.getPrefHeight(text.getWidth());
    if(prefHeight>_updatingMinHeight || text.getHeight()>_updatingMinHeight)
        text.setHeight(Math.max(prefHeight, _updatingMinHeight));
}

/**
 * Called when editor changes focus to update SelectedView (TextArea) CaretAnim.
 */
protected void editorFocusedChange()
{
    TextArea text = getSelectedView(); if(text==null) return;
    text.setCaretAnim(isCaretAnimNeeded(text));
}

/**
 * Overrides tool tooltip method to return text string if some chars aren't visible.
 */
public String getToolTip(T aText, ViewEvent anEvent)
{
    // If all text is visible and greater than 8 pt, return null
    if(!aText.getTextBox().isOutOfRoom() && aText.getFont().getSize()>=8) return null;
    
    // Get text string (just return if empty), trim to 64 chars or less and return
    String string = aText.getText(); if(string==null || string.length()==0) return null;
    if(string.length()>64) string = string.substring(0,64) + "...";
    return string;
}

/**
 * Paints selected view indicator, like handles (and maybe a text linking indicator).
 */
public void paintHandles(T aText, Painter aPntr, boolean isSuperSelected)
{
    // If super-selected, draw box
    if(paintBoundsRect(aText)) {
        aPntr.save();
        aPntr.setColor(isSuperSelected? new Color(.9f, .4f, .4f) : Color.LIGHTGRAY);
        aPntr.setStroke(Stroke.Stroke1.copyForDashes(3, 2));
        Shape path = aText.getBoundsShape();
        path = aText.localToParent(path, getEditor());
        aPntr.setAntialiasing(false); aPntr.draw(path); aPntr.setAntialiasing(true);
        aPntr.restore();
    }
    
    // If not super-selected draw normal
    if(!isSuperSelected)
        super.paintHandles(aText, aPntr, isSuperSelected);
}

/**
 * Returns whether to draw bounds rect.
 */
private boolean paintBoundsRect(T aText)
{
    if(aText.getBorder()!=null) return false; // If text draws it's own stroke, return false
    if(getEditor().isSelected(aText) || getEditor().isSuperSelected(aText)) return true; // If selected, return true
    if(aText.length()==0) return true; // If text is zero length, return true
    return false; // Otherwise, return false
}

/**
 * Returns the view class that this tool edits.
 */
public Class getViewClass()  { return TextArea.class; }

/**
 * Returns the name of this tool to be displayed by inspector.
 */
public String getWindowTitle()  { return "Text Inspector"; }

/**
 * Returns whether text tool should convert view to text.
 */
public boolean shouldConvertToText(View aView)
{
    return aView instanceof RectView || aView instanceof ArcView || aView instanceof PathView;
}

/**
 * Converts a view to a TextArea.
 */
public void convertToText(View aView, String aString)
{
    // If view is null, just return
    if(aView==null) return;
    
    // Create TextArea for given view (if given view is text, just use it)
    TextArea text = aView instanceof TextArea? (TextArea)aView : new TextArea();
    
    // Copy attributes of given view
    if(text!=aView)
        text.setBounds(aView.getBounds()); //text.copyShape(aShape); text.setPathShape(aShape);
    
    // Swap this view in for original
    if(text!=aView) { ChildView cview = (ChildView)aView.getParent();
        cview.addChild(text, aView.indexInParent());
        cview.removeChild(aView);
    }
    
    // Install a bogus string for testing
    if(aString!=null && aString.equals("test"))
        aString = getTestString();
    
    // If aString is non-null, install in text
    if(aString!=null)
        text.setText(aString);
    
    // Select new view
    getEditor().setSuperSelectedView(text);
}

/**
 * Returns a rect suitable for the default bounds of a given text at a given point. This takes into account the font
 * and margins of the given text.
 */
private static Rect getDefaultBounds(TextArea aText, Point aPoint)
{
    // Get text font (or default font, if not available)
    Font font = aText.getFont(); //if(font==null) font = Font.getDefaultFont();
    
    // Get bounds and return integral bounds
    Insets ins = aText.getPadding();
    double x = aPoint.getX() - ins.left; //aText.getMarginLeft();
    double y = aPoint.getY() - font.getAscent() - ins.top; //aText.getMarginTop();
    double w = aPoint.getX() + 4 + ins.right - x; //aText.getMarginRight() - x;
    double h = aPoint.getY() + font.getDescent() + ins.bottom - y; //aText.getMarginBottom() - y;
    Rect rect = new Rect(x,y,w,h); rect.snap(); return rect;
}

/**
 * Returns a test string.
 */
private static String getTestString()
{
    return "Leo vitae diam est luctus, ornare massa mauris urna, vitae sodales et ut facilisis dignissim, " +
    "imperdiet in diam, quis que ad ipiscing nec posuere feugiat ante velit. Viva mus leo quisque. Neque mi vitae, " +
    "nulla cras diam fusce lacus, nibh pellentesque libero. " +
    "Dolor at venenatis in, ac in quam purus diam mauris massa, dolor leo vehicula at commodo. Turpis condimentum " +
    "varius aliquet accumsan, sit nullam eget in turpis augue, vel tristique, fusce metus id consequat orci " +
    "penatibus. Ipsum vehicula euismod aliquet, pharetra. " +
    "Fusce lectus proin, neque cr as eget, integer quam facilisi a adipiscing posuere. Imper diet sem sapien. " +
    "Pretium natoque nibh, tristique odio eligendi odio molestie mas sa. Volutpat justo fringilla rut rum augue. " +
    "Lao reet ulla mcorper molestie.";
}

/** Sets the character spacing for the currently selected views. */
private static void setCharSpacing(Editor anEditor, float aValue)
{
    anEditor.undoerSetUndoTitle("Char Spacing Change");
    //for(View view : anEditor.getSelectedOrSuperSelectedViews())
    //    if(view instanceof TextArea) ((TextArea)view).setCharSpacing(aValue);
}

/** Sets the line spacing for all chars (or all selected chars, if editing). */
private static void setLineSpacing(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Line Spacing Change");
    //for(View view : anEditor.getSelectedOrSuperSelectedViews())
    //    if(view instanceof TextArea) ((TextArea)view).setLineSpacing(aHeight);
}

/** Sets the line gap for all chars (or all selected chars, if editing). */
private static void setLineGap(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Line Gap Change");
    //for(View view : anEditor.getSelectedOrSuperSelectedViews())
    //    if(view instanceof TextArea) ((TextArea)view).setLineGap(aHeight);
}

/** Sets the minimum line height for all chars (or all selected chars, if editing). */
private static void setLineHeightMin(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Min Line Height Change");
    //for(View view : anEditor.getSelectedOrSuperSelectedViews())
    //    if(view instanceof TextArea) ((TextArea)view).setLineHeightMin(aHeight);
}

/** Sets the maximum line height for all chars (or all selected chars, if eiditing). */
private static void setLineHeightMax(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Max Line Height Change");
    //for(View view : anEditor.getSelectedOrSuperSelectedViews())
    //    if(view instanceof TextArea) ((TextArea)view).setLineHeightMax(aHeight);
}

}