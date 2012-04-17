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

    /**
     * This object helds, a camera object. From a video stream, it extracts sub-images, and detect 
     * marker boards using ARToolKitPlus. 
     * 
     * @param parent
     * @param camNo
     * @param width
     * @param height
     * @param calibrationYAML
     * @param calibrationData
     * @param sheets 
     */
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

    /**
     * It makes the camera update continuously. 
     */
    public void setThread() {
        if (thread == null) {
            thread = new ARTThread(art, sheets);
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    /**
     * Stops the update thread.
     */
    public void stopThread() {
        thread.stopThread();
        thread = null;
    }

    /**
     * If the video is threaded, this sets if the tracking is on or not. 
     * @param auto automatic Tag detection: ON if true. 
     */
    public void setAutoUpdate(boolean auto) {
        if (thread != null) {
            thread.setCompute(auto);
        } else {
            System.err.println("Camera: Error AutoCompute only if threaded.");
        }
    }

    /**
     * Gets the 2D location in the image of a 3D point. 
     * @param pt  3D point seen by the camera. 
     * @return 2D location of the 3D point in the image. 
     */
    public PVector getCamViewPoint(PVector pt) {
        PVector tmp = new PVector();
        camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
        //TODO: lens distorsion ?
        return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }

    /**
     * Asks the camera to grab an image. Not to use with the threaded option.
     */
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
        TrackedView trackedView = trackedViews.get(sheet);
        if (trackedView == null) {
            System.err.println("Error: paper sheet not registered as tracked view.");
            return null;
        }
        float[] pos = art.findMarkers(sheet);
        trackedView.setPos(pos);
        trackedView.computeCorners(this);
        return trackedView.getImage(art.getImageIpl());
    }

    /**
     * This function is typically to be used with high resolution camera. 
     * It stops the video stream, takes a picture, takes the zone of interests and 
     * returns it as an image. It restarts the video stream before exiting. 
     * 
     * @param sheet
     * @return image
     */
    public PImage stopGetViewStart(PaperSheet sheet) {
        if (thread != null) {
            System.err.println("Camera : Error: stopGetViewStart is to use only when thread is started");
            return null;
        }
        boolean wasAutoUpdate = thread.isCompute();
        thread.stopThread();
        this.grab();

        TrackedView trackedView = trackedViews.get(sheet);
        if (trackedView == null) {
            System.err.println("Error: paper sheet not registered as tracked view.");
            return null;
        }
        float[] pos = art.findMarkers(sheet);
        trackedView.setPos(pos);
        trackedView.computeCorners(this);
        PImage out = trackedView.getImage(art.getImageIpl());

        setThread();
        thread.setCompute(wasAutoUpdate);

        return out;
    }

    /**
     * This function is typically to be used with high resolution camera. 
     * It stops the video stream, takes a picture, takess the zone of interests and 
     * returns it as an array of images. It restarts the video stream before exiting. 
     * 
     * @param sheet
     * @return image
     */
    public PImage[] stopGetViewStart(PaperSheet[] sheets) {
        if (thread != null) {
            System.err.println("Camera : Error: stopGetViewStart is to use only when thread is started");
            return null;
        }
        boolean wasAutoUpdate = thread.isCompute();
        thread.stopThread();
        this.grab();

        PImage[] out = new PImage[sheets.length];
        int k = 0;

        for (PaperSheet sheet : sheets) {
            TrackedView trackedView = trackedViews.get(sheet);
            if (trackedView == null) {
                System.err.println("Error: paper sheet not registered as tracked view.");
                return null;
            }
            float[] pos = art.findMarkers(sheet);
            trackedView.setPos(pos);
            trackedView.computeCorners(this);
            out[k++] = trackedView.getImage(art.getImageIpl());
        }
        setThread();
        thread.setCompute(wasAutoUpdate);

        return out;
    }

    public PImage getLastPaperView(PaperSheet sheet) {
        return trackedViews.get(sheet).img;
    }

    public void close() {
        art.close();
    }
}
