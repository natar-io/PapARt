/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.core.PImage;
import processing.core.PVector;

/**
 * Experimental class, do not use
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class ColorDetection {

    PVector size = new PVector(10, 10);
    PVector pos;
    int picSize = 8; // Works better with power  of 2
    int pxNb = picSize * picSize;
    private PaperScreen paperScreen;
    protected TrackedView boardView;
    protected int col;

    public ColorDetection(PaperScreen paperScreen, PVector pos) {
        this(paperScreen, pos, new PVector());
    }

    public ColorDetection(PaperScreen paperScreen, PVector pos, PVector captureOffset) {
        this.pos = pos;
        this.paperScreen = paperScreen;

        boardView = new TrackedView(paperScreen.markerBoard,
                new PVector(pos.x + captureOffset.x,
                        pos.y + captureOffset.y),
                size,
                picSize, picSize);
        paperScreen.cameraTracking.addTrackedView(boardView);
    }

    public void update() {
        computeColor();
    }

    public void drawSelf() {
        computeColor();

        paperScreen.pushMatrix();
        paperScreen.translate(pos.x,
                pos.y, 1);

        drawCaptureZone();
        drawCapturedColor();
//        drawCapturedImage();
        paperScreen.popMatrix();
    }
    
    public PVector getPosition(){
        return this.pos;
    }

    public void drawCapturedImage() {
        PImage out = paperScreen.cameraTracking.getPView(boardView);
        if (out != null) {
            paperScreen.image(out, 0, -picSize - 5, picSize, picSize);
        }
    }

    public void drawCapturedColor() {
        paperScreen.fill(this.col);
        paperScreen.noStroke();
        paperScreen.ellipse(0, -picSize - 5, picSize, picSize);
    }

    public void drawCaptureZone() {
        paperScreen.strokeWeight(3);
        paperScreen.noFill();
        paperScreen.stroke(80);
        paperScreen.rect(0, 0, size.x, size.y);
    }

    public void computeColor() {
        PImage out = paperScreen.cameraTracking.getPView(boardView);
        if (out == null) {
            return;
        }

        out.loadPixels();
        int avgRed = 0;
        int avgGreen = 0;
        int avgBlue = 0;
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

    public int getColor() {
        return this.col;
    }

}
