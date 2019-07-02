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
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Papart;
import tech.lity.rea.nectar.tracking.MarkerBoard;
import tech.lity.rea.nectar.markers.DetectedMarker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.ARToolKitPlus;
import static org.bytedeco.javacpp.ARToolKitPlus.MARKER_ID_BCH;
import static org.bytedeco.javacpp.ARToolKitPlus.PIXEL_FORMAT_LUM;
import static org.bytedeco.javacpp.ARToolKitPlus.UNDIST_NONE;
import org.bytedeco.javacpp.IntPointer;
import static org.bytedeco.javacpp.opencv_core.CV_32F;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_EPS;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.CvSize;
import org.bytedeco.javacpp.opencv_core.CvTermCriteria;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import org.bytedeco.javacpp.opencv_imgcodecs;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PVector;
import java.util.Arrays;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_EPS;
import org.bytedeco.javacpp.opencv_core.CvTermCriteria;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_core.cvTermCriteria;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindCornerSubPix;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CameraGrabberThread;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.CameraRGBIRDepth;
import tech.lity.rea.nectar.camera.SubCamera;
import tech.lity.rea.nectar.camera.TrackedObject;

/**
 *
 * @author jeremylaviole
 */
public class CameraComputeThread extends CameraGrabberThread {

    Camera cameraForMarkerboard;
    private boolean compute;
    private IplImage image, grayImage;
    private DetectedMarker[] detectedMarkers;

    public boolean stop;

    /**
     *
     * @param camera
     */
    public CameraComputeThread(Camera camera) {
        super(camera);

        // Thread version... No bonus whatsoever for now.
        initThreadPool();
    }

    private final int nbThreads = 4;
    private ExecutorService threadPool = null;

    private void tryInitThreadPool() {
        if (threadPool == null) {
            this.initThreadPool();
        }
    }

    private void initThreadPool() {
        threadPool = Executors.newFixedThreadPool(nbThreads);
//        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {

        while (!stop) {
            checkSubCamera();
            camera.grab();
            // If there is no camera for tracking...
            if (cameraForMarkerboard == null || !compute || cameraForMarkerboard.getTrackedSheets().isEmpty()) {
                continue;
            }
            image = camera.getIplImage();
            if (image != null) {
                this.compute();
            }
        }
    }

    /**
     * Set an image, used without starting the thread...
     *
     * @param image
     */
    public void setImage(IplImage image) {
        this.image = image;
    }

    private void checkSubCamera() {
        if (!(camera instanceof CameraRGBIRDepth)) {
            return;
        }
        Camera actAsCam = ((CameraRGBIRDepth) camera).getActingCamera();
        if (actAsCam != null) {
            cameraForMarkerboard = actAsCam;
        }
    }

    /**
     * Find the Markers, or features. Can be used without a running thread with
     * setImage.
     */
    public void compute() {
        try {
            camera.getSheetSemaphore().acquire();
            tryComputeGrayScale();
            tryToFindMarkers();

            updateSequential();
//            updateParallel();

            camera.getSheetSemaphore().release();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraComputeThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void tryComputeGrayScale() {

//        if (image.depth() == IPL_DEPTH_8U) {
        if (image.nChannels() == 1) {
            grayImage = image;
            return;
        }

        for (TrackedObject tracked : (List<TrackedObject>) camera.getTrackedSheets()) {
            MarkerBoard sheet = (MarkerBoard) tracked;
            if (sheet.useMarkers()) {
                if (grayImage == null) {
                    initGrayScale();
                }
                computeGrayScaleImage();
                break;
            }
        }
    }

    private void tryToFindMarkers() {
        for (TrackedObject tracked : (List<TrackedObject>) camera.getTrackedSheets()) {
            MarkerBoard sheet = (MarkerBoard) tracked;
            if (sheet.useCustomARToolkitBoard()) {
                if (tracker == null) {
                    initMarkerTracking();
                }
                this.detectedMarkers = computeMarkerLocations();
                camera.setMarkers(this.detectedMarkers);
//                for(DetectedMarker m : detectedMarkers){
//                    
//                    PVector corners[] = m.getCorners();
//                    System.out.println("marker: " + m.id + " :" );
//                    for(PVector c : corners){
//                        System.out.print("c: " + c);
//                    }
//                     System.out.println("");
//                }
//                System.out.println("Detected markers: " + detectedMarkers.length);
//                System.out.println("In camera: " + camera);
                break;
            }
        }
    }

    private void initGrayScale() {
        int width = image.width();
        int height = image.height();
        grayImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    }

    private ARToolKitPlus.MultiTracker tracker = null;

    private void initMarkerTracking() {
        int cameraWidth = camera.width();
        int cameraHeight = camera.height();
        tracker = createDetector(cameraWidth, cameraHeight);
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

    private void computeGrayScaleImage() {
        cvCvtColor(image, grayImage, CV_BGR2GRAY);
    }

    static int k = 0;

    private DetectedMarker[] computeMarkerLocations() {

        // DEBUG
//        if(camera instanceof ProjectorAsCamera){
//             opencv_imgcodecs.cvSaveImage("/home/jiii/tmp/art-" + k++ + ".bmp", grayImage);
//        }   
        if (camera instanceof SubCamera) {
            SubCamera sub = (SubCamera) camera;
            Camera main = sub.getMainCamera();
            if (main instanceof CameraNectar) {
                // do nothing
                return new DetectedMarker[0];
//                markers = ((CameraNectar) main).getMarkers();
            }
        }
        return detect(tracker, grayImage);
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

    protected void updateSequential() {
        for (TrackedObject tracked : (List<TrackedObject>) cameraForMarkerboard.getTrackedSheets()) {
            MarkerBoard markerBoard = (MarkerBoard) tracked;
            updateBoardLocation(markerBoard);
        }
    }

    protected void updateBoardLocation(MarkerBoard markerBoard) {
        // The markerboard will know the real camera, not the top-level camera. 
        if (markerBoard.useMarkers()) {
            markerBoard.updateLocation(cameraForMarkerboard, grayImage, this.detectedMarkers);
        } else {
            markerBoard.updateLocation(cameraForMarkerboard, image, null);
        }
    }

    protected void updateParallel() {
        tryInitThreadPool();

        ArrayList<FutureTask<ARTrackingTask>> tasks = new ArrayList<>();
        for (TrackedObject tracked : (List<TrackedObject>) camera.getTrackedSheets()) {
            MarkerBoard sheet = (MarkerBoard) tracked;

            ARTrackingTask depthPixelTask = new ARTrackingTask(sheet);
            FutureTask<ARTrackingTask> task = new FutureTask<ARTrackingTask>(depthPixelTask);
            threadPool.submit(task);
            tasks.add(task);
        }
//        try {
//            for (FutureTask<DepthPixelTask> task : tasks) {
//                task.get();
//            }
//        } catch (ExecutionException e) {
//        } catch (InterruptedException e) {
//        }

    }

    class ARTrackingTask implements Callable {

        private final MarkerBoard markerBoard;

        public ARTrackingTask(MarkerBoard markerBoard) {
            this.markerBoard = markerBoard;
        }

        @Override
        public Object call() {
            updateBoardLocation(markerBoard);
            return null;
        }

    }

    public boolean isCompute() {
        return compute;
    }

    public void setCompute(boolean compute) {
        this.compute = compute;

        if (compute == false && this.threadPool != null) {
            this.threadPool.shutdown();
            this.threadPool = null;
        }
    }

    public void stopThread() {
        stop = true;
    }
}
