/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.CameraConfiguration;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import org.bytedeco.javacpp.freenect;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.ScreenConfiguration;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.panel.Panel;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.reflections.Reflections;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PMatrix3D;
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
    public static String kinectStereoCalib = folder + "/data/calibration/calibration-kinect-Stereo.yaml";

    // -- computerconfig will be removed !
//    public static String computerConfig = folder + "/data/calibration/ComputerConfiguration.xml";
    public static String screenConfig = folder + "/data/calibration/screenConfiguration.xml";
    public static String cameraConfig = folder + "/data/calibration/cameraConfiguration.xml";
    public static String tablePosition = folder + "/data/calibration/tablePosition.xml";
    public static String planeCalib = folder + "/data/calibration/PlaneCalibration.xml";
    public static String homographyCalib = folder + "/data/calibration/HomographyCalibration.xml";
    public static String planeAndProjectionCalib = folder + "/data/calibration/PlaneProjectionCalibration.xml";
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
    private Kinect kinect;

//    private TouchInput touchInput;
    private TouchInput touchInput;
    private PVector frameSize;
    private CameraOpenKinect cameraOpenKinect;
    private boolean isWithoutCamera = false;

    private CameraConfiguration cameraConfiguration;
    private ScreenConfiguration screenConfiguration;
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

        applet.size(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight(),
                PConstants.OPENGL);

        Papart papart = new Papart(applet);
        papart.registerForWindowLocation();
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

        applet.size(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight(),
                PConstants.OPENGL);

        Papart papart = new Papart(applet);
        papart.registerForWindowLocation();
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
        cameraTracking.setCalibration(proCamCalib);

        applet.size(cameraTracking.width(),
                cameraTracking.height(),
                PConstants.OPENGL);

        Papart papart = new Papart(applet);

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

        applet.size(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight(),
                PConstants.OPENGL);

        Papart papart = new Papart(applet);
        papart.registerForWindowLocation();

//        Panel panel = new Panel(applet);
        return papart;
    }

    private boolean shouldSetWindowLocation = false;

    private void registerForWindowLocation() {
        applet.registerMethod("draw", this);
        this.shouldSetWindowLocation = true;
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
    }

    /**
     * Does not draw anything, it used only to check the window location.
     */
    public void draw() {
        checkWindowLocation();
        applet.unregisterMethod("draw", this);
    }

    /**
     * Set the frame to default location.
     */
    public void defaultFrameLocation() {
        this.applet.frame.setLocation(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());
    }

    protected static void removeFrameBorder(PApplet applet) {
        if (!applet.isGL()) {
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

        cameraTracking = CameraFactory.createCamera(cameraType, cameraNo);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(proCamCalib);
        cameraTracking.start();
        loadTracking(proCamCalib);
        cameraTracking.setThread();

        checkInitialization();
    }

    public void initKinectCamera(float quality) {
        assert (!cameraInitialized);

        cameraTracking = CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        cameraTracking.setParent(applet);
        cameraTracking.setCalibration(kinectRGBCalib);
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
        cameraTracking.setCalibration(proCamCalib);
        cameraTracking.start();
        loadTracking(proCamCalib);
        cameraTracking.setThread();

        initARDisplay(quality);
        checkInitialization();
    }

    private void initProjectorDisplay(float quality) {
        // TODO: check if file exists !
        projector = new ProjectorDisplay(this.applet, proCamCalib);
        projector.setZNearFar(zNear, zFar);
        projector.setQuality(quality);
        arDisplay = projector;
        display = projector;
        projector.init();
        displayInitialized = true;
        frameSize = new PVector(projector.getWidth(), projector.getHeight());
    }

    private void initARDisplay(float quality) {
        assert (this.cameraTracking != null && this.applet != null);

        arDisplay = new ARDisplay(this.applet, cameraTracking);
        arDisplay.setZNearFar(zNear, zFar);
        arDisplay.setQuality(quality);
        arDisplay.init();
        this.display = arDisplay;
        frameSize = new PVector(arDisplay.getWidth(), arDisplay.getHeight());
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
    public void loadTouchInputKinectOnly(int touch2DPrecision,
            int touch3DPrecision) {

        if (this.cameraOpenKinect == null) {
            loadDefaultCameraKinect();
            cameraTracking = cameraOpenKinect;
            cameraInitialized = true;
            checkInitialization();
        }

        loadDefaultTouchKinect(touch2DPrecision, touch3DPrecision);
        ((KinectTouchInput) this.touchInput).useRawDepth(cameraOpenKinect);
    }

    /**
     * *
     * Touch input with a Kinect calibrated with the display area.
     *
     * @param touch2DPrecision
     * @param touch3DPrecision
     */
    public void loadTouchInput(int touch2DPrecision, int touch3DPrecision) {

        loadDefaultCameraKinect();
        loadDefaultTouchKinect(touch2DPrecision, touch3DPrecision);
    }

    private void loadDefaultCameraKinect() {
        cameraOpenKinect = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        cameraOpenKinect.setParent(applet);
        cameraOpenKinect.setCalibration(kinectRGBCalib);
        cameraOpenKinect.getDepthCamera().setDepthFormat(depthFormat);
        cameraOpenKinect.start();
        cameraOpenKinect.setThread();
    }

    private void loadDefaultTouchKinect(int touch2DPrecision, int touch3DPrecision) {

        kinect = new Kinect(this.applet,
                kinectIRCalib,
                kinectRGBCalib,
                kinectFormat);
        kinect.setStereoCalibration(kinectStereoCalib);

        PlaneAndProjectionCalibration calibration = new PlaneAndProjectionCalibration();
        calibration.loadFrom(this.applet, planeAndProjectionCalib);

        KinectTouchInput kinectTouchInput
                = new KinectTouchInput(this.applet,
                        cameraOpenKinect,
                        kinect, calibration);

        cameraOpenKinect.setTouch(kinectTouchInput);

        kinectTouchInput.setPrecision(touch2DPrecision, touch3DPrecision);
        this.touchInput = kinectTouchInput;
        touchInitialized = true;
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

    public Kinect getKinect() {
        return kinect;
    }

    public PApplet getApplet() {
        return applet;
    }

}
