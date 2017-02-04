package studio.app;
import java.util.*;
import snap.gfx.*;
import snap.view.*;

/**
 * Provides a tool for editing RMFills.
 */
public class EffectTool extends EditorPane.SupportPane {

    // Map of tool instances by shape class
    Map            _tools = new Hashtable();
    
    // Tools
    BlurEffectTool    _blurTool;
    EmbossEffectTool  _embsTool;
    ShadowEffectTool  _shadTool;
    ReflectEffectTool _reflTool;
    
    
    // List of known effects
    static Effect  _effects[] = { new ShadowEffect(), new ReflectEffect(), new BlurEffect(),
        new EmbossEffect() };
    
/**
 * Creates a new EffectTool.
 */
public EffectTool()  { super(null); }

/**
 * Returns the number of known effects.
 */
public int getEffectCount()  { return _effects.length; }

/**
 * Returns an individual effect at given index.
 */
public Effect getEffect(int anIndex)  { return _effects[anIndex]; }

/**
 * Returns the currently selected shape's effect.
 */
public Effect getSelectedEffect()
{
    View shape = getEditor().getSelectedOrSuperSelectedView();
    return shape.getEffect();
}

/**
 * Iterate over editor selected shapes and set fill.
 */
public void setSelectedEffect(Effect anEffect)
{
    Editor editor = getEditor();
    for(int i=0, iMax=editor.getSelectedOrSuperSelectedViewCount(); i<iMax; i++) {
        View shape = editor.getSelectedOrSuperSelectedView(i);
        shape.setEffect(anEffect);
    }
}

/**
 * Returns the specific tool for a given shape.
 */
public EffectTool getTool(Object anObj)
{
    if(_blurTool==null) {
        _blurTool = new BlurEffectTool(); _blurTool.setEditorPane(getEditorPane());
        _embsTool = new EmbossEffectTool(); _embsTool.setEditorPane(getEditorPane());
        _reflTool=new ReflectEffectTool(); _reflTool.setEditorPane(getEditorPane());
        _shadTool = new ShadowEffectTool(); _shadTool.setEditorPane(getEditorPane());
    }
    
    // Get tool from tools map - just return if present
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
    if(cls==BlurEffect.class) return _blurTool;
    if(cls==EmbossEffect.class) return _embsTool;
    if(cls==ReflectEffect.class) return _reflTool;
    if(cls==ShadowEffect.class) return _shadTool;
    return this;
}

}