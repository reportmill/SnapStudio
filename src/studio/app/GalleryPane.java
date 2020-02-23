package studio.app;
import snap.geom.HPos;
import snap.geom.Point;
import snap.geom.Pos;
import snap.gfx.*;
import snap.util.ClassUtils;
import snap.view.*;

/**
 * A custom class.
 */
public class GalleryPane extends studio.app.EditorPane.SupportPane {

    // The UI
    ColView       _ui;

    // The shared GalleryPane
    static GalleryPane  _shared;
    
    // The currently dragging view
    static View         _dragView;

/**
 * Creates a new GalleryPane.
 */
public GalleryPane(EditorPane anEP)  { super(anEP); _shared = this; }

/**
 * Creates a new GalleryPane.
 */
protected View createUI()
{
    _ui = new ColView(); _ui.setSpacing(6); _ui.setGrowWidth(true); _ui.setPickable(false);
    
    // Create Label, Buttons, Separator
    Label lbl = new Label("Label"); lbl.setPrefWidth(80);
    Button btn = new Button("Button"); btn.setPrefSize(100,20);
    ToggleButton tbtn = new ToggleButton("ToggleButton"); tbtn.setPrefSize(100,20);
    CheckBox cbtn = new CheckBox("CheckBox");
    RadioButton rbtn = new RadioButton("RadioButton");
    Separator sep = new Separator(); sep.setPrefWidth(100);
    
    // Create TextField, Spinner, ComboBox, MenuButton
    TextField tfd = new TextField(); tfd.setPrefWidth(100);
    Spinner spnr = new Spinner(); spnr.setPrefWidth(100);
    ComboBox cbox = new ComboBox(); cbox.setItems("ComboBox"); cbox.setText("ComboBox"); cbox.setPrefSize(100,20);
    MenuButton mbtn = new MenuButton(); mbtn.setText("MenuButton"); mbtn.setPrefSize(100,20);
    
    // Create Slider, Thumbwheel, ProgressBar
    Slider sldr = new Slider(); sldr.setPrefWidth(100);
    ThumbWheel twh = new ThumbWheel(); twh.setPrefSize(100,16); twh.setType(ThumbWheel.TYPE_RADIAL);
    ProgressBar pbar = new ProgressBar(); pbar.setPrefSize(100,20);
    
    // Create ListView, TableView, TreeView, BrowserView
    
    // Create ScrollView, SplitView
    
    // Create TabView, TitleView, TextView
    TabView tab = new TabView(); tab.addTab("One", new Label()); tab.addTab("Two", new Label());tab.setPrefSize(100,40);
    TitleView ttlp = new TitleView("TitleView", new Label()); ttlp.setPrefSize(100,30);
    TextView txtv = new TextView(); txtv.setText("TextView"); txtv.setPrefSize(100,30);
    
    // Create MenuBar, MenuItem, Menu
    
    // Create ImageView, RectView, ShapeView
    
    // Create DocView, PageView
    
    // Create HBox, VBox, BorderView, StackView, SpringView
    RowView hbox = new RowView(); hbox.setBorder(Color.LIGHTGRAY,1); hbox.setPrefSize(100,25);
    ColView vbox = new ColView(); vbox.setBorder(Color.LIGHTGRAY,1); vbox.setPrefSize(100,25);
    
    // Add nodes
    View nodes[] = { lbl, btn, tbtn, cbtn, rbtn, sep, tfd, spnr, cbox, mbtn, sldr, twh, pbar };
    for(View n : nodes) addItem(_ui, n);
    View nodes2[] = { tab, ttlp , txtv, hbox, vbox };
    for(View n : nodes2) addItem(_ui, n);
    //Box box2 = new Box(); box2.setPadding(4,4,4,4); box2.setContent(_ui); //box2.setFill(ViewUtils.getBackDarkFill());
    //_ui.setFill(ViewUtils.getBackFill()); _ui.setEffect(new ShadowEffect());
    _ui.setBorder(Border.createCompoundBorder(Border.createLoweredBevelBorder(), Border.createEmptyBorder(8,8,8,12)));
    BoxView box = new BoxView(); box.setPadding(3,3,3,3); box.setContent(_ui); box.setFillWidth(true);
    box.setAlign(Pos.TOP_CENTER); enableEvents(box, DragGesture, ViewEvent.Type.DragSourceEnd);
    ScrollView sview = new ScrollView(box); sview.setFill(ViewUtils.getBackFill());
    sview.setGrowWidth(true); sview.setGrowHeight(true);
    return sview;
}

/**
 * Respond to UI.
 */
public void respondUI(ViewEvent anEvent)
{
    // Handle DragGesture
    if(anEvent.isDragGesture()) {
        ParentView view = anEvent.getView(ParentView.class); _ui.setPickable(true);
        _dragView = ViewUtils.getDeepestChildAt(view, anEvent.getX(), anEvent.getY()); _ui.setPickable(false);
        while(!(_dragView.getParent() instanceof ChildView))
            _dragView = _dragView.getParent();
        Clipboard cboard = anEvent.getClipboard();
        cboard.addData("GalleryPane: " + _dragView.getClass().getName());
        Image img = ViewUtils.getImage(_dragView); cboard.setDragImage(img);
        cboard.startDrag();
    }
    
    // Handle DragSource
    if(anEvent.isDragSourceEnd())
        _dragView = null;
}

/**
 * Adds a Gallery item.
 */
public void addItem(ColView aVBox, View aView)
{
    RowView hbox = new RowView(); hbox.setSpacing(3); aView.setLeanX(HPos.CENTER);
    Label label = new Label(aView.getClass().getSimpleName()); label.setPrefWidth(80);
    hbox.setChildren(label, aView);
    aVBox.addChild(hbox);
}

/**
 * Drop a View.
 */
public void dropView(View aView, ViewEvent anEvent)
{
    // Get new view to add
    View view = createDragView(aView, anEvent);
    
    // If drop point not in content and view is ChildView, make it content
    Editor editor = getEditor();
    if(!aView.contains(anEvent.getX(), anEvent.getY()) && view instanceof ChildView) {
        ChildView cview = (ChildView)view;
        View content = editor.getContent();
        editor.setContent(cview);
        cview.addChild(content);
    }
    
    // Otherwise, if view is child view, just add
    else if(aView instanceof ChildView) { ChildView cview = (ChildView)aView;
        cview.addChild(view); }
        
    // Select view
    editor.setSelectedView(view);
}

/**
 * Creates a new View for drag.
 */
public View createDragView(View aView, ViewEvent anEvent)
{
    // Create new view from drag view class
    Class cls = _dragView.getClass(); //ClassUtils.getClass(cname);
    View view = (View)ClassUtils.newInstance(cls);
    
    // Set bounds using drag image
    Image img = Clipboard.getDrag().getDragImage();
    Point pnt = aView.parentToLocal(anEvent.getX(), anEvent.getY(), anEvent.getView());
    double w = img.getWidth(), h = img.getHeight();
    double x = Math.round(pnt.getX() - w/2), y = Math.round(pnt.getY() - h/2);
    view.setBounds(x, y, w, h);
    
    // Special cases
    if(view instanceof ButtonBase) view.setText(view.getClass().getSimpleName());
    if(view instanceof RectView) { view.setPrefSize(w,h); view.setFill(Color.PINK); view.setBorder(Color.BLACK,1); }
    if(view instanceof Label) view.setText("Label");
    return view;
}

/**
 * Returns whether GalleryPane is dragging.
 */
public static boolean isDragging()  { return _dragView!=null; }

/**
 * Returns the current dragging view.
 */
public static View getDragView()  { return _dragView; }

/**
 * Returns the Shared GalleryPane.
 */
public static GalleryPane get()  { return _shared; }

}