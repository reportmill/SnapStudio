package studio.app;
import java.io.File;
import snap.util.*;

/**
 * Some utility methods for EditorPane.
 */
public class EditorPaneUtils {

/**
 * Opens the named sample file from the examples package.
 */
public static EditorPane openSample(String aTitle)
{
    // If file is xml resource, get temp file, get XML bytes, write to file, open file and return null
    if(aTitle.endsWith(".xml")) {
        File file = FileUtils.getTempFile(StringUtils.getPathFileName(aTitle));
        byte bytes[] = SnapUtils.getBytes(aTitle);
        SnapUtils.writeBytes(bytes, file);
        FileUtils.openFile(file);
        return null;
    }
    
    // If not url, append Jar:/com/reportmill prefix
    if(!aTitle.startsWith("http:")) aTitle = "Jar:/com/reportmill/examples/" + aTitle + ".rpt";
        
    // Create new editor pane, open document and window, and return editor pane
    EditorPane editorPane = new EditorPane();
    editorPane.open(aTitle);
    editorPane.setWindowVisible(true);
    return editorPane;
}

/**
 * Preview PDF.
 */
public static void previewPDF(EditorPane anEP)  { }

/**
 * Generate report, save as HTML in temp file and open.
 */
public static void previewHTML(EditorPane anEP)  { }

/**
 * Generate report, save as CSV in temp file and open.
 */
public static void previewCSV(EditorPane anEP)  { }

/**
 * Generate report, save as JPG in temp file and open.
 */
public static void previewJPG(EditorPane anEP)  { }

/**
 * Generate report, save as PNG in temp file and open.
 */
public static void previewPNG(EditorPane anEP)  { }

/**
 * Preview XLS.
 */
public static void previewXLS(EditorPane anEP)  { }

/**
 * Preview RTF.
 */
public static void previewRTF(EditorPane anEP)  { }

/**
 * Preview XML.
 */
public static void previewXML(EditorPane anEP)  { }

/**
 * Save document as PDF to given path.
 */
public static void saveAsPDF(EditorPane anEP)  { }

}