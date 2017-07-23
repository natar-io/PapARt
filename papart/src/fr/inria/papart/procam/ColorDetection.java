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
package fr.inria.papart.procam;

import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.TrackedView;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Experimental class, do not use
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class ColorDetection {

    private final PaperScreen paperScreen;
    protected TrackedView boardView;

    private PVector captureSize = new PVector(10, 10);
    private final PVector pos = new PVector();

    private int picWidth = 8; // Works better with power  of 2
    private int picHeight = 8; // Works better with power  of 2
    private PVector captureOffset;


    // output 
    protected int col;

    /**
     * Create a color detection on a given PaperScreen. 
     * It will use this paperScreen's coordinates.
     * @param paperScreen 
     */
    public ColorDetection(PaperScreen paperScreen) {
        this.paperScreen = paperScreen;
        setCaptureOffset(new PVector());
    }

    /**
     * Allocates the memory. 
     */
    public void init(){
        initialize();
    }
    
    @Deprecated
    public void initialize() {
        boardView = new TrackedView(paperScreen);
        setPosition(pos);
        boardView.setCaptureSizeMM(captureSize);
        boardView.setImageWidthPx(picWidth);
        boardView.setImageHeightPx(picHeight);
        boardView.init();
    }

    public void setPosition(PVector pos) {
        this.pos.set(pos);
        if (boardView != null) {
                boardView.setTopLeftCorner(pos);
        }
    }

    /** 
     * Compute the color. 
     */
    public void update() {
        computeColor();
    }

    /** 
     * Compute the color, and draw the detection zone and the detected color. 
     * For debug purposes. 
     */
    public void drawSelf() {
        computeColor();

        drawCaptureZone();

        paperScreen.pushMatrix();
        paperScreen.translate(pos.x,
                pos.y, 0.2f);
        paperScreen.translate(captureSize.x + 20, 0);
        drawCapturedImage();

        paperScreen.translate(20, 0);
        drawCapturedColor();

        paperScreen.popMatrix();
    }

    public PVector getPosition() {
        return this.pos;
    }

    /**
     * Draw the image from 'getImage', at the current location.
     */
    public void drawCapturedImage() {
        PImage out = getImage();
        if (out != null) {
            paperScreen.image(out, 0, 0, captureSize.x, captureSize.y);
        }
    }

    /**
     * Draw an ellipse with a fill of the captured color.
     * ellipse(0, 0, 10, 10);
     */
    public void drawCapturedColor() {
        paperScreen.fill(this.col);
        paperScreen.noStroke();
        paperScreen.ellipse(0, 0, 10, 10);
    }

    /** 
     * Draw the zone captured to compute the color. 
     */
    public void drawCaptureZone() {
        paperScreen.pushMatrix();
        paperScreen.translate(pos.x,
                pos.y,
                0.2f);

        paperScreen.strokeWeight(2);
        paperScreen.noFill();
        paperScreen.stroke(80);
        paperScreen.rectMode(PApplet.CORNER);
        paperScreen.rect(0, 0, captureSize.x, captureSize.y);
        paperScreen.popMatrix();
    }

    /**
     * Return the image used for color computation.
     * Warning, can return null images. 
     * @return the PImage or null in debug mode. 
     */
    public PImage getImage() {
        // TODO: NoCamera HACK
        if (paperScreen.cameraTracking == null) {
            return null;
        }

        PImage out = boardView.getViewOf(paperScreen.cameraTracking);
        return out;
    }

    /**
     * Compute the average color of the patch analyzed.
     */
    public void computeColor() {

        // HACK -> TODO error management. 
        if (paperScreen.cameraTracking == null) {
            return;
        }

        PImage out = getImage();
        if (out == null) {
            return;
        }
        out.loadPixels();
        int avgRed = 0;
        int avgGreen = 0;
        int avgBlue = 0;
        int pxNb = picWidth * picHeight;
        for (int k = 0; k < pxNb; k++) {
            int c = out.pixels[k];
            avgRed += c >> 16 & 0xFF;
            avgGreen += c >> 8 & 0xFF;
            avgBlue += c >> 0 & 0xFF;
        }

        avgRed = (avgRed / pxNb) << 16;
        avgGreen = (avgGreen / pxNb) << 8;
        avgBlue /= pxNb;
        this.col = 255 << 24 | avgRed | avgGreen | avgBlue;
    }

    /**
     * Get the occurences of a given color, given a error. 
     * @param c  color to find. 
     * @param threshold error margin
     * @return number of occurences. 
     */
    public int computeOccurencesOfColor(int c, int threshold) {

        // TODO: Hack for noCamera, better to be done. 
        if (paperScreen.cameraTracking == null) {
            return 0;
        }

        PImage out = getImage();
        if (out == null) {
            return 0;
        }

        out.loadPixels();
        int pxNb = picWidth * picHeight;
        int nbSameColor = 0;

        for (int k = 0; k < pxNb; k++) {
            int c2 = out.pixels[k];
            boolean isClose = MathUtils.colorDistRGB(c, c2, threshold);
            if (isClose) {
                nbSameColor++;
            }
        }
        return nbSameColor;
    }

    /**
     * Color found. Call update() or computeColor() before to get the latest color. 
     * @return the color as int. 
     */
    public int getColor() {
        return this.col;
    }

    public PVector getCaptureOffset() {
        return captureOffset;
    }

    /**
     * Set the position of the capture in millimeter. 
     * @param captureOffset 
     */
    public void setCaptureOffset(PVector captureOffset) {
        this.captureOffset = captureOffset;
    }
  /**
     * Set the position of the capture in millimeter. 
     * @param captureOffset 
     */
    public void setCaptureOffset(float x, float y) {
        this.captureOffset.set(x, y);
    }

    public PVector getCaptureSize() {
        return captureSize.copy();
    }

    /**
     * Set the capture size in millimeters.
     *
     * @param x
     * @param y
     */
    public void setCaptureSize(float x, float y) {
        this.captureSize.set(x, y);
    }

    /**
     * Set the capture size in millimeters.
     *
     * @param size in mm.
     */
    public void setCaptureSize(PVector size) {
        setCaptureSize(size.x, size.y);
    }

    /**
     * Set the picture size for analysis in pixels.
     *
     * @param picWidth
     * @param picHeight
     */
    public void setPicSize(int picWidth, int picHeight) {
        this.picWidth = picWidth;
        this.picHeight = picHeight;
    }

    public int getPicWidth() {
        return picWidth;
    }

    public int getPicHeight() {
        return picHeight;
    }

}
