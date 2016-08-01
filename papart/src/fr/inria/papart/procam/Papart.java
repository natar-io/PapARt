/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole.
 *
 * No licence yet.
 */
package fr.inria.papart.procam;

import com.jogamp.newt.opengl.GLWindow;
import fr.inria.papart.calibration.CalibrationPopup;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.CameraConfiguration;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import org.bytedeco.javacpp.freenect;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.calibration.ScreenConfiguration;
import fr.inria.papart.depthcam.devices.Kinect360;
import fr.inria.papart.depthcam.analysis.KinectDepthAnalysis;
import fr.inria.papart.depthcam.devices.KinectDevice;
import fr.inria.papart.depthcam.devices.KinectOne;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.panel.Panel;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.awt.Robot;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.reflections.Reflections;
import processing.awt.PSurfaceAWT;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.opengl.PSurfaceJOGL;
import toxi.geom.Plane;

/**
 *
 * @author jiii
 */
public class Papart {

    public final static String folder = fr.inria.papart.procam.Utils.getPapartFolder() + "/data/";
    public final static String calibrationFolder = folder + "calibration/";
    public final static String markerFolder = folder + "markers/";

    public static String cameraCalibName = "camera.yaml";
    public static String projectorCalibName = "projector.yaml";

    public static String cameraCalib = calibrationFolder + cameraCalibName;
    public static String projectorCalib = calibrationFolder + projectorCalibName;

    public static String camCalibARtoolkit = calibrationFolder + "camera-projector.cal";
    public static String kinectIRCalib = calibrationFolder + "calibration-kinect-IR.yaml";
    public static String kinectRGBCalib = calibrationFolder + "calibration-kinect-RGB.yaml";
    public static String kinectStereoCalib = calibrationFolder + "calibration-kinect-Stereo.xml";

    public static String kinectTrackingCalib = "kinectTracking.xml";
    public static String cameraProjExtrinsics = "camProjExtrinsics.xml";

    public static String screenConfig = calibrationFolder + "screenConfiguration.xml";
    public static String cameraConfig = calibrationFolder + "cameraConfiguration.xml";
    public static String cameraKinectConfig = calibrationFolder + "cameraKinectConfiguration.xml";

    public static String tablePosition = calibrationFolder + "tablePosition.xml";
    public static String planeCalib = calibrationFolder + "PlaneCalibration.xml";
    public static String homographyCalib = calibrationFolder + "HomographyCalibration.xml";
    public static String planeAndProjectionCalib = calibrationFolder + "PlaneProjectionCalibration.xml";
    public static String touchCalib = calibrationFolder + "Touch2DCalibration.xml";
    public static String touchCalib3D = calibrationFolder + "Touch3DCalibration.xml";
    public static String defaultFont = folder + "Font/" + "GentiumBookBasic-48.vlw";
    public int defaultFontSize = 12;

    protected static Papart singleton = null;

    protected float zNear = 10;
    protected float zFar = 6000;

    private final PApplet applet;
    private final Class appletClass;

    private boolean displayInitialized;
    private boolean cameraInitialized;
    private boolean touchInitialized;

    private BaseDisplay display;
    private ARDisplay arDisplay;
    private ProjectorDisplay projector;

    private Camera cameraTracking;
    private KinectDepthAnalysis kinectDepthAnalysis;

    private TouchInput touchInput;
    private PVector frameSize = new PVector();
    private boolean isWithoutCamera = false;

    public KinectDevice kinectDevice;

    public CameraConfiguration cameraConfiguration;
    public ScreenConfiguration screenConfiguration;
    // TODO: find what to do with these...
//    private final int depthFormat = freenect.FREENECT_DEPTH_10BIT;
//    private final int kinectFormat = Kinect.KINECT_10BIT;
    private final int depthFormat = freenect.FREENECT_DEPTH_MM;
    private final int kinectFormat = Kinect360.KINECT_MM;

