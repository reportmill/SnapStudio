package studio.app;
import java.io.*;
import java.util.*;
import snap.gfx.Point;
import snap.util.*;
import snap.view.*;

/**
 * Handles editor methods specific to clipboard operations (cut, copy paste).
 */
public class EditorClipboard {
    
    // A defined data flavor for RM shapes and DataFlavors supported by Editor
    //public static DataFlavor RMDataFlavor = new DataFlavor("application/reportmill", "ReportMill Shape Data");
    //public static DataFlavor SupportedFlavors[] = { RMDataFlavor, DataFlavor.stringFlavor };
    
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
        
        // Get xml for selected shapes, create and set in EditorClipboard and install in SystemClipboard
        XMLElement xml = new ViewArchiver().writeObject(anEditor.getSelectedOrSuperSelectedViews());
        //EditorClipboard ec = new EditorClipboard(xml.getBytes());tkit.getSystemClipboard().setContents(ec,null);
        Clipboard cb = Clipboard.get();
        cb.setContent("RMData", xml.getBytes(), Clipboard.STRING, xml.toString());
        
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
    Clipboard cb = Clipboard.get(); //tkit.getSystemClipboard().getContents(null);
    ParentView parent = anEditor.firstSuperSelectedViewThatAcceptsChildren();
    paste(anEditor, cb, parent, null);
}

/**
 * Handles editor paste operation for given transferable, parent shape and location.
 */
public static void paste(Editor anEditor, Clipboard aCB, ParentView aParent, Point aPoint)
{
    // Declare variable for pasted view
    View pastedView = null;

    // If PasteBoard has ReportMill Data, paste it
    if(aCB.hasContent("RMData")) try { //isDataFlavorSupported(RMDataFlavor)) try {
        
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
    
    // Catch paste RMData exceptions
    catch(Exception e) { e.printStackTrace(); }
    
    // Paste Image
    //else if(aCB.isDataFlavorSupported(DataFlavor.imageFlavor)) try {
    //    Image image = (Image)contents.getTransferData(DataFlavor.imageFlavor);
    //    byte bytes[] = RMAWTUtils.getBytesJPEG(image);
    //    pastedShape = new RMImageShape(bytes); }
    //catch(Exception e) { e.printStackTrace(); }
    
    // paste pdf
    //else if((pastedView=getTransferPDF(aCB)) != null) { }
    
    // last one - plain text
    else if((pastedView=getTransferText(aCB)) != null) { }
        
    // Might as well log unsupported paste types
    else {
        //DataFlavor flvrs[] = contents.getTransferDataFlavors();
        //for(DataFlavor f : flvrs) System.err.println("Unsupported flavor: " + f.getMimeType() + " " + f.getSubType());
        ViewUtils.beep();
    }

    // Add pastedShape
    if(pastedView!=null) {
        
        // Set undo title
        anEditor.undoerSetUndoTitle("Paste");
        
        // Resize/relocate shape (if point was provided, move pasted shape to that point)
        pastedView.setSize(pastedView.getBestSize());
        if(aPoint!=null) {
            aPoint = aParent.parentToLocal(anEditor, aPoint.x, aPoint.y);
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
    if(aCB==null)
        aCB = Clipboard.get(); // tkit.getSystemClipboard().getContents(null);        
    
    // If PasteBoard has ReportMill Data, paste it
    if(aCB.hasContent("RMData")) try { //isDataFlavorSupported(RMDataFlavor)) try {
    
        // Get bytes from clipboard
        InputStream bis = (InputStream)aCB.getContent("RMData"); //getTransferData(RMDataFlavor);
        byte bytes[] = new byte[bis.available()];
        bis.read(bytes);
        
        // Get unarchived object from clipboard bytes
        Object object = new ViewArchiver().readObject(bytes);

        // A bit of a hack - remove any non-shapes (plugins for one)
        if(object instanceof List) { List list = (List)object;
            for(int i=list.size()-1; i>=0; --i)
                if(!(list.get(i) instanceof View))
                    list.remove(i);
        }
        
        // Return object
        return object;
    }
    
    // Handle exceptions and return
    catch(Exception e) { e.printStackTrace(); }
    return null;
}

/**
 * Returns an RMText object with the contents if there's a plain text string on the clipboard.
 */
public static View getTransferText(Clipboard aCB) 
{
    String string = aCB.getString();
    return null;//string==null? null : new RMTextShape(string);
}

/**
 * Returns an RMImage with the contents if there's a pdf image on the clipboard.
 */
/*public static RMShape getTransferPDF(Clipboard aCB) 
{
    try {
        //DataFlavor pdflav = new DataFlavor("application/pdf");
        if(aCB.hasContent("application/pdf")) { //.isDataFlavorSupported(pdflav)) {
            InputStream ps = (InputStream)aCB.getContent("application/pdf"); //contents.getTransferData(pdflav);
            if(ps!=null) return new RMImageShape(ps);
        }
    }
    catch(Exception e) { e.printStackTrace(); } return null;
}*/

/** Transferable methods. */
//public DataFlavor[] getTransferDataFlavors()  { return SupportedFlavors; }
//public boolean isDataFlavorSupported(DataFlavor f){ return f.equals(RMDataFlavor)||f.equals(DataFlavor.stringFlavor);}
/*public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException {
    if(aFlavor.equals(RMDataFlavor)) return new ByteArrayInputStream(_bytes);
    if(aFlavor.equals(DataFlavor.stringFlavor)) return new String(_bytes);
    throw new UnsupportedFlavorException(aFlavor); }*/

}