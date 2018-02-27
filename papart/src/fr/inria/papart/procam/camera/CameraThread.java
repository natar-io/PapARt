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
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.tracking.DetectedMarker;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.ARToolKitPlus;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_imgcodecs;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class CameraThread extends Thread {

    private final Camera camera;
    Camera cameraForMarkerboard;
    private boolean compute;
    private IplImage image, grayImage;
    private DetectedMarker[] detectedMarkers;

    public boolean stop;

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;

        cameraForMarkerboard = camera;

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
            if (cameraForMarkerboard == null || !compute || camera.getTrackedSheets().isEmpty()) {
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
     * Find the Markers, or features. 
     * Can be used without a running thread with setImage. 
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
            Logger.getLogger(CameraThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void tryComputeGrayScale() {

//        if (image.depth() == IPL_DEPTH_8U) {
        if (image.nChannels() == 1) {
            grayImage = image;
            return;
        }

        for (MarkerBoard sheet : camera.getTrackedSheets()) {
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
        for (MarkerBoard sheet : camera.getTrackedSheets()) {
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
        tracker = DetectedMarker.createDetector(cameraWidth, cameraHeight);
    }

    private void computeGrayScaleImage() {
        cvCvtColor(image, grayImage, CV_BGR2GRAY);
    }

    static int k  = 0;
    private DetectedMarker[] computeMarkerLocations() {
        
        // DEBUG
        
        if(camera instanceof ProjectorAsCamera){
             opencv_imgcodecs.cvSaveImage("/home/jiii/tmp/art-" + k++ + ".bmp", grayImage);
        }   
        
        
        return DetectedMarker.detect(tracker, grayImage);
    }

    protected void updateSequential() {
        for (MarkerBoard markerBoard : camera.getTrackedSheets()) {
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