    /**
     * Create the main PapARt object, look at the examples for how to use it.
     *
     * @param applet
     */
    public Papart(Object applet) {
        this.displayInitialized = false;
        this.cameraInitialized = false;
        this.touchInitialized = false;
        this.applet = (PApplet) applet;

        cameraConfiguration = getDefaultCameraConfiguration(this.applet);
        screenConfiguration = getDefaultScreenConfiguration(this.applet);

        this.appletClass = applet.getClass();
        PFont font = this.applet.loadFont(defaultFont);
        Button.setFont(font);
        Button.setFontSize(defaultFontSize);
        // TODO: singleton -> Better implementation.
        if (Papart.singleton == null) {
            Papart.singleton = this;
            fr.inria.papart.drawingapp.DrawUtils.applet = (PApplet) applet;
        }
    }

    public static CameraConfiguration getDefaultCameraConfiguration(PApplet applet) {
        CameraConfiguration config = new CameraConfiguration();
        config.loadFrom(applet, cameraConfig);
        return config;
    }

    public static CameraConfiguration getDefaultKinectConfiguration(PApplet applet) {
        CameraConfiguration config = new CameraConfiguration();
        config.loadFrom(applet, cameraKinectConfig);
        return config;
    }

    public static ScreenConfiguration getDefaultScreenConfiguration(PApplet applet) {
        ScreenConfiguration config = new ScreenConfiguration();
        config.loadFrom(applet, screenConfig);
        return config;
    }

    private CalibrationPopup calibrationPopup = null;

    public void calibration() {
        if (calibrationPopup == null) {
            calibrationPopup = new CalibrationPopup();
        } else if (calibrationPopup.isHidden()) {
            calibrationPopup.show();
        } else {
            calibrationPopup.hide();
        }
    }

    /**
     * Start a projection with a procam, it replaces size().
     *
     * @param applet
     * @return
     */
    public static Papart projection(PApplet applet) {
        return projection(applet, 1f);
    }

    /**
     * Start a projection with a procam, it replaces size().
     *
     * @param applet
     * @return
     */
    public static Papart projection(PApplet applet, float quality) {

        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);

        removeFrameBorder(applet);

        Papart papart = new Papart(applet);

        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight());
        papart.shouldSetWindowLocation = true;
        papart.shouldSetWindowSize = true;
        papart.registerPost();
        papart.initProjectorCamera(quality);
        papart.registerKey();

        return papart;
    }

    /**
     * Start a projection with a procam, it replaces size().
     *
     * @param applet
     * @return
     */
    public static Papart projectionOnly(PApplet applet) {

        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);

        removeFrameBorder(applet);

        Papart papart = new Papart(applet);

        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight());
        papart.shouldSetWindowLocation = true;
        papart.shouldSetWindowSize = true;
        papart.registerPost();
        papart.initProjectorDisplay(1.5f);

        return papart;
    }

    /**
     * Start a see through AR application, it replaces size().
     *
     * @param applet
     * @return
     */
    public static Papart seeThrough(PApplet applet) {
        return seeThrough(applet, 1);
    }

    /**
     * Start a see through AR application, it replaces size().
     *
     * @param applet
     * @return
     */
    public static Papart seeThrough(PApplet applet, float quality) {

        CameraConfiguration cameraConfiguration = getDefaultCameraConfiguration(applet);

        Camera cameraTracking = CameraFactory.createCamera(
                cameraConfiguration.getCameraType(),
                cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraFormat());
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);

        Papart papart = new Papart(applet);

        papart.frameSize.set(cameraTracking.width(), cameraTracking.height());
        papart.shouldSetWindowSize = true;
        papart.registerPost();

        papart.initCamera(quality);

        return papart;
    }

    /**
     * Start a 2D projection in screen space, it replaces size() .
     *
     * @param applet
     * @return
     */
    public static Papart projection2D(PApplet applet) {

        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);

        removeFrameBorder(applet);

        Papart papart = new Papart(applet);

        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight());
        papart.shouldSetWindowLocation = true;
        papart.shouldSetWindowSize = true;
        papart.registerPost();

