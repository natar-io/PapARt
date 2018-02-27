/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
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
package fr.inria.papart.tracking;

import fr.inria.papart.procam.ProjectiveDeviceP;
import java.util.ArrayList;
import java.util.Arrays;
import org.bytedeco.javacpp.ARToolKitPlus;

import static org.bytedeco.javacpp.ARToolKitPlus.*;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindCornerSubPix;
import org.bytedeco.javacv.Marker;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class DetectedMarker implements Cloneable {

    public int id;
    public double[] corners;
    public double confidence;

    public DetectedMarker(int id, double[] corners, double confidence) {
        this.id = id;
        this.corners = corners;
        this.confidence = confidence;
    }

    public int getId() {
        return id;
    }

    public Marker copyAsMarker() {
        return new org.bytedeco.javacv.Marker(id, corners, confidence);
    }

    public void drawSelf(PGraphics g, int size) {
        for (int i = 0; i < 8; i += 2) {
            g.ellipse((float) corners[i], (float) corners[i + 1], size, size);
        }
    }

    public PVector[] getCorners() {
        PVector[] out = new PVector[4];
        out[0] = new PVector((float) corners[0], (float) corners[1]);
        out[1] = new PVector((float) corners[2], (float) corners[3]);
        out[2] = new PVector((float) corners[4], (float) corners[5]);
        out[3] = new PVector((float) corners[6], (float) corners[7]);
        return out;
    }

    public DetectedMarker(int id, double... corners) {
        this(id, corners, 1.0);
    }

    @Override
    public DetectedMarker clone() {
        return new DetectedMarker(id, corners.clone(), confidence);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DetectedMarker) {
            DetectedMarker m = (DetectedMarker) o;
            return m.id == id && Arrays.equals(m.corners, corners);
        }
        return false;
    }

    public double[] getCenter() {
        double x = 0, y = 0;
        if (true) {
// the centroid is not what we want as it does not remain at
// the same physical point under projective transformations..
// But it has the advantage of averaging noise better, and does
// give better results
            for (int i = 0; i < 4; i++) {
                x += corners[2 * i];
                y += corners[2 * i + 1];
            }
            x /= 4;
            y /= 4;
        } else {
            double x1 = corners[0];
            double y1 = corners[1];
            double x2 = corners[4];
            double y2 = corners[5];
            double x3 = corners[2];
            double y3 = corners[3];
            double x4 = corners[6];
            double y4 = corners[7];

            double u = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3))
                    / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
            x = x1 + u * (x2 - x1);
            y = y1 + u * (y2 - y1);
        }
        return new double[]{x, y};
    }

    public IplImage getImage() {
        return getImage(id);
    }

    public static ARToolKitPlus.MultiTracker createDetector(int width, int height) {
        ARToolKitPlus.MultiTracker tracker = new ARToolKitPlus.MultiTracker(width, height);

        tracker.setPixelFormat(PIXEL_FORMAT_LUM);
        tracker.setBorderWidth(0.125f);
//        tracker.setThreshold(128);
        tracker.activateAutoThreshold(true);
//        tracker.setNumAutoThresholdRetries(10);

        tracker.setUndistortionMode(UNDIST_NONE);

//      tracker.setPoseEstimator(POSE_ESTIMATOR_RPP);
//      tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_ORIGINAL);
//      tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_ORIGINAL_CONT);
        tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);

        tracker.setMarkerMode(MARKER_ID_BCH);
