package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.DepthData;
import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.papart.depthcam.Homography;
import fr.inria.papart.procam.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.depthcam.KinectScreenCalibration;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.Projector;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 * Touch input, using a Kinect device for now.
 *
 * TODO: Refactor all this.
 *
 * @author jeremylaviole
 */
public class TouchInput {

    public static final int NO_TOUCH = -1;
    private int touch2DPrecision, touch3DPrecision;
    private Kinect kinect;

    private PApplet parent;

    private final Semaphore touchPointSemaphore = new Semaphore(1, true);
    private final Semaphore depthDataSem = new Semaphore(1);

// Tracking parameters
    static public final float trackNearDist = 30f;  // in mm
    static public final float trackNearDist3D = 70f;  // in mm
    static public final int forgetTime = 250;       // in ms

    // List of TouchPoints, given to the user
    ArrayList<TouchPoint> touchPoints2D;
    ArrayList<TouchPoint> touchPoints3D;

    private KinectScreenCalibration kinectCalibration;

    public TouchInput(PApplet applet, CameraOpenKinect camera, Kinect kinect,
            String calibration) {
        this.parent = applet;
        this.kinect = kinect;
//        this.pdp = camera.getProjectiveDevice();
        touchPoints2D = new ArrayList<TouchPoint>();
        touchPoints3D = new ArrayList<TouchPoint>();
        this.setCalibration(calibration);
        camera.setTouch(this);
    }

