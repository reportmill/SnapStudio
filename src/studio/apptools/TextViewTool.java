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
 * This class provides UI editing for TextView.
 */
public class TextViewTool <T extends TextView> extends ViewTool <T> {
    
    // The inspector TextView
    TextView            _textView;
    
    // The view hit by text tool on mouse down
    View                _downView;
    
    // Whether editor should resize TextView whenever text changes
    boolean             _updatingSize = false;
    
    // The minimum height of the RMText when editor text editor is updating size
    double              _updatingMinHeight = 0;
    
    // A PropChange listener to listen to selected TextView changes
    PropChangeListener  _textPropLsner = pce -> textPropChange(pce);

/**
 * Returns whether a given view is super-selectable.
 */
public boolean isSuperSelectable(T aView)  { return true; }

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    _textView = getView("TextView", TextView.class);
    _textView.addPropChangeListener(pce -> textViewPropChange(pce), TextView.Selection_Prop);
    getEditor().addPropChangeListener(pce -> editorFocusedChange(), View.Focused_Prop);
}

/**
 * Refreshes UI controls from currently selected text shape.
 */
public void resetUI()
{
    // Get editor and currently selected text
    Editor editor = getEditor();
    TextView text = getSelectedView(); if(text==null) return;
    
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
    
    // Revalidate TextView for (potentially) updated TextShape
    _textView.getTextBox().setText(text.getRichText()); //_textView.setSel(text.getSelStart(),text.getSelEnd());
    
    // Reset PaddingText
    setViewValue("PaddingText", text.getPadding().getStringLong());

    // Get text's background color and set in TextArea if found
    //Color color = null; for(RMShape shape=text; color==null && shape!=null;) {
    //    if(shape.getFill()==null) shape = shape.getParent(); else color = shape.getFill().getColor(); }
    //_textArea.setBackground(color==null? Color.white : color);
    // Get xstring font size and scale up to 12pt if any string run is smaller
    //RMXString xstring = text.getXString(); double fsize = 12;
    //for(int i=0,iMax=xstring.getRunCount();i<iMax;i++) fsize = Math.min(fsize, xstring.getRun(i).getFont().getSize());
    //_textArea.setFontScale(fsize<12? 12/fsize : 1);

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
    boolean lineHtMinSet = lineHtMin!=0; if(!lineHtMinSet) lineHtMin = EditorShapes.getFont(editor).getSize();
    setViewValue("LineHeightMinSpinner", lineHtMin);
    
    // If line height max not set, update LineHeightMaxSpinner with current font size
    // If line height max is set, update LineHeightMaxSpinner with line height max
    double lineHtMax = lstyle.getMaxHeight();
    boolean lineHtMaxSet = lineHtMax>999; if(!lineHtMaxSet) lineHtMax = EditorShapes.getFont(editor).getSize();
    setViewValue("LineHeightMaxSpinner", lineHtMax);
}

/**
 * Handles changes from UI panel controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get editor, currently selected text view and text views (just return if null)
    Editor editor = getEditor();
    TextView text = getSelectedView(); if(text==null) return;
    List <TextView> texts = (List)getSelectedViews();
    
    // Register repaint for texts
    texts.forEach(i -> i.repaint());
    
    // Handle TextView: Send KeyEvents to Editor.TextEditor (and update its selection after MouseEvents)
    /*if(anEvent.getTarget()==_textArea) {
        
        // Get Editor TextEditor (if not yet installed, SuperSelect text and try again)
        RMEditorTextEditor ted = editor.getTextEditor();
        if(ted==null) {
            getEditor().setSuperSelectedShape(text);
            ted = editor.getTextEditor(); if(ted==null) return;
        }
        
        // If KeyEvent, reroute to Editor.TextEditor
        if(anEvent.isKeyEvent()) {
            ted.processKeyEvent(anEvent.getEvent(KeyEvent.class)); anEvent.consume();
            if(anEvent.isKeyPressed()) _textArea.hideCursor();
            _textArea.setSel(ted.getSelStart(), ted.getSelEnd());
        }
        
        // If MouseEvent, update Editor.TextEditor selection
        if(anEvent.isMouseReleased())
            ted.setSel(_textArea.getSelStart(), _textArea.getSelEnd(), _textArea.getSelAnchor());
    }*/
    
    // Handle AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    if(anEvent.equals("AlignLeftButton")) EditorShapes.setAlignX(editor, HPos.LEFT);
    if(anEvent.equals("AlignCenterButton")) EditorShapes.setAlignX(editor, HPos.CENTER);
    if(anEvent.equals("AlignRightButton")) EditorShapes.setAlignX(editor, HPos.RIGHT);
    if(anEvent.equals("AlignFullButton")) EditorShapes.setJustify(editor, true);
    if(anEvent.equals("AlignTopButton")) texts.forEach(i -> i.getTextBox().setAlignY(VPos.TOP));
    if(anEvent.equals("AlignMiddleButton")) texts.forEach(i -> i.getTextBox().setAlignY(VPos.CENTER));
    if(anEvent.equals("AlignBottomButton")) texts.forEach(i -> i.getTextBox().setAlignY(VPos.BOTTOM));
    
    // Handle PaddingText
    if(anEvent.equals("PaddingText")) {
        Insets ins = Insets.get(anEvent.getStringValue());
        texts.forEach(i -> i.setPadding(ins));
    }
    
    // Handle RoundingThumb, RoundingText: make sure shapes have stroke
    if(anEvent.equals("RoundingThumb") || anEvent.equals("RoundingText"))
        for(TextView t : texts) t.setBorder(Color.BLACK, 1);

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
    if(anEvent.equals("MakeMinWidthMenuItem")) texts.forEach(i -> i.setWidth(i.getBestWidth(-1)));
    if(anEvent.equals("MakeMinHeightMenuItem")) texts.forEach(i -> i.setHeight(i.getBestHeight(-1)));
}

