/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017-2018 RealityTech
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart;

import com.jogamp.newt.opengl.GLWindow;
import fr.inria.papart.calibration.PlanarTouchCalibration;
import tech.lity.rea.nectar.camera.Camera;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.depthcam.devices.NectarOpenNI;
import fr.inria.papart.multitouch.ColorTouchInput;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.multitouch.detection.BlinkTracker;
import fr.inria.papart.multitouch.detection.CalibratedColorTracker;
import fr.inria.papart.multitouch.detection.ColorTracker;
import fr.inria.papart.procam.PaperScreen;
import tech.lity.rea.javacvprocessing.ProjectiveDeviceP;
import fr.inria.papart.utils.LibraryUtils;
import fr.inria.papart.procam.camera.CameraFactory;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.CameraRGBIRDepth;
import tech.lity.rea.nectar.camera.CannotCreateCameraException;
import tech.lity.rea.markers.DetectedMarker;
import fr.inria.papart.utils.MathUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class Papart {

    public final static String folder = LibraryUtils.getPapartDataFolder() + "/";
    public final static String calibrationFolder = folder + "calibration/";
    public final static String markerFolder = folder + "markers/";

    public static boolean isInria = false;
    public static boolean isReality = true;
    public static String calibrationFileName = "A4-calib.svg";

    public static String cameraCalibName = "camera.yaml";
    public static String projectorCalibName = "projector.yaml";

    public static String cameraCalib = calibrationFolder + cameraCalibName;
    public static String projectorCalib = calibrationFolder + projectorCalibName;

    public static String camCalibARtoolkit = calibrationFolder + "camera-projector.cal";
    public static String kinectIRCalib = calibrationFolder + "calibration-kinect-IR.yaml";
    public static String SR300IRCalib = calibrationFolder + "calibration-SR300-IR.yaml";
    public static String kinectRGBCalib = calibrationFolder + "calibration-kinect-RGB.yaml";
    public static String kinectStereoCalib = calibrationFolder + "calibration-kinect-Stereo.xml";

    public static String AstraSDepthCalib = calibrationFolder + "calibration-AstraS-depth.yaml";
    public static String AstraSRGBCalib = calibrationFolder + "calibration-AstraS-rgb.yaml";
    public static String AstraSIRCalib = calibrationFolder + "calibration-AstraS-ir.yaml";
    public static String AstraSStereoCalib = calibrationFolder + "calibration-AstraS-stereo.xml";

    public static String kinectTrackingCalib = "kinectTracking.xml";
    public static String cameraProjExtrinsics = "camProjExtrinsics.xml";
    public static String cameraProjHomography = "camProjHomography.xml";

    public static String screenConfig = calibrationFolder + "screenConfiguration.xml";
    public static String cameraConfig = calibrationFolder + "cameraConfiguration.xml";
    public static String depthCameraConfig = calibrationFolder + "depthCameraConfiguration.xml";

    public static String colorThresholds = calibrationFolder + "colorThreshold";
    public static String redThresholds = calibrationFolder + "redThresholds.txt";
    public static String blueThresholds = calibrationFolder + "blueThresholds.txt";
    public static String blinkThresholds = calibrationFolder + "blinkThresholds.txt";

    public static String tablePosition = calibrationFolder + "tablePosition.xml";
    public static String planeCalib = calibrationFolder + "PlaneCalibration.xml";
    public static String homographyCalib = calibrationFolder + "HomographyCalibration.xml";
    public static String planeAndProjectionCalib = calibrationFolder + "PlaneProjectionCalibration.xml";
    public static String touchColorCalib = calibrationFolder + "TouchColorCalibration.xml";
    public static String colorZoneCalib = calibrationFolder + "ColorZoneCalibration.xml";
    public static String touchBlinkCalib = calibrationFolder + "TouchBlinkCalibration.xml";

    public static String touchCalib = calibrationFolder + "Touch2DCalibration.xml";
    public static String objectTouchCalib = calibrationFolder + "ObjectTouchCalibration.xml";
    public static String touchCalib3D = calibrationFolder + "Touch3DCalibration.xml";

    public static String touchCalibrations[];

    public int defaultFontSize = 12;

    protected static Papart singleton = null;
    private final PApplet applet;

    private BaseDisplay display;
    private ARDisplay arDisplay;
    private ProjectorDisplay projector;

    private Camera cameraTracking;
    private DepthAnalysisImpl depthAnalysis;

    private TouchInput touchInput;
    private boolean isWithoutCamera = false;

    private DepthCameraDevice depthCameraDevice;

// TODO: zNear and zFar should move somewhere else... in Nectar
    protected float zNear = 10;
    protected float zFar = 6000;

    PVector frameSize = new PVector();

    /**
     * Create the main PapARt object, this object is used to hande calibration
     * files and automatic creation of cameras and projectors.
     *
     * @param applet
     */
    public Papart(Object applet) {
        this.applet = (PApplet) applet;

        // TODO: singleton -> Better implementation.
        if (Papart.singleton == null) {
            Papart.singleton = this;

            Papart.touchCalibrations = new String[3];
            for (int i = 0; i < touchCalibrations.length; i++) {
                touchCalibrations[i] = calibrationFolder + "TouchCalibration" + i + ".xml";
            }

            fr.inria.papart.utils.DrawUtils.applet = (PApplet) applet;
        }
    }

    /**
     * Load the default CameraConfiguration, to start the default camera.
     *
     * @param applet
     * @return
     */
//    public static CameraConfiguration getDefaultCameraConfiguration(PApplet applet) {
//        CameraConfiguration config = new CameraConfiguration();
//        config.loadFrom(applet, cameraConfig);
//        return config;
//    }
    /**
     * Load the default detph camera configuration, to start the default depth
     * camera.
     *
     * @param applet
     * @return
     */
//    public static CameraConfiguration getDefaultDepthCameraConfiguration(PApplet applet) {
//        CameraConfiguration config = new CameraConfiguration();
//        config.loadFrom(applet, depthCameraConfig);
//        return config;
//    }
    /**
     * Load the default screen configuration. The screen configuration is not
     * used in the current releases.
     *
     * @param applet
     * @return
     */
//    public static ScreenConfiguration getDefaultScreenConfiguration(PApplet applet) {
//        ScreenConfiguration config = new ScreenConfiguration();
//        config.loadFrom(applet, screenConfig);
//        return config;
//    }
    /**
     * Start the default camera and projector. This initialize a camera and a
     * ProjectorDisplay. You still need to enable the tracking and start the
     * camera. The projectorDisplay will run fullscreen on the main screen.
     *
     * @param applet
     * @return
     */
    public static Papart projection(PApplet applet) {
        return projection(applet, 1f);
    }

    /**
     * Start the default camera and projector. This initialize a camera and a
     * ProjectorDisplay. You still need to enable the tracking and start the
     * camera. The projectorDisplay will run fullscreen on the main screen.
     *
     * @param applet
     * @param quality the quality can upscale or downscale the ProjectorDisplay.
     * Increase (2.0) for better quality. If you have a 1280 width display, and
     * 2 quality the rendered image width will be 2560 pixels.
     * @return
     */
    public static Papart projection(PApplet applet, float quality) {

//        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);
//
//        removeFrameBorder(applet);
//
        Papart papart = new Papart(applet);
//
//        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
//                screenConfiguration.getProjectionScreenHeight());
//        papart.shouldSetWindowLocation = false;
//        papart.shouldSetWindowSize = true;
//        papart.registerPost();
//
//        papart.initProjectorDisplay(quality);
//        try {
//            papart.initCamera();
//        } catch (CannotCreateCameraException ex) {
//            throw new RuntimeException("Cannot start the default camera: " + ex);
//        }
//
//        papart.tryLoadExtrinsics();
//        papart.projector.setCamera(papart.getPublicCameraTracking());
//
//        papart.checkInitialization();
//        papart.registerKey();

        return papart;
    }

    /**
     * Start the default projector. This initialize a ProjectorDisplay. The
     * projectorDisplay will run fullscreen on the main screen.
     *
     * @param applet
     * @return
     */
    public static Papart projectionOnly(PApplet applet) {
        return projectionOnly(applet, 1);
    }

    /**
     * TO IMPLEMENT Start the default projector. This initialize a
     * ProjectorDisplay. The projectorDisplay will run fullscreen on the main
     * screen.
     *
     * @param applet
     * @param quality the quality can upscale or downscale the ProjectorDisplay.
     * Increase (2.0) for better quality. If you have a 1280 width display, and
     * 2 quality the rendered image width will be 2560 pixels.
     * @return
     */
    public static Papart projectionOnly(PApplet applet, float quality) {

//        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);
//
//        removeFrameBorder(applet);
//
        Papart papart = new Papart(applet);
//
//        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
//                screenConfiguration.getProjectionScreenHeight());
//        papart.shouldSetWindowLocation = true;
//        papart.shouldSetWindowSize = true;
//        papart.registerPost();
//        papart.initProjectorDisplay(quality);
//        papart.isWithoutCamera = true;

        return papart;
    }

    /**
     * Start the default camera and a CameraDisplay. You still need to enable
     * the tracking and start the camera. The window will resize itself to the
     * camera size.
     *
     * @param applet
     * @return
     */
    public static Papart seeThrough(PApplet applet) {
        return seeThrough(applet, 1);
    }

    /**
     * Start the default camera and a CameraDisplay. You still need to enable
     * the tracking and start the camera. The window will resize itself to the
     * camera size.
     *
     * @param applet
     * @param quality the quality can upscale or downscale the CameraDisplay.
     * Increase (2.0) for better quality. If you have a 640 width display, and 2
     * quality the rendered image width will be 1280 pixels.
     * @return
     */
    public static Papart seeThrough(PApplet applet, float quality) {
        return seeThrough(applet, "camera0", "rgb", quality);
    }

    public static Papart seeThrough(PApplet applet, String cameraName, String cameraConfig, float quality) {
        Papart papart = new Papart(applet);
        CameraNectar camera = papart.initCamera(cameraName, cameraConfig);
        ARDisplay display = papart.initARDisplay(camera, quality);

        papart.arDisplay = display;
        papart.cameraTracking = camera;

        return papart;
    }

    public CameraNectar initCamera() {
        return initCamera("camera0", "rgb");
    }

    /**
     * Initialize a camera for object tracking.
     *
     * @param cameraNo
     * @param cameraFormat
     * @return the created camera
     */
    public CameraNectar initCamera(String cameraNo, String cameraFormat) {
        CameraNectar cam = (CameraNectar) CameraFactory.createCamera(Camera.Type.NECTAR, cameraNo, cameraFormat);
        cam.setParent(applet);
        return cam;
    }

    /**
     * Start a fullscreen sketch.
     *
     * @param applet
     * @return
     */
    public static Papart projection2D(PApplet applet) {

//        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);
//        removeFrameBorder(applet);
        Papart papart = new Papart(applet);

//        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
//                screenConfiguration.getProjectionScreenHeight());
//        papart.shouldSetWindowLocation = true;
//        papart.shouldSetWindowSize = true;
//        papart.registerPost();
        return papart;
    }

    private boolean shouldSetWindowLocation = false;
    private boolean shouldSetWindowSize = false;

    /**
     * Register the "post" method, this is used to change the window location.
     */
    private void registerPost() {
        applet.registerMethod("post", this);
    }

    private void registerKey() {
        applet.registerMethod("keyEvent", this);
    }

    /**
     * KeyEvent to handle keys. No global keys analysed for now.
     *
     * @param e
     */
    public void keyEvent(KeyEvent e) {
//        if (e.getKey() == 'c') {
//            calibration();
//        }
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

    /**
     * TO REIMPLEMENT Save a PMatrix3D to the Papart calibration folder. This
     * can be used to communicate 3D locations between sketches. The calibration
     * folder is this: sketchbook/libraries/PapARt/data/calibration.
     *
     * @param fileName
     * @param mat
     */
    public void saveCalibration(String fileName, PMatrix3D mat) {
//        HomographyCalibration.saveMatTo(applet, mat, Papart.calibrationFolder + fileName);
    }

    /**
     * TO REIMPLEMENT Saves a 3D transformation matrix (such as a paper
     * location).
     *
     * @param fileName
     * @param mat
     */
    public void saveLocationTo(String fileName, PMatrix3D mat) {
//        HomographyCalibration.saveMatTo(applet, mat, fileName);
    }

    /**
     * TO REIMPLEMENT Load a PMatrix3D to the Papart calibration folder. This
     * can be used to communicate 3D locations between sketches. The calibration
     * folder is this: sketchbook/libraries/PapARt/data/calibration.
     *
     * @param fileName
     * @return null if the file does not exists.
     */
    public PMatrix3D loadCalibration(String fileName) {
        return new PMatrix3D();
//        File f = new File(Papart.calibrationFolder + fileName);
//        if (f.exists()) {
//            return HomographyCalibration.getMatFrom(applet, Papart.calibrationFolder + fileName);
//        } else {
//            return null;
//        }
    }

    /**
     * TO REIMPLEMENT Save the position of a paperScreen as the default table
     * location.
     *
     * @param paperScreen
     */
    public void setTableLocation(PaperScreen paperScreen) {
//        HomographyCalibration.saveMatTo(applet, paperScreen.getLocation(), tablePosition);
    }

    /**
     * TO REIMPLEMENT Save the position of a matrix the default table location.
     *
     * @param mat
     */
    public void setTableLocation(PMatrix3D mat) {
//        HomographyCalibration.saveMatTo(applet, mat, tablePosition);
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
     * TO REIMPLEMENT The location of the table, warning it must be set once by
     * setTablePosition.
     *
     * @return
     */
    public PMatrix3D getTableLocation() {
        return new PMatrix3D();
//        return HomographyCalibration.getMatFrom(applet, tablePosition);
    }

    /**
     * TO REIMPLEMENT Work in progress function
     *
     * @return
     */
//    public PlaneCalibration getTablePlane() {
//        return new PlaneCalibration();
////        return PlaneCalibration.CreatePlaneCalibrationFrom(HomographyCalibration.getMatFrom(applet, tablePosition),
////                new PVector(100, 100));
//    }
    /**
     * TO REIMPLEMENT Move a PaperScreen to the table location. After this, the
     * paperScreen location is not updated anymore. To activate the tracking
     * again use : paperScreen.useManualLocation(false); You can move the
     * paperScreen according to its current location with the
     * paperScreen.setLocation() methods.
     *
     * @param paperScreen
     */
    public void moveToTablePosition(PaperScreen paperScreen) {
//        paperScreen.useManualLocation(true, HomographyCalibration.getMatFrom(applet, tablePosition));
//        paperScreen.markerBoard.setFakeLocation(getPublicCameraTracking(), HomographyCalibration.getMatFrom(applet, tablePosition));
    }

    /**
     * Load a BaseDisplay, used for debug. This call replaces the projector or
     * seeThrough.
     */
    public void initDebug() {
        this.isWithoutCamera = true;
        initDebugDisplay();
    }

    private void tryLoadExtrinsics() {
        PMatrix3D extrinsics = loadCalibration(cameraProjExtrinsics);
        if (extrinsics == null) {
            System.out.println("loading default extrinsics. Could not find " + cameraProjExtrinsics + " .");
        } else {
            arDisplay.setExtrinsics(extrinsics);
        }
    }

    public void loadDefaultProjector() {
        initProjectorDisplay(1);
        projector.manualMode();
    }

    /**
     * Initialize the default ProjectorDisplay from the projectorCalib file.
     *
     * @param quality
     */
    private void initProjectorDisplay(float quality) {
        // TODO: check if file exists !
        projector = new ProjectorDisplay(this.applet, projectorCalib);
        projector.setZNearFar(zNear, zFar);
        projector.setQuality(quality);
        arDisplay = projector;
        display = projector;
        projector.init();
        frameSize.set(projector.getWidth(), projector.getHeight());
    }

    /**
     * Initialize the default ARDisplay from the cameraTracking.
     *
     * @param quality
     */
    private ARDisplay initARDisplay(CameraNectar cameraTracking, float quality) {
        assert (cameraTracking != null && this.applet != null);

        // TODO: It could work with a standard calibration though...
        // be sure that the camera calibration is loaded.
        boolean updateCalibration = cameraTracking.updateCalibration();
        if (!updateCalibration) {
            String err = "Cannot get the calibration of: " + cameraTracking.getCameraDescription();
            this.applet.die(err, new RuntimeException(err));
        }
        // Crash here ! -- No calibration 

        ARDisplay arDisp = new ARDisplay(this.applet, getPublicCamera(cameraTracking));
        arDisp.setZNearFar(zNear, zFar);
        arDisp.setQuality(quality);
        arDisp.init();
        this.display = arDisp;
        frameSize.set(arDisp.getWidth(), arDisp.getHeight());
        return arDisp;
    }

    /**
     * Create a BaseDisplay.
     */
    private void initDebugDisplay() {
        display = new BaseDisplay();
        display.setFrameSize(applet.width, applet.height);
        display.setDrawingSize(applet.width, applet.height);
        display.init();
    }

    // TODO: variants: hand, finger, object, small object etc... 
    // Default is hand, no simple, no object.
    /**
     * Create the default touchInput, using a depthCamera. This call loads the
     * depthCamera and the TouchInput.
     */
    public DepthTouchInput loadTouchInput() {
        try {
            // HACK load also the main camera... :[
            if (this.cameraTracking == null) {
                initCamera();
            }

            loadDefaultDepthCamera();
            loadDefaultDepthTouchInput();
        } catch (CannotCreateCameraException cce) {
            throw new RuntimeException("Cannot start the depth camera");
        }
        updateDepthCameraDeviceExtrinsics();
        return (DepthTouchInput) this.getTouchInput();
    }

    /**
     * Get the extrinsics from a depth camera.
     */
    private void updateDepthCameraDeviceExtrinsics() {
        // Check if depthCamera is the same as the camera !
        if (projector == null
                && cameraTracking instanceof CameraRGBIRDepth
                && cameraTracking == depthCameraDevice.getMainCamera()) {

            // No extrinsic used, it is already in the camera... 
            depthCameraDevice.getDepthCamera().setExtrinsics(depthCameraDevice.getStereoCalibration());

            // Specific
            // Important to use it for now ! Used in KinectTouchInput.projectPointToScreen
            ((DepthTouchInput) this.touchInput).useRawDepth();

//            System.out.println("Papart: Using Touchextrinsics from the device.");
        } else {
            // Two different cameras  
            // setExtrinsics must after the kinect stereo calibration is loaded
            PMatrix3D extr = (Papart.getPapart()).loadCalibration(Papart.kinectTrackingCalib);
            extr.invert();
            depthCameraDevice.getDepthCamera().setExtrinsics(extr);
//            System.out.println("Papart: Using Touchextrinsics from the calibrated File.");
        }
    }

    @Deprecated
    private boolean useKinectOne = true;

    @Deprecated
    public void useKinectOne(boolean kinectOne) {
        this.useKinectOne = kinectOne;
    }

    /**
     * TO REIMPLEMENT Initialize the default depth camera. You still need to
     * start the camera.
     *
     * @return @throws CannotCreateCameraException
     */
    public DepthCameraDevice loadDefaultDepthCamera() throws CannotCreateCameraException {

        // Two cases, either the other camera running of the same type
//        CameraConfiguration depthCamConfiguration = Papart.getDefaultDepthCameraConfiguration(applet);
        // If the camera is not instanciated, we use depth + color from the camera.
//        if (cameraTracking == null) {
//            System.err.println("You must choose a camera to create a DepthCamera.");
//        }
//        if (Camera.Type.valueOf(depthCamConfiguration.getCameraType()) == Camera.Type.NECTAR) {
        depthCameraDevice = new NectarOpenNI(applet, cameraTracking);

        if (depthCameraDevice == null) {
            System.err.println("Could not load the depth camera !");
        }

        this.cameraTracking = depthCameraDevice.getMainCamera();

        // At this point, cameraTracking & depth Camera are ready. 
        return depthCameraDevice;
    }

    /**
     * TO REIMPLEMENT Initialize the default touch input. You need to create the
     * depth camera first.
     */
    private void loadDefaultDepthTouchInput() {
        depthAnalysis = new DepthAnalysisImpl(this.applet, depthCameraDevice);

//        PlaneAndProjectionCalibration calibration = new PlaneAndProjectionCalibration();
//        calibration.loadFrom(this.applet, planeAndProjectionCalib);
//        HomographyCalibration hc = new HomographyCalibration();
//        hc.loadFrom(applet, Papart.homographyCalib);
//
//        PlaneCalibration pc = new PlaneCalibration();
//        pc.loadFrom(applet, Papart.planeCalib);
//        calibration.setPlane(pc);
//        calibration.setHomography(hc);
//
//        DepthTouchInput depthTouchInput
//                = new DepthTouchInput(this.applet,
//                        depthCameraDevice,
//                        depthAnalysis, calibration);
//
//        depthCameraDevice.setTouch(depthTouchInput);
//
//        // UPDATE: default is hand.
//        // depthTouchInput.initHandDetection();
//        this.touchInput = depthTouchInput;
    }

    public PlanarTouchCalibration getDefaultColorTouchCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchColorCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultColorZoneCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.colorZoneCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultBlinkTouchCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchBlinkCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultTouchCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultObjectTouchCalibration() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.objectTouchCalib);
        return calib;
    }

    public PlanarTouchCalibration getDefaultTouchCalibration3D() {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalib3D);
        return calib;
    }

    public PlanarTouchCalibration getTouchCalibration(int id) {
        PlanarTouchCalibration calib = new PlanarTouchCalibration();
        calib.loadFrom(applet, Papart.touchCalibrations[id]);
        return calib;
    }

    public TUIOTouchInput loadTouchInputTUIO() {
        TUIOTouchInput tuioTouch = new TUIOTouchInput(this.applet, getDisplay(), 3333);
        return tuioTouch;
    }


    public void setDistantCamera(String url, int port) {
        if (this.cameraTracking instanceof CameraNectar) {
            ((CameraNectar) cameraTracking).DEFAULT_REDIS_HOST = url;
            ((CameraNectar) cameraTracking).DEFAULT_REDIS_PORT = port;
        } else {
            System.err.println("Cannot set distant camera url.");
            return;
        }
    }

    public Jedis videoOutput;
    public String videoOutputKey;

    public void streamOutput(Jedis connection, String key) {
        videoOutput = connection;
        videoOutputKey = key;
    }


    /**
     * Start the camera thread, and the tracking. it calls automatically
     * startCameraThread().
     */
    public void startTracking() { 
        if (cameraTracking == null) {
            System.err.println("Start Tracking requires a Camera...");
            return;
        }
        startTracking((CameraNectar) cameraTracking);
    }
    /**
     * Start the camera thread, and the tracking. it calls automatically
     * startCameraThread().
     */
    public void startTracking(CameraNectar camera) {
        if (camera == null) {
            System.err.println("Start Tracking requires a Camera...");
            return;
        }
//        getPublicCamera(camera).trackSheets(true);
        startCameraThread(camera);
    }

    /**
     * Start the camera(s) in a thread. This call also starts the depth camera
     * when needed.
     */
    public void startCameraThread() {
        startCameraThread((CameraNectar) cameraTracking);
    }

    /**
     * Start the camera(s) in a thread.This call also starts the depth camera
 when needed.
     * @param cameraTracking
     */
    public void startCameraThread(CameraNectar cameraTracking) {
        cameraTracking.start();

        if (depthCameraDevice != null) {
            depthCameraDevice.loadDataFromDevice();
        }

        // Calibration might be loaded from the device and require an update. 
        if (arDisplay != null && !(arDisplay instanceof ProjectorDisplay)) {
            arDisplay.reloadCalibration();
        }

        cameraTracking.setThread();

        if (depthCameraDevice != null
                && cameraTracking != depthCameraDevice.getMainCamera()) {
            depthCameraDevice.getMainCamera().start();
            depthCameraDevice.getMainCamera().setThread();
        }
    }

    public void startDepthCameraThread() {
        depthCameraDevice.getMainCamera().start();
        depthCameraDevice.getMainCamera().setThread();
    }

    /**
     * Get the Position of a marker. MarkerTracking must be enabled with at
     * least one SVG marker to find.
     *
     * @param markerID id of the marker to find.
     * @param markerWidth size of square marker in mm.
     * @return null is the marker is not found.
     */
    public PMatrix3D getMarkerMatrix(int markerID, float markerWidth) {
        if (cameraTracking == null
                || cameraTracking.getDetectedMarkers() == null) {
            return null;
        }

        for (DetectedMarker marker : cameraTracking.getDetectedMarkers()) {
            if (marker.id == markerID) {
                return MathUtils.compute3DPos(marker, markerWidth, cameraTracking);
            }
        }
        return null;
    }

    /**
     * Get the Position of a marker. MarkerTracking must be enabled with at
     * least one SVG marker to find.
     *
     * @return null is the marker is not found.
     */
    public DetectedMarker[] getMarkerList() {
        if (cameraTracking == null
                || cameraTracking.getDetectedMarkers() == null) {
            return new DetectedMarker[0];
        }
        return cameraTracking.getDetectedMarkers();
    }

    /**
     * Return the x,y positions of a 3D location projected onto a given
     * reference. The Z axis is the angle (in radians) given by the rotation of
     * the positionToFind.
     *
     * @param positionToFind
     * @param reference
     * @return
     */
    public PVector projectPositionTo2D(PMatrix3D positionToFind,
            PMatrix3D reference,
            float referenceHeight) {
        PMatrix3D referenceInv = reference.get();
        referenceInv.invert();
        PMatrix3D relative = positionToFind.get();
        relative.preApply(referenceInv);

        PMatrix3D positionToFind2 = positionToFind.get();
        positionToFind2.translate(100, 0, 0);
        PMatrix3D relative2 = positionToFind2.get();
        relative2.preApply(referenceInv);

        PVector out = new PVector();
        float x = relative.m03 - relative2.m03;
        float y = relative.m13 - relative2.m13;
        out.z = PApplet.atan2(x, y);

        out.x = relative.m03;
        out.y = referenceHeight - relative.m13;

        return out;
    }

    /**
     * Return the x,y positions of a 3D location projected onto a given
     * reference. The Z axis is the angle (in radians) given by the rotation of
     * the positionToFind.
     *
     * @param positionToFind
     * @param reference
     * @return
     */
    public PVector projectPositionTo(PMatrix3D positionToFind,
            PaperScreen reference) {
        PMatrix3D referenceInv = reference.getLocation().get();
        referenceInv.invert();
        PMatrix3D relative = positionToFind.get();
        relative.preApply(referenceInv);

        PMatrix3D positionToFind2 = positionToFind.get();
        positionToFind2.translate(100, 0, 0);
        PMatrix3D relative2 = positionToFind2.get();
        relative2.preApply(referenceInv);

        PVector out = new PVector();
        float x = relative.m03 - relative2.m03;
        float y = relative.m13 - relative2.m13;
        out.z = PApplet.atan2(x, y);

        out.x = relative.m03;
        out.y = reference.getDrawingSize().y - relative.m13;

        return out;
    }

    /**
     * Create a multi-color ColorTracker for a PaperScreen.
     *
     * @param screen PaperScreen to set the location of the tracking.
     * @param quality capture quality in px/mm. lower (0.5f) for higher
     * performance.
     * @return
     */
    public CalibratedColorTracker initAllTracking(PaperScreen screen, float quality) {
        CalibratedColorTracker colorTracker = new CalibratedColorTracker(screen, quality);
        return colorTracker;
    }
