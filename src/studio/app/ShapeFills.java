package studio.app;
import java.util.List;
import snap.gfx.*;
import snap.view.*;

/**
 * This class provides UI for editing the currently selected views stroke, fill, effect, transparency.
 */
public class ShapeFills extends EditorPane.SupportPane {
    
    // The RMFillTool
    FillTool        _fillTool = new FillTool();
    
    // The EffectTool
    EffectTool      _effectTool = new EffectTool();
    
/**
 * Creates a new ShapeFills pane.
 */
public ShapeFills(EditorPane anEP)
{
    super(anEP);
    _fillTool.setEditorPane(anEP);
    _effectTool.setEditorPane(anEP);
}

/**
 * Initialize UI panel.
 */
protected void initUI()
{
    // Get array of known stroke names and initialize StrokeComboBox
    int scount = _fillTool.getStrokeCount();
    Object snames[] = new String[scount]; for(int i=0;i<scount;i++) snames[i] = _fillTool.getStroke(i).getName();
    setViewItems("StrokeComboBox", snames);
    
    // Get array of known fill names and initialize FillComboBox
    int fcount = _fillTool.getFillCount();
    Object fnames[] = new String[fcount]; for(int i=0;i<fcount;i++) fnames[i] = _fillTool.getFill(i).getName();
    setViewItems("FillComboBox", fnames);
    
    // Get array of known effect names and initialize EffectComboBox
    int ecount = _effectTool.getEffectCount();
    Object enames[] = new String[ecount]; for(int i=0;i<ecount;i++) enames[i] = _effectTool.getEffect(i).getName();
    setViewItems("EffectComboBox", enames);
}

/**
 * Reset UI controls from current selection.
 */
public void resetUI()
{
    // Get currently selected view
    View view = getEditor().getSelectedOrSuperSelectedView();
    int tabViewIndex = getViewSelectedIndex("TabView");
    
    // If Stroke tab is showing, ensure proper inspector is showing and forward on
    if(tabViewIndex==0) {
        
        // Get stroke from shape (or default, if not available)
        Border stroke = view.getBorder(); if(stroke==null) stroke = Border.createLineBorder(Color.BLACK,1);

        // Update StrokeCheckBox, StrokeComboBox
        setViewValue("StrokeCheckBox", view.getBorder()!=null);
        setViewValue("StrokeComboBox", stroke.getName());
        
        // Get stroke tool, install tool UI in stroke panel and ResetUI
        FillTool tool = _fillTool.getTool(stroke);
        getView("StrokePane", BorderView.class).setCenter(tool.getUI());
        tool.resetLater();
    }
    
    // If Fill tab is showing, ensure proper inspector is showing and forward on
    else if(tabViewIndex==1) {
        
        // Get fill from view (or default, if not available)
        Paint fill = view.getFill(); if(fill==null) fill = Color.BLACK;

        // Update FillCheckBox, FillComboBox
        setViewValue("FillCheckBox", view.getFill()!=null);
        setViewValue("FillComboBox", fill.getName());
        
        // Get fill tool, install tool UI in fill panel and ResetUI
        FillTool tool = _fillTool.getTool(fill);
        getView("FillPane", BorderView.class).setCenter(tool.getUI());
        tool.resetLater();
    }
    
    // If Effect tab is showing, ensure proper inspector is showing and forward on
    else if(tabViewIndex==2) {
        
        // Get effect from shape (or default, if not available)
        Effect effect = view.getEffect(); if(effect==null) effect = new ShadowEffect();

        // Update EffectCheckBox, EffectComboBox
        setViewValue("EffectCheckBox", view.getEffect()!=null);
        setViewValue("EffectComboBox", effect.getName());
        
        // Get effect tool, install tool UI in effect panel and ResetUI
        EffectTool tool = _effectTool.getTool(effect);
        getView("EffectPane", BorderView.class).setCenter(tool.getUI());
        tool.resetLater();
    }
    
    // Update TransparencySlider, TransparencyText (transparency is opposite of opacity and on 0-100 scale)
    double transp = 100 - view.getOpacity()*100;
    setViewValue("TransparencySlider", transp);
    setViewValue("TransparencyText", transp);
}

/**
 * Updates currently selected shapes from UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current editor and currently selected view list (just return if null)
    Editor editor = getEditor(); if(editor==null) return;
    View view = editor.getSelectedOrSuperSelectedView(); if(view==null) return;
    List <View> views = editor.getSelectedOrSuperSelectedViews();
    int tabViewIndex = getViewSelectedIndex("TabView");
    
    // If Stroke tab is showing, handle basic StrokePane stuff
    if(tabViewIndex==0) {
        
        // Handle StrokeCheckBox: Iterate over shapes and add stroke if not there or remove if there
        if(anEvent.equals("StrokeCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            for(View s : views) {
                if(selected && s.getBorder()==null) s.setBorder(Border.createLineBorder(Color.BLACK,1)); // If requested and missing, add
                if(!selected && s.getBorder()!=null) s.setBorder(null); // If turned off and present, remove
            }
        }
        
        // Handle StrokeComboBox: Get selected stroke instance and iterate over shapes and add stroke if not there
        if(anEvent.equals("StrokeComboBox")) {
            Border newStroke = _fillTool.getStroke(anEvent.getSelectedIndex());
            for(View v : views) v.setBorder(newStroke);
        }
    }

    // If Fill tab is showing, handle basic FillPane stuff
    else if(tabViewIndex==1) {
        
        // Handle FillCheckBox: Iterate over shapes and add fill if not there or remove if there
        if(anEvent.equals("FillCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            for(View s : views) {
                if(selected && s.getFill()==null) s.setFill(Color.PINK); // If requested and missing, add
                if(!selected && s.getFill()!=null) s.setFill(null); // If turned off and present, remove
            }
        }
        
        // Handle FillComboBox: Get selected fill instance and iterate over shapes and add fill if not there
        if(anEvent.equals("FillComboBox")) {
            Paint newFill = _fillTool.getFill(anEvent.getSelectedIndex());
            for(View v : views) v.setFill(newFill);// newFill.deriveFill(s.getFill()));
        }
    }
    
    // If Effect tab is showing, handle basic EffectPane stuff
    else if(tabViewIndex==2) {
        
        // Handle EffectCheckBox: Iterate over shapes and add effect if not there or remove if there
        if(anEvent.equals("EffectCheckBox")) {
            boolean selected = anEvent.getBoolValue();
            for(View s : views) {
                if(selected && s.getEffect()==null) s.setEffect(new ShadowEffect()); // If requested and missing, add
                if(!selected && s.getEffect()!=null) s.setEffect(null); // If turned off and present, remove
            }
        }
        
        // Handle EffectComboBox: Get selected effect instance and iterate over shapes and add effect if not there
        if(anEvent.equals("EffectComboBox")) {
            Effect eff = _effectTool.getEffect(anEvent.getSelectedIndex());
            for(View v : views) v.setEffect(eff);
        }
    }
    
    // Handle Transparency Slider and Text
    if(anEvent.equals("TransparencySlider") || anEvent.equals("TransparencyText")) {
        editor.undoerSetUndoTitle("Transparency Change");
        double value = 1 - anEvent.getFloatValue()/100;
        for(View v : views) v.setOpacity(value);
    }
}

/**
 * Returns the display name for the inspector.
 */
public String getWindowTitle()  { return "Paint/Fill Inspector"; }

}