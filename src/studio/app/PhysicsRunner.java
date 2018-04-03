package studio.app;
import java.util.*;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import snap.gfx.*;
import snap.util.MathUtils;
import snap.view.*;
import snap.view.EventListener;

/**
 * A class to run Box2D physics for a view.
 */
public class PhysicsRunner {
    
    // The Snap View
    ParentView     _view;

    // The Box2D World
    World          _world;
    
    // The ratio of screen points to Box2D world meters.
    double         _scale = 720/10d;
    
    // The Runner
    Runnable       _runner;
    
    // Transforms
    Transform      _localToBox;
    
    // Listener to handle drags
    EventListener  _dragFilter = e -> handleDrag(e);
    
    // Drag vars
    View _drag; double _dragX, _dragY;

/**
 * Create new PhysicsRunner.
 */
public PhysicsRunner(ParentView aView)
{
    // Set View
    _view = aView;
    
    // Create world
    _world = new World(new Vec2(0, -9.8f));
    
    // Add bodies for view children
    for(View child : _view.getChildren()) {
        child.getPhysics(true).setDynamic(true);
        createBody(child);
        addDragger(child);
    }
    
    // Add sidewalls
    double vw = _view.getWidth(), vh = _view.getHeight();
    RectView r0 = new RectView(-1, -900, 1, vh+900); r0.getPhysics(true);  // Left
    RectView r1 = new RectView(0, vh+1, vw, 1); r1.getPhysics(true);       // Bottom
    RectView r2 = new RectView(vw, -900, 1, vh+900); r2.getPhysics(true);  // Right
    createBody(r0); createBody(r1); createBody(r2);
}

/**
 * Returns the scale of the world in screen points to Box2D world meters.
 */
public double getScreenPointsToWorldMeters(double aScale)  { return _scale; }

/**
 * Sets the scale of the world in screen points to Box2D world meters.
 * 
 * So if you want your 720 point tall window to be 10m, set scale to be 720/10d (the default).
 */
public void setScreenPointsToWorldMeters(double aScale)  { _scale = aScale; }

/**
 * Returns whether physics is running.
 */
public boolean isRunning()  { return _runner!=null; }

/**
 * Sets whether physics is running.
 */
public void setRunning(boolean aValue)
{
    // If already set, just return
    if(aValue==isRunning()) return;
    
    // Set timer to call timerFired 25 times a second
    if(_runner==null)
        ViewEnv.getEnv().runIntervals(_runner = () -> timerFired(), 40, 0, true, true);
    else {
        ViewEnv.getEnv().stopIntervals(_runner); _runner = null; }
}

/**
 * Called when world timer fires.
 */
void timerFired()
{
    // Update Statics
    for(int i=0,iMax=_view.getChildCount();i<iMax;i++)
        updateBody(_view.getChild(i));
        
    // Update drag
    if(_drag!=null) updateDrag();
    
    // Update world  
    _world.step(.040f,20,20);
    
    // Update Dynamics
    for(int i=0,iMax=_view.getChildCount();i<iMax;i++)
        updateView(_view.getChild(i));
}

/**
 * Updates a view from a body.
 */
public void updateView(View aView)
{
    // Get ViewPhysics and body
    ViewPhysics <Body> phys = aView.getPhysics(); if((phys==null || !phys.isDynamic()) && aView!=_drag) return;
    Body body = phys.getNative();

    // Get/set position
    Vec2 pos = body.getPosition();
    Point posV = boxToView(pos.x, pos.y);
    aView.setXY(posV.x-aView.getWidth()/2, posV.y-aView.getHeight()/2);
    
    // Get set rotation
    float angle = body.getAngle();
    aView.setRotate(-Math.toDegrees(angle));
}

/**
 * Updates a body from a view.
 */
public void updateBody(View aView)
{
    // Get ViewPhysics and body
    ViewPhysics <Body> phys = aView.getPhysics(); if(phys==null || phys.isDynamic() || aView==_drag) return;
    Body body = phys.getNative();

    // Get/set position
    Vec2 pos0 = body.getPosition();
    Vec2 pos1 = viewToBox(aView.getMidX(), aView.getMidY());
    double vx = (pos1.x - pos0.x)*25;
    double vy = (pos1.y - pos0.y)*25;
    body.setLinearVelocity(new Vec2((float)vx, (float)vy));
    
    // Get/set rotation
    double rot0 = body.getAngle();
    double rot1 = Math.toRadians(-aView.getRotate());
    double dr = rot1 - rot0;
    if(dr>Math.PI || dr<-Math.PI) dr = MathUtils.mod(dr + Math.PI, Math.PI*2) - Math.PI;
    body.setAngularVelocity((float)dr*25);
}

/**
 * Updates drag view's body.
 */
void updateDrag()
{
    ViewPhysics <Body> phys = _drag.getPhysics();
    Body body = phys.getNative();
    
    // Get positions
    Vec2 pos0 = body.getPosition();
    Vec2 pos1 = viewToBox(_dragX, _dragY);
    double dx = pos1.x - pos0.x;
    double dy = pos1.y - pos0.y;
    double vx = (pos1.x - pos0.x)*25;
    double vy = (pos1.y - pos0.y)*25;
    body.setLinearVelocity(new Vec2((float)vx, (float)vy));
}

/**
 * Convert View coord to Box2D.
 */
public float viewToBox(double aValue)  { return (float)(aValue/_scale); }

/**
 * Convert View coord to Box2D.
 */
public Vec2 viewToBox(double aX, double aY)  { return getVec(getViewToBox().transform(aX, aY)); }

/**
 * Convert Box2D coord to View.
 */
public double boxToView(double aValue)  { return aValue*_scale; }

/**
 * Convert Box2D coord to View.
 */
public Point boxToView(double aX, double aY)  { return getBoxToView().transform(aX, aY); }

/**
 * Returns transform from View coords to Box coords.
 */
public Transform getViewToBox()
{
    // If already set, just return
    if(_localToBox!=null) return _localToBox;
    
    // Create transform from WorldView bounds to World bounds
    Rect r0 = _view.getBoundsLocal();
    Rect r1 = new Rect(0, 0, r0.width/_scale, -r0.height/_scale);
    double bw = r0.width, bh = r0.height;
    double sx = bw!=0? r1.width/bw : 0, sy = bh!=0? r1.height/bh : 0;
    Transform trans = Transform.getScale(sx, sy);
    trans.translate(r1.x - r0.x, r1.y - r0.y);
    return trans;
}

/**
 * Returns transform from Box coords to View coords.
 */
public Transform getBoxToView()  { return getViewToBox().getInverse(); }
    
/**
 * Returns a body for a view.
 */
public Body createBody(View aView)
{
    // Create BodyDef
    ViewPhysics <Body> phys = aView.getPhysics();
    BodyDef bdef = new BodyDef(); bdef.type = phys.isDynamic()? BodyType.DYNAMIC : BodyType.KINEMATIC;
    bdef.position.set(viewToBox(aView.getMidX(), aView.getMidY()));
    bdef.angle = (float)Math.toRadians(-aView.getRotate());
    
    // Create Body
    Body body = _world.createBody(bdef);
    
    // Create PolygonShape
    Shape vshape = aView.getBoundsShape();
    org.jbox2d.collision.shapes.Shape pshapes[] = createShape(vshape);
    
    // Create FixtureDef
    for(org.jbox2d.collision.shapes.Shape pshp : pshapes) {
        FixtureDef fdef = new FixtureDef(); fdef.shape = pshp; fdef.restitution = .25f; fdef.density = 1;
        body.createFixture(fdef);
    }
    
    // Return body
    phys.setNative(body);
    return body;
}

/**
 * Creates a Box2D shape for given snap shape.
 */
public org.jbox2d.collision.shapes.Shape[] createShape(Shape aShape)
{
    // Handle Rect (simple case)
    if(aShape instanceof Rect) { Rect rect = (Rect)aShape;
        PolygonShape pshape = new PolygonShape();
        float pw = viewToBox(rect.width/2);
        float ph = viewToBox(rect.height/2);
        pshape.setAsBox(pw, ph);
        return new org.jbox2d.collision.shapes.Shape[] { pshape };
    }
    
    // Handle Ellipse
    if(aShape instanceof Ellipse && aShape.getWidth()==aShape.getHeight()) { Ellipse elp = (Ellipse)aShape;
        CircleShape cshape = new CircleShape();
        cshape.setRadius(viewToBox(elp.getWidth()/2));
        return new org.jbox2d.collision.shapes.Shape[] { cshape };
    }
    
    // Handle Arc
    if(aShape instanceof Arc && aShape.getWidth()==aShape.getHeight()) { Arc arc = (Arc)aShape;
        if(arc.getSweepAngle()==360) {
            CircleShape cshape = new CircleShape();
            cshape.setRadius(viewToBox(arc.getWidth()/2));
            return new org.jbox2d.collision.shapes.Shape[] { cshape };
        }
    }
    
    // Handle Polygon if Simple, Convex and less than 8 points
    if(aShape instanceof Polygon) { Polygon poly = (Polygon)aShape;
        org.jbox2d.collision.shapes.Shape pshape = createShape(poly);
        if(pshape!=null) return new org.jbox2d.collision.shapes.Shape[] { pshape };
    }
    
    // Get shape centered around shape midpoint
    Rect bnds = aShape.getBounds();
    Shape shape = aShape.copyFor(new Transform(-bnds.width/2, -bnds.height/2));
    
    // Get PolygonList for shape
    PolygonList polyList = new PolygonList(shape);
    List <org.jbox2d.collision.shapes.Shape> pshapes = new ArrayList();
    
    // Iterate over polygons
    for(int i=0, iMax=polyList.getPolyCount();i<iMax;i++) { Polygon poly = polyList.getPoly(i);
    
        // Try simple case
        org.jbox2d.collision.shapes.Shape pshp = createShape(poly);
        if(pshp!=null) pshapes.add(pshp);
        
        // Otherwise, create Convex Polys and add them
        else {
            PolygonList polys = poly.getConvexPolys(8);
            for(int j=0,jMax=polys.getPolyCount();j<jMax;j++) { Polygon p = polys.getPoly(j);
                pshapes.add(createShape(p)); }
        }
    }
    
    // Return Box2D shapes array
    return pshapes.toArray(new org.jbox2d.collision.shapes.Shape[0]);
}

/**
 * Creates a Box2D shape for given snap shape.
 */
public org.jbox2d.collision.shapes.Shape createShape(Polygon aPoly)
{
    // If invalid, just return null
    if(!aPoly.isSimple() || !aPoly.isConvex() || aPoly.getPointCount()>8) return null;
    
    // Create Box2D PolygonShape and return
    int pc = aPoly.getPointCount();
    Vec2 vecs[] = new Vec2[pc]; for(int i=0;i<pc;i++) vecs[i] = viewToBox(aPoly.getX(i), aPoly.getY(i));
    PolygonShape pshape = new PolygonShape(); pshape.set(vecs, vecs.length);
    return pshape;
}

/**
 * Return Vec2 for snap Point.
 */
Vec2 getVec(Point aPnt)  { return new Vec2((float)aPnt.x, (float)aPnt.y); }

/**
 * Adds DragFilter to view.
 */
void addDragger(View aView)
{
    aView.addEventFilter(_dragFilter, View.MousePress, View.MouseDrag, View.MouseRelease);
}

/**
 * Called when View gets drag event.
 */
void handleDrag(ViewEvent anEvent)
{
    anEvent.consume();
    
    // Get View, ViewPhysics and Body
    View view = anEvent.getView();
    ViewPhysics <Body> phys = view.getPhysics();
    Body body = phys.getNative();
    
    // Get point and set drag x/y
    Point pnt = anEvent.getPoint(anEvent.getView().getParent());
    _dragX = pnt.x; _dragY = pnt.y;
    
    // Handle MousePress
    if(anEvent.isMousePress()) {
        body.setType(BodyType.KINEMATIC);
        body.setAngularVelocity(0);
        _drag = view;
        return;
    }
    
    // Handle MouseRelease
    if(anEvent.isMouseRelease()) {
        body.setType(phys.isDynamic()? BodyType.DYNAMIC : BodyType.KINEMATIC);
        _drag = null;
        return;
    }
}

}