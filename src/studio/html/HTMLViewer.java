package studio.html;
import snap.view.*;

/**
 * A custom class.
 */
public class HTMLViewer extends ViewOwner {
    
    // The source
    Object       _src;
    
    // The document
    HTMLDoc      _doc;

/**
 * Creates a new HTMLViewer.
 */
public HTMLViewer(Object aSource)
{
    _src = aSource;
}

/**
 * Creates the UI.
 */
protected View createUI()  { return getDoc(); }

/**
 * Returns the document.
 */
public HTMLDoc getDoc()
{
    if(_doc!=null || _src==null) return _doc;
    return _doc = new HTMLDoc(_src);
}

/**
 * Standard main method.
 */
public static void main(String args[])
{
    HTMLViewer viewer = new HTMLViewer("/Temp/ReportMill!/index2.html");
    viewer.getWindow().setTitle(viewer.getDoc().getTitle());
    viewer.setWindowVisible(true);
}

}