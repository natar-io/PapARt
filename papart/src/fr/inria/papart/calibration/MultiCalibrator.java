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
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.detection.BlinkTracker;
import fr.inria.papart.multitouch.detection.ColorTracker;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.TrackedView;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.DrawUtils;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import static processing.core.PConstants.CENTER;
import static processing.core.PConstants.RGB;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
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
    protected boolean active = false;

    BlinkTracker blinkTrackerTop, blinkTrackerBot;
    private Papart papart;
    int capW, capH;

    float freqToFind = 4.5f;
    public float zShift = 22f;
    // debug
    byte[] found = null;
    private DepthCameraDevice depthCameraDevice;

    @Override
    public void settings() {
        papart = Papart.getPapart();
        try {
            // the size of the draw area is 297mm x 210mm.
            setDrawingSize(297, 210);

            // loads the marker that are actually printed and tracked by the camera.
            loadMarkerBoard(Papart.markerFolder + "calib1.svg", 297, 210);

            System.out.print("Markerboard after init: " + this.getMarkerBoard());

            // the application will render drawings and shapes only on the surface of the sheet of paper.
            setDrawOnPaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setup() {
        try {
            setDrawingFilter(0);
            setTrackingFilter(0, 0);

            capW = 128;
            capH = 32;
            // Quality to find !
            // Start with max...
            blinkTrackerBot = papart.initXTracking(this, 1f, freqToFind); // 0.5f);
            TrackedView trackedViewBot = blinkTrackerBot.getTrackedView();
            trackedViewBot.setTopLeftCorner(new PVector(80, 145));

            // Size ?!
            trackedViewBot.setCaptureSizeMM(new PVector(142f, 50f));
            trackedViewBot.setImageWidthPx(capW);
            trackedViewBot.setImageHeightPx(capH);
            trackedViewBot.init();
            blinkTrackerBot.initTouchDetection();

            blinkTrackerTop = papart.initXTracking(this, 1f, freqToFind); // 0.5f);
            TrackedView trackedViewTop = blinkTrackerTop.getTrackedView();
            trackedViewTop.setTopLeftCorner(new PVector(76, 11.6f));

            trackedViewTop.setCaptureSizeMM(new PVector(146.6f, 46.4f));
            trackedViewTop.setImageWidthPx(capW);
            trackedViewTop.setImageHeightPx(capH);
            trackedViewTop.init();

            blinkTrackerTop.initTouchDetection();

            depthTouchInput = (DepthTouchInput) papart.getTouchInput();
            depthTouchInput.useRawDepth();
            depthCameraDevice = papart.getDepthCameraDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
        startCalib();
    }

    DepthTouchInput depthTouchInput;

    @Override
    public void drawOnPaper() {
        // setLocation(63, 45, 0);
//      DetectedMarker[] list = papart.getMarkerList();
//      println("markers :  " + list.length);

        // background: blue
        background(0, 0, 200, 50);

        if (active) {
            System.out.println("Framerate: " + parent.frameRate);
            computeTouch();
            if (toSave) {
                saveTouch();
            }
            drawTouch(10);

            float d = getMarkerBoard().lastMovementDistance(getCameraTracking());
//        g.text(d, 100, 100);

            // Not moving, draw something.
            if (d < 8f) {

                ArrayList<TrackedElement> teBot = blinkTrackerBot.findColor(parent.millis());
                TouchList touchsBot = blinkTrackerBot.getTouchList();
                found = blinkTrackerBot.getLastFoundArray();
                System.out.println("tracked bot: " + teBot.size());

                ArrayList<TrackedElement> teTop = blinkTrackerTop.findColor(parent.millis());
                TouchList touchsTop = blinkTrackerTop.getTouchList();
                System.out.println("tracked top: " + teTop.size());

//            // 3 Found !
                if (teBot.size() >= 0) {
                    cameraPointsBot = new PVector[touchsBot.size()];
                    int k = 0;
                    for (Touch touch : touchsBot) {
                        // Position in 2D image space.
                        PVector positionPxPaper = touch.position;
                        PVector positionMM = blinkTrackerBot.getTrackedView().pixelsToMM(positionPxPaper);
                        fill(255, 0, 255f);
                        ellipseMode(CENTER);
                        ellipse(positionMM.x, positionMM.y, 10, 10);
                        // Get the camera pixel value. 
                        cameraPointsBot[k++] = this.computePxPosition(positionMM);
//                        System.out.println("Cam pos " + cameraPointsBot[k-1]); 
                    }
                }
                if (teTop.size() >= 0) {
                    cameraPointsTop = new PVector[touchsTop.size()];
                    int k = 0;
                    for (Touch touch : touchsTop) {
                        // Position in 2D image space.
                        PVector positionPxPaper = touch.position;
                        PVector positionMM = blinkTrackerTop.getTrackedView().pixelsToMM(positionPxPaper);
                        fill(255, 0, 255f);
                        ellipseMode(CENTER);
                        ellipse(positionMM.x, positionMM.y, 10, 10);
                        // Get the camera pixel value. 
                        cameraPointsTop[k++] = this.computePxPosition(positionMM);
//                        System.out.println("Cam pos " + cameraPointsTop[k-1]); 
                    }
                }

            } else {
                System.out.println("Reset Img.");
                blinkTrackerBot.resetImages();
            }
//            ArrayList<TrackedElement> teTop = blinkTrackerTop.findColor(parent.millis());
//            TouchList touchsTop = blinkTrackerTop.getTouchList();
//            if (getDisplay() instanceof ARDisplay) {
//                drawDebugZones();
//            }
        }

    }

    PlaneAndProjectionCalibration planeProjCalib;
    public boolean toSave = false;

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

        planeProjCalib.setPlane(getPlane(paperViewedByDepth));
        planeProjCalib.setHomography(findHomography(paperViewedByDepth));

        return planeProjCalib;
    }

    private PlaneCalibration getPlane(PMatrix3D paperViewedByDepth) {

        PlaneCalibration planeCalib
                = PlaneCalibration.CreatePlaneCalibrationFrom(paperViewedByDepth,
                        //app.getLocation(),
                        new PVector(100, 100));
        planeCalib.flipNormal();

        // Y shift here. 
        planeCalib.moveAlongNormal(zShift);

        return planeCalib;
    }



    private HomographyCalibration findHomography(PMatrix3D paperViewedByDepth) {
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

    PVector[] cameraPointsBot = new PVector[0];
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

    private static PVector mouseClick = new PVector(0, 0);

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
            mouseClick.set(parent.mouseX, parent.mouseY);
        }

        g.noFill();
        g.stroke(255);
        g.ellipse(mouseClick.x, mouseClick.y, 50, 50);
        g.rect(mouseClick.x - 5, mouseClick.y, 10, 1);
        g.rect(mouseClick.x, mouseClick.y - 5, 1, 10);
        g.ellipse(mouseClick.x, mouseClick.y, 20, 20);

        float d = multiCalibrator.getMarkerBoard().lastMovementDistance(multiCalibrator.getCameraTracking());
        g.text(d, 100, 100);

        // Not moving, draw something.
        if (d < 2f) {
            int stillW = 25;
            g.fill(0, 255, 0);
            g.rect(display.getWidth() / 2 - stillW, 80,
                    stillW, stillW);
            g.fill(255);
            g.text("Ne bougez plus la feuille.", display.getWidth() / 2, 100); //stillW + 10);
        }

        PImage img = multiCalibrator.blinkTrackerBot.getTrackedImage();

        if (img != null) {
            g.image(img, 200, 200, 100, 40);
        }

        if (seeThrough) {
            // Camera test 
            for (PVector v : multiCalibrator.cameraPointsBot) {
                g.fill(200, 180, 130);
                if (v != null) {
                    g.ellipse(v.x, v.y, 3, 3);
                }
            }
            for (PVector v : multiCalibrator.cameraPointsTop) {
                g.fill(200, 130, 180);
                if (v != null) {
                    g.ellipse(v.x, v.y, 3, 3);
                }
            }
        }

        // Middle.. 
        PVector mid = new PVector();
        if (multiCalibrator.cameraPointsBot.length == 3 && multiCalibrator.cameraPointsTop.length == 3) {
            for (PVector v : multiCalibrator.cameraPointsBot) {
                mid.add(v);
            }
            for (PVector v : multiCalibrator.cameraPointsTop) {
                mid.add(v);
            }

            mid.mult(1f / 6f);

            PVector pxFromBoard = multiCalibrator.computePxPosition(new PVector(148.6f, 103.3f));

            if (seeThrough) {
                g.noFill();
                g.stroke(0, 255, 0);
                // Debug AR
                g.ellipse(mid.x, mid.y, 8, 8);

                g.stroke(40, 255, 120);
                g.ellipse(pxFromBoard.x, pxFromBoard.y, 8, 8);
            }

            // Find the distance, reset the array, go to the next point.
            System.out.println("Found: " + mid);
            System.out.println("board Found: " + pxFromBoard);
            System.out.println("Dist in px: " + PVector.dist(mid, pxFromBoard));

            // Dist enregistrées ~4-5 px
        }

//        System.out.println("FrameRate: " + parent.frameRate);
        // Debug sin. 
        byte[] found = multiCalibrator.found;

        if (found != null) {
            g.pushMatrix();
            g.translate(300, 300);

            g.noStroke();
            g.colorMode(RGB, 255);
            int k = 0;
//	scale(sc);
            for (int j = 0; j < multiCalibrator.capH; j++) {
                for (int i = 0; i < multiCalibrator.capW; i++) {
                    if (k >= found.length) {
                        continue;
                    }
                    if (found[k++] == 0) {
                        g.fill(20, 255, 10, 180);
                    } else {
                        g.fill(30);
                    }
                    g.rect(i, j, 1, 1);
                }
            }
            g.popMatrix();
        }

        //
        parent.g.pushMatrix();
        parent.g.translate(mouseClick.x, mouseClick.y - 90);
        sin(parent, parent.g, 255, multiCalibrator.freqToFind, 60, 8);
        parent.g.popMatrix();

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

}