/**
 * Overrides standard tool method to deselect any currently editing text.
 */
public void activateTool()
{
    if(getEditor().getSuperSelectedView() instanceof TextView)
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
    if(getEditor().getViewAtPoint(anEvent.getPoint()) instanceof TextView) {
        getEditor().setCursor(Cursor.TEXT); anEvent.consume(); }
}

/**
 * Handles mouse pressed for text tool. Special support to super select any text hit by tool mouse pressed.
 */
public void mousePressed(ViewEvent anEvent)
{
    // Register all selectedShapes dirty because their handles will probably need to be wiped out
    Editor editor = getEditor();
    editor.getSelectedViews().forEach(i -> i.repaint());

    // Get shape hit by down point
    _downView = editor.getViewAtPoint(anEvent.getX(),anEvent.getY());
    
    // Get _downPoint from editor
    _downPoint = getEditorEvents().getEventPointInShape(true);
    
    // Create default text instance and set initial bounds to reasonable value
    _view = (T)new TextView(); _view.setRich(true); _view.setWrapText(true); _view.setFill(null);
    _view.setBounds(getDefaultBounds((TextView)_view, _downPoint)); // Was setFrame()
    
    // Add shape to superSelectedShape (within an undo grouping) and superSelect
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
    
    // Set new shape bounds
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
    
    // If upRect is really small, see if the user meant to conver a shape to text instead
    if(Math.abs(_downPoint.getX() - upPoint.getX())<=3 && Math.abs(_downPoint.getY() - upPoint.getY())<=3) {
        
        // If hit shape is text, just super-select that text and return
        if(_downView instanceof TextView) {
            ParentView pview = _view.getParent(); ViewTool ptool = getTool(pview);
            ptool.removeChild(pview, _view);
            getEditor().setSuperSelectedView(_downView);
        }
        
        // If hit shape is Rectangle, Oval or Polygon, swap for RMText and return
        else if(_downView instanceof RectView || _downView instanceof ArcView || _downView instanceof PathView) {
            ParentView pview = _view.getParent(); ViewTool ptool = getTool(pview);
            ptool.removeChild(pview, _view);
            convertToText(_downView, null);
        }
    }
    
    // Set editor current tool to select tool and reset tool view
    getEditor().setCurrentToolToSelectTool(); _view = null;
}

/**
 * Event handling for shape editing (just forwards to text editor).
 */
public void processEvent(T aText, ViewEvent anEvent)
{
    // Handle KeyEvent: Forward to TextView and return
    if(anEvent.isKeyEvent()) {
        ViewUtils.processEvent(aText, anEvent);
        aText.repaint(); return;
    }
        
    // If shape isn't super selected, just return
    if(!isSuperSelected(aText)) return;
    
    // If mouse event, convert event to text shape coords and consume
    if(anEvent.isMouseEvent()) { anEvent.consume();
        anEvent = anEvent.copyForView(aText); }
        
    // Forward to TextView
    ViewUtils.processEvent(aText, anEvent); aText.repaint();
    if(anEvent.isMouseRelease()) aText.setCaretAnim(isCaretAnimNeeded(aText));
}

/**
 * Event handling for shape editing (just forwards to text editor).
 */
public void processKeyEvent(T aText, ViewEvent anEvent)
{
    ViewUtils.processEvent(aText, anEvent);
    if(anEvent.isKeyRelease()) aText.setCaretAnim(isCaretAnimNeeded(aText));
}

/**
 * Returns whether caret anim is needed.
 */
protected boolean isCaretAnimNeeded(TextView aText)
{
    Editor editor = getEditor();
    return editor.isFocused() && editor.isSuperSelected(aText) && aText.getSel().isEmpty();
}

/**
 * Editor method - installs this text in Editor's text editor.
 */
