/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import org.bytedeco.javacpp.freenect;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.drawingapp.DrawUtils;
import fr.inria.papart.kinect.Kinect;
import fr.inria.papart.multitouchKinect.TouchInput;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class Papart {

    public final static String folder = fr.inria.papart.procam.Utils.getPapartFolder();
    public static String proCamCalib = folder + "/data/calibration/camera-projector.yaml";
    public static String camCalibARtoolkit = folder + "/data/calibration/camera-projector.cal";
    public static String kinectIRCalib = folder + "/data/calibration/calibration-kinect-IR.yaml";
    public static String kinectRGBCalib = folder + "/data/calibration/calibration-kinect-RGB.yaml";
    public static String kinectScreenCalib = folder + "/data/calibration/KinectScreenCalibration.txt";
    public static String defaultFont = folder + "/data/Font/" + "GentiumBookBasic-48.vlw";
    public int defaultFontSize = 12;

    protected float zNear = 10;
    protected float zFar = 6000;

    private final PApplet applet;
    private boolean displayInitialized;
    private boolean cameraInitialized;
    private boolean touchInitialized;
    private ARDisplay display;
    private Projector projector;
    private Camera cameraTracking;
    private Kinect kinect;
    private TouchInput touchInput;
    private PVector frameSize;
    private OpenKinectFrameGrabber openKinectGrabber;

    public Papart(PApplet applet) {
        this.displayInitialized = false;
        this.cameraInitialized = false;
        this.touchInitialized = false;
        this.applet = applet;
        DrawUtils.applet = applet;
        PFont font = applet.loadFont(defaultFont);
        Button.setFont(font);
        Button.setFontSize(defaultFontSize);
    }

    /**
     * Load a projector & camera couple. Default configuration files are used.
     *
     * @param quality
     * @param cameraNo
     * @param cameraType
     * @return
     */
    public void initProjectorCamera(float quality, String cameraNo, int cameraType) {
        assert (!cameraInitialized);
        // TODO: check if file exists !
        projector = new Projector(this.applet, proCamCalib, zNear, zFar, quality);
        display = projector;
        displayInitialized = true;

        cameraTracking = new Camera(this.applet, cameraNo, proCamCalib, cameraType);
        loadTracking(proCamCalib);

        frameSize = new PVector(projector.getWidth(), projector.getHeight());
        checkInitialization();
    }

    public void initKinectCamera(float quality) {
        assert (!cameraInitialized);

        cameraTracking = new Camera(this.applet, "0", Kinect.KINECT_WIDTH,
                Kinect.KINECT_HEIGHT, kinectRGBCalib, Camera.KINECT_VIDEO);
        loadTracking(kinectRGBCalib);
        display = new ARDisplay(this.applet, cameraTracking,
                zNear, zFar, quality);
        displayInitialized = true;
        frameSize = new PVector(display.getWidth(), display.getHeight());
        checkInitialization();
    }

    public void initCamera(float quality, String cameraNo, int cameraType) {
        assert (!cameraInitialized);

        cameraTracking = new Camera(this.applet, cameraNo, proCamCalib, cameraType);
        loadTracking(proCamCalib);
        display = new ARDisplay(this.applet, cameraTracking,
                zNear, zFar, quality);
        frameSize = new PVector(display.getWidth(), display.getHeight());
        displayInitialized = true;
        checkInitialization();
    }

    private void checkInitialization() {
        assert (cameraTracking != null);
        this.applet.registerMethod("dispose", this);
        this.applet.registerMethod("stop", this);
    }

    private void loadTracking(String calibrationPath) {
        // TODO: check if file exists !
        Camera.convertARParams(this.applet, calibrationPath, camCalibARtoolkit);
        cameraTracking.initMarkerDetection(camCalibARtoolkit);

        // The camera view is handled in another thread;
        cameraTracking.setThread(true);
        cameraInitialized = true;
    }

    // TODO: find what to do with these...
    private final int depthFormat = freenect.FREENECT_DEPTH_10BIT;
    private final int kinectFormat = Kinect.KINECT_10BIT;

//    private final int depthFormat = freenect.FREENECT_DEPTH_MM;
//    private final int kinectFormat = Kinect.KINECT_MM;
    // OpenKinectGrabber depth format
// /** 11 bit depth information in one uint16_t/pixel */
// FREENECT_DEPTH_11BIT        = 0,
// /** 10 bit depth information in one uint16_t/pixel */
// FREENECT_DEPTH_10BIT        = 1,
// /** 11 bit packed depth information */
// FREENECT_DEPTH_11BIT_PACKED = 2,
// /** 10 bit packed depth information */
// FREENECT_DEPTH_10BIT_PACKED = 3,
// /** processed depth data in mm, aligned to 640x480 RGB */
// FREENECT_DEPTH_REGISTERED   = 4,
// /** depth to each pixel in mm, but left unaligned to RGB image */
// FREENECT_DEPTH_MM           = 5,
// /** Dummy value to force enum to be 32 bits wide */
// FREENECT_DEPTH_DUMMY        = 2147483647;
    /**
     * Touch input when the camera tracking the markers is a Kinect.
     *
     * @param touch2DPrecision
     * @param touch3DPrecision
     */
    public void loadTouchInputKinectOnly(int touch2DPrecision,
            int touch3DPrecision) {

        if (this.cameraTracking == null) {
            cameraTracking = new Camera(this.applet, "0", Kinect.KINECT_WIDTH,
                    Kinect.KINECT_HEIGHT, kinectRGBCalib, Camera.KINECT_VIDEO);
            cameraTracking.setThread(true);
            cameraInitialized = true;
            checkInitialization();
        }

        assert (this.cameraTracking.useKinect());
        kinect = new Kinect(this.applet,
                kinectIRCalib,
                kinectRGBCalib,
                0, kinectFormat);

        touchInput = new TouchInput(this.applet, kinectScreenCalib,
                cameraTracking, kinect, true, touch2DPrecision, touch3DPrecision);
        touchInitialized = true;
    }

    /**
     * *
     * Touch input with a Kinect calibrated with the display area.
     *
     * @param touch2DPrecision
     * @param touch3DPrecision
     */
    public void loadTouchInput(int touch2DPrecision, int touch3DPrecision) {
        assert (!this.cameraTracking.useKinect());

        openKinectGrabber = new OpenKinectFrameGrabber(0);

        try {
            openKinectGrabber.start();
            openKinectGrabber.setVideoFormat(0);  // rgb
            openKinectGrabber.setDepthFormat(depthFormat);  //  depth mm

        } catch (FrameGrabber.Exception e) {
            System.err.println("Kinect exception: " + e);
        }

        kinect = new Kinect(this.applet,
                kinectIRCalib,
                kinectRGBCalib,
                0, kinectFormat);

        touchInput = new TouchInput(this.applet, kinectScreenCalib,
                kinect, openKinectGrabber, false, touch2DPrecision, touch3DPrecision);
        touchInitialized = true;
    }

    public void stop() {
        this.dispose();
    }

    public void dispose() {
        if (touchInitialized && openKinectGrabber != null) {
            try {
                openKinectGrabber.stop();
            } catch (FrameGrabber.Exception e) {
                System.err.println("Error closing Kinect grabber " + e);
            }
        }
        if (cameraInitialized && cameraTracking != null) {
            try {
                cameraTracking.close();
            } catch (Exception e) {
                System.err.println("Error closing the tracking camera" + e);
            }
        }
//        System.out.println("Cameras closed.");
    }

    public ARDisplay getDisplay() {
        assert (displayInitialized);
        return this.display;
    }

    public Camera getCameraTracking() {
        assert (cameraInitialized);
        return this.cameraTracking;
    }

    public TouchInput getTouchInput() {
        assert (touchInitialized);
        return this.touchInput;
    }

    public PVector getFrameSize() {
        assert (this.frameSize != null);
        return this.frameSize.get();
    }

}
