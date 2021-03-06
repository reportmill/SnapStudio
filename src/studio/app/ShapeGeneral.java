package studio.app;
import java.util.Collections;
import java.util.List;
import snap.view.*;

/**
 * An inspector for general shape attributes, like property keys, name, text wrap around, etc.
 */
public class ShapeGeneral extends EditorPane.SupportPane {
    
    // The bindings table
    TableView <String>  _bindingsTable;

/**
 * Creates a new ShapeGeneral pane.
 */
public ShapeGeneral(EditorPane anEP)  { super(anEP); }

/**
 * Initialize UI panel for this inspector.
 */
protected void initUI()
{
    // Get bindings table
    _bindingsTable = getView("BindingsTable", TableView.class); _bindingsTable.setRowHeight(18);
    _bindingsTable.setCellConfigure(this :: configureBindingsTable);
    enableEvents(_bindingsTable, MouseRelease, DragDrop);
}

/**
 * Updates UI controsl from current selection.
 */
public void resetUI()
{
    // Get currently selected view
    View view = getSelectedView();
    
    // Reset NameText, LockedCheckBox, UrlText
    setViewValue("NameText", view.getName());
    //setViewValue("LockedCheckBox", view.getName());
    //setViewValue("UrlText", view.getURL());

    // Reset table model shape
    _bindingsTable.setItems(new String[] { "X", "Y", "Width", "Height" }); //shape.getPropNames());
    if(_bindingsTable.getSelIndex()<0) _bindingsTable.setSelIndex(0);
    _bindingsTable.updateItems();
    
    // Reset BindingsText
    String pname = _bindingsTable.getSelItem();
    Binding binding = view.getBinding(pname);
    setViewValue("BindingsText", binding!=null? binding.getKey() : null);
}

/**
 * Updates current selection from UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current editor and selected view (just return if null) and selected shapes
    View view = getSelectedView(); if(view==null) return;
    List <View> views = getEditor().getSelectedOrSuperSelectedViews();
    
    // Handle NameText, LockedCheckBox, UrlText
    if(anEvent.equals("NameText")) view.setName(anEvent.getStringValue());
    //if(anEvent.equals("LockedCheckBox")) view.setLocked(anEvent.getStringValue());
    //if(anEvent.equals("UrlText")) view.setURL(anEvent.getStringValue());

    // Handle BindingsTable
    if(anEvent.equals("BindingsTable")) {
        
        // Handle MouseClick
        if(anEvent.getClickCount()==2)
            requestFocus(getView("BindingsText"));
            
        // Handle DragDrop
        if(anEvent.isDragDrop()) {
            Clipboard dboard = anEvent.getClipboard(); anEvent.acceptDrag();
            if(dboard.hasString()) {
                int row = _bindingsTable.getRowAt(anEvent.getX(), anEvent.getY()); if(row<0) return;
                //String pname = shape.getPropNames()[row];
                //String bkey = KeysPanel.getDragKey();
                //shape.addBinding(pname, bkey);
            }
            anEvent.dropComplete();
        }
    }
    
    // Handle BindingsText
    if(anEvent.equals("BindingsText")) {
        
        // Get selected PropertyName and Key
        String pname = _bindingsTable.getSelItem(); if(pname==null) return;
        String key = getViewStringValue("BindingsText"); if(key!=null && key.length()==0) key = null;
        
        // Remove previous binding and add new one (if valid)
        for(View shp : views)
            if(key!=null) shp.addBinding(pname, key);
            else shp.removeBinding(pname);
    }
}

/**
 * Returns the current selected view for the current editor.
 */
public View getSelectedView()
{
    Editor e = getEditor(); if(e==null) return null;
    return e.getSelectedOrSuperSelectedView();
}

/**
 * Returns the current selected shape for the current editor.
 */
public List <? extends View> getSelectedViews()
{
    Editor e = getEditor(); if(e==null) return Collections.EMPTY_LIST;
    return e.getSelectedOrSuperSelectedViews();
}

/**
 * Called to configure BindingsTable.
 */
private void configureBindingsTable(ListCell <String> aCell)
{
    if(aCell.getCol()==0) return;
    String pname = aCell.getItem(); if(pname==null) return;
    View shape = getSelectedView(); if(shape==null) return;
    Binding binding = getSelectedView().getBinding(pname);
    aCell.setText(binding!=null? binding.getKey() : null);
}

/**
 * Returns the name to be used in the inspector's window title.
 */
public String getWindowTitle()  { return "General Inspector"; }

}