/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

/**
 *
 * @author jeremylaviole
 */
import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class Camera {

    protected int width, height;
    public ARTagDetector art;
    protected CameraDevice cameraDevice;
    // Camera parameters
    protected PMatrix3D camIntrinsicsP3D;
    public PApplet parent;
    public float offscreenScale;
    PVector offscreenSize;
    HashMap<PaperSheet, TrackedView> trackedViews;
    PaperSheet[] sheets;
    ARTThread thread = null;

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationData, int width, int height) {
        try {
            fr.inria.papart.Utils.convertARParam(parent, calibrationYAML, calibrationData, width, height);
        } catch (Exception e) {
            PApplet.println("Conversion error. " + e);
        }
    }

    public Camera(PApplet parent, int camNo,
            int width, int height,
            String calibrationYAML, String calibrationData,
            PaperSheet[] sheets) {
        this(parent, camNo, null, width, height, calibrationYAML, calibrationData, sheets);
    }

    public Camera(PApplet parent, String fileName,
            int width, int height,
            String calibrationYAML, String calibrationData,
            PaperSheet[] sheets) {
        this(parent, -1, fileName, width, height, calibrationYAML, calibrationData, sheets);
    }

    protected Camera(PApplet parent, int camNo, String videoFile,
            int width, int height,
            String calibrationYAML, String calibrationData,
            PaperSheet[] sheets) {


        art = new ARTagDetector(camNo, videoFile, width, height, 60, calibrationYAML,
                calibrationData,
                sheets);

        this.sheets = sheets;
        this.trackedViews = new HashMap<PaperSheet, TrackedView>();

        // Load the camera parameters. 
        try {
            CameraDevice[] camDev = CameraDevice.read(calibrationYAML);
            if (camDev.length > 0) {
                cameraDevice = camDev[0];
            }

            double[] camMat = cameraDevice.cameraMatrix.get();
            camIntrinsicsP3D = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
                    (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
                    (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
                    0, 0, 0, 1);

        } catch (Exception e) {
            parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
        }
    }

    public void setThread() {
        if (thread == null) {
            thread = new ARTThread(art, sheets);
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    public void stopThread() {
        thread.stopThread();
        thread = null;
    }

    public void setAutoUpdate(boolean auto) {
        if (thread != null) {
            thread.setCompute(auto);
        } else {
            System.err.println("Camera: Error AutoCompute only if threaded.");
        }
    }

    public PVector getCamViewPoint(PVector pt) {
        PVector tmp = new PVector();
        camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
        return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }

    public void grab() {
        if (thread != null) {
            art.grab();
        } else {
            System.err.println("Camera: Please use Grab() only while not threaded.");
        }
    }

    public void addTrackedView(PaperSheet sheet, TrackedView view) {
        trackedViews.put(sheet, view);
    }

    public PImage getView(PaperSheet sheet) {
        float[] pos = art.findMarkers(sheet);
        TrackedView trackedView = trackedViews.get(sheet);
        if(trackedView == null){
            System.err.println("Error: paper sheet not registered as tracked view.");
            return null;
        }
        
        
        trackedView.setPos(pos);
        trackedView.computeCorners(this);
        return trackedView.getImage(art.getImageIpl());
    }

    public PImage getLastPaperView(PaperSheet sheet) {
        return trackedViews.get(sheet).img;
    }

    public void close() {
        art.close();
    }
}
