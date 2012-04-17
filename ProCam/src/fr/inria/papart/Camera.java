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
    HashMap<String, TrackedView> sheets;
    

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
            PVector paperSize,
            String calibrationYAML, String calibrationData,
            String[] boards) {


        art = new ARTagDetector(camNo, width, height, 60, calibrationYAML,
                calibrationData,
                boards);
        
        sheets = new HashMap<String, TrackedView>();

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

    public Camera(PApplet parent, String videoPath,
            int width, int height,
            PVector paperSize,
            String calibrationYAML, String calibrationData,
            String[] boards) {


        art = new ARTagDetector(videoPath, width, height, 60, calibrationYAML,
                calibrationData,
                boards);

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

    public PVector getCamViewPoint(PVector pt) {
        PVector tmp = new PVector();
        camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
        return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }
    
    public void grab() {
        art.grab();
    }
    
    
    public void addTrackedView(String name, TrackedView view){
        sheets.put(name, view);
    }
    

    public PImage getView(String view) {
        float[][] allPos = art.findMultiMarkers(true, false);

        TrackedView trackedView = sheets.get(view);
            trackedView.setPos(allPos[k]);
            trackedView.computeCorners(this);
        return trackedView.getImage(art.getImageIpl());
    }

    public PImage getPaperView(int id) {
        float[][] allPos = art.findMultiMarkers(true, false);
        int k = 0;
        TrackedView ps = sheets[id];
        ps.setPos(allPos[id]);
        ps.computeCorners(this);
        return ps.getImage(art.getImageIpl());
    }

    public PImage getLastPaperView(int id) {
        return sheets[id].img;
    }

    public void close() {
    }
}
