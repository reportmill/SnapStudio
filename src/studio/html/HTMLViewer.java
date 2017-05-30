package studio.html;
import snap.view.*;

/**
 * A custom class.
 */
public class HTMLViewer extends ViewOwner {
    
    // The source
    Object       _src;

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
protected View createUI()
{
    HTMLDoc doc = new HTMLDoc(_src);
    return doc;
}

/**
 * Standard main method.
 */
public static void main(String args[])
{
    new HTMLViewer("/Temp/ReportMill!/index2.html").setWindowVisible(true);
}

}