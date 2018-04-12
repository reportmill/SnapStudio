/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.app;
import java.text.DecimalFormat;
import java.util.*;
import snap.gfx.Color;
import snap.util.*;
import snap.view.*;

/**
 * This class provides UI editing for shape animation.
 */
public class Animation extends EditorPane.SupportPane {
    
    // The key frames JList
    ListView <Integer>  _keyFramesList;
    
    // The changes JList
    ListView <String>   _changesList;
    
    // Whether to allow update to time slider/text
    boolean             _update = true;
    
    // The list of key frames
    Integer             _keyFrames[];
    
    // The list of key frames for selected views
    Integer             _viewKeyFrames[];
    
    // The list of changes (keys) for key frame and selected shape
    List <String>       _changes = new Vector();

/**
 * Creates a new Animation pane for EditorPane.
 */
public Animation(EditorPane anEP)  { super(anEP); }

/**
 * Initialize UI for this inspector.
 */
protected void initUI()
{
    // Get KeyFrameList and customize
    _keyFramesList = getView("KeyFrameList", ListView.class);
    _keyFramesList.setCellConfigure(c -> configureKeyFrameList(c));
    
    // Get ChangesList and customize
    _changesList = getView("ChangesList", ListView.class);
    
    // Configure InterpolationComboBox
    updateInterpolatorCombobox();
}

/**
 * Populates the combobox with all the interpolator names if necessary.
 */
public void updateInterpolatorCombobox()
{
    String interps[] = new String[Interpolator.getInterpolatorCount()];
    for(int i=0; i<Interpolator.getInterpolatorCount(); i++) interps[i] = Interpolator.getInterpolator(i).getName();
    setViewItems("InterpolationComboBox", interps);
}

/**
 * Updates the UI panel controls from the current selection.
 */
public void resetUI()
{
    // Get the editor and current animator
    Editor editor = getEditor();
    //Animator animator = getAnimator(false);
    
    // If animator is null, replace with default instance
    /*if(animator==null) {
        animator = new RMAnimator();
        animator.setOwner(editor.getSuperSelectedParentShape());
    }*/
    
    // If animator is running, just return
    //if(animator.isRunning()) return;
    
    // Get the currently selected view and views
    View view = editor.getSelectedOrSuperSelectedView();
    List <View> views = editor.getSelectedOrSuperSelectedViews();
    
    // Get ViewAnim
    int time = getTime();
    ViewAnim animRoot = view.getAnim(-1), animNow = animRoot!=null? getAnim(animRoot, time, false) : null;
    
    // Update TimeText, TimeSlider and TimeSlider Maximum
    setViewValue("TimeText", time); //format(time));
    setViewValue("TimeSlider", time); //Math.round(anim.getTime()*getFrameRate(anim)));
    getView("TimeSlider", Slider.class).setMax(getMaxTime()); //Math.round(anim.getMaxTime()*getFrameRate(anim)));
    
    // Update LoopCheckBox
    setViewValue("LoopCheckBox", animNow!=null && animNow.getLoopCount()>10);
    
    // Update FrameRateText
    setViewValue("FrameRateText", getFrameRate(animRoot));
    
    // Update MaxTimeText
    setViewValue("MaxTimeText", getMaxTime());
    
    // If there wasn't really an animator, just return
    //if(getAnimator(false)==null) return;
    
    // Add this inspector as listener
    //animator.addAnimatorListener(this);
    
    // Get animator key frames
    _keyFrames = getKeyFrameTimes();
    
    // Get selected view key frames
    _viewKeyFrames = animRoot!=null? animRoot.getKeyFrameTimes() : new Integer[0];
    
    // Reset KeyFrameList KeyFrames
    System.out.println("SetItems: " + _keyFrames.length + ", " + _keyFrames);
    _keyFramesList.setItems(_keyFrames);
    System.out.println("SetItems2: " + _keyFrames);

    // Get animator selected frame indices (start and end)
    //int frameStartIndex = _keyFrames.indexOf(animator.getScopeTime());
    //int frameEndIndex = _keyFrames.indexOf(animator.getTime());
    
    // If animator selected frames are adjacent, just select animator time
    //if(frameEndIndex==frameStartIndex+1) frameStartIndex++;
        
    // If KeyFramesList and animator still don't match, reset keyFrameList
    //_keyFramesList.setSelectionInterval(frameStartIndex, frameEndIndex);
    _keyFramesList.setSelItem(time);
    
    // Update FreezeFrameButton
    setViewEnabled("FreezeFrameButton", isFreezableFrame());
    
    // Clear list of changes
    _changes.clear();
    if(animNow!=null)
        for(String key : animNow.getKeys())
            _changes.add(key);
    
    // Get currently selected shape timeline and key frame
    //RMTimeline timeline = shape.getTimeline();
    //RMKeyFrame keyFrame = timeline!=null? timeline.getKeyFrameAt(animator.getTime()) : null;
    // If frame isn't empty, set changes to attributes at time
    //if(keyFrame!=null) for(RMKeyValue kval : keyFrame.getKeyValues()) _changes.add(kval.getKey());
    
    // Update ChangesList Changes
    _changesList.setItems(_changes);
        
    // Get selected change
    String change = _changesList.getSelItem();
    
    // Get key/value for change
    //RMKeyValue keyValue = keyFrame!=null && change!=null? keyFrame.getKeyValue(shape, change) : null;
    
    // Get interpolator for change
    //Interpolator interp = keyValue!=null? keyValue.getInterpolator() : null;
    
    // Update InterpolationComboBox (and enabled status)
    //updateInterpolatorCombobox();
    //setViewEnabled("InterpolationComboBox", keyValue!=null);
    //setViewValue("InterpolationComboBox", interp!=null? interp.getName() : "Linear");

    // Update HelpText - one frame selected
    /*if(frameEndIndex-frameStartIndex>1) {
        String ts = getKeyFrameFormatted(frameStartIndex);
        setViewValue("HelpText", "All changes are made relative to start of selected range (" + ts + ").");
    }*/
    
    // Update HelpText - multiple frames selected
    /*else if(frameStartIndex>0 && frameStartIndex<=getKeyFrameCount()) {
        String ts = getKeyFrameFormatted(frameStartIndex - 1);
        setViewValue("HelpText", "Select multiple key frames to make changes across a range.\n" +
            "All changes are made relative to previous key frame (" + ts + ").");
    }*/
    
    // Update HelpText - all frames selected
    //else setViewValue("HelpText", "Select multiple key frames to make changes across a range.\n" +
    //    "All changes are made relative to previous key frame.");
}

/**
 * Responds to changes from UI controls.
 */
public void respondUI(ViewEvent anEvent)
{
    // Get the current animator (just return if null) - if running, stop it
    //Animator animator = getAnimator(true); if(animator==null) return;
    //if(animator.isRunning()) animator.stop();
    
    // Get Editor, View and Anim
    Editor editor = getEditor();
    View view = editor.getSelectedOrSuperSelectedView();
    ViewAnim anim = view.getAnim(-1);
    int time = getTime();
    
    // Handle TimeSlider or TimeTextField
    if(anEvent.equals("TimeSlider"))
        setTime(anEvent.getIntValue());

    // Handle TimeTextField
    if(anEvent.equals("TimeText"))
        setTimeSeconds(anEvent.getIntValue());

    // Handle PlayButton
    if(anEvent.equals("PlayButton") && anim!=null) {
        //animator.setResetTimeOnStop(true);
        editor.getContent().playAnimDeep();
    }
    
    // Handle StopButton
    if(anEvent.equals("StopButton") && anim!=null)
        editor.getContent().stopAnimDeep();
    
    // Handle StepButton
    if(anEvent.equals("StepButton"))
        setTime(time + getInterval(anim));
    
    // Handle BackButton
    if(anEvent.equals("BackButton"))
        setTime(time - getInterval(anim));
    
    // Handle LoopCheckBox
    if(anEvent.equals("LoopCheckBox"))
        anim.setLoopCount(anEvent.getBoolValue()? 1000 : 0);

    // Handle FrameRateText
    //if(anEvent.equals("FrameRateText")) anim.setFrameRate(anEvent.getFloatValue());

    // Handle MaxTimeText
    //if(anEvent.equals("MaxTimeText")) anim.setMaxTimeSeconds(anEvent.getFloatValue());

    // Handle KeyFrameList
    if(anEvent.equals(_keyFramesList)) {
        Integer ntime = _keyFramesList.getSelItem(); if(ntime==null) return;
        setTime(ntime);
        /*int index = _keyFramesList.getSelIndexMax();
        if(index>=0 && index<getKeyFrameCount()) {
            if(index!=_keyFramesList.getSelIndexMin())
                setTimeForScopedKeyFrame(getKeyFrame(index), getKeyFrame(_keyFramesList.getSelIndexMin()));
            else setTime(getKeyFrame(index)); }*/
    }

    // Handle freezeFrameButton
    //if(anEvent.equals("FreezeFrameButton")) animator.addFreezeFrame();

    // Handle ShiftKeyFramesMenuItem
    /*if(anEvent.equals("ShiftFramesMenuItem")) {
        
        // Run option panel
        String msg = "Shift key frames from time " + time/1000f + " to end by time:";
        DialogBox dbox = new DialogBox("Shift Key Frames"); dbox.setQuestionMessage(msg);
        String shiftString = dbox.showInputDialog(getUI(), "0.0");
        int shift = shiftString==null? 0 : Math.round(StringUtils.floatValue(shiftString)*1000);

        // Shift frames
        if(shift!=0)
            animator.shiftFrames(time, shift);
    }*/

    // Handle ScaleFramesMenuItem
    /*if(anEvent.equals("ScaleFramesMenuItem")) {
        
        // Run option panel
        int maxTime = animator.getMaxTime();
        String msg = "Scale key frames from current frame to new max time";
        DialogBox dbox = new DialogBox("Scale Key Frames"); dbox.setQuestionMessage(msg);
        String newMaxTimeString = dbox.showInputDialog(getUI(), Float.toString(maxTime/1000f));
        int newMaxTime = newMaxTimeString==null? maxTime : Math.round(StringUtils.floatValue(newMaxTimeString)*1000);

        // Scale frames
        if(newMaxTime!=maxTime)
            animator.scaleFrames(animator.getTime(), newMaxTime);
    }*/
    
    // Handle DeleteButton
    if(anEvent.equals("DeleteButton"))
        delete();
    
    // Handle interpolationCombo
    /*if(anEvent.equals("InterpolationComboBox")) {
        View shape = getEditor().getSelectedOrSuperSelectedView();
        RMTimeline timeline = shape.getTimeline();
        String interpName = anEvent.getStringValue();
        String change = (String)getViewSelItem(_changesList);
        RMKeyFrame keyFrame = timeline!=null? timeline.getKeyFrameAt(animator.getTime()) : null;
        RMKeyValue keyValue = keyFrame!=null? keyFrame.getKeyValue(shape, change) : null;
        if(keyValue!=null) {
            Interpolator interp = Interpolator.getInterpolator(interpName);
            keyValue.setInterpolator(interp); // Should derive instead?
        }
    }*/
}

/**
 * Returns the current animator from main editor super selected shape.
 */
/*private RMAnimator getAnimator(boolean create)
{
    RMEditor editor = getEditor();
    return editor!=null? editor.getSuperSelectedShape().getChildAnimator(create) : null;
}*/

/**
 * Returns the number of key frames for the current animator.
 */
private int getKeyFrameCount()  { return _keyFrames==null? 0 : _keyFrames.length; }

/**
 * Returns the float time value of the key frame at the given index.
 */
private Integer getKeyFrame(int anIndex)  { return _keyFrames[anIndex]; }

/**
 * Returns the float time value of the key frame at the given index as a formatted string.
 */
private String getKeyFrameFormatted(int anIndex)  { return format(getKeyFrame(anIndex)); }

/**
 * Returns whether frame is "Freezable" (is intermediate frame with no changes).
 */
public boolean isFreezableFrame()  { return true; }
/*{
    RMAnimator anim = getAnimator(false);
    return anim!=null && !anim.isRunning() && anim.canFreezeFrame();
}*/

/**
 * Returns the current time (in milliseconds).
 */
public int getTime()  { return getEditor().getTime(); }

/**
 * Sets the time of the current animator to the given time.
 */
public void setTime(int aTime)  { getEditor().setTime(aTime); } //{ setTimeForScopedKeyFrame(aTime, null); }

/**
 * Returns the max time.
 */
public int getMaxTime()  { return 5000; }

/**
 * Sets the time of the current animator to the given time.
 */
/*public void setTimeForScopedKeyFrame(int aTime, Integer aScope)
{
    RMAnimator animator = getAnimator(true);

    getEditor().undoerSetUndoTitle("Time Change");

    // Perform time change
    _update = false;
    animator.setScopeTime(aScope);
    animator.setTime(aTime);
    _update = true;
    
    if(aScope==null) {
        setViewSelIndex(_keyFramesList, -1);
        setViewValue(_keyFramesList, animator.getTime());
        setViewSelIndex(_changesList, -1);
    }
}*/

/**
 * Sets the time of the current animator to the given time.
 */
public void setTimeSeconds(float aTime)  { setTime(Math.round(aTime*1000)); }

/**
 * Handles delete of key frame(s) or change(s).
 */
public void delete()  { }

/**
 * Override to customize KeyFramesList rendering.
 */
private void configureKeyFrameList(ListCell <Integer> aCell)
{
    // Get item time formatted and set
    Integer item = aCell.getItem(); if(item==null) return;
    String str = format(item/1000f); aCell.setText(str);
    
    // If not relevant to selected shape make brighter
    if(!aCell.isSelected() && !ArrayUtils.contains(_viewKeyFrames, item))
        aCell.setTextFill(Color.LIGHTGRAY);
}

/** Animator Listener method. */
//public void animatorStarted(RMAnimator anAnimator)  { }
//public void animatorStopped(RMAnimator anAnimator)  { }

/**
 * Animator Listener method : updates time slider and time text when animator has been updated.
 */
/*public void animatorUpdated(RMAnimator anAnimator)
{
    if(_update && anAnimator==getAnimator(true)) {
        setViewValue("TimeSlider", Math.round(anAnimator.getTimeSeconds()*anAnimator.getFrameRate()));
        setViewValue("TimeText", format(anAnimator.getTimeSeconds()));
    }
}*/

/**
 * Returns the KeyFrame times for all anims.
 */
public Integer[] getKeyFrameTimes()
{
    Set <Integer> timesSet = new HashSet(); timesSet.add(0); timesSet.add(getTime());
    getKeyFrameTimes(getEditor().getContent(), timesSet);
    Integer times[] = timesSet.toArray(new Integer[timesSet.size()]);
    Arrays.sort(times);
    return times;
}

/**
 * Gets key frame times for view.
 */
private void getKeyFrameTimes(View aView, Set aSet)
{
    ViewAnim anim = aView.getAnim(-1);
    if(anim!=null && !anim.isEmpty()) {
        Integer times[] = anim.getKeyFrameTimes();
        Collections.addAll(aSet, times);
    }
    if(aView instanceof ParentView) { ParentView par = (ParentView)aView;
        for(View child : par.getChildren())
            getKeyFrameTimes(child, aSet); }
}

/**
 * Returns the anim for given view at given time.
 */
public static ViewAnim getAnim(View aView, int aTime)  { return getAnim(aView.getAnim(0), aTime, true); }

/**
 * Returns the anim for given view at given time.
 */
public static ViewAnim getAnim(ViewAnim theAnim, int aTime, boolean doCreate)
{
    if(theAnim.getEnd()==aTime)
        return theAnim;
    if(theAnim.getEnd()>aTime)
        return null;
    for(ViewAnim child : theAnim.getAnims()) {
        ViewAnim anim = getAnim(child, aTime, false);
        if(anim!=null)
            return anim;
    }
    if(!doCreate)
        return null;
    if(theAnim.getAnims().size()>0) {
        ViewAnim anim = getAnim(theAnim.getAnims().get(0), aTime, true);
        if(anim!=null)
            return anim;
    }
    return theAnim.getAnim(aTime);
}

/**
 * Returns the name for this inspector.
 */
public String getWindowTitle()  { return "Animation"; }

/** Formats a number to 3 decimal places. */
private String format(double aValue)  { return _fmt.format(aValue); }
DecimalFormat _fmt = new DecimalFormat("0.000");

private double getFrameRate(ViewAnim anAnim)  { return 25; }
private int getInterval(ViewAnim anAnim)  { return 40; }

}