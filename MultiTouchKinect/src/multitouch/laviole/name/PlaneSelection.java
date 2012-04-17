/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitouch.laviole.name;

import toxi.geom.Plane;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;
import toxi.math.MathUtils;

/**
 *
 * @author jeremy
 */
public class PlaneSelection {

    float planeHeight = 1.00f;
    public static float planeSpeed = 0.01f;
    int currentPoint;
    Plane plane;
    Plane bigPlane;
    Plane planeOver;
    Vec3D[] points;
    String filename = "../data/plane.txt";
    boolean valid = false;

    public PlaneSelection(String filename) {
        this.filename = filename;
        init();
        loadPlane();
    }


    public PlaneSelection() {
        init();
    }

    private void init() {
        currentPoint = 0;
        points = new Vec3D[3];
        plane = null;
        planeSpeed = 0.01f;
        setValid(false);
    }

    void addPoint(Vec3D point) {
        if (currentPoint == 3) {
            MyApplet.pa.println("Enough points are selected, calculate the plane");
            return;
        }
        points[currentPoint++] = point;
    }

    boolean computePlane() {
        Triangle3D tri = new Triangle3D(points[0],
                points[1],
                points[2]);
        plane = new Plane(tri);
        Vec3D bigPlanePos = new Vec3D(plane.x * 100, plane.y * 100, plane.z * 100);
        bigPlane = new Plane(bigPlanePos, plane.normal);
        setValid(true);
        return true;
    }

    Plane computePlaneOver(float distance) {
        planeOver = new Plane(plane, plane.normal);
        planeOver.x += distance * plane.normal.x;
        planeOver.y += distance * plane.normal.y;
        planeOver.z += distance * plane.normal.z;
        return planeOver;
    }

    boolean orientation(Vec3D point, float value) {
        return plane.classifyPoint(point, 0.05f) == Plane.Classifier.BACK;
    }

    boolean orientation(Vec3D p) {
        float d = plane.sub(p).dot(plane.normal);
        if (d < -MathUtils.EPS) {
            return false;
        } else if (d > MathUtils.EPS) {
            return true;
        }
        return true; //ON_PLANE;
    }

    float distanceTo(Vec3D point) {
        return plane.getDistanceToPoint(point);
    }

    void moveUpDown(float value) {
        plane.x = plane.x + value * plane.normal.x;
        plane.y = plane.y + value * plane.normal.y;
        plane.z = plane.z + value * plane.normal.z;
    }

    void savePlane() {
        String[] lines = new String[8];
        lines[0] = "" + plane.x;
        lines[1] = "" + plane.y;
        lines[2] = "" + plane.z;
        lines[3] = "" + plane.normal.x;
        lines[4] = "" + plane.normal.y;
        lines[5] = "" + plane.normal.z;
        lines[6] = "" + planeHeight;
        lines[7] = "" + planeSpeed;
        MyApplet.pa.saveStrings(filename, lines);
        MyApplet.pa.println("Plane successfully saved");
    }

    private void loadPlane() {
        String[] lines = MyApplet.pa.loadStrings(filename);
        Vec3D pos = new Vec3D(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]));
        Vec3D norm = new Vec3D(Float.parseFloat(lines[3]), Float.parseFloat(lines[4]), Float.parseFloat(lines[5]));
        planeHeight = 0.05f * Float.parseFloat(lines[6]);
        planeSpeed = Float.parseFloat(lines[7]);

        plane = new Plane(pos, norm);
        MyApplet.pa.println("Plane successfully loaded");
        setValid(true);
    }

    void setValid(boolean valid) {
        this.valid = valid;
    }

    boolean isValid() {
        return valid;
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
