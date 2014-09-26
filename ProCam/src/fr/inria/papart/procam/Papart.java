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
import fr.inria.papart.kinect.Kinect;
import fr.inria.papart.multitouchKinect.TouchInput;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;
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

    protected static Papart singleton = null;

    protected float zNear = 10;
    protected float zFar = 6000;

    private final PApplet applet;
    private final Class appletClass;

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

    public Papart(Object applet) {
        this.displayInitialized = false;
        this.cameraInitialized = false;
        this.touchInitialized = false;
        this.applet = (PApplet) applet;

        this.appletClass = applet.getClass();
        PFont font = this.applet.loadFont(defaultFont);
        Button.setFont(font);
        Button.setFontSize(defaultFontSize);
        if (Papart.singleton == null) {
            Papart.singleton = this;
        }
    }

    public static Papart getPapart() {
        return Papart.singleton;
    }

    public void loadSketches() {

        // Sketches are not within a package.
        Reflections reflections = new Reflections("");

        
        Set<Class<? extends PaperTouchScreen>> paperTouchScreenClasses = reflections.getSubTypesOf(PaperTouchScreen.class);
        for (Class<? extends PaperTouchScreen> klass : paperTouchScreenClasses) {
            try {
                Class[] ctorArgs2 = new Class[1];
                ctorArgs2[0] = this.appletClass;
                Constructor<? extends PaperTouchScreen> constructor = klass.getDeclaredConstructor(ctorArgs2);
                System.out.println("Starting a PaperTouchScreen. " + klass.getName());
                constructor.newInstance(this.appletClass.cast(this.applet));
//            } catch (InstantiationException ex) {
//                Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IllegalAccessException ex) {
//                Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (IllegalArgumentException ex) {
//                Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (InvocationTargetException ex) {
//                Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (NoSuchMethodException ex) {
//                Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (SecurityException ex) {
//                Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
//            }
            } catch (Exception ex) {
                System.out.println("Error loading PapartTouchApp : " + klass.getName());
            }
        }

        Set<Class<? extends PaperScreen>> paperScreenClasses = reflections.getSubTypesOf(PaperScreen.class);

        // Add them once.
        paperScreenClasses.removeAll(paperTouchScreenClasses);
        for (Class<? extends PaperScreen> klass : paperScreenClasses) {
            try {
                Class[] ctorArgs2 = new Class[1];
                ctorArgs2[0] = this.appletClass;
                Constructor<? extends PaperScreen> constructor = klass.getDeclaredConstructor(ctorArgs2);
                System.out.println("Starting a PaperScreen. " + klass.getName());
                constructor.newInstance(this.appletClass.cast(this.applet));
            } catch (Exception ex) {
                System.out.println("Error loading PapartApp : " + klass.getName());
            }
        }

    }

    public void loadSketches(Class[] sketches) {

        for (Class klass : sketches) {
            
             System.out.println("First subclass. " + klass.getName());
            Class[] ctorArgs2 = new Class[1];
            ctorArgs2[0] = this.appletClass;

            if (klass.getSuperclass() == PaperTouchScreen.class) {

                try {
                    Constructor<? extends PaperTouchScreen> constructor = klass.getDeclaredConstructor(ctorArgs2);
                    System.out.println("Starting a PaperTouchScreen. " + klass.getName());
                    constructor.newInstance(this.appletClass.cast(this.applet));
                } catch (InstantiationException ex) {
                    Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                if (klass.getSuperclass() == PaperScreen.class) {

                }
            }

        }

    }

    /**
     * Load a projector & camera couple. Default configuration files are used.
     *
     * @param quality
     * @param cameraNo
     * @param cameraType
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

    public void startTracking() {
        this.cameraTracking.trackSheets(true);
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

    public PApplet getApplet() {
        return applet;
    }

}
