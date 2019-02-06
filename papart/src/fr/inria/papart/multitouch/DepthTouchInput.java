/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.multitouch.detection.ArmDetection;
import fr.inria.papart.multitouch.detection.FingerDetection;
import fr.inria.papart.multitouch.detection.HandDetection;
import fr.inria.papart.multitouch.detection.ObjectDetection;
import fr.inria.papart.multitouch.detection.Simple2D;
import fr.inria.papart.multitouch.detection.TouchDetection;
import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.MathUtils;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * Touch input, using a depth camera device.
 *
 * @author jeremylaviole - laviole@rea.lity.tech
 */
public class DepthTouchInput extends TouchInput {

    public static final int NO_TOUCH = -1;
    private int touch2DPrecision, touch3DPrecision;
    private DepthAnalysisImpl depthAnalysis;

    private PApplet parent;

    private final Semaphore touchPointSemaphore = new Semaphore(1, true);
    private final Semaphore depthDataSem = new Semaphore(1);

    // List of TouchPoints, given to the user
    private final DepthCameraDevice depthCameraDevice;

    private PlaneAndProjectionCalibration planeAndProjCalibration;

    private Simple2D simpleDetection;
    private ObjectDetection objectDetection;
    private ArmDetection armDetection;
    private HandDetection handDetection;
    private FingerDetection fingerDetection;

    private TouchDetectionDepth touchDetections[] = new TouchDetectionDepth[3];

    private PlanarTouchCalibration touchCalibrations[] = new PlanarTouchCalibration[3];
    private PlanarTouchCalibration simpleTouchCalibration, objectTouchCalibration;

    public DepthTouchInput(PApplet applet,
            DepthCameraDevice kinectDevice,
            DepthAnalysisImpl depthAnalysis,
            PlaneAndProjectionCalibration calibration) {
        this.parent = applet;
        this.depthAnalysis = depthAnalysis;
        this.depthCameraDevice = kinectDevice;
        this.planeAndProjCalibration = calibration;
    }

    private boolean touchDetectionsReady = false;
    private boolean depthAnalysisReady = false;

    @Override
    public boolean isReady() {
        return touchDetectionsReady;
    }

    public void loadConfiguration() {
        loadConfiguration(Papart.getPapart());
    }

    public void loadConfiguration(Papart papart) {
        for (int i = 0; i < 3; i++) {
            setTouchDetectionCalibration(i, papart.getTouchCalibration(i));
        }
        setSimpleTouchDetectionCalibration(papart.getDefaultTouchCalibration());
        setObjectTouchDetectionCalibration(papart.getDefaultObjectTouchCalibration());
    }

    /**
     * Load a set of (default) touch detections, only hands for now.
     */
    public void initTouchDetections() {
        // First run, get calibrations from device after start.
//        initSimpleTouchDetection();
//        initObjectDetection();
        initHandDetection();
        touchDetectionsReady = true;
    }

    /**
     * Create Simple touch detection: detects everything that is on the table.
     */
    public void initSimpleTouchDetection() {
        checkCalibrations();
        simpleDetection = new Simple2D(depthAnalysis, simpleTouchCalibration);
    }

    /**
     * TouchInput with object detection. Implementation in progress. This may
     * lead to many classes of object detections.
     */
    public ObjectDetection initObjectDetection() {
        checkCalibrations();
        objectDetection = new ObjectDetection(depthAnalysis, objectTouchCalibration);
        return objectDetection;
    }

    /**
     * TouchInput with hand detection: provides Arm, hand and finger detections.
     */
    public TouchDetectionDepth initHandDetection() {
        checkCalibrations();
        touchDetections[0] = new ArmDetection(depthAnalysis, touchCalibrations[0]);
        armDetection = (ArmDetection) touchDetections[0];

        touchDetections[1] = new HandDetection(depthAnalysis, touchCalibrations[1]);
        handDetection = (HandDetection) touchDetections[1];

        touchDetections[2] = new FingerDetection(depthAnalysis, touchCalibrations[2]);
        fingerDetection = (FingerDetection) touchDetections[2];
        touchDetectionsReady = true;
        return touchDetections[2];
    }

