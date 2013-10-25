/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

/**
 *
 * @author jeremylaviole
 */
import processing.opengl.Texture;
import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import fr.inria.papart.opengl.CustomTexture;
import fr.inria.papart.tools.CaptureIpl;
import fr.inria.papart.tools.GSIplImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class Camera {

    protected ARTagDetector art;
    // Camera parameters
    protected PMatrix3D camIntrinsicsP3D;
    protected PApplet parent;
    private ArrayList<TrackedView> trackedViews;
    private MarkerBoard[] sheets;
    private ARTThread thread = null;
    protected ProjectiveDeviceP pdp;
    // GStreamer  Video input
//    protected GSCapture gsCapture;
//    protected GSPipeline pipeline;
//    protected GSIplImage converter;
    protected CaptureIpl captureIpl;
    // OpenCV  video input 
    private FrameGrabber grabber;
    // Texture for video visualization (OpenCV generally)
    protected IplImage iimg = null, copyUndist;
    protected CustomTexture tex = null;
    protected PImage camImage = null;
    public final static int OPENCV_VIDEO = 1;
    public final static int PROCESSING_VIDEO = 2;
    public static int videoInput = OPENCV_VIDEO;
    protected int width, height;
    protected int videoInputType;
    protected int frameRate;
    protected boolean autoUpdate = false;
    protected boolean gotPicture = false;

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationData, int width, int height) {
        try {
            // ARToolkit Plus 2.1.1
//            fr.inria.papart.procam.Utils.convertARParam(parent, calibrationYAML, calibrationData, width, height);
            // ARToolkit Plus 2.3.0
            fr.inria.papart.procam.Utils.convertARParam2(parent, calibrationYAML, calibrationData, width, height);
        } catch (Exception e) {
            PApplet.println("Conversion error. " + e);
        }
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height) {
        this(parent, camDevice, width, height, 30, null, videoInput);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, int videoInput) {
        this(parent, camDevice, width, height, 30, null, videoInput);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, String calibrationYAML) {
        this(parent, camDevice, width, height, 30, calibrationYAML, videoInput);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, String calibrationYAML, int videoInputType) {
        this(parent, camDevice, width, height, 30, calibrationYAML, videoInputType);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, int frameRate, String calibrationYAML, int videoInputType) {

        this.parent = parent;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.videoInputType = videoInputType;

        if (videoInputType == OPENCV_VIDEO) {
            OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(Integer.parseInt(camDevice));

            grabberCV.setImageWidth(width);
            grabberCV.setImageHeight(height);
            grabberCV.setImageMode(FrameGrabber.ImageMode.COLOR);

            try {
                grabberCV.start();
            } catch (Exception e) {
                System.err.println("Could not start frameGrabber... " + e);
            }

            this.grabber = grabberCV;
        }

        if (videoInputType == PROCESSING_VIDEO) {

            if (camDevice == null) {
                System.out.println("Starting capture !");
                this.captureIpl = new CaptureIpl(parent, width, height);
            } else {
                
                System.out.println("Starting capture on device " + camDevice);
                this.captureIpl = new CaptureIpl(parent, width, height, camDevice);
            }
            
            this.captureIpl.start();
        }


        if (calibrationYAML != null) {

            // Load the camera parameters. 
            try {
                pdp = ProjectiveDeviceP.loadCameraDevice(calibrationYAML, 0);
                camIntrinsicsP3D = pdp.getIntrinsics();
            } catch (Exception e) {
                parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
            }
        }

    }

    public boolean useProcessingVideo() {
        return this.videoInputType == PROCESSING_VIDEO;
    }

    public boolean useOpenCV() {
        return this.videoInputType == OPENCV_VIDEO;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

//    public void captureEvent(GSCapture cam) {
//        cam.read();
//        this.gotPicture = true;
//    }
//
//    public void pipelineEvent(GSPipeline pipeline) {
//        pipeline.read();
//    }
    public void initMarkerDetection(PApplet applet, String calibrationARToolkit, MarkerBoard[] paperSheets) {
        art = new ARTagDetector(applet, this, calibrationARToolkit, width, height, paperSheets, videoInputType);
        this.sheets = paperSheets;
        this.trackedViews = new ArrayList<TrackedView>();
    }
    protected boolean photoCapture;

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
            thread = new ARTThread(this, sheets, undistort);
            thread.start();
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    /**
     * Stops the update thread.
     */
    public void stopThread() {
        if (thread != null) {
            thread.stopThread();
            thread = null;
        }
    }

    /**
     * If the video is threaded, this sets if the tracking is on or not.
     *
     * @param auto automatic Tag detection: ON if true.
     */
    public void setAutoUpdate(boolean auto) {
        this.autoUpdate = auto;

        if (thread != null) {
            thread.setCompute(auto);
        } else {
            System.err.println("Camera: Error AutoCompute only if threaded.");
        }

    }

    public boolean useThread() {
        return thread != null;
    }

    /**
     * Asks the camera to grab an image. Not to use with the threaded option.
     */
    public void grab() {
        grab(true);
    }

    public IplImage grab(boolean undistort) {

        IplImage img = null;

        if (videoInputType == OPENCV_VIDEO) {

            try {
                img = grabber.grab();

            } catch (Exception e) {
                System.err.println("Camera: Grab() Error ! " + e);
                e.printStackTrace();
            }
        }

        if (videoInputType == PROCESSING_VIDEO) {

            // System.out.println("Grab () ?");
            if (this.captureIpl.available()) {
                
               // System.out.println("Avail");
                captureIpl.read();
               img = captureIpl.getIplImage();

            } 
            else {
                try {
                   // System.out.println("Sleep...");
                    
   
               // TimeUnit.MILLISECONDS.sleep((long) (1f / frameRate));
               TimeUnit.MILLISECONDS.sleep((long) (10));
   
                } catch (Exception e) {
                }

                return null;
            }
        }


        if (img != null) {
            if (undistort) {
                if (copyUndist == null) {
                    copyUndist = img.clone();
                }
                pdp.getDevice().undistort(img, copyUndist);
                iimg = copyUndist;
            } else {
                iimg = img;
            }

            this.gotPicture = true;
        }

        return iimg;

    }

    public PImage getPImage() {
        imageRetreived();
//        if (tex == null) {
//            tex = new CustomTexture(width, height);
//        }

//        if (iimg != null) {
//            if (videoInputType == OPENCV_VIDEO) {
//                // TODO: Check direct link to OpenGL again... :( 
////                tex.updateBuffer(iimg.getIntBuffer());
////                tex.putBuffer(GL2.GL_BGR, GL.GL_UNSIGNED_BYTE, iimg.getIntBuffer());
//            } else {
//                // TODO: GSTREAMER !
////                if (videoInputType == GSTREAMER_VIDEO) {
////                    tex.putBuffer(GL2.GL_RGBA, GL.GL_UNSIGNED_BYTE, iimg.getIntBuffer());
////                }
//            }
//        }

        if (camImage == null) {
            System.out.println("Creation ...");
            camImage = parent.createImage(width, height, PApplet.RGB);
        }

        if (useProcessingVideo()) {
            return captureIpl;
        }

        if (iimg != null) {
            Utils.IplImageToPImage(iimg, false, camImage);
        }
        return camImage;
    }

    public IplImage getIplImage() {
        imageRetreived();
        return iimg;
    }

    public ProjectiveDeviceP getProjectiveDevice() {
        return this.pdp;
    }

    public void close() {
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }
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
     * Add a tracked view to the camera. This camera must be tracking the board
     * already. Returns true if the camera is already tracking.
     *
     * @param view
     * @return
     */
    public boolean addTrackedView(TrackedView view) {
        return trackedViews.add(view);
    }

    public IplImage getView(TrackedView trackedView) {
        return getViewIpl(trackedView);
    }

    public PImage getPView(TrackedView trackedView) {
        if (iimg == null) {
            return null;
        }

        trackedView.computeCorners(this);
        return trackedView.getImage(iimg);
    }

    public IplImage getViewIpl(TrackedView trackedView) {
        if (iimg == null) {
            return null;
        }

        trackedView.computeCorners();
        return trackedView.getImageIpl(iimg);
    }

    public void grabTo(PImage pimg, boolean undist) {
        try {

            IplImage img = this.grab(undist);

            if (img != null) {
                Utils.IplImageToPImage(img, false, pimg);
            }

        } catch (Exception e) {
            System.err.println("Error while grabbing frame " + e);
            e.printStackTrace();
        }
    }

    protected void imageRetreived() {
        this.hasNewPhoto = false;
    }

    // TODO: check this code... may be broken.
    public void setPhotoCapture() {

        this.photoCapture = true;
        if (useProcessingVideo()) {

            captureIpl.stop();
        }
        if (useOpenCV()) {
            try {
                grabber.stop();
            } catch (Exception e) {
                System.out.println("Error " + e);
            }
        }

    }
    protected boolean isTakingPhoto = false;
    protected boolean hasNewPhoto = false;

    public void takePhoto(boolean undistort) {
        takePhoto(null, undistort);
    }

    public void takePhoto(PImage img, boolean undistort) {
        System.out.println("Is Takiing ? " + this.isTakingPhoto);
        if (!this.isTakingPhoto) {
            this.hasNewPhoto = false;
            this.isTakingPhoto = true;
            System.out.println("Is Takiing ? " + this.isTakingPhoto);
            PhotoTaker pt = new PhotoTaker(this, img, undistort);
            pt.start();
            System.out.println("Photo started...");
        } else {
            System.out.println("Wait for the previous photo.");
        }
    }

    public boolean isTakingPhoto() {
        return this.isTakingPhoto;
    }

    public boolean hasNewPhoto() {
        return this.hasNewPhoto;
    }

    /////////////// TODO: MOVE TO A CLASS ??? /////////
    class PhotoTaker extends Thread {

        private PImage img;
        private boolean undist;
        private Camera cam;

        PhotoTaker(Camera cam, PImage img, boolean undistort) {
            this.img = img;
            this.undist = undistort;
            this.cam = cam;
        }

        // TODO: Magic numbers... 
        @Override
        public void run() {

            System.out.println("Grabbing frames");
            if (cam.useProcessingVideo()) {

                captureIpl.start();

                // check gotPicture etc... 

                while (!gotPicture) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                // Drop 10 frames
                for (int i = 0; i < 10; i++) {
                    if (img != null) {
                        grabTo(img, false);
                    }

                    while (!gotPicture) {

                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Camera.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    grab(false);

                    // NO IDEA WHY THIS IS NOT WORKING
                }

                if (img != null) {
                    grabTo(img, undist);
                }
//                gsCapture.stop();
            }


            if (cam.useOpenCV()) {
                try {
                    grabber.start();
                } catch (Exception e) {
                    System.err.println("Could not start frameGrabber... " + e);
                }
                // Drop 10 frames
                for (int i = 0; i < 30; i++) {
//                    grabTo(img, undist);
                    grab(false);
                    System.out.println("Grabbed " + i);
                }

                if (img != null) {
                    grabTo(img, undist);
                }

                try {
                    grabber.stop();
                } catch (Exception e) {
                    System.err.println("Could not stop frameGrabber... " + e);
                }
            }

            System.out.println("Photo OK ");
            cam.isTakingPhoto = false;
            cam.hasNewPhoto = true;
        }
    }
}
