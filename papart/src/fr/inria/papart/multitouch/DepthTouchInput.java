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
import fr.inria.papart.multitouch.detection.ArmDetection;
import fr.inria.papart.multitouch.detection.FingerDetection;
import fr.inria.papart.multitouch.detection.HandDetection;
import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.utils.ImageUtils;
import fr.inria.papart.utils.MathUtils;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import static org.bytedeco.javacpp.helper.opencv_imgproc.cvFindContours;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_WHOLE_SEQ;
import org.bytedeco.javacpp.opencv_core.CvBox2D;
import org.bytedeco.javacpp.opencv_core.CvContour;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.CvSize2D32f;
import org.bytedeco.javacpp.opencv_core.IplROI;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvCvtSeqToArray;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import org.bytedeco.javacpp.opencv_imgproc;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RETR_LIST;
import static org.bytedeco.javacpp.opencv_imgproc.cvCanny;
import static org.bytedeco.javacpp.opencv_imgproc.cvContourArea;
import static org.bytedeco.javacpp.opencv_imgproc.cvDilate;
import static org.bytedeco.javacpp.opencv_imgproc.cvErode;
import static org.bytedeco.javacpp.opencv_imgproc.cvMinAreaRect2;
import static org.bytedeco.javacpp.opencv_imgproc.cvMinAreaRect2;
import static org.bytedeco.javacpp.opencv_imgproc.cvMinAreaRect2;
import static org.bytedeco.javacpp.opencv_imgproc.cvSmooth;
import static org.bytedeco.javacpp.opencv_imgproc.cvSmooth;
import processing.core.PApplet;
import static processing.core.PApplet.println;
import static processing.core.PConstants.ALPHA;
import static processing.core.PConstants.RGB;
import processing.core.PImage;
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
        this.kinectDevice = kinectDevice;
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

    public void initTouchDetections() {
        // First run, get calibrations from device after start.
        depthAnalysis.initWithCalibrations(kinectDevice);

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

            if (!touchDetectionsReady) {
                initTouchDetections();
            }

            int initPrecision = 2;
            depthAnalysis.computeDepthAndNormals(depthImage, colImage, initPrecision);

//                if (armDetection.getCalibration().getPrecision() % initPrecision == 0) {
            armDetection.findTouch(planeAndProjCalibration);

//                    if (handDetection.getCalibration().getPrecision() % initPrecision == 0) {
            handDetection.findTouch(armDetection, planeAndProjCalibration);

            
            fingerDetection.findTouch(handDetection, colImage, planeAndProjCalibration);
            // last step, get the color/IR image.
//                        ArrayList<TrackedDepthPoint> touchPoints = touchDetection3D.getTouchPoints();
            // Get the IR image to refine the touch points...? 
//                        ArrayList<TrackedDepthPoint> touchPoints = touchDetectionRefine.getTouchPoints();
//                        if (!touchPoints.isEmpty()) {
//                            TrackedDepthPoint pt0 = touchPoints.get(0);
//                            // find the contours in this image. 
//                            Vec3D fingerPosition = pt0.getPositionDepthCam();
//                            Vec3D handPosition = touchDetection3D.getTouchPoints().get(0).getPositionDepthCam();
//                            Vec3D addedVec = fingerPosition.copy();
//                            Vec3D dist = addedVec.sub(handPosition).normalize().scale(30);
////                // DISABLED
//                            fingerPosition.addSelf(dist);
//
//                            setROI(colImage, pt0, (int) touchDetectionRefine.getCalibration().getTest5());
//                        }

//                touchDetection2D.findTouch(planeAndProjCalibration, touchDetection3D);
//                touchDetection2D.findTouch(planeAndProjCalibration, touchDetection3D);
//                refineFingers();
//                findHands();
        } catch (InterruptedException ex) {
            Logger.getLogger(DepthTouchInput.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            depthDataSem.release();
        }
    }

    private void setROI(IplImage image, TrackedDepthPoint pt0, int imageSize) {

        ProjectiveDeviceP projectiveDevice = kinectDevice.getIRCamera().getProjectiveDevice();
//        ProjectiveDeviceP projectiveDevice;
//        if (kinectDevice.getMainCamera().isUseIR()) {

//        } else {
//            projectiveDevice = kinectDevice.getColorCamera().getProjectiveDevice();
//        }
        int offset = this.depthAnalysis.getDepthCameraDevice().findMainImageOffset(pt0.getPositionDepthCam());
        int fingerX = offset % projectiveDevice.getWidth();
        int fingerY = offset / projectiveDevice.getWidth();

        int minX = fingerX - imageSize / 2;
        int minY = fingerY - imageSize / 2;

        if (minX + imageSize >= projectiveDevice.getWidth()) {
            minX = projectiveDevice.getWidth() - imageSize - 1;
        }

        if (minY + imageSize >= projectiveDevice.getHeight()) {
            minY = projectiveDevice.getHeight() - imageSize - 1;
        }

        CvRect roi = cvRect(minX, minY, imageSize, imageSize);
        cvSetImageROI(image, roi);

        IplImage copy = IplImage.create(imageSize, imageSize, 8, 1);
        cvCopy(image, copy);

        IplImage dst = cvCreateImage(cvGetSize(copy), copy.depth(), 1);

//        opencv_imgproc.blur(new Mat(copy), new Mat(dst), new Size(2, 2));
//        cvSmooth(copy, dst);  // OK
        opencv_imgproc.medianBlur(new opencv_core.Mat(copy), new opencv_core.Mat(dst), 3);

//        colorDst = cvCreateImage(cvGetSize(copy), copy.depth(), 3);
        cvCanny(dst, dst,
                handDetection.getCalibration().getTest3(),
                handDetection.getCalibration().getTest4(),
                7);

        int morph_size = 1;
        Mat element = opencv_imgproc.getStructuringElement(0, new Size(2 * morph_size + 1, 2 * morph_size + 1), new Point(morph_size, morph_size));
        opencv_imgproc.dilate(new Mat(dst), new Mat(dst), element);
        opencv_imgproc.erode(new Mat(dst), new Mat(dst), element);
//        opencv_imgproc.dilate(new Mat(dst), new Mat(dst));
//        opencv_imgproc.erode(new Mat(dst), new Mat(dst));
//        cvDilate(dst, dst);
//        cvErode(dst, dst);

        //  Contour computations...
//        contourList.clear();
//        hullList.clear();
//
//        CvMemStorage storage = CvMemStorage.create();
//        opencv_core.CvSeq contours = new opencv_core.CvContour(null);
//        cvFindContours(dst, storage, contours, Loader.sizeof(opencv_core.CvContour.class),
//                CV_RETR_EXTERNAL, CV_CHAIN_APPROX_NONE);
//
//        CvSeq bigContour = null;
//        // find the largest contour in the list based on bounded box size
//        float maxArea = SMALLEST_AREA;
//        CvBox2D maxBox = null;
//        while (contours != null && !contours.isNull()) {
//            if (contours.elem_size() > 0) {
//                CvBox2D box = cvMinAreaRect2(contours, contourStorage);
//                if (box != null) {
//                    CvSize2D32f size = box.size();
//                    float area = size.width() * size.height();
//                    if (area > maxArea) {
//                        maxArea = area;
//                        bigContour = contours;
//                    }
//                }
//            }
//            contours = contours.h_next();
//        }
//
//        contourPointsSize = bigContour.total();
//        if (contourPoints == null || contourPoints.capacity() < contourPointsSize) {
//            contourPoints = new opencv_core.CvPoint(contourPointsSize);
//            contourPointsBuffer = contourPoints.asByteBuffer().asIntBuffer();
//        }
//        cvCvtSeqToArray(bigContour, contourPoints.position(0));
//
//        double m00 = 0, m10 = 0, m01 = 0;
//        for (int i = 0; i < contourPointsSize; i++) {
//            int x = contourPointsBuffer.get(2 * i);
//            int y = contourPointsBuffer.get(2 * i + 1);
//            contourList.add(new PVector(x, y, 0));
//        }
        out = parent.createImage(imageSize, imageSize, ALPHA);
        ImageUtils.IplImageToPImage(dst, out);

//        ImageUtils.IplImageToPImage(copy, out);
    }

    // DEBUG
    public PImage out;
    public ArrayList<PVector> contourList = new ArrayList<>();
    public ArrayList<PVector> hullList = new ArrayList<>();

    private IplImage relativeResidual = null, binaryImage = null;
    private CvMemStorage storage = CvMemStorage.create();
    private int contourPointsSize = 0;
    private IntPointer intPointer = new IntPointer(1);
    private opencv_core.CvPoint contourPoints = null;
    private opencv_core.CvPoint hullPoints = null;
    private IntBuffer contourPointsBuffer = null;
    private IntBuffer hullPointsBuffer = null;

    // globals
    private static final float SMALLEST_AREA = 10f;
    // ignore smaller contour areas
    private CvMemStorage contourStorage;

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

    private void refineFingers() {
        ProjectedDepthData depthData = depthAnalysis.getDepthData();

        Iterator<TrackedDepthPoint> it = fingerDetection.getTouchPoints().iterator();
        while (it.hasNext()) {
            TrackedDepthPoint touch = it.next();
//            if (!touch.refineTouchAlongNormal(depthData)) {
//                it.remove();
//            } else {
//                if (!filterPCA(touch)) {
//                    it.remove();
//                }
//            }
        }
    }

    private boolean filterPCA(TrackedDepthPoint pt) {
        // Array of all Points... 
        double[][] dataPoints = new double[pt.getDepthDataElements().size()][3];

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

//            System.out.println("Eigen values: " + e0 + " " + e1 + " " + e2);
            return e0 > 20
                    && e0 < 500
                    && e1 > 1
                    && e1 < 200;

        } catch (Exception e) {
            return false;
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
        for (TrackedDepthPoint pt : armDetection.getTouchPoints()) {
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

        // Get the points from the fingers. 
        int fingerID = 0;
        for (TrackedDepthPoint pt : fingerDetection.getTouchPoints()) {

            // Reset main finger 
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

                // AT least one point !
//                System.out.println("Common: " + copy.size());
                if (copy.size() >= 1) {
                    pt.setAttachedHandID(handID);
                    armDetection.getTouchPoints().get(handID).addFinger(fingerID);
//                    System.out.println("Points in common : " + copy.size());
                }
                handID++;
            }
            fingerID++;
        }

        // For each potential «hand»
        for (TrackedDepthPoint hand : armDetection.getTouchPoints()) {
            ArrayList<Integer> fingers = hand.getFingers();
            if (fingers.isEmpty()) {
                continue;
            }
            float maxDist = Float.MIN_VALUE;
            int minID = 0;

//            hand.filter(depthData.timeStamp);
            // find the further finger
//            System.out.println("nb fingers: " + fingers.size() + " nbTouchPoints " + touchDetection2D.getTouchPoints().size());
            for (int i = 0; i < fingers.size(); i++) {

                // Refine all the touches
                TrackedDepthPoint finger = fingerDetection.getTouchPoints().get(fingers.get(i));

                // Select the points away from the hand.
//                finger.refineTouchWithHand(hand.getPositionDepthCam(), depthData);
//                finger.filter(depthData.timeStamp);
                float dist = finger.distanceTo(hand);
//                System.out.println("fDist: " + dist);
                if (dist > maxDist) {
                    maxDist = dist;
                    minID = i;
                }
            }

            TrackedDepthPoint furtherAwayFinger = fingerDetection.getTouchPoints().get(fingers.get(minID));
            // set as main  -- Disabled for debug
            furtherAwayFinger.setMainFinger();

            // move outwards from the hand by x  20 mm.
//            Vec3D fingerPosition = furtherAwayFinger.getPositionKinect();
//            // Move the points towards the opposite size
//            if (furtherAwayFinger.isUpdated) {
//                Vec3D handPosition = hand.getPositionKinect();
//                Vec3D addedVec = fingerPosition.copy();
//                Vec3D dist = addedVec.sub(handPosition).normalize().scale(30);
////            System.out.println("Distance added: " + dist);
//                // DISABLED
//                fingerPosition.addSelf(dist);
//                // project again. 
//                PVector fingerPositionProj = furtherAwayFinger.getPosition();
//                Vec3D output = new Vec3D();
////                depthData.planeAndProjectionCalibration.project(fingerPosition, output);
//                // Warning: The touch is updated twice, the speed will be invalid :(
//                // Force a position,  so that speed may stay valid  
//                // DISABLED
////                furtherAwayFinger.getPosition().set(output.x, output.y, output.z);
//            }
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
        getTouchColors(colorImage, fingerDetection.getTouchPoints());
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

}