    /**
     * Called by the camera automatically now.
     *
     * @param depthImage
     */
    public void updateTouch(IplImage depthImage) {
        try {
            depthDataSem.acquire();
            if (touch2DPrecision > 0 && touch3DPrecision > 0) {
                kinect.updateMT(depthImage, kinectCalibration, touch2DPrecision, touch3DPrecision);
                findAndTrack2D();
                findAndTrack3D();
            } else {
                if (touch2DPrecision > 0) {
                    kinect.updateMT2D(depthImage, kinectCalibration, touch2DPrecision);
                    findAndTrack2D();
                }
                if (touch3DPrecision > 0) {
                    kinect.updateMT3D(depthImage, kinectCalibration, touch3DPrecision);
                    findAndTrack3D();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(TouchInput.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            depthDataSem.release();
        }
    }

    public TouchList projectTouchToScreen(Screen screen, ARDisplay display) {

        TouchList touchList = new TouchList();

        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        for (TouchPoint tp : touchPoints2D) {
            Touch touch = new Touch();

            try {
                if (useRawDepth) {
                    projectPositionAndSpeedRaw(screen, display, touch, tp);
                } else {
                    projectPositionAndSpeed(screen, display, touch, tp);
                }
            } catch (Exception e) {
                continue;
            }

            touch.is3D = false;
            touch.touchPoint = tp;
            touchList.add(touch);
        }

        for (TouchPoint tp : touchPoints3D) {
            Touch touch = new Touch();
            try {
                if (useRawDepth) {
                    projectPositionAndSpeedRaw(screen, display, touch, tp);
                } else {
                    projectPositionAndSpeed(screen, display, touch, tp);
                }
            } catch (Exception e) {
                continue;
            }
            touch.is3D = true;
            touch.touchPoint = tp;
            touchList.add(touch);
        }

        touchPointSemaphore.release();
        return touchList;
    }

    // TODO: Raw Depth is for Kinect Only, find a cleaner solution.
    private ProjectiveDeviceP pdp;
    private boolean useRawDepth = false;

    public void useRawDepth(Camera camera) {
        this.useRawDepth = true;
        this.pdp = camera.getProjectiveDevice();
    }

    private void projectPositionAndSpeedRaw(Screen screen,
            ARDisplay display,
            Touch touch, TouchPoint tp) throws Exception {

        Vec3D touchPosition = tp.getPositionKinect();

        PVector p = pdp.worldToPixelCoord(touchPosition);

        // Current point 
        PVector paperScreenCoord = project(screen, display,
                p.x / pdp.getWidth(),
                p.y / pdp.getHeight());

        paperScreenCoord.z = tp.getPosition().z;
        touch.position = paperScreenCoord;

        // Speed
        try {
            p = pdp.worldToPixelCoord(tp.getPreviousPositionKinect());
            paperScreenCoord = project(screen, display,
                    p.x / pdp.getWidth(),
                    p.y / pdp.getHeight());

            paperScreenCoord.z = tp.getPreviousPosition().z;
            touch.setPrevPos(paperScreenCoord);
        } catch (Exception e) {
            // Speed is set to 0
            touch.defaultPrevPos();
        }
    }

    private void projectPositionAndSpeed(Screen screen,
            ARDisplay display,
            Touch touch, TouchPoint tp) throws Exception {

        PVector touchPositionNormalized = tp.getPosition();

        // Current point 
        PVector paperScreenCoord = project(screen, display,
                touchPositionNormalized.x,
                touchPositionNormalized.y);

        paperScreenCoord.z = tp.getPosition().z;
        touch.position = paperScreenCoord;
        // Speed
        try {
            float prevX = tp.getPreviousPosition().x;
            float prevY = tp.getPreviousPosition().y;
            paperScreenCoord = project(screen, display,
                    prevX,
                    prevY);
            paperScreenCoord.z = tp.getPreviousPosition().z;
            touch.setPrevPos(paperScreenCoord);
        } catch (Exception e) {
            // Speed is set to 0
            touch.defaultPrevPos();
        }
    }

    private PVector project(Screen screen, ARDisplay display, float x, float y) throws Exception {
        boolean isProjector = display instanceof Projector;
        PVector paperScreenCoord = (isProjector ? (Projector) display : display).projectPointer(screen, x, y);

//        if (TouchDetection.isInside(paperScreenCoord, 0, 1)) {
//            throw new Exception("Outside screen");
//        }
        return paperScreenCoord;
    }

    public ArrayList<Vec3D> projectDepthData(ARDisplay display, Screen screen) {
        try {

            depthDataSem.acquire();
            DepthData depthData = kinect.getDepthData();

            ArrayList<Vec3D> projected = new ArrayList<Vec3D>();
            Vec3D[] projPoints = depthData.projectedPoints;
            boolean isProjector = display instanceof Projector;

            for (int i = 0; i < projPoints.length; i++) {
                Vec3D vec = projPoints[i];
                if (vec == Kinect.INVALID_POINT) {
                    continue;
                }

                try {
                    PVector screenPosition = (isProjector ? (Projector) display : display).projectPointer(screen, vec.x, vec.y);
                    screenPosition.z = vec.z;
                    projected.add(vec);
                } catch (Exception e) {
                }

            }
            depthDataSem.release();
            return projected;
//                    res = (isProjector ? (Projector)display : display ).projectPointer(screen, tp);

        } catch (InterruptedException ex) {
            Logger.getLogger(TouchInput.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public ArrayList<TouchPoint> getTouchPoint2D() {
        return touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoint3D() {
        return touchPoints3D;
    }

    public void getTouch2DColors(IplImage colorImage) {
        getTouchColors(colorImage, this.touchPoints2D);
    }

    public void getTouchColors(IplImage colorImage,
            ArrayList<TouchPoint> touchPointList) {

        if (touchPointList.isEmpty()) {
            return;
        }

        ByteBuffer cBuff = colorImage.getByteBuffer();

//        System.out.println("Searching for point color");
        for (TouchPoint tp : touchPointList) {
            int offset = 3 * kinect.findColorOffset(tp.getPositionKinect());

            tp.setColor((255 & 0xFF) << 24
                    | (cBuff.get(offset + 2) & 0xFF) << 16
                    | (cBuff.get(offset + 1) & 0xFF) << 8
                    | (cBuff.get(offset) & 0xFF));
        }

    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no updates, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw(int skip) {
        assert (skip > 0);
        return TouchDetection.findMultiTouch2D(kinect.getDepthData(), skip);
    }

    public ArrayList<TouchPoint> find3DTouchRaw(int skip) {
        assert (skip > 0);
        return TouchDetection.findMultiTouch3D(kinect.getDepthData(), skip);
    }

    protected void findAndTrack2D() {
        assert (touch2DPrecision != 0);
        ArrayList<TouchPoint> newList = TouchDetection.findMultiTouch2D(kinect.getDepthData(),
                touch2DPrecision);
        trackPoints(touchPoints2D, newList, trackNearDist);
    }

    protected void findAndTrack3D() {
        assert (touch3DPrecision != 0);
        ArrayList<TouchPoint> newList = TouchDetection.findMultiTouch3D(kinect.getDepthData(),
                touch3DPrecision);
        trackPoints(touchPoints3D, newList, trackNearDist3D);
    }

    private void trackPoints(ArrayList<TouchPoint> currentList,
            ArrayList<TouchPoint> newPoints,
            float trackDistance) {
        if (newPoints != null) {
            updatePoints(currentList, newPoints, trackDistance);
            addNewPoints(currentList, newPoints);
        }
        deleteOldPoints(currentList);
    }

    private void updatePoints(ArrayList<TouchPoint> currentList, ArrayList<TouchPoint> newPoints, float trackDistance) {

        // many previous points, try to find correspondances.
        ArrayList<TouchPointTracker> tpt = new ArrayList<TouchPointTracker>();
        for (TouchPoint newPoint : newPoints) {
            for (TouchPoint oldPoint : currentList) {
                tpt.add(new TouchPointTracker(oldPoint, newPoint));
            }
        }

        // update the old touch points with the new informations. 
        // to keep the informations coherent.
        Collections.sort(tpt);
        for (TouchPointTracker tpt1 : tpt) {
            if (tpt1.distance < trackDistance) {
                // new points are marked for deletion after update.
                tpt1.update(parent.millis());
            }
        }
    }

    private void addNewPoints(ArrayList<TouchPoint> currentList, ArrayList<TouchPoint> newPoints) {
        int currentTime = parent.millis();
        // Add the new ones ?
        for (TouchPoint tp : newPoints) {
            if (!tp.isToDelete()) {
                tp.updateTime = currentTime;
                currentList.add(tp);
            }
        }
    }

    private void deleteOldPoints(ArrayList<TouchPoint> currentList) {
        int currentTime = parent.millis();
        // Clear the old ones 
        for (Iterator<TouchPoint> it = currentList.iterator();
                it.hasNext();) {
            TouchPoint tp = it.next();
            tp.setUpdated(false);
            if (tp.isObselete(currentTime, forgetTime)) {
                tp.setToDelete();
                it.remove();
            }
        }
    }

    public void setCalibration(String calibrationFile) {
        try {
            kinectCalibration = new KinectScreenCalibration(this.parent, calibrationFile);
        } catch (FileNotFoundException e) {
            System.out.println("Calibration file error :" + calibrationFile + " \n" + e);
        }
    }

    public KinectScreenCalibration getCalibration() {
        return this.kinectCalibration;
    }

    public void setPrecision(int precision2D, int precision3D) {
        setPrecision2D(precision2D);
        setPrecision3D(precision3D);
    }

    public void setPrecision2D(int precision) {
        this.touch2DPrecision = precision;
    }

    public void setPrecision3D(int precision) {
        this.touch3DPrecision = precision;
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return this.touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return this.touchPoints3D;
    }

    public void lock() {
        try {
            touchPointSemaphore.acquire();
        } catch (Exception e) {
        }
    }

    public void unlock() {
        touchPointSemaphore.release();
    }

}
