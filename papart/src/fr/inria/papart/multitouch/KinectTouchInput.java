/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.depthcam.devices.KinectDepthData;
import fr.inria.papart.depthcam.DepthDataElementKinect;
import fr.inria.papart.depthcam.DepthPoint;
import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.devices.KinectDepthAnalysis;
import fr.inria.papart.depthcam.devices.KinectDevice;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * Touch input, using a Kinect device for now.
 *
 * TODO: Refactor all this.
 *
 * @author jeremylaviole
 */
public class KinectTouchInput extends TouchInput {

    public static final int NO_TOUCH = -1;
    private int touch2DPrecision, touch3DPrecision;
    private KinectDepthAnalysis depthAnalysis;
    private PApplet parent;

    private final Semaphore touchPointSemaphore = new Semaphore(1, true);
    private final Semaphore depthDataSem = new Semaphore(1);

    // List of TouchPoints, given to the user
    private final KinectDevice kinectDevice;

    private PlaneAndProjectionCalibration planeAndProjCalibration;

    // List of TouchPoints, given to the user
    private final ArrayList<TouchPoint> touchPoints2D = new ArrayList<>();
    private final ArrayList<TouchPoint> touchPoints3D = new ArrayList<>();
    private final TouchDetectionSimple2D touchDetection2D;
    private final TouchDetectionSimple3D touchDetection3D;

    public KinectTouchInput(PApplet applet,
            KinectDevice kinectDevice,
            KinectDepthAnalysis depthAnalysis,
            PlaneAndProjectionCalibration calibration) {
        this.parent = applet;
        this.depthAnalysis = depthAnalysis;
        this.kinectDevice = kinectDevice;
        this.planeAndProjCalibration = calibration;
        this.touchDetection2D = new TouchDetectionSimple2D(depthAnalysis.getDepthSize());
        this.touchDetection3D = new TouchDetectionSimple3D(depthAnalysis.getDepthSize());
    }
    
    public void setPlaneAndProjCalibration(PlaneAndProjectionCalibration papc){
        this.planeAndProjCalibration = papc;
    }

    public void setTouchDetectionCalibration(PlanarTouchCalibration touchCalib) {
        this.touchDetection2D.setCalibration(touchCalib);
    }

    public void setTouchDetectionCalibration3D(PlanarTouchCalibration touchCalib) {
        this.touchDetection3D.setCalibration(touchCalib);
    }

