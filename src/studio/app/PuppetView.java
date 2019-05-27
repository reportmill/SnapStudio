package studio.app;
import snap.gfx.*;
import snap.util.*;
import snap.view.*;
import studio.app.Puppet.Part;

/**
 * A View to display a puppet.
 */
public class PuppetView extends ParentView {
    
    // The puppet
    Puppet     _puppet;
    
    // The scale
    double     _scale = 1;

/**
 * Creates a PuppetView.
 */
public PuppetView()  { }

/**
 * Creates a PuppetView.
 */
public PuppetView(String aSource, double aScale)
{
    // Set puppet and scale
    Puppet puppet = new ORAPuppet(aSource);
    _scale = aScale;
    
    // Set puppet
    setPuppet(puppet);
}

/**
 * Returns the puppet.
 */
public Puppet getPuppet()  { return _puppet; }

/**
 * Sets the puppet.
 */
protected void setPuppet(Puppet aPuppet)
{
    // Set puppet
    _puppet = aPuppet;
    
    // Add parts/joints
    addAllParts();
    setSize(_puppet.getBounds().getMaxX()*_scale, _puppet.getBounds().getMaxY()*_scale);
}

/**
 * Adds all parts.
 */
void addAllParts()
{
    // Remove children
    removeChildren();
    
    // Iterate over parts
    for(String pname : _puppet.getPartNames()) {
        Part part = _puppet.getPart(pname);
        addImageViewForBodyPart(part);
    }
    
    // Iterate over joints
    for(String jname : _puppet.getJointNames()) {
        Part joint = _puppet.getJoint(jname);
        addImageViewForJoint(joint);
    }
    
    // Make Torso really dense
    getChild(Puppet.Torso).getPhysics().setDensity(1000);
    
    // Resize children
    for(View c : getChildren())
        c.setBounds(c.getX()*_scale, c.getY()*_scale, c.getWidth()*_scale, c.getHeight()*_scale);
}

/**
 * Adds an image shape for given layer.
 */
ImageView addImageViewForPart(Part aPart)
{
    ImageView iview = new ImageView(aPart.getImage()); iview.setName(aPart.name);
    iview.setXY(aPart.x, aPart.y);
    iview.setSize(iview.getPrefSize());
    addChild(iview);
    return iview;
}

/**
 * Adds an image shape for given layer.
 */
ImageView addImageViewForBodyPart(Part aPart)
{
    ImageView iview = addImageViewForPart(aPart);
    iview.getPhysics(true).setGroupIndex(-1);
    return iview;
}

/**
 * Adds an image shape for given layer.
 */
ImageView addImageViewForJoint(Part aPart)
{
    ImageView iview = addImageViewForPart(aPart);
    iview.getPhysics(true).setJoint(true);
    return iview;
}

/**
 * Convert to preview Views.
 */
public void convertToPreview()
{
    View children[] = getChildren();
    double x = getX(), y = getY();
    ChildView par = (ChildView)getParent();
    for(View c : children) {
        par.addChild(c); c.setXY(c.getX() + x, c.getY() + y);
    }
    par.removeChild(this);
}

/**
 * XML Archival.
 */
public XMLElement toXML(XMLArchiver anArchiver)
{
    // Do normal unarchive
    XMLElement xml = super.toXML(anArchiver); xml.setName("PuppetView");
    
    xml.add("Path", _puppet.getSource());
    xml.add("Scale", _scale);
    
    // Return xml
    return xml;
}

/**
 * XML unarchival.
 */
public ParentView fromXML(XMLArchiver anArchiver, XMLElement anElement)
{
    // Do normal version
    ParentView obj = (ParentView)super.fromXML(anArchiver, anElement);
    
    // Unarchive Path, Scale
    String path = anElement.getAttributeValue("Path");
    Puppet puppet = new ORAPuppet(path);
    _scale = anElement.getAttributeDoubleValue("Scale");
    setPuppet(puppet);
    
    // Return
    return obj;
}

}