/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.util.ArrayList;
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
    private ArrayList<MarkerBoard> sheets = null;
    private boolean undistort;
    private boolean compute;
    public boolean stop;
    
    public ARTThread(Camera camera, ArrayList<MarkerBoard> sheets) {
        this(camera, sheets, true);
    }

    public ARTThread(Camera camera, ArrayList<MarkerBoard> sheets, boolean undistort) {
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
        for (MarkerBoard sheet : sheets) {
            sheet.updatePosition(camera, img);
        }
    }

    public boolean isCompute() {
        return compute;
    }

    public void setCompute(boolean compute) {
        this.compute = compute;
        if(compute && this.sheets == null){
            this.sheets = camera.getTrackedSheets();
        }
    }

    public void stopThread() {
        stop = true;
    }
}
