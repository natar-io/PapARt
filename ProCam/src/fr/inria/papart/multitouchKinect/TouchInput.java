package fr.inria.papart.multitouchKinect;

import com.googlecode.javacv.OpenKinectFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import fr.inria.papart.kinect.Homography;
import fr.inria.papart.procam.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.kinect.Kinect;
import fr.inria.papart.multitouchKinect.MultiTouchKinect;
import fr.inria.papart.multitouchKinect.TouchPoint;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 * Touch input, using a Kinect device for now.
 *
 * @author jeremylaviole
 */
public class TouchInput {

    private ArrayList<TouchPoint> touchPoints2D, touchPoints3D;
    private int touch2DPrecision, touch3DPrecision;
    private MultiTouchKinect mtk;
    private Kinect kinect;
    private GrabberThread grabberThread = null;
    private static final int MAX_AVAILABLE = 1;
    private final Semaphore touchPointSemaphore = new Semaphore(MAX_AVAILABLE, true);
    private ProjectiveDeviceP pdp;
    private boolean useExternalGrabber = false;

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect) {
        this(applet, calibrationFile, kinect, null, 1, 4);
    }

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, int precision2D, int precision3D) {
        this(applet, calibrationFile, kinect, null, precision2D, precision3D);
    }

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, OpenKinectFrameGrabber grabber, int precision2D, int precision3D) {
        this(applet, calibrationFile, kinect, grabber, false, precision2D, precision3D);
    }

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, OpenKinectFrameGrabber grabber, boolean color, int precision2D, int precision3D) {
        this(applet, calibrationFile, kinect, grabber, color, true, precision2D, precision3D);
    }

    public TouchInput(PApplet applet, String calibrationFile, Camera kinectCamera, Kinect kinect, boolean color, int precision2D, int precision3D) {
        if (!kinectCamera.useKinect()) {
            System.err.println("Impossible to init a Touch Input without a  Kinect camera. ");

        }
        mtk = new MultiTouchKinect(applet, kinect, calibrationFile);
        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;
        touchPoints2D = mtk.getTouchPoint2D();
        touchPoints3D = mtk.getTouchPoint3D();
        this.kinect = kinect;
        kinectCamera.setTouch(this);
        this.useExternalGrabber = true;
        this.pdp = kinectCamera.getProjectiveDevice();
        // TODO: récup le Grabber, et récup le grabber de la profondeur. 
    }

    public TouchInput(PApplet applet, String calibrationFile, Kinect kinect, OpenKinectFrameGrabber grabber, boolean color, boolean colorUndist, int precision2D, int precision3D) {
        mtk = new MultiTouchKinect(applet, kinect, calibrationFile);
        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;
        touchPoints2D = mtk.getTouchPoint2D();
        touchPoints3D = mtk.getTouchPoint3D();
        this.kinect = kinect;

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
        try{
        touchPointSemaphore.acquire();
        } catch(Exception e){
            
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

            if (useColor) {

                while (isRunning) {
                    try {
                        IplImage depthImage = grabber.grabDepth();
                        IplImage colorImage = grabber.grabVideo();

                        if (useUndist) {
                            if (colorImageUndist == null) {
                                colorImageUndist = colorImage.clone();
                            }
                            kinect.undistortRGB(colorImage, colorImageUndist);
                            colorImage = colorImageUndist;
                        }

                        touchPointSemaphore.acquire();
                        touchInput.startTouch(depthImage);
                        touchInput.findColors(depthImage, colorImage);
                        touchInput.endTouch();

                    } catch (Exception e) {
                        System.err.println("ERROR in grabber " + e);
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

        if (touch2DPrecision > 0) {
            mtk.updateKinect(depthImage, touch2DPrecision);
            mtk.find2DTouch(touch2DPrecision);
        }

        if (touch3DPrecision > 0) {
            mtk.updateKinect3D(depthImage, touch3DPrecision);
            mtk.find3DTouch(touch3DPrecision);
        }
    }

    public void findColors(IplImage depthImage, IplImage colorImage) {
        mtk.findColor(depthImage, colorImage, kinect, touchPoints2D, touch2DPrecision);
    }

    public void endTouch() {
        if (touch2DPrecision > 0) {
            mtk.touch2DFound();
        }
        if (touch3DPrecision > 0) {
            mtk.touch3DFound();
        }
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return touchPoints3D;
    }

    public TouchElement projectTouchToScreen(Screen screen, ARDisplay projector) {
        return projectTouchToScreen(screen, projector, false, true, true, true, true);
    }

    public TouchElement projectTouchToScreen(Screen screen, ARDisplay projector, boolean is2D, boolean is3D) {
        return projectTouchToScreen(screen, projector, false, is2D, is3D, is2D, is3D);
    }

    public TouchElement projectTouchToScreen(Screen screen, ARDisplay projector, boolean isAll,
            boolean is2D, boolean is3D,
            boolean isSpeed2D, boolean isSpeed3D) {

        if (isSpeed2D) {
            is2D = true;
        }
        if (isSpeed3D) {
            is3D = true;
        }

        ArrayList<PVector> position2D = new ArrayList<PVector>();
        ArrayList<PVector> position3D = new ArrayList<PVector>();
        ArrayList<PVector> speed2D = new ArrayList<PVector>();
        ArrayList<PVector> speed3D = new ArrayList<PVector>();


        ArrayList<TouchPoint> points2D = new ArrayList<TouchPoint>();
        ArrayList<TouchPoint> points3D = new ArrayList<TouchPoint>();

        ArrayList<Touch> locationList = new ArrayList<Touch>();


        TouchElement elem = new TouchElement();
        elem.position2D = position2D;
        elem.position3D = position3D;
        elem.speed2D = speed2D;
        elem.speed3D = speed3D;
        elem.points2D = points2D;
        elem.points3D = points3D;
        elem.touches = locationList;


        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        if (is2D && !touchPoints2D.isEmpty()) {
            for (TouchPoint tp : touchPoints2D) {

                Touch tl = new Touch();

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
                    res = projector.projectPointer(screen,
                            (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                            ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);

                } else {
                    res = projector.projectPointer(screen, vec.x, vec.y);
                }
//                    res = projector.projectPointer(screen, tp);

                if (isSpeed2D) {

                    if (tp.oldvKinect != null) {
                        if (useExternalGrabber) {

                            int p = pdp.worldToPixel(tp.oldvKinect);
                            res2 = projector.projectPointer(screen,
                                    (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                                    ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);

                        } else {
                            res2 = projector.projectPointer(screen, tp.oldV.x, tp.oldV.y);
                        }
                    }
                }

                if (res == null) {
                    continue;
                }

                tl.p = null;
                // inside the paper sheet 	      
                if (TouchDetection.isInside(res, 0, 1) || isAll) {
                    PVector p = new PVector(res.x, res.y);
                    position2D.add(p);
                    tl.p = p;
                    points2D.add(tp);
                } else {
                    continue;
                }

                tl.is3D = false;
                tl.speed = null;
                tl.touchPoint = tp;

                if (res2 != null) {
                    // inside the paper sheet 	      
                    if (TouchDetection.isInside(res2, 0, 1) || isAll) {
                        PVector p = new PVector(res.x - res2.x,
                                res.y - res2.y);
                        tl.speed = p;
                        speed2D.add(p);
                    }
                }

                locationList.add(tl);

            }
        }

        if (is3D && !touchPoints3D.isEmpty()) {
            for (TouchPoint tp : touchPoints3D) {

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
                    res = projector.projectPointer(screen,
                            (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                            ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);

                } else {
                    res = projector.projectPointer(screen, vec.x, vec.y);
                }

//                    res = projector.projectPointer(screen, tp);


                if (isSpeed3D && tp.oldV != null) {


                    if (useExternalGrabber) {
                        int p = pdp.worldToPixel(tp.oldvKinect);
                        res2 = projector.projectPointer(screen,
                                (float) (p % Kinect.KINECT_WIDTH) / Kinect.KINECT_WIDTH,
                                ((float) p / Kinect.KINECT_WIDTH) / Kinect.KINECT_HEIGHT);
                    } else {
                        res2 = projector.projectPointer(screen, tp.oldV.x, tp.oldV.y);
                    }

                    if (res2 != null) {
                        res2.z = tp.oldV.z;
                    }
//                        res2 = (tp.oldV != null) ? projector.projectPointer(screen, tp) : null;
                }
                if (res == null) {
                    continue;
                }

                res.z = vec.z;
                // inside the paper sheet 	      
                if (TouchDetection.isInside(res, 0f, 1f) || isAll) {
                    PVector p = new PVector(res.x, res.y, vec.z);
                    position3D.add(p);
                    tl.p = p;
                    points3D.add(tp);
                }

                tl.speed = null;
                if (res2 != null) {
                    // inside the paper sheet 	      
                    //			if(res2.x >= 0 && res2.x <= 1 && res2.y >= 0 && res2.y <= 1)

                    PVector p = new PVector(res.x - res2.x,
                            res.y - res2.y,
                            (vec.z - tp.oldV.z));
                    speed3D.add(p);
                    tl.speed = p;
                }

                tl.touchPoint = tp;
                tl.is3D = true;
                locationList.add(tl);
            }
        }

        touchPointSemaphore.release();

        return elem;
    }
}