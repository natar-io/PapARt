/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 20017 RealityTech
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
package fr.inria.papart.multitouch.detection;

import Jama.Matrix;
import com.mkobos.pca_transform.PCA;
import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Compute2DFrom3D;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import static fr.inria.papart.multitouch.ConnectedComponent.INVALID_COMPONENT;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.utils.ImageUtils;
import java.util.ArrayList;
import java.util.HashMap;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvRect;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import org.bytedeco.javacpp.opencv_imgproc;
import static org.bytedeco.javacpp.opencv_imgproc.cvCanny;
import static processing.core.PConstants.ALPHA;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 * The touch Points are created from a 3D point cloud (hand or object).
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class FingerDetection extends TouchDetectionDepth {

    private final HashMap<Byte, ConnectedComponent> contactPoints;
    private final Compute2DFrom3D touchRecognition;
    private final CheckTouchPoint pointCheck;
    
    public FingerDetection(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        this.contactPoints = new HashMap<>();
        touchRecognition = new Compute2DFrom3D(depthAnalysisImpl);
        pointCheck = new CheckTouchPoint();
        currentPointValidityCondition = pointCheck;
    }
    

    public class CheckTouchPoint implements PointValidityCondition {

        public ProjectedDepthData getData() {
            return depthData;
        }

        DepthData.DepthSelection localDepthSelection;
        public void setSelection(DepthData.DepthSelection selection) {
            this.localDepthSelection = selection;
        }

        @Override
        public boolean checkPoint(int candidate, int currentPoint) {

            boolean classicCheck = !assignedPoints[candidate] // not assigned  

                    && localDepthSelection.validPointsMask[candidate] // is valid

                    // Lower than the hand.
                    && depthData.planeAndProjectionCalibration.distanceTo(depthData.depthPoints[candidate]) < (handCalib.getTest2() - 0.5f)
                    //                    && depthData.planeAndProjectionCalibration.distanceTo(depthData.depthPoints[candidate]) < 20f
                    && depthData.depthPoints[initialPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[candidate].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance();

            boolean goodNormal = true;

            if (depthData.normals[candidate] != null) {
                float dN = (depthData.planeAndProjectionCalibration.getPlane().normal).distanceToSquared(depthData.normals[candidate]);
                float d1 = (depthData.planeAndProjectionCalibration.getPlane().getDistanceToPoint(depthData.depthPoints[candidate]));
                // WARNING MAGIC NUMBER HERE
//                boolean higher = depthData.projectedPoints[candidate].z < depthData.projectedPoints[currentPoint].z;
                goodNormal = (depthData.normals[candidate] != null && dN > calib.getNormalFilter());  // Higher  than Xmm
            }

//            int[] neighbourColorList = depthData.connexity.getNeighbourColorList(getX(candidate), 
//                    getY(candidate), 
//                    depthData.pointColors);
//            int colorSum = 0;
//            for(int argb : neighbourColorList){
//                  int g = (argb >> 8) & 0xFF;
//                  colorSum += g / 255;
//            }
            int argb = depthData.pointColors[candidate];
//            boolean goodColor = ((depthData.pointColors[candidate] & 0xFF) >> 8) == 255;  // 0xFF = 255
            int r = (argb >> 16) & 0xFF;  // Faster way of getting red(argb)
            int g = (argb >> 8) & 0xFF;   // Faster way of getting green(argb)
            int b = argb & 0xFF;          // Faster way of getting blue(argb)
            int total = r + g + b;

//            System.out.println("sum: " + colorSum);
            return classicCheck && goodNormal;
        }
    }

    int handOffset;
    Vec3D handPos;

    TrackedDepthPoint currentHand;
    TrackedDepthPoint currentArm;
    PlanarTouchCalibration handCalib;
    PlaneAndProjectionCalibration currentPlaneProj;

    /**
     *
     * @param planeAndProjCalibration
     * @param colorImg
     * @param hand
     */
    public void findTouch(HandDetection hand, ArmDetection arm,
            IplImage colorImg,
            PlaneAndProjectionCalibration planeAndProjCalibration) {

        // Warning, this should be done before ?
        this.depthData = depthAnalysis.getDepthData();
        this.handCalib = hand.getCalibration();
        this.currentPlaneProj = planeAndProjCalibration;

        ArrayList<TrackedDepthPoint> allNewFingers = new ArrayList<TrackedDepthPoint>();
        // For each hand
        for (TrackedDepthPoint subHand : hand.getTouchPoints()) {

            int offset = depthAnalysis.getDepthCameraDevice().findDepthOffset(subHand.getPositionDepthCam());
//
            offset = getValidOffset(offset);
            currentHand = subHand;
            currentArm = subHand.getParent();
            handOffset = offset;
            handPos = subHand.getPositionDepthCam();

            int rectSize = (int) this.calib.getTest1();

            // the IRImage here is the modified one. 
            // 1. Compute the contours of the hand.
//            IplImage irImg = computeContour(colorImg, offset, (int) this.calib.getTest1());
            IplImage pointColor = colorImg;
//            IplImage pointColor = irImg;

            // Set the contour Color data. 
            touchRecognition.find2DTouchFrom3D(planeAndProjCalibration,
                    getPrecision(),
                    pointColor,
                    offset,
                    (int) this.calib.getTest2());

            // Use arm 
//            depthSelection = arm.getDepthSelection();
//       use local
            DepthData.DepthSelection depthSelection = touchRecognition.getSelection();
            pointCheck.setSelection(depthSelection);
            
            // Start from the hand -- not the detected points !
            
//            this.toVisit.addAll(depthSelection.validPointsList);
            this.toVisit.addAll(currentArm.getDepthDataAsConnectedComponent());

            // 2. Select points that are part of the border 
//            DepthElementList handBounds = subHand.getDepthDataElements();
//            handBounds =  handBounds.getBoundaries(depthData, touchPoint.getDetection());
            // 3. Select the ones that not part of the contour. 
//            handBounds.selectDark(depthData);
//            System.out.println("Bounds in Hand and not in Edge detection: " + handBounds.size());
            // USELESS?
            // 4. Remove the points that are also in the arm
//            handBounds.removeAll(currentArm.getDepthDataElements());
//            System.out.println("Same not in arm: " + handBounds.size());
            // Start searching from the bounds
//            this.toVisit.addAll(handBounds.toConnectedComponent());
            // Search points "black"  -> not in bounds ?
            // Generate a touch list from these points. 
            ArrayList<TrackedDepthPoint> newList;
            newList = this.compute(this.depthAnalysis.getDepthData());

            // WARNING - FINGERS CAN JUMP FROM ONE HAND TO ANOTHER
            allNewFingers.addAll(newList);
        }

        int imageTime = this.depthAnalysis.getDepthData().timeStamp;

//        touchPoints.clear();
//        touchPoints.addAll(allNewFingers);
        TouchPointTracker.trackPoints(touchPoints, allNewFingers, imageTime);
        TouchPointTracker.filterPositions(touchPoints, imageTime);
    }

    private IplImage irImage = null;
    public PImage irPImage = null;
    private int lastSize = 0;
    opencv_core.IplImage irDetect;

    private IplImage computeContour(opencv_core.IplImage image, int offset, int imageSize) {

        if (irImage == null) {
//            irImage = opencv_core.IplImage.create(image.width(), image.height(), 8, 1);
            irImage = opencv_core.IplImage.create(image.width(), image.height(), image.depth(), image.nChannels());

        }

        if (imageSize != lastSize) {
            lastSize = imageSize;
            irDetect = opencv_core.IplImage.create(imageSize, imageSize, 8, 1);
            irPImage = Papart.getPapart().getApplet().createImage(imageSize, imageSize, ALPHA);
        }

        // Copy the incoming IR Image.
        cvCopy(image, irImage);

        ProjectiveDeviceP projectiveDevice = depthAnalysis.getColorProjectiveDevice();

        // Find the zone to work on.   
        int fingerX = offset % projectiveDevice.getWidth();
        int fingerY = offset / projectiveDevice.getWidth();

        int minX = fingerX - imageSize / 2;
        int minY = fingerY - imageSize / 2;

        if (minX < 0) {
            minX = 0;
        }
        if (minY < 0) {
            minY = 0;
        }
        if (minX + imageSize >= projectiveDevice.getWidth()) {
            minX = projectiveDevice.getWidth() - imageSize - 1;
        }

        if (minY + imageSize >= projectiveDevice.getHeight()) {
            minY = projectiveDevice.getHeight() - imageSize - 1;
        }

        opencv_core.CvRect defaultRoi = cvRect(0, 0,
                projectiveDevice.getWidth(), projectiveDevice.getHeight());

        opencv_core.CvRect roi = cvRect(minX, minY, imageSize, imageSize);

        // Select the zone 
        cvSetImageROI(irImage, roi);

        opencv_imgproc.medianBlur(new opencv_core.Mat(irImage), new opencv_core.Mat(irImage), 1);
//        opencv_imgproc.blur(new Mat(irImage), new Mat(irImage), new Size(2, 2));

        cvCanny(irImage, irImage,
                getCalibration().getTest3(),
                getCalibration().getTest4(),
                7);

        int morph_size = 1;
        opencv_core.Mat element = opencv_imgproc.getStructuringElement(0, new opencv_core.Size(2 * morph_size + 1, 2 * morph_size + 1), new opencv_core.Point(morph_size, morph_size));
        opencv_imgproc.dilate(new opencv_core.Mat(irImage), new opencv_core.Mat(irImage), element);
        opencv_imgproc.dilate(new opencv_core.Mat(irImage), new opencv_core.Mat(irImage), element);
        opencv_imgproc.erode(new opencv_core.Mat(irImage), new opencv_core.Mat(irImage), element);

        cvCopy(irImage, irDetect);

        // Copy to PImage to preview  (to delete for production) 
//        ImageUtils.IplImageToPImage(irDetect, irPImage);
        // set reset ROI
        cvSetImageROI(irImage, defaultRoi);
        return irImage;
    }

    public PImage getIRImage() {
        if (irDetect == null || irPImage == null) {
            return null;
        }
        // Copy to PImage to preview  (to delete for production) 
        ImageUtils.IplImageToPImage(irDetect, irPImage);
        return irPImage;
    }

    //////////////////////////////////////////
    // DEAD CODE ZONE 
    // Contour detection and rectonstruction. 
    //////////////////////////////////////////
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
    // WARNING IMAGE CREATION HERE  
//        ImageUtils.IplImageToPImage(copy, out);
    @Override
    protected ConnectedComponent findConnectedComponent(int startingPoint) {

        // searchDepth is by precision steps. 
        searchDepth = calib.getSearchDepth() * calib.getPrecision();
        precision = calib.getPrecision();

        w = imgSize.getWidth();
        h = imgSize.getHeight();
        initialPoint = startingPoint;
//        ConnectedComponent cc = findNeighboursRec(startingPoint, 0, getX(startingPoint), getY(startingPoint));
        ConnectedComponent cc = findNeighboursFloodFill(startingPoint);

        // Do not accept 1 point compo ?!
        if (cc.size() <= calib.getMinimumComponentSize()) {
            clearPoints(cc);
            // Remove all points
            contactPoints.remove(currentCompo);
            connectedComponentImage[startingPoint] = NO_CONNECTED_COMPONENT;
            return INVALID_COMPONENT;
        }
        cc.setId(currentCompo);
        currentCompo++;
        return cc;
    }

    protected void clearPoints(ConnectedComponent cc) {
        for (Integer pt : cc) {
            connectedComponentImage[pt] = NO_CONNECTED_COMPONENT;
        }
    }

    // Disabled for testing distance from hand
    protected ArrayList<TrackedDepthPoint> createTouchPointsWithContacts(ArrayList<ConnectedComponent> connectedComponents) {

        // Bypass this step and use our now found points.
        ArrayList<TrackedDepthPoint> newPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {
//        for (ConnectedComponent connectedComponent : contactPoints.values()) {

            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < calib.getMinimumComponentSize()
                    || height < calib.getMinimumHeight()
                    || !contactPoints.containsKey((byte) connectedComponent.getId())) {

                continue;
            }

            TrackedDepthPoint tp = createTouchPoint(contactPoints.get((byte) connectedComponent.getId()));
            tp.setDepthDataElements(depthData, connectedComponent);
            newPoints.add(tp);
        }

        return newPoints;
    }

    @Override
    public ArrayList<TrackedDepthPoint> compute(ProjectedDepthData dData) {

        this.setDepthData(dData);
        if (!hasCCToFind()) {
            return new ArrayList<TrackedDepthPoint>();
        }
        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents();
        ArrayList<TrackedDepthPoint> newPoints = this.createTouchPointsFrom(connectedComponents);
        return newPoints;
    }

    @Override
    protected ArrayList<TrackedDepthPoint> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TrackedDepthPoint> newPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

//            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < calib.getMinimumComponentSize()) {
//                    || height < calib.getMinimumHeight()) {

                continue;
            }

            int k = 0;
            int total = connectedComponent.size();
            double[][] dataPoints = new double[total][3];
            for (Integer offsetPt : connectedComponent) {
                dataPoints[k][0] = depthData.depthPoints[offsetPt].x;
                dataPoints[k][1] = depthData.depthPoints[offsetPt].y;
                dataPoints[k][2] = depthData.depthPoints[offsetPt].z;
                k++;
            }
            //  column corresponding to dimension: x, y, z */
            Matrix trainingData = new Matrix(dataPoints);
            PCA fingerPCA = new PCA(trainingData);
            Matrix vectors = fingerPCA.getEigenvectorsMatrix();

            try {

                if (fingerPCA.getEigenvectorsMatrix().getColumnDimension() != 3
                        || fingerPCA.getEigenvectorsMatrix().getRowDimension() != 3) {
//                    System.out.println("EigenMatrix too short.");
// Propably not finger
                    TrackedDepthPoint tp = createTouchPoint(connectedComponent, new Vec3D(0, 0, 0));
                    newPoints.add(tp);

                    continue;
                }
                double e0 = fingerPCA.getEigenvalue(0);
                double e1 = fingerPCA.getEigenvalue(1);

//                fingerPCA.transform(vectors, PCA.TransformationType.ROTATION)
                // e0 -> length  of finger
                // e1 -> width of finger                // e0 -> length  of finger
                // e1 -> width of finger
                Matrix eigenvectorsMatrix = fingerPCA.getEigenvectorsMatrix().copy();
                eigenvectorsMatrix.inverse();

                Matrix furtherPt = new Matrix(new double[][]{
                    {
                        e0 / 8f, 0, 0}});

                Matrix mult = furtherPt.times(eigenvectorsMatrix);

//                System.out.println("Mult: " + mult.get(0, 0)
//                        + " " + mult.get(0, 1)
//                        + " " + mult.get(0, 2));
                Vec3D shift = new Vec3D(
                        (float) mult.get(0, 0),
                        (float) mult.get(0, 1), 0);
//                        (float) mult.get(0, 2));

                TrackedDepthPoint tp = createTouchPoint(connectedComponent, shift);
//                Matrix testData = new Matrix(new double[][]{
//                    {
//                        tp.getPositionDepthCam().x,
//                        tp.getPositionDepthCam().y,
//                        tp.getPositionDepthCam().z}});
//                Matrix transformedData
//                        = fingerPCA.transform(testData, PCA.TransformationType.ROTATION);
//                float trX = (float) transformedData.get(0, 0);
//                float trY = (float) transformedData.get(0, 1);
//                float trZ = (float) transformedData.get(0, 2);
//                System.out.println(trX + " " + trY + " " + trZ);
                newPoints.add(tp);
//
//                if (e0 < 20f && e0 > 200f
//                        && e1 < 30) {
//                    continue;
//                }

            } catch (Exception e) {
                System.out.println("Error ");
                e.printStackTrace();
            }

        }
        return newPoints;
    }

    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent,
            Vec3D shift) {

        Vec3D meanProj, meanKinect;
        meanProj = new Vec3D();
        meanKinect = connectedComponent.getMean(depthData.depthPoints);
//        meanKinect.addSelf(shift);

        Vec3D handPos = currentHand.getPositionDepthCam();
        Vec3D dir = meanKinect.sub(handPos).normalize().scale(12f);

        meanKinect.addSelf(dir);

        currentPlaneProj.project(meanKinect, meanProj);

        TrackedDepthPoint tp = new TrackedDepthPoint();
        tp.setDetection(this);
        tp.setPosition(meanProj);
        tp.setPositionKinect(meanKinect);
        tp.setCreationTime(depthData.timeStamp);
        tp.set3D(false);
        tp.setConfidence(connectedComponent.size() / calib.getMinimumComponentSize());
        // TODO: re-enable this one day ?
//        tp.setConnectedComponent(connectedComponent);
        tp.setDepthDataElements(depthData, connectedComponent);
        return tp;
    }

    @Override
    public boolean hasCCToFind() {
        return !this.toVisit.isEmpty();
    }

    // DISABLED 
    @Override
    protected void setSearchParameters() {

        // Done before
//        this.toVisit.clear();
//        this.toVisit.addAll(touchRecognition.getSelection().validPointsList);
//        contactPoints.clear();
    }

}