//        tracker.setImageProcessingMode(IMAGE_HALF_RES);
        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);
        tracker.setUseDetectLite(false);
        return tracker;
    }

    private static IplImage imageCache[] = new IplImage[4096];

    public static IplImage getImage(int id) {
        if (imageCache[id] == null) {
            imageCache[id] = IplImage.create(8, 8, IPL_DEPTH_8U, 1);
            createImagePatternBCH(id, imageCache[id].getByteBuffer());
        }
        return imageCache[id];
    }

    public static DetectedMarker[] detect(ARToolKitPlus.TrackerMultiMarker tracker, opencv_core.IplImage image) {

        int cameraWidth = image.width();
        int cameraHeight = image.height();
        // TODO: check imgWith and init width.

        CvPoint2D32f corners = new CvPoint2D32f(4);
        CvMemStorage memory = CvMemStorage.create();
//        CvMat points = CvMat.create(1, 4, CV_32F, 2);
        Mat points = new Mat(1, 4, CV_32F, 2);

        CvSize subPixelSize = null, subPixelZeroZone = null;
        CvTermCriteria subPixelTermCriteria = null;
        int subPixelWindow = 11;

        subPixelSize = cvSize(subPixelWindow / 2, subPixelWindow / 2);
        subPixelZeroZone = cvSize(-1, -1);
        subPixelTermCriteria = cvTermCriteria(CV_TERMCRIT_EPS, 100, 0.001);

//        tracker.setThreshold(128);
        int n = 0;
        IntPointer markerNum = new IntPointer(1);
        ARToolKitPlus.ARMarkerInfo markers = new ARToolKitPlus.ARMarkerInfo(null);
//        tracker.arDetectMarkerLite(image.imageData(), tracker.getThreshold() /* 100 */, markers, markerNum);
        tracker.arDetectMarker(image.imageData(), tracker.getThreshold() /* 100 */, markers, markerNum);
        DetectedMarker[] markers2 = new DetectedMarker[markerNum.get(0)];

        for (int i = 0; i < markers2.length && !markers.isNull(); i++) {

            markers.position(i);
            int id = markers.id();
            if (id < 0) {
                // no detected ID...
                continue;
            }
            int dir = markers.dir();
            float confidence = markers.cf();
            float[] vertex = new float[8];
            markers.vertex().get(vertex);

            int w = subPixelWindow / 2 + 1;
            if (vertex[0] - w < 0 || vertex[0] + w >= cameraWidth || vertex[1] - w < 0 || vertex[1] + w >= cameraHeight
                    || vertex[2] - w < 0 || vertex[2] + w >= cameraWidth || vertex[3] - w < 0 || vertex[3] + w >= cameraHeight
                    || vertex[4] - w < 0 || vertex[4] + w >= cameraWidth || vertex[5] - w < 0 || vertex[5] + w >= cameraHeight
                    || vertex[6] - w < 0 || vertex[6] + w >= cameraWidth || vertex[7] - w < 0 || vertex[7] + w >= cameraHeight) {
                // too tight for cvFindCornerSubPix...

                continue;
            }

            // TODO: major bug here -> free error...
//            opencv_core.CvMat points = opencv_core.CvMat.create(1, 4, CV_32F, 2);
//            points.getFloatBuffer().put(vertex);
//            opencv_core.CvBox2D box = cvMinAreaRect2(points, memory);
//
//            float bw = box.size().width();
//            float bh = box.size().height();
//            cvClearMemStorage(memory);
//            if (bw <= 0 || bh <= 0 || bw / bh < 0.1 || bw / bh > 10) {
//                // marker is too "flat" to have been IDed correctly...
//                continue;
//            }
            for (int j = 0; j < 4; j++) {
                corners.position(j).put(vertex[2 * j], vertex[2 * j + 1]);
            }

            cvFindCornerSubPix(image, corners.position(0), 4, subPixelSize, subPixelZeroZone, subPixelTermCriteria);
            double[] d = {corners.position((4 - dir) % 4).x(), corners.position((4 - dir) % 4).y(),
                corners.position((5 - dir) % 4).x(), corners.position((5 - dir) % 4).y(),
                corners.position((6 - dir) % 4).x(), corners.position((6 - dir) % 4).y(),
                corners.position((7 - dir) % 4).x(), corners.position((7 - dir) % 4).y()};

            markers2[n++] = new DetectedMarker(id, d, confidence);
        }
        return Arrays.copyOf(markers2, n);
    }

    /**
     * Find the 3D position of detected markers. It uses the solvePnP function
     * of OpenCV.
     *
     * @param detectedMarkers Array of markers found. (Image)
     * @param markersFromSVG Model.
     * @param camera its calibration is used.
     * @return
     */
    public static PMatrix3D compute3DPos(DetectedMarker[] detectedMarkers, MarkerList markersFromSVG,
            fr.inria.papart.procam.camera.Camera camera) {
        // We create a pair model ( markersFromSVG) -> observation (markers) 

//         markersFromSVG
        ArrayList<PVector> objectPoints = new ArrayList<PVector>();
        ArrayList<PVector> imagePoints = new ArrayList<PVector>();
        int k = 0;

        for (DetectedMarker detected : detectedMarkers) {
            if (markersFromSVG.containsKey(detected.id)) {

//                System.out.println("Detected marker: " + detected.id + " confidence " + detected.confidence);
                if (detected.confidence < 1.0) {
                    continue;
                }

                // Center instead ? 
//                PVector object = markersFromSVG.get(detected.id).getCenter();
////                PVector image = detected.getCenter();
//                double[] im = detected.getCenter();
//                PVector image = new PVector((float) im[0], (float) im[1]);
//                objectPoints.add(object);
//                imagePoints.add(image);

                ////// Corners 
                PVector[] object = markersFromSVG.get(detected.id).getCorners();
                PVector[] image = detected.getCorners();
                for (int i = 0; i < 4; i++) {
//                    System.out.println("Model " + object[i] + " image " + image[i]);
                    objectPoints.add(object[i]);
                    imagePoints.add(image[i]);
                }
                k++;
            }
        }
//        if (k < 4) {
        if (k < 1) {
            return MarkerBoard.INVALID_LOCATION;
        }

        PVector[] objectArray = new PVector[k];
        PVector[] imageArray = new PVector[k];
        objectArray = objectPoints.toArray(objectArray);
        imageArray = imagePoints.toArray(imageArray);

        ProjectiveDeviceP pdp = camera.getProjectiveDevice();
        return pdp.estimateOrientation(objectArray, imageArray);
//        return pdp.estimateOrientationRansac(objectArray, imageArray);
    }

}
