/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import java.io.FileNotFoundException;
import processing.core.PApplet;
import processing.core.PMatrix2D;
import toxi.geom.Matrix4x4;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class KinectScreenCalibration {

    private Homography homography;
    private PlaneThreshold planeThreshold;
    private Matrix4x4 transform = null;
    private static final int planeOffset = 15;
    
    
    public KinectScreenCalibration() {
    }

    public void setHomography(Homography h) {
        this.homography = h;
    }

    public void setPlaneThreshold(PlaneThreshold p) {
        this.planeThreshold = p;
    }

    public KinectScreenCalibration(PApplet pa, String filename) throws FileNotFoundException {
        String[] lines = pa.loadStrings(filename);

        int homographyOffset = 0;
        if (lines == null) {
            throw new FileNotFoundException(filename);
        }
        transform = new Matrix4x4(Float.parseFloat(lines[homographyOffset + 0]), Float.parseFloat(lines[homographyOffset + 1]), Float.parseFloat(lines[homographyOffset + 2]), Float.parseFloat(lines[homographyOffset + 3]),
                Float.parseFloat(lines[homographyOffset + 4]), Float.parseFloat(lines[homographyOffset + 5]), Float.parseFloat(lines[homographyOffset + 6]), Float.parseFloat(lines[homographyOffset + 7]),
                Float.parseFloat(lines[homographyOffset + 8]), Float.parseFloat(lines[homographyOffset + 9]), Float.parseFloat(lines[homographyOffset + 10]), Float.parseFloat(lines[homographyOffset + 11]),
                Float.parseFloat(lines[homographyOffset + 12]), Float.parseFloat(lines[homographyOffset + 13]), Float.parseFloat(lines[homographyOffset + 14]), Float.parseFloat(lines[homographyOffset + 15]));
        homography = new Homography(transform);

        Vec3D pos = new Vec3D(Float.parseFloat(lines[planeOffset + 0]), Float.parseFloat(lines[planeOffset + 1]), Float.parseFloat(lines[planeOffset + 2]));
        Vec3D norm = new Vec3D(Float.parseFloat(lines[planeOffset + 3]), Float.parseFloat(lines[planeOffset + 4]), Float.parseFloat(lines[planeOffset + 5]));
        float planeHeight = Float.parseFloat(lines[planeOffset + 6]);

        planeThreshold = new PlaneThreshold(pos, norm, planeHeight);
    }

    public PlaneThreshold plane() {
        return this.planeThreshold;
    }

    public Matrix4x4 homography() {
        return this.transform;
    }

    public Vec3D project(Vec3D pt) {
        Vec3D tr = transform.applyTo(planeThreshold.plane.getProjectedPoint(pt));
        tr.x /= tr.z;
        tr.y /= tr.z;
        return tr; 
    }

    public void saveTo(PApplet pa, String filename) {
        String[] lines = new String[16 + 7];
        double[] transformArray = new double[16];
        homography.transform.toArray(transformArray);
        for (int i = 0; i < 16; i++) {
            lines[i] = "" + transformArray[i];
        }

        Plane plane = planeThreshold.plane;
        float height = planeThreshold.planeHeight;

        System.out.println("Saving plane " + plane);
        
//        String[] lines = new String[7];
        lines[planeOffset + 0] = "" + plane.x;
        lines[planeOffset + 1] = "" + plane.y;
        lines[planeOffset + 2] = "" + plane.z;
        lines[planeOffset + 3] = "" + plane.normal.x;
        lines[planeOffset + 4] = "" + plane.normal.y;
        lines[planeOffset + 5] = "" + plane.normal.z;
        lines[planeOffset + 6] = "" + height;

        pa.saveStrings(filename, lines);
    }

    public static void save(PApplet pa, String filename, Homography homography, PlaneThreshold planeThreshold) {
        String[] lines = new String[16 + 7];
        double[] transformArray = new double[16];
        homography.transform.toArray(transformArray);
        for (int i = 0; i < 16; i++) {
            lines[i] = "" + transformArray[i];
        }

        Plane plane = planeThreshold.plane;
        float height = planeThreshold.planeHeight;

//        String[] lines = new String[7];
        lines[planeOffset + 0] = "" + plane.x;
        lines[planeOffset + 1] = "" + plane.y;
        lines[planeOffset + 2] = "" + plane.z;
        lines[planeOffset + 3] = "" + plane.normal.x;
        lines[planeOffset + 4] = "" + plane.normal.y;
        lines[planeOffset + 5] = "" + plane.normal.z;
        lines[planeOffset + 6] = "" + height;

        pa.saveStrings(filename, lines);
    }
}