//
//    /**
//     * Create a red ColorTracker for a PaperScreen.
//     *
//     * @param screen PaperScreen to set the location of the tracking.
//     * @param quality capture quality in px/mm. lower (0.5f) for higher
//     * performance.
//     * @return
//     */
//    public ColorTracker initRedTracking(PaperScreen screen, float quality) {
//        return initColorTracking("red", redThresholds, screen, quality);
//    }
//
//    /**
//     * Create a blue ColorTracker for a PaperScreen.
//     *
//     * @param screen PaperScreen to set the location of the tracking.
//     * @param quality capture quality in px/mm. lower (0.5f) for higher
//     * performance.
//     * @return
//     */
//    public ColorTracker initBlueTracking(PaperScreen screen, float quality) {
//        return initColorTracking("blue", blueThresholds, screen, quality);
//    }
//
//    /**
//     * Create a blue ColorTracker for a PaperScreen.
//     *
//     * @param screen PaperScreen to set the location of the tracking.
//     * @param quality capture quality in px/mm. lower (0.5f) for higher
//     * performance.
//     * @param freq
//     * @return
//     */
//    public BlinkTracker initXTracking(PaperScreen screen, float quality, float freq) {
//        BlinkTracker blinkTracker = new BlinkTracker(screen, getDefaultBlinkTouchCalibration(), quality);
//        String[] list = applet.loadStrings(blinkThresholds);
//        for (String data : list) {
//            blinkTracker.loadParameter(data);
//        }
//        blinkTracker.setName("x");
//        blinkTracker.setFreq(freq);
//        return blinkTracker;
//    }
//
//    private ColorTracker initColorTracking(String name, String calibFile, PaperScreen screen, float quality) {
//        ColorTracker colorTracker = new ColorTracker(screen, getDefaultColorTouchCalibration(), quality);
//        String[] list = applet.loadStrings(calibFile);
//        colorTracker.loadParameters(list);
//        colorTracker.setName(name);
//        return colorTracker;
//    }

    public BaseDisplay getDisplay() {
        return this.display;
    }

    public void setDisplay(BaseDisplay display) {
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

    public ProjectorDisplay getProjectorDisplay() {
        return this.projector;
    }

    public ARDisplay getARDisplay() {
        return this.arDisplay;
    }

    public Camera getCameraTracking() {
        return this.cameraTracking;
    }

    public Camera getPublicCameraTracking() {
        return getPublicCamera(cameraTracking);
    }

    /**
     * The public camera is the main one: usually the color.
     *
     * @param camera
     * @return
     */
    public Camera getPublicCamera(Camera camera) {
        if (camera instanceof CameraRGBIRDepth) {
            if (((CameraRGBIRDepth) camera).getActingCamera() == null) {
                throw new RuntimeException("Papart: Impossible to use the mainCamera, use a subCamera or set the ActAsX methods.");
            }
            return ((CameraRGBIRDepth) camera).getActingCamera();
        }
        return camera;
    }

    public TouchInput getTouchInput() {
//        assert (touchInitialized);
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
        return this.depthCameraDevice.getColorCamera();
    }

    public DepthCameraDevice getDepthCameraDevice() {
        return depthCameraDevice;
    }

    public DepthAnalysisImpl getKinectAnalysis() {
        return this.depthAnalysis;
    }

    public Camera.Type getDepthCameraType() {
        if (depthCameraDevice == null) {
            return Camera.Type.FAKE;
        }
        return depthCameraDevice.type();
    }

    public PApplet getApplet() {
        return applet;
    }

}
