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
import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.utils.MathUtils;
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

    private FingerDetection fingerDetection;
    private ArmDetection armDetection;
    private HandDetection handDetection;
    private TouchDetectionDepth touchDetections[] = new TouchDetectionDepth[3];

    private PlanarTouchCalibration touchCalibrations[] = new PlanarTouchCalibration[3];

    public DepthTouchInput(PApplet applet,
            DepthCameraDevice kinectDevice,
            DepthAnalysisImpl depthAnalysis,
            PlaneAndProjectionCalibration calibration) {
        this.parent = applet;
        this.depthAnalysis = depthAnalysis;
        this.depthCameraDevice = kinectDevice;
        this.planeAndProjCalibration = calibration;
    }

    public void setPlaneAndProjCalibration(PlaneAndProjectionCalibration papc) {
        this.planeAndProjCalibration = papc;
    }

    public void setTouchDetectionCalibration(int i, PlanarTouchCalibration touchCalib) {
        touchCalibrations[i] = touchCalib;
    }

    public TouchDetectionDepth getTouchDetection(int i) {
        return touchDetections[i];
    }

    public TouchDetectionDepth[] getTouchDetections() {
        return touchDetections;
    }

    public FingerDetection getTouchDetection2D() {
        return fingerDetection;
    }

    public ArmDetection getTouchDetection3D() {
        return armDetection;
    }

    private boolean touchDetectionsReady = false;

    @Override
    public boolean isReady() {
        return touchDetectionsReady;
    }

    public void initTouchDetections() {
        // First run, get calibrations from device after start.
        depthAnalysis.initWithCalibrations(depthCameraDevice);

        touchDetections[0] = new ArmDetection(depthAnalysis, touchCalibrations[0]);
        armDetection = (ArmDetection) touchDetections[0];

        touchDetections[1] = new HandDetection(depthAnalysis, touchCalibrations[1]);
        handDetection = (HandDetection) touchDetections[1];

        touchDetections[2] = new FingerDetection(depthAnalysis, touchCalibrations[2]);
        fingerDetection = (FingerDetection) touchDetections[2];

        touchDetectionsReady = true;
    }

    @Override
    public void update() {
        try {
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

            depthDataSem.acquire();

            if (!touchDetectionsReady) {
                initTouchDetections();
            }

            int initPrecision = 3;
//            Instant start = Instant.now();
            
            depthAnalysis.computeDepthAndNormals(depthImage, colImage, initPrecision);
            
//            Instant depth = Instant.now();
            
            armDetection.findTouch(planeAndProjCalibration);
            
//            Instant touch1 =  Instant.now();
            handDetection.findTouch(armDetection, planeAndProjCalibration);
                 
//            Instant touch2 =  Instant.now();
            fingerDetection.findTouch(handDetection, armDetection, colImage, planeAndProjCalibration);
     
//            Instant touch3 =  Instant.now();
//            Instant end = Instant.now();
//            
//            
//            System.out.println("Depth: " +  Duration.between(start, depth).toMillis() + " milliseconds");
//            System.out.println("Arm: " +  Duration.between(depth, touch1).toMillis() + " milliseconds");
//            System.out.println("hand: " +  Duration.between(touch1, touch2).toMillis() + " milliseconds");
//            System.out.println("finger: " +  Duration.between(touch2, touch3).toMillis() + " milliseconds");
//            System.out.println("Total: " +  Duration.between(start, end).toMillis() + " milliseconds");

            
        } catch (InterruptedException ex) {
            Logger.getLogger(DepthTouchInput.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            depthDataSem.release();
        }
    }

    private static final Touch INVALID_TOUCH = new Touch();

    @Override
    public TouchList projectTouchToScreen(PaperScreen screen, BaseDisplay display) {

        TouchList touchList = new TouchList();

        // Not initialized
        if (fingerDetection == null || armDetection == null) {
            return touchList;
        }

        try {
            touchPointSemaphore.acquire();
        } catch (InterruptedException ie) {
            System.err.println("Semaphore Exception: " + ie);
        }
        for (TrackedDepthPoint tp : fingerDetection.getTouchPoints()) {
            Touch touch = createTouch(screen, display, tp);
            if (touch != INVALID_TOUCH) {
                touchList.add(touch);
            }
        }

        for (TrackedDepthPoint tp : armDetection.getTouchPoints()) {
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

        touch.setPosition(paperScreenCoord);

        return paperScreenCoord != NO_INTERSECTION;
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
            transfo.invert();
            transfo.mult(pKinectP, paperScreenCoord);

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

        if (touchPointList.isEmpty()) {
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

    protected void findAndTrack2D() {
        assert (touch2DPrecision != 0);
        ArrayList<TrackedDepthPoint> newList = fingerDetection.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(fingerDetection.getTouchPoints(), newList,
                parent.millis());
    }

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
        return fingerDetection.getTouchPoints();
    }

    @Deprecated
    public ArrayList<TrackedDepthPoint> getTouchPoints3D() {
        return armDetection.getTouchPoints();
    }

    public ArrayList<TrackedDepthPoint> getTrackedDepthPoints2D() {
        if (fingerDetection == null) {
            System.err.println("No 2D touch tracking.");
            return new ArrayList<>();
        }
        return fingerDetection.getTouchPoints();
    }

    public ArrayList<TrackedDepthPoint> getTrackedDepthPoints3D() {
        if (armDetection == null) {
            System.err.println("No 3D touch tracking.");
            return new ArrayList<>();
        }
        return armDetection.getTouchPoints();
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
}
