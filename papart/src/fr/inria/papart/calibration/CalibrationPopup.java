/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.depthcam.devices.KinectDevice;
import fr.inria.papart.depthcam.devices.KinectDevice.Type;
import fr.inria.papart.procam.MarkerBoard;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.ProjectorAsCamera;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.skatolo.Skatolo;
import fr.inria.skatolo.gui.controllers.Toggle;
import fr.inria.skatolo.gui.group.Group;
import fr.inria.skatolo.gui.group.RadioButton;
import fr.inria.skatolo.gui.group.Textarea;
import java.util.ArrayList;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class CalibrationPopup extends PApplet {

    Papart papart;

    // todo: setTableLocation ?
    // GUI
    private Skatolo skatolo;
    private Group cornersGroup;
    private RadioButton cornersRadio;
    private Toggle cornerToggle;
    protected Toggle zoomToggle;

    // Corners
    private CalibrationVideoPopup videoPopup;
    protected PVector[] corners;
    protected int currentCorner = 0;
    protected boolean showZoom = false;
    private String cornersFileName;
    static final String CORNERS_NAME = "cornersProj.json";

    // projector rendering.
    private ProjectorDisplay projector;
    private ProjectorAsCamera projectorAsCamera;
    static final String PROJECTOR_ARTOOLKIT_NAME = "projectorCalibration.cal";
    static final String KINECT_ARTOOLKIT_NAME = "kinectCalibration.cal";

    // calibration App / board 
    private CalibrationApp calibrationApp;
    private MarkerBoard board;

    // Matrices 
    private Textarea cameraMatrixText, projectorMatrixText, kinectMatrixText;

    // Cameras
    private Camera cameraTracking;
    private Camera cameraKinect;
    private TrackedView projectorView;

    // Kinect
    private KinectDevice.Type kinectType;

    // calibrations
    private ArrayList<CalibrationSnapshot> snapshots = new ArrayList<CalibrationSnapshot>();
    private String isProCamCalibrated = NOTHING;
    private String isKinectCalibrated = NOTHING;

    private static final String COMPUTING = "Computing...";
    private static final String OK = "OK";
    private static final String NOTHING = "";

    private CalibrationExtrinsic calibrationExtrinsic;

    public CalibrationPopup() {
        super();
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    @Override
    public void settings() {
        size(900, 600);
    }

    @Override
    public void setup() {

        papart = Papart.getPapart();
        cornersFileName = Papart.calibrationFolder + CORNERS_NAME;
        calibrationApp = new CalibrationApp();
        calibrationApp.pre();
        board = calibrationApp.getBoard();

        cameraTracking = Papart.getPapart().getCameraTracking();
        projector = Papart.getPapart().getProjectorDisplay();
        projector.setCalibrationMode(true);

        calibrationExtrinsic = new CalibrationExtrinsic(this);
        initProjectorAsCamera();
        calibrationExtrinsic.setProjector(projector);

        kinectType = papart.getKinectType();
        if (kinectType != Type.NONE) {
            if (kinectType == Type.X360) {
                initKinect360(papart.getKinectDevice());
            }
            calibrationExtrinsic.setKinect(papart.getKinectDevice(), kinectType);
        }

        initCorners();
        loadCorners();

        skatolo = new Skatolo(this, this);
        initMainGui();
        initMatrixGui();
        initCornersGUI();

        this.isReady = true;
        frameRate(10);
    }

    boolean isReady = false;

    private void initProjectorAsCamera() {
        projectorView = new TrackedView();
        projectorView.setImageWidthPx(projector.getWidth());
        projectorView.setImageHeightPx(projector.getHeight());
        projectorView.init();

        projectorAsCamera = new ProjectorAsCamera();
        projectorAsCamera.setCalibration(Papart.projectorCalib);
        projectorAsCamera.setParent(this);

        String ARToolkitCalibFile = Papart.calibrationFolder + "projector.cal";
        ProjectorAsCamera.convertARProjParams(this, projectorAsCamera.getCalibrationFile(),
                ARToolkitCalibFile);
        projectorAsCamera.initMarkerDetection(ARToolkitCalibFile);
        projectorAsCamera.trackMarkerBoard(board);
    }

    private void initKinect360(KinectDevice kinectDevice) {
        cameraKinect = kinectDevice.getCameraRGB();

        String ARToolkitCalib = Papart.calibrationFolder + KINECT_ARTOOLKIT_NAME;
        Camera.convertARParams(this, cameraKinect.getCalibrationFile(), ARToolkitCalib);
        cameraKinect.initMarkerDetection(ARToolkitCalib);

        // No display for now...
//        arDisplayKinect = new ARDisplay(this, cameraKinect);
//        arDisplayKinect.init();
//        arDisplayKinect.manualMode();
//        app.addDisplay(arDisplayKinect);
        cameraKinect.trackSheets(true);

        // as it does not comes with the display:
        cameraKinect.trackMarkerBoard(board);
    }

    void reset() {
        snapshots.clear();
        this.isProCamCalibrated = NOTHING;
        this.isKinectCalibrated = NOTHING;
        cameraMatrixText.setColor(color(255));
        cameraMatrixText.setColor(color(255));
    }

    static final int ADD_CALIBRATION_HEIGHT = 180;
    static final int DO_CALIBRATION_HEIGHT = 380;
    static final int MATRICES_HEIGHT = 250;

    void initMainGui() {
        cornerToggle = skatolo.addToggle("cornerCalibration")
                .setPosition(20, 40)
                .setCaptionLabel("Projector Corner Calibration (c)");

        skatolo.addBang("addCalibration")
                .setPosition(20, ADD_CALIBRATION_HEIGHT)
                .setCaptionLabel("Add Calibration (a)");

        skatolo.addBang("clearCalibrations")
                .setPosition(DO_CALIBRATION_HEIGHT, ADD_CALIBRATION_HEIGHT)
                .setCaptionLabel("clear Calibration");

        skatolo.addBang("calibrate procam")
                .setPosition(20, DO_CALIBRATION_HEIGHT)
                //                .setCaptionLabel("Calibration ProCam")
                .plugTo(this, "calibrateProCam");

        if (kinectType != Type.NONE) {
            skatolo.addBang("calibrateKinect")
                    .setPosition(200, DO_CALIBRATION_HEIGHT)
                    .setCaptionLabel("Plane Calibration");
        }

    }

    public void addCalibration() {
        snapshots.add(
                new CalibrationSnapshot(
                        currentCamBoard(),
                        currentProjBoard(),
                        (kinectType == Type.X360) ? currentKinect360Board() : null));
    }

    public void clearCalibrations() {

        reset();
    }

    public void calibrateProCam() {
        this.isProCamCalibrated = COMPUTING;
        calibrationExtrinsic.computeProjectorCameraExtrinsics(snapshots);
        calibrationExtrinsic.calibrateKinect(snapshots);
        this.isProCamCalibrated = OK;
        this.isKinectCalibrated = OK;
    }

    public void calibrateKinect() {
        boolean isCalibOK = calibrationExtrinsic.calibrateKinect360Plane(snapshots);
        if (isCalibOK) {
            this.isKinectCalibrated = OK;
        }
    }

    private PMatrix3D currentKinect360Board() {
        return board.getTransfoMat(cameraKinect).get();
    }

    private PMatrix3D currentCamBoard() {
        return board.getTransfoMat(cameraTracking).get();
    }

    private PMatrix3D currentProjBoard() {
        opencv_core.IplImage projImage = projectorImage();
        if (projImage == null) {
            return new PMatrix3D();
        }
        board.updatePosition(projectorAsCamera, projImage);
        return board.getTransfoMat(projectorAsCamera);
    }

    private opencv_core.IplImage grayImage = null;

    private opencv_core.IplImage projectorImage() {
        projectorView.setCorners(corners);
        opencv_core.IplImage projImage = projectorView.getIplViewOf(cameraTracking);
        if (board.useARToolkit()) {
            projImage = greyProjectorImage(projImage);
        }
        return projImage;
    }

    private opencv_core.IplImage greyProjectorImage(opencv_core.IplImage projImage) {
        if (grayImage == null) {
            grayImage = opencv_core.IplImage.create(projector.getWidth(),
                    projector.getHeight(),
                    IPL_DEPTH_8U, 1);
        }
        cvCvtColor(projImage, grayImage, CV_BGR2GRAY);

        // if(test){
        //     cvSaveImage( sketchPath() + "/data/projImage.jpg", grayImage);
        //     cvSaveImage( sketchPath() + "/data/camImage.jpg", camera.getIplImage());
        // }
        return grayImage;
    }
    //// Calibrations 

    void initMatrixGui() {

        PFont arial = createFont("arial", 12);

        cameraMatrixText = skatolo.addTextarea("Camera")
                .setPosition(10, MATRICES_HEIGHT)
                .setSize(330, 100)
                .setFont(arial)
                .setLineHeight(14);
        projectorMatrixText = skatolo.addTextarea("Projector")
                .setPosition(300, MATRICES_HEIGHT)
                .setSize(330, 100)
                .setFont(arial)
                .setLineHeight(14);

        if (this.kinectType == Type.X360) {
            System.out.println("Kinect 360 OK...");
            kinectMatrixText = skatolo.addTextarea("Kinect")
                    .setPosition(600, MATRICES_HEIGHT)
                    .setSize(330, 100)
                    .setFont(arial)
                    .setLineHeight(14) //.setColor(color(128))
                    ;
        } else {
            System.out.println("Kinect 360 NOT Activated !");
        }

    }

    public void cornerCalibration(boolean toggleValue) {
        if (toggleValue) {
            startCornerCalibration();
        } else {
            stopCornerCalibration();
        }
    }

    public void startCornerCalibration() {
        loadCorners();
        if (videoPopup == null) {
            videoPopup = new CalibrationVideoPopup(this);
        }
        videoPopup.getSurface().setVisible(true);
        cornersGroup.show();
        cornerToggle.activate();
    }

    public void stopCornerCalibration() {
        videoPopup.getSurface().setVisible(false);
        cornersGroup.hide();
        cornerToggle.deactivate();
    }

    void initCorners() {
        corners = new PVector[4];
        // Corners of the image of the projector
        corners[0] = new PVector(100, 100);
        corners[1] = new PVector(200, 100);
        corners[2] = new PVector(200, 200);
        corners[3] = new PVector(100, 200);
    }

    public void loadCorners() {
        try {
            JSONArray values = loadJSONArray(cornersFileName);
            for (int i = 0; i < values.size(); i++) {
                JSONObject cornerJSON = values.getJSONObject(i);
                corners[i].set(cornerJSON.getFloat("x"),
                        cornerJSON.getFloat("y"));
            }
        } catch (Exception e) {
            System.out.println("Could not load saved corners. check your file : " + cornersFileName);

        }
    }

    public void saveCorners() {
        JSONArray values = new JSONArray();
        for (int i = 0; i < corners.length; i++) {
            JSONObject cornerJSON = new JSONObject();
            cornerJSON.setFloat("x", corners[i].x);
            cornerJSON.setFloat("y", corners[i].y);
            values.setJSONObject(i, cornerJSON);
        }
        saveJSONArray(values, cornersFileName);
    }

    public void activateCornerNo(int nb) {
        cornersRadio.activate(nb);
        // TODO: why is this not automatic ?
        activeCorner(nb);
    }

    public void moveCornerUp(boolean isUp, float amount) {
        corners[currentCorner].y += isUp ? -amount : amount;
    }

    public void moveCornerLeft(boolean isLeft, float amount) {
        corners[currentCorner].x += isLeft ? -amount : amount;
    }

    public void activeCorner(int value) {
        if (value == -1) {
            value = 0;
        }
        currentCorner = value;
    }

    private void initCornersGUI() {
        cornersGroup = skatolo.addGroup("CornersGroup")
                .setPosition(250, 30)
                // .setWidth(300)
                // .setHeight(300)
                .activateEvent(true)
                .setBackgroundColor(color(30))
                .setLabel("Corners :");

        cornersRadio = skatolo.addRadioButton("Corners")
                .setPosition(90, 10)
                .setItemWidth(20)
                .setItemHeight(20)
                .addItem("bottom Left (1)", 0) // 0, y
                .addItem("bottom Right (2)", 1) // x ,y
                .addItem("top right (3)", 2) // x, 0
                .addItem("Top Left (4)", 3) // 0, 0
                .activate(0)
                .plugTo(this, "activeCorner")
                .setGroup("CornersGroup");

        skatolo.addBang("Load")
                .setPosition(10, 10)
                .setCaptionLabel("Load (l)")
                .plugTo(this, "loadCorners")
                .setGroup("CornersGroup");

        this.zoomToggle = skatolo.addToggle("showZoom")
                .setPosition(10, 60)
                .setCaptionLabel("Zoom (z)")
                .setGroup("CornersGroup");

        skatolo.addBang("Save")
                .setPosition(200, 10)
                .setCaptionLabel("Save (s)")
                .plugTo(this, "saveCorners")
                .setGroup("CornersGroup");

        skatolo.addToggle("stopCornerCalibration")
                .setCaptionLabel("Validate")
                .setPosition(200, 60)
                .setGroup("CornersGroup");
        cornersGroup.hide();

    }

    @Override
    public void draw() {
        background(0);

        pushMatrix();
        translate(130, ADD_CALIBRATION_HEIGHT);
        fill(200);
        stroke(180);
        for (int i = 0; i < snapshots.size(); i++) {
            rect(0, 0, 20, 20);
            translate(25, 0);

            if (i % 6 == 5) {
                translate(-25 * 6, 25);
            }
        }
        popMatrix();

        text(this.isProCamCalibrated, 100, DO_CALIBRATION_HEIGHT + 15);
        text(this.isKinectCalibrated, 300, DO_CALIBRATION_HEIGHT + 15);

        if (this.isProCamCalibrated == OK) {
            cameraMatrixText.setColor(color(0, 255, 0));
            cameraMatrixText.setText(Utils.matToString(projector.getExtrinsics()));
            projectorMatrixText.setText("");
        } else {

            // compute the Paper location... 
            cameraMatrixText.setText(Utils.matToString(currentCamBoard()));
            projectorMatrixText.setText(Utils.matToString(currentProjBoard()));
        }

        if (this.kinectType == Type.X360) {
            if (this.isKinectCalibrated == OK) {
                cameraMatrixText.setColor(color(0, 255, 0));
                cameraMatrixText.setText(Utils.matToString(calibrationExtrinsic.getKinectCamExtrinsics()));
            } else {
                kinectMatrixText.setText(Utils.matToString(currentKinect360Board()));
            }
        }

    }

    public void keyPressed() {
        if (key == 'c') {
            startCornerCalibration();
        }

        if (key == 'a') {
            this.addCalibration();
        }

        if (key == 'r') {
            this.reset();
        }

        if (key == 27) { //The ASCII code for esc is 27, so therefore: 27
            this.hide();
            // do not kill the app...
            key = 0;
        }

    }

    private boolean isHidden = false;

    public void hide() {

        if (!isReady) {
            return;
        }
        this.isHidden = true;
        this.getSurface().setVisible(false);
        System.out.println("Projector: " + projector);
        projector.setCalibrationMode(false);
    }

    public void show() {
        if (!isReady) {
            return;
        }
        this.isHidden = false;
        projector.setCalibrationMode(true);
        this.getSurface().setVisible(true);
    }

    public boolean isHidden() {
        return this.isHidden;
    }

}
