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

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.DepthPoint;
import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.multitouch.detection.Simple2D;
import fr.inria.papart.multitouch.detection.Simple3D;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.utils.MathUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import static processing.core.PApplet.println;
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
    private final DepthCameraDevice kinectDevice;

    private PlaneAndProjectionCalibration planeAndProjCalibration;

    private Simple2D touchDetection2D;
    private Simple3D touchDetection3D;

    private PlanarTouchCalibration touchCalib2D;
    private PlanarTouchCalibration touchCalib3D;

    public DepthTouchInput(PApplet applet,
            DepthCameraDevice kinectDevice,
            DepthAnalysisImpl depthAnalysis,
            PlaneAndProjectionCalibration calibration) {
        this.parent = applet;
        this.depthAnalysis = depthAnalysis;
        this.kinectDevice = kinectDevice;
        this.planeAndProjCalibration = calibration;
    }

    public void setPlaneAndProjCalibration(PlaneAndProjectionCalibration papc) {
        this.planeAndProjCalibration = papc;
    }

    public void setTouchDetectionCalibration(PlanarTouchCalibration touchCalib) {
        touchCalib2D = touchCalib;
    }

    public void setTouchDetectionCalibration3D(PlanarTouchCalibration touchCalib) {
//        this.touchDetection3D.setCalibration(touchCalib);
        touchCalib3D = touchCalib;
    }

    @Override
    public void update() {
        try {
            IplImage depthImage;
            IplImage colImage = null;

            if (kinectDevice.getMainCamera().isUseColor()) {
                colImage = kinectDevice.getColorCamera().getIplImage();
            }
            if (kinectDevice.getMainCamera().isUseIR()) {
                colImage = kinectDevice.getIRCamera().getIplImage();
            }

            depthImage = kinectDevice.getDepthCamera().getIplImage();

            if (depthImage == null) {
                return;
            }

            depthDataSem.acquire();

            // Allocate the data when everything else is ready. 
            // TODO: all the time ?...
            if (touchDetection2D == null) {
                // First run, get calibrations from device after start.
                depthAnalysis.initWithCalibrations(kinectDevice);

                touchDetection2D = new Simple2D(depthAnalysis);
                touchDetection3D = new Simple3D(depthAnalysis);

                touchDetection2D.setCalibration(touchCalib2D);
                touchDetection3D.setCalibration(touchCalib3D);
            }

            touch2DPrecision = touchDetection2D.getPrecision();
            touch3DPrecision = touchDetection3D.getPrecision();

            if (touch2DPrecision > 0) {
                depthAnalysis.computeDepthAndNormals(depthImage, colImage, touch2DPrecision);
                touchDetection2D.findTouch(planeAndProjCalibration);

                boolean multiple = (touch3DPrecision % touch2DPrecision) == 0;
                if (multiple) {
                    touchDetection3D.findTouch(planeAndProjCalibration);
                    findHands();
//                    testPCA();
                    TouchPointTracker.filterPositions(touchDetection2D.getTouchPoints());
                    TouchPointTracker.filterPositions(touchDetection3D.getTouchPoints());

                }
            } else {
                if (touch3DPrecision > 0) {
                    depthAnalysis.computeDepthAndNormals(depthImage, colImage, touch3DPrecision);
                    touchDetection3D.setCurrentTime(parent.millis());
                    touchDetection3D.findTouch(planeAndProjCalibration);
                    TouchPointTracker.filterPositions(touchDetection3D.getTouchPoints());
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(DepthTouchInput.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            depthDataSem.release();
        }
    }

    private void testPCA() {

        for (TrackedDepthPoint pt : touchDetection2D.getTouchPoints()) {

            pt.mainFinger = false;

            // Array of all Points... 
            double[][] dataPoints = new double[pt.getDepthDataElements().size()][3];

//            System.out.println("CC: Size: " + pt.getDepthDataElements().size());
            for (int i = 0; i < pt.getDepthDataElements().size(); i++) {
                DepthDataElementProjected depthPoint = pt.getDepthDataElements().get(i);
                dataPoints[i][0] = depthPoint.depthPoint.x;
                dataPoints[i][1] = depthPoint.depthPoint.y;
                dataPoints[i][2] = depthPoint.depthPoint.z;
            }

// ** each column corresponding to dimension. */
            Matrix trainingData = new Matrix(dataPoints);
            PCA pca = new PCA(trainingData);

            // This seem to work pretty good to identify the fingers. 
            try {
                double e0 = pca.getEigenvalue(0);
                double e1 = pca.getEigenvalue(1);
                double e2 = pca.getEigenvalue(2);

//                System.out.println("Eigen values: " + e0 + " " + e1 + " " + e2);
                if (!(e0 < 10 && e0 < 250
                        && e1 > 10 && e1 < 40
                        && e2 < 50)) {
                    pt.mainFinger = false;
                }

            } catch (Exception e) {
                System.out.println("Error in eigen value computing " + e + " nb points " + dataPoints.length);
            }

            /**
             * Test data to be transformed. The same convention of representing
             * data points as in the training data matrix is used.
             */
//        Matrix testData = new Matrix(new double[][] {
//                {1, 2, 3, 4, 5, 6},
//                {1, 2, 1, 2, 1, 2}});
            /**
             * The transformed test data.
             */
//        Matrix transformedData =
//            pca.transform(testData, PCA.TransformationType.WHITENING);
//        }
        }
    }

    private void findHands() {
        // each 3D component can be hand.
        // Find which one are
        ProjectedDepthData depthData = depthAnalysis.getDepthData();

        // check if the component contains border elements.
        // BORDER METHOD NOT WORKING WITH SR300, arms are not seen in the borders.
//        for (TrackedDepthPoint pt : touchPoints3D) {
//            int borders = 0;
//            for (DepthDataElementProjected depthPoint : pt.getDepthDataElements()) {
//
//                int offset = depthPoint.offset;
//                int x = offset % depthAnalysis.getWidth();
//                int y = offset / depthAnalysis.getWidth();
//                if (x  <= touch3DPrecision * 3 ||
//                        x >= depthAnalysis.getWidth() - (1 + touch3DPrecision * 3) ||
//                        y <= touch3DPrecision * 3 ||
//                        y >= depthAnalysis.getHeight() - (1+ touch3DPrecision * 3)) {
//                    borders++;
//                }
//            }
//            System.out.println("Borders: " + borders + " " +  depthAnalysis.getWidth() + " " + depthAnalysis.getHeight());
//        }
        ArrayList<ArrayList<Integer>> offset3D = new ArrayList<>();
        for (TrackedDepthPoint pt : touchDetection3D.getTouchPoints()) {
            pt.clearFingers();

            ArrayList<Integer> offsets = new ArrayList<>();
            for (DepthDataElementProjected depthPoint : pt.getDepthDataElements()) {
                int offset = depthPoint.offset;
                offsets.add(offset);
            }
            offset3D.add(offsets);
//            System.out.println("x: " + xOffset + " y: " + yOffset + " z: " + zOffset);
        }

        ArrayList<ArrayList<Integer>> offset2D = new ArrayList<>();

        int fingerID = 0;
        for (TrackedDepthPoint pt : touchDetection2D.getTouchPoints()) {

            pt.mainFinger = false;

            ArrayList<Integer> offsets = new ArrayList<>();
            for (DepthDataElementProjected depthPoint : pt.getDepthDataElements()) {
                int offset = depthPoint.offset;
                offsets.add(offset);
            }
            offset2D.add(offsets);

            // Now check if they have a 3D in common.
            int handID = 0;
            for (ArrayList<Integer> touch3DOffsets : offset3D) {
                ArrayList<Integer> copy = new ArrayList<>();
                copy.addAll(offsets);

                copy.retainAll(touch3DOffsets);
//                System.out.println("Common: " + copy.size());
                if (copy.size() > 1) {
                    pt.setAttachedHandID(handID);
                    touchDetection3D.getTouchPoints().get(handID).addFinger(fingerID);
//                    System.out.println("Points in common : " + copy.size());
                }
                handID++;
            }
            fingerID++;
        }

        // For each potential «hand»
        for (TrackedDepthPoint hand : touchDetection3D.getTouchPoints()) {
            ArrayList<Integer> fingers = hand.getFingers();
            if (fingers.size() == 0) {
                continue;
            }
            float maxDist = Float.MIN_VALUE;
            int minID = 0;

            // find the further finger
//            System.out.println("nb fingers: " + fingers.size() + " nbTouchPoints " + touchDetection2D.getTouchPoints().size());
            for (int i = 0; i < fingers.size(); i++) {

                // Refine all the touches
                TrackedDepthPoint finger = touchDetection2D.getTouchPoints().get(fingers.get(i));

                finger.refineTouchWithHand(hand.getPositionDepthCam(), depthData);
                float dist = finger.distanceTo(hand);
                if (dist > maxDist) {
                    maxDist = dist;
                    minID = fingers.get(i);
                }
            }

            // set as main
            touchDetection2D.getTouchPoints().get(minID).setMainFinger();

            // move outwards from the hand by x  20 mm.
            Vec3D fingerPosition = touchDetection2D.getTouchPoints().get(minID).getPositionKinect();

            if (touchDetection2D.getTouchPoints().get(minID).isUpdated) {
                Vec3D handPosition = hand.getPositionKinect();

                Vec3D addedVec = fingerPosition.copy();
                Vec3D dist = addedVec.sub(handPosition).normalize().scale(30);
//            System.out.println("Distance added: " + dist);
                // DISABLED
//                fingerPosition.addSelf(dist);

                // project again. 
                PVector fingerPositionProj = touchDetection2D.getTouchPoints().get(minID).getPosition();
                Vec3D output = new Vec3D();
                depthData.planeAndProjectionCalibration.project(fingerPosition, output);
                // Warning: The touch is updated twice, the speed will be invalid :(

                // Force a position,  so that speed may stay valid  
                // DISABLED
//                touchDetection2D.getTouchPoints().get(minID).getPosition().set(output.x, output.y, output.z);
            }
        }
        // Try to match the hand with the points... 
        // A 3D touch can contain multiple 2D touch or none. 
        // a 2D touch can belong to none or one 3D. 
        // Match the elements in the hand, and the ones in the touch
        // Attach the corresponding hand IDs.
        // Set as "main" touch the one futher away from the hand. 
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

        for (TrackedDepthPoint tp : touchDetection2D.getTouchPoints()) {
            Touch touch = createTouch(screen, display, tp);
            if (touch != INVALID_TOUCH) {
                touchList.add(touch);
            }
        }

        for (TrackedDepthPoint tp : touchDetection3D.getTouchPoints()) {
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

    private Touch createTouch(Screen screen, BaseDisplay display, TrackedDepthPoint tp) {
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

    private boolean projectAndSetPositionAndSpeed(Screen screen,
            BaseDisplay display,
            Touch touch, TrackedDepthPoint tp) {

        boolean hasProjectedPos = projectAndSetPosition(screen, display, touch, tp);
        if (hasProjectedPos) {
            projectSpeed(screen, display, touch, tp);
        }
        return hasProjectedPos;
    }

    private boolean projectAndSetPosition(Screen screen,
            BaseDisplay display,
            Touch touch, TrackedDepthPoint tp) {

        PVector paperScreenCoord = projectPointToScreen(screen,
                display,
                tp.getPositionKinect(),
                MathUtils.toVec(tp.getPreviousPosition()));

        touch.setPosition(paperScreenCoord);

        return paperScreenCoord != NO_INTERSECTION;
    }

    private boolean projectSpeed(Screen screen,
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

//    /**
//     * WARNING: To be deprecated soon.
//     *
//     * @param display
//     * @param screen
//     * @return
//     */
//    public ArrayList<DepthDataElementProjected> getDepthData() {
//        try {
//            depthDataSem.acquire();
//            ProjectedDepthData depthData = depthAnalysis.getDepthData();
//            ArrayList<DepthDataElementProjected> output = new ArrayList<>();
//            ArrayList<Integer> list = depthData.validPointsList3D;
//            for (Integer i : list) {
//                output.add(depthData.getElementKinect(i));
//            }
//            depthDataSem.release();
//            return output;
//
//        } catch (InterruptedException ex) {
//            Logger.getLogger(DepthTouchInput.class
//                    .getName()).log(Level.SEVERE, null, ex);
//
//            return null;
//        }
//    }
//    // TODO: Do the same without the Display, use the extrinsics instead! 
//    // TODO: Do the same with DepthDataElement  instead of  DepthPoint ?
//    /**
//     * WARNING: To be deprecated soon.
//     *
//     * @param display
//     * @param screen
//     * @return
//     */
//    public ArrayList<DepthPoint> projectDepthData(ARDisplay display, Screen screen) {
//        ArrayList<DepthPoint> list = projectDepthData2D(display, screen);
//        list.addAll(projectDepthData3D(display, screen));
//        return list;
//    }
//
//    public ArrayList<DepthPoint> projectDepthData2D(ARDisplay display, Screen screen) {
//        return projectDepthDataXD(display, screen, true);
//    }
//
//    public ArrayList<DepthPoint> projectDepthData3D(ARDisplay display, Screen screen) {
//        return projectDepthDataXD(display, screen, false);
//    }
//
//    private ArrayList<DepthPoint> projectDepthDataXD(ARDisplay display, Screen screen, boolean is2D) {
//        try {
//            depthDataSem.acquire();
//            ProjectedDepthData depthData = depthAnalysis.getDepthData();
//            ArrayList<DepthPoint> projected = new ArrayList<DepthPoint>();
//            ArrayList<Integer> list = is2D ? depthData.validPointsList : depthData.validPointsList3D;
//            for (Integer i : list) {
//                DepthPoint depthPoint = tryCreateDepthPoint(display, screen, i);
//                if (depthPoint != null) {
//                    projected.add(depthPoint);
//                }
//            }
//            depthDataSem.release();
//            return projected;
//
//        } catch (InterruptedException ex) {
//            Logger.getLogger(DepthTouchInput.class
//                    .getName()).log(Level.SEVERE, null, ex);
//
//            return null;
//        }
//    }
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
            BaseDisplay display, DepthDataElementProjected dde) {

        PVector out = this.projectPointToScreen(screen,
                display,
                dde.depthPoint,
                dde.projectedPoint);
        return out;
    }

    // PaperScreen coordinates as computed here. 
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
        getTouchColors(colorImage, touchDetection2D.getTouchPoints());
    }

    public void getTouchColors(IplImage colorImage,
            ArrayList<TrackedDepthPoint> touchPointList) {

        if (touchPointList.isEmpty()) {
            return;
        }
        ByteBuffer cBuff = colorImage.getByteBuffer();

        if (colorImage.nChannels() == 1) {
            for (TrackedDepthPoint tp : touchPointList) {
                int offset = depthAnalysis.getDepthCameraDevice().findColorOffset(tp.getPositionKinect());
                int c = cBuff.get(offset);
                tp.setColor((255 & 0xFF) << 24
                        | (c & 0xFF) << 16
                        | (c & 0xFF) << 8
                        | (c & 0xFF));
            }
        }
        if (colorImage.nChannels() == 3) {
            for (TrackedDepthPoint tp : touchPointList) {
                int offset = 3 * depthAnalysis.getDepthCameraDevice().findColorOffset(tp.getPositionKinect());

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
        return touchDetection2D.compute(depthAnalysis.getDepthData());
    }

    public ArrayList<TrackedDepthPoint> find3DTouchRaw(int skip) {
        return touchDetection3D.compute(depthAnalysis.getDepthData());
    }

    protected void findAndTrack2D() {
        assert (touch2DPrecision != 0);
        ArrayList<TrackedDepthPoint> newList = touchDetection2D.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(touchDetection2D.getTouchPoints(), newList,
                parent.millis());
    }

    protected void findAndTrack3D() {
        assert (touch3DPrecision != 0);
        ArrayList<TrackedDepthPoint> newList = touchDetection3D.compute(
                depthAnalysis.getDepthData());
        TouchPointTracker.trackPoints(touchDetection3D.getTouchPoints(),
                newList,
                parent.millis());
    }

    @Deprecated
    public ArrayList<TrackedDepthPoint> getTouchPoints2D() {
        return touchDetection2D.getTouchPoints();
    }

    @Deprecated
    public ArrayList<TrackedDepthPoint> getTouchPoints3D() {
        return touchDetection3D.getTouchPoints();
    }

    public ArrayList<TrackedDepthPoint> getTrackedDepthPoints2D() {
        if (touchDetection2D == null) {
            System.err.println("No 2D touch tracking.");
            return new ArrayList<>();
        }
        return touchDetection2D.getTouchPoints();
    }

    public ArrayList<TrackedDepthPoint> getTrackedDepthPoints3D() {
        if (touchDetection3D == null) {
            System.err.println("No 3D touch tracking.");
            return new ArrayList<>();
        }
        return touchDetection3D.getTouchPoints();
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
