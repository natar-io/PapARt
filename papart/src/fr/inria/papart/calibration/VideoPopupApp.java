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
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class VideoPopupApp extends PApplet {

    Camera cameraTracking;
    CalibrationUI parent;

    public int previewSize = 10;
    public int previewSizeVisu = 5;

    protected PVector[] corners;
    protected PVector[] secondCorners;

    protected int currentCorner = 0;
    protected boolean showZoom = false;
    protected boolean useSmallCorners = true;

    static final String CORNERS_NAME = "cornersProj.json";
    private String cornersFileName;

    private Skatolo skatolo;
    private PixelSelect[] cornerWidgets;
    private Group cornersGroup;
    private Toggle zoomToggle;
    private RadioButton cornersRadio;

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
        surface.setSize(cameraTracking.width(), cameraTracking.height());
    }

    public void setup() {
        setSize();

        initCorners();
        cornersFileName = Papart.calibrationFolder + CORNERS_NAME;
        initGUI();

        loadCorners();

        this.smallCorners(1);
    }

    void initCorners() {
        corners = new PVector[4];
        secondCorners = new PVector[4];
        // Corners of the image of the projector
        corners[0] = new PVector(100, 100);
        corners[1] = new PVector(200, 100);
        corners[2] = new PVector(200, 200);
        corners[3] = new PVector(100, 200);
    }

    private void updateCornerWidgets() {
        cornerWidgets[0].setArrayValue(new float[]{corners[0].x,
            corners[0].y});

        cornerWidgets[1].setArrayValue(new float[]{corners[1].x,
            corners[1].y});

        cornerWidgets[2].setArrayValue(new float[]{corners[2].x,
            corners[2].y});

        cornerWidgets[3].setArrayValue(new float[]{corners[3].x,
            corners[3].y});
    }

    private void initGUI() {
        skatolo = new Skatolo(this);
        cornerWidgets = new PixelSelect[4];
        cornerWidgets[0] = skatolo.addPixelSelect("origin").setLabel("white (1)");

        cornerWidgets[1] = skatolo.addPixelSelect("xAxis")
                .setLabel("red (2)");

        cornerWidgets[2] = skatolo.addPixelSelect("corner")
                .setLabel("green (3)");

        cornerWidgets[3] = skatolo.addPixelSelect("yAxis")
                .setLabel("yellow (4)");

        cornersGroup = skatolo.addGroup("CornersGroup")
                .setPosition(250, 30)
                // .setWidth(300)
                // .setHeight(300)
                .activateEvent(true)
                .setBackgroundColor(color(30))
                .setLabel("Menu :");

        // All of this is desactivated, it will be updated in needed.
//        cornersRadio = skatolo.addRadioButton("Corners")
//                .setPosition(240, 10)
//                .setItemWidth(20)
//                .setItemHeight(20)
//                .addItem("bottom Left (1)", 0) // 0, y
//                .addItem("bottom Right (2)", 1) // x ,y
//                .addItem("top right (3)", 2) // x, 0
//                .addItem("Top Left (4)", 3) // 0, 0
//                .activate(0)
//                .plugTo(this, "activeCorner")
//                .setGroup("CornersGroup");
//
//        zoomToggle = skatolo.addToggle("showZoom")
//                .setPosition(240, 120)
//                .setCaptionLabel("Zoom (z)")
//                .setGroup("CornersGroup");

        skatolo.addBang("Load")
                .setPosition(10, 10)
                .setCaptionLabel("Load (l)")
                .plugTo(this, "loadCorners")
                .setGroup("CornersGroup");

        skatolo.addBang("Save")
                .setPosition(10, 50)
                .setCaptionLabel("Save (s)")
                .plugTo(this, "saveCorners")
                .setGroup("CornersGroup");

        skatolo.addToggle("stopCalibration")
                .setCaptionLabel("Validate and close (ESC)")
                .setPosition(10, 90)
                .setGroup("CornersGroup");

        skatolo.addRadioButton("smallCorners")
                .setPosition(150, 10)
                .setItemWidth(20)
                .setItemHeight(20)
                .addItem("FullScreen corners", 0) // 0, y
                .addItem("Smaller corners", 1) // x ,y
                .activate(1)
                .setGroup("CornersGroup");

        skatolo.addButton("prepareScreenShot")
                .setPosition(150, 55)
                .setCaptionLabel("Take Picure (u)")
                .setGroup("CornersGroup");

        cornersGroup.show();

    }

    public void smallCorners(int incoming) {
        if (incoming == 0 || incoming == 1) {
            this.useSmallCorners = incoming == 1;

        }
        parent.projector.setSmallCornerCalibration(useSmallCorners);
    }

    public PVector corners(int id) {
        return corners[id];
    }

    public PVector secondCorners(int id) {
        return secondCorners[id];
    }

    public int currentCorner() {
        return currentCorner;
    }

    PImage camImage;

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

        image(camImage, 0, 0, cameraTracking.width(), cameraTracking.height());
        updateCornersFrowWidgets();
        drawProjectionZone();
