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
package fr.inria.papart.procam;

import com.jogamp.newt.opengl.GLWindow;
import fr.inria.papart.calibration.MultiCalibrator;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.calibration.files.CameraConfiguration;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlanarTouchCalibration;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.calibration.files.ScreenConfiguration;
import fr.inria.papart.depthcam.devices.Kinect360;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.depthcam.devices.KinectOne;
import fr.inria.papart.depthcam.devices.NectarOpenNI;
import fr.inria.papart.depthcam.devices.OpenNI2;
import fr.inria.papart.depthcam.devices.RealSense;
import fr.inria.papart.multitouch.ColorTouchInput;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.multitouch.detection.BlinkTracker;
import fr.inria.papart.multitouch.detection.CalibratedColorTracker;
import fr.inria.papart.multitouch.detection.ColorReferenceThresholds;
import fr.inria.papart.multitouch.detection.ColorTracker;
import fr.inria.papart.utils.LibraryUtils;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraNectar;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.utils.MathUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    protected float zNear = 10;
    protected float zFar = 6000;

    private final PApplet applet;

    private boolean cameraInitialized;

    private BaseDisplay display;
    private ARDisplay arDisplay;
    private ProjectorDisplay projector;

    private Camera cameraTracking;
    private DepthAnalysisImpl depthAnalysis;

    private TouchInput touchInput;
    private PVector frameSize = new PVector();
    private boolean isWithoutCamera = false;

    private DepthCameraDevice depthCameraDevice;

    /**
     * Create the main PapARt object, this object is used to hande calibration
     * files and automatic creation of cameras and projectors.
     *
     * @param applet
     */
    public Papart(Object applet) {
        this.cameraInitialized = false;
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
    public static CameraConfiguration getDefaultCameraConfiguration(PApplet applet) {
        CameraConfiguration config = new CameraConfiguration();
        config.loadFrom(applet, cameraConfig);
        return config;
    }

    /**
     * Load the default detph camera configuration, to start the default depth
     * camera.
     *
     * @param applet
     * @return
     */
    public static CameraConfiguration getDefaultDepthCameraConfiguration(PApplet applet) {
        CameraConfiguration config = new CameraConfiguration();
        config.loadFrom(applet, depthCameraConfig);
        return config;
    }

    /**
     * Load the default screen configuration. The screen configuration is not
     * used in the current releases.
     *
     * @param applet
     * @return
     */
    public static ScreenConfiguration getDefaultScreenConfiguration(PApplet applet) {
        ScreenConfiguration config = new ScreenConfiguration();
        config.loadFrom(applet, screenConfig);
        return config;
    }

    public MultiCalibrator multiCalibrator;

    public void multiCalibration() {
        multiCalibration(false);
    }

    public void multiCalibration(boolean colorOnly) {

        try {

            if (multiCalibrator == null) {
                multiCalibrator = new MultiCalibrator();

            } else {
                if (multiCalibrator.isActive()) {
                    multiCalibrator.stopCalib();
                } else {
                    multiCalibrator.startCalib();
                }
//            calibrationPopup.hide();
            }
            multiCalibrator.setColorOnly(colorOnly);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);

        removeFrameBorder(applet);

        Papart papart = new Papart(applet);

        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight());
        papart.shouldSetWindowLocation = false;
        papart.shouldSetWindowSize = true;
        papart.registerPost();

        papart.initProjectorDisplay(quality);
        try {
            papart.initCamera();
        } catch (CannotCreateCameraException ex) {
            throw new RuntimeException("Cannot start the default camera: " + ex);
        }

        papart.tryLoadExtrinsics();
        papart.projector.setCamera(papart.getPublicCameraTracking());

        papart.checkInitialization();
        papart.registerKey();

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
     * Start the default projector. This initialize a ProjectorDisplay. The
     * projectorDisplay will run fullscreen on the main screen.
     *
     * @param applet
     * @param quality the quality can upscale or downscale the ProjectorDisplay.
     * Increase (2.0) for better quality. If you have a 1280 width display, and
     * 2 quality the rendered image width will be 2560 pixels.
     * @return
     */
    public static Papart projectionOnly(PApplet applet, float quality) {

        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(applet);

        removeFrameBorder(applet);

        Papart papart = new Papart(applet);

        papart.frameSize.set(screenConfiguration.getProjectionScreenWidth(),
                screenConfiguration.getProjectionScreenHeight());
        papart.shouldSetWindowLocation = true;
        papart.shouldSetWindowSize = true;
        papart.registerPost();
        papart.initProjectorDisplay(quality);
        papart.isWithoutCamera = true;

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

        ProjectiveDeviceP pdp = null;
        try {
            pdp = ProjectiveDeviceP.loadCameraDevice(applet, cameraCalib);
        } catch (Exception ex) {
            Logger.getLogger(Papart.class.getName()).log(Level.SEVERE, null, ex);
        }

        Papart papart = new Papart(applet);

//        if (pdp != null) {
//            papart.frameSize.set(pdp.getWidth(), pdp.getHeight());
//            papart.shouldSetWindowSize = true;
//            papart.registerPost();
//        }
        try {
            papart.initCamera();
        } catch (CannotCreateCameraException ex) {
            throw new RuntimeException("Cannot start the default camera: " + ex);
        }
        papart.initARDisplay(quality);
        papart.checkInitialization();

        return papart;
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

    /**
     * Force the size of the sketch to the cameraTracking size. This is used for
     * SeeThrough augmented reality.
     */
    public void forceCameraSize() {
        forceWindowSize(cameraTracking.width(),
                cameraTracking.height());
    }

    /**
     * Force the size of the sketch to the depthCamera size. This is used for
     * SeeThrough augmented reality with depth cameras.
     */
    public void forceDepthCameraSize() {
        forceWindowSize(depthCameraDevice.getDepthCamera().width(),
                depthCameraDevice.getDepthCamera().height());
    }

    /**
     * Force a custom window size.
     *
     * @param w width in pixels.
     * @param h height in pixels.
     */
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

    /**
     * Force the window size to the default projector size.
     */
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

    /**
     * Force a fullscreen size (for projectors). This call removes the window
     * decoration: menu bars, and makets it fullscreen.
     *
     * @param w width in pixels.
     * @param h hegiht in pixels.
     * @param px location in pixels (from left).
     * @param py location in pixels (from top).
     */
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
     * configuration. Do not call directly, it may crash the application.
     * Register the method "post" and it will call this method.
     *
     */
    public static void checkWindowLocation() {
        Papart papart = getPapart();

        if (papart == null) {
            System.err.println("Cannot update window location without a Papart object.");
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
     * Does not draw anything, it used only to check the window location. This
     * is called once then unregistered.
     */
    public void post() {
        checkWindowLocation();
        applet.unregisterMethod("post", this);
    }

    /**
     * Set the frame to default location given by the screenConfiguration.
     */
    public void defaultFrameLocation() {
        ScreenConfiguration screenConfiguration = getDefaultScreenConfiguration(this.applet);
        this.applet.frame.setLocation(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());

        GLWindow window = (GLWindow) applet.getSurface().getNative();
        window.setPosition(screenConfiguration.getProjectionScreenOffsetX(),
                screenConfiguration.getProjectionScreenOffsetY());
    }

    /**
     * Update the applet size to the current frameSize. The current frameSize
     * depends on which type of rendering is used.
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

    /**
     * Save a PMatrix3D to the Papart calibration folder. This can be used to
     * communicate 3D locations between sketches. The calibration folder is
     * this: sketchbook/libraries/PapARt/data/calibration.
     *
     * @param fileName
     * @param mat
     */
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
     * Load a PMatrix3D to the Papart calibration folder. This can be used to
     * communicate 3D locations between sketches. The calibration folder is
     * this: sketchbook/libraries/PapARt/data/calibration.
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

        if (this.cameraTracking instanceof CameraNectar) {
            CameraNectar cam = ((CameraNectar) this.cameraTracking);
            Jedis connection = cam.createConnection();
            System.out.println("Loading table location from Natar camera. Key: " + cam.getCameraKey() + ":table .");
            String data = connection.get(cam.getCameraKey() + ":table");
            return HomographyCalibration.getMatFromData(data);
        }

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
        paperScreen.useManualLocation(true, HomographyCalibration.getMatFrom(applet, tablePosition));
//        paperScreen.markerBoard.setFakeLocation(getPublicCameraTracking(), HomographyCalibration.getMatFrom(applet, tablePosition));
    }

    @Deprecated
    public void initNoCamera(int quality) {
        this.isWithoutCamera = true;
        initNoCameraDisplay(quality);
    }

    /**
     * Load a BaseDisplay, used for debug.This call replaces the projector or
     * seeThrough.
     *
     * @param applet
     * @return
     */
    public static Papart initDebug(PApplet applet, float quality, float scale) {
        Papart papart = new Papart(applet);
        papart.isWithoutCamera = true;
        papart.initDebugDisplay(applet, quality, scale);
        return papart;
    }

    private void tryLoadExtrinsics() {
        PMatrix3D extrinsics = loadCalibration(cameraProjExtrinsics);
        if (extrinsics == null) {
            System.out.println("loading default extrinsics. Could not find " + cameraProjExtrinsics + " .");
        } else {
            arDisplay.setExtrinsics(extrinsics);
        }
    }

    /**
     * Initialize the default calibrated camera for object tracking.
     *
     * @throws fr.inria.papart.procam.camera.CannotCreateCameraException
     */
    public void initCamera() throws CannotCreateCameraException {
        CameraConfiguration cameraConfiguration = getDefaultCameraConfiguration(applet);
        initCamera(cameraConfiguration);
    }

    /**
     * Initialize a camera for object tracking.
     *
     * @param cameraConfiguration
     * @throws fr.inria.papart.procam.camera.CannotCreateCameraException
     *
     */
    public void initCamera(CameraConfiguration cameraConfiguration) throws CannotCreateCameraException {
        initCamera(cameraConfiguration.getCameraName(),
                cameraConfiguration.getCameraType(),
                cameraConfiguration.getCameraFormat(),
                cameraConfiguration.getCaptureFormat());
    }

    /**
     * Initialize a camera for object tracking.
     *
     */
    public void initCamera(String cameraNo, Camera.Type cameraType, String cameraFormat, String captureFormat) throws CannotCreateCameraException {
        assert (!cameraInitialized);

        //  HACK Natar :
        //Check if Natar is aread
        boolean cameraServerFound = false;
        try {
            CameraNectar cameraNectar = (CameraNectar) CameraFactory.createCamera(Camera.Type.NECTAR, "camera0", "rgb");
            Jedis jedis = cameraNectar.createConnection();
//            jedis.get(folder)
            ClientList list = new ClientList(jedis.clientList());
            if (list.hasClient("CameraServer")) {
                cameraServerFound = true;
                System.out.println("Switching to Natar");

                cameraTracking = cameraNectar;
                cameraTracking.setParent(applet);
                cameraTracking.setCalibration(cameraCalib);
            }

        } catch (Exception e) {
            System.out.println("Natar not found");
        }

        if (!cameraServerFound) {
            cameraTracking = CameraFactory.createCamera(cameraType, cameraNo, cameraFormat);
            cameraTracking.setParent(applet);
            cameraTracking.setCalibration(cameraCalib);
        }

        cameraTracking.setCaptureFormat(captureFormat);
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
    private void initARDisplay(float quality) {
        assert (this.cameraTracking != null && this.applet != null);

        arDisplay = new ARDisplay(this.applet, getPublicCameraTracking());
        arDisplay.setZNearFar(zNear, zFar);
        arDisplay.setQuality(quality);
        arDisplay.init();
        this.display = arDisplay;
        frameSize.set(arDisplay.getWidth(), arDisplay.getHeight());
    }

    @Deprecated
    private void initNoCameraDisplay(float quality) {
     //   initDebugDisplay(applet, 1);
    }

    /**
     * Create a BaseDisplay.
     */
    private void initDebugDisplay(PApplet applet, float quality, float scale) {
        display = new BaseDisplay(applet, quality, scale);
        display.setFrameSize(applet.width, applet.height);
        display.setDrawingSize(applet.width, applet.height);
        display.init();
    }

    private void checkInitialization() {
        assert (cameraTracking != null);
//        this.applet.registerMethod("dispose", this);
//        this.applet.registerMethod("stop", this);
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

    public void loadIRTouchInput() {
        try {
            initCamera();
            ((CameraRGBIRDepth) cameraTracking).setUseIR(true);
            loadIRTouch();

        } catch (CannotCreateCameraException cce) {
            throw new RuntimeException("Cannot start the depth camera");
        }
//        updateDepthCameraDeviceExtrinsics();
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
     * Initialize the default depth camera. You still need to start the camera.
     *
     * @return @throws CannotCreateCameraException
     */
    public DepthCameraDevice loadDefaultDepthCamera() throws CannotCreateCameraException {

        // Two cases, either the other camera running of the same type
        CameraConfiguration depthCamConfiguration = Papart.getDefaultDepthCameraConfiguration(applet);

        try {
            CameraNectar cameraNectar = (CameraNectar) CameraFactory.createCamera(Camera.Type.NECTAR, "camera0", "rgb");
            Jedis jedis = cameraNectar.createConnection();
            ClientList list = new ClientList(jedis.clientList());
            if (list.hasClient("DepthCameraServer")) {
                System.out.println("Switching to Natar for Depth Camera");
                depthCamConfiguration.setCameraType(Camera.Type.NECTAR);
                depthCamConfiguration.setCameraName("camera0");
                depthCamConfiguration.setCameraFormat("");
            }

        } catch (Exception e) {
            System.out.println("Natar not found");
        }

        // If the camera is not instanciated, we use depth + color from the camera.
//        if (cameraTracking == null) {
//            System.err.println("You must choose a camera to create a DepthCamera.");
//        }
        if (depthCamConfiguration.getCameraType() == Camera.Type.REALSENSE) {
            depthCameraDevice = new RealSense(applet, cameraTracking);
        }

        if (depthCamConfiguration.getCameraType() == Camera.Type.OPEN_KINECT) {
            depthCameraDevice = new Kinect360(applet, cameraTracking);
        }

        if (depthCamConfiguration.getCameraType() == Camera.Type.OPEN_KINECT_2) {
            depthCameraDevice = new KinectOne(applet, cameraTracking);
        }
        if (depthCamConfiguration.getCameraType() == Camera.Type.OPENNI2) {
            depthCameraDevice = new OpenNI2(applet, cameraTracking);
        }
        if (depthCamConfiguration.getCameraType() == Camera.Type.NECTAR) {
            depthCameraDevice = new NectarOpenNI(applet, cameraTracking);
        }

        if (depthCameraDevice == null) {
            System.err.println("Could not load the depth camera !" + "Camera Type " + depthCamConfiguration.getCameraType());
        }

        this.cameraTracking = depthCameraDevice.getMainCamera();

        // At this point, cameraTracking & depth Camera are ready. 
        return depthCameraDevice;
    }

    /**
     * Initialize the default touch input. You need to create the depth camera
     * first.
     */
    private void loadDefaultDepthTouchInput() {
        depthAnalysis = new DepthAnalysisImpl(this.applet, depthCameraDevice);

        PlaneAndProjectionCalibration calibration = new PlaneAndProjectionCalibration();
//        calibration.loadFrom(this.applet, planeAndProjectionCalib);

        HomographyCalibration hc = new HomographyCalibration();
        hc.loadFrom(applet, Papart.homographyCalib);

        PlaneCalibration pc = new PlaneCalibration();
        pc.loadFrom(applet, Papart.planeCalib);
        calibration.setPlane(pc);
        calibration.setHomography(hc);

        DepthTouchInput depthTouchInput
                = new DepthTouchInput(this.applet,
                        depthCameraDevice,
                        depthAnalysis, calibration);

        depthCameraDevice.setTouch(depthTouchInput);

        // UPDATE: default is hand.
        // depthTouchInput.initHandDetection();
        this.touchInput = depthTouchInput;
    }

    private void loadIRTouch() {

//        PlaneAndProjectionCalibration calibration = new PlaneAndProjectionCalibration();
//        calibration.loadFrom(this.applet, planeAndProjectionCalib);
        ColorTouchInput colorTouchInput
                = new ColorTouchInput(this.applet, ((CameraRGBIRDepth) cameraTracking).getIRCamera());
        this.touchInput = colorTouchInput;
        cameraTracking.setTouchInput(colorTouchInput);
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

    /**
     * Start the tracking without a thread.
     */
    public void startTrackingWithoutThread() {
        if (this.cameraTracking == null) {
            System.err.println("Start Tracking requires a Camera...");
            return;
        }
        this.getPublicCameraTracking().trackSheets(true);
    }

    public void setDistantCamera(String url, int port) {
        if (this.cameraTracking instanceof CameraNectar) {
            ((CameraNectar) cameraTracking).setRedisHost(url);
            ((CameraNectar) cameraTracking).setRedisPort(port);
        } else {
            System.err.println("Cannot set distant camera url.");
            return;
        }
    }

    public void streamOutput(String host, int port, String auth, String key) {
        display.setVideoEmitter(new VideoEmitter(host, port, auth, key));
    }

    /**
     * Start the camera thread, and the tracking. it calls automatically
     * startCameraThread().
     */
    public void startTracking() {
        if (this.cameraTracking == null) {
            System.err.println("Start Tracking requires a Camera...");
            return;
        }
        this.getPublicCameraTracking().trackSheets(true);
        startCameraThread();
    }

    /**
     * Start the camera(s) in a thread. This call also starts the depth camera
     * when needed.
     */
    public void startCameraThread() {
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

//        Optional<DetectedMarker> marker = Arrays.asList(cameraTracking.getDetectedMarkers())
//                .stream()
//                .filter((m) -> m.id == markerID)
//                .findFirst(); 
//        
//        if(marker != null){
//            return MathUtils.compute3DPos(marker.get(), markerWidth, cameraTracking);
//        }
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
        out.y = reference.drawingSize.y - relative.m13;

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

    /**
     * Create a red ColorTracker for a PaperScreen.
     *
     * @param screen PaperScreen to set the location of the tracking.
     * @param quality capture quality in px/mm. lower (0.5f) for higher
     * performance.
     * @return
     */
    public ColorTracker initRedTracking(PaperScreen screen, float quality) {
        return initColorTracking("red", redThresholds, screen, quality);
    }

    /**
     * Create a blue ColorTracker for a PaperScreen.
     *
     * @param screen PaperScreen to set the location of the tracking.
     * @param quality capture quality in px/mm. lower (0.5f) for higher
     * performance.
     * @return
     */
    public ColorTracker initBlueTracking(PaperScreen screen, float quality) {
        return initColorTracking("blue", blueThresholds, screen, quality);
    }

    /**
     * Create a blue ColorTracker for a PaperScreen.
     *
     * @param screen PaperScreen to set the location of the tracking.
     * @param quality capture quality in px/mm. lower (0.5f) for higher
     * performance.
     * @param freq
     * @return
     */
    public BlinkTracker initXTracking(PaperScreen screen, float quality, float freq) {
        BlinkTracker blinkTracker = new BlinkTracker(screen, getDefaultBlinkTouchCalibration(), quality);
        String[] list = applet.loadStrings(blinkThresholds);
        for (String data : list) {
            blinkTracker.loadParameter(data);
        }
        blinkTracker.setName("x");
        blinkTracker.setFreq(freq);
        return blinkTracker;
    }

    private ColorTracker initColorTracking(String name, String calibFile, PaperScreen screen, float quality) {
        ColorTracker colorTracker = new ColorTracker(screen, getDefaultColorTouchCalibration(), quality);
        String[] list = applet.loadStrings(calibFile);
        colorTracker.loadParameters(list);
        colorTracker.setName(name);
        return colorTracker;
    }

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
        if (cameraTracking instanceof CameraRGBIRDepth) {
            if (((CameraRGBIRDepth) cameraTracking).getActingCamera() == null) {
                throw new RuntimeException("Papart: Impossible to use the mainCamera, use a subCamera or set the ActAsX methods.");
            }
            return ((CameraRGBIRDepth) cameraTracking).getActingCamera();
        }
        return this.cameraTracking;
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

    public void createColorReference(int colorId, int[] colorData) {
        ColorReferenceThresholds c = new ColorReferenceThresholds(colorId);

        String[] list = c.createReference(colorData);
        if (list != ColorReferenceThresholds.INVALID_COLOR) {
            String saveFile = Papart.colorThresholds + colorId + ".txt";
            applet.saveStrings(saveFile, list);

            if (colorId == 0) {
                applet.saveStrings(Papart.redThresholds, list);
            }
            if (colorId == 1) {
                applet.saveStrings(Papart.blueThresholds, list);
            }
        } else {
            System.out.println("could not determine color: " + colorId);
        }
    }

}
