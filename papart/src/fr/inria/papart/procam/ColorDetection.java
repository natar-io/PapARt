/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.TrackedView;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Experimental class, do not use
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ColorDetection {

    private final PaperScreen paperScreen;
    protected TrackedView boardView;

    private PVector captureSize = new PVector(10, 10);
    private final PVector pos = new PVector();

    private int picWidth = 8; // Works better with power  of 2
    private int picHeight = 8; // Works better with power  of 2
    private PVector captureOffset;

    private boolean invY;

    // output 
    protected int col;

    public ColorDetection(PaperScreen paperScreen) {
        this.invY = false;
        this.paperScreen = paperScreen;
        setCaptureOffset(new PVector());
    }

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
            if (invY) {
                boardView.setBottomLeftCorner(new PVector(pos.x, paperScreen.drawingSize.y - pos.y));
            } else {
                boardView.setBottomLeftCorner(pos);
            }
        }
    }

    public void update() {
        computeColor();
    }

    public void drawSelf() {
        computeColor();

        paperScreen.pushMatrix();
        paperScreen.translate(pos.x,
                pos.y, 1);

        drawCaptureZonePriv();
        drawCapturedColor();
        drawCapturedImage();
        paperScreen.popMatrix();
    }

    public PVector getPosition() {
        return this.pos;
    }

    public void drawCapturedImage() {
        PImage out = getImage();
        if (out != null) {
            paperScreen.image(out, 0, -picHeight - 5, picWidth, picHeight);
        }
    }

    public void drawCapturedColor() {
        paperScreen.fill(this.col);
        paperScreen.noStroke();
        paperScreen.ellipse(0, -picWidth - 5, picHeight, picHeight);
    }

    public void drawCaptureZonePriv() {
        paperScreen.strokeWeight(3);
        paperScreen.noFill();
        paperScreen.stroke(80);
        paperScreen.rectMode(PApplet.CORNER);
        paperScreen.rect(0, 0, captureSize.x, captureSize.y);
    }

    public void drawCaptureZone() {
        paperScreen.pushMatrix();
        paperScreen.translate(pos.x,
                pos.y, 1);
        paperScreen.strokeWeight(2);
        paperScreen.noFill();
        paperScreen.stroke(80);
        paperScreen.rectMode(PApplet.CORNER);
        paperScreen.rect(0, 0, captureSize.x, captureSize.y);
        paperScreen.popMatrix();
    }

    public PImage getImage() {
        // TODO: NoCamera HACK
        if (paperScreen.cameraTracking == null) {
            return null;
        }

        PImage out = boardView.getViewOf(paperScreen.cameraTracking);
        return out;
    }

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
            boolean isClose = Utils.colorDist(c, c2, threshold);
            if (isClose) {
                nbSameColor++;
            }
        }
        return nbSameColor;
    }

    public int getColor() {
        return this.col;
    }

    public PVector getCaptureOffset() {
        return captureOffset;
    }

    public void setCaptureOffset(PVector captureOffset) {
        this.captureOffset = captureOffset;
    }

    public PVector getCaptureSize() {
        return captureSize;
    }

    public void setCaptureSize(float x, float y) {
        this.captureSize.set(x, y);
    }

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

    public boolean isInvY() {
        return invY;
    }

    public void setInvY(boolean invY) {
        this.invY = invY;
    }

}
