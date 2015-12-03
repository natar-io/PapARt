/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import static fr.inria.papart.procam.MarkerBoard.BLOCK_UPDATE;
import static fr.inria.papart.procam.MarkerBoard.FORCE_UPDATE;
import static fr.inria.papart.procam.MarkerBoard.NORMAL;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerList;
import fr.inria.papart.tracking.MarkerSvg;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.bytedeco.javacpp.ARToolKitPlus;
import static org.bytedeco.javacpp.ARToolKitPlus.IMAGE_HALF_RES;
import static org.bytedeco.javacpp.ARToolKitPlus.MARKER_ID_BCH;
import static org.bytedeco.javacpp.ARToolKitPlus.PIXEL_FORMAT_LUM;
import static org.bytedeco.javacpp.ARToolKitPlus.UNDIST_NONE;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_EPS;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_64F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvConvertScale;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_core.cvTermCriteria;
import org.bytedeco.javacpp.opencv_imgproc;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindCornerSubPix;
import static org.bytedeco.javacpp.opencv_imgproc.cvFont;
import static org.bytedeco.javacpp.opencv_imgproc.cvMinAreaRect2;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.XML;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerBoardSvg extends MarkerBoard {

    private final int subPixelWindow = 11;
    private MarkerList markersFromSVG;

    public MarkerBoardSvg(String fileName, float width, float height) {
        super(fileName, width, height);
        trackers = new ArrayList<>();
        this.type = MarkerType.SVG;

        // TODO: better than getting the Papart object...
        XML xml = Papart.getPapart().getApplet().loadXML(getFileName());
        markersFromSVG = MarkerSvg.getMarkersFromSVG(xml);

    }

    @Override
    protected void addTrackerImpl(Camera camera) {

        int cameraWidth = camera.width();
        int cameraHeight = camera.height();

        ARToolKitPlus.MultiTracker tracker = new ARToolKitPlus.MultiTracker(cameraWidth, cameraHeight);

        tracker.setPixelFormat(PIXEL_FORMAT_LUM);
        tracker.setBorderWidth(0.125f);
//        tracker.setThreshold(128);
        tracker.activateAutoThreshold(true);
//        tracker.setNumAutoThresholdRetries(10);
        tracker.setUndistortionMode(UNDIST_NONE);

//      tracker.setPoseEstimator(POSE_ESTIMATOR_RPP);
//      tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_RPP);
//      tracker.setPoseEstimator(ARToolKitPlus.POSE_ESTIMATOR_ORIGINAL_CONT);
        tracker.setMarkerMode(MARKER_ID_BCH);
        tracker.setImageProcessingMode(IMAGE_HALF_RES);
        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_HALF_RES);
//        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);

        tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);

        tracker.setUseDetectLite(false);
