/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

/**
 *
 * @author jeremylaviole
 */
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import org.bytedeco.javacpp.ARToolKitPlus;
import org.bytedeco.javacpp.ARToolKitPlus.TrackerMultiMarker;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.IplImage;
//import diewald_PS3.PS3;
//import diewald_PS3.constants.COLOR_MODE;
//import diewald_PS3.constants.VIDEO_MODE;
//import diewald_PS3.logger.PS3Logger;
import fr.inria.papart.multitouchKinect.TouchInput;
import fr.inria.papart.tools.CaptureIpl;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;

public class Camera implements PConstants {

    // Camera parameters
    protected PMatrix3D camIntrinsicsP3D;
    protected PApplet parent;
    private ArrayList<TrackedView> trackedViews;
    private ArrayList<MarkerBoard> sheets = null;
    protected String calibrationARToolkit;
    private ARTThread thread = null;
    protected ProjectiveDeviceP pdp = null;
    // GStreamer  Video input
//    protected GSCapture gsCapture;
//    protected GSPipeline pipeline;
//    protected GSIplImage converter;
    protected CaptureIpl captureIpl;
    // OpenCV  video input 
    private FrameGrabber grabber;
//    private PS3 ps3;
    private OpenKinectFrameGrabber openKinectGrabber;
    // Texture for video visualization (OpenCV generally)
    protected IplImage iimg = null, copyUndist, depthImage = null;
    protected CamImage camImage = null;
    protected PImage depthPImage = null;
    public final static int OPENCV_VIDEO = 1;
    public final static int PROCESSING_VIDEO = 2;
//    public final static int PSEYE_VIDEO = 3;
    public final static int KINECT_VIDEO = 4;
    public static int videoInput = OPENCV_VIDEO;
    protected int width, height;
    protected int videoInputType;
    protected int frameRate;
    protected boolean trackSheets = false;
    protected boolean gotPicture = false;

