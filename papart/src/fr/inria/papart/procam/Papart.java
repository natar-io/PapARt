/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.CameraConfiguration;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import org.bytedeco.javacpp.freenect;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.calibration.ScreenConfiguration;
import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.depthcam.KinectDepthAnalysis;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.panel.Panel;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.reflections.Reflections;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Plane;

/**
 *
 * @author jiii
 */
public class Papart {

    public final static String folder = fr.inria.papart.procam.Utils.getPapartFolder();
    public final static String calibrationFolder = folder + "/data/calibration/";
    public final static String markerFolder = folder + "/data/markers/";

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
    public static String tablePosition = calibrationFolder + "tablePosition.xml";
    public static String planeCalib = calibrationFolder + "PlaneCalibration.xml";
    public static String homographyCalib = calibrationFolder + "HomographyCalibration.xml";
    public static String planeAndProjectionCalib = calibrationFolder + "PlaneProjectionCalibration.xml";
    public static String touchCalib = calibrationFolder + "Touch2DCalibration.xml";
    public static String touchCalib3D = calibrationFolder + "Touch3DCalibration.xml";
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

    private BaseDisplay display;
    private ARDisplay arDisplay;
    private ProjectorDisplay projector;

    private Camera cameraTracking;
    private KinectDepthAnalysis kinectDepthAnalysis;


    private PVector frameSize = new PVector();
    private CameraOpenKinect cameraOpenKinect;
    private boolean isWithoutCamera = false;