    private void checkCalibrations() {
        if (simpleTouchCalibration == null || touchCalibrations[0] == null) {
            loadConfiguration();
        }
        touchDetectionsReady = true;
    }

    private void initDepthAnalysis() {
        depthAnalysis.initWithCalibrations(depthCameraDevice);
        depthAnalysisReady = true;
        System.out.println("Init depth analysis");
    }

    @Override
    public void update() {
        Instant initUpdate = Instant.now();
        try {
            
//            System.out.println("Update " + parent.millis());
            IplImage depthImage;
            IplImage colImage = null;

            if (depthCameraDevice.getMainCamera().isUseColor()) {
                colImage = depthCameraDevice.getColorCamera().getIplImage();
            }
            if (depthCameraDevice.getMainCamera().isUseIR()) {
                colImage = depthCameraDevice.getIRCamera().getIplImage();
            }

            depthImage = depthCameraDevice.getDepthCamera().getIplImage();

            if (depthImage == null) {
                return;
            }

           lockDepthData();
            if (!depthAnalysisReady) {
                initDepthAnalysis();
            }

            int initPrecision = 3;

            // Use simple detection's precision
            if (simpleDetection != null && handDetection == null) {
                initPrecision = simpleDetection.getPrecision();
            }

            if (armDetection != null) {
                initPrecision = armDetection.getPrecision();
            }
//            System.out.println("ComputeDepthNormals: " + initPrecision);
            Instant start = Instant.now();

//            depthAnalysis.computeDepthAndNormals(depthImage, colImage, initPrecision);
            depthAnalysis.computeDepth(depthImage, colImage, initPrecision);
            Instant depth = Instant.now();

            if (simpleDetection != null) {
                simpleDetection.findTouch(planeAndProjCalibration);
            }
            if (objectDetection != null) {
                objectDetection.findTouch(planeAndProjCalibration);
            }

            if (armDetection != null) {
                armDetection.findTouch(planeAndProjCalibration);
            }
            Instant touch1 = Instant.now();

            if (handDetection != null) {
                handDetection.findTouch(armDetection, planeAndProjCalibration);
            }
            Instant touch2 = Instant.now();

            if (fingerDetection != null) {
                fingerDetection.findTouch(handDetection, armDetection, colImage, planeAndProjCalibration);
            }
            Instant touch3 = Instant.now();
            Instant end = Instant.now();

//            System.out.println("Depth: " + Duration.between(start, depth).toMillis() + " milliseconds");
//            System.out.println("Arm: " + Duration.between(depth, touch1).toMillis() + " milliseconds");
//            System.out.println("hand: " + Duration.between(touch1, touch2).toMillis() + " milliseconds");
//            System.out.println("finger: " + Duration.between(touch2, touch3).toMillis() + " milliseconds");
//            System.out.println("Total: " + Duration.between(start, end).toMillis() + " milliseconds");
//            System.out.println("Arm touch: " + armDetection.getTouchPoints().size());
//        } catch (InterruptedException ex) {
//            Logger.getLogger(DepthTouchInput.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            releaseDepthData();
        }
//        Instant endUpdate = Instant.now();
//       System.out.println("Total update: " + Duration.between(initUpdate, endUpdate).toMillis() + " milliseconds");

    }

    private static final Touch INVALID_TOUCH = new Touch();

    @Deprecated
    @Override
    public TouchList projectTouchToScreen(PaperScreen screen, BaseDisplay display) {

        TouchList touchList = new TouchList();
        // Not initialized
        if (fingerDetection == null && armDetection == null && simpleDetection == null) {
            return touchList;
        }

//        lock();
        tryToAddTouchs(fingerDetection, touchList, screen, display);
        tryToAddTouchs(armDetection, touchList, screen, display);
        tryToAddTouchs(simpleDetection, touchList, screen, display);

        return touchList;
    }
    
