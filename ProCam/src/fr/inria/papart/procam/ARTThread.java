/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.util.concurrent.TimeUnit;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremylaviole
 */
class ARTThread extends Thread {

    private Camera camera;
    private ARTagDetector art;
    private MarkerBoard[] sheets = null;
    private boolean undistort;
    private boolean compute;
    public boolean stop;
    
    // TODO: what is this ?
    private boolean waitForFrames = false;

    public ARTThread(Camera camera, MarkerBoard[] sheets, float frameRate) {
        this(camera, sheets, true);
    }

    public ARTThread(Camera camera, MarkerBoard[] sheets, boolean undistort) {
        this.undistort = undistort;
        this.camera = camera;
        this.sheets = sheets;
        waitForFrames = camera.useProcessingVideo();
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            IplImage img = camera.grab(undistort);
            
            if (img != null && compute) {
                this.compute(img);
            }

        }
    }

    public void compute(IplImage img) {
        for (MarkerBoard sheet : sheets) {
            sheet.updatePosition(camera, img);
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
