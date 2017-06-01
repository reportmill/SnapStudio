package studio.html;
import java.util.Arrays;

/**
 * HTML Utility methods.
 */
public class HTMLUtils {

    // Empty tags
    static String EMPTY_TAGS[] = { "area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta",
        "param", "source", "track", "wbr"};
    
/**
 * Returns whether tag is empty.
 */
public static boolean isEmptyTag(String aName)
{
    String name = aName.toLowerCase();
    return Arrays.binarySearch(EMPTY_TAGS, name)>=0;
}

}