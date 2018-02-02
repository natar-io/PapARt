/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.multitouch.SkatoloLink;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.detection.BlinkTracker;
import fr.inria.papart.multitouch.detection.ColorTracker;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.ColorDetection;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.ProjectorAsCamera;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.tracking.DetectedMarker;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.utils.DrawUtils;
import java.util.ArrayList;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PConstants;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.HALF_PI;
import static processing.core.PConstants.RGB;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import tech.lity.rea.skatolo.Skatolo;
import tech.lity.rea.skatolo.gui.controllers.HoverButton;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
import fr.inria.papart.procam.camera.CameraThread;
import fr.inria.papart.procam.display.BaseDisplay;
import org.bytedeco.javacpp.opencv_imgcodecs;
import static processing.core.PApplet.abs;
import static processing.core.PApplet.split;
import static processing.core.PConstants.CORNER;
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;
import tech.lity.rea.skatolo.gui.controllers.Button;
import tech.lity.rea.skatolo.gui.controllers.Toggle;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

class TableTest extends PaperTouchScreen {

    public void settings() {
        try {
            setDrawingSize(200, 200);
//            loadMarkerBoard(Papart.markerFolder + "calib1.svg", 297, 210);
            setDrawOnPaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup() {
        useManualLocation(true, Papart.getPapart().getTableLocation());
    }

    public void drawOnPaper() {
//        setLocation(Papart.getPapart().getTableLocation());
//        setLocation(-100, -100, 0);
//        background(180, 20, 20);

        stroke(180);
        noFill();
        for (int i = 0; i < 200; i += 10) {
            line(0, i, drawingSize.x, i);
        }
        for (int i = 0; i < 200; i += 10) {
            line(i, 0, i, drawingSize.y);
        }
//        fill(200, 200, 20);
//        rect(0, 0, 50, 50);
    }

}

public class MultiCalibrator extends PaperTouchScreen {

    // TODO: Add depth calibration in the scenario. 
    // TODO: add current calibration estimation. 
    // Take color Image screenshots and store them (as JPG / PNG or in memory ?)
    // 1. Project a light point +  2~4 blinking points top & bot 
    //  -> When paperScreen stops moving (< 1cm?), capture all images for blinking analysis.
    // Visual indication that the movement stopped (OK stay still for x seconds...) 
    // Compute center position from Markers +  center from blinking points if all found.
    // If not too different, store it, and a screenshot for later estimation. 
    // Capture color spots from blinking analysis and store them. 
    // Repeat 4 - 6 times. 
    // Once done we have: 
    // 4 - 6 screenshots from Camera. 
    // Pairs of projected / captured points.  
    // Loads of color data for color points. 
    // 1. Homography computation:  Camera - projector.
    // 2. Extract 4-6 projector images. 
    // 3. Find markers if these projector images +  find markers in Cam images. 
    // 4. Match 3D positions for extrinsic calibration and save it. 
    // 5. Check - Extrinsic calibration ? (How to do it easily?)  
    // 6. Compute color histograms, take the most commons, compute H,S,B + R,G,B  means + stdev. 
    // 7. Check the colors for color tracking across 4-6 screenshots. save the colors.
    // IDEA for touch / test: add 2-3 cubes or cylinder for touch testing ?
    protected boolean active = false;

    private Papart papart;
    DepthTouchInput depthTouchInput;
    private DepthCameraDevice depthCameraDevice;

    public float zShift = 5f;

//    private Skatolo skatolo;
//    private HoverButton hoverButton, resetButton;
    int pressedAmt = 0;
//    int maxPressedAmt = 30 * 3; // 2 secs
    int maxPressedAmt = 20; // 2 secs

    int pressedAmtReset = 0;

    // 6 points for Homography matching. 
    public PVector screenPoints[];
    public int currentScreenPoint = 0;
    int nbScreenPoints = 8;
    int nbColors = 5;

    PlaneAndProjectionCalibration planeProjCalib;
    public boolean toSave = false;

    IplImage savedImages[];
    PMatrix3D savedLocations[];
    int savedColors[][];
    private TrackedView projectorView;
    private ProjectorAsCamera projectorAsCamera;

    // projector rendering.
    protected ProjectorDisplay projector;

    @Override
    public void settings() {
        papart = Papart.getPapart();
        try {
            setDrawingSize(297, 210);
            loadMarkerBoard(Papart.markerFolder + "calib1.svg", 297, 210);
            setDrawOnPaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void button() {
        System.out.println("Button pressed.");
    }

    public void undo() {
        currentScreenPoint = currentScreenPoint - 1;
        if (currentScreenPoint < 0) {
            currentScreenPoint = 0;
        }
    }

    public static TableTest tableTest = null;

    @Override
    public void setup() {
        try {

            if (tableTest == null) {
//                tableTest = new TableTest();
//                tableTest.setDrawing(false);
            }
            setDrawingFilter(0);
            setTrackingFilter(0, 0);

            savedImages = new IplImage[nbScreenPoints];
            savedLocations = new PMatrix3D[nbScreenPoints];
            savedColors = new int[nbScreenPoints * 2][nbColors];

            this.seeThrough = !(getDisplay() instanceof ProjectorDisplay);
//            initButtons();
            initScreenPoints();
            initProjectorAsCamera();

            initColorTrackers();
            // GREEN circle 
//                 fill(0, 255, 0);
//        rect(79.8f, 123.8f, 15f, 15f);
            depthTouchInput = (DepthTouchInput) papart.getTouchInput();
            depthTouchInput.useRawDepth();
            depthCameraDevice = papart.getDepthCameraDevice();

        } catch (Exception e) {
            e.printStackTrace();
        }
        startCalib();
    }

    void initColorTrackers() {

        PVector red1P = new PVector(79.8f, 95.2f);
        PVector red2P = new PVector(208.2f, 95.2f);

        red1 = createColorDetection(red1P);
        red2 = createColorDetection(red2P);

        PVector blue1P = new PVector(79.8f, 67.1f);
        PVector blue2P = new PVector(208.2f, 67.1f);

        blue1 = createColorDetection(blue1P);
        blue2 = createColorDetection(blue2P);

        detections = new ColorDetection[this.nbColors * 2];

        detections[0] = red1;
        detections[1] = red2;
        detections[2] = blue1;
        detections[3] = blue2;

        // green
        detections[4] = createColorDetection(new PVector(79.8f, 123.8f));
        detections[5] = createColorDetection(new PVector(208.2f, 123.8f));

        // purple
        detections[6] = createColorDetection(new PVector(108.1f, 123.8f));
        detections[7] = createColorDetection(new PVector(179.4f, 123.8f));

        // orange
        detections[8] = createColorDetection(new PVector(108.1f, 67.1f));
        detections[9] = createColorDetection(new PVector(179.4f, 67.1f));
    }

    ColorDetection red1, red2;
    ColorDetection blue1, blue2;
    ColorDetection[] detections;

    ColorDetection createColorDetection(PVector position) {
        int captureW = 15; // 15mm
        float smallSquareHalfWidth = 2.7f;
        int capSizePx = 4;

        ColorDetection detection = new ColorDetection((PaperScreen) this);
        detection.setPosition(new PVector(position.x + captureW / 2 - smallSquareHalfWidth,
                position.y + captureW / 2 - smallSquareHalfWidth));
        detection.setCaptureSize(new PVector(smallSquareHalfWidth * 2, smallSquareHalfWidth * 2));
        // in PX
        detection.setPicSize(capSizePx, capSizePx);
        detection.init();
        return detection;
    }

//    void initButtons() {
//        skatolo = new Skatolo(this.parent, this);
//        skatolo.getMousePointer().disable();
//        skatolo.setAutoDraw(false);
//        int sizeAdd = 10;
//        hoverButton = skatolo.addHoverButton("button")
//                .setPosition(79.8f - sizeAdd, 123.8f - sizeAdd)
//                .setSize(15 + 2 * sizeAdd, 15 + 2 * sizeAdd);
//        resetButton = skatolo.addHoverButton("resetPoint")
//                .setPosition(208.2f - sizeAdd, 123.8f - sizeAdd)
//                .setSize(15 + 2 * sizeAdd, 15 + 2 * sizeAdd);
//    }
    private int deadZone = 150;
    int pw, ph;

    void initScreenPoints() {
        screenPoints = new PVector[nbScreenPoints];
        pw = this.getDisplay().getWidth();
        ph = this.getDisplay().getHeight();
        // 1 in each quadrant. 
        // 2 random
        screenPoints[0] = new PVector(pw / 2, ph / 2, 0);
        screenPoints[1] = new PVector(
                parent.random(deadZone, pw / 2 - deadZone),
                parent.random(deadZone, ph / 2 - deadZone),
                parent.random(HALF_PI));
        screenPoints[2] = new PVector(
                parent.random(pw / 2 + deadZone, pw - deadZone),
                parent.random(deadZone, ph / 2 - deadZone),
                parent.random(HALF_PI));

        screenPoints[3] = new PVector(
                parent.random(deadZone, pw / 2 - deadZone),
                parent.random(ph / 2 + deadZone, ph - deadZone),
                parent.random(HALF_PI));
        screenPoints[4] = new PVector(
                parent.random(pw / 2 + deadZone, pw - deadZone),
                parent.random(ph / 2 + deadZone, ph - deadZone),
                parent.random(HALF_PI));

        for (int i = 5; i < nbScreenPoints; i++) {
            screenPoints[i] = createRandomPoint();
        }
    }

    private PVector createRandomPoint() {
        return new PVector(
                parent.random(deadZone, pw - deadZone),
                parent.random(deadZone, ph - deadZone),
                parent.random(HALF_PI));
    }

    boolean seeThrough = false;

    private void initProjectorAsCamera() {

        if (seeThrough) {
            System.out.println("No Projector to calibrate...");
            return;
        }
        projector = (ProjectorDisplay) getDisplay();

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
        projectorAsCamera.setParent(parent);

        MarkerBoard board = this.getMarkerBoard();
        // if it uses gray images.
        // All of this needs to be more explicit. 
        if (board.useGrayImages()) {
            projectorAsCamera.setPixelFormat(Camera.PixelFormat.GRAY);
        }

        // Warning -> Only works with SVG tracking. 
//        if (board.getMarkerType() == MarkerBoard.MarkerType.SVG) {
//            projectorTracker = DetectedMarker.createDetector(projector.getWidth(), projector.getHeight());
//        }
        projectorAsCamera.trackSheets(true);
        projectorAsCamera.trackMarkerBoard(board);

// No filtering        
        board.forceUpdate(projectorAsCamera, Integer.MAX_VALUE);
        board.setDrawingMode(projectorAsCamera, false, 0);
        board.removeFiltering(projectorAsCamera);

        // warrning experimental
//        projectorAsCamera.setThread();
    }

    float maxMovement = 8f;

    @Override
    public void drawOnPaper() {
        // setLocation(63, 45, 0);
//      DetectedMarker[] list = papart.getMarkerList();
//      println("markers :  " + list.length);

        // background: blue
        background(0, 0, 200, 50);

        if (active) {

//            red1.drawSelf();
//            System.out.println("Framerate: " + parent.frameRate);
            computeTouch();
            if (toSave) {
                saveTouch();
            }
            if (showTouch) {
                drawTouch(10);
            }

            float d = getMarkerBoard().lastMovementDistance(getCameraTracking());

            if (waitForMovement && d > 20) {
                waitForMovement = false;
            }

//            SkatoloLink.updateTouch(touchList.get2DTouchs(), skatolo);
//            skatolo.draw(getGraphics());
            if (showProjection) {
                drawDebugZones();
            }

//            PMatrix3D camPos = currentCamBoard();
//            PVector pos3D = new PVector(camPos.m03, camPos.m13, camPos.m23);
//            PVector pxCam = cameraTracking.getProjectiveDevice().worldToPixelCoord(pos3D, false);
//            System.out.println("PXCam: " + pxCam);
            // Not moving, draw something.
            if (!waitForMovement && d < maxMovement) {
                // Touch
                if (parent.mousePressed) {
                    if (parent.mouseButton == LEFT) {
                        valid();
                    }
                    if (parent.mouseButton == RIGHT) {
                        cancel();
                    }
                }

//                if (resetButton.isActive()) {
//                    cancel();
//                }
//                if (hoverButton.isActive()) {
//                    valid();
//                }
            } else {
                pressedAmt = 0;
                pressedAmtReset = 0;
            }
        }
    }

    void valid() {
        pressedAmt++;
        if (pressedAmt == maxPressedAmt) {
            savePicture();
            nextScreenshot();
            waitForMovement = true;
//            System.out.println("Saving location: ");
//            getLocation().print();
//            this.hoverButton.isActive = false;
        }
    }

    void cancel() {
        pressedAmtReset++;
        if (pressedAmtReset == maxPressedAmt) {
            resetCurrentPoint();
            pressedAmtReset = 0;
//            this.resetButton.isActive = false;
        }
    }

    public void changePoint() {
        resetCurrentPoint();
    }

    // Except first point
    void resetCurrentPoint() {
        if (currentScreenPoint != 0) {
            this.screenPoints[currentScreenPoint] = createRandomPoint();
        }
    }

    // Move the piece of paper between poses
    boolean waitForMovement = true;

    void savePicture() {
        this.savedImages[currentScreenPoint] = cameraTracking.getIplImage().clone();
        this.savedLocations[currentScreenPoint] = currentCamBoard();

        // Save the touch plane  TODO: not used remove this
//        red1.computeColor();
//        red2.computeColor();
//        blue1.computeColor();
//        blue2.computeColor();
//        this.savedColors[currentScreenPoint * 2][0] = red1.getColor();
//        this.savedColors[currentScreenPoint * 2 + 1][0] = red2.getColor();
//        this.savedColors[currentScreenPoint * 2][1] = blue1.getColor();
//        this.savedColors[currentScreenPoint * 2 + 1][1] = blue1.getColor();
        for (int i = 0; i < this.nbColors; i++) {
            this.detections[i * 2].computeColor();
            this.detections[i * 2 + 1].computeColor();
            savedColors[currentScreenPoint * 2][i] = detections[i * 2].getColor();
            savedColors[currentScreenPoint * 2 + 1][i] = detections[i * 2 + 1].getColor();
        }

        PMatrix3D loc = currentCamBoard();

        // MIDDLE translate !
        loc.translate(148.6f, 103.1f);
        PMatrix3D camPos = loc;
        PVector pos3D = new PVector(camPos.m03, camPos.m13, camPos.m23);
        PVector pxCam = cameraTracking.getProjectiveDevice().worldToPixelCoord(pos3D, false);
        PVector pt = new PVector(this.screenPoints[currentScreenPoint].x, this.screenPoints[currentScreenPoint].y);

        if (!seeThrough) {
            projectorView.addObjectImagePair(pxCam, pt);
        }
    }

    private PMatrix3D currentCamBoard() {
        return getMarkerBoard().getTransfoMat(cameraTracking).get();
    }

    void nextScreenshot() {
        pressedAmt = 0;

        if (currentScreenPoint == this.nbScreenPoints - 1) {
            currentScreenPoint = 0;
            System.out.println("Ended !");

            if (!seeThrough) {
                calibrate();
                isCalibratingToggle.show();
                showProjectionToggle.show();
                showTouchToggle.show();
                showProjectionToggle.setState(true);
                showTouchToggle.setState(true);
                isCalibratingToggle.setState(false);
                projectorView.clearObjectImagePairs();
            }
            calibrateColors();

        } else {
            currentScreenPoint++;
        }
    }

    // Loads of color data for color points. 
    // 1. Homography computation:  Camera - projector.
    // 2. Extract 4-6 projector images. 
    // 3. Find markers if these projector images +  find markers in Cam images. 
    // 4. Match 3D positions for extrinsic calibration and save it. 
    // 5. Check - Extrinsic calibration ? (How to do it easily?)  
    // 6. Compute color histograms, take the most commons, compute H,S,B + R,G,B  means + stdev. 
    // 7. Check the colors for color tracking across 4-6 screenshots. save the colors.
    void calibrate() {

        // Do homography here. 
        // Save homography
        Papart.getPapart().saveCalibration(Papart.cameraProjHomography,
                projectorView.getHomographyOf(cameraTracking).getHomography());

        ArrayList<ExtrinsicSnapshot> snapshots = new ArrayList<ExtrinsicSnapshot>();

        CameraThread projectorFakeThread = new CameraThread(projectorAsCamera);

        PMatrix3D tableCenter = savedLocations[0].get();
        tableCenter.translate(148.6f, 103.1f);

        Papart.getPapart().setTableLocation(tableCenter);

        // Homography is ready
        // we can get images from it. 
        for (int i = 0; i < nbScreenPoints; i++) {

            IplImage img = savedImages[i];
            // Set the image in the projector
            projectorAsCamera.grab(img);

            // Send to the thread, for pose estimation. 
            projectorFakeThread.setImage(projectorAsCamera.getIplImage());
            projectorFakeThread.setImage(projectorAsCamera.getIplImage());
            projectorFakeThread.setImage(projectorAsCamera.getIplImage());

            PMatrix3D projPos = getMarkerBoard().getTransfoMat(projectorAsCamera);

            // TODO: WARNING - IT requires 3 or 4 compute for it to work. 
            // NO IDEA WHY. 
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();

            // 5 Max and he found 8 !
//            DetectedMarker[] detectedMarkers = projectorAsCamera.getDetectedMarkers();
//            System.out.println("Number of markers found: " + detectedMarkers.length);
//            System.out.println("in (fake) Camera: " + projectorAsCamera);
            projPos = getMarkerBoard().getTransfoMat(projectorAsCamera);

//            opencv_imgcodecs.cvSaveImage("/home/jiii/tmp/cam-" + i + ".bmp", img);
//            opencv_imgcodecs.cvSaveImage("/home/jiii/tmp/proj-" + i + ".bmp", projectorAsCamera.getIplImage());
//            System.out.println("Saved " + "/home/jiii/tmp/cam-" + i + ".bmp");
//            System.out.println("Saved " + "/home/jiii/tmp/proj-" + i + ".bmp");
//            projPos.print();
            snapshots.add(new ExtrinsicSnapshot(savedLocations[i],
                    projPos, null));
        }

        ExtrinsicCalibrator calibrationExtrinsic = new ExtrinsicCalibrator(parent);
        calibrationExtrinsic.setProjector(projector);

        // Compute, save and set !...
        calibrationExtrinsic.computeProjectorCameraExtrinsics(snapshots);

        // Save average plane, and table touch !
        Plane sumPlanes = new Plane(new Vec3D(0, 0, 0),
                new Vec3D(0, 0, 0));
        PMatrix3D extr = depthCameraDevice.getStereoCalibrationInv().get();

        for (int i = 0; i < nbScreenPoints; i++) {

            // Re-compute the plane...
            PMatrix3D paperViewedByCam = savedLocations[i];
            paperViewedByCam.apply(extr);
            PMatrix3D paperViewedByDepth = paperViewedByCam;

            PlaneCalibration planeCalib
                    = PlaneCalibration.CreatePlaneCalibrationFrom(paperViewedByDepth,
                            //app.getLocation(),
                            new PVector(100, 100));
            Utils.sumPlane(sumPlanes, planeCalib.getPlane());
        }

        Utils.averagePlane(sumPlanes, 1f / nbScreenPoints);

        PlaneCalibration planeCalib = new PlaneCalibration();
        planeCalib.setPlane(sumPlanes);
        planeCalib.setHeight(10f); // NOT used anymore -> to remove.
        planeCalib.flipNormal();
        planeCalib.moveAlongNormal(zShift);

        this.planeProjCalib.setPlane(planeCalib);

        // Now the projection for screen-space.
        // planes from the camera perspective. 
        PlaneCalibration planeCalibCam = calibrationExtrinsic.computeAveragePlaneCam(snapshots);
        planeCalibCam.flipNormal();

        // identity - no external camera for ProCam calibration
        PMatrix3D depthCamExtrinsics = new PMatrix3D();
        // Depth -> Color calibration.
        depthCamExtrinsics.set(extr);

        HomographyCalibration homography = ExtrinsicCalibrator.computeScreenPaperIntersection(projector, planeCalibCam, depthCamExtrinsics);

        this.planeProjCalib.setHomography(homography);
        this.saveTouch();
        // Now projector-space homography. 

    }

    void calibrateColors() {
        // Save the color calibration 
        colorMode(RGB, 255);

        for (int colorId = 0; colorId < nbColors; colorId++) {
            float averageHue = 0;
            float averageSat = 0;
            float averageIntens = 0;

            float averageR = 0;
            float averageG = 0;
            float averageB = 0;

            float stdevHue = 0;
            float stdevSat = 0;
            float stdevIntens = 0;

            float stdevR = 0;
            float stdevG = 0;
            float stdevB = 0;

            // todo: IF Red > 180, hue gets shifted ?
            for (int i = 0; i < nbScreenPoints * 2; i++) {
                int c = this.savedColors[i][colorId];

                float r = this.red(c);
                averageHue += this.hue(c);
                averageSat += this.saturation(c);
                averageIntens += this.brightness(c);
                averageR += this.red(c);
                averageG += this.green(c);
                averageB += this.blue(c);
            }

            averageHue /= nbScreenPoints * 2;
            averageSat /= nbScreenPoints * 2;
            averageIntens /= nbScreenPoints * 2;
            averageR /= nbScreenPoints * 2;
            averageG /= nbScreenPoints * 2;
            averageB /= nbScreenPoints * 2;

            // potentially problematic hue for red
            // Solution, we shift it by 255 for low values.
            if (averageR > 180) {
                averageHue = 0;
                for (int i = 0; i < nbScreenPoints * 2; i++) {
                    int c = this.savedColors[i][colorId];
                    float h = this.hue(c);
                    if (h < 30) {
                        h = h + 255;
                    }
                    averageHue += h;
                }
                averageHue /= nbScreenPoints * 2;
//                if (averageHue > 255) {
//                    averageHue = averageHue - 255;
//                }
            }

            int averageCol = color(averageR, averageG, averageB);

            for (int i = 0; i < nbScreenPoints * 2; i++) {
                int c = this.savedColors[i][colorId];

                // high red intensity with a redish rue.
                if (averageR > 180 && this.hue(c) < 30) {
                    stdevHue += abs(this.hue(c) + 255 - averageHue);
                } else {
                    stdevHue += abs(this.hue(c) - averageHue);
                }
                stdevSat += abs(this.saturation(c) - averageSat);
                stdevIntens += abs(this.brightness(c) - averageIntens);
                stdevR += abs(this.red(c) - averageR);
                stdevG += abs(this.green(c) - averageG);
                stdevB += abs(this.blue(c) - averageB);
            }

            stdevHue /= nbScreenPoints * 2;
            stdevSat /= nbScreenPoints * 2;
            stdevIntens /= nbScreenPoints * 2;
            stdevR /= nbScreenPoints * 2;
            stdevG /= nbScreenPoints * 2;
            stdevB /= nbScreenPoints * 2;

            String words = "hue:" + Float.toString(stdevHue * 2) + " "
                    + "sat:" + Float.toString(stdevSat) + " "
                    + "intens:" + Float.toString(stdevIntens) + " "
                    + "erosion:" + Integer.toString(1) + " ";
            //	"red:"+  Float.toString(redThresh) + " "+
            //	"blue:"+  Float.toString(blueThresh) + " "+
            //	"col:"+  Integer.toString(redColor);

            String saveFile = Papart.colorThresholds + colorId + ".txt";
            words = words + "id:" + Integer.toString(colorId) + "\n";
            words = words + "col:" + Integer.toString(averageCol);

            String[] list = split(words, ' ');
            parent.saveStrings(saveFile, list);

        }

    }

    public void stopCalib() {
        if (active) {
            // stop rendering.
//            mainDisplay.removePaperScreen(this);
            active = false;

            ((ARDisplay) getDisplay()).setCalibrationMode(false);
            System.out.println("Stopping multi-calib...");
        }
    }

    public void startCalib() {
        // start rendering again.
//        if (!mainDisplay.paperScreens.contains(this)) {
//            mainDisplay.addPaperScreen(this);
//        }
        active = true;
        ((ARDisplay) getDisplay()).setCalibrationMode(true);

        System.out.println("Starting multi-calib...");
    }

    public boolean isActive() {
        return active;
    }

    private static Skatolo skatolo;
    private static Button button;

    // Button activated
    public boolean showProjection, showTouch, doCalibration;
    public Toggle showProjectionToggle, showTouchToggle, isCalibratingToggle;

    public static void drawCalibration(PGraphicsOpenGL screenGraphics) {
        Papart papart = Papart.getPapart();
        MultiCalibrator multiCalibrator = papart.multiCalibrator;
        PApplet parent = multiCalibrator.parent;
        PGraphicsOpenGL g = (PGraphicsOpenGL) parent.g;

        if (!multiCalibrator.isActive()) {
            System.out.println("ERROR: cannot calibrate with inactive calibrator.");
            return;
        }

        if (skatolo == null) {
            initGUI(parent, g, multiCalibrator);
        }

        g.clear();

        drawAR(parent, g, multiCalibrator, INVALID_VECTOR);

        drawFrame(parent, g, multiCalibrator);

        // number of valid 
        PVector point = multiCalibrator.screenPoints[multiCalibrator.currentScreenPoint];

        drawProgress(g, multiCalibrator, point);

        if (multiCalibrator.doCalibration) {
            PVector pt = multiCalibrator.screenPoints[multiCalibrator.currentScreenPoint];
            g.pushMatrix();
            g.translate(pt.x, pt.y);
            g.rotate(pt.z);
            drawTarget(g, multiCalibrator);
            drawHints(g, multiCalibrator, pt);
            g.popMatrix();
        }

        if (multiCalibrator.showTouch) {
            drawTouchCalibrated(g, multiCalibrator);
        }

        skatolo.draw(g);
    }

    public static void initGUI(PApplet parent, PGraphicsOpenGL g, MultiCalibrator multiCalibrator) {
        skatolo = new Skatolo(parent, multiCalibrator);
//            button = skatolo.addButton("button")
//                    .setPosition(200, 100)
//                    .setSize(50, 20);
        skatolo.addButton("undo")
                .setPosition(240, 70)
                .setSize(50, 20);
        skatolo.addButton("resetCurrentPoint")
                .setPosition(300, 70)
                .setLabel("reset")
                .setSize(50, 20);

        multiCalibrator.isCalibratingToggle = skatolo.addToggle("doCalibration")
                .setPosition(380, 70)
                .setSize(50, 20);
        multiCalibrator.showProjectionToggle = skatolo.addToggle("showProjection")
                .setPosition(380, 120)
                .setSize(50, 20);
        multiCalibrator.showTouchToggle = skatolo.addToggle("showTouch")
                .setPosition(380, 170)
                .setSize(50, 20);
        multiCalibrator.isCalibratingToggle.hide();
//            multiCalibrator.showProjectionToggle.hide();
//            multiCalibrator.showTouchToggle.hide();

        // calibration displayed
        multiCalibrator.isCalibratingToggle.setState(true);
        skatolo.setAutoDraw(false);

    }

    public static void drawTarget(PGraphicsOpenGL g, MultiCalibrator multiCalibrator) {
        g.noFill();
        g.stroke(255);
        g.ellipseMode(CENTER);

        // PIXEL sizes. (projector resolution dependent)
        g.ellipse(0, 0, 50, 50);
        g.line(-5, 0, 5, 0);
        g.line(0, -5, 0, 5);

//        g.stroke(200, 0, 200);
//        g.rect(-5, 0, 10, 1);
//        g.rect(0, - 5, 1, 10);
        g.ellipse(0, 0, 20, 20);

        int w = 200;
        int h = 150;
        g.rect(-w, -h, w * 2, h * 2);

    }

    public static void drawFrame(PApplet parent, PGraphicsOpenGL g,
            MultiCalibrator multiCalibrator) {

        g.rectMode(CORNER);
        // GlobalFrame
        g.noFill();
        g.strokeWeight(2f);
        g.stroke(255f);
        g.rect(0, 0, parent.width, parent.height);
    }

    public static void drawAR(PApplet parent, PGraphicsOpenGL g,
            MultiCalibrator multiCalibrator, PVector pt) {

        if (multiCalibrator.getDisplay() instanceof ProjectorDisplay) {
            ProjectorDisplay projector = (ProjectorDisplay) multiCalibrator.getDisplay();
//            display = projector;
            projector.drawScreensOver();
            parent.noStroke();
            DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                    projector.render(),
                    0, 0, projector.getWidth(), projector.getHeight());
        } else {
            // AR rendering, for touch and color tracking (and debug). 
            if (multiCalibrator.getDisplay() instanceof ARDisplay) {
                ARDisplay display = (ARDisplay) multiCalibrator.getDisplay();
                display.drawScreensOver();
                parent.noStroke();
                PImage img = multiCalibrator.getCameraTracking().getPImage();
                if (multiCalibrator.getCameraTracking() != null && img != null) {
                    parent.image(img, 0, 0, parent.width, parent.height);
//            ((PGraphicsOpenGL) (parent.g)).image(camera.getPImage(), 0, 0, frameWidth, frameHeight);
                }

                // TODO: Distorsion problems with higher image space distorisions (useless ?)
                DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                        display.render(),
                        0, 0, parent.width, parent.height);
            }
        }

    }

    public static void drawProgress(PGraphicsOpenGL g,
            MultiCalibrator multiCalibrator, PVector pt) {
        g.pushMatrix();
        g.translate(120, 150);
        g.fill(255);  // necessary for text
        g.text("Photos: ", 0, 8);
//        g.text("Markers: ", 150, 200 + 8);
        g.strokeWeight(1);
        g.stroke(255);

        for (int i = 0; i < multiCalibrator.nbScreenPoints; i++) {
            if (i < multiCalibrator.currentScreenPoint) {
                g.fill(0, 255, 0); // green
            } else {
                if (i == multiCalibrator.currentScreenPoint) {
                    g.fill(242, 193, 48); // orange
                } else {
                    g.fill(66, 66, 66); // nothing
                }
            }
            g.rect(50 + i * 20, 0, 20, 20);
        }
        g.popMatrix();

        // Color captures
        int[][] savedColors = multiCalibrator.savedColors;
        g.noStroke();
        int rectSize = 15;
        for (int i = 0; i < multiCalibrator.nbScreenPoints; i++) {

            for (int j = 0; j < multiCalibrator.nbColors; j++) {
                g.fill(savedColors[i * 2][j]);
                g.rect(i * rectSize, j * 2 * rectSize, rectSize, rectSize);

                g.fill(savedColors[i * 2 + 1][j]);
                g.rect(i * rectSize, (j * 2 + 1) * rectSize, rectSize, rectSize);
            }
        }

    }

    public static void drawHints(PGraphicsOpenGL g,
            MultiCalibrator multiCalibrator, PVector pt) {

        Papart papart = Papart.getPapart();
        PApplet parent = multiCalibrator.parent;
        BaseDisplay display = multiCalibrator.getDisplay();

//        float freq = 0.2f + 2f * ((float) multiCalibrator.pressedAmt / (float) multiCalibrator.maxPressedAmt);
//        float freq = 0.5f;
//        if (multiCalibrator.pressedAmt > 3f) {
//            freq = 2f;
//        }
        if (multiCalibrator.waitForMovement) {
            g.fill(200, 0, 0);
        } else {
            float d = multiCalibrator.getMarkerBoard().lastMovementDistance(multiCalibrator.getCameraTracking());
            if (d < multiCalibrator.maxMovement) {
                g.fill(180);
                if (multiCalibrator.pressedAmt > 3f) {
                    g.fill(255f);
                }
            } else {
                g.fill(60);
            }
        }
//        float v = (PApplet.sin((parent.millis() / 1000f) * PConstants.TWO_PI * freq) + 1f) / 2f;
//        g.fill(255 * v);

        int dx = 60;
        int dy = 70;
        int h = 50;
//        g.rect(-w, -h, w * 2, h * 2);

        g.rect(-dx, -dy - h, dx * 2, h);
        g.rect(-dx, dy, dx * 2, h);

        for (int i = 0; i < 8; i++) {
            if (i < papart.getMarkerList().length) {
                g.fill(129, 247, 156); // green
            } else {
                g.fill(247, 206, 56); //orange
            }
            g.rect(-dx + i * 16, dy + 15, 15, 15);
        }

//        g.rect(-50, 100, 150, 80);
//        g.rect(-50, -100, 150, 80);
//        float d = multiCalibrator.getMarkerBoard().lastMovementDistance(multiCalibrator.getCameraTracking());
//        g.text(d, 100, 100);
//        if (pt.y < display.getHeight() - 180) {
//            g.translate(0, 150);
//        } else {
//            g.translate(0, -150);
//        }
        // Not moving, draw something.
//        if (d < 2f) {
//            g.fill(0, 255, 0);
//            g.fill(255);
//            g.text("Ne bougez plus la feuille.", 0, 0); //stillW + 10);
//        }
    }

    public static void drawTouchCalibrated(PGraphicsOpenGL g,
            MultiCalibrator multiCalibrator) {
        Papart papart = Papart.getPapart();
        PApplet parent = multiCalibrator.parent;
        DepthTouchInput touchInput = (DepthTouchInput) papart.getTouchInput();
        if (!touchInput.isReady()) {
            return;
        }

        // Get a copy, as the arrayList is constantly modified
        ArrayList<TrackedDepthPoint> touchs2D = new ArrayList<TrackedDepthPoint>(touchInput.getTrackedDepthPoints2D());
        for (TrackedDepthPoint tp : touchs2D) {

            ArrayList<DepthDataElementProjected> depthDataElements = tp.getDepthDataElements();
            for (DepthDataElementProjected dde : depthDataElements) {
                Vec3D v = dde.projectedPoint;
                g.noStroke();
                setColor(g, dde.pointColor, 255);
                g.ellipse(v.x * parent.width,
                        v.y * parent.height,
                        10, 10);
            }

            g.fill(50, 50, 255);
            PVector pos = tp.getPosition();
            g.ellipse(pos.x * parent.width,
                    pos.y * parent.height, 20, 20);
        }

        g.fill(255, 0, 0);
        ArrayList<TrackedDepthPoint> touchs3D = new ArrayList<TrackedDepthPoint>(touchInput.getTrackedDepthPoints3D());
        for (TrackedDepthPoint tp : touchs3D) {

            ArrayList<DepthDataElementProjected> depthDataElements = tp.getDepthDataElements();

            for (DepthDataElementProjected dde : depthDataElements) {
                Vec3D v = dde.projectedPoint;
                g.noStroke();
                setColor(g, dde.pointColor, 100);

                g.ellipse(v.x * parent.width,
                        v.y * parent.height,
                        10, 10);
            }

            PVector pos = tp.getPosition();
            g.ellipse(pos.x * parent.width,
                    pos.y * parent.height, 40, 40);
        }
    }

    static void setColor(PGraphicsOpenGL graphics, int rgb, float intens) {
        int r = (rgb >> 16) & 0xFF;  // Faster way of getting red(argb)
        int g = (rgb >> 8) & 0xFF;   // Faster way of getting green(argb)
        int b = rgb & 0xFF;          // Faster way of getting blue(argb)
        graphics.fill(r, g, b, intens);
    }

    // Pixel rendering
    public static void sin(PApplet parent, PGraphics g, int amt, float freq, int xDiff, float size) {
        float v = (PApplet.sin((float) (parent.millis()) / 1000f * PConstants.TWO_PI * freq) + 1f) / 2f;
        g.noStroke();
        g.ellipseMode(CENTER);
        g.fill(v * amt);
        g.ellipse(-xDiff, 0, size, size);
        g.ellipse(0, 0, size, size);
        g.ellipse(xDiff, 0, size, size);
    }

    /////////////////
    ////////// Touch calibration
    /////////////////
    void computeTouch() {
        planeProjCalib = getPlaneFromPaperViewedByDepth();
//        depthTouchInput.setPlaneAndProjCalibration(planeProjCalib);
    }

    void saveTouch() {
        planeProjCalib.saveTo(parent, Papart.planeAndProjectionCalib);
//        touchInput.setPlaneAndProjCalibration(planeProjCalib);
        System.out.println("Calibration saved !");
        toSave = false;
    }

    /**
     * Get the 3D plane from the depth Camera point of view.
     *
     * @return
     */
    PlaneAndProjectionCalibration getPlaneFromPaperViewedByDepth() {
        PlaneAndProjectionCalibration planeProjCalib = new PlaneAndProjectionCalibration();

        PMatrix3D paperViewedByCam = this.getLocation().get();
        PMatrix3D extr = depthCameraDevice.getStereoCalibrationInv().get();
        paperViewedByCam.apply(extr);
        PMatrix3D paperViewedByDepth = paperViewedByCam;

        planeProjCalib.setPlane(getTouchPlane(paperViewedByDepth));
        planeProjCalib.setHomography(findTouchHomography(paperViewedByDepth));

        return planeProjCalib;
    }

    private PlaneCalibration getTouchPlane(PMatrix3D paperViewedByDepth) {
        PlaneCalibration planeCalib
                = PlaneCalibration.CreatePlaneCalibrationFrom(paperViewedByDepth,
                        //app.getLocation(),
                        new PVector(100, 100));
        planeCalib.flipNormal();

        // Y shift here. 
        planeCalib.moveAlongNormal(zShift);

        return planeCalib;
    }

    private HomographyCalibration findTouchHomography(PMatrix3D paperViewedByDepth) {
        PVector[] corners = new PVector[4];

        // 3D points of the PaperScreen.  (object)
        corners[0] = new PVector(paperViewedByDepth.m03, paperViewedByDepth.m13, paperViewedByDepth.m23);
        paperViewedByDepth.translate(drawingSize.x, 0, 0);
        corners[1] = new PVector(paperViewedByDepth.m03, paperViewedByDepth.m13, paperViewedByDepth.m23);
        paperViewedByDepth.translate(0, drawingSize.y, 0);
        corners[2] = new PVector(paperViewedByDepth.m03, paperViewedByDepth.m13, paperViewedByDepth.m23);
        paperViewedByDepth.translate(-drawingSize.x, 0, 0);
        corners[3] = new PVector(paperViewedByDepth.m03, paperViewedByDepth.m13, paperViewedByDepth.m23);

        // Image points
        PVector originDst = new PVector();
        PVector xAxisDst = new PVector(1, 0);
        PVector cornerDst = new PVector(1, 1);
        PVector yAxisDst = new PVector(0, 1);

        HomographyCreator homographyCreator = new HomographyCreator(3, 3, 4);

        homographyCreator.addPoint(corners[0], originDst);
        homographyCreator.addPoint(corners[1], xAxisDst);
        homographyCreator.addPoint(corners[2], cornerDst);
        boolean success = homographyCreator.addPoint(corners[3], yAxisDst);
        if (success) {
            // Yayah
            return homographyCreator.getHomography();
        } else {
            // System.err.println("Error in computing the 3D homography");
            return HomographyCalibration.INVALID;
        }
    }

    PVector cameraPointsBot[] = new PVector[0];
    PVector cameraPointsTop[] = new PVector[0];

    private void drawDebugZones() {
        noStroke();
        // draw a green rectangle
        // top projection
        rect(75f, 11f, 146f, 50f);

        // bot projection 
        rect(75f, 145f, 146f, 50f);

        // green circles
        fill(0, 255, 0);
        rect(79.8f, 123.8f, 15f, 15f);
        rect(208.2f, 123.8f, 15f, 15f);

        // purple circles
        fill(153, 0, 204);
        rect(108.1f, 123.8f, 15f, 15f);
        rect(179.4f, 123.8f, 15f, 15f);

        // red circles
        // 2 color trackers ici
        fill(255, 0, 0);
        rect(79.8f, 95.2f, 15f, 15f);
        rect(208.2f, 95.2f, 15f, 15f);

        // blue circles
        fill(0, 0, 255);
        rect(79.8f, 67.1f, 15f, 15f);
        rect(208.2f, 67.1f, 15f, 15f);

        // orange circles
        fill(255, 200, 30);
        rect(108.1f, 67.1f, 15f, 15f);
        rect(179.4f, 67.1f, 15f, 15f);
    }

}
