/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.depthcam.devices.Kinect360;
import fr.inria.papart.depthcam.devices.KinectDepthAnalysis;
import fr.inria.papart.depthcam.devices.KinectDevice;
import fr.inria.papart.depthcam.devices.KinectOne;
import fr.inria.papart.depthcam.devices.KinectProcessing;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.MarkerBoard;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.ProjectiveDeviceP;
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
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import toxi.geom.Plane;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class CalibrationPopup extends PApplet {

    Papart papart;

    // todo: setTableLocation ?
    // GUI
    private Skatolo skatolo;
    Group cornersGroup;
    RadioButton cornersRadio;
    Toggle cornerToggle;
    protected Toggle zoomToggle;

    // Corners
    CalibrationVideoPopup videoPopup;
    protected PVector[] corners;
    protected int currentCorner = 0;
    protected boolean showZoom = false;
    String cornersFileName;
    static final String CORNERS_NAME = "cornersProj.json";

    // projector rendering.
    ProjectorDisplay projector;
    ProjectorAsCamera projectorAsCamera;
    static final String PROJECTOR_ARTOOLKIT_NAME = "projectorCalibration.cal";
    static final String KINECT_ARTOOLKIT_NAME = "kinectCalibration.cal";

    // calibration App / board 
    CalibrationApp calibrationApp;
    MarkerBoard board;

    // Matrices 
    Textarea cameraMatrixText, projectorMatrixText, kinectMatrixText;

    // Cameras
    Camera cameraTracking;
    Camera cameraKinect;
    TrackedView projectorView;

    // Kinect
    boolean isKinectOne = false;
    boolean isKinect360 = false;
    boolean isKinectOneActivated = false;
    boolean isKinect360Activated = false;

    private KinectDevice kinectDevice;
    private ProjectiveDeviceP projectorDevice;

    private PlaneCalibration planeCalibCam;
    private int frameWidth, frameHeight;

    // calibrations
    ArrayList<CalibrationSnapshot> snapshots = new ArrayList<CalibrationSnapshot>();
    private String isProCamCalibrated = NOTHING;
    private String isKinectCalibrated = NOTHING;

    private static final String COMPUTING = "Computing...";
    private static final String OK = "OK";
    private static final String NOTHING = "";

    public CalibrationPopup() {
        super();
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
    }

    public void settings() {
        size(900, 600);
    }

    public void setup() {

        papart = Papart.getPapart();
        cornersFileName = Papart.calibrationFolder + CORNERS_NAME;
        calibrationApp = new CalibrationApp();
        calibrationApp.pre();
        board = calibrationApp.getBoard();

        cameraTracking = Papart.getPapart().getCameraTracking();
        projector = Papart.getPapart().getProjectorDisplay();
        projectorDevice = projector.getProjectiveDeviceP();
        frameWidth = projectorDevice.getWidth();
        frameHeight = projectorDevice.getHeight();

        initProjectorAsCamera();
        checkKinectVersion();
        activateKinect();

        initCorners();
        loadCorners();

        skatolo = new Skatolo(this, this);
        initMainGui();

        initMatrixGui();

        initCornersGUI();

        frameRate(10);
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

        if (this.isKinect360 || this.isKinectOne) {
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
                        isKinect360Activated ? currentKinect360Board() : null));

    }

    public void clearCalibrations() {

        reset();
    }

    public void calibrateProCam() {
        this.isProCamCalibrated = COMPUTING;
        computeManualCalibrations();
        if (isKinectOne) {
            calibrateKinectOne();
        }
        if (isKinect360) {
            calibrateKinect360();
        }
        this.isProCamCalibrated = OK;
    }

    public void calibrateKinect() {
        calibrateKinect360Plane();
    }

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

        if (isKinect360Activated) {
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

    private void computeManualCalibrations() {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        for (CalibrationSnapshot snapshot : snapshots) {
            PMatrix3D extr = computeExtrinsics(snapshot.cameraPaper,
                    snapshot.projectorPaper);
            Utils.addMatrices(sum, extr);
        }
        Utils.multMatrix(sum, 1f / (float) snapshots.size());
        Papart.getPapart().saveCalibration(Papart.cameraProjExtrinsics, sum);
        projector.setExtrinsics(sum);
    }

    private PMatrix3D computeExtrinsics(PMatrix3D camPaper, PMatrix3D projPaper) {
        PMatrix3D extr = projPaper.get();
        extr.invert();
        extr.preApply(camPaper);
        extr.invert();
        return extr;
    }

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
        projector.setCalibrationMode(true);
        cornerToggle.activate();
    }

    public void stopCornerCalibration() {
        projector.setCalibrationMode(false);
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

    void checkKinectVersion() {
        kinectDevice = Papart.getPapart().getKinectDevice();
        // if the program does not have a Kinect, we should not try to calibrate it... 
        if (kinectDevice == null) {
            return;
        }
        isKinect360 = kinectDevice instanceof Kinect360;
        isKinectOne = kinectDevice instanceof KinectOne;
    }

    void activateKinect() {
        if (isKinectOne) {
            initKinectOne();
        }
        if (isKinect360) {
            initKinect360();
        }
    }

    void initKinectOne() {
        initCommonKinect();

        // Kinect camera is the main tracking Camera
        isKinectOneActivated = true;
    }

    void initKinect360() {
        cameraKinect = kinectDevice.getCameraRGB();

        String ARToolkitCalib = Papart.calibrationFolder + KINECT_ARTOOLKIT_NAME;
        cameraKinect.convertARParams(this, cameraKinect.getCalibrationFile(), ARToolkitCalib);
        cameraKinect.initMarkerDetection(ARToolkitCalib);

        // No display for now...
//        arDisplayKinect = new ARDisplay(this, cameraKinect);
//        arDisplayKinect.init();
//        arDisplayKinect.manualMode();
//        app.addDisplay(arDisplayKinect);
        cameraKinect.trackSheets(true);

        // as it does not comes with the display:
        cameraKinect.trackMarkerBoard(board);

        initCommonKinect();
        isKinect360Activated = true;
        System.out.println("Kinect 360 Activated !");
    }

    void initCommonKinect() {
//        kinectAnalysis = Papart.getPapart().getKinectAnalysis(); // new KinectProcessing(this, kinectDevice);

        // init is done later now.

    }

    private PMatrix3D currentKinect360Board() {
        return board.getTransfoMat(cameraKinect).get();
    }

    private PMatrix3D currentCamBoard() {
        return board.getTransfoMat(cameraTracking).get();
    }

    private PMatrix3D currentProjBoard() {
        IplImage projImage = projectorImage();
        if (projImage == null) {
            return new PMatrix3D();
        }
        board.updatePosition(projectorAsCamera, projImage);
        return board.getTransfoMat(projectorAsCamera);
    }

    private IplImage grayImage = null;

    private IplImage projectorImage() {
        projectorView.setCorners(corners);
        IplImage projImage = projectorView.getIplViewOf(cameraTracking);
        if (board.useARToolkit()) {
            projImage = greyProjectorImage(projImage);
        }
        return projImage;
    }

    private IplImage greyProjectorImage(IplImage projImage) {
        if (grayImage == null) {
            grayImage = IplImage.create(projector.getWidth(),
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

    private void calibrateKinectOne() {
        PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();
        kinectExtr.invert();

        planeCalibCam = computeAveragePlaneCam();
        planeCalibCam.flipNormal();

        // identity - no external camera for ProCam calibration
        PMatrix3D kinectCameraExtrinsics = new PMatrix3D();
        // Depth -> Color calibration.
        kinectCameraExtrinsics.set(kinectExtr);

        HomographyCalibration homography = computeScreenPaperIntersection(planeCalibCam, kinectCameraExtrinsics);

        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return;
        }


        // move the plane up a little.
        planeCalibCam.moveAlongNormal(-7f);

        saveKinectPlaneCalibration(planeCalibCam, homography);
        saveKinectCameraExtrinsics(kinectCameraExtrinsics);
    }

    private void calibrateKinect360() {
        calibrateKinect360Extr();
        calibrateKinect360Plane();
    }

    private void calibrateKinect360Extr() {
        // Depth -> color  extrinsics
        PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        kinectExtr.invert();

        // depth -> tracking
        PMatrix3D kinectCameraExtrinsics = computeKinectCamExtrinsics(kinectExtr);

        // // tracking -> depth
        kinectCameraExtrinsics.invert();

        saveKinectCameraExtrinsics(kinectCameraExtrinsics);
    }

    private void calibrateKinect360Plane() {
        // Depth -> color  extrinsics
        PMatrix3D kinectExtr = kinectDevice.getStereoCalibration().get();

        // color -> depth  extrinsics
        kinectExtr.invert();

        planeCalibCam = computeAveragePlaneCam();
        PlaneCalibration planeCalibKinect = computeAveragePlaneKinect(kinectExtr);
        planeCalibCam.flipNormal();

        // Tracking --> depth
        PMatrix3D kinectCameraExtrinsics = papart.loadCalibration(Papart.kinectTrackingCalib);

        HomographyCalibration homography = computeScreenPaperIntersection(planeCalibCam,
                kinectCameraExtrinsics);
        if (homography == HomographyCalibration.INVALID) {
            System.err.println("No intersection");
            return;
        }

        // move the plane up a little.
        planeCalibKinect.flipNormal();
        planeCalibKinect.moveAlongNormal(-20f);

        saveKinectPlaneCalibration(planeCalibKinect, homography);
        this.isKinectCalibrated = OK;
    }

    private PMatrix3D computeKinectCamExtrinsics(PMatrix3D stereoExtr) {
        PMatrix3D sum = new PMatrix3D(0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0);

        int nbCalib = 0;
        for (CalibrationSnapshot snapshot : snapshots) {
            if (snapshot.kinectPaper == null) {
                continue;
            }

            // Color -> Paper
            PMatrix3D boardFromDepth = snapshot.kinectPaper.get();

            /// depth -> color -> color -> Paper
            boardFromDepth.preApply(stereoExtr);

            PMatrix3D extr = computeExtrinsics(boardFromDepth, snapshot.cameraPaper);

            Utils.addMatrices(sum, extr);
            nbCalib++;
        }

        Utils.multMatrix(sum, 1f / (float) nbCalib);
        return sum;
    }

    private PlaneCalibration computeAveragePlaneKinect(PMatrix3D stereoExtr) {
        PVector paperSize = new PVector(297, 210);

        Plane sumKinect = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbCalib = 0;
        for (CalibrationSnapshot snapshot : snapshots) {
            if (snapshot.kinectPaper == null) {
                continue;
            }

            //  color -> paper
            PMatrix3D boardFromDepth = snapshot.kinectPaper.get();

            // Depth -> color -> color -> paper
            boardFromDepth.preApply(stereoExtr);

            PlaneCalibration planeCalibKinect
                    = PlaneCalibration.CreatePlaneCalibrationFrom(boardFromDepth, paperSize);
            Utils.sumPlane(sumKinect, planeCalibKinect.getPlane());
            nbCalib++;
        }

        Utils.averagePlane(sumKinect, 1f / nbCalib);

        PlaneCalibration calibration = new PlaneCalibration();
        calibration.setPlane(sumKinect);
        calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

//        System.out.println("Plane viewed by the kinect");
//        println(sumKinect);
        return calibration;
    }

    private PlaneCalibration computeAveragePlaneCam() {
        PVector paperSize = new PVector(297, 210);

        Plane sumCam = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));

        int nbPlanes = 0;
        for (CalibrationSnapshot snapshot : snapshots) {

            if (snapshot.cameraPaper == null) {
                continue;
            }

            PlaneCalibration cam = PlaneCalibration.CreatePlaneCalibrationFrom(
                    snapshot.cameraPaper.get(), paperSize);

            Utils.sumPlane(sumCam, cam.getPlane());
            nbPlanes++;
        }

        Utils.averagePlane(sumCam, 1f / nbPlanes);

        PlaneCalibration calibration = new PlaneCalibration();
        calibration.setPlane(sumCam);
        calibration.setHeight(PlaneCalibration.DEFAULT_PLANE_HEIGHT);

//        System.out.println("Plane viewed by the camera");
//        println(sumCam);
        return calibration;
    }

    PMatrix3D lastKinectCamExtrinsics = null;
    void saveKinectCameraExtrinsics(PMatrix3D kinectCameraExtrinsics) {
        lastKinectCamExtrinsics = kinectCameraExtrinsics;
        papart.saveCalibration(Papart.kinectTrackingCalib, kinectCameraExtrinsics);
    }

    void saveKinectPlaneCalibration(PlaneCalibration planeCalib, HomographyCalibration homography) {
        PlaneAndProjectionCalibration planeProjCalib = new PlaneAndProjectionCalibration();
        planeProjCalib.setPlane(planeCalib);
        planeProjCalib.setHomography(homography);
        planeProjCalib.saveTo(this, Papart.planeAndProjectionCalib);

        ((KinectTouchInput) papart.getTouchInput()).setPlaneAndProjCalibration(planeProjCalib);
    }

    private HomographyCalibration computeScreenPaperIntersection(PlaneCalibration planeCalibCam,
            PMatrix3D kinectCameraExtrinsics) {

        // generate coordinates...
        float step = 0.5f;
        int nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
        HomographyCreator homographyCreator = new HomographyCreator(3, 2, nbPoints);

        // Creates 3D points on the corner of the screen
        int k = 0;
        for (float i = 0; i <= 1.0; i += step) {
            for (float j = 0; j <= 1.0; j += step, k++) {

                PVector screenPoint = new PVector(i, j);
                PVector kinectPoint = new PVector();

                // where the point is on the table. viewed by the tracking.
                PVector inter = computeIntersection(planeCalibCam, i, j);
                if (inter == null) {
                    return HomographyCalibration.INVALID;
                }

                // inter is viewed from tracking.
                // depth --> tracking
                kinectCameraExtrinsics.mult(inter, kinectPoint);

                homographyCreator.addPoint(kinectPoint, screenPoint);
            }
        }
        return homographyCreator.getHomography();
    }

    PVector computeIntersection(PlaneCalibration planeCalibCam, float px, float py) {

        // Create ray from the projector (origin / viewed pixel)
        // Intersect this ray with the piece of paper.
        // Compute the Two points for the ray
        PVector originP = new PVector(0, 0, 0);
        PVector viewedPtP = projectorDevice.pixelToWorldNormP((int) (px * frameWidth), (int) (py * frameHeight));

        // Pass it to the camera point of view (origin)
        PMatrix3D proCamExtrinsics = projector.getExtrinsicsInv();
        PVector originC = new PVector();
        PVector viewedPtC = new PVector();
        proCamExtrinsics.mult(originP, originC);
        proCamExtrinsics.mult(viewedPtP, viewedPtC);

        // Second argument is a direction
        viewedPtC.sub(originC);

        Ray3D ray = new Ray3D(new Vec3D(originC.x,
                originC.y,
                originC.z),
                new Vec3D(viewedPtC.x,
                        viewedPtC.y,
                        viewedPtC.z));

        // Intersect ray with Plane
        ReadonlyVec3D inter = planeCalibCam.getPlane().getIntersectionWithRay(ray);

        if (inter == null) {
            println("No intersection :( check stuff");
            return null;
        }

        return new PVector(inter.x(), inter.y(), inter.z());
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

        if (isKinect360Activated) {
            if (this.isKinectCalibrated == OK) {
                cameraMatrixText.setColor(color(0, 255, 0));
                cameraMatrixText.setText(Utils.matToString(lastKinectCamExtrinsics));
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
        }
        if (key == ESC) {
            key = 0;
        }

    }

    private boolean isHidden = false;

    public void hide() {
        this.isHidden = true;
        this.getSurface().setVisible(false);
    }

    public void show() {
        this.isHidden = false;
        this.getSurface().setVisible(true);
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    class CalibrationSnapshot {

        PMatrix3D cameraPaper = null;
        PMatrix3D projectorPaper = null;
        PMatrix3D kinectPaper = null;

        public CalibrationSnapshot(PMatrix3D cameraPaperCalibration,
                PMatrix3D projectorPaperCalibration,
                PMatrix3D kinectPaperCalibration) {
            cameraPaper = cameraPaperCalibration.get();
            projectorPaper = projectorPaperCalibration.get();
            if (kinectPaperCalibration != null) {
                kinectPaper = kinectPaperCalibration.get();
            }
        }

    }

}
