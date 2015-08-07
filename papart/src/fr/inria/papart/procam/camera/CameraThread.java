/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.depthcam.KinectDepthAnalysis;
import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.procam.MarkerBoard;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public boolean stop;
    private IplImage image;

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;

        // Thread version... No bonus whatsoever for now.
        initThreadPool();
    }

    private final int nbThreads = 4;
    private ExecutorService threadPool;

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
                if (thresholdedImage == null) {
                    initGrayScale();
                }
                this.compute();
            }

        }
    }

    public void compute() {
        try {
            camera.sheetsSemaphore.acquire();

            for (MarkerBoard sheet : camera.getTrackedSheets()) {
                if (sheet.useARToolkit()) {
                    computeGrayScaleImage();
                    break;
                }
            }

//            for (MarkerBoard markerBoard : camera.getTrackedSheets()) {
//                if (markerBoard.useARToolkit()) {
//                    markerBoard.updatePosition(camera, grayImage);
//                } else {
//                    markerBoard.updatePosition(camera, image);
//                }
//            }
            updateParallel();

            camera.sheetsSemaphore.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void updateParallel() {
        ArrayList<FutureTask<DepthPixelTask>> tasks = new ArrayList<>();
        for (MarkerBoard sheet : camera.getTrackedSheets()) {
            DepthPixelTask depthPixelTask = new DepthPixelTask(sheet);
            FutureTask<DepthPixelTask> task = new FutureTask<DepthPixelTask>(depthPixelTask);
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

    class DepthPixelTask implements Callable {

        private final MarkerBoard markerBoard;

        public DepthPixelTask(MarkerBoard markerBoard) {
            this.markerBoard = markerBoard;
        }

        @Override
        public Object call() {

            if (markerBoard.useARToolkit()) {
                markerBoard.updatePosition(camera, grayImage);
            } else {
                markerBoard.updatePosition(camera, image);
            }
            return null;
        }

    }

    private IplImage grayImage, tempImage2, sumImage, sqSumImage, thresholdedImage = null;
    private int width, height, depth, channels;

    private void initGrayScale() {
        width = image.width();
        height = image.height();
        depth = image.depth();
        channels = image.nChannels();

        if (depth != IPL_DEPTH_8U || channels > 1) {
            grayImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
        }
        if (depth != IPL_DEPTH_8U && channels > 1) {
            tempImage2 = IplImage.create(width, height, IPL_DEPTH_8U, 3);
        }
        sumImage = IplImage.create(width + 1, height + 1, IPL_DEPTH_64F, 1);
        sqSumImage = IplImage.create(width + 1, height + 1, IPL_DEPTH_64F, 1);
        thresholdedImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
    }

    private void computeGrayScaleImage() {

//        if (depth != IPL_DEPTH_8U && channels > 1) {
//            cvConvertScale(image, tempImage2, 255 / image.highValue(), 0);
//            cvCvtColor(tempImage2, grayImage, channels > 3 ? CV_RGBA2GRAY : CV_BGR2GRAY);
////            image = tempImage;
//        } else if (depth != IPL_DEPTH_8U) {
//            cvConvertScale(image, grayImage, 255 / image.highValue(), 0);
////            image = tempImage;
//        } else if (channels > 1) {
            cvCvtColor(image, grayImage, channels > 3 ? CV_RGBA2GRAY : CV_BGR2GRAY);
//            image = tempImage;
//        }
        /*
         boolean whiteMarkers = false;
         int thresholdWindowMax = 63;
         int thresholdWindowMin = 5;
         float thresholdVarMultiplier = 1.0f;
         float thresholdKBlackMarkers = 0.6f;
         float thresholdKWhiteMarkers = 1.0f;

         //long time1 = System.currentTimeMillis();
         JavaCV.adaptiveThreshold(image, sumImage, sqSumImage, thresholdedImage, whiteMarkers,
         thresholdWindowMax, thresholdWindowMin, thresholdVarMultiplier,
         whiteMarkers ? thresholdKWhiteMarkers : thresholdKBlackMarkers);
        
         //CanvasFrame.global.showImage(thresholded, 0.5);
         //CanvasFrame.global.waitKey();
         //long time2 = System.currentTimeMillis();
         return thresholdedImage;
         */
    }

    public boolean isCompute() {
        return compute;
    }

    public void setCompute(boolean compute) {
        this.compute = compute;
    }

    public void stopThread() {
        stop = true;
    }
}