    public CameraConfiguration cameraConfiguration;
    public ScreenConfiguration screenConfiguration;
    // TODO: find what to do with these...
//    private final int depthFormat = freenect.FREENECT_DEPTH_10BIT;
//    private final int kinectFormat = Kinect.KINECT_10BIT;
    private final int depthFormat = freenect.FREENECT_DEPTH_MM;
    private final int kinectFormat = Kinect.KINECT_MM;

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
        }
    }

    private static CameraConfiguration getDefaultCameraConfiguration(PApplet applet) {
        CameraConfiguration config = new CameraConfiguration();
        config.loadFrom(applet, cameraConfig);
        return config;
    }

    private static ScreenConfiguration getDefaultScreenConfiguration(PApplet applet) {
        ScreenConfiguration config = new ScreenConfiguration();
        config.loadFrom(applet, screenConfig);
        return config;
    }

    /**
     * Start a projection with a procam, it replaces size().
     *
     * @param applet
     * @return
     */
    public static Papart projection(PApplet applet) {

        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);

        removeFrameBorder(applet);

        Papart papart = new Papart(applet);

        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight());
        papart.shouldSetWindowLocation = true;
        papart.shouldSetWindowSize = true;
        papart.registerPost();
        papart.initProjectorCamera();

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
        papart.initProjectorDisplay(1);

        return papart;
    }

    /**
     * Start a see through AR application, it replaces size().
     *
     * @param applet$
     * @return
     */
    public static Papart seeThrough(PApplet applet) {

        CameraConfiguration cameraConfiguration = getDefaultCameraConfiguration(applet);

        Camera cameraTracking = CameraFactory.createCamera(
                cameraConfiguration.getCameraType(),
                cameraConfiguration.getCameraName());
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);

        Papart papart = new Papart(applet);

        papart.frameSize.set(cameraTracking.width(), cameraTracking.height());
        papart.shouldSetWindowSize = true;
        papart.registerPost();

        papart.initCamera();

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

    private boolean shouldSetWindowLocation = false;
    private boolean shouldSetWindowSize = false;

    private void registerPost() {
        applet.registerMethod("post", this);
    }

    /**
     * Places the window at the correct location if required, according to the
     * configuration.
     *
     */
    public static void checkWindowLocation() {
        Papart papart = getPapart();
        if (papart != null && papart.shouldSetWindowLocation) {
            papart.defaultFrameLocation();
            papart.shouldSetWindowLocation = false;
        }
        if (papart != null && papart.shouldSetWindowSize) {

            papart.setFrameSize();
            papart.shouldSetWindowSize = true;
        }
    }

    /**
     * Does not draw anything, it used only to check the window location.
     */
    public void post() {
        checkWindowLocation();
        applet.unregisterMethod("post", this);
    }

    /**
     * Set the frame to default location.
     */
    public void defaultFrameLocation() {
        System.out.println("Setting the frame location...");
        this.applet.frame.setLocation(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());
    }

    /**
     * Set the frame to default location.
     */
    public void setFrameSize() {
        System.out.println("Trying to set the size of the frame...");
//        this.applet.frame.setSize((int) frameSize.x, (int) frameSize.y);
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
        paperScreen.screen.setMainLocation(HomographyCalibration.getMatFrom(applet, tablePosition));
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
                cameraConfiguration.getCameraType(), 1);
    }

    public void initProjectorCamera(String cameraNo, Camera.Type cameraType) {
        initProjectorCamera(cameraNo, cameraType, 1);
    }

    /**
     * Load a projector & camera couple. Default configuration files are used.
     *
     * @param quality
     * @param cameraNo
     * @param cameraType
     */
    public void initProjectorCamera(String cameraNo, Camera.Type cameraType, float quality) {
        assert (!cameraInitialized);
        initProjectorDisplay(quality);
        tryLoadExtrinsics();
        cameraTracking = CameraFactory.createCamera(cameraType, cameraNo);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(cameraCalib);
        cameraTracking.start();
        loadTracking(cameraCalib);
        cameraTracking.setThread();
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

        cameraTracking = CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(kinectRGBCalib);
        ((CameraOpenKinect) cameraTracking).getDepthCamera().setCalibration(kinectIRCalib);
        cameraTracking.start();
        cameraOpenKinect = (CameraOpenKinect) cameraTracking;
        loadTracking(kinectRGBCalib);
        cameraTracking.setThread();

        initARDisplay(quality);

        checkInitialization();
    }

    /**
     * Initialize the default camera for object tracking.
     *
     * @see initCamera(String, int, float)
     */
    public void initCamera() {
        initCamera(cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraType(), 1);
    }

    /**
     * Initialize a camera for object tracking.
     *
     * @see initCamera(String, int, float)
     */
    public void initCamera(String cameraNo, Camera.Type cameraType) {
        initCamera(cameraNo, cameraType, 1);
    }

    public void initCamera(String cameraNo, Camera.Type cameraType, float quality) {
        assert (!cameraInitialized);

        cameraTracking = CameraFactory.createCamera(cameraType, cameraNo);
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
     * @param touch2DPrecision
     * @param touch3DPrecision
     */
    public void loadTouchInputKinectOnly() {

        if (this.cameraOpenKinect == null) {
            loadDefaultCameraKinect();
            cameraTracking = cameraOpenKinect;
            cameraInitialized = true;
            checkInitialization();
        }
        loadDefaultTouchKinect();
        ((KinectTouchInput) this.touchInput).useRawDepth(cameraOpenKinect);
    }

    /**
     * *
     * Touch input with a Kinect calibrated with the display area.
     *
     * @param touch2DPrecision
     * @param touch3DPrecision
     */
    public void loadTouchInput() {

        loadDefaultCameraKinect();
        loadDefaultTouchKinect();
    }

    private void loadDefaultCameraKinect() {
        cameraOpenKinect = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        cameraOpenKinect.setParent(applet);
        cameraOpenKinect.setCalibration(kinectRGBCalib);
        cameraOpenKinect.getDepthCamera().setCalibration(kinectIRCalib);
        cameraOpenKinect.getDepthCamera().setDepthFormat(depthFormat);
        cameraOpenKinect.start();
        cameraOpenKinect.setThread();
    }

    private void loadDefaultTouchKinect() {

        kinectDepthAnalysis = new KinectDepthAnalysis(this.applet, cameraOpenKinect);
        kinectDepthAnalysis.setStereoCalibration(kinectStereoCalib);

        PlaneAndProjectionCalibration calibration = new PlaneAndProjectionCalibration();
        calibration.loadFrom(this.applet, planeAndProjectionCalib);

        KinectTouchInput kinectTouchInput
                = new KinectTouchInput(this.applet,
                        cameraOpenKinect,
                        kinectDepthAnalysis, calibration);

        cameraOpenKinect.setTouch(kinectTouchInput);

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

        Set<Class<? extends PaperTouchScreen>> paperTouchScreenClasses = reflections.getSubTypesOf(PaperTouchScreen.class);
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
        if (touchInitialized && cameraOpenKinect != null) {
            cameraOpenKinect.close();
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

    public DepthAnalysis getKinect() {
        return kinectDepthAnalysis;
    }

    public CameraOpenKinect getKinectCamera() {
        return this.cameraOpenKinect;
    }

    public PApplet getApplet() {
        return applet;
    }

}