//        Panel panel = new Panel(applet);
        return papart;
    }

    public static KinectDevice loadDefaultKinectDevice(PApplet applet) {
        CameraConfiguration kinectConfiguration = Papart.getDefaultKinectConfiguration(applet);

        if (kinectConfiguration.getCameraType() == Camera.Type.OPEN_KINECT) {
            return new Kinect360(applet);
        }
        if (kinectConfiguration.getCameraType() == Camera.Type.KINECT2_RGB) {
            return new KinectOne(applet);
        }
        System.err.println("Papart: Could not identify default Kinect Device.");
        return null;
    }

    private boolean shouldSetWindowLocation = false;
    private boolean shouldSetWindowSize = false;

    private void registerPost() {
        applet.registerMethod("post", this);
    }

    private void registerKey() {
        applet.registerMethod("keyEvent", this);
    }

    public void keyEvent(KeyEvent e) {
        // disabled for now.
//        if (e.getKey() == 'c') {
//            calibration();
//        }
    }

    public void forceCameraSize() {
        forceWindowSize(cameraTracking.width(),
                cameraTracking.height());
    }

    public void forceDepthCameraSize() {
        forceWindowSize(kinectDevice.depthWidth(),
                kinectDevice.depthHeight());
    }

    public void forceWindowSize(int w, int h) {

        Papart papart = Papart.getPapart();

        papart.shouldSetWindowLocation = true;
        papart.shouldSetWindowSize = true;
        papart.registerPost();
        
        frameSize.set(w, h);
//        this.shouldSetWindowSize = true;
//        registerPost();
//
//        GLWindow window = (GLWindow) applet.getSurface().getNative();
//        window.setUndecorated(false);
//        window.setSize(w, h);
    }

    public void forceProjectorSize() {
        frameSize.set(projector.getWidth(),
                projector.getHeight());
//        this.shouldSetWindowSize = true;
//        registerPost();

        GLWindow window = (GLWindow) applet.getSurface().getNative();
        window.setUndecorated(true);
        window.setSize(projector.getWidth(),
                projector.getHeight());
    }

    public void forceProjectorSize(int w, int h, int px, int py) {
        frameSize.set(w, h);
//        this.shouldSetWindowSize = true;
//        registerPost();

        GLWindow window = (GLWindow) applet.getSurface().getNative();
        window.setUndecorated(true);
        window.setSize(w, h);
        window.setPosition(px, py);
    }

    /**
     * Places the window at the correct location if required, according to the
     * configuration.
     *
     */
    public static void checkWindowLocation() {
        Papart papart = getPapart();

        if (papart == null) {
            return;
        }
        if (papart.shouldSetWindowLocation) {
            papart.defaultFrameLocation();
        }
        if (papart.shouldSetWindowSize) {
            papart.setFrameSize();
        }
        papart.shouldSetWindowLocation = false;
        papart.shouldSetWindowSize = false;
    }

    /**
     * Does not draw anything, it used only to check the window location.
     */
    public void post() {
        checkWindowLocation();
        System.out.println("Post");
        applet.unregisterMethod("post", this);
    }

    /**
     * Set the frame to default location.
     */
    public void defaultFrameLocation() {
        System.out.println("Setting the frame location...");
        this.applet.frame.setLocation(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());

        GLWindow window = (GLWindow) applet.getSurface().getNative();
        window.setPosition(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());
    }

    /**
     * Set the frame to current valid location.
     */
    public void setFrameSize() {
        this.applet.getSurface().setSize((int) frameSize.x, (int) frameSize.y);
    }

    protected static void removeFrameBorder(PApplet applet) {
        if (!applet.g.isGL()) {
            applet.frame.removeNotify();
            applet.frame.setUndecorated(true);
            applet.frame.addNotify();
        }
    }

    /**
     * Get the Papart singleton.
     *
     * @return
     */
    public static Papart getPapart() {
        return Papart.singleton;
    }

    public void saveCalibration(String fileName, PMatrix3D mat) {
        HomographyCalibration.saveMatTo(applet, mat, Papart.calibrationFolder + fileName);
    }

    /**
     * Saves a 3D transformation matrix (such as a paper location).
     *
     * @param fileName
     * @param mat
     */
    public void saveLocationTo(String fileName, PMatrix3D mat) {
        HomographyCalibration.saveMatTo(applet, mat, fileName);
    }

    /**
     * Get a calibration from sketchbook/libraries/PapARt/data/calibration
     * folder.
     *
     * @param fileName
     * @return null if the file does not exists.
     */
    public PMatrix3D loadCalibration(String fileName) {

        File f = new File(Papart.calibrationFolder + fileName);
        if (f.exists()) {
            return HomographyCalibration.getMatFrom(applet, Papart.calibrationFolder + fileName);
        } else {
            return null;
        }
    }

    /**
     * Save the position of a paperScreen as the default table location.
     *
     * @param paperScreen
     */
    public void setTableLocation(PaperScreen paperScreen) {
        HomographyCalibration.saveMatTo(applet, paperScreen.getLocation(), tablePosition);
    }

    /**
     * Save the position of a matrix the default table location.
     *
     * @param mat
     */
    public void setTableLocation(PMatrix3D mat) {
        HomographyCalibration.saveMatTo(applet, mat, tablePosition);
    }

    /**
     * Use if the table location is relative to the projector.
     *
     * @return
     */
    public PMatrix3D getTableLocationFromProjector() {
        PMatrix3D extr = getProjectorDisplay().getExtrinsics();
        extr.invert();
        PMatrix3D table = getTableLocation();
        table.preApply(extr);
        return table;
    }

    /**
     * The location of the table, warning it must be set once by
     * setTablePosition.
     *
     * @return
     */
    public PMatrix3D getTableLocation() {
        return HomographyCalibration.getMatFrom(applet, tablePosition);
    }

    /**
     * Work in progress function
     *
     * @return
     */
    public PlaneCalibration getTablePlane() {
        return PlaneCalibration.CreatePlaneCalibrationFrom(HomographyCalibration.getMatFrom(applet, tablePosition),
                new PVector(100, 100));
    }

    /**
     * Move a PaperScreen to the table location. After this, the paperScreen
     * location is not updated anymore. To activate the tracking again use :
     * paperScreen.useManualLocation(false); You can move the paperScreen
     * according to its current location with the paperScreen.setLocation()
     * methods.
     *
     * @param paperScreen
     */
    public void moveToTablePosition(PaperScreen paperScreen) {
        paperScreen.useManualLocation(true);
        paperScreen.screen.setMainLocation(HomographyCalibration.getMatFrom(applet, tablePosition), cameraTracking);
    }

    @Deprecated
    public void initNoCamera(int quality) {
        this.isWithoutCamera = true;
        initNoCameraDisplay(quality);
    }

    public void initDebug() {
        this.isWithoutCamera = true;
        initDebugDisplay();
    }

    public void initProjectorCamera() {
        initProjectorCamera(cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraType(),
                cameraConfiguration.getCameraFormat(),
                1);
    }

    public void initProjectorCamera(float quality) {
        initProjectorCamera(cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraType(), 
                cameraConfiguration.getCameraFormat(), 
                quality);
    }

    public void initProjectorCamera(String cameraNo, Camera.Type cameraType, String cameraFormat) {
        initProjectorCamera(cameraNo, cameraType, cameraFormat, 1);
    }

    /**
     * Load a projector - camera couple. Default configuration files are used.
     *
     * @param quality
     * @param cameraNo
     * @param cameraType
     */
    public void initProjectorCamera(String cameraNo, Camera.Type cameraType, String cameraFormat, float quality) {
        assert (!cameraInitialized);
        initProjectorDisplay(quality);
        tryLoadExtrinsics();
        cameraTracking = CameraFactory.createCamera(cameraType, cameraNo, cameraFormat);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);
        cameraTracking.start();
        loadTracking(cameraCalib);
        cameraTracking.setThread();
        projector.setCamera(cameraTracking);
        checkInitialization();
    }

    private void tryLoadExtrinsics() {
        PMatrix3D extrinsics = loadCalibration(cameraProjExtrinsics);
        if (extrinsics == null) {
            System.out.println("loading default extrinsics. Could not find " + cameraProjExtrinsics + " .");
        } else {
            arDisplay.setExtrinsics(extrinsics);
        }
    }

    public void initKinectCamera(float quality) {
        assert (!cameraInitialized);
        kinectDevice = loadDefaultCameraKinect();
        cameraTracking = kinectDevice.getCameraRGB();
        loadTracking(kinectRGBCalib);
        cameraTracking.setThread();
        initARDisplay(quality);
        checkInitialization();
    }

    public void startDefaultKinectCamera() {
        assert (!cameraInitialized);
        kinectDevice = loadDefaultCameraKinect();
        cameraTracking = kinectDevice.getCameraRGB();
    }

    /**
     * Initialize the default camera for object tracking.
     *
     */
    public void initCamera() {
        initCamera(1);
    }

    /**
     * Initialize the default camera for object tracking.
     *
     * @param quality default is 1, to downscale go below 1, try 0.8, for better
     * quality go higher like 2.
     */
    public void initCamera(float quality) {
        initCamera(cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraType(), 
                cameraConfiguration.getCameraFormat(), 
                quality);
    }

    /**
     * Initialize a camera for object tracking.
     *
     */
    public void initCamera(String cameraNo, Camera.Type cameraType, String cameraFormat) {
        initCamera(cameraNo, cameraType, cameraFormat, 1);
    }

    public void initCamera(String cameraNo, Camera.Type cameraType, String cameraFormat, float quality) {
        assert (!cameraInitialized);

        cameraTracking = CameraFactory.createCamera(cameraType, cameraNo, cameraFormat);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);
        cameraTracking.start();
        loadTracking(cameraCalib);
        cameraTracking.setThread();

        initARDisplay(quality);
        checkInitialization();
    }

    private void initProjectorDisplay(float quality) {
        // TODO: check if file exists !
        projector = new ProjectorDisplay(this.applet, projectorCalib);
        projector.setZNearFar(zNear, zFar);
        projector.setQuality(quality);
        arDisplay = projector;
        display = projector;
        projector.init();
        displayInitialized = true;
        frameSize.set(projector.getWidth(), projector.getHeight());
    }

    private void initARDisplay(float quality) {
        assert (this.cameraTracking != null && this.applet != null);

        arDisplay = new ARDisplay(this.applet, cameraTracking);
        arDisplay.setZNearFar(zNear, zFar);
        arDisplay.setQuality(quality);
        arDisplay.init();
        this.display = arDisplay;
        frameSize.set(arDisplay.getWidth(), arDisplay.getHeight());
        displayInitialized = true;
    }

    @Deprecated
    private void initNoCameraDisplay(float quality) {
        initDebugDisplay();
    }

    private void initDebugDisplay() {
        display = new BaseDisplay();

        display.setFrameSize(applet.width, applet.height);
        display.setDrawingSize(applet.width, applet.height);
        display.init();
        displayInitialized = true;
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
        cameraInitialized = true;
    }

    /**
     * Touch input when the camera tracking the markers is a Kinect.
     *
     */
    public void loadTouchInputKinectOnly() {

        if (this.kinectDevice == null) {
            kinectDevice = loadDefaultCameraKinect();
            cameraTracking = kinectDevice.getCameraRGB();
            kinectDevice.getCameraRGB().setThread();
            kinectDevice.getCameraDepth().setThread();
            cameraInitialized = true;

            checkInitialization();
        }

        loadDefaultTouchKinect();

        PMatrix3D extr = kinectDevice.getStereoCalibration();
        kinectDevice.setExtrinsics(extr);

        ((KinectTouchInput) this.touchInput).useRawDepth();
    }

    /**
     * *
     * Touch input with a Kinect calibrated with the display area.
     *
     */
    public void loadTouchInput() {
        kinectDevice = loadDefaultCameraKinect();
        kinectDevice.getCameraRGB().setThread();
        kinectDevice.getCameraDepth().setThread();

        loadDefaultTouchKinect();

        // setExtrinsics must after the kinect stereo calibration is loaded
        PMatrix3D extr = (Papart.getPapart()).loadCalibration(Papart.kinectTrackingCalib);
        kinectDevice.setExtrinsics(extr);
    }

    private boolean useKinectOne = true;

    public void useKinectOne(boolean kinectOne) {
        this.useKinectOne = kinectOne;
    }

    /**
     * WORK IN PROGRESS
     *
     * @return
     */
    public KinectDevice loadDefaultCameraKinect() {
        CameraConfiguration kinectConfiguration = Papart.getDefaultKinectConfiguration(applet);

        if (kinectConfiguration.getCameraType() == Camera.Type.OPEN_KINECT) {
            return new Kinect360(applet);
        }
        if (kinectConfiguration.getCameraType() == Camera.Type.KINECT2_RGB) {

            if (this.cameraTracking == null) {
                return new KinectOne(applet);
            } else {
                return new KinectOne(applet, cameraTracking);
            }
        }
        System.err.println("Could not load the Kinect !" + "Camera Type " + kinectConfiguration.getCameraType());
        return null;
    }

    private void loadDefaultTouchKinect() {

        kinectDevice.setStereoCalibration(kinectStereoCalib);
        kinectDepthAnalysis = new KinectDepthAnalysis(this.applet, kinectDevice);

        PlaneAndProjectionCalibration calibration = new PlaneAndProjectionCalibration();
        calibration.loadFrom(this.applet, planeAndProjectionCalib);

        KinectTouchInput kinectTouchInput
                = new KinectTouchInput(this.applet,
                        kinectDevice,
                        kinectDepthAnalysis, calibration);

        kinectDevice.setTouch(kinectTouchInput);

        kinectTouchInput.setTouchDetectionCalibration(getDefaultTouchCalibration());
        kinectTouchInput.setTouchDetectionCalibration3D(getDefaultTouchCalibration3D());
        this.touchInput = kinectTouchInput;
        touchInitialized = true;
    }

    public PlanarTouchCalibration getDefaultTouchCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultTouchCalibration3D() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalib3D);
        return calib;
    }

    public void loadTouchInputTUIO() {
        touchInput = new TUIOTouchInput(this.applet, 3333);
        this.touchInitialized = true;
    }

    public void loadSketches() {

        // Sketches are not within a package.
        Reflections reflections = new Reflections("");

        Set<Class<? extends PaperTouchScreen>> paperTouchScreenClasses = reflections.getSubTypesOf(PaperTouchScreen.class
        );
        for (Class<? extends PaperTouchScreen> klass : paperTouchScreenClasses) {
            try {
                Class[] ctorArgs2 = new Class[1];
                ctorArgs2[0] = this.appletClass;
                Constructor<? extends PaperTouchScreen> constructor = klass.getDeclaredConstructor(ctorArgs2);
                System.out.println("Starting a PaperTouchScreen. " + klass.getName());
                constructor.newInstance(this.appletClass.cast(this.applet));
            } catch (Exception ex) {
                System.out.println("Error loading PapartTouchApp : " + klass.getName() + ex);
                ex.printStackTrace();
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

    public void startTracking() {
        if (this.cameraTracking == null) {
            System.err.println("Start Tracking requires a Camera...");
            return;
        }
        this.cameraTracking.trackSheets(true);
    }

    public void stop() {
        this.dispose();
    }

    public void dispose() {
        if (touchInitialized && kinectDevice != null) {
            kinectDevice.close();
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

    public BaseDisplay getDisplay() {
//        assert (displayInitialized);
        return this.display;
    }

    public void setDisplay(BaseDisplay display) {
        // todo check this .
        displayInitialized = true;
        this.display = display;
    }

    public void setTrackingCamera(Camera camera) {
        this.cameraTracking = camera;
        if (camera == null) {
            setNoTrackingCamera();
        }
    }

    public void setNoTrackingCamera() {
        this.isWithoutCamera = true;
    }

    // TODO: remove these asserts...
    public ProjectorDisplay getProjectorDisplay() {
//        assert (displayInitialized);
        return this.projector;
    }

    public ARDisplay getARDisplay() {
//        assert (displayInitialized);
        return this.arDisplay;
    }

    public Camera getCameraTracking() {
//        assert (cameraInitialized);
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

    public boolean isWithoutCamera() {
        return this.isWithoutCamera;
    }

    public Camera getKinectCamera() {
        return this.kinectDevice.getCameraRGB();
    }

    public KinectDevice getKinectDevice() {
        return kinectDevice;
    }

    public KinectDepthAnalysis getKinectAnalysis() {
        return this.kinectDepthAnalysis;
    }

    public KinectDevice.Type getKinectType() {
        if (kinectDevice == null) {
            return KinectDevice.Type.NONE;

        }
        return kinectDevice.type();
    }

    public PApplet getApplet() {
        return applet;
    }

}
