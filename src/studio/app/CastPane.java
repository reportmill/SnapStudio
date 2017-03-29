package studio.app;
import snap.gfx.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.WebFile;

/**
 * A pane to add SnapActors to a SnapScene.
 */
public class CastPane extends EditorPane.SupportPane {

    // The UI
    VBox       _ui;
    
    // The shared CastPane
    static CastPane   _shared;
    
    // The currently dragging actor
    static SnapActor  _dragActor;

/**
 * Creates a new CastPane.
 */
public CastPane(EditorPane anEP)  { super(anEP); _shared = this; }

/**
 * Creates a new CastPane.
 */
protected View createUI()
{
    _ui = new VBox(); _ui.setSpacing(6); _ui.setGrowWidth(true); _ui.setPickable(false);
    _ui.setAlign(Pos.CENTER);

    // Create Actors
    SnapActor cat = createActor("Cat");
    SnapActor car = createActor("Car");
    SnapActor dog = createActor("Dog");
    SnapActor duke = createActor("Duke");
    
    // Add nodes
    View nodes[] = { cat, car, dog, duke };
    for(View n : nodes) addItem(_ui, n);

    //Box box2 = new Box(); box2.setPadding(4,4,4,4); box2.setContent(_ui); //box2.setFill(ViewUtils.getBackDarkFill());
    //_ui.setFill(ViewUtils.getBackFill()); _ui.setEffect(new ShadowEffect());
    _ui.setBorder(Border.createCompoundBorder(Border.createLoweredBevelBorder(), Border.createEmptyBorder(8,8,8,12)));
    Box box = new Box(); box.setPadding(3,3,3,3); box.setContent(_ui); box.setFillWidth(true);
    box.setAlign(Pos.TOP_CENTER); enableEvents(box, DragGesture, ViewEvent.Type.DragSourceEnd);
    ScrollView sview = new ScrollView(box); sview.setFill(ViewUtils.getBackFill());
    sview.setGrowWidth(true); sview.setGrowHeight(true);
    return sview;
}

protected SnapActor createActor(String aName)
{
    SnapActor actr = new SnapActor();
    actr.setName(aName); actr.setRealClassName(aName);
    actr.setImage(Image.get(getClass(), "actor.images/" + aName + ".png"));
    actr.setImageName(aName + ".png");
    actr.setPrefSize(120,120);
    return actr;
}

/**
 * Respond to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle DragGesture
    if(anEvent.isDragGesture()) {
        ParentView view = anEvent.getView(ParentView.class); _ui.setPickable(true);
        _dragActor = ViewUtils.getDeepestChildAt(view, anEvent.getX(), anEvent.getY(), SnapActor.class);
        _ui.setPickable(false);
        //while(!(view2.getParent() instanceof ChildView)) view2 = view2.getParent();
        Clipboard dboard = anEvent.getDragboard();
        dboard.setContent("CastPane: " + _dragActor.getRealClassName());
        dboard.setDragImage(_dragActor.getImage());
        dboard.startDrag();
    }
    
    // Handle DragSource
    if(anEvent.isDragSourceEnd())
        _dragActor = null;
}

/**
 * Adds a Gallery item.
 */
public void addItem(VBox aVBox, View aView)
{
    HBox hbox = new HBox(); hbox.setSpacing(3); aView.setLeanX(HPos.CENTER);
    //Label label = new Label(aView.getClass().getSimpleName()); label.setPrefWidth(80);
    hbox.setChildren(aView); //label
    aVBox.addChild(hbox);
}

/**
 * Called to drop an Actor.
 */
public void dropActor(View aView, ViewEvent anEvent)
{
    String name = _dragActor.getName();
    Image img = _dragActor.getImage();
    SnapActor actr = new SnapActor();
    actr.setName(name);
    actr.setImage(img); actr.setImageName(_dragActor.getImageName());
    actr.setRealClassName(_dragActor.getRealClassName());
    actr.setSize(actr.getPrefSize());
    
    ParentView content = getEditor().getContent();
    SnapScene scene = content instanceof SnapScene? (SnapScene)content : null; if(scene==null) { beep(); return; }
    
    Point pnt = scene.parentToLocal(anEvent.getView(), anEvent.getX(), anEvent.getY());
    double w = img.getWidth(), h = img.getHeight();
    double x = Math.round(pnt.getX() - w/2), y = Math.round(pnt.getY() - h/2);
    actr.setBounds(x, y, w, h);
    scene.addActor(actr, x, y);
    getEditor().setSelectedView(actr);
    
    // Adds the actor source and image file
    addActorSource(name);
    addActorImage(name, img);
}

/**
 * Adds an actor for name, image and location.
 */
void addActorSource(String aName)
{
    // Get Site and SceneFile
    WebFile sfile = getEditor().getSourceURL().getFile();
    WebFile sdir = sfile.getParent();
    String actrPath = sdir.getDirPath() + aName + ".java";
    WebFile actrfile = sdir.getSite().getFile(actrPath); if(actrfile!=null) return;
    actrfile = sdir.getSite().createFile(actrPath, false);
    
    // Create ActorFile text
    StringBuffer sb = new StringBuffer();
    sb.append("import snap.viewx.*;\n\n");
    sb.append("/**\n * An Actor implementation. SnapEdit=true.\n */\n");
    sb.append("public class " + aName + " extends SnapActor {\n\n");
    sb.append("/**\n * Creates a new " + aName + " (constructor).\n */\n");
    sb.append("public " + aName + "()\n{\n}\n\n");
    //sb.append("/**\n * Initialize Actor here.\n */\n");
    //sb.append("public void main()\n{\n}\n\n");
    sb.append("/**\n * Update Actor here.\n */\n");
    sb.append("public void act()\n{\n}\n\n}");
    
    // Set text and save file
    actrfile.setText(sb.toString());
    try { actrfile.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Adds an actor for name, image and location.
 */
void addActorImage(String aName, Image anImg)
{
    WebFile sfile = getEditor().getSourceURL().getFile();
    WebFile sdir = sfile.getParent();
    String path = sdir.getDirPath() + "pkg.images/" + aName + '.' + anImg.getType();
    WebFile ifile = sdir.getSite().getFile(path); if(ifile!=null) return;
    ifile = sdir.getSite().createFile(path, false);
    ifile.setBytes(anImg.getBytes());
    try { ifile.save(); }
    catch(Exception e) { throw new RuntimeException(e); }
}

/**
 * Returns whether CastPane is dragging.
 */
public static boolean isDragging()  { return _dragActor!=null; }

/**
 * Returns the current dragging actor.
 */
public static SnapActor getDragActor()  { return _dragActor; }

/**
 * Returns the Shared CastPane.
 */
public static CastPane get()  { return _shared; }

}