/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitouch.laviole.name;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class MultiTouchKinect {
//TODO:  near dist and forget time ! as public or modifiable

    static final float trackNearDist = 0.20f;
    static final int forgetTime = 100;
    PApplet applet;
    Vec3D[] kinectPoints;
    Vec3D[] projPoints;
    KinectVisu kinectVisu;
    PlaneSelection planeSelection;
    boolean[] backgroundValidPoints;
    boolean[] validPoints;
    int[] depth;
//    float[] depthf;
    Homography homography;
    Matrix4x4 transform;
    int previousTime;
    int backgroundDeletionTime;
    ArrayList<TouchPoint> touchPoint2D = new ArrayList<TouchPoint>();
    ArrayList<TouchPoint> touchPoint3D = new ArrayList<TouchPoint>();

    public MultiTouchKinect(PApplet applet, String planeFile, String planeParametersfilename, String homographyFilename) {

        MyApplet.init(applet);
        KinectVisu.initKinect();
        this.applet = applet;

        backgroundValidPoints = new boolean[MyApplet.w * MyApplet.h];
        validPoints = new boolean[MyApplet.w * MyApplet.h];

        // TODO: memory occupation of Vec3D ? 
        kinectPoints = new Vec3D[MyApplet.w * MyApplet.h];
        projPoints = new Vec3D[MyApplet.w * MyApplet.h];

        for (int k = 0; k < MyApplet.w * MyApplet.h; k++) {
            backgroundValidPoints[k] = true;
        }
//        depthf = new float[MyApplet.w * MyApplet.h];

        planeSelection = new PlaneSelection(planeFile);
        kinectVisu = new KinectVisu(planeParametersfilename);

        // Kinect homography calibration
        try {
            homography = new Homography(homographyFilename);
        } catch (FileNotFoundException e) {
            System.out.println("Homography file " + homographyFilename + " not found : " + e);
            applet.die("Homography file not found");
        } catch (NullPointerException e) {
            applet.die("Null pointer Exception " + e);
        }
        transform = homography.getTransformation();
    }

    public void updateKinect(int[] depth) {
        this.depth = depth;

        if (kinectVisu.isDeletingBackground) {
            if ((applet.millis() - previousTime) > backgroundDeletionTime) {
                kinectVisu.isDeletingBackground = false;
                kinectVisu.isBackgroundDeleted = true;
                applet.println("Background deleted");
            }
        }

        for (int k = 0; k < MyApplet.w * MyApplet.h; k++) {
            projPoints[k] = null;
        }

    }

    public void deleteBackground(int duration) {
        backgroundDeletionTime = duration;
        kinectVisu.isDeletingBackground = true;
        previousTime = applet.millis();
    }

    public Vec3D[] getKinectPoints() {
        return kinectPoints;
    }

    public Vec3D[] getProjPoints() {
        return projPoints;
    }

    public Vec3D findHead() {

        return new Vec3D();
    }

    
    // Raw versions of the algorithm are providing each points at each time. 
    // no uptades, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw() {
        return find2DTouchRaw(1);
    }

    public ArrayList<TouchPoint> find2DTouchRaw(int skip) {
        assert (skip > 0);
        kinectVisu.kinectVisuSkip = skip;
        ArrayList<Integer> imgVec = kinectVisu.view(validPoints, kinectPoints, projPoints,
                depth, planeSelection, planeSelection.planeHeight, true, transform);

        return Touch.findMultiTouch(imgVec, kinectPoints, projPoints, depth, validPoints, planeSelection, transform, skip);
    }

    public ArrayList<TouchPoint> find2DTouch() {
        return find2DTouch(1);
    }

    public ArrayList<TouchPoint> find2DTouch(int skip) {
        assert (skip > 0);
        kinectVisu.kinectVisuSkip = skip;
        ArrayList<Integer> imgVec = kinectVisu.view(validPoints, kinectPoints, projPoints,
                depth, planeSelection, planeSelection.planeHeight, true, transform);

        ArrayList<TouchPoint> touchPoints = Touch.findMultiTouch(imgVec, kinectPoints, projPoints, depth, validPoints, planeSelection, transform, skip);


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
        kinectVisu.kinectVisuSkip = skip;
        ArrayList<Integer> imgVec2 = kinectVisu.view(validPoints, kinectPoints, projPoints,
                depth, planeSelection, height3D,
                false, transform);
        return Touch3D.find3D(imgVec2, kinectPoints, projPoints, depth,
                validPoints, planeSelection.plane, transform, planeSelection, skip, height3D);
    }

    public ArrayList<TouchPoint> find3DTouch(float height3D) {
        return find3DTouch(4, height3D);
    }

    public ArrayList<TouchPoint> find3DTouch(int skip, float height3D) {
        assert (skip > 0);
        kinectVisu.kinectVisuSkip = skip;
        ArrayList<Integer> imgVec2 = kinectVisu.view(validPoints, kinectPoints, projPoints,
                depth, planeSelection, height3D,
                false, transform);

        ArrayList<TouchPoint> touchPoints =
                Touch3D.find3D(imgVec2, kinectPoints, projPoints, depth,
                validPoints, planeSelection.plane, transform, planeSelection, skip, height3D);

        if (touchPoints == null) {
            return null;
        }

        // no previous points add all and return.
        if (touchPoint3D.isEmpty()) {
            for (TouchPoint tp : touchPoints) {
                tp.updateTime = applet.millis();
                touchPoint3D.add(tp);
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
