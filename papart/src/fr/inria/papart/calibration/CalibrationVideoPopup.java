/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
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
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class CalibrationVideoPopup extends PApplet {

    Camera cameraTracking;
    CalibrationPopup calibrationPopup;
    public int previewSize = 10;
    public int previewSizeVisu = 5;

    public CalibrationVideoPopup(CalibrationPopup calibrationPopup) {
        super();
        PApplet.runSketch(new String[]{this.getClass().getName()}, this);
        this.calibrationPopup = calibrationPopup;
    }

    public void settings() {
        size(200, 200);
    }

    public void setSize() {
        // println("AWT ? " + (this.getSurface() instanceof PSurfaceAWT));
        PSurfaceAWT surface = (PSurfaceAWT) this.getSurface();
        SmoothCanvas canvas = (SmoothCanvas) surface.getNative();
        Frame frame = ((SmoothCanvas) canvas).getFrame();
        frame.removeNotify();
        frame.setUndecorated(true);
        frame.addNotify();
//        surface.setLocation(screenOffsetX, screenOffsetY);
        cameraTracking = Papart.getPapart().getPublicCameraTracking();
        surface.setSize(cameraTracking.width(), cameraTracking.height());
    }

    public void setup() {
        setSize();

    }

    public PVector corners(int id) {
        return calibrationPopup.corners[id];
    }

    public PVector secondCorners(int id) {
        return calibrationPopup.secondCorners[id];
    }

    public int currentCorner() {
        return calibrationPopup.currentCorner;
    }

    @Override
    public void draw() {
        image(cameraTracking.getPImageCopy(this), 0, 0, cameraTracking.width(), cameraTracking.height());

        drawProjectionZone();
        drawRectAroundCurrentCorner();
        if (calibrationPopup.showZoom) {
            drawZoom();
        }
        noStroke();
    }

    private void drawProjectionZone() {

        fill(0, 180, 0, 100);
        quad(corners(0).x, corners(0).y,
                corners(1).x, corners(1).y,
                corners(2).x, corners(2).y,
                corners(3).x, corners(3).y);

        if (calibrationPopup.useSmallCorners) {
            fill(0, 0, 120, 100);
            quad(secondCorners(0).x, secondCorners(0).y,
                    secondCorners(1).x, secondCorners(1).y,
                    secondCorners(2).x, secondCorners(2).y,
                    secondCorners(3).x, secondCorners(3).y);
        }
    }

    private void drawRectAroundCurrentCorner() {
        noFill();
        rectMode(CENTER);
        stroke(255);
        strokeWeight(1);

        pushMatrix();
        translate(corners(currentCorner()).x,
                corners(currentCorner()).y);
        rect(0, 0, 15, 15);
        popMatrix();
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

    @Override
    public void mouseDragged() {
        corners(currentCorner()).set(mouseX, mouseY);
    }

    @Override
    public void keyPressed() {

        if (key == '1') {
            calibrationPopup.activateCornerNo(0);
        }

        if (key == '2') {
            calibrationPopup.activateCornerNo(1);
        }

        if (key == '3') {
            calibrationPopup.activateCornerNo(2);
        }

        if (key == '4') {
            calibrationPopup.activateCornerNo(3);
        }

        if (key == 'l') {
            calibrationPopup.loadCorners();
        }
        if (key == 's') {
            calibrationPopup.saveCorners();
        }

        if (key == 'z') {
            calibrationPopup.zoomToggle.setState(!calibrationPopup.showZoom);
//            calibrationPopup.showZoom = !calibrationPopup.showZoom;
        }

        if (key == CODED) {
            if (keyCode == UP) {
                calibrationPopup.moveCornerUp(true, 1);
            }

            if (keyCode == DOWN) {
                calibrationPopup.moveCornerUp(false, 1);
            }

            if (keyCode == LEFT) {
                calibrationPopup.moveCornerLeft(true, 1);
            }

            if (keyCode == RIGHT) {
                calibrationPopup.moveCornerLeft(false, 1);
            }
        }

        if (key == 27) { //The ASCII code for esc is 27, so therefore: 27
            calibrationPopup.stopCornerCalibration();

        }
        if (key == ESC) {
            key = 0;
        }

    }

}
