package studio.app;
import java.util.*;
import snap.geom.Point;
import snap.util.*;
import snap.view.*;

/**
 * Handles editor methods specific to clipboard operations (cut, copy paste).
 */
public class EditorClipboard {
    
    // The MIME type for reportmill xstring
    public static final String    SNAP_XML_TYPE = "snap-studio/xml";
    
/**
 * Handles editor cut operation.
 */
public static void cut(Editor anEditor)
{
    // If text editing, have text editor do copy instead
    /*if(anEditor.getTextEditor()!=null) anEditor.getTextEditor().cut(); else { */
    
    // If not text editing, do copy and delete (and null anchor & smart paste shape)
    anEditor.copy();
    anEditor.delete();
    anEditor._lastCopyView = anEditor._lastPasteView = null;
}

/**
 * Handles editor copy operation.
 */
public static void copy(Editor anEditor)
{
    // If text editing, have text editor do copy instead
    /*if(anEditor.getTextEditor()!=null) anEditor.getTextEditor().copy(); else { */

    // If not text editing, add selected shapes (serialized) to pasteboard for DrawPboardType
    if(!(anEditor.getSelectedOrSuperSelectedView()==anEditor.getContent()) &&
            !(anEditor.getSelectedOrSuperSelectedView()==anEditor.getContentPage())) {
        
        // Get xml for selected shapes, and get as string
        XMLElement xml = new ViewArchiver().writeObject(anEditor.getSelectedOrSuperSelectedViews());
        String xmlStr = xml.toString();
        
        // Get clipboard and add data as XML string (RMData) and plain string
        Clipboard cb = Clipboard.get();
        cb.addData(SNAP_XML_TYPE, xmlStr);
        cb.addData(xmlStr);
        
        // Reset Editor.LastCopyShape/LastPasteShape
        anEditor._lastCopyView = anEditor.getSelectedView(0); anEditor._lastPasteView = null;
    }
    
    // Otherwise beep
    else ViewUtils.beep();
}

/**
 * Handles editor paste operation.
 */
public static void paste(Editor anEditor)
{
    // If text editing, have text editor do paste instead
    /*if(anEditor.getTextEditor()!=null) anEditor.getTextEditor().paste(); else { */
    
    // If not text editing, do paste for system clipboard
    ParentView parent = anEditor.firstSuperSelectedViewThatAcceptsChildren();
    paste(anEditor, Clipboard.get(), parent, null);
}

/**
 * Handles editor paste operation for given transferable, parent shape and location.
 */
public static void paste(Editor anEditor, Clipboard aCB, ParentView aParent, Point aPoint)
{
    // Declare variable for pasted view
    View pastedView = null;

    // If PasteBoard has ReportMill Data, paste it
    if(aCB.hasData(SNAP_XML_TYPE)) {
        
        // Unarchive shapes from clipboard bytes
        Object object = getViewsFromClipboard(anEditor, aCB);
        
        // If data is list of previously copied shapes, add them
        if(object instanceof List) {
            List shapes = (List)object;
            anEditor.undoerSetUndoTitle("Paste Shape" + (shapes.size()>1? "s" : ""));
            anEditor.addViewsToView(shapes, aParent, true);
            anEditor.setSelectedViews(shapes);
        }
        
        // If data is text, create text object and add it
        /*else if(object instanceof RMXString) {
            RMTextShape text = new RMTextShape((RMXString)object);
            double width = Math.min(text.getPrefWidth(), aParent.getWidth());
            double height = Math.min(text.getPrefHeight(), aParent.getHeight());
            text.setSize(width, height);
            anEditor.undoerSetUndoTitle("Paste Text");
            anEditor.addShapesToShape(Arrays.asList(text), aParent, true);
            anEditor.setSelectedShape(text);
        }*/
        
        // Promote _smartPastedShape to anchor and set new _smartPastedShape
        if(anEditor._lastPasteView!=null)
            anEditor._lastCopyView = anEditor._lastPasteView;
        anEditor._lastPasteView = anEditor.getSelectedView(0);
        
    }
    
    // Paste Image
    //else if(aCB.hasImage()) { Image img = aCB.getImage(); pastedShape = new RMImageShape(img.getBytes()); }
    
    // paste pdf
    //else if((pastedView=getTransferPDF(aCB)) != null) { }
    
    // last one - plain text
    else if((pastedView=getTransferText(aCB)) != null) { }
        
    // Might as well log unsupported paste types
    else { //for(String typ : aCB.getMIMETypes()) System.err.println("Unsupported type: " + type);
        ViewUtils.beep(); }

    // Add pastedShape
    if(pastedView!=null) {
        
        // Set undo title
        anEditor.undoerSetUndoTitle("Paste");
        
        // Resize/relocate shape (if point was provided, move pasted shape to that point)
        pastedView.setSize(pastedView.getBestSize());
        if(aPoint!=null) {
            aPoint = aParent.parentToLocal(aPoint.x, aPoint.y, anEditor);
            pastedView.setXY(aPoint.getX() - pastedView.getWidth()/2, aPoint.getY() - pastedView.getHeight()/2);
        }
        
        // Add pasted shape to parent
        //if(aParent instanceof)
        ViewTool tool = anEditor.getTool(aParent);
        tool.addChild(aParent, pastedView);

        // Select imageShape, set selectTool and redisplay
        anEditor.setSelectedView(pastedView);
        anEditor.setCurrentToolToSelectTool();
        anEditor.repaint();
    }
}

/**
 * Returns the first Shape read from the system clipboard.
 */
public static View getViewFromClipboard(Editor anEditor)
{
    Object shapes = getViewsFromClipboard(anEditor, null);
    if(shapes instanceof List) shapes = ListUtils.get((List)shapes, 0);
    return shapes instanceof View? (View)shapes : null;
}

/**
 * Returns the shape or shapes read from the given transferable (uses system clipboard if null).
 */
public static Object getViewsFromClipboard(Editor anEditor, Clipboard aCB)
{
    // If no contents, use system clipboard
    Clipboard cboard = aCB!=null? aCB : Clipboard.get();
    
    // If PasteBoard has ReportMill Data, paste it
    if(!cboard.hasData(SNAP_XML_TYPE))
        return null;
    
    // Get unarchived object from clipboard bytes
    byte bytes[] = cboard.getDataBytes(SNAP_XML_TYPE);
    Object obj = new ViewArchiver().readObject(bytes);

    // A bit of a hack - remove any non-shapes (plugins for one)
    if(obj instanceof List) { List list = (List)obj;
        for(int i=list.size()-1; i>=0; --i)
            if(!(list.get(i) instanceof View))
                list.remove(i);
    }
        
    // Return object
    return obj;
}

/**
 * Returns an RMText object with the contents if there's a plain text string on the clipboard.
 */
public static View getTransferText(Clipboard aCB) 
{
    String str = aCB.getString();
    return null;//str!=null? new RMTextShape(string) : null;
}

}