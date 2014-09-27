/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import org.bytedeco.javacpp.opencv_core.IplImage;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeremylaviole
 */
class ARTThread extends Thread {

    private Camera camera;
    private List<MarkerBoard> sheets = null;
    private boolean undistort;
    private boolean compute;
    public boolean stop;

    public ARTThread(Camera camera, List<MarkerBoard> sheets) {
        this(camera, sheets, true);
    }

    public ARTThread(Camera camera, List<MarkerBoard> sheets, boolean undistort) {
        this.undistort = undistort;
        this.camera = camera;
        this.sheets = sheets;
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            IplImage img = camera.grab(undistort);

            if (img != null && compute && this.sheets != null) {
                this.compute(img);
            }

        }
    }

    public void compute(IplImage img) {
        try {
            camera.sheetsSemaphore.acquire();

            for (MarkerBoard sheet : sheets) {
                sheet.updatePosition(camera, img);
            }
            camera.sheetsSemaphore.release();
        } catch (InterruptedException ex) {
            Logger.getLogger(ARTThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isCompute() {
        return compute;
    }

    public void setCompute(boolean compute) {
        this.compute = compute;
        if (compute && this.sheets == null) {
            this.sheets = camera.getTrackedSheets();
        }
    }

    public void stopThread() {
        stop = true;
    }
}
