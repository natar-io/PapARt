/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import fr.inria.papart.kinect.*;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;
import processing.core.PVector;
import processing.xml.XMLElement;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class MultiTouchKinect {

    static public final float trackNearDist = 30f;  // in mm
    static public final int forgetTime = 250;       // in ms
    PApplet applet;
    Vec3D[] kinectPoints;
    Vec3D[] projPoints;
    boolean[] validPoints, readPoints;
    int[] depth;
    int currentPrecision = 1;
//    float[] depthf;
    ArrayList<TouchPoint> touchPoint2D = new ArrayList<TouchPoint>();
    ArrayList<TouchPoint> touchPoint3D = new ArrayList<TouchPoint>();
    private KinectScreenCalibration kinectCalibration;
    private Kinect kinect;
    private ArrayList<Integer> goodPointOffsets = null;

    public MultiTouchKinect(PApplet applet, Kinect kinect, String configurationFile) {

        this.kinect = kinect;
        KinectCst.init(applet);
        this.applet = applet;

        validPoints = kinect.getValidPoints();
        kinectPoints = kinect.getDepthPoints();

        // Not sure if used in the next versions... 
        projPoints = new Vec3D[KinectCst.size];
        readPoints = new boolean[KinectCst.size];

//        this.pointCloud = new PointCloudKinect(applet, kinect);

        try {
            kinectCalibration = new KinectScreenCalibration(applet, configurationFile);

            System.out.println("Calibration loaded : " + kinectCalibration.plane());
        } catch (FileNotFoundException e) {
            System.out.println("Calibration file error :" + configurationFile + " \n" + e);
        }

    }

    public KinectScreenCalibration getCalibration() {
        return this.kinectCalibration;
    }

    public ArrayList<TouchPoint> getTouchPoint2D() {
        return touchPoint2D;
    }

    public ArrayList<TouchPoint> getTouchPoint3D() {
        return touchPoint3D;
    }

    public void updateKinect(IplImage depthImage, IplImage color, int skip) {
        currentPrecision = skip;
        goodPointOffsets = kinect.updateMT(depthImage, color, kinectCalibration, projPoints, skip);
    }

    public void updateKinect(IplImage depthImage, int skip) {
        currentPrecision = skip;
        goodPointOffsets = kinect.updateMT(depthImage, kinectCalibration, projPoints, skip);
    }

    public void updateKinect3D(IplImage depthImage, int skip) {
        currentPrecision = skip;
        goodPointOffsets = kinect.updateMT3D(depthImage, kinectCalibration, projPoints, skip);
    }

    public Vec3D[] getKinectPoints() {
        return kinectPoints;
    }

    public Vec3D[] getProjPoints() {
        return projPoints;
    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no updates, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw(int skip) {
        assert (skip > 0);

        return Touch.findMultiTouch(goodPointOffsets, kinectPoints, projPoints,
                validPoints, readPoints, kinectCalibration, false, skip);
    }

    public ArrayList<TouchPoint> find3DTouchRaw(int skip) {
        assert (skip > 0);

        return Touch.findMultiTouch(goodPointOffsets, kinectPoints, projPoints,
                validPoints, readPoints, kinectCalibration, true, skip);
    }

    public ArrayList<TouchPoint> findTouch(ArrayList<TouchPoint> touchPointList, boolean is3D, int skip) {

        assert (skip > 0);

        ArrayList<TouchPoint> touchPoints = Touch.findMultiTouch(goodPointOffsets, kinectPoints, projPoints,
                validPoints, readPoints, kinectCalibration, is3D, skip);

        if (touchPoints == null) {
            return null;
        }

        // no previous points add all and return.
        if (touchPointList.isEmpty()) {
            for (TouchPoint tp : touchPoints) {
                tp.updateTime = applet.millis();
                touchPointList.add(tp);
            }
            return touchPointList;
        }

        // many previous points, try to find correspondances.
        ArrayList<TouchPointTracker> tpt = new ArrayList<TouchPointTracker>();
        for (TouchPoint tpNew : touchPoints) {
            for (TouchPoint tpOld : touchPointList) {
                tpt.add(new TouchPointTracker(tpOld, tpNew));
            }
        }

        // update the old touch points with the new informations. 
        // to keep the informations coherent.
        Collections.sort(tpt);

        for (TouchPointTracker tpt1 : tpt) {
            if (tpt1.distance < trackNearDist) {
                tpt1.update(applet.millis());
            }
        }

        ArrayList<TouchPoint> ret = new ArrayList<TouchPoint>();

        for (TouchPoint tp : touchPoints) {
            if (!tp.toDelete) {
                tp.updateTime = applet.millis();
                touchPointList.add(tp);
                ret.add(tp);
            }
        }

        return ret;
    }

    public void touchFound(ArrayList<TouchPoint> touchPointList) {
        for (TouchPoint tp : touchPointList) {
            tp.setUpdated(false);
        }

        ArrayList<TouchPoint> toDelete = new ArrayList<TouchPoint>();
        for (TouchPoint tpOld : touchPointList) {
            if (tpOld.isObselete(applet.millis(), forgetTime)) {
                tpOld.toDelete = true;
                toDelete.add(tpOld);
            }
        }
        // remove too old elements
        if (!toDelete.isEmpty()) {
            for (TouchPoint tp : toDelete) {
                tp.toDelete = true;
                touchPointList.remove(tp);
            }
        }


    }

    public ArrayList<TouchPoint> find2DTouch(int skip) {
        return findTouch(touchPoint2D, false, skip);
    }

    public void touch2DFound() {
        touchFound(touchPoint2D);
    }

    public ArrayList<TouchPoint> find3DTouch(int skip) {
        return findTouch(touchPoint3D, true, skip);
    }

    public void touch3DFound() {
        touchFound(touchPoint3D);
    }
}
