/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

/**
 *
 * @author jeremylaviole
 */
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class Camera {

    // Camera parameters
    protected PMatrix3D camIntrinsicsP3D;
    public PApplet parent;
    ArrayList<TrackedView> trackedViews;
    MarkerBoard[] sheets;
    public PVector resolution;

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationData, int width, int height) {
        try {
            // TODO: check if all file exists 
           
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
            MarkerBoard[] sheets){
         this(parent, -1, settingsFile, -1, -1, calibrationYAML, calibrationData, sheets);
    }
    
    public Camera(PApplet parent, int camNo, String videoFile,
            int width, int height,
            String calibrationYAML, String calibrationData, 
            MarkerBoard[] sheets) {

        this.resolution = new PVector(width, height);
        // No real tag detection
        this.sheets = sheets;
        this.trackedViews = new ArrayList<TrackedView>();

        // TODO: check the files... 
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
        // TODO: maybe something ? '_' 
    }

    /**
     * Stops the update thread.
     */
    public void stopThread() {
    }

    /**
     * If the video is threaded, this sets if the tracking is on or not.
     *
     * @param auto automatic Tag detection: ON if true.
     */
    public void setAutoUpdate(boolean auto) {
    }

    public float[] getPosPointer(MarkerBoard board) {
        return new float[16];
    }

    /**
     * Gets the 2D location in the image of a 3D point. TODO: undistort ?
     *
     * @param pt 3D point seen by the camera.
     * @return 2D location of the 3D point in the image.
     */
    public PVector getCamViewPoint(PVector pt) {
        return new PVector(0, 0);
    }

    /**
     * Asks the camera to grab an image. Not to use with the threaded option.
     */
    public void grab() {
        grab(true);
    }

    public void grab(boolean undistort) {
        // 
    }

    /**
     * Add a tracked view to the camera. This camera must be tracking the board
     * already. Returns true if the camera is already tracking.
     *
     * @param view
     * @return
     */
    public boolean addTrackedView(TrackedView view) {
        trackedViews.add(view);
//        System.out.println("board already tracked ?" + art.getTransfoMap().containsKey(view.getBoard()));
        return true;
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
    
    public IplImage getViewIpl(TrackedView trackedView){
        
 // TODO: return an IPLImage....
       return null;
    }

    /**
     * Get an image from the view.
     *
     * @param trackedView
     * @return
     */
    public PImage getView(TrackedView trackedView, boolean undistort) {
          
        // TODO: return an image ? 
       return null;
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
       
        // Too weird, not implemented

        return null;
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
       // same as above
        return null;
    }

    public PImage getPImage(){
        // TODO: return a PImage
        return null;
        
    }
    
//    public PImage getLastPaperView(MarkerBoard sheet) {
//        return trackedViews.get(sheet).img;
//    }
    public void close() {
    }
}
