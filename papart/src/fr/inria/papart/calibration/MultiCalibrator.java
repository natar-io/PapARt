/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.multitouch.SkatoloLink;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.detection.BlinkTracker;
import fr.inria.papart.multitouch.detection.ColorTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
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
import static processing.core.PConstants.LEFT;
import static processing.core.PConstants.RIGHT;

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

    public float zShift = 10f;

    private Skatolo skatolo;
    private HoverButton hoverButton, resetButton;
    int pressedAmt = 0;
//    int maxPressedAmt = 30 * 3; // 2 secs
    int maxPressedAmt = 20; // 2 secs

    int pressedAmtReset = 0;

    // 6 points for Homography matching. 
    public PVector screenPoints[];
    public int currentScreenPoint = 0;
    int nbScreenPoints = 8;

    IplImage savedImages[];
    PMatrix3D savedLocations[];
    private TrackedView projectorView;
    private ProjectorAsCamera projectorAsCamera;
    public boolean evaluateMode = false;

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
    }

    @Override
    public void setup() {
        try {
            setDrawingFilter(0);
            setTrackingFilter(0, 0);

            savedImages = new IplImage[nbScreenPoints];
            savedLocations = new PMatrix3D[nbScreenPoints];

            initButtons();
            initScreenPoints();
            initProjectorAsCamera();
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

    void initButtons() {
        skatolo = new Skatolo(this.parent, this);
        skatolo.getMousePointer().disable();
        skatolo.setAutoDraw(false);
        int sizeAdd = 10;
        hoverButton = skatolo.addHoverButton("button")
                .setPosition(79.8f - sizeAdd, 123.8f - sizeAdd)
                .setSize(15 + 2 * sizeAdd, 15 + 2 * sizeAdd);
        resetButton = skatolo.addHoverButton("resetPoint")
                .setPosition(208.2f - sizeAdd, 123.8f - sizeAdd)
                .setSize(15 + 2 * sizeAdd, 15 + 2 * sizeAdd);
    }

    private int deadZone = 100;
    int pw, ph;

    void initScreenPoints() {
        screenPoints = new PVector[nbScreenPoints];
        pw = this.getDisplay().getWidth();
        ph = this.getDisplay().getHeight();
        // 1 in each quadrant. 
        // 2 random
        screenPoints[0] = new PVector(
                parent.random(deadZone, pw / 2),
                parent.random(deadZone, ph / 2),
                parent.random(HALF_PI));
        screenPoints[1] = new PVector(
                parent.random(pw / 2, pw - deadZone),
                parent.random(deadZone, ph / 2),
                parent.random(HALF_PI));

        screenPoints[2] = new PVector(
                parent.random(deadZone, pw / 2),
                parent.random(ph / 2, ph - deadZone),
                parent.random(HALF_PI));
        screenPoints[3] = new PVector(
                parent.random(pw / 2, pw - deadZone),
                parent.random(ph / 2, ph - deadZone),
                parent.random(HALF_PI));

        for (int i = 4; i < nbScreenPoints; i++) {
            screenPoints[i] = createRandomPoint();
        }
    }

    private PVector createRandomPoint() {
        return new PVector(
                parent.random(deadZone, pw - deadZone),
                parent.random(deadZone, ph - deadZone),
                parent.random(HALF_PI));
    }

    private void initProjectorAsCamera() {

        if (!(getDisplay() instanceof ProjectorDisplay)) {
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

    @Override
    public void drawOnPaper() {
        // setLocation(63, 45, 0);
//      DetectedMarker[] list = papart.getMarkerList();
//      println("markers :  " + list.length);

        // background: blue
        background(0, 0, 200, 50);

        if (active) {
//            System.out.println("Framerate: " + parent.frameRate);
            computeTouch();
            if (toSave) {
                saveTouch();
            }
            drawTouch(10);

            float d = getMarkerBoard().lastMovementDistance(getCameraTracking());

            if (waitForMovement && d > 20) {
                waitForMovement = false;
            }

            SkatoloLink.updateTouch(touchList.get2DTouchs(), skatolo);
            skatolo.draw(getGraphics());

            if (evaluateMode) {
                drawDebugZones();
            }

//            PMatrix3D camPos = currentCamBoard();
//            PVector pos3D = new PVector(camPos.m03, camPos.m13, camPos.m23);
//            PVector pxCam = cameraTracking.getProjectiveDevice().worldToPixelCoord(pos3D, false);
//            System.out.println("PXCam: " + pxCam);
            // Not moving, draw something.
            if (!waitForMovement && d < 8f) {
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
            System.out.println("Saving location: ");
            getLocation().print();
            this.hoverButton.isActive = false;
        }
    }

    void cancel() {
        pressedAmtReset++;
        if (pressedAmtReset == maxPressedAmt) {
            resetCurrentPoint();
            pressedAmtReset = 0;
            this.resetButton.isActive = false;
        }
    }

    void resetCurrentPoint() {
        this.screenPoints[currentScreenPoint] = createRandomPoint();
    }

    // Move the piece of paper between poses
    boolean waitForMovement = true;

    void savePicture() {
        this.savedImages[currentScreenPoint] = cameraTracking.getIplImage().clone();
        this.savedLocations[currentScreenPoint] = currentCamBoard();

        PMatrix3D loc = currentCamBoard();

        // MIDDLE translate !
//        loc.translate(179.4f, 67.1f);
        loc.translate(148f, 103.1f);
        PMatrix3D camPos = loc;
        PVector pos3D = new PVector(camPos.m03, camPos.m13, camPos.m23);
        PVector pxCam = cameraTracking.getProjectiveDevice().worldToPixelCoord(pos3D, false);

        PVector pt = new PVector(this.screenPoints[currentScreenPoint].x, this.screenPoints[currentScreenPoint].y);
        System.out.println("adding pair: " + pxCam + " " + pt);

        projectorView.addObjectImagePair(pxCam, pt);
    }

    private PMatrix3D currentCamBoard() {
        return getMarkerBoard().getTransfoMat(cameraTracking).get();
    }

    void nextScreenshot() {
        pressedAmt = 0;
        currentScreenPoint++;
        if (currentScreenPoint == this.nbScreenPoints) {
            System.out.println("Ended !");
            calibrate();

            projectorView.clearObjectImagePairs();
            currentScreenPoint = 0;
            this.evaluateMode = true;
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

            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();
            projectorFakeThread.compute();

            // 5 Max and he found 8 !
            DetectedMarker[] detectedMarkers = projectorAsCamera.getDetectedMarkers();
            System.out.println("Number of markers found: " + detectedMarkers.length);
            System.out.println("in (fake) Camera: " + projectorAsCamera);
            projPos = getMarkerBoard().getTransfoMat(projectorAsCamera);
            projPos.print();
            opencv_imgcodecs.cvSaveImage("/home/jiii/tmp/cam-" + i + ".bmp", img);
            opencv_imgcodecs.cvSaveImage("/home/jiii/tmp/proj-" + i + ".bmp", projectorAsCamera.getIplImage());
            System.out.println("Saved " + "/home/jiii/tmp/cam-" + i + ".bmp");
            System.out.println("Saved " + "/home/jiii/tmp/proj-" + i + ".bmp");
            System.out.println("Location found: ");

            snapshots.add(new ExtrinsicSnapshot(savedLocations[i],
                    projPos, null));
        }

        ExtrinsicCalibrator calibrationExtrinsic = new ExtrinsicCalibrator(parent);
        calibrationExtrinsic.setProjector(projector);

        // Compute, save and set !...
        calibrationExtrinsic.computeProjectorCameraExtrinsics(snapshots);

        // Get average of 3D planes and set the table location !?
//        calibrationExtrinsic.calibrateKinect(snapshots, useExternalColorCamera);
//        projectorView.getHomographyOf(cameraTracking).saveTo(this, Papart.cameraProjHomography);       
        // Set it !
    }

    PlaneAndProjectionCalibration planeProjCalib;
    public boolean toSave = false;

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

    public static void drawCalibration(PGraphicsOpenGL screenGraphics) {
        Papart papart = Papart.getPapart();
        MultiCalibrator multiCalibrator = papart.multiCalibrator;
        PApplet parent = multiCalibrator.parent;
        PGraphicsOpenGL g = (PGraphicsOpenGL) parent.g;
        // Classical rendering (if needed... )

        if (!multiCalibrator.isActive()) {
            System.out.println("ERROR: cannot calibrate with inactive calibrator.");
            return;
        }

        parent.g.clear();

        ARDisplay display = null;
        boolean seeThrough = false;

        if (multiCalibrator.getDisplay() instanceof ProjectorDisplay) {
            ProjectorDisplay projector = (ProjectorDisplay) multiCalibrator.getDisplay();
            display = projector;
            projector.drawScreensOver();
            parent.noStroke();
            DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                    projector.render(),
                    0, 0, projector.getWidth(), projector.getHeight());

            // TODO:
//            g = projector.getGraphics();
        } else {
            // AR rendering, for touch and color tracking (and debug). 
            if (multiCalibrator.getDisplay() instanceof ARDisplay) {
                display = (ARDisplay) multiCalibrator.getDisplay();
                display.drawScreensOver();
                seeThrough = true;
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

        // Both display modes for now. 
        if (parent.mousePressed) {
            //   mouseClick.set(parent.mouseX, parent.mouseY);
        }

//        if (multiCalibrator.projectorView.getNbPairs() >= 4) {
//            PImage img = multiCalibrator.projectorView.getViewOf(multiCalibrator.cameraTracking);
//            if (img != null) {
//                g.image(img,
//                        400, 400,
//                        400, 400);
//            }
//        }
        PVector pt = multiCalibrator.screenPoints[multiCalibrator.currentScreenPoint];

        g.pushMatrix();
        g.translate(pt.x, pt.y);
        g.rotate(pt.z);
        drawTarget(g, multiCalibrator);
        drawHints(g, multiCalibrator, pt);

        g.popMatrix();
    }

    public static void drawTarget(PGraphicsOpenGL g, MultiCalibrator multiCalibrator) {
        g.noFill();
        g.stroke(255);
        g.ellipseMode(CENTER);

        // PIXEL sizes. (projector resolution dependent)
        g.ellipse(0, 0, 50, 50);
        g.rect(-5, 0, 10, 1);
        g.rect(0, - 5, 1, 10);
        g.ellipse(0, 0, 20, 20);

    }

    public static void drawHints(PGraphicsOpenGL g,
            MultiCalibrator multiCalibrator, PVector pt) {

        Papart papart = Papart.getPapart();
        PApplet parent = multiCalibrator.parent;
        BaseDisplay display = multiCalibrator.getDisplay();

//        float freq = 0.2f + 2f * ((float) multiCalibrator.pressedAmt / (float) multiCalibrator.maxPressedAmt);
        float freq = 0.5f;

        if (multiCalibrator.pressedAmt > 3f) {
            freq = 2f;
        }
        float v = (PApplet.sin((parent.millis() / 1000f) * PConstants.TWO_PI * freq) + 1f) / 2f;

//        if (multiCalibrator.hoverButton.isActive) {
//            g.stroke(0, 255, 0);
//        }
//        if (multiCalibrator.resetButton.isActive) {
//            g.stroke(255, 0, 0);
//        }
        g.fill(255 * v);

        for (int i = 0; i < papart.getMarkerList().length; i++) {
            g.rect(10 * i, 40, 20, 20);
        }

        g.rect(-50, 100, 100, 20);
        g.rect(-50, -100, 100, 20);

        float d = multiCalibrator.getMarkerBoard().lastMovementDistance(multiCalibrator.getCameraTracking());
//        g.text(d, 100, 100);

        if (pt.y < display.getHeight() - 180) {
            g.translate(0, 150);
        } else {
            g.translate(0, -150);
        }
        // Not moving, draw something.
        if (d < 2f) {
            g.fill(0, 255, 0);
            g.fill(255);
            g.text("Ne bougez plus la feuille.", 0, 0); //stillW + 10);
        }

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
        depthTouchInput.setPlaneAndProjCalibration(planeProjCalib);
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
        rect(76f, 11.6f, 146.4f, 46.4f);

        // bot projection 
        rect(80f, 145f, 142.7f, 50f);

        // green circles
        fill(0, 255, 0);
        rect(79.8f, 123.8f, 15f, 15f);
        rect(208.2f, 123.8f, 15f, 15f);

        // purple circles
        fill(153, 0, 204);
        rect(108.1f, 123.8f, 15f, 15f);
        rect(179.4f, 123.8f, 15f, 15f);

        // red circles
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