    private boolean isClosing = false;

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationARtoolkit) {
        convertARParams(parent, calibrationYAML, calibrationARtoolkit, 0, 0);
    }

    static public void convertARParams(PApplet parent, String calibrationYAML,
            String calibrationARtoolkit, int width, int height) {
        try {
            // ARToolkit Plus 2.1.1
//            fr.inria.papart.procam.Utils.convertARParam(parent, calibrationYAML, calibrationData, width, height);
            // ARToolkit Plus 2.3.0
            fr.inria.papart.procam.Utils.convertARParam2(parent, calibrationYAML, calibrationARtoolkit);
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

    public Camera(PApplet parent, String camDevice, String calibrationYAML, int videoInputType) {
        // Resolution is taken from the YAML file.
        this(parent, camDevice, 0, 0, 60, calibrationYAML, videoInputType);
    }

    public Camera(PApplet parent, String camDevice,
            int width, int height, int frameRate, String calibrationYAML, int videoInputType) {

        this.parent = parent;
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.videoInputType = videoInputType;

        if (calibrationYAML != null) {
            // Load the camera parameters. 
            try {
                pdp = ProjectiveDeviceP.loadCameraDevice(calibrationYAML, 0);
                camIntrinsicsP3D = pdp.getIntrinsics();
//                System.out.println("Calibration loaded for camera " + camDevice);
                this.width = pdp.getWidth();
                this.height = pdp.getHeight();
            } catch (Exception e) {
                parent.die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
            }
        }

        // Init the video
        if (videoInputType == OPENCV_VIDEO) {
            System.out.println("Camera: Grabbing OpenCV Camera");
            OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(Integer.parseInt(camDevice));
            grabberCV.setImageWidth(width);
            grabberCV.setImageHeight(height);
            grabberCV.setImageMode(FrameGrabber.ImageMode.COLOR);

            try {
                grabberCV.start();
                System.out.println("Camera: " + camDevice + " Camera started");
            } catch (Exception e) {
                System.err.println("Could not start frameGrabber... " + e);
            }
            this.grabber = grabberCV;
        }

        if (videoInputType == KINECT_VIDEO) {
            openKinectGrabber = new OpenKinectFrameGrabber(Integer.parseInt(camDevice));

            // TODO: check 640 * 480... ?
            openKinectGrabber.setImageWidth(width);
            openKinectGrabber.setImageHeight(height);

            try {
                openKinectGrabber.start();
                openKinectGrabber.setVideoFormat(0);
                openKinectGrabber.setDepthFormat(1);

            } catch (Exception e) {
                System.err.println("Could not Kinect start frameGrabber... " + e);
            }

            this.grabber = openKinectGrabber;
        }

//        if (videoInputType == PSEYE_VIDEO) {
//
//            PS3Logger.TYPE.DEBUG.active(false);
//            PS3Logger.TYPE.ERROR.active(false);
//            PS3Logger.TYPE.INFO.active(false);
//            PS3Logger.TYPE.WARNING.active(false);
//
//            ps3 = PS3.create(Integer.parseInt(camDevice));
//            ps3.init(VIDEO_MODE.VGA, COLOR_MODE.COLOR_PROCESSED, 30);
//            ps3.start();
//            ps3.setLed(true);
//        }
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

    }

    public static Camera loadCamera(PApplet applet, String file, String calibration) {
        String[] lines = applet.loadStrings(file);
        return new Camera(applet, lines[0], calibration, Integer.parseInt(lines[1]));
    }

//    public void savePlane(String filename) {
//        String[] lines = new String[7];
//        lines[0] = "" + plane.x;
//        lines[1] = "" + plane.y;
//        lines[2] = "" + plane.z;
//        lines[3] = "" + plane.normal.x;
//        lines[4] = "" + plane.normal.y;
//        lines[5] = "" + plane.normal.z;
//        lines[6] = "" + planeHeight;
//        Kinect.CURRENTPAPPLET.saveStrings(filename, lines);
//        Kinect.CURRENTPAPPLET.println("Plane successfully saved");
//    }
//    
//    @Override
//    public String toString(){
//        return "Plane " + plane + " height " + planeHeight;
//    }
//
//    public void loadPlane(String fileName) {
//        String[] lines = Kinect.CURRENTPAPPLET.loadStrings(fileName);
//        Vec3D pos = new Vec3D(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]));
//        Vec3D norm = new Vec3D(Float.parseFloat(lines[3]), Float.parseFloat(lines[4]), Float.parseFloat(lines[5]));
//        planeHeight = Float.parseFloat(lines[6]);
//
//        plane = new Plane(pos, norm);
//        Kinect.CURRENTPAPPLET.println("Plane " + fileName + " successfully loaded");
//    }
    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public boolean useProcessingVideo() {
        return this.videoInputType == PROCESSING_VIDEO;
    }

    public boolean useOpenCV() {
        return this.videoInputType == OPENCV_VIDEO;
    }

//    public boolean usePSEYE() {
//        throw new Exception("PSEye Not supported anymore");
//    }
    public boolean useKinect() {
        return this.videoInputType == KINECT_VIDEO;
    }

    public int getFrameRate() {
        return this.frameRate;
    }

    // Legacy, use the two next functions.
    public void initMarkerDetection(PApplet applet, String calibrationARToolkit, MarkerBoard[] paperSheets) {
        initMarkerDetection(calibrationARToolkit);

        for (MarkerBoard b : paperSheets) {
            trackMarkerBoard(b);
        }
    }

    // Legacy, use trackMarkerBoard now. 
    public void initMarkerDetection(String calibrationARToolkit) {
        // Marker Detection and view
        this.calibrationARToolkit = calibrationARToolkit;
        this.trackedViews = new ArrayList<TrackedView>();
        this.sheets = new ArrayList<MarkerBoard>();
    }

    public void trackMarkerBoard(MarkerBoard sheet) {
        sheet.addTracker(parent, this);
        this.sheets.add(sheet);
    }

    public boolean tracks(MarkerBoard board) {
        return this.sheets.contains(board);
    }

    public ArrayList<MarkerBoard> getTrackedSheets() {
        return this.sheets;
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
            thread = new ARTThread(this, sheets, undistort);
            thread.setCompute(this.trackSheets);
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
     * Deprecated : use trackSheets instead
     *
     * @param auto
     */
    public void setAutoUpdate(boolean auto) {
        this.trackSheets(auto);
    }

    /**
     * If the video is threaded, this sets if the tracking is on or not.
     *
     * @param auto automatic Tag detection: ON if true.
     */
    public void trackSheets(boolean auto) {
        this.trackSheets = auto;

        if (thread != null) {
            thread.setCompute(auto);
        } else {
            System.err.println("Camera: Error AutoCompute only if threaded.");
        }
    }

    public boolean useThread() {
        return thread != null;
    }
    private TouchInput touchInput = null;
    private boolean isGrabbingDepth = false;

    public void setTouch(TouchInput touchInput) {
        if (!this.useKinect()) {
            System.err.println("ERROR: SetTouch must be used with KINECT ONLY");
        }
        grabDepthImage(true);
        this.touchInput = touchInput;

    }

    public void grabDepthImage(boolean isGrabDepth) {
        if (!this.useKinect()) {
            System.err.println("ERROR: SetTouch must be used with KINECT ONLY");
        }
        this.isGrabbingDepth = isGrabDepth;
    }

    /**
     * Asks the camera to grab an image. Not to use with the threaded option.
     */
    public void grab() {
        grab(pdp != null);
    }

    public IplImage grab(boolean undistort) {

        if (isClosing) {
            return iimg;
        }

        IplImage img = null;

        if (videoInputType == OPENCV_VIDEO) {
            try {
                img = grabber.grab();

            } catch (Exception e) {
                System.err.println("Camera: OpenCV Grab() Error ! " + e);
                e.printStackTrace();
                return null;
            }
        }

        if (videoInputType == KINECT_VIDEO) {
            try {
                img = openKinectGrabber.grabVideo();

                if (isGrabbingDepth) {
                    IplImage dimg = openKinectGrabber.grabDepth();

                    this.depthImage = dimg;
                    if (touchInput != null) {
                        touchInput.lock();
                        touchInput.startTouch(dimg);
                        touchInput.getTouch2DColors(img);
                        touchInput.endTouch();
                        touchInput.unlock();
                    }
                } else {
                    if (touchInput != null) {
                        System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
                    }
                }

            } catch (Exception e) {
                System.err.println("Camera: Kinect Grab() Error ! " + e);
                e.printStackTrace();
                return null;
            }
        }

        if (videoInputType == PROCESSING_VIDEO) {
            if (this.captureIpl.available()) {

                captureIpl.read();
                img = captureIpl.getIplImage();

            } else {
                try {
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
                // Workaround for crash when the java program is closing
                // to avoid native code to continue to run...
                if (isClosing) {
                    return img;
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

    public PImage getImage() {
        return getPImage();
    }

    class Dummy {

        public synchronized void disposeBuffer(Object buf) {
            System.out.println("Dispose !");
//    ((Buffer)buf).dispose();
        }
    }

    Dummy dummy = new Dummy();
    ByteBuffer argbBuffer;

    /* TODO: Performance measure of Texture method vs pixel method */
    public PImage getPImage() {
        imageRetreived();

        if (useProcessingVideo()) {
            return captureIpl;
        }

        if (camImage == null) {

            if (iimg == null) {
                return null;
            }
            // First method, through PImage pixels
//            camImage = parent.createImage(width, height, RGB);
            // Second Method, with the Texture Object
            camImage = new CamImage(parent, iimg);
        }

        if (useOpenCV() || useKinect()) {
            if (iimg != null) {
                // First Method
//                Utils.IplImageToPImage(iimg, false, camImage);

                // Second method
                camImage.update(iimg);
            }
        }
        return camImage;
    }

    /**
     * Check the use of this
     *
     * @return
     */
    public IplImage getIplImage() {
        imageRetreived();
        return iimg;
    }

    /**
     * Check the use of this
     *
     * @return
     */
    public IplImage getDepthIplImage() {
        return depthImage;
    }

    /**
     * Check the use of this
     *
     * @return
     */
    public PImage getDepthPImage() {

        assert (useKinect());
        if (depthPImage == null) {
            depthPImage = parent.createImage(width, height, PApplet.ALPHA);
        }

        if (depthImage != null) {
            Utils.IplImageToPImageKinect(depthImage, false, depthPImage);
        }
        return depthPImage;
    }

    public ProjectiveDeviceP getProjectiveDevice() {
        return this.pdp;
    }

    public void close() {

        this.isClosing = true;

        if (videoInputType == OPENCV_VIDEO) {
            if (grabber != null) {
                try {
                    this.stopThread();
                    grabber.stop();
                } catch (Exception e) {
                }
            }
        }
        if (videoInputType == PROCESSING_VIDEO) {

            if (captureIpl != null) {
                captureIpl.stop();
            }
        }

//        if (videoInputType == PSEYE_VIDEO) {
//            PS3.shutDown();
//        }
    }

    /**
     * To use instead of getCamViewpoint
     *
     * @param point
     * @return
     */
    public PVector getViewPoint(PVector point) {
        return getCamViewPoint(point);
    }

    /**
     * Gets the 2D location in the image of a 3D point. TODO: undistort ?
     *
     * @param pt 3D point seen by the camera.
     * @return 2D location of the 3D point in the image.
     */
    @Deprecated
    public PVector getCamViewPoint(PVector pt) {
        PVector tmp = new PVector();
        camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
        //TODO: lens distorsion ?
        return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }
    private CvMat internalParams = null;

    public PMatrix3D estimateOrientation(PVector[] objectPoints,
            PVector[] imagePoints) {

        return pdp.estimateOrientation(objectPoints, imagePoints);
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
     * Check the use
     *
     * @param trackedView
     * @return
     */
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

        trackedView.computeCorners(this);
        return trackedView.getImageIpl(iimg);
    }

    /**
     * *******************************
     */
    /// TODO: Check the end of the code 
    /**
     * Used only in photoTaker...
     *
     * @param pimg
     * @param undist
     */
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
    protected boolean photoCapture;

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