    @Override
    public void update() {
        try {
            IplImage depthImage = kinectDevice.getCameraDepth().getIplImage();
            IplImage colImage = kinectDevice.getCameraRGB().getIplImage();

            depthDataSem.acquire();
            if (colImage == null || depthImage == null) {
                return;
            }

            touch2DPrecision = touchDetection2D.getPrecision();
            touch3DPrecision = touchDetection3D.getPrecision();
            if (touch2DPrecision > 0 && touch3DPrecision > 0) {
                depthAnalysis.updateMT(depthImage, colImage, planeAndProjCalibration, touch2DPrecision, touch3DPrecision);
                findAndTrack2D();
                findAndTrack3D();
            } else {
                if (touch2DPrecision > 0) {
                    depthAnalysis.updateMT2D(depthImage, colImage, planeAndProjCalibration, touch2DPrecision);
                    findAndTrack2D();
                }
                if (touch3DPrecision > 0) {
                    depthAnalysis.updateMT3D(depthImage, colImage, planeAndProjCalibration, touch3DPrecision);
                    findAndTrack3D();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(KinectTouchInput.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            depthDataSem.release();
        }
    }

    private static final Touch INVALID_TOUCH = new Touch();

    @Override
    public TouchList projectTouchToScreen(Screen screen, BaseDisplay display) {

        TouchList touchList = new TouchList();

        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }

        for (TouchPoint tp : touchPoints2D) {
            Touch touch = createTouch(screen, display, tp);
            if (touch != INVALID_TOUCH) {
                touchList.add(touch);
            }
        }

        for (TouchPoint tp : touchPoints3D) {
            try {
                Touch touch = createTouch(screen, display, tp);
                if (touch != INVALID_TOUCH) {
                    touchList.add(touch);
                }
            } catch (Exception e) {
//                System.err.println("Intersection fail. " + e);
            }
        }

        touchPointSemaphore.release();
        return touchList;
    }

    private Touch createTouch(Screen screen, BaseDisplay display, TouchPoint tp) {
        Touch touch = tp.getTouch();
        boolean hasProjectedPos = projectPositionAndSpeed(screen, display, touch, tp);
        if (!hasProjectedPos) {
            return INVALID_TOUCH;
        }
        touch.isGhost = tp.isToDelete();
        touch.is3D = tp.is3D();
        touch.touchPoint = tp;
        return touch;
    }

    // TODO: Raw Depth is for Kinect Only, find a cleaner solution.
//    private ProjectiveDeviceP pdp;
    private boolean useRawDepth = false;

    public void useRawDepth() {
        this.useRawDepth = true;
//        this.pdp = camera.getProjectiveDevice();
    }

    private boolean projectPositionAndSpeed(Screen screen,
            BaseDisplay display,
            Touch touch, TouchPoint tp) {

        boolean hasProjectedPos = projectPosition(screen, display, touch, tp);
        if (hasProjectedPos) {
            projectSpeed(screen, display, touch, tp);
        }
        return hasProjectedPos;
    }

    private boolean projectPosition(Screen screen,
            BaseDisplay display,
            Touch touch, TouchPoint tp) {

        PVector paperScreenCoord = projectPointToScreen(screen,
                display,
                tp.getPositionKinect(),
                tp.getPositionVec3D());

        touch.setPosition(paperScreenCoord);

        return paperScreenCoord != NO_INTERSECTION;
    }

    private boolean projectSpeed(Screen screen,
            BaseDisplay display,
            Touch touch, TouchPoint tp) {

        PVector paperScreenCoord = projectPointToScreen(screen,
                display,
                tp.getPreviousPositionKinect(),
                tp.getPreviousPositionVec3D());

        if (paperScreenCoord == NO_INTERSECTION) {
            touch.defaultPrevPos();
        } else {
            touch.setPrevPos(paperScreenCoord);
        }
        return paperScreenCoord != NO_INTERSECTION;
    }

    public ArrayList<DepthDataElementKinect> getDepthData() {
        try {
            depthDataSem.acquire();
            KinectDepthData depthData = depthAnalysis.getDepthData();
            ArrayList<DepthDataElementKinect> output = new ArrayList<>();
            ArrayList<Integer> list = depthData.validPointsList3D;
            for (Integer i : list) {
                output.add(depthData.getElementKinect(i));
            }
            depthDataSem.release();
            return output;

        } catch (InterruptedException ex) {
            Logger.getLogger(KinectTouchInput.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }
    }

    // TODO: Do the same with DepthDataElement  instead of  DepthPoint ?
    public ArrayList<DepthPoint> projectDepthData(ARDisplay display, Screen screen) {
        ArrayList<DepthPoint> list = projectDepthData2D(display, screen);
        list.addAll(projectDepthData3D(display, screen));
        return list;
    }

    public ArrayList<DepthPoint> projectDepthData2D(ARDisplay display, Screen screen) {
        return projectDepthDataXD(display, screen, true);
    }

    public ArrayList<DepthPoint> projectDepthData3D(ARDisplay display, Screen screen) {
        return projectDepthDataXD(display, screen, false);
    }

    private ArrayList<DepthPoint> projectDepthDataXD(ARDisplay display, Screen screen, boolean is2D) {
        try {
            depthDataSem.acquire();
            KinectDepthData depthData = depthAnalysis.getDepthData();
            ArrayList<DepthPoint> projected = new ArrayList<DepthPoint>();
            ArrayList<Integer> list = is2D ? depthData.validPointsList : depthData.validPointsList3D;
            for (Integer i : list) {
                DepthPoint depthPoint = tryCreateDepthPoint(display, screen, i);
                if (depthPoint != null) {
                    projected.add(depthPoint);
                }
            }
            depthDataSem.release();
            return projected;

        } catch (InterruptedException ex) {
            Logger.getLogger(KinectTouchInput.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }
    }

    private DepthPoint tryCreateDepthPoint(ARDisplay display, Screen screen, int offset) {
        Vec3D projectedPt = depthAnalysis.getDepthData().projectedPoints[offset];

        PVector screenPosition = projectPointToScreen(screen, display,
                depthAnalysis.getDepthData().depthPoints[offset],
                projectedPt);

        if (screenPosition == NO_INTERSECTION) {
            return null;
        }

        int c = depthAnalysis.getDepthData().pointColors[offset];
        return new DepthPoint(screenPosition.x, screenPosition.y, screenPosition.z, c);
    }

    /**
     * *
     *
     * @param screen
     * @param display
     * @param dde
     * @return the projected point, NULL if no intersection was found.
     */
    public PVector projectPointToScreen(Screen screen,
            BaseDisplay display, DepthDataElementKinect dde) {

        PVector out = this.projectPointToScreen(screen,
                display,
                dde.depthPoint,
                dde.projectedPoint);
        return out;
    }

    private PVector projectPointToScreen(Screen screen,
            BaseDisplay display, Vec3D pKinect, Vec3D pNorm) {

        PVector paperScreenCoord;
        if (useRawDepth) {

            // Method 1  -> Loose information of Depth !
            // Stays here, might be used later.
//            PVector p = pdp.worldToPixelCoord(pKinect);
//            paperScreenCoord = project(screen, display,
//                    p.x / (float) pdp.getWidth(),
//                    p.y / (float) pdp.getHeight());
            paperScreenCoord = new PVector();
            PVector pKinectP = new PVector(pKinect.x, pKinect.y, pKinect.z);

            PMatrix3D transfo = screen.getLocation(display.getCamera());
            transfo.invert();
            transfo.mult(pKinectP, paperScreenCoord);

            // TODO: check bounds too ?!
        } else {
            paperScreenCoord = project(screen, display,
                    pNorm.x,
                    pNorm.y);

            if (paperScreenCoord == NO_INTERSECTION) {
                return NO_INTERSECTION;
            }
            paperScreenCoord.z = pNorm.z;
            paperScreenCoord.x *= screen.getSize().x;
            paperScreenCoord.y = (1f - paperScreenCoord.y) * screen.getSize().y;
        }

        if (computeOutsiders) {
            return paperScreenCoord;
        }

        if (paperScreenCoord.x == PApplet.constrain(paperScreenCoord.x, 0, screen.getSize().x)
                && paperScreenCoord.y == PApplet.constrain(paperScreenCoord.y, 0, screen.getSize().y)) {
            return paperScreenCoord;
        } else {
            return NO_INTERSECTION;
        }
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

        for (TouchPoint tp : touchPointList) {
            int offset = 3 * depthAnalysis.kinectDevice().findColorOffset(tp.getPositionKinect());

            tp.setColor((255 & 0xFF) << 24
                    | (cBuff.get(offset + 2) & 0xFF) << 16
                    | (cBuff.get(offset + 1) & 0xFF) << 8
                    | (cBuff.get(offset) & 0xFF));
        }
    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no updates, no tracking. 
    public ArrayList<TouchPoint> find2DTouchRaw() {
        return touchDetection2D.compute(depthAnalysis.getDepthData());
    }

    public ArrayList<TouchPoint> find3DTouchRaw(int skip) {
        return touchDetection3D.compute(depthAnalysis.getDepthData());
    }

    protected void findAndTrack2D() {
        assert (touch2DPrecision != 0);
        ArrayList<TouchPoint> newList = touchDetection2D.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(touchPoints2D, newList,
                parent.millis());
    }

    protected void findAndTrack3D() {
        assert (touch3DPrecision != 0);
        ArrayList<TouchPoint> newList = touchDetection3D.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(touchPoints3D,
                newList,
                parent.millis());
    }

    public ArrayList<TouchPoint> getTouchPoints2D() {
        return this.touchPoints2D;
    }

    public ArrayList<TouchPoint> getTouchPoints3D() {
        return this.touchPoints3D;
    }

    public PlaneAndProjectionCalibration getCalibration() {
        return planeAndProjCalibration;
    }

    public boolean isUseRawDepth() {
        return useRawDepth;
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
