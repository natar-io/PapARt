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

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.procam.display.ARDisplay;
import org.bytedeco.javacpp.freenect;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.lang.reflect.Constructor;
import java.util.Set;
import org.reflections.Reflections;
import processing.core.PApplet;
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

    // TODO: find what to do with these...
//    private final int depthFormat = freenect.FREENECT_DEPTH_10BIT;
//    private final int kinectFormat = Kinect.KINECT_10BIT;
    private final int depthFormat = freenect.FREENECT_DEPTH_MM;
    private final int kinectFormat = Kinect.KINECT_MM;

    public Papart(Object applet) {
        this.displayInitialized = false;
        this.cameraInitialized = false;
        this.touchInitialized = false;
        this.applet = (PApplet) applet;

        this.appletClass = applet.getClass();
        PFont font = this.applet.loadFont(defaultFont);
        Button.setFont(font);
        Button.setFontSize(defaultFontSize);
        // TODO: singleton -> Better implementation. 
        if (Papart.singleton == null) {
            Papart.singleton = this;
        }
    }

    public static Papart getPapart() {
        return Papart.singleton;
    }

    public void setTablePosition(PaperScreen paperScreen) {
        HomographyCalibration.saveMatTo(applet, paperScreen.getLocation(), tablePosition);
    }

    public PMatrix3D getTablePosition() {
        return HomographyCalibration.getMatFrom(applet, tablePosition);
    }

    public void moveToTablePosition(PaperScreen paperScreen) {
        paperScreen.useManualLocation(true);
        paperScreen.screen.setMainLocation(HomographyCalibration.getMatFrom(applet, tablePosition));
    }

    private boolean isWithoutCamera = false;

    public void initNoCamera(int quality) {
        this.isWithoutCamera = true;
        initNoCameraDisplay(quality);
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

    private void initNoCameraDisplay(float quality) {
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
        cameraOpenKinect.setDepthFormat(depthFormat);
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
        assert (displayInitialized);
        return this.display;
    }

    public ProjectorDisplay getProjectorDisplay() {
        assert (displayInitialized);
        return this.projector;
    }

    public ARDisplay getARDisplay() {
        assert (displayInitialized);
        return this.arDisplay;
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
