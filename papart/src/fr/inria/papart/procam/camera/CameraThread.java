/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.depthcam.devices.KinectDepthAnalysis;
import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.tracking.DetectedMarker;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.ARToolKitPlus;
import static org.bytedeco.javacpp.ARToolKitPlus.IMAGE_HALF_RES;
import static org.bytedeco.javacpp.ARToolKitPlus.MARKER_ID_BCH;
import static org.bytedeco.javacpp.ARToolKitPlus.PIXEL_FORMAT_LUM;
import static org.bytedeco.javacpp.ARToolKitPlus.UNDIST_NONE;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_64F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvConvertScale;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import org.bytedeco.javacv.JavaCV;

/**
 *
 * @author jeremylaviole
 */
class CameraThread extends Thread {

    private final Camera camera;
    private boolean compute;
    private IplImage image, grayImage;
    private DetectedMarker[] detectedMarkers;

    public boolean stop;

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;

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
            camera.grab();
            image = camera.getIplImage();
            // TODO: check if img can be null...        
            if (image != null && compute && !camera.getTrackedSheets().isEmpty()) {
                this.compute();
            }

        }
    }

    public void compute() {
        try {
            camera.sheetsSemaphore.acquire();

            tryComputeGrayScale();
            tryToFindMarkers();

            // updateSequential();
            updateParallel();

            camera.sheetsSemaphore.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void tryComputeGrayScale() {
        for (MarkerBoard sheet : camera.getTrackedSheets()) {
            if (sheet.useGrayscaleImages()) {
                if (grayImage == null) {
                    initGrayScale();
                }
                computeGrayScaleImage();
                break;
            }
        }
    }

    private void tryToFindMarkers() {
        for (MarkerBoard sheet : camera.getTrackedSheets()) {
            if (sheet.useCustomARToolkitBoard()) {
                if (tracker == null) {
                    initMarkerTracking();
                }
                this.detectedMarkers = computeMarkerLocations();
                camera.setMarkers(this.detectedMarkers);
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

        tracker = new ARToolKitPlus.MultiTracker(cameraWidth, cameraHeight);

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
//        tracker.setImageProcessingMode(IMAGE_HALF_RES);
        tracker.setImageProcessingMode(ARToolKitPlus.IMAGE_FULL_RES);

        tracker.setMarkerMode(ARToolKitPlus.MARKER_ID_BCH);
        tracker.setUseDetectLite(false);
    }

    private void computeGrayScaleImage() {
        // TODO BRG2Gray or RGB 2 gray ?
        cvCvtColor(image, grayImage, CV_BGR2GRAY);
    }

    private DetectedMarker[] computeMarkerLocations() {
        return DetectedMarker.detect(tracker, grayImage);
    }

    protected void updateSequential() {
        for (MarkerBoard markerBoard : camera.getTrackedSheets()) {
            updateBoardLocation(markerBoard);
        }
    }

    protected void updateBoardLocation(MarkerBoard markerBoard) {
        if (markerBoard.useGrayscaleImages()) {
            markerBoard.updateLocation(camera, grayImage, this.detectedMarkers);
        } else {
            markerBoard.updateLocation(camera, image, null);
        }
    }

    protected void updateParallel() {
        tryInitThreadPool();

        ArrayList<FutureTask<ARTrackingTask>> tasks = new ArrayList<>();
        for (MarkerBoard sheet : camera.getTrackedSheets()) {
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