//        tracker.setUseDetectLite(true);

        PMatrix3D tr = new PMatrix3D();
        this.trackers.add(tracker);
        this.transfos.add(tr);
    }

    public int MIN_ARTOOLKIT_MARKER_DETECTED = 1;

    @Override
    protected void updatePositionImpl(int id, int currentTime, int endTime, int mode, Camera camera, opencv_core.IplImage img) {

        ARToolKitPlus.MultiTracker tracker = (ARToolKitPlus.MultiTracker) trackers.get(id);
        DetectedMarker[] markers = detect(tracker, img);

        PMatrix3D newPos = compute3DPos(markers, camera);

        if (newPos == INVALID_LOCATION) {
            return;
        }
        PVector currentPos = new PVector(newPos.m03, newPos.m13, newPos.m23);

        // Cannot detect elements as close as closer than 10cm
        if (currentPos.z < 10) {
            return;
        }

        // if the update is forced 
        if (mode == FORCE_UPDATE && currentTime < endTime) {
            update(newPos, id);
            return;
        }

        // the force and block updates are finished, revert back to normal
        if (mode == FORCE_UPDATE || mode == BLOCK_UPDATE && currentTime > endTime) {
            updateStatus.set(id, NORMAL);
        }

        float distance = currentPos.dist(lastPos.get(id));
        lastDistance.set(id, distance);

        // if it is a drawing mode
        if (drawingMode.get(id)) {

            if (distance > this.minDistanceDrawingMode.get(id)) {
                update(newPos, id);
                lastPos.set(id, currentPos);
                updateStatus.set(id, FORCE_UPDATE);
                nextTimeEvent.set(id, applet.millis() + MarkerBoard.updateTime);
            }

        } else {
            update(newPos, id);

        }

    }

    private PMatrix3D compute3DPos(DetectedMarker[] detectedMarkers, Camera camera) {
        // We create a pair model ( markersFromSVG) -> observation (markers) 

//         markersFromSVG
        ArrayList<PVector> objectPoints = new ArrayList<PVector>();
        ArrayList<PVector> imagePoints = new ArrayList<PVector>();
        int k = 0;

        for (DetectedMarker detected : detectedMarkers) {
            if (markersFromSVG.containsKey(detected.id)) {

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
        if (k < 1) {
            return MarkerBoard.INVALID_LOCATION;
        }

        PVector[] objectArray = new PVector[k];
        PVector[] imageArray = new PVector[k];
        objectArray = objectPoints.toArray(objectArray);
        imageArray = imagePoints.toArray(imageArray);

        ProjectiveDeviceP pdp = camera.getProjectiveDevice();
        return pdp.estimateOrientation(objectArray, imageArray);

    }

    public DetectedMarker[] detect(ARToolKitPlus.TrackerMultiMarker tracker, opencv_core.IplImage image) {

        int cameraWidth = image.width();
        int cameraHeight = image.height();
        // TODO: check imgWith and init width.

        opencv_core.CvPoint2D32f corners = new opencv_core.CvPoint2D32f(4);
        opencv_core.CvMemStorage memory = opencv_core.CvMemStorage.create();
        opencv_core.CvSize subPixelSize = null, subPixelZeroZone = null;
        opencv_core.CvTermCriteria subPixelTermCriteria = null;

        subPixelSize = cvSize(subPixelWindow / 2, subPixelWindow / 2);
        subPixelZeroZone = cvSize(-1, -1);
        subPixelTermCriteria = cvTermCriteria(CV_TERMCRIT_EPS, 100, 0.001);

        int n = 0;
        IntPointer markerNum = new IntPointer(1);
        ARToolKitPlus.ARMarkerInfo markers = new ARToolKitPlus.ARMarkerInfo(null);
        tracker.arDetectMarkerLite(image.imageData(), tracker.getThreshold() /* 100 */, markers, markerNum);

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

            opencv_core.CvMat points = opencv_core.CvMat.create(1, 4, CV_32F, 2);
            points.getFloatBuffer().put(vertex);
            opencv_core.CvBox2D box = cvMinAreaRect2(points, memory);

            float bw = box.size().width();
            float bh = box.size().height();
            cvClearMemStorage(memory);
            if (bw <= 0 || bh <= 0 || bw / bh < 0.1 || bw / bh > 10) {
                // marker is too "flat" to have been IDed correctly...
                continue;
            }

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

    private void update(PMatrix3D newPos, int id) {

        PMatrix3D transfo = (PMatrix3D) transfos.get(id);
        fr.inria.papart.multitouch.OneEuroFilter filter[] = filters.get(id);

        if (filter == null) {
            transfo.set(newPos);
        } else {
            try {
                // Rotation
                transfo.m00 = (float) filter[0].filter(newPos.m00);
                transfo.m01 = (float) filter[1].filter(newPos.m01);
                transfo.m02 = (float) filter[2].filter(newPos.m02);
                transfo.m10 = (float) filter[3].filter(newPos.m10);
                transfo.m11 = (float) filter[4].filter(newPos.m11);
                transfo.m12 = (float) filter[5].filter(newPos.m12);
                transfo.m20 = (float) filter[6].filter(newPos.m20);
                transfo.m21 = (float) filter[7].filter(newPos.m21);
                transfo.m22 = (float) filter[8].filter(newPos.m22);

                // Translation
                transfo.m03 = (float) filter[9].filter(newPos.m03);
                transfo.m13 = (float) filter[10].filter(newPos.m13);
                transfo.m23 = (float) filter[11].filter(newPos.m23);
            } catch (Exception e) {
                System.out.println("Filtering error " + e);
            }
        }
        
        float pageHeight = markersFromSVG.getSheetHeight();
        
        transfo.scale(1, -1, 1);
        transfo.translate(0, -pageHeight, 0);        
    }

}
