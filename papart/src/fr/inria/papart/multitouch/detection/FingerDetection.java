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
import fr.inria.papart.depthcam.DepthDataElement;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.isValidPoint;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.analysis.Compute2D;
import fr.inria.papart.depthcam.analysis.Compute2DFrom3D;
import fr.inria.papart.depthcam.analysis.Compute3D;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import static fr.inria.papart.multitouch.ConnectedComponent.INVALID_COMPONENT;
import fr.inria.papart.multitouch.tracking.TouchPointTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.utils.ImageUtils;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
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

    public FingerDetection(DepthAnalysisImpl depthAnalysisImpl, PlanarTouchCalibration calib) {
        super(depthAnalysisImpl, calib);
        this.contactPoints = new HashMap<>();
        touchRecognition = new Compute2DFrom3D(depthAnalysisImpl);
        currentPointValidityCondition = new CheckTouchPoint();
    }

    public class CheckTouchPoint implements PointValidityCondition {

        private int inititalPoint;

        public ProjectedDepthData getData() {
            return depthData;
        }

        @Override
        public boolean checkPoint(int candidate, int currentPoint) {

//            MAGIC NUMBER: 20
            boolean classicCheck = !assignedPoints[candidate] // not assigned  
                    && depthData.planeAndProjectionCalibration.distanceTo(depthData.depthPoints[candidate]) < 20f
                    && depthData.depthPoints[inititalPoint].distanceTo(depthData.depthPoints[candidate]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[candidate].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance();

//            // Rotate along with the PCA
//            Matrix testData = new Matrix(new double[][]{
//                {
//                    depthData.depthPoints[candidate].x,
//                    depthData.depthPoints[candidate].y,
//                    depthData.depthPoints[candidate].z}});
//
//            /**
//             * The transformed test data.
//             */
//            Matrix transformedData
//                    = handPCA.transform(testData, PCA.TransformationType.ROTATION);
//            float trX = (float) transformedData.get(0, 0);
//            float trY = (float) transformedData.get(0, 1);
//            float trZ = (float) transformedData.get(0, 2);
//            System.out.println(trX + " " + trY + " " + trZ);
            boolean goodNormal = true;

            if (depthData.normals[candidate] != null) {
                float dN = (depthData.planeAndProjectionCalibration.getPlane().normal).distanceToSquared(depthData.normals[candidate]);
                float d1 = (depthData.planeAndProjectionCalibration.getPlane().getDistanceToPoint(depthData.depthPoints[candidate]));
                // WARNING MAGIC NUMBER HERE
//                boolean higher = depthData.projectedPoints[candidate].z < depthData.projectedPoints[currentPoint].z;
                goodNormal = (depthData.normals[candidate] != null && dN > calib.getNormalFilter());  // Higher  than Xmm
            }

            int argb = depthData.pointColors[candidate];
//            boolean goodColor = ((depthData.pointColors[candidate] & 0xFF) >> 8) == 255;  // 0xFF = 255
            int r = (argb >> 16) & 0xFF;  // Faster way of getting red(argb)
            int g = (argb >> 8) & 0xFF;   // Faster way of getting green(argb)
            int b = argb & 0xFF;          // Faster way of getting blue(argb)
            int total = r + g + b;
            
            boolean handDist = depthData.depthPoints[candidate].distanceTo(handPos) > calib.getTest5();
            
//            System.out.print(total + " ");
            return (classicCheck && handDist && goodNormal); // && (total == 765 && goodNormal));
        }
    }

    PCA handPCA;
    int handOffset; 
    Vec3D handPos; 
    /**
     *
     * @param planeAndProjCalibration
     * @param colorImg
     * @param hand
     */
    public void findTouch(HandDetection hand, IplImage colorImg, PlaneAndProjectionCalibration planeAndProjCalibration) {

        // Warning, this should be done before ?
        this.depthData = depthAnalysis.getDepthData();

        this.touchPoints.clear();

        // For each hand
        for (TrackedDepthPoint touchPoint : hand.getTouchPoints()) {

//            System.out.println("Incoming size of hand: " + touchPoint.getDepthDataElements().size());
            int offset = depthAnalysis.getDepthCameraDevice().findDepthOffset(touchPoint.getPositionDepthCam());

            int w = depthAnalysis.getDepthCameraDevice().getDepthCamera().width();
            int x = offset % w;
            int y = offset / w;
            while (x % getPrecision() != 0) {
                x++;
//                System.out.println("incr x");
            }
            while (y % getPrecision() != 0) {
                y++;
//                System.out.println("incr y");
            }

            offset = x + y * w;
            handOffset = offset;
            handPos = touchPoint.getPositionDepthCam();
            
            int rectSize = (int) this.calib.getTest1();
            computeContour(colorImg, offset, (int) this.calib.getTest1());

//            System.out.println("Offset: " + (offset % 640) + "  " + (offset / 640));
            // Look for better depth
            touchRecognition.find2DTouchFrom3D(planeAndProjCalibration,
                    getPrecision(),
                    colorImg,
                    offset,
                    (int) this.calib.getTest2());

            if (touchRecognition.getSelection().validPointsList.size() == 0) {
                continue;
            }

//            System.out.println("nb Valid: " + this.toVisit.size());
            this.toVisit.addAll(touchRecognition.getSelection().validPointsList);

            // PCA on all valid points (including the table). 
            int k = 0;
            int total = touchRecognition.getSelection().validPointsList.size();
            double[][] dataPoints = new double[total][3];
            for (Integer offsetPt : touchRecognition.getSelection().validPointsList) {
                dataPoints[k][0] = depthData.depthPoints[offsetPt].x;
                dataPoints[k][1] = depthData.depthPoints[offsetPt].y;
                dataPoints[k][2] = depthData.depthPoints[offsetPt].z;
                k++;
            }
            //  column corresponding to dimension: x, y, z */
            Matrix trainingData = new Matrix(dataPoints);
            handPCA = new PCA(trainingData);
            try {
                Matrix vectors = handPCA.getEigenvectorsMatrix();
                double e0 = handPCA.getEigenvalue(0);
                double e1 = handPCA.getEigenvalue(1);
                double e2 = handPCA.getEigenvalue(2);
//
//                for (int r = 0; r < vectors.getRowDimension(); r++) {
//                    for (int c = 0; c < vectors.getColumnDimension(); c++) {
//                        System.out.print(vectors.get(r, c));
//                        if (c == vectors.getColumnDimension() - 1) {
//                            continue;
//                        }
//                        System.out.print(", ");
//                    }
//                    System.out.println("");
//                }

            } catch (Exception e) {
            }

            // Generate a touch list from these points. 
            ArrayList<TrackedDepthPoint> newList;
            newList = this.compute(this.depthAnalysis.getDepthData());

            int imageTime = this.depthAnalysis.getDepthData().timeStamp;
            // Track the points and update the touchPoints2D variable.
//        TouchPointTracker.trackPoints(touchPoints, newList, imageTime);
            this.touchPoints.addAll(newList);
        }

        System.out.println("TouchPoints trouvés: " + this.touchPoints.size());
    }

    private void computeContour(opencv_core.IplImage image, int offset, int imageSize) {

        ProjectiveDeviceP projectiveDevice = depthAnalysis.getColorProjectiveDevice();

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

        System.out.println("ROI: " + minX + " " + minY + " " + imageSize + " " + imageSize);

        opencv_core.CvRect roi = cvRect(minX, minY, imageSize, imageSize);
        cvSetImageROI(image, roi);

        opencv_core.IplImage copy = opencv_core.IplImage.create(imageSize, imageSize, 8, 1);

        System.out.println("Orig Size: " + image.width() + " " + image.height() + " " + image.depth() + " " + image.nChannels());
        System.out.println("Copy Size: " + copy.width() + " " + copy.height() + " " + copy.depth() + " " + copy.nChannels());

        cvCopy(image, copy);

        // WARNING IMAGE CREATION HERE 
        opencv_core.IplImage dst = cvCreateImage(cvGetSize(copy), copy.depth(), 1);

//        opencv_imgproc.blur(new Mat(copy), new Mat(dst), new Size(2, 2));
//        cvSmooth(copy, dst);  // OK
        opencv_imgproc.medianBlur(new opencv_core.Mat(copy), new opencv_core.Mat(dst), 3);

//        colorDst = cvCreateImage(cvGetSize(copy), copy.depth(), 3);
        cvCanny(dst, dst,
                getCalibration().getTest3(),
                getCalibration().getTest4(),
                7);

        int morph_size = 2;
        opencv_core.Mat element = opencv_imgproc.getStructuringElement(0, new opencv_core.Size(2 * morph_size + 1, 2 * morph_size + 1), new opencv_core.Point(morph_size, morph_size));
//        opencv_imgproc.dilate(new opencv_core.Mat(dst), new opencv_core.Mat(dst), element);
        opencv_imgproc.dilate(new opencv_core.Mat(dst), new opencv_core.Mat(dst), element);

//        opencv_imgproc.dilate(new opencv_core.Mat(dst), new opencv_core.Mat(image), element);
        opencv_imgproc.erode(new opencv_core.Mat(dst), new opencv_core.Mat(image), element);

        cvCopy(image, copy);

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
        // WARNING IMAGE CREATION HERE  
        out = Papart.getPapart().getApplet().createImage(imageSize, imageSize, ALPHA);

        ImageUtils.IplImageToPImage(copy, out);

        cvSetImageROI(image, defaultRoi);

//        ImageUtils.IplImageToPImage(copy, out);
    }

    public PImage out = null;

    @Override
    protected ConnectedComponent findConnectedComponent(int startingPoint) {

        // searchDepth is by precision steps. 
        searchDepth = calib.getSearchDepth() * calib.getPrecision();
        precision = calib.getPrecision();

        w = imgSize.getWidth();
        h = imgSize.getHeight();
inititalPoint = startingPoint;
        ConnectedComponent cc = findNeighboursRec(startingPoint, 0, getX(startingPoint), getY(startingPoint));

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
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {

        Vec3D meanProj, meanKinect;
        meanProj = connectedComponent.getMean(depthData.projectedPoints);
        meanKinect = connectedComponent.getMean(depthData.depthPoints);

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
    public boolean hasCCToFind() {
        return !this.toVisit.isEmpty();
    }

    // DISABLED 
    @Override
    protected void setSearchParameters() {

        // Done before
//        this.toVisit.clear();
//        this.toVisit.addAll(touchRecognition.getSelection().validPointsList);
        contactPoints.clear();
    }

}
