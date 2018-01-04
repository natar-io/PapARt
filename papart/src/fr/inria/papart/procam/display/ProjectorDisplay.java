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
package fr.inria.papart.procam.display;

import fr.inria.papart.calibration.MultiCalibrator;
import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.utils.DrawUtils;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.procam.ProjectiveDeviceP;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

public class ProjectorDisplay extends ARDisplay {

    public ProjectorDisplay(PApplet parent, String calibrationYAML) {
        super(parent, calibrationYAML);
    }

    @Override
    protected void loadCalibration(String calibrationYAML) {
//        System.out.println("Loading projector internals ... " + calibrationYAML);
        try {
            projectiveDeviceP = ProjectiveDeviceP.loadProjectorDevice(parent, calibrationYAML);
            setCalibration(projectiveDeviceP);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading the projector device." + e);
        }
    }

    @Override
    public void pre() {
        this.clear();
        this.graphics.background(0);
    }

    public PVector mouseClick = new PVector();

    @Override
    public void draw() {
        if (this.isCalibrationMode) {
            MultiCalibrator.drawCalibration(getGraphics());
            return;
        }
        drawScreensOver();
        parent.noStroke();
        DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                this.render(),
                0, 0, this.frameWidth, this.frameHeight);
    }

    /**
     * Previous internal corner calibration.
     */
    private void drawCalibration() {
        parent.g.background(0);
        if (parent.mousePressed) {
            mouseClick.set(parent.mouseX, parent.mouseY);
        }
        parent.g.ellipse(mouseClick.x, mouseClick.y, 50, 50);
        parent.g.rect(mouseClick.x - 5, mouseClick.y, 10, 1);
        parent.g.rect(mouseClick.x, mouseClick.y - 5, 1, 10);
        parent.g.ellipse(mouseClick.x, mouseClick.y, 50, 50);

        if (this.isSmallCalibration) {
            projectSmallCornersImage();
        } else {
            projectCornersImage();
        }
    }

    private boolean isSmallCalibration = false;

    public void setSmallCornerCalibration(boolean smallC) {
        this.isSmallCalibration = smallC;
    }

    public PGraphicsOpenGL beginDrawOnBoard(Camera camera, MarkerBoard board) {
        this.beginDraw();

        // Get the markerboard viewed by the camera
        PMatrix3D camBoard = board.getTransfoMat(camera);
        camBoard.preApply(getExtrinsics());
        this.graphics.applyMatrix(camBoard);

        return this.graphics;
    }

    public void drawOnBoard(Camera camera, MarkerBoard board) {
        loadModelView();
        PMatrix3D camBoard = board.getTransfoMat(camera);
        camBoard.preApply(getExtrinsics());
        this.graphics.applyMatrix(camBoard);
    }

    @Override
        public void drawScreens() {
        this.beginDraw();
        this.graphics.clear();
        drawScreensProjection();
        this.endDraw();
    }

    @Override
        public void drawScreensOver() {
        this.beginDraw();
        drawScreensProjection();
        this.endDraw();
    }

    public void drawScreensProjection() {
        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.applyMatrix(getExtrinsics());

        super.renderScreens();
    }

    protected ReadonlyVec3D projectPointer3DVec3D(PaperScreen screen, float px, float py) {
        // Create ray from the projector (origin / viewed pixel)
        // Intersect this ray with the piece of paper. 
        // Compute the Two points for the ray          
        PVector originP = new PVector(0, 0, 0);
        PVector viewedPtP = projectiveDeviceP.pixelToWorldNormP((int) (px * frameWidth), (int) (py * frameHeight));

        // Pass it to the camera point of view (origin)
        PMatrix3D extr = extrinsicsInv;
        PVector originC = new PVector();
        PVector viewedPtC = new PVector();
        extr.mult(originP, originC);
        extr.mult(viewedPtP, viewedPtC);

        // Second argument is a direction
        viewedPtC.sub(originC);

        Ray3D ray
                = new Ray3D(new Vec3D(originC.x,
                        originC.y,
                        originC.z),
                        new Vec3D(viewedPtC.x,
                                viewedPtC.y,
                                viewedPtC.z));

        // Intersect ray with Plane 
        // TODO: Do no use screen.getPlane !!!
        ReadonlyVec3D inter = screen.getPlane().getIntersectionWithRay(ray);

        // It may not intersect.
        if (inter == null) {
            return null;
        } else {
            return inter;
        }
        // Check the error of the ray casting -- Debug only  
//        PVector inter1P = new PVector();
//        projExtrinsicsP3D.mult(interP, inter1P);
//        PVector px2 = projectiveDeviceP.worldToPixel(inter1P, false);
//        px2.sub(px * frameWidth, py * frameHeight, 0);
//        System.out.println("Error " + px2.mag());
    }

    public PVector projectPointer3D(PaperScreen paperScreen, float px, float py) {
        ReadonlyVec3D inter = projectPointer3DVec3D(paperScreen, px, py);
        if (inter == null) {
            return TouchInput.NO_INTERSECTION;
        } else {
            return new PVector(inter.x(), inter.y(), inter.z());
        }
    }

    // *** Projects the pixels viewed by the projector to the screen.
    // px and py are normalized -- [0, 1] in screen space
    @Override
        public PVector projectPointer(PaperScreen paperScreen, float px, float py) {

        ReadonlyVec3D inter = projectPointer3DVec3D(paperScreen, px, py);
        // It may not intersect.
        if (inter == null) {
            return TouchInput.NO_INTERSECTION;
        }

        // Get the normalized coordinates in Paper coordinates
        Vec3D res = paperScreen.getWorldToScreen().applyTo(inter);
//        PVector out = new PVector(res.x(), res.y(), res.z());
        PVector out = new PVector(res.x() / res.z(),
                res.y() / res.z(), 1);
        out.y = 1 - out.y;

        // Second possiblity... (WORKING)  Use directly the 3D location instead of the plane.
//     PVector interP = new PVector(inter.x(), inter.y(), inter.z());
//        PVector out3 = new PVector();
//        PMatrix3D posInv = screen.getPos().get();
//        posInv.invert();
//        posInv.mult(interP, out3);
//        out3.x /= screen.getSize().x;
//        out3.y /= screen.getSize().y;
//
//        PVector diff = PVector.sub(out, out3);
//        System.out.println("Diff " + diff);
//        out3.y = 1 - out3.y;
        return out;
    }

    /**
     * Computes the 3D coordinates of a projected pixel in the tracking camera
     * coordinate system.
     *
     * @param planeCalibCam projection plane
     * @param px x axis in pixel coordinates
     * @param py x axis in pixel coordinates
     * @return
     */
    public PVector getProjectedPointOnPlane(PlaneCalibration planeCalibCam, float px, float py) {
        // Create ray from the projector (origin / viewed pixel)
        // Intersect this ray with the piece of paper.
        // Compute the Two points for the ray
        PVector originP = new PVector(0, 0, 0);
        PVector viewedPtP = getProjectiveDeviceP().pixelToWorldNormalized(px, py);

        // Pass it to the camera point of view (origin)
        PMatrix3D proCamExtrinsics = getExtrinsicsInv();
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
            return null;
        }

        return new PVector(inter.x(), inter.y(), inter.z());
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        this.hasCamera = true;
    }

    private int cornerS = 2, cornerM = 30, cornerL = 50, cornerXL = 100;

    public void setCalibrationSize(int small, int med, int large, int xl) {
        this.cornerS = small;
        this.cornerM = med;
        this.cornerL = large;
        this.cornerXL = xl;
    }

    /**
     * Draws in parent (main) rendering.
     */
    void projectCornersImage() {
        PGraphicsOpenGL g = (PGraphicsOpenGL) parent.g;

        g.pushMatrix();
        g.ellipseMode(PApplet.CENTER);

        g.noStroke();
        // corner 4  Yellow 0, 0
        g.fill(255, 255, 50);
        g.translate(0, 0);

        drawEllipses(g);

        // corner 3  green x, 0  
        g.translate(g.width, 0);
        g.fill(50, 255, 50);
        drawEllipses(g);

        // Corner 2  Red  x,y
        g.fill(255, 50, 55);
        g.translate(0, g.height);
        drawEllipses(g);

        // Corner 1  White 0,y
        g.fill(255);
        g.translate(-g.width, 0);
        drawEllipses(g);

        g.popMatrix();
        // In TrackedView 
        // 0 is ->  0, h   (white)
        // 1 is ->  w, h   (Red)
        // 2 is ->  w, 0   (Green)
        // 3 is ->  0, 0   (Yellow)
        g.stroke(200);
        g.noFill();
        g.strokeWeight(3);
        g.rect(0, 0, g.width, g.height);
    }

    /**
     * Draws in parent (main) rendering.
     */
    private void projectSmallCornersImage() {
        PGraphicsOpenGL g = (PGraphicsOpenGL) parent.g;

        g.pushMatrix();
        g.ellipseMode(PApplet.CENTER);

        // half from each size ?
        float nbDivision = 2;

        float xOrig = g.width / (nbDivision * 2);
        float yOrig = g.height / (nbDivision * 2);
        float xWidth = nbDivision * (g.width / (nbDivision * 2));
        float yHeight = nbDivision * (g.height / (nbDivision * 2));

        g.noStroke();
        // corner 4  Yellow 0, 0
        g.fill(255, 255, 50);
        g.translate(xOrig, yOrig);
        drawEllipses(g);

        // corner 3  green x, 0  
        g.translate(xWidth, 0);
        g.fill(50, 255, 50);
        drawEllipses(g);

        // Corner 2  Red  x,y
        g.fill(255, 50, 55);
        g.translate(0, yHeight);
        drawEllipses(g);

        // Corner 1  White 0,y
        g.fill(255);
        g.translate(-xWidth, 0);
        drawEllipses(g);

        g.popMatrix();
        // In TrackedView 
        // 0 is ->  0, h   (white)
        // 1 is ->  w, h   (Red)
        // 2 is ->  w, 0   (Green)
        // 3 is ->  0, 0   (Yellow)
        g.stroke(200);
        g.noFill();
        g.strokeWeight(3);
        g.rect(xOrig, yOrig, xWidth, yHeight);
    }

    void drawEllipses(PGraphicsOpenGL g) {

        g.ellipse(0, 0, cornerXL, cornerXL);

        g.fill(120);
        g.ellipse(0, 0, cornerL, cornerL);

        g.fill(160);
        g.ellipse(0, 0, cornerM, cornerM);

        g.fill(200);
        g.ellipse(0, 0, cornerS, cornerS);
    }

}