public void didBecomeSuperSelected(T aText)
{
    // Start listening to changes to TextView and RichText
    aText.addPropChangeListener(_textPropLsner);
    aText.getRichText().addPropChangeListener(_textPropLsner);
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

    // Stop listening to changes to TextShape RichText
    aText.removePropChangeListener(_textPropLsner);
    aText.getRichText().removePropChangeListener(_textPropLsner);
    aText.setSel(aText.length(), aText.length());
    aText.setCaretAnim(false);
    _updatingSize = false; _updatingMinHeight = 0;
}

/**
 * Called when selected TextView has property changes.
 */
public void textPropChange(PropChange aPC)
{
    // Get Selected TextView
    TextView text = getSelectedView(); if(text==null) return;
    String prop = aPC.getPropertyName();
    
    // If updating size, reset text width & height to accommodate text
    if(_updatingSize && aPC.getSource() instanceof RichText)
        runLater(() -> resizeText(text));
    
    // Sync selection
    if(prop==TextView.Selection_Prop)
        _textView.setSel(text.getSelStart(), text.getSelEnd());
}

/**
 * Called when TextView (in inspector) has property changes.
 */
protected void textViewPropChange(PropChange aPC)
{
    TextView text = getSelectedView();
    text.setSel(_textView.getSelStart(), _textView.getSelEnd());
}

/**
 * Called when editor changes focus to update SelectedView (TextView) CaretAnim.
 */
protected void editorFocusedChange()
{
    TextView text = getSelectedView(); if(text==null) return;
    text.setCaretAnim(isCaretAnimNeeded(text));
}

/**
 * Resizes Selected TextView for current content.
 */
protected void resizeText(TextView aText)
{
    // Get preferred text shape width
    double maxWidth = _updatingMinHeight==0? aText.getParent().getWidth() - aText.getX() : aText.getWidth();
    double prefWidth = aText.getPrefWidth(); if(prefWidth>maxWidth) prefWidth = maxWidth;

    // If width gets updated, get & set desired width (make sure it doesn't go beyond page border)
    if(_updatingMinHeight==0)
        aText.setWidth(prefWidth);

    // If PrefHeight or current height is greater than UpdatingMinHeight (which won't be zero if user drew a
    //  text box to enter text), set Height to PrefHeight
    double prefHeight = aText.getPrefHeight();
    if(prefHeight>_updatingMinHeight || aText.getHeight()>_updatingMinHeight)
        aText.setHeight(Math.max(prefHeight, _updatingMinHeight));
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
 * Paints selected shape indicator, like handles (and maybe a text linking indicator).
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
public Class getViewClass()  { return TextView.class; }

/**
 * Returns the name of this tool to be displayed by inspector.
 */
public String getWindowTitle()  { return "Text Inspector"; }

/**
 * Converts a shape to a text shape.
 */
public void convertToText(View aView, String aString)
{
    // If shape is null, just return
    if(aView==null) return;
    
    // Get text shape for given shape (if given shape is text, just use it)
    TextView text = aView instanceof TextView? (TextView)aView : new TextView();
    
    // Copy attributes of given shape
    if(text!=aView)
        text.setBounds(aView.getBounds()); //text.copyShape(aShape); text.setPathShape(aShape);
    
    // Swap this shape in for original
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
    
    // Select new shape
    getEditor().setSuperSelectedView(text);
}

/**
 * Returns a rect suitable for the default bounds of a given text at a given point. This takes into account the font
 * and margins of the given text.
 */
private static Rect getDefaultBounds(TextView aText, Point aPoint)
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

/** Sets the character spacing for the currently selected shapes. */
private static void setCharSpacing(Editor anEditor, float aValue)
{
    anEditor.undoerSetUndoTitle("Char Spacing Change");
    //for(View shape : anEditor.getSelectedOrSuperSelectedShapes())
    //    if(shape instanceof TextView) ((TextView)shape).setCharSpacing(aValue);
}

/** Sets the line spacing for all chars (or all selected chars, if editing). */
private static void setLineSpacing(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Line Spacing Change");
    //for(View shape : anEditor.getSelectedOrSuperSelectedShapes())
    //    if(shape instanceof TextView) ((TextView)shape).setLineSpacing(aHeight);
}

/** Sets the line gap for all chars (or all selected chars, if editing). */
private static void setLineGap(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Line Gap Change");
    //for(View shape : anEditor.getSelectedOrSuperSelectedShapes())
    //    if(shape instanceof TextView) ((TextView)shape).setLineGap(aHeight);
}

/** Sets the minimum line height for all chars (or all selected chars, if editing). */
private static void setLineHeightMin(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Min Line Height Change");
    //for(View shape : anEditor.getSelectedOrSuperSelectedShapes())
    //    if(shape instanceof TextView) ((TextView)shape).setLineHeightMin(aHeight);
}

/** Sets the maximum line height for all chars (or all selected chars, if eiditing). */
private static void setLineHeightMax(Editor anEditor, float aHeight)
{
    anEditor.undoerSetUndoTitle("Max Line Height Change");
    //for(View shape : anEditor.getSelectedOrSuperSelectedShapes())
    //    if(shape instanceof TextView) ((TextView)shape).setLineHeightMax(aHeight);
}

}