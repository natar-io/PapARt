/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016-2017 RealityTech
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
package fr.inria.papart.calibration;

import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.ProjectorAsCamera;
import fr.inria.papart.procam.camera.SubCamera;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.tracking.DetectedMarker;
import tech.lity.rea.skatolo.Skatolo;
import tech.lity.rea.skatolo.gui.controllers.Toggle;
import tech.lity.rea.skatolo.gui.group.RadioButton;
import tech.lity.rea.skatolo.gui.group.Textarea;
import java.util.ArrayList;
import org.bytedeco.javacpp.ARToolKitPlus;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;
import processing.core.PMatrix3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class CalibrationUI extends PApplet {

    Papart papart;

    // todo: setTableLocation ?
    // GUI
    private Skatolo skatolo;
    private RadioButton cornersRadio;
    private Toggle cornerToggle;
    protected Toggle zoomToggle;

    // Corners
    private VideoPopupApp videoPopup;

    // projector rendering.
    protected ProjectorDisplay projector;

    static final String PROJECTOR_ARTOOLKIT_NAME = "projectorCalibration.cal";
    static final String KINECT_ARTOOLKIT_NAME = "kinectCalibration.cal";

    private ARToolKitPlus.MultiTracker projectorTracker = null;

    // calibration App / board
    protected PaperScreen calibrationApp;
    protected PaperScreen pointer;
    private MarkerBoard board;

    // Matrices
    private Textarea cameraMatrixText, projectorMatrixText, kinectMatrixText;

    // Cameras
    private Camera cameraTracking;
    private Camera cameraFromDepthCam;  // can be the same as cameraTracking.
    protected TrackedView projectorView;
    protected ProjectorAsCamera projectorAsCamera;

    // Kinect
    private Camera.Type depthCameraType;
    private boolean useExternalColorCamera = false;

    // calibrations
    private ArrayList<ExtrinsicSnapshot> snapshots = new ArrayList<ExtrinsicSnapshot>();
    private String isProCamCalibrated = NOTHING;
    private String isKinectCalibrated = NOTHING;

    private static final String COMPUTING = "Computing...";
    private static final String OK = "OK";
    private static final String NOTHING = "";

    private ExtrinsicCalibrator calibrationExtrinsic;
    private PImage backgroundImg;

    public CalibrationUI(PaperScreen screen, PaperScreen pointer) {
        super();
        this.calibrationApp = screen;
        this.pointer = pointer;
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    @Override
    public void settings() {
        size(900, 600);
    }

    @Override
    public void setup() {

        // Papart was created before, already with a camera, projector and Dcam. 
        if (calibrationApp == null) {
            calibrationApp = new CalibrationApp();
            calibrationApp.pre();
        }

        board = calibrationApp.getBoard();

        cameraTracking = Papart.getPapart().getPublicCameraTracking();
        projector = Papart.getPapart().getProjectorDisplay();

        calibrationExtrinsic = new ExtrinsicCalibrator(this);
        initProjectorAsCamera();
        calibrationExtrinsic.setProjector(projector);

        papart = Papart.getPapart();
        depthCameraType = papart.getDepthCameraType();
        DepthCameraDevice depthCameraDevice = papart.getDepthCameraDevice();

        // There is a depth camera, and it is started. 
        if (depthCameraType != Camera.Type.FAKE && depthCameraDevice != null) {

            // There is a depth camera: 
            //   - either the tracking is with the depth camera
            //   - or it is another camera
            // 1. tracking camera is part of a depth camera
            if (cameraTracking instanceof SubCamera
                    && cameraTracking == depthCameraDevice.getMainCamera().getActingCamera()) {
                // tracking should be loaded already ! 
                System.out.println("Calibration with a Depth and color camera in the same device.");
                cameraFromDepthCam = cameraTracking;
                useExternalColorCamera = false;
            }

            // 2. Tracking camera is another camera. 
            if (cameraTracking != depthCameraDevice.getColorCamera()
                    && cameraTracking != depthCameraDevice.getIRCamera()) {
                // the depth camera 
                initTrackingOn(papart.getDepthCameraDevice());
                System.out.println("Calibration with a Depth and color camera in DIFFERENT devices.");
                useExternalColorCamera = true;
            }
            calibrationExtrinsic.setDepthCamera(depthCameraDevice);
        } else {
            System.out.println("Calibration with a color camera.");
        }

        skatolo = new Skatolo(this, this);
        initMainGui();
        initMatrixGui();

        backgroundImg = loadImage(Papart.calibrationFolder + "ressources/background.png");
        this.isReady = true;
    }

    boolean isReady = false;

    private void initProjectorAsCamera() {
        projectorView = new TrackedView();
        projectorView.setImageWidthPx(projector.getWidth());
        projectorView.setImageHeightPx(projector.getHeight());

        if (cameraTracking.isPixelFormatGray()) {
            projectorView.init(PApplet.GRAY);
        }
        if (cameraTracking.isPixelFormatColor()) {
            projectorView.init(PApplet.RGB);
        }

        projectorView.useListOfPairs(true);
        projectorView.clearObjectImagePairs();

        projectorAsCamera = new ProjectorAsCamera(projector, cameraTracking, projectorView);
        projectorAsCamera.setCalibration(Papart.projectorCalib);
        projectorAsCamera.setParent(this);

        // if it uses gray images.
        // All of this needs to be more explicit. 
        if (board.useGrayImages()) {
            projectorAsCamera.setPixelFormat(Camera.PixelFormat.GRAY);
        }

        /** This might be obselete */
        if (board.getMarkerType() == MarkerBoard.MarkerType.ARTOOLKITPLUS) {
            String ARToolkitCalibFile = Papart.calibrationFolder + "projector.cal";
            ProjectorAsCamera.convertARProjParams(this, projectorAsCamera.getCalibrationFile(),
                    ARToolkitCalibFile);
            projectorAsCamera.setCalibrationARToolkit(ARToolkitCalibFile);
        }
        if (board.getMarkerType() == MarkerBoard.MarkerType.SVG) {
            projectorTracker = DetectedMarker.createDetector(projector.getWidth(), projector.getHeight());
        }

        projectorAsCamera.trackSheets(true);
        projectorAsCamera.trackMarkerBoard(board);

        // warrning experimental
        projectorAsCamera.setThread();
    }

    private void initTrackingOn(DepthCameraDevice kinectDevice) {
        kinectDevice.getMainCamera().actAsColorCamera();
        kinectDevice.getMainCamera().trackSheets(true);

        cameraFromDepthCam = kinectDevice.getColorCamera();

        if (board.getMarkerType() == MarkerBoard.MarkerType.ARTOOLKITPLUS) {
            String ARToolkitCalib = Papart.calibrationFolder + KINECT_ARTOOLKIT_NAME;
            Camera.convertARParams(this, cameraFromDepthCam.getProjectiveDevice(), ARToolkitCalib);
            cameraFromDepthCam.setCalibrationARToolkit(ARToolkitCalib);
        }

        // No display for now...
        cameraFromDepthCam.trackSheets(true);

        // as it does not comes with the display:
        cameraFromDepthCam.trackMarkerBoard(board);
    }

    void reset() {
        snapshots.clear();
        this.isProCamCalibrated = NOTHING;
        this.isKinectCalibrated = NOTHING;
        cameraMatrixText.setColor(color(255));
        cameraMatrixText.setColor(color(255));
    }

    static final int ADD_CALIBRATION_HEIGHT = 250;
    static final int DO_CALIBRATION_HEIGHT = 250;
    static final int MATRICES_HEIGHT = 450;

    void initMainGui() {
        cornerToggle = skatolo.addToggle("cornerCalibration")
                .setPosition(20, 40)
                .setCaptionLabel("Projector Corner Calibration (c)");

        skatolo.addBang("addCalibration")
                .setPosition(20, ADD_CALIBRATION_HEIGHT)
                .setCaptionLabel("Add Calibration (a)");

        skatolo.addBang("clearCalibrations")
                .setPosition(320, ADD_CALIBRATION_HEIGHT)
                .setCaptionLabel("clear Calibration");

        skatolo.addBang("calibrate procam")
                .setPosition(530, DO_CALIBRATION_HEIGHT)
                //                .setCaptionLabel("Calibration ProCam")
                .plugTo(this, "calibrateProCam");

        if (depthCameraType != Camera.Type.FAKE) {
            skatolo.addBang("calibrateKinect")
                    .setPosition(720, DO_CALIBRATION_HEIGHT)
                    .setCaptionLabel("Plane Calibration");
        }

    }

    public void addCalibration() {
        snapshots.add(new ExtrinsicSnapshot(
                currentCamBoard(),
                currentProjBoard(),
                currentDepthCameraBoard()));
    }

    public void clearCalibrations() {

        reset();
    }

    public void calibrateProCam(boolean useExternal) {
        this.isProCamCalibrated = COMPUTING;
        calibrationExtrinsic.computeProjectorCameraExtrinsics(snapshots);
        calibrationExtrinsic.calibrateKinect(snapshots, useExternalColorCamera);

//        projectorView.getHomographyOf(cameraTracking).saveTo(this, Papart.cameraProjHomography);
        Papart.getPapart().saveCalibration(Papart.cameraProjHomography,
                projectorView.getHomographyOf(cameraTracking).getHomography());
        this.isProCamCalibrated = OK;
        this.isKinectCalibrated = OK;

    }

    public void calibrateKinect(boolean useExternal) {
        boolean isCalibOK = calibrationExtrinsic.calibrateDepthCamPlane(snapshots);
        if (isCalibOK) {
            this.isKinectCalibrated = OK;
        }
    }

    private PMatrix3D currentDepthCameraBoard() {
        if (cameraFromDepthCam == null || cameraFromDepthCam == cameraTracking) {
            return null;
        }
        return board.getTransfoMat(cameraFromDepthCam).get();
    }

    private PMatrix3D currentCamBoard() {
        return board.getTransfoMat(cameraTracking).get();
    }

    private PMatrix3D currentProjBoard() {
        return board.getTransfoMat(projectorAsCamera);
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

        if (useExternalColorCamera) {
            System.out.println("Depth camera OK...");
            kinectMatrixText = skatolo.addTextarea("Kinect")
                    .setPosition(600, MATRICES_HEIGHT)
                    .setSize(330, 100)
                    .setFont(arial)
                    .setLineHeight(14) //.setColor(color(128))
                    ;
        } else {
            System.out.println("External camera not activated.");
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
        if (videoPopup == null) {
            videoPopup = new VideoPopupApp(this);
        } else {
            videoPopup.activate();
        }
        projector.setCalibrationMode(true);
        this.hideForScreenshot();
    }

    public void stopCornerCalibration() {
        cornerToggle.deactivate();
        projector.setCalibrationMode(false);
        this.showAfterScreenshot();
    }

    @Override
    public void draw() {
        image(backgroundImg, 0, 0, backgroundImg.width, backgroundImg.height);

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

        if (useExternalColorCamera) {
            if (this.isKinectCalibrated == OK) {
                cameraMatrixText.setColor(color(0, 255, 0));
                cameraMatrixText.setText(Utils.matToString(calibrationExtrinsic.getKinectCamExtrinsics()));
            } else {
                kinectMatrixText.setText(Utils.matToString(currentDepthCameraBoard()));
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
        calibrationApp.setDrawing(false);
        this.projectorAsCamera.stopThread();
    }

    public void hideForScreenshot() {
        this.isHidden = true;
        this.getSurface().setVisible(false);
    }

    public void show() {
        if (!isReady) {
            return;
        }
        this.isHidden = false;
        calibrationApp.setDrawing(true);
        this.getSurface().setVisible(true);

        if (!this.projectorAsCamera.useThread()) {
            this.projectorAsCamera.setThread();
        }
    }

    public void showAfterScreenshot() {
        this.isHidden = false;
        this.getSurface().setVisible(true);
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    class CalibrationApp extends PaperScreen {

        @Override
        public void settings() {
            setDrawingSize(297, 210);
            loadMarkerBoard(Papart.markerFolder + Papart.calibrationFileName, 162, 104);// 297, 210);
            setDrawAroundPaper();
        }

        @Override
        public void setup() {
            // No filtering
            setDrawingFilter(0);
        }

        @Override
        public void drawAroundPaper() {
        }
    }

}
