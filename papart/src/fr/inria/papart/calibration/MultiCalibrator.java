/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.display.ARDisplay;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.DrawUtils;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class MultiCalibrator extends PaperScreen {

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
    @Override
    public void settings() {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        startCalib();
    }

    protected boolean active = false;

    @Override
    public void drawOnPaper() {
        // setLocation(63, 45, 0);
//      DetectedMarker[] list = papart.getMarkerList();
//      println("markers :  " + list.length);

        // background: blue
        background(0, 0, 200, 50);

        if (active) {
            drawDebugZones();
            System.out.print("Location: " + this.getMarkerBoard());
            this.getLocation().print();
        }

    }

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
        // Classical rendering (if needed... )

        if (!multiCalibrator.isActive()) {
            System.out.println("ERROR: cannot calibrate with inactive calibrator.");
            return;
        }

        parent.g.clear();

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
        if (multiCalibrator.getDisplay() instanceof ProjectorDisplay) {
            ProjectorDisplay display = (ProjectorDisplay) multiCalibrator.getDisplay();
            display.drawScreensOver();
            parent.noStroke();
            DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                    display.render(),
                    0, 0, display.getWidth(), display.getHeight());
        }

        // Both display modes for now. 
        if (parent.mousePressed) {
            mouseClick.set(parent.mouseX, parent.mouseY);
        }
        parent.g.noFill();
        parent.g.stroke(255);
        parent.g.ellipse(mouseClick.x, mouseClick.y, 50, 50);
        parent.g.rect(mouseClick.x - 5, mouseClick.y, 10, 1);
        parent.g.rect(mouseClick.x, mouseClick.y - 5, 1, 10);
        parent.g.ellipse(mouseClick.x, mouseClick.y, 20, 20);
    }

}
