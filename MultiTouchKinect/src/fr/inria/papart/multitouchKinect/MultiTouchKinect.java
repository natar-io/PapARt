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

    static public final float trackNearDist = 0.20f;
    static public final int forgetTime = 100;
    PApplet applet;
    Vec3D[] kinectPoints;
    Vec3D[] projPoints;
    boolean[] validPoints;
    byte[] validPoints2;
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
        validPoints2 = new byte[KinectCst.size];
        projPoints = new Vec3D[KinectCst.size];

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

    public void updateKinect(IplImage depthImage, int skip) {
        currentPrecision = skip;
        goodPointOffsets = kinect.updateMT(depthImage, kinectCalibration, validPoints2, projPoints, skip);
    }

    public Vec3D[] getKinectPoints() {
        return kinectPoints;
    }

    public Vec3D[] getProjPoints() {
        return projPoints;
    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no uptades, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw() {
        return find2DTouchRaw(1);
    }

    public ArrayList<TouchPoint> find2DTouchRaw(int skip) {
        assert (skip > 0);

        return Touch.findMultiTouch(goodPointOffsets, kinectPoints, projPoints, validPoints, kinectCalibration, skip);
    }

    public ArrayList<TouchPoint> find2DTouch() {
        return find2DTouch(1);
    }

    public ArrayList<TouchPoint> find2DTouch(int skip) {

        assert (skip > 0);

        ArrayList<TouchPoint> touchPoints = Touch.findMultiTouch(goodPointOffsets, kinectPoints, projPoints, validPoints, kinectCalibration, skip);

        if (touchPoints == null) {
            return null;
        }

        // no previous points add all and return.
        if (touchPoint2D.isEmpty()) {
            for (TouchPoint tp : touchPoints) {
                tp.updateTime = applet.millis();
                touchPoint2D.add(tp);
            }
            return touchPoints;
        }

        // many previous points, try to find correspondances.
        ArrayList<TouchPointTracker> tpt = new ArrayList<TouchPointTracker>();
        for (TouchPoint tpNew : touchPoints) {
            for (TouchPoint tpOld : touchPoint2D) {
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
//                System.out.println("Adding new Touchpoint : " + tp.id);
                tp.updateTime = applet.millis();
                touchPoint2D.add(tp);
                ret.add(tp);
            }
        }

        return ret;
    }

    public void touch2DFound() {
        for (TouchPoint tp : touchPoint2D) {
            tp.setUpdated(false);
        }

        ArrayList<TouchPoint> toDelete = new ArrayList<TouchPoint>();
        for (TouchPoint tpOld : touchPoint2D) {
            if (tpOld.isObselete(applet.millis(), forgetTime)) {
                tpOld.toDelete = true;
                toDelete.add(tpOld);
            }
        }
        // remove too old elements
        if (!toDelete.isEmpty()) {
            for (TouchPoint tp : toDelete) {
                tp.toDelete = true;
                touchPoint2D.remove(tp);
            }
        }

        savePos2D();
    }

    // Does the same, with an external list. 
    public void touch2DFound(ArrayList<TouchPoint> externList) {
        for (TouchPoint tp : touchPoint2D) {
            tp.setUpdated(false);
        }

        ArrayList<TouchPoint> toDelete = new ArrayList<TouchPoint>();
        for (TouchPoint tpOld : touchPoint2D) {
            if (tpOld.isObselete(applet.millis(), forgetTime)) {
                tpOld.toDelete = true;
                toDelete.add(tpOld);
            }
        }
        // remove too old elements
        if (!toDelete.isEmpty()) {
            for (TouchPoint tp : toDelete) {
                tp.toDelete = true;
                touchPoint2D.remove(tp);
                try {
                    externList.remove(tp);
                } catch (NullPointerException e) {
                    // ... nothing
                } catch (Exception e) {
                    System.out.println("Exception in deleting the element " + e);
                }
            }
        }

        savePos2D();
    }

    private void savePos2D() {
        for (TouchPoint tp : touchPoint2D) {
            tp.oldV = tp.v.copy();
        }
    }

    public ArrayList<TouchPoint> find3DTouchRaw(float height3D) {
        return find3DTouchRaw(4, height3D);
    }

    public ArrayList<TouchPoint> find3DTouchRaw(int skip, float height3D) {
        assert (skip > 0);

        return Touch3D.find3D(goodPointOffsets, kinectPoints, projPoints, validPoints, kinectCalibration, skip);
    }

    public ArrayList<TouchPoint> find3DTouch(float height3D) {
        return find3DTouch(4, height3D);
    }

    public ArrayList<TouchPoint> find3DTouch(int skip, float height3D) {
        assert (skip > 0);

        ArrayList<TouchPoint> touchPoints =
                Touch3D.find3D(goodPointOffsets, kinectPoints, projPoints, validPoints, kinectCalibration, skip, height3D);

        if (touchPoints == null) {
            return null;
        }

        // no previous points add all and return.
        if (touchPoint3D.isEmpty()) {
            for (TouchPoint tp : touchPoints) {
                tp.updateTime = applet.millis();
                touchPoint3D.add(tp);
//                tp.filter();
            }
            return touchPoints;
        }

        // many previous points, try to find correspondances.
        ArrayList<TouchPointTracker> tpt = new ArrayList<TouchPointTracker>();
        for (TouchPoint tpNew : touchPoints) {
            for (TouchPoint tpOld : touchPoint3D) {
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
//                System.out.println("Adding new Touchpoint : " + tp.id);
                tp.updateTime = applet.millis();
                touchPoint3D.add(tp);
                ret.add(tp);
//                tp.filter();
            }
        }

        return ret;
    }

    public void touch3DFound() {
        for (TouchPoint tp : touchPoint3D) {
            tp.setUpdated(false);
        }

        ArrayList<TouchPoint> toDelete = new ArrayList<TouchPoint>();
        for (TouchPoint tpOld : touchPoint3D) {
            if (tpOld.isObselete(applet.millis(), forgetTime)) {
                tpOld.toDelete = true;
                toDelete.add(tpOld);
            }
        }
        // remove too old elements
        if (!toDelete.isEmpty()) {
            for (TouchPoint tp : toDelete) {
                tp.toDelete = true;
                touchPoint3D.remove(tp);
            }
        }
    }

    public void touch3DFound(ArrayList<TouchPoint> externList) {
        for (TouchPoint tp : touchPoint3D) {
            tp.setUpdated(false);
        }

        ArrayList<TouchPoint> toDelete = new ArrayList<TouchPoint>();
        for (TouchPoint tpOld : touchPoint3D) {
            if (tpOld.isObselete(applet.millis(), forgetTime)) {
                tpOld.toDelete = true;
                toDelete.add(tpOld);
            }
        }
        // remove too old elements
        if (!toDelete.isEmpty()) {
            for (TouchPoint tp : toDelete) {
                tp.toDelete = true;
                touchPoint3D.remove(tp);
                try {
                    externList.remove(tp);
                } catch (NullPointerException e) {
                    // ... nothing
                } catch (Exception e) {
                    System.out.println("Exception in deleting the element " + e);
                }
            }
        }
        savePos3D();
    }

    private void savePos3D() {
        for (TouchPoint tp : touchPoint3D) {
            tp.oldV = tp.v.copy();
        }
    }
}
