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

/**
 *
 * @author jeremylaviole
 */
class CameraThread extends Thread {

    private final Camera camera;
    private boolean compute;
    public boolean stop;
    private IplImage img;

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;

        // Thread version... No bonus whatsoever for now.
        initThreadPool();
    }

    private final int nbThreads = 32;
    private ExecutorService threadPool;

    private void initThreadPool() {
//        threadPool = Executors.newFixedThreadPool(nbThreads);
        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (!stop) {
            camera.grab();
            img = camera.getIplImage();
            // TODO: check if img can be null...        
            if (img != null && compute && !camera.getTrackedSheets().isEmpty()) {
                this.compute();
            }

        }
    }

    public void compute() {
        try {
            camera.sheetsSemaphore.acquire();

//            for (MarkerBoard sheet : camera.getTrackedSheets()) {
//                sheet.updatePosition(camera, img);
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
        try {
            for (FutureTask<DepthPixelTask> task : tasks) {
                task.get();
            }
        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
        }
    }

    class DepthPixelTask implements Callable {

        private final MarkerBoard markerBoard;

        public DepthPixelTask(MarkerBoard markerBoard) {
            this.markerBoard = markerBoard;
        }

        @Override
        public Object call() {
            markerBoard.updatePosition(camera, img);
            return null;
        }

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
