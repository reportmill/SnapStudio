package studio.apptools;
import snap.geom.Ellipse;
import snap.geom.Polygon;
import snap.gfx.*;
import snap.view.*;

/**
 * A custom class.
 */
public class BoneView extends View {

/**
 * Override to paint joint and bone.
 */
public void paintFront(Painter aPntr)
{
    double w = getWidth(), h = getHeight(), jw = 2;

    aPntr.setColor(Color.RED); aPntr.fill(new Ellipse(0,h/2-jw/2,jw,jw));
    
    Polygon pg = new Polygon();
    pg.addPoint(jw+2, h/2-4); pg.addPoint(w,h/2-2); pg.addPoint(w,h/2+2); pg.addPoint(jw+2,h/2+4);
    aPntr.fill(pg);
}

}