   @Override
    public TouchList projectTouch(PaperScreen paperScreen, BaseDisplay display, TouchDetection td) {
        return projectTouch(paperScreen, display, (TouchDetectionDepth) td);
    }
    
    public TouchList projectTouch(PaperScreen screen, BaseDisplay display, TouchDetectionDepth touchDetection) {
        TouchList touchList = new TouchList();
        tryToAddTouchs(touchDetection, touchList, screen, display);
        return touchList;
    }

    private void tryToAddTouchs(TouchDetectionDepth detection,
            TouchList touchList,
            PaperScreen screen,
            BaseDisplay display) {

        if (detection != null) {
            ArrayList<TrackedDepthPoint> list = new ArrayList<>(detection.getTouchPoints());
            for (TrackedDepthPoint tp : list) {
                Touch touch = createTouch(screen, display, tp);
                if (touch != INVALID_TOUCH) {
                    touchList.add(touch);
                }
            }
        }
    }

    @Override
    public Touch projectTouch(PaperScreen paperScreen, BaseDisplay display, TrackedElement e) {
        return createTouch(paperScreen, display, (TrackedDepthPoint) e);
    }

    private Touch createTouch(PaperScreen screen, BaseDisplay display, TrackedDepthPoint tp) {
        Touch touch = tp.getTouch();
        boolean hasProjectedPos = projectAndSetPositionAndSpeed(screen, display, touch, tp);
        if (!hasProjectedPos) {
            return INVALID_TOUCH;
        }
        touch.isGhost = tp.isToDelete();
        touch.is3D = tp.is3D();
        touch.trackedSource = tp;
        return touch;
    }

    // TODO: Raw Depth is for Kinect Only, find a cleaner solution.
//    private ProjectiveDeviceP pdp;
//    private boolean useRawDepth = false;
    private boolean useRawDepth = false;

    public void useRawDepth() {
        this.useRawDepth = true;
//        this.pdp = camera.getProjectiveDevice();
    }

    private boolean projectAndSetPositionAndSpeed(PaperScreen screen,
            BaseDisplay display,
            Touch touch, TrackedDepthPoint tp) {

        boolean hasProjectedPos = projectAndSetPosition(screen, display, touch, tp);
        if (hasProjectedPos) {
            projectSpeed(screen, display, touch, tp);
        }
        return hasProjectedPos;
    }

    private boolean projectAndSetPosition(PaperScreen screen,
            BaseDisplay display,
            Touch touch, TrackedDepthPoint tp) {

        PVector paperScreenCoord = projectPointToScreen(screen,
                display,
                tp.getPositionKinect(),
                MathUtils.toVec(tp.getPosition()));

        if (paperScreenCoord != null) {
            touch.setPosition(paperScreenCoord);
//                System.out.println("Touch update: " + paperScreenCoord +  " " + tp.getPositionKinect());
//        } else {
//            System.out.println("Touch: No update possible");
        }
        return paperScreenCoord != null && paperScreenCoord != NO_INTERSECTION;
    }

