package studio.app;
import snap.view.*;

/**
 * An inspector to show ViewTree.
 */
public class ViewTree extends EditorPane.SupportPane {
    
    // The ViewTree
    TreeView <View>  _viewTree;

/**
 * Creates a new ViewTree.
 */
public ViewTree(EditorPane anEP)  { super(anEP); }

/**
 * Returns the ViewTree.
 */
protected View createUI()
{
    _viewTree = new TreeView(); _viewTree.setName("ViewTree"); _viewTree.setGrowHeight(true);
    _viewTree.setResolver(new ViewTreeResolver());
    return _viewTree;
}

/**
 * Initialize UI.
 */
protected void initUI()
{
    enableEvents(_viewTree, MouseRelease);
}

/**
 * Reset UI.
 */
protected void resetUI()
{
    _viewTree.setItems(getEditor().getContent());
    _viewTree.expandAll();
    _viewTree.setSelItem(getEditor().getSelectedOrSuperSelectedView());
}

/**
 * Respond UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle ViewTree
    if(anEvent.equals("ViewTree") && anEvent.isActionEvent())
        getEditor().setSelectedView((View)anEvent.getSelItem());
        
    // Handle MouseClick
    if(anEvent.isMouseClick() && anEvent.getClickCount()==2)
        getEditor().setSuperSelectedView((View)anEvent.getSelItem());
}

/**
 * A resolver for Views.
 */
public class ViewTreeResolver extends TreeResolver <View> {
    
    /** Returns the parent of given item. */
    public View getParent(View anItem)  { return anItem!=getEditor().getContent()? anItem.getParent() : null; }

    /** Whether given object is a parent (has children). */
    public boolean isParent(View anItem)
    {
        if(!(anItem instanceof ParentView)) return false;
        if(anItem instanceof Label || anItem instanceof ButtonBase || anItem instanceof Spinner) return false;
        if(anItem instanceof ComboBox || anItem instanceof ListView) return false;
        return ((ParentView)anItem).getChildCount()>0;
    }

    /** Returns the children. */
    public View[] getChildren(View aParent)
    {
        ParentView par = (ParentView)aParent;
        if(par instanceof ScrollView) { ScrollView sp = (ScrollView)par;
            return sp.getContent()!=null? new View[] { sp.getContent() } : new View[0]; }
        return par.getChildren();
    }

    /** Returns the text to be used for given item. */
    public String getText(View anItem)
    {
        String str = anItem.getClass().getSimpleName();
        String name = anItem.getName(); if(name!=null) str += " - " + name;
        String text = anItem.getText(); if(text!=null) str += " \"" + text + "\" ";
        return str;
    }

    /** Return the image to be used for given item. */
    public View getGraphic(View anItem)  { return null; }
}

}