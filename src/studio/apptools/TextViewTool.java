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
public class TextViewTool <T extends TextView> extends ViewTool <T> implements PropChangeListener {
    
    // The text area
    TextViewPane      _textView;
    
    // The shape hit by text tool on mouse down
    View              _downView;
    
    // Whether editor should resize RMText whenever text changes
    boolean           _updatingSize = false;
    
    // The minimum height of the RMText when editor text editor is updating size
    double            _updatingMinHeight = 0;

/**
 * Returns whether a given view is super-selectable.
 */
public boolean isSuperSelectable(T aView)
{
    return true;
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get the TextPrea
    _textView = getView("TextPane", TextViewPane.class);
}

/**
 * Refreshes UI controls from currently selected text shape.
 */
public void resetUI()
{
    // Get editor and currently selected text
    Editor editor = getEditor();
    TextView text = getSelectedShape(); if(text==null) return;
    
    // Get paragraph from text
    //RMParagraph pgraph = text.getXString().getParagraphAt(0);
    TextStyle style = text.getRichText().getStyleAt(0);
    TextLineStyle lstyle = text.getRichText().getLineStyleAt(0);
    
    // If editor is text editing, get paragraph from text editor instead
    //RMTextEditor ted = editor.getTextEditor();
    //if(ted!=null) pgraph = ted.getInputParagraph();
    
    // Update AlignLeftButton, AlignCenterButton, AlignRightButton, AlignFullButton, AlignTopButton, AlignMiddleButton
    setViewValue("AlignLeftButton", lstyle.getAlign()==HPos.LEFT && !lstyle.isJustify());
    setViewValue("AlignCenterButton", lstyle.getAlign()==HPos.CENTER && !lstyle.isJustify());
    setViewValue("AlignRightButton", lstyle.getAlign()==HPos.RIGHT && !lstyle.isJustify());
    setViewValue("AlignFullButton", lstyle.isJustify());
    setViewValue("AlignTopButton", text.getTextBox().getAlignY()==VPos.TOP);
    setViewValue("AlignMiddleButton", text.getTextBox().getAlignY()==VPos.CENTER);
    setViewValue("AlignBottomButton", text.getTextBox().getAlignY()==VPos.BOTTOM); // Update AlignBottomButton
    
    // Revalidate TextPane for (potentially) updated TextShape
    _textView.getTextBox().setText(text.getRichText());
    //if(ted!=null) _textView.setSel(ted.getSelStart(),ted.getSelEnd());

    // Get text's background color and set in TextArea if found
    //Color color = null; for(RMShape shape=text; color==null && shape!=null;) {
    //    if(shape.getFill()==null) shape = shape.getParent(); else color = shape.getFill().getColor(); }
    //_textArea.setBackground(color==null? Color.white : color);
    // Set the xstring in text inspector
    //RMXString xstring = text.getXString();
    //if(!_textArea.isFocusOwner()) _textArea.setXString(xstring);
    // Get xstring font size and scale up to 12pt if any string run is smaller
    //double fsize = 12;
    //for(int i=0,iMax=xstring.getRunCount();i<iMax;i++) fsize = Math.min(fsize, xstring.getRun(i).getFont().getSize());
    //_textArea.setFontScale(fsize<12? 12/fsize : 1);

    // Update PaginateRadio, ShrinkRadio, GrowRadio
    //setViewValue("PaginateRadio", text.getWraps()==RMTextShape.WRAP_BASIC);
    //setViewValue("ShrinkRadio", text.getWraps()==RMTextShape.WRAP_SCALE);
    //setViewValue("GrowRadio", text.getWraps()==RMTextShape.WRAP_NONE);
    
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
    // Get editor, currently selected text shape and text shapes (just return if null)
    Editor editor = getEditor();
    TextView text = getSelectedShape(); if(text==null) return;
    List <TextView> texts = (List)getSelectedShapes();
    
    // Register repaint for texts
    texts.forEach(i -> i.repaint());
    
    // Handle TextArea: Send KeyEvents to Editor.TextEditor (and update its selection after MouseEvents)
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
    if(anEvent.equals("AlignLeftButton")) EditorShapes.setAlignmentX(editor, HPos.LEFT);
    if(anEvent.equals("AlignCenterButton")) EditorShapes.setAlignmentX(editor, HPos.CENTER);
    if(anEvent.equals("AlignRightButton")) EditorShapes.setAlignmentX(editor, HPos.RIGHT);
    //if(anEvent.equals("AlignFullButton")) EditorShapes.setAlignmentX(editor, RMTypes.AlignX.Full);
    if(anEvent.equals("AlignTopButton")) texts.forEach(i -> i.getTextBox().setAlignY(VPos.TOP));
    if(anEvent.equals("AlignMiddleButton")) texts.forEach(i -> i.getTextBox().setAlignY(VPos.CENTER));
    if(anEvent.equals("AlignBottomButton")) texts.forEach(i -> i.getTextBox().setAlignY(VPos.BOTTOM));
    
    // If RoundingThumb or RoundingText, make sure shapes have stroke
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
    
    // Handle TurnToPathMenuItem
    /*if(anEvent.equals("TurnToPathMenuItem"))
        for(int i=0; i<texts.size(); i++) { TextView text1 = texts.get(i);
            View textPathShape = TextViewUtils.getTextPathShape(text1);
            ParentView parent = text1.getParent();
            parent.addChild(textPathShape, text1.indexOf());
            parent.removeChild(text1);
            editor.setSelectedShape(textPathShape);
        }*/
    
    // Handle TurnToCharsShapeMenuItem
    /*if(anEvent.equals("TurnToCharsShapeMenuItem"))
        for(int i=0; i<texts.size(); i++) { TextView text1 = texts.get(i);
            ShapeView textCharsShape = TextShapeUtils.getTextCharsShape(text1);
            ParentView parent = text1.getParent();
            parent.addChild(textCharsShape, text1.indexOf());
            parent.removeChild(text1);
            editor.setSelectedShape(textCharsShape);
        }*/
    
    // Handle LinkedTextMenuItem
    /*if(anEvent.equals("LinkedTextMenuItem")) {
        
        // Get linked text identical to original text and add to text's parent
        LinkedText linkedText = new LinkedText(text);
        text.getParent().addChild(linkedText);
        
        // Shift linked text down if there's room, otherwise right, otherwise just offset by quarter inch
        if(text.getFrameMaxY() + 18 + text.getFrame().height*.75 < text.getParent().getHeight())
            linkedText.offsetXY(0, text.getHeight() + 18);
        else if(text.getFrameMaxX() + 18 + text.getFrame().width*.75 < text.getParent().getWidth())
            linkedText.offsetXY(text.getWidth() + 18, 0);
        else linkedText.offsetXY(18, 18);
        
        // Select and repaint new linked text
        editor.setSelectedShape(linkedText); linkedText.repaint();
    }*/   
}

/**
 * Overrides standard tool method to deselect any currently editing text.
 */
public void activateTool()
{
    if(getEditor().getSuperSelectedShape() instanceof TextView)
        getEditor().setSuperSelectedShape(getEditor().getSuperSelectedShape().getParent());
}

/**
 * Event handling - overridden to install text cursor.
 */
public void mouseMoved(ViewEvent anEvent)  { getEditor().setCursor(Cursor.TEXT); }

/**
 * Event handling - overridden to install text cursor.
 */
public void mouseMoved(T aShape, ViewEvent anEvent)
{
    if(getEditor().getShapeAtPoint(anEvent.getPoint()) instanceof TextView) {
        getEditor().setCursor(Cursor.TEXT); anEvent.consume(); }
}

/**
 * Handles mouse pressed for text tool. Special support to super select any text hit by tool mouse pressed.
 */
public void mousePressed(ViewEvent anEvent)
{
    // Register all selectedShapes dirty because their handles will probably need to be wiped out
    Editor editor = getEditor();
    editor.getSelectedShapes().forEach(i -> i.repaint());

    // Get shape hit by down point
    _downView = editor.getShapeAtPoint(anEvent.getX(),anEvent.getY());
    
    // Get _downPoint from editor
    _downPoint = getEditorEvents().getEventPointInShape(true);
    
    // Create default text instance and set initial bounds to reasonable value
    _view = (T)new TextView(); _view.setBorder(Color.BLACK, 10);
    _view.setBounds(getDefaultBounds((TextView)_view, _downPoint)); // Was setFrame()
    
    // Add shape to superSelectedShape (within an undo grouping) and superSelect
    editor.undoerSetUndoTitle("Add Text");
    ParentView parent = editor.getSuperSelectedParentShape(); ViewTool ptool = getTool(parent);
    ptool.addChild(parent, _view);
    editor.setSuperSelectedShape(_view);
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
public void mouseReleased(ViewEvent e)
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
            getEditor().setSuperSelectedShape(_downView);
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
public void processEvent(T aTextView, ViewEvent anEvent)
{
    // Handle KeyEvent
    if(anEvent.isKeyEvent()) {
        processKeyEvent(aTextView, anEvent); return; }
        
    // If shape isn't super selected, just return
    if(!isSuperSelected(aTextView)) return;
    
    // If mouse event, convert event to text shape coords and consume
    if(anEvent.isMouseEvent()) { anEvent.consume();
        //Point pnt = getEditor().convertToShape(anEvent.getX(), anEvent.getY(), aTextView);
        //anEvent = anEvent.copyForPoint(pnt.getX(), pnt.getY());
        anEvent = anEvent.copyForView(aTextView);
    }
        
    // Forward on to editor
    //aTextShape.getTextEditor().processEvent(anEvent); aTextShape.repaint();
    ViewUtils.processEvent(aTextView, anEvent);
}

/**
 * Key event handling for super selected text.
 */
public void processKeyEvent(T aTextView, ViewEvent anEvent)
{
    // Have text editor process key event
    //aTextView.getTextEditor().processEvent(anEvent); aTextShape.repaint();
    ViewUtils.processEvent(aTextView, anEvent);
}

/**
 * Editor method - installs this text in Editor's text editor.
 */
public void didBecomeSuperSelected(T aTextShape)
{
    // If not superselected by TextInspector pane, have editor request focus
    //if(!isUISet() || !_textArea.hasFocus()) anEditor.requestFocus();
    
    // Start listening to changes to TextShape RichText
    aTextShape.getRichText().addPropChangeListener(this);
    
    // If UI is loaded, install string in text area
    //if(isUISet()) _textArea.getTextEditor().setXString(text.getXString());
}

/**
 * Editor method - uninstalls this text from RMEditor's text editor and removes new text if empty.
 */
public void willLoseSuperSelected(T aTextView)
{
    // If text editor was really just an insertion point and ending text length is zero, remove text
    if(_updatingSize && aTextView.length()==0 && getEditor().getSelectTool().getDragMode()==SelectTool.DragMode.None) {
        ParentView pview = aTextView.getParent(); ViewTool ptool = getTool(pview);
        ptool.removeChild(pview, aTextView);
    }

    // Stop listening to changes to TextShape RichText
    aTextView.getRichText().removePropChangeListener(this);
    _updatingSize = false; _updatingMinHeight = 0;
    
    // Set text editor's text shape to null
    //aTextShape.clearTextEditor();
}

/**
 * Handle changes to Selected TextShape 
 */
public void propertyChange(PropChange aPC)
{
    // If updating size, reset text width & height to accommodate text
    if(_updatingSize) {
        
        // Get TextShape
        TextView textShape = getSelectedShape(); if(textShape==null) return;
    
        // Get preferred text shape width
        double maxWidth = _updatingMinHeight==0? textShape.getParent().getWidth() - textShape.getX() :
            textShape.getWidth();
        double prefWidth = textShape.getPrefWidth(); if(prefWidth>maxWidth) prefWidth = maxWidth;

        // If width gets updated, get & set desired width (make sure it doesn't go beyond page border)
        if(_updatingMinHeight==0)
            textShape.setWidth(prefWidth);

        // If PrefHeight or current height is greater than UpdatingMinHeight (which won't be zero if user drew a
        //  text box to enter text), set Height to PrefHeight
        double prefHeight = textShape.getPrefHeight();
        if(prefHeight>_updatingMinHeight || textShape.getHeight()>_updatingMinHeight)
            textShape.setHeight(Math.max(prefHeight, _updatingMinHeight));
    }
}

/**
 * Overrides tool tooltip method to return text string if some chars aren't visible.
 */
public String getToolTip(T aTextView, ViewEvent anEvent)
{
    // If all text is visible and greater than 8 pt, return null
    if(!aTextView.getTextBox().isOutOfRoom() && aTextView.getFont().getSize()>=8) return null;
    
    // Get text string (just return if empty), trim to 64 chars or less and return
    String string = aTextView.getText(); if(string==null || string.length()==0) return null;
    if(string.length()>64) string = string.substring(0,64) + "...";
    return string;
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
    getEditor().setSuperSelectedShape(text);
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

/**
 * A TextView subclass to edit current text shape text in text tool.
 */
public static class TextViewPane extends TextView {
    
    /** Returns the TextTool. */
    TextViewTool tool()  { return getOwner(TextViewTool.class); }
    
    /** Returns the current editor. */
    Editor editor()  { return tool().getEditor(); }
    
    /** Returns the current text shape. */
    View shape()  { Editor e = editor(); return e!=null? e.getSelectedOrSuperSelectedShape() : null; }
    
    /** Returns the current text shape. */
    TextView text()  { View s = shape(); return s instanceof TextView? (TextView)s : null; }
    
    /** Returns the current text editor. */
    //TextEditor ted()  { Editor e = editor(); return e!=null? e.getTextEditor() : null; }
    
    /** Sets the character index of the start and end of the text selection. */
    public void setSel(int aStart, int anEnd, int anAnchor)
    {
        super.setSel(aStart, anEnd, anAnchor);
        //TextEditor ted = ted(); if(ted!=null) ted.setSel(aStart,anEnd, anAnchor);
        TextView text = text(); text.setSel(aStart, anEnd); //if(text!=null) text.repaint();
    }
}

}