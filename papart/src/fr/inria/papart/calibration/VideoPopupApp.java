/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import tech.lity.rea.skatolo.Skatolo;
import tech.lity.rea.skatolo.gui.group.Group;
import tech.lity.rea.skatolo.gui.group.RadioButton;
import java.awt.Frame;
import org.bytedeco.javacpp.opencv_core;
import processing.awt.PSurfaceAWT;
import processing.awt.PSurfaceAWT.SmoothCanvas;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import tech.lity.rea.skatolo.gui.controllers.Toggle;
import tech.lity.rea.skatolo.gui.widgets.PixelSelect;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class VideoPopupApp extends PApplet {

    Camera cameraTracking;
    CalibrationUI parent;

    protected boolean showZoom = false;
    protected boolean useSmallCorners = true;

    static final String CORNERS_NAME = "cornersProj.json";
    private String cornersFileName;

    private Skatolo skatolo;

    PImage camImage;

    public VideoPopupApp(CalibrationUI calibrationPopup) {
        super();
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
        this.parent = calibrationPopup;
    }

    public void settings() {
        size(1280, 720);
    }

    public void setSize() {
        // println("AWT ? " + (this.getSurface() instanceof PSurfaceAWT));
        PSurfaceAWT surface = (PSurfaceAWT) this.getSurface();
        SmoothCanvas canvas = (SmoothCanvas) surface.getNative();
        Frame frame = ((SmoothCanvas) canvas).getFrame();
//        frame.removeNotify();
//        frame.setUndecorated(true);
//        frame.addNotify();
//        surface.setLocation(screenOffsetX, screenOffsetY);
        cameraTracking = Papart.getPapart().getPublicCameraTracking();
//        surface.setSize(cameraTracking.width(), cameraTracking.height());

        // 400 x 400 images.
        surface.setSize(400 * 2, 400);

    }

    @Override
    public void setup() {
        setSize();
        cornersFileName = Papart.calibrationFolder + CORNERS_NAME;
        initGUI();
    }

    private void initGUI() {
        skatolo = new Skatolo(this);
    }

    @Override
    public void draw() {

        // Initial image.
        if (camImage == null) {
            camImage = cameraTracking.getPImageCopy(this);
        }

        if (takeScreenshot && !screenShotTaken
                && screenShotTime < parent.millis()) {
            takeScreenShot();
        }

        updateCornersInProjectorView();

        // Draw the camera image + the detected point. 
        image(camImage, 0, 0, 400, 400);

        // draw estimated projector image ?
//        updateCornersFrowWidgets();
//        drawProjectionZone();
        PVector p = parent.pointer.getLocationVector();
        PVector v = cameraTracking.getProjectiveDevice().worldToPixelCoord(p, false);
        noFill();
        stroke(255);
        ellipse(v.x / cameraTracking.width() * 400f,
                v.y / cameraTracking.height() * 400f,
                20, 20);
        
        // TODO: Add distorsion from the projector
        PVector imagePts = parent.projector.mouseClick.copy();

        if (keyPressed && key == 'a' && imagePts.x != 0 && imagePts.y != 0) {
            parent.pointer.getLocationVector().set(0, 0);
            PVector objectPts = v.copy();
            System.out.println("Adding pair.");
            
            parent.projectorView.addObjectImagePair(objectPts, imagePts);
            parent.projector.mouseClick.x = 0;
            parent.projector.mouseClick.y = 0;
        }

// If enough pairs try to display the projector image. 
        if (parent.projectorView.getNbPairs() >= 3) {
            PImage img = parent.projectorView.getViewOf(cameraTracking);
            if (img != null) {
                image(img,
                        400, 0,
                        400, 400);
            }
        }

        skatolo.draw();

        noStroke();
    }

    private boolean takeScreenshot = false;
    private int screenShotTime = 0;
    private int screenShotDelay = 1500;
    private boolean screenShotTaken = false;

    private void prepareScreenShot() {
        takeScreenshot = true;
        screenShotTaken = false;
        screenShotTime = parent.millis() + screenShotDelay;
        this.hide();
        parent.hideForScreenshot();
    }

    private void takeScreenShot() {
        takeScreenshot = false;
        screenShotTaken = true;
        camImage = cameraTracking.getPImageCopy(this);
        this.show();
//        parent.showAfterScreenshot();

    }

//    private void drawZoom() {
//        PImage currentImage = cameraTracking.getPImageCopy();
//        PImage cornerPreview = currentImage.get((int) (corners(currentCorner()).x - previewSize),
//                (int) (corners(currentCorner()).y - previewSize),
//                previewSize * 2,
//                previewSize * 2);
//        image(cornerPreview, 0, 0, previewSize * 2 * previewSizeVisu, previewSize * 2 * previewSizeVisu);
//
//        pushMatrix();
//        translate(previewSize * previewSizeVisu, previewSize * previewSizeVisu);
//
//        stroke(255);
//        strokeWeight(1);
//        int crossSize = previewSize * previewSizeVisu / 2;
//        line(-crossSize, 0, crossSize, 0);
//        line(0, -crossSize, 0, crossSize);
//        popMatrix();
//    }
    private void updateCornersInProjectorView() {
//        if (useSmallCorners) {
//            // Find 3D plane of selected points (arbitrary sizes) 
//            float aspectRatio = ((float) parent.projector.getHeight())
//                    / ((float) parent.projector.getWidth());
//
//            PVector object[] = new PVector[4];
//            object[0] = new PVector(0, 0);
//            object[1] = new PVector(2, 0);
//            object[2] = new PVector(2, 2 * aspectRatio);
//            object[3] = new PVector(0, 2 * aspectRatio);
//            PMatrix3D transfo = cameraTracking.getProjectiveDevice().estimateOrientation(object, this.corners);
//
//            PMatrix3D tr = transfo.get();
//
//            // First point top left corner. 
//            tr.translate(-1, -aspectRatio);
//            PVector c = new PVector(tr.m03, tr.m13, tr.m23);
//            secondCorners[0] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);
//
//            tr = transfo.get();
//            tr.translate(3, -aspectRatio);
//            c = new PVector(tr.m03, tr.m13, tr.m23);
//            secondCorners[1] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);
//
//            tr = transfo.get();
//            tr.translate(3, 3 * aspectRatio);
//            c = new PVector(tr.m03, tr.m13, tr.m23);
//            secondCorners[2] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);
//
//            tr = transfo.get();
//            tr.translate(-1, 3 * aspectRatio);
//            c = new PVector(tr.m03, tr.m13, tr.m23);
//            secondCorners[3] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);
//
//            parent.projectorView.setCorners(secondCorners);
//        } else {
//            parent.projectorView.setCorners(corners);
//        }
    }

    @Override
    public void keyPressed() {

        if (key != CODED) {
        }
        
        if(key == 'd'){
            System.out.println("Clear image Pair");
             parent.projectorView.clearObjectImagePairs();
        }

        if (key == 'u') {
            prepareScreenShot();
        }

        if (key == 'z') {
//            zoomToggle.setState(!showZoom);
//            calibrationPopup.showZoom = !calibrationPopup.showZoom;
        }

        if (key == 27) { //The ASCII code for esc is 27, so therefore: 27
            stopCalibration();

        }
        if (key == ESC) {
            key = 0;
        }
    }

    public void stopCalibration() {
        this.hide();
        parent.stopCornerCalibration();
    }

    public void hide() {
        this.getSurface().setVisible(false);
    }

    public void show() {
        this.getSurface().setVisible(true);
    }

    void activate() {
        show();
    }

    void deActivate() {
        hide();
    }

}
