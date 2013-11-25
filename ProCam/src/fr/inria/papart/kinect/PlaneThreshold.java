/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import java.net.NoRouteToHostException;
import toxi.geom.Plane;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.math.MathUtils;

/**
 *
 * @author jeremy
 */
public class PlaneThreshold {

    protected float planeHeight = 25.00f;
    int currentPoint;
    protected Plane plane;
    Plane bigPlane;
    Plane planeOver;
    protected Vec3D[] points;

    public PlaneThreshold(String fileName) {
        loadPlane(fileName);
    }

    public PlaneThreshold() {
        init();
    }

    public PlaneThreshold(Vec3D pos, Vec3D normal, float height) {
        plane = new Plane(pos, normal);
        this.planeHeight = height;
    }

//    public PlaneThreshold(XMLElement planeXML) {
//
//        XMLElement origin = planeXML.getChild("Origin");
//        XMLElement normal = planeXML.getChild("Normal");
//        XMLElement height = planeXML.getChild("Height");
//
//        XMLElement[] origins = origin.getChildren();
//        Vec3D pos = new Vec3D(
//                Float.parseFloat(origins[0].getContent()),
//                Float.parseFloat(origins[1].getContent()),
//                Float.parseFloat(origins[2].getContent()));
//        XMLElement[] norms = normal.getChildren();
//        Vec3D norm = new Vec3D(
//                Float.parseFloat(norms[0].getContent()),
//                Float.parseFloat(norms[1].getContent()),
//                Float.parseFloat(norms[2].getContent()));
//        planeHeight = Float.parseFloat(height.getContent());
//
//        plane = new Plane(pos, norm);
//        Kinect.pa.println("Plane successfully loaded");
//        setValid(true);
//    }
    private void init() {
        currentPoint = 0;
        points = new Vec3D[3];
        plane = null;
    }
    
    public void addPoint(Vec3D point) {
        if (currentPoint == 3) {
            Kinect.CURRENTPAPPLET.println("Enough points are selected, calculate the plane");
            return;
        }
        points[currentPoint++] = point;
    }

    public boolean computePlane() {
        
        if(points[0] == null || points[1] == null || points[2] == null)
            return false;
        
        Triangle3D tri = new Triangle3D(points[0],
                points[1],
                points[2]);
        plane = new Plane(tri);

        // TODO: check that !
        Vec3D bigPlanePos = new Vec3D(plane.x * 100, plane.y * 100, plane.z * 100);
        bigPlane = new Plane(bigPlanePos, plane.normal);
        return true;
    }

    public void setHeight(float h){
        this.planeHeight = h;
    }
    
    public float getHeight(){
        return this.planeHeight;
    }
    
    public Plane getPlane(){
        return this.plane;
    }
    
    public Plane computePlaneOver(float distance) {
        planeOver = new Plane(plane, plane.normal);
        planeOver.x += distance * plane.normal.x;
        planeOver.y += distance * plane.normal.y;
        planeOver.z += distance * plane.normal.z;
        return planeOver;
    }

    public boolean orientation(Vec3D point, float value) {
        return plane.classifyPoint(point, 0.05f) == Plane.Classifier.BACK;
    }

    public boolean orientation(Vec3D p) {
        float d = plane.sub(p).dot(plane.normal);
        if (d < -MathUtils.EPS) {
            return false;
        } else if (d > MathUtils.EPS) {
            return true;
        }
        return true; //ON_PLANE;
    }
    
    public boolean hasGoodOrientationAndDistance(Vec3D point) {
        return orientation(point) && plane.getDistanceToPoint(point) <= planeHeight;
    }
 
    public boolean hasGoodDistance(Vec3D point) {
        return plane.getDistanceToPoint(point) <= planeHeight;
    }

    public boolean hasGoodOrientation(Vec3D point) {
        return orientation(point);
    }

    public float distanceTo(Vec3D point) {
        return plane.getDistanceToPoint(point);
    }

    public void flipNormal() {
        plane.normal = plane.normal.invert();
    }

    public void moveUpDown(float value) {
        plane.x = plane.x + value * plane.normal.x;
        plane.y = plane.y + value * plane.normal.y;
        plane.z = plane.z + value * plane.normal.z;
    }

    public void savePlane(String filename) {
        String[] lines = new String[7];
        lines[0] = "" + plane.x;
        lines[1] = "" + plane.y;
        lines[2] = "" + plane.z;
        lines[3] = "" + plane.normal.x;
        lines[4] = "" + plane.normal.y;
        lines[5] = "" + plane.normal.z;
        lines[6] = "" + planeHeight;
        Kinect.CURRENTPAPPLET.saveStrings(filename, lines);
        Kinect.CURRENTPAPPLET.println("Plane successfully saved");
    }
    
    @Override
    public String toString(){
        return "Plane " + plane + " height " + planeHeight;
    }

    public void loadPlane(String fileName) {
        String[] lines = Kinect.CURRENTPAPPLET.loadStrings(fileName);
        Vec3D pos = new Vec3D(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]));
        Vec3D norm = new Vec3D(Float.parseFloat(lines[3]), Float.parseFloat(lines[4]), Float.parseFloat(lines[5]));
        planeHeight = Float.parseFloat(lines[6]);

        plane = new Plane(pos, norm);
        Kinect.CURRENTPAPPLET.println("Plane " + fileName + " successfully loaded");
    }
    static int nbPlanes = 0;

    public static Plane mergePlane(Plane p1, Plane p2) {
        nbPlanes++;
        Plane res = new Plane();
        res.x = p1.x + p2.x;
        res.y = p1.y + p2.y;
        res.z = p1.z + p2.z;
        res.normal = p1.normal.add(p2.normal);
        return res;
    }
}
