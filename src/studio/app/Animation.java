/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package studio.app;
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
    ListView            _changesList;
    
    // Whether to allow update to time slider/text
    boolean             _update = true;
    
    // The list of key frames
    List <Integer>      _keyFrames;
    
    // The list of key frames for selected shapes
    List <Integer>      _selectedShapesKeyFrames;
    
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
    _keyFramesList.setCellConfigure(this :: configureKeyFrameList);
    
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
    View shape = editor.getSelectedOrSuperSelectedView();
    List <View> shapes = editor.getSelectedOrSuperSelectedViews();
    
    // Get ViewAnim
    ViewAnim anim = shape.getAnim(0);
    
    // Update TimeText, TimeSlider and TimeSlider Maximum
    setViewValue("TimeText", format(anim.getTime()));
    setViewValue("TimeSlider", Math.round(anim.getTime()*getFrameRate(anim)));
    getView("TimeSlider", Slider.class).setMax(Math.round(anim.getMaxTime()*getFrameRate(anim)));
    
    // Update LoopCheckBox
    setViewValue("LoopCheckBox", anim.getLoopCount()>10);
    
    // Update FrameRateText
    setViewValue("FrameRateText", getFrameRate(anim));
    
    // Update MaxTimeText
    setViewValue("MaxTimeText", anim.getMaxTime());
    
    // If there wasn't really an animator, just return
    //if(getAnimator(false)==null) return;
    
    // Add this inspector as listener
    //animator.addAnimatorListener(this);
    
    // Get animator key frames
    //_keyFrames = animator.getKeyFrameTimes();
    
    // Get selected shapes key frames
    //_selectedShapesKeyFrames = shape.isRoot()? null : animator.getKeyFrameTimes(shapes, true);
    
    // Reset KeyFrameList KeyFrames
    setViewItems(_keyFramesList, _keyFrames);

    // Get animator selected frame indices (start and end)
    //int frameStartIndex = _keyFrames.indexOf(animator.getScopeTime());
    //int frameEndIndex = _keyFrames.indexOf(animator.getTime());
    
    // If animator selected frames are adjacent, just select animator time
    //if(frameEndIndex==frameStartIndex+1) frameStartIndex++;
        
    // If KeyFramesList and animator still don't match, reset keyFrameList
    //_keyFramesList.setSelectionInterval(frameStartIndex, frameEndIndex);
    
    // Clear list of changes
    _changes.clear();
    
    // Get currently selected shape timeline and key frame
    //RMTimeline timeline = shape.getTimeline();
    //RMKeyFrame keyFrame = timeline!=null? timeline.getKeyFrameAt(animator.getTime()) : null;
    
    // If frame isn't empty, set changes to attributes at time
    //if(keyFrame!=null)
    //    for(RMKeyValue kval : keyFrame.getKeyValues())
    //        _changes.add(kval.getKey());
    
    // Update ChangesList Changes
    setViewItems(_changesList, _changes);
        
    // Get selected change
    String change = getViewStringValue(_changesList);
    
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
    ViewAnim anim = view.getAnim(0);
    
    // Handle TimeSlider or TimeTextField
    if(anEvent.equals("TimeSlider"))
        setTime(anEvent.getIntValue());

    // Handle TimeTextField
    if(anEvent.equals("TimeText"))
        setTimeSeconds(anEvent.getIntValue());

    // Handle PlayButton
    if(anEvent.equals("PlayButton")) {
        //animator.setResetTimeOnStop(true);
        anim.play();
    }
    
    // Handle StopButton
    if(anEvent.equals("StopButton"))
        anim.stop();
    
    // Handle StepButton
    if(anEvent.equals("StepButton"))
        setTime(anim.getTime() + getInterval(anim));
    
    // Handle BackButton
    if(anEvent.equals("BackButton"))
        setTime(anim.getTime() - getInterval(anim));
    
    // Handle LoopCheckBox
    if(anEvent.equals("LoopCheckBox"))
        anim.setLoopCount(anEvent.getBoolValue()? 1000 : 0);

    // Handle FrameRateText
    //if(anEvent.equals("FrameRateText")) anim.setFrameRate(anEvent.getFloatValue());

    // Handle MaxTimeText
    //if(anEvent.equals("MaxTimeText")) anim.setMaxTimeSeconds(anEvent.getFloatValue());

    // Handle KeyFrameList
    if(anEvent.getView()==_keyFramesList) {
        /*int index = _keyFramesList.getSelectedIndexMax();
        if(index>=0 && index<getKeyFrameCount()) {
            if(index!=_keyFramesList.getSelectedIndexMin())
                setTimeForScopedKeyFrame(getKeyFrame(index), getKeyFrame(_keyFramesList.getSelectedIndexMin()));
            else setTime(getKeyFrame(index));
        }*/
    }

    // Handle freezeFrameButton
    //if(anEvent.equals("FreezeFrameButton")) animator.addFreezeFrame();

    // Handle ShiftKeyFramesMenuItem
    /*if(anEvent.equals("ShiftFramesMenuItem")) {
        
        // Run option panel
        int time = animator.getTime();
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
        String change = (String)getViewSelectedItem(_changesList);
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
private int getKeyFrameCount()  { return _keyFrames==null? 0 : _keyFrames.size(); }

/**
 * Returns the float time value of the key frame at the given index.
 */
private Integer getKeyFrame(int anIndex)  { return anIndex>=0? _keyFrames.get(anIndex) : null; }

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
 * Sets the time of the current animator to the given time.
 */
public void setTime(int aTime)  //{ setTimeForScopedKeyFrame(aTime, null); }
{
    Editor editor = getEditor();
    View view = editor.getSelectedOrSuperSelectedView();
    ViewAnim anim = view.getAnim(0);
    anim.setTime(aTime);
}

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
        setViewSelectedIndex(_keyFramesList, -1);
        setViewValue(_keyFramesList, animator.getTime());
        setViewSelectedIndex(_changesList, -1);
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
    if(!aCell.isSelected() && !ListUtils.contains(_selectedShapesKeyFrames, item))
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
 * Returns the name for this inspector.
 */
public String getWindowTitle()  { return "Animation"; }

/** Formats a number to 3 decimal places. */
private String format(double aValue)  { return String.format("%.3f", aValue); }

private double getFrameRate(ViewAnim anAnim)  { return 25; }
private int getInterval(ViewAnim anAnim)  { return 40; }

}