//        drawRectAroundCurrentCorner();
//   Zoom deactivated       
// if (showZoom) {
//            drawZoom();
//        }

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

    private void updateCornersFrowWidgets() {
        for (int i = 0; i < 4; i++) {
            PixelSelect corner = cornerWidgets[i];
            float[] pos = corner.getArrayValue();
            corners(i).set(pos[0], pos[1]);
        }
    }

    private void drawProjectionZone() {
        fill(0, 180, 0, 100);
        quad(corners(0).x, corners(0).y,
                corners(1).x, corners(1).y,
                corners(2).x, corners(2).y,
                corners(3).x, corners(3).y);

        if (useSmallCorners) {
            fill(0, 0, 120, 50);
            quad(secondCorners(0).x, secondCorners(0).y,
                    secondCorners(1).x, secondCorners(1).y,
                    secondCorners(2).x, secondCorners(2).y,
                    secondCorners(3).x, secondCorners(3).y);
        }
    }

    private void drawZoom() {
        PImage currentImage = cameraTracking.getPImageCopy();
        PImage cornerPreview = currentImage.get((int) (corners(currentCorner()).x - previewSize),
                (int) (corners(currentCorner()).y - previewSize),
                previewSize * 2,
                previewSize * 2);
        image(cornerPreview, 0, 0, previewSize * 2 * previewSizeVisu, previewSize * 2 * previewSizeVisu);

        pushMatrix();
        translate(previewSize * previewSizeVisu, previewSize * previewSizeVisu);

        stroke(255);
        strokeWeight(1);
        int crossSize = previewSize * previewSizeVisu / 2;
        line(-crossSize, 0, crossSize, 0);
        line(0, -crossSize, 0, crossSize);
        popMatrix();
    }

    public void loadCorners() {
        try {
            JSONArray values = loadJSONArray(cornersFileName);
            for (int i = 0; i < values.size(); i++) {
                JSONObject cornerJSON = values.getJSONObject(i);
                corners[i].set(cornerJSON.getFloat("x"),
                        cornerJSON.getFloat("y"));
                updateCornerWidgets();
            }
        } catch (Exception e) {
            System.out.println("Could not load saved corners. check your file : " + cornersFileName);

        }
    }

    public void saveCorners() {
        JSONArray values = new JSONArray();
        for (int i = 0; i < cornerWidgets.length; i++) {
            JSONObject cornerJSON = new JSONObject();
            cornerJSON.setFloat("x", corners[i].x);
            cornerJSON.setFloat("y", corners[i].y);
            values.setJSONObject(i, cornerJSON);
        }
        saveJSONArray(values, cornersFileName);
    }

    public void activateCornerNo(int nb) {
        cornersRadio.activate(nb);
        currentCorner = nb;
    }

    private void updateCornersInProjectorView() {
        if (useSmallCorners) {
            // Find 3D plane of selected points (arbitrary sizes) 
            float aspectRatio = ((float) parent.projector.getHeight())
                    / ((float) parent.projector.getWidth());

            PVector object[] = new PVector[4];
            object[0] = new PVector(0, 0);
            object[1] = new PVector(2, 0);
            object[2] = new PVector(2, 2 * aspectRatio);
            object[3] = new PVector(0, 2 * aspectRatio);
            PMatrix3D transfo = cameraTracking.getProjectiveDevice().estimateOrientation(object, this.corners);

            PMatrix3D tr = transfo.get();

            // First point top left corner. 
            tr.translate(-1, -aspectRatio);
            PVector c = new PVector(tr.m03, tr.m13, tr.m23);
            secondCorners[0] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);

            tr = transfo.get();
            tr.translate(3, -aspectRatio);
            c = new PVector(tr.m03, tr.m13, tr.m23);
            secondCorners[1] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);

            tr = transfo.get();
            tr.translate(3, 3 * aspectRatio);
            c = new PVector(tr.m03, tr.m13, tr.m23);
            secondCorners[2] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);

            tr = transfo.get();
            tr.translate(-1, 3 * aspectRatio);
            c = new PVector(tr.m03, tr.m13, tr.m23);
            secondCorners[3] = cameraTracking.getProjectiveDevice().worldToPixelUnconstrained(c);

            parent.projectorView.setCorners(secondCorners);
        } else {
            parent.projectorView.setCorners(corners);
        }
    }

    @Override
    public void keyPressed() {

        if (key != CODED) {

            for (PixelSelect corner : cornerWidgets) {
                corner.setKeyboardControlled(false);
            }

            try {
                int v = Character.getNumericValue(key);
                if (v >= 1 && v < 5) {
                    cornerWidgets[v - 1].setKeyboardControlled(true);
                    currentCorner = v - 1;
                }
            } catch (Exception e) {
                // Ignored
            }

        }

        if (key == 'u') {
            prepareScreenShot();
        }

        if (key == 'l') {
            loadCorners();
        }
        if (key == 's') {
            saveCorners();
        }

        if (key == 'z') {
            zoomToggle.setState(!showZoom);
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
