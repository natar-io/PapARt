/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.MarkerBoard;
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

    public CameraThread(Camera camera) {
        this.camera = camera;
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            camera.grab();
            IplImage img = camera.getIplImage();
            // TODO: check if img can be null...        
            if (img != null && compute && !camera.getTrackedSheets().isEmpty()) {
                this.compute(img);
            }

        }
    }

    public void compute(IplImage img) {
        try {
            camera.sheetsSemaphore.acquire();

            for (MarkerBoard sheet : camera.getTrackedSheets()) {
                sheet.updatePosition(camera, img);
            }
            camera.sheetsSemaphore.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraThread.class.getName()).log(Level.SEVERE, null, ex);
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
