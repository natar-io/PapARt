package fr.inria.papart.multitouchKinect;

import com.googlecode.javacv.OpenKinectFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import fr.inria.papart.procam.Projector;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.kinect.Kinect;
import fr.inria.papart.multitouchKinect.MultiTouchKinect;
import fr.inria.papart.multitouchKinect.TouchPoint;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import processing.core.PApplet;
import processing.core.PVector;

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
    private final Semaphore sem = new Semaphore(MAX_AVAILABLE, true);

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
        mtk = new MultiTouchKinect(applet, kinect, calibrationFile);
        this.touch2DPrecision = precision2D;
        this.touch3DPrecision = precision3D;
        touchPoints2D = mtk.getTouchPoint2D();
        touchPoints3D = mtk.getTouchPoint3D();

        if (grabber != null) {
            grabberThread = new GrabberThread(this, grabber, color);
            grabberThread.start();
        }
    }

    class GrabberThread extends Thread {

        private OpenKinectFrameGrabber grabber;
        private TouchInput touchInput;
        private boolean isRunning = true;
        private boolean useColor;

        public GrabberThread(TouchInput ti, OpenKinectFrameGrabber grabber, boolean useColor) {
            this.grabber = grabber;
            this.touchInput = ti;
            this.useColor = useColor;
        }

        @Override
        public void run() {

            if (useColor) {

                while (isRunning) {
                    try {
                        IplImage depthImage = grabber.grabDepth();
                        IplImage colorImage = grabber.grabVideo();

                        sem.acquire();
                        touchInput.startTouch(depthImage, colorImage);
                        touchInput.endTouch();

                    } catch (Exception e) {
                        System.err.println("ERROR in grabber " + e);
                        e.printStackTrace();
                    } finally {
                        sem.release();
                    }
                }

            } else {

                while (isRunning) {
                    try {
                        IplImage depthImage = grabber.grabDepth();
                        sem.acquire();
                        touchInput.startTouch(depthImage);
                        touchInput.endTouch();
                        sem.release();
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

        mtk.updateKinect(depthImage, touch2DPrecision);
        mtk.find2DTouch(touch2DPrecision);

        mtk.updateKinect3D(depthImage, touch3DPrecision);
        mtk.find3DTouch(touch3DPrecision);
    }

    // TODO 3D ? : at least 2D for now...
    public void startTouch(IplImage depthImage, IplImage colorImage) {

        mtk.updateKinect(depthImage, touch2DPrecision);
        mtk.find2DTouch(touch2DPrecision);
        
        // BROKEN 
//        mtk.findColor(depthImage, colorImage, kinect, touchPoints2D, touch2DPrecision);

        mtk.updateKinect3D(depthImage, touch3DPrecision);
        mtk.find3DTouch(touch3DPrecision);
    }

    public void endTouch() {
        mtk.touch2DFound();
        mtk.touch3DFound();
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return touchPoints3D;
    }

    public TouchElement projectTouchToScreen(Screen screen, Projector projector) {
        return projectTouchToScreen(screen, projector, true, true, true, true);
    }

    public TouchElement projectTouchToScreen(Screen screen, Projector projector, boolean is2D, boolean is3D) {
        return projectTouchToScreen(screen, projector, is2D, is3D, true, true);
    }

    public TouchElement projectTouchToScreen(Screen screen, Projector projector,
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

        // OLD API
        TouchElement elem = new TouchElement();
        elem.position2D = position2D;
        elem.position3D = position3D;

        elem.speed2D = speed2D;
        elem.speed3D = speed3D;

        // New API
        elem.points2D = points2D;
        elem.points3D = points3D;


        try {
            sem.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        if (is2D && !touchPoints2D.isEmpty()) {
            for (TouchPoint tp : touchPoints2D) {

                // TODO: change this to get outside points ? 
                // Inside the window
                if (tp.v.x >= 0 && tp.v.x < 1
                        && tp.v.y >= 0 && tp.v.y < 1) {

                    PVector res, res2;
                    res = projector.projectPointer(screen, tp.v.x, tp.v.y);


                    if (isSpeed2D) {
                        res2 = (tp.oldV != null) ? projector.projectPointer(screen, tp.oldV.x, tp.oldV.y) : null;
                    } else {
                        res2 = null;
                    }

                    if (res != null) {

                        // inside the paper sheet 	      
                        if (res.x >= 0 && res.x <= 1 && res.y >= 0 && res.y <= 1) {
                            position2D.add(new PVector(res.x, res.y));
                            points2D.add(tp);
                        }

                        if (res2 != null) {
                            // inside the paper sheet 	      
                            if (res2.x >= 0 && res2.x <= 1 && res2.y >= 0 && res2.y <= 1) {
                                speed2D.add(new PVector(res.x - res2.x,
                                        res.y - res2.y));
                            }
                        }
                    }
                }

            }
        }

        if (is3D && !touchPoints3D.isEmpty()) {
            for (TouchPoint tp : touchPoints3D) {

                // TODO: inside necessary ??
                // Inside the window
                if (tp.v.x >= 0 && tp.v.x < 1
                        && tp.v.y >= 0 && tp.v.y < 1) {

                    PVector res, res2;
                    res = projector.projectPointer(screen, tp.v.x, tp.v.y);
//                    res = projector.projectPointer(screen, tp);


                    if (isSpeed3D && tp.oldV != null) {
                        res2 = projector.projectPointer(screen, tp.oldV.x, tp.oldV.y);

                        if (res2 != null) {
                            res2.z = tp.oldV.z;
                        }
//                        res2 = (tp.oldV != null) ? projector.projectPointer(screen, tp) : null;
                    } else {
                        res2 = null;
                    }

                    if (res != null) {

                        res.z = tp.v.z;
                        // inside the paper sheet 	      
                        if (res.x >= 0 && res.x <= 1 && res.y >= 0 && res.y <= 1) {
                            position3D.add(new PVector(res.x, res.y, tp.v.z));
                            points3D.add(tp);
                        }

                        if (res2 != null) {

                            // inside the paper sheet 	      
                            //			if(res2.x >= 0 && res2.x <= 1 && res2.y >= 0 && res2.y <= 1)
                            speed3D.add(new PVector(res.x - res2.x,
                                    res.y - res2.y,
                                    (tp.v.z - tp.oldV.z)));
                        }
                    }

                }
            }
        }

        sem.release();

        return elem;
    }

    public TouchElement projectAllTouchToScreen(Screen screen, Projector projector,
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

        // OLD API
        TouchElement elem = new TouchElement();
        elem.position2D = position2D;
        elem.position3D = position3D;

        elem.speed2D = speed2D;
        elem.speed3D = speed3D;

        // New API
        elem.points2D = points2D;
        elem.points3D = points3D;

        try {
            sem.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        if (is2D && !touchPoints2D.isEmpty()) {
            for (TouchPoint tp : touchPoints2D) {
                PVector proj, projSpeed;
                proj = projector.projectPointer(screen, tp.v.x, tp.v.y);


                if (isSpeed2D) {
                    projSpeed = (tp.oldV != null) ? projector.projectPointer(screen, tp.oldV.x, tp.oldV.y) : null;
                } else {
                    projSpeed = null;
                }

                if (proj != null) {
                    position2D.add(new PVector(proj.x, proj.y));
                    points2D.add(tp);

                    if (projSpeed != null) {
                        // inside the paper sheet 	      
                        if (projSpeed.x >= 0 && projSpeed.x <= 1 && projSpeed.y >= 0 && projSpeed.y <= 1) {
                            speed2D.add(new PVector(proj.x - projSpeed.x,
                                    proj.y - projSpeed.y));
                        }
                    }
                }

            }
        }

        if (is3D && !touchPoints3D.isEmpty()) {
            for (TouchPoint tp : touchPoints3D) {

                PVector proj, projSpeed;
                proj = projector.projectPointer(screen, tp.v.x, tp.v.y);
                points3D.add(tp);
                if (isSpeed3D && tp.oldV != null) {
                    projSpeed = projector.projectPointer(screen, tp.oldV.x, tp.oldV.y);
                    if (projSpeed != null) {
                        projSpeed.z = tp.oldV.z;
                    }
                } else {
                    projSpeed = null;
                }

                if (proj != null) {

                    proj.z = tp.v.z;
                    position3D.add(new PVector(proj.x, proj.y, tp.v.z));

                    if (projSpeed != null) {
                        speed3D.add(new PVector(proj.x - projSpeed.x,
                                proj.y - projSpeed.y,
                                (tp.v.z - tp.oldV.z)));
                    }
                }

            }
        }

        sem.release();
        return elem;
    }
}