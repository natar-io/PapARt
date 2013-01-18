/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

/**
 *
 * @author jeremylaviole
 */
import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class Camera {

    public ARTagDetector art;
    // Camera parameters
    protected PMatrix3D camIntrinsicsP3D;
    public PApplet parent;
    private ArrayList<TrackedView> trackedViews;
    private MarkerBoard[] sheets;
    private ARTThread thread = null;
    protected ProjectiveDeviceP pdp;
    private FrameGrabber grabber;

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationData, int width, int height) {
        try {
            fr.inria.papart.procam.Utils.convertARParam(parent, calibrationYAML, calibrationData, width, height);
        } catch (Exception e) {
            PApplet.println("Conversion error. " + e);
        }
    }

    /**
     * This object helds, a camera object. From a video stream, it extracts
     * sub-images, and detect marker boards using ARToolKitPlus.
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
            MarkerBoard[] sheets) {
        this(parent, camNo, null, width, height, calibrationYAML, calibrationData, sheets);
    }

    public Camera(PApplet parent, String fileName,
            int width, int height,
            String calibrationYAML, String calibrationData,
            MarkerBoard[] sheets) {
        this(parent, -1, fileName, width, height, calibrationYAML, calibrationData, sheets);
    }

    public Camera(PApplet parent, String settingsFile, String calibrationYAML, String calibrationData,
            MarkerBoard[] sheets) {
        this(parent, -1, settingsFile, -1, -1, calibrationYAML, calibrationData, sheets);
    }

    public Camera(PApplet parent, int camNo, String videoFile,
            int width, int height,
            String calibrationYAML, String calibrationData,
            MarkerBoard[] sheets) {

        art = new ARTagDetector(camNo, videoFile, width, height, 60, calibrationYAML,
                calibrationData,
                sheets);

        this.grabber = art.getGrabber();
        this.sheets = sheets;
        this.trackedViews = new ArrayList<TrackedView>();

        // Load the camera parameters. 
        try {
            pdp = ProjectiveDeviceP.loadCameraDevice(calibrationYAML, 0);
            camIntrinsicsP3D = pdp.getIntrinsics();
        } catch (Exception e) {
            parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
        }
    }

    public Camera(FrameGrabber grabber,
            String calibrationYAML, String calibrationData,
            MarkerBoard[] sheets) {

        this.grabber = grabber;

        art = new ARTagDetector(grabber, calibrationYAML,
                calibrationData,
                sheets);

        this.sheets = sheets;
        this.trackedViews = new ArrayList<TrackedView>();

        // Load the camera parameters. 
        try {
            pdp = ProjectiveDeviceP.loadCameraDevice(calibrationYAML, 0);
            camIntrinsicsP3D = pdp.getIntrinsics();
        } catch (Exception e) {
            parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
        }
    }

    /**
     * It makes the camera update continuously.
     */
    public void setThread() {
        setThread(true);
    }

    /**
     * It makes the camera update continuously.
     */
    public void setThread(boolean undistort) {
        if (thread == null) {
            thread = new ARTThread(art, sheets, undistort);
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

    public void setCopyToPImage(boolean isCopy) {
        art.setCopyToPimage(isCopy);
    }

    /**
     * If the video is threaded, this sets if the tracking is on or not.
     *
     * @param auto automatic Tag detection: ON if true.
     */
    public void setAutoUpdate(boolean auto) {
        if (thread != null) {
            thread.setCompute(auto);
        } else {
            System.err.println("Camera: Error AutoCompute only if threaded.");
        }
    }

    public float[] getPosPointer(MarkerBoard board) {
        return board.getTransfo();
    }

    /**
     * Gets the 2D location in the image of a 3D point. TODO: undistort ?
     *
     * @param pt 3D point seen by the camera.
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
        grab(true);
    }

    public void grab(boolean undistort) {
        if (thread == null) {
            art.grab(undistort);
        } else {
            System.err.println("Camera: Please use Grab() only while not threaded.");
        }
    }

    /**
     * Add a tracked view to the camera. This camera must be tracking the board
     * already. Returns true if the camera is already tracking.
     *
     * @param view
     * @return
     */
    public boolean addTrackedView(TrackedView view) {
        return trackedViews.add(view);
    }

    /**
     * Get an image from the view.
     *
     * @param trackedView
     * @return
     */
    public PImage getView(TrackedView trackedView) {
        return getView(trackedView, true);
    }

    public IplImage getViewIpl(TrackedView trackedView) {

        if (trackedView == null) {
            System.err.println("Error: paper sheet not registered as tracked view.");
            return null;
        }

//        grab(undistort);
        if (!art.isReady(true)) {
            return null;
        }
        float[] pos = art.findMarkers(trackedView.getBoard());
        trackedView.setPos(pos);
        trackedView.computeCorners(this);
        return trackedView.getImageIpl(art.getImageIpl());
    }

    /**
     * Get an image from the view.
     *
     * @param trackedView
     * @return
     */
    public PImage getView(TrackedView trackedView, boolean undistort) {

        if (trackedView == null) {
            System.err.println("Error: paper sheet not registered as tracked view.");
            return null;
        }

//        grab(undistort);
        if (!art.isReady(undistort)) {
            return null;
        }
        float[] pos = art.findMarkers(trackedView.getBoard());
        trackedView.setPos(pos);
        trackedView.computeCorners(this);
        return trackedView.getImage(art.getImageIpl());
    }

    /**
     * This function is typically to be used with high resolution camera. It
     * stops the video stream, takes a picture, takes the zone of interests and
     * returns it as an image. It restarts the video stream before exiting.
     *
     * @param sheet
     * @return image
     */
    public PImage stopGetViewStart(TrackedView trackedView) {
        if (thread == null) {
            System.err.println("Camera : Error: stopGetViewStart is to use only when thread is started");
            return null;
        }
        boolean wasAutoUpdate = thread.isCompute();
        stopThread();
        this.grab();

        if (trackedView == null) {
            System.err.println("Error: paper sheet not registered as tracked view.");
            return null;
        }
        float[] pos = art.findMarkers(trackedView.getBoard());
        trackedView.setPos(pos);
        trackedView.computeCorners(this);
        PImage out = trackedView.getImage(art.getImageIpl());

        setThread();
        thread.setCompute(wasAutoUpdate);

        return out;
    }

    /**
     * This function is typically to be used with high resolution camera. It
     * stops the video stream, takes a picture, takess the zone of interests and
     * returns it as an array of images. It restarts the video stream before
     * exiting.
     *
     * @param sheet
     * @return image
     */
    public PImage[] stopGetViewStart(TrackedView[] trackedViews) {
        if (thread == null) {
            System.err.println("Camera : Error: stopGetViewStart is to use only when thread is started");
            return null;
        }
        boolean wasAutoUpdate = thread.isCompute();
        thread.stopThread();
        this.grab();

        PImage[] out = new PImage[sheets.length];
        int k = 0;

        for (TrackedView trackedView : trackedViews) {
            if (trackedView == null) {
                System.err.println("Error: paper sheet not registered as tracked view.");
                return null;
            }
            float[] pos = art.findMarkers(trackedView.getBoard());
            trackedView.setPos(pos);
            trackedView.computeCorners(this);
            out[k++] = trackedView.getImage(art.getImageIpl());
        }
        setThread();
        thread.setCompute(wasAutoUpdate);

        return out;
    }

//    public PImage getPImage(){
//        // TODO: verif non thread etc...
//        art.grab(true, true);
////        art.grab(false, true);
//        return art.getImage();
//    }
//    
    public PImage getPImage() {
        return art.getImage();
    }

//    public PImage getLastPaperView(MarkerBoard sheet) {
//        return trackedViews.get(sheet).img;
//    }
    public void close() {
        art.close();
    }
}
