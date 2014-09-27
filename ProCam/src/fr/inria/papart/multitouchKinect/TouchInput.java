package fr.inria.papart.multitouchKinect;

import fr.inria.papart.kinect.DepthData;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.papart.kinect.Homography;
import fr.inria.papart.procam.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.kinect.Kinect;
import fr.inria.papart.kinect.KinectScreenCalibration;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.Projector;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
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

    private int touch2DPrecision, touch3DPrecision;
    private Kinect kinect;
    private GrabberThread grabberThread = null;
    private static final int MAX_AVAILABLE = 1;
    private final Semaphore touchPointSemaphore = new Semaphore(MAX_AVAILABLE, true);
    private ProjectiveDeviceP pdp;
    private boolean useExternalGrabber = false;

    private final Semaphore depthDataSem = new Semaphore(1);
    /**
     * * From Multitouch kinect **
     */
    static public final float trackNearDist = 30f;  // in mm
    static public final float trackNearDist3D = 70f;  // in mm
    static public final int forgetTime = 250;       // in ms
    PApplet papplet;

    int currentPrecision = 1;
//    float[] depthf;

    // List of TouchPoints, given to the user
    ArrayList<TouchPoint> touchPoint2D = new ArrayList<TouchPoint>();
    ArrayList<TouchPoint> touchPoint3D = new ArrayList<TouchPoint>();

    private KinectScreenCalibration kinectCalibration;

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect) {
        this(applet, calibrationFile, kinect, null, 1, 4);
    }

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, int precision2D, int precision3D) {
        this(applet, calibrationFile, kinect, null, precision2D, precision3D);
    }

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, OpenKinectFrameGrabber grabber, int precision2D, int precision3D) {
        this(applet, calibrationFile, kinect, grabber, false, precision2D, precision3D);
    }

    // called by PapARt (with another camera for tracking) 
    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, OpenKinectFrameGrabber grabber, boolean color, int precision2D, int precision3D) {
        this(applet, calibrationFile, kinect, grabber, color, true, precision2D, precision3D);
    }

    // called by PapARt (Kinect Only)! 
    public TouchInput(PApplet applet, String calibrationFile, Camera kinectCamera, Kinect kinect, boolean color, int precision2D, int precision3D) {
        if (!kinectCamera.useKinect()) {
            System.err.println("Impossible to init a Touch Input without a  Kinect camera. ");

        }
        this.papplet = applet;

        try {
            kinectCalibration = new KinectScreenCalibration(applet, calibrationFile);
            System.out.println("Calibration loaded : " + kinectCalibration.plane());
        } catch (FileNotFoundException e) {
            System.out.println("Calibration file error :" + calibrationFile + " \n" + e);
        }

        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;
        this.kinect = kinect;
        kinectCamera.setTouch(this);
        this.useExternalGrabber = true;
        this.pdp = kinectCamera.getProjectiveDevice();
        // TODO: récup le Grabber, et récup le grabber de la profondeur. 
    }

    // called by PapARt (with another camera for Tracking)  (color & undist = true)
    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, OpenKinectFrameGrabber grabber, boolean color, boolean colorUndist, int precision2D, int precision3D) {

        this.kinect = kinect;
        this.papplet = applet;

        try {
            kinectCalibration = new KinectScreenCalibration(applet, calibrationFile);
        } catch (FileNotFoundException e) {
            System.out.println("Calibration file error :" + calibrationFile + " \n" + e);
        }

        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;

        if (grabber != null) {
            grabberThread = new GrabberThread(this, grabber, color, colorUndist);
            grabberThread.start();
        }
    }
    private Matrix4x4 transfo = null;

    public void setTransfo(Homography homography) {
        this.transfo = homography.getTransformation();
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

    class GrabberThread extends Thread {

        private OpenKinectFrameGrabber grabber;
        IplImage colorImageUndist = null;
        private TouchInput touchInput;
        private boolean isRunning = true;
        private boolean useColor;
        private boolean useUndist;

        public GrabberThread(TouchInput ti, OpenKinectFrameGrabber grabber, boolean useColor, boolean useUndist) {
            this.grabber = grabber;
            this.touchInput = ti;
            this.useColor = useColor;
            this.useUndist = useUndist;

        }

        @Override
        public void run() {

            // default is true
            if (useColor) {

                while (isRunning) {
                    try {
                        IplImage depthImage = grabber.grabDepth();
                        IplImage colorImage = grabber.grabVideo();

                        // default is true
                        if (useUndist) {
                            if (colorImageUndist == null) {
                                colorImageUndist = colorImage.clone();
                            }
                            kinect.undistortRGB(colorImage, colorImageUndist);
                            colorImage = colorImageUndist;
                        }

                        touchPointSemaphore.acquire();
                        // Get the 3D location of kinect Points, 
                        // Select the blobs & tracks them. 
                        touchInput.startTouch(depthImage);

                        // assing a color to the touches
                        touchInput.getTouch2DColors(colorImage);
                        touchInput.endTouch();

                    } catch (FrameGrabber.Exception e) {
                        System.err.println("ERROR in grabber " + e);
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println("Semaphore Exception in grabber " + e);
                        e.printStackTrace();
                    } finally {
                        touchPointSemaphore.release();
                    }
                }

            } else {

                while (isRunning) {
                    try {
                        IplImage depthImage = grabber.grabDepth();
                        touchPointSemaphore.acquire();
                        touchInput.startTouch(depthImage);
                        touchInput.endTouch();
                        touchPointSemaphore.release();
                    } catch (Exception e) {
                        System.err.println("ERROR in grabber " + e);

                    }
                }
            }

        }

        public void stopThread() {
            this.isRunning = false;
        }
    }

    public void startTouch(IplImage depthImage) {

        try {
            depthDataSem.acquire();
            if (touch2DPrecision > 0 && touch3DPrecision > 0) {

                // 2D & 3D precision the same ? 
                kinect.updateMT(depthImage, kinectCalibration, touch2DPrecision, touch3DPrecision);

                findTouch(touchPoint2D, false, touch2DPrecision);
                findTouch(touchPoint3D, true, touch3DPrecision);

            } else {

                if (touch2DPrecision > 0) {
                    kinect.updateMT2D(depthImage, kinectCalibration, touch2DPrecision);
                    findTouch(touchPoint2D, false, touch2DPrecision);
                }

                if (touch3DPrecision > 0) {
                    kinect.updateMT3D(depthImage, kinectCalibration, touch3DPrecision);
                    findTouch(touchPoint3D, true, touch3DPrecision);
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TouchInput.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            depthDataSem.release();
        }
    }

    public void endTouch() {
        if (touch2DPrecision > 0) {
            touchFound(touchPoint2D);
        }
        if (touch3DPrecision > 0) {
            touchFound(touchPoint3D);
        }
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return this.touchPoint2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return this.touchPoint3D;
    }

    public ArrayList<Touch> projectTouchToScreen(Screen screen, ARDisplay display) {
        return projectTouchToScreen(screen, display, false, true, true, true, true);
    }

    public ArrayList<Touch> projectTouchToScreen(Screen screen, ARDisplay display, boolean is2D, boolean is3D) {
        return projectTouchToScreen(screen, display, false, is2D, is3D, is2D, is3D);
    }

    public ArrayList<Touch> projectTouchToScreen(Screen screen, ARDisplay display, boolean isAll,
            boolean is2D, boolean is3D,
            boolean isSpeed2D, boolean isSpeed3D) {

        if (isSpeed2D) {
            is2D = true;
        }
        if (isSpeed3D) {
            is3D = true;
        }

        ArrayList<Touch> touchList = new ArrayList<Touch>();

        boolean isProjector = display instanceof Projector;

//        Projector display = 
        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        if (is2D && !touchPoint2D.isEmpty()) {
            for (TouchPoint tp : touchPoint2D) {

                Touch touch = new Touch();

                // TouchPoint -> Normalized (screen) coordinates
                Vec3D vec = tp.v;
                if (this.transfo != null) {
                    vec = transfo.applyTo(vec);
                }

                if (!(TouchDetection.isInside(vec, 0, 1) || isAll)) {
                    continue;
                }

                PVector res, res2 = null;

                if (useExternalGrabber) {
                    int p = pdp.worldToPixel(tp.vKinect);
                    res = (isProjector ? (Projector) display : display).projectPointer(screen,
                            (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                            ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);

                } else {
                    res = (isProjector ? (Projector) display : display).projectPointer(screen, vec.x, vec.y);
                }
//                    res = (isProjector ? (Projector)display : display ).projectPointer(screen, tp);

                if (isSpeed2D) {

                    if (tp.oldvKinect != null) {
                        if (useExternalGrabber) {

                            int p = pdp.worldToPixel(tp.oldvKinect);
                            res2 = (isProjector ? (Projector) display : display).projectPointer(screen,
                                    (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                                    ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);

                        } else {
                            res2 = (isProjector ? (Projector) display : display).projectPointer(screen, tp.oldV.x, tp.oldV.y);
                        }
                    }
                }

                if (res == null) {
                    continue;
                }

                touch.p = null;
                // inside the paper sheet 	      
                if (TouchDetection.isInside(res, 0, 1) || isAll) {
                    PVector p = new PVector(res.x, res.y);
//                    touchList.position2D.add(p);
                    touch.p = p;
//                    touchList.points2D.add(tp);
                } else {
                    continue;
                }

                touch.is3D = false;
                touch.speed = null;
                touch.touchPoint = tp;

                if (res2 != null) {
                    // inside the paper sheet 	      
                    if (TouchDetection.isInside(res2, 0, 1) || isAll) {
                        PVector p = new PVector(res.x - res2.x,
                                res.y - res2.y);
                        touch.speed = p;
//                        touchList.speed2D.add(p);
                    }
                }

                touchList.add(touch);

            }
        }

        if (is3D && !touchPoint3D.isEmpty()) {
            for (TouchPoint tp : touchPoint3D) {

                Touch tl = new Touch();

                Vec3D vec = tp.v;
                if (this.transfo != null) {
                    vec = transfo.applyTo(vec);
                }

                // TODO: inside necessary ??
                // Inside the window
                if (!(TouchDetection.isInside(vec, -0.5f, 1.5f) || isAll)) {
                    continue;
                }

                PVector res, res2 = null;

                if (useExternalGrabber) {
                    int p = pdp.worldToPixel(tp.vKinect);
                    res = (isProjector ? (Projector) display : display).projectPointer(screen,
                            (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                            ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);

                } else {
                    res = (isProjector ? (Projector) display : display).projectPointer(screen, vec.x, vec.y);
                }

//                    res = (isProjector ? (Projector)display : display ).projectPointer(screen, tp);
                if (isSpeed3D && tp.oldV != null) {

                    if (useExternalGrabber) {
                        int p = pdp.worldToPixel(tp.oldvKinect);
                        res2 = (isProjector ? (Projector) display : display).projectPointer(screen,
                                (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                                ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);
                    } else {
                        res2 = (isProjector ? (Projector) display : display).projectPointer(screen, tp.oldV.x, tp.oldV.y);
                    }

                    if (res2 != null) {
                        res2.z = tp.oldV.z;
                    }
//                        res2 = (tp.oldV != null) ? (isProjector ? (Projector)display : display ).projectPointer(screen, tp) : null;
                }
                if (res == null) {
                    continue;
                }

                res.z = vec.z;
                // inside the paper sheet 	      
                if (TouchDetection.isInside(res, 0f, 1f) || isAll) {
                    PVector p = new PVector(res.x, res.y, vec.z);
//                    touchList.position3D.add(p);
                    tl.p = p;
//                    touchList.points3D.add(tp);
                }

                tl.speed = null;
                if (res2 != null) {
                    // inside the paper sheet 	      
                    //			if(res2.x >= 0 && res2.x <= 1 && res2.y >= 0 && res2.y <= 1)

                    PVector p = new PVector(res.x - res2.x,
                            res.y - res2.y,
                            (vec.z - tp.oldV.z));
//                    touchList.speed3D.add(p);
                    tl.speed = p;
                }

                tl.touchPoint = tp;
                tl.is3D = true;
                touchList.add(tl);
            }
        }

        touchPointSemaphore.release();

        return touchList;
    }

    public KinectScreenCalibration getCalibration() {
        return this.kinectCalibration;
    }

    public ArrayList<Vec3D> projectDepthData(ARDisplay display, Screen screen) {
        try {

            depthDataSem.acquire();
            DepthData depthData = kinect.getDepthData();

            ArrayList<Vec3D> projected = new ArrayList<Vec3D>();
            Vec3D[] projPoints = depthData.projectedPoints;
            boolean[] valid3D = depthData.validPointsMask3D;
            boolean isProjector = display instanceof Projector;

            for (int i = 0; i < projPoints.length; i++) {
                Vec3D vec = projPoints[i];
                if (vec == null || valid3D[i] == false) {
                    continue;
                }

                // Inside the window
                if (!TouchDetection.isInside(vec, -0.5f, 1.5f)) {
                    continue;
                }

                PVector res;
                res = (isProjector ? (Projector) display : display).projectPointer(screen, vec.x, vec.y);
                if (res == null) {
                    continue;
                }

                res.z = vec.z;
                projected.add(vec);
            }
            depthDataSem.release();
            return projected;
//                    res = (isProjector ? (Projector)display : display ).projectPointer(screen, tp);
        } catch (InterruptedException ex) {
            Logger.getLogger(TouchInput.class.getName()).log(Level.SEVERE, null, ex);
        }


        return null;
    }

    public ArrayList<TouchPoint> getTouchPoint2D() {
        return touchPoint2D;
    }

    public ArrayList<TouchPoint> getTouchPoint3D() {
        return touchPoint3D;
    }

    public void getTouch2DColors(IplImage colorImage) {
        getTouchColors(colorImage, this.touchPoint2D);
    }

    public void getTouchColors(IplImage colorImage,
            ArrayList<TouchPoint> touchPointList) {

        if (touchPointList.isEmpty()) {
            return;
        }

        ByteBuffer cBuff = colorImage.getByteBuffer();

//        System.out.println("Searching for point color");
        for (TouchPoint tp : touchPointList) {
            int offset = 3 * kinect.findColorOffset(tp.vKinect);

            tp.color = (255 & 0xFF) << 24
                    | (cBuff.get(offset + 2) & 0xFF) << 16
                    | (cBuff.get(offset + 1) & 0xFF) << 8
                    | (cBuff.get(offset) & 0xFF);
        }

    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no updates, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw(int skip) {
        assert (skip > 0);

        return TouchDetection.findMultiTouch(kinect.getDepthData(),
                kinectCalibration,
                false, skip);
    }

    public ArrayList<TouchPoint> find3DTouchRaw(int skip) {
        assert (skip > 0);

        return TouchDetection.findMultiTouch(kinect.getDepthData(),
                kinectCalibration, true, skip);
    }

    public void findTouch(ArrayList<TouchPoint> touchPointList, boolean is3D, int skip) {

        assert (skip > 0);

        ArrayList<TouchPoint> touchPoints = TouchDetection.findMultiTouch(kinect.getDepthData(),
                kinectCalibration, is3D, skip);

        if (touchPoints == null) {
            return;
        }

        // no previous points add all and return.
        if (touchPointList.isEmpty()) {
            for (TouchPoint tp : touchPoints) {
                tp.updateTime = papplet.millis();
                touchPointList.add(tp);
            }
            return;
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

        float trackDist = is3D ? trackNearDist3D : trackNearDist;

        for (TouchPointTracker tpt1 : tpt) {
            if (tpt1.distance < trackDist) {
                tpt1.update(papplet.millis());
            }
        }

        // Add the new ones ?
        for (TouchPoint tp : touchPoints) {
            if (!tp.toDelete) {
                tp.updateTime = papplet.millis();
                touchPointList.add(tp);
            }
        }
    }

    public void touchFound(ArrayList<TouchPoint> touchPointList) {
        for (TouchPoint tp : touchPointList) {
            tp.setUpdated(false);
        }

        ArrayList<TouchPoint> toDelete = new ArrayList<TouchPoint>();
        for (TouchPoint tpOld : touchPointList) {
            if (tpOld.isObselete(papplet.millis(), forgetTime)) {
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
}

//
//    public ArrayList<TouchPoint> find2DTouch(int skip) {
//        return findTouch(touchPoint2D, false, skip);
//    }
//
//    public ArrayList<TouchPoint> find3DTouch(int skip) {
//        return findTouch(touchPoint3D, true, skip);
//    }
//
//    public void touch2DFound() {
//        touchFound(touchPoint2D);
//    }
//
//    public void touch3DFound() {
//        touchFound(touchPoint3D);
//    }
//    

