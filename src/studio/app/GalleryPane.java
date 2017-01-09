package studio.app;
import snap.gfx.*;
import snap.view.*;

/**
 * A custom class.
 */
public class GalleryPane extends ViewOwner {

    // The UI
    VBox       _ui;

/**
 * Creates a new GalleryPane.
 */
protected View createUI()
{
    _ui = new VBox(); _ui.setSpacing(6); _ui.setGrowWidth(true); _ui.setPickable(false);
    
    // Rect, RoundRect, Ellipse
    RectView rview = new RectView(0,0,80,20); rview.setFill(Color.PINK); rview.setBorder(Color.BLACK,1);
    ShapeView rrview = new ShapeView(new RoundRect(0,0,80,20,4)); rrview.setFill(Color.PINK);
    rrview.setBorder(Color.BLACK,1);
    ShapeView eview = new ShapeView(new Ellipse(0,0,80,20)); eview.setFill(Color.PINK); eview.setBorder(Color.BLACK,1);
    
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
    
    // Create HBox, VBox, BorderView, StackView, SpringView
    
    // Create ListView, TableView, TreeView, BrowserView
    
    // Create ScrollView, SplitView
    
    // Create TabView, TitleView, TextView
    TabView tab = new TabView(); tab.addTab("One", new Label()); tab.addTab("Two", new Label());tab.setPrefSize(100,40);
    TitleView ttlp = new TitleView(); ttlp.setTitle("TitleView"); ttlp.setContent(new Label());ttlp.setPrefSize(100,30);
    TextView txtv = new TextView(); txtv.setText("TextView"); txtv.setPrefSize(100,30);
    
    // Create MenuBar, MenuItem, Menu
    
    // Create ImageView, RectView, ShapeView
    
    // Create DocView, PageView
    
    // Add nodes
    View nodes[] = { rview, rrview, eview, lbl, btn, tbtn, cbtn, rbtn, sep, tfd, spnr, cbox, mbtn, sldr, twh, pbar };
    for(View n : nodes) addItem(_ui, n);
    View nodes2[] = { tab, ttlp , txtv };
    for(View n : nodes2) addItem(_ui, n);
    //Box box2 = new Box(); box2.setPadding(4,4,4,4); box2.setContent(_ui); //box2.setFill(ViewUtils.getBackDarkFill());
    //_ui.setFill(ViewUtils.getBackFill()); _ui.setEffect(new ShadowEffect());
    _ui.setBorder(Border.createCompoundBorder(Border.createLoweredBevelBorder(), Border.createEmptyBorder(8,8,8,12)));
    Box box = new Box(); box.setPadding(3,3,3,3); box.setContent(_ui); box.setFillWidth(true);
    box.setAlign(Pos.TOP_CENTER); enableEvents(box, DragGesture);
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
        View view2 = ViewUtils.getDeepestChildAt(view, anEvent.getX(), anEvent.getY()); _ui.setPickable(false);
        while(!(view2.getParent() instanceof ChildView))
            view2 = view2.getParent();
        Clipboard dboard = anEvent.getDragboard();
        dboard.setContent("GalleryPane: " + view2.getClass().getName());
        Image img = ViewUtils.getImage(view2); dboard.setDragImage(img);
        dboard.startDrag();
    }
}

/**
 * Adds a Gallery item.
 */
public void addItem(VBox aVBox, View aView)
{
    HBox hbox = new HBox(); hbox.setSpacing(3); aView.setLeanX(HPos.CENTER);
    Label label = new Label(aView.getClass().getSimpleName()); label.setPrefWidth(80);
    hbox.setChildren(label, aView);
    aVBox.addChild(hbox);
}

}