    private boolean projectSpeed(PaperScreen screen,
            BaseDisplay display,
            Touch touch, TrackedDepthPoint tp) {

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

    /**
     * *
     *
     * @param screen
     * @param display
     * @param dde
     * @return the projected point, NULL if no intersection was found.
     */
    public PVector projectPointToScreen(PaperScreen screen,
            BaseDisplay display, DepthDataElementProjected dde) {

        PVector out = this.projectPointToScreen(screen,
                display,
                dde.depthPoint,
                dde.projectedPoint);
        return out;
    }

    // PaperScreen coordinates as computed here. 
    private PVector projectPointToScreen(PaperScreen screen,
            BaseDisplay display, Vec3D pKinect, Vec3D pNorm) {

        PVector paperScreenCoord;
        if (useRawDepth) {

            // Method 1  -> Loose information of Depth !
            // Stays here, might be used later.
//            PVector p = pdp.worldToPixelCoord(pKinect);
//            paperScreenCoord = project(screen, display,
//                    p.x / (float) pdp.getWidth(),
//                    p.y / (float) pdp.getHeight());
            // This works well in the best of worlds, where the depth information is 
            // reliable. 
            paperScreenCoord = new PVector();
            PVector pKinectP = new PVector(pKinect.x, pKinect.y, pKinect.z);

            // maybe not here... 
            // TODO: maybe a better way to this, or to tweak the magic numbers. 
//            // yOffset difference ? 1cm  -> surface view. 
//            // zOffset difference ? 1cm  -> surface view. 
//            pKinectP.y -= 10;
//            pKinectP.z += 10;
            // TODO: Here change the display.getCamera() to 
            // another way to get the screen location... 
            PMatrix3D transfo = screen.getLocation(display.getCamera());
//            if (display instanceof ProjectorDisplay) {
//                ProjectorDisplay proj = (ProjectorDisplay) display;
//                transfo.apply(proj.getExtrinsicsInv());
//            }
            PVector depthColorCam = new PVector();
            depthCameraDevice.getStereoCalibration().mult(pKinectP, depthColorCam);

            // TODO: ADD the depth camera extrinsics
            transfo.invert();
            transfo.mult(depthColorCam, paperScreenCoord);

            // TODO: check bounds too ?!
        } else {

//            // other possib, we know the 3D Point -> screen transfo... 
//            PMatrix3D kinectExtrinsics = kinectDevice.getDepthCamera().getExtrinsics().get();
//            kinectExtrinsics.invert();
//            PMatrix3D paperLocation = screen.getLocation(display.getCamera()).get();
//            paperLocation.invert();
//            Vec3D depthPoint = pKinect;
//            PVector pointPosExtr = new PVector();
//            PVector pointPosDisplay = new PVector();
//            kinectExtrinsics.mult(new PVector(depthPoint.x,
//                    depthPoint.y,
//                    depthPoint.z),
//                    pointPosExtr);
//            paperLocation.mult(pointPosExtr,
//                    pointPosDisplay);
//            System.out.println("new: " + pointPosDisplay);
//            return pointPosDisplay;
            // Not ready yet...
// This is not working with raw Depth, because the coordinates
            // of pNorm is not in display Space, but in a custom space
            // defined for the touch surface... 
            paperScreenCoord = display.project(screen,
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

        // FingerDetection can be not initialized
        if (fingerDetection != null) {
            getTouchColors(colorImage, fingerDetection.getTouchPoints());
        }
    }

    public void getTouchColors(IplImage colorImage,
            ArrayList<TrackedDepthPoint> touchPointList) {

        if (touchPointList.isEmpty() || colorImage == null) {
            return;
        }
        ByteBuffer cBuff = colorImage.getByteBuffer();

        if (colorImage.nChannels() == 1) {
            for (TrackedDepthPoint tp : touchPointList) {
                int offset = depthAnalysis.getDepthCameraDevice().findMainImageOffset(tp.getPositionKinect());
                int c = cBuff.get(offset);
                tp.setColor((255 & 0xFF) << 24
                        | (c & 0xFF) << 16
                        | (c & 0xFF) << 8
                        | (c & 0xFF));
            }
        }
        if (colorImage.nChannels() == 3) {
            for (TrackedDepthPoint tp : touchPointList) {
                int offset = 3 * depthAnalysis.getDepthCameraDevice().findMainImageOffset(tp.getPositionKinect());

                tp.setColor((255 & 0xFF) << 24
                        | (cBuff.get(offset + 2) & 0xFF) << 16
                        | (cBuff.get(offset + 1) & 0xFF) << 8
                        | (cBuff.get(offset) & 0xFF));
            }
        }
    }

    // Raw versions of the algorithm are providing each points at each time. 
    // no updates, no tracking. 
    public ArrayList<TrackedDepthPoint> find2DTouchRaw() {
        return fingerDetection.compute(depthAnalysis.getDepthData());
    }

    public ArrayList<TrackedDepthPoint> find3DTouchRaw(int skip) {
        return armDetection.compute(depthAnalysis.getDepthData());
    }

    @Deprecated
    protected void findAndTrack2D() {
        assert (touch2DPrecision != 0);
        ArrayList<TrackedDepthPoint> newList = fingerDetection.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(fingerDetection.getTouchPoints(), newList,
                parent.millis());
    }

    @Deprecated
    protected void findAndTrack3D() {
        assert (touch3DPrecision != 0);
        ArrayList<TrackedDepthPoint> newList = armDetection.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(armDetection.getTouchPoints(),
                newList,
                parent.millis());
    }

    @Deprecated
    public ArrayList<TrackedDepthPoint> getTouchPoints2D() {
//        return simpleDetection.getTouchPoints();
        return fingerDetection.getTouchPoints();
    }

    @Deprecated
    public ArrayList<TrackedDepthPoint> getTouchPoints3D() {
//        return simpleDetection.getTouchPoints();
        return armDetection.getTouchPoints();
    }

    public ArrayList<TrackedDepthPoint> getTrackedDepthPoints2D() {
        if (fingerDetection == null) {
//            System.err.println("No 2D touch tracking.");
            return new ArrayList<>();
        }
//        return simpleDetection.getTouchPoints();
        return fingerDetection.getTouchPoints();
    }

    public ArrayList<TrackedDepthPoint> getTrackedDepthPoints3D() {
        if (armDetection == null) {
//            System.err.println("No 3D touch tracking.");
            return new ArrayList<>();
        }
//        return simpleDetection.getTouchPoints();
        return armDetection.getTouchPoints();
    }

    public PlaneAndProjectionCalibration getCalibration() {
        return planeAndProjCalibration;
    }

    public boolean isUseRawDepth() {
        return useRawDepth;
    }

    public void setPlaneAndProjCalibration(PlaneAndProjectionCalibration papc) {
        this.planeAndProjCalibration = papc;
    }

    public void setTouchDetectionCalibration(int i, PlanarTouchCalibration touchCalib) {
        touchCalibrations[i] = touchCalib;
    }

    public void setSimpleTouchDetectionCalibration(PlanarTouchCalibration touchCalib) {
        simpleTouchCalibration = touchCalib;
    }

    public void setObjectTouchDetectionCalibration(PlanarTouchCalibration touchCalib) {
        objectTouchCalibration = touchCalib;
    }

    public ObjectDetection getObjectDetection() {
        return objectDetection;
    }

    public FingerDetection getFingerDetection() {
        return fingerDetection;
    }

    public ArmDetection getArmDetection() {
        return armDetection;
    }

    public HandDetection getHandDetection() {
        return handDetection;
    }

    public Simple2D getSimpleDetection() {
        return simpleDetection;
    }

    @Deprecated
    public TouchDetectionDepth getTouchDetection(int i) {
        return touchDetections[i];
    }

    @Deprecated
    public TouchDetectionDepth[] getTouchDetections() {
        return touchDetections;
    }

    @Deprecated
    public FingerDetection getTouchDetection2D() {
        return fingerDetection;
    }

    @Deprecated
    public ArmDetection getTouchDetection3D() {
        return armDetection;
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

    public void lockDepthData() {
        try {
            depthDataSem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(DepthTouchInput.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void releaseDepthData() {
        depthDataSem.release();
    }

    public DepthAnalysisImpl getDepthAnalysis() {
        return depthAnalysis;
    }

    public Vec3D[] getDepthPoints() {
        return depthAnalysis.getDepthPoints();
    }

 
}
