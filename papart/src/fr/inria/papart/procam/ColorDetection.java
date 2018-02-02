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
package fr.inria.papart.procam;

import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.procam.camera.TrackedView;
import java.util.Arrays;
import processing.core.PApplet;
import static processing.core.PApplet.sqrt;
import processing.core.PImage;
import processing.core.PVector;

/**
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public class ColorDetection {

    private final PaperScreen paperScreen;
    protected TrackedView boardView;

    private final PVector captureSize = new PVector(10, 10);
    private final PVector pos = new PVector();

    private int picWidth = 8; // Works better with power  of 2
    private int picHeight = 8; // Works better with power  of 2
    private PVector captureOffset;

    // output 
    protected int col;

    /**
     * Create a color detection on a given PaperScreen. It will use this
     * paperScreen's coordinates.
     *
     * @param paperScreen
     */
    public ColorDetection(PaperScreen paperScreen) {
        this.paperScreen = paperScreen;
        setCaptureOffset(new PVector());
    }

    /**
     * Allocates the memory.
     */
    public void init() {
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
     */
    public void drawCapturedColor() {
        paperScreen.fill(this.col);
        paperScreen.noStroke();
        paperScreen.ellipse(0, 5, 10, 10);
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
     * Return the image used for color computation. Warning, can return null
     * images.
     *
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

    protected int avgRed = 0;
    protected int avgGreen = 0;
    protected int avgBlue = 0;

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
        avgRed = 0;
        avgGreen = 0;
        avgBlue = 0;
        int pxNb = picWidth * picHeight;
        for (int k = 0; k < pxNb; k++) {
            int c = out.pixels[k];
            avgRed += c >> 16 & 0xFF;
            avgGreen += c >> 8 & 0xFF;
            avgBlue += c >> 0 & 0xFF;
        }

        avgRed = (avgRed / pxNb);
        avgGreen = (avgGreen / pxNb);
        avgBlue = avgBlue / pxNb;
        int r = avgRed << 16;
        int g = avgGreen << 8;
        int b = avgBlue;
        this.col = 255 << 24 | r | g | b;
    }

    /**
     * Get the occurences of a given color, given a error.
     *
     * @param c color to find.
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
     * Color found. Call update() or computeColor() before to get the latest
     * color.
     *
     * @return the color as int.
     */
    public int getColor() {
        return this.col;
    }

    ////// Find blinking rate -> to capture projection. 
    // max rate: 0.25 Hz  (1 beat every 4 sec / sec) observed 3 to 6 times. 
    public void initBlinkTracker() {
        initBlinkTracker(30f, 256);
    }

    float[] blinkFrames = new float[0];
    float[] blinkFramesr = new float[0];
    float[] blinkFramesg = new float[0];
    float[] blinkFramesb = new float[0];
    int currentBlinkIndex = 0;
    private int MAX_BLINK_OBSERVED = 12;
    private int MIN_BLINK_OBSERVED = 8;
    private static final int NOT_OBSERVED = Integer.MIN_VALUE;

    float[] spectrum;
    int fftSize = 128;  // 2 sec?
    FFT fft;
    float frameRate;

    /**
     * Blink tracker, to find a blinking (sine) pattern.
     * @param frameRate
     * @param ffts
     */
    public void initBlinkTracker(float frameRate, int ffts) {
        this.frameRate = frameRate;

//        int nbFrames = (int) (MAX_BLINK_OBSERVED * frameRate / minRate);
//        if (nbFrames < ffts) {
//            nbFrames = ffts;
//        }
        int nbFrames = ffts;
        blinkFrames = new float[nbFrames];
        blinkFramesr = new float[nbFrames];
        blinkFramesg = new float[nbFrames];
        blinkFramesb = new float[nbFrames];

        Arrays.fill(blinkFrames, NOT_OBSERVED);
        Arrays.fill(blinkFramesr, NOT_OBSERVED);
        Arrays.fill(blinkFramesg, NOT_OBSERVED);
        Arrays.fill(blinkFramesb, NOT_OBSERVED);

        // something else ?
        fftSize = ffts;
        fft = new FFT(ffts);
//        methCla = new MethClaInterface();
//        // 44100, 512 . 
//        «.engineNew(30, nbFrames);
//        methCla.engineStart();
//
//        fftSize = 512;
//        spectrum = new float[fftSize];
    }

    /**
     * Find the frequency between black and white projection. Algo: Find the
     * difference.
     */
    public void findBlinkRate() {

        float[] frame = new float[fftSize];
        float[] im = new float[fftSize];
        float[] framer = new float[fftSize];
        float[] imr = new float[fftSize];
        float[] frameg = new float[fftSize];
        float[] img = new float[fftSize];
        float[] frameb = new float[fftSize];
        float[] imb = new float[fftSize];

        int k = currentBlinkIndex;
        for (int i = 0; i < fftSize; i++) {
            // go back in time
            k--;
            if (k <= 0) {
                k = blinkFrames.length - 1;
            }

            if (blinkFrames[k] == NOT_OBSERVED) {
                frame[i] = 0;
                framer[i] = 0;
                frameg[i] = 0;
                frameb[i] = 0;
            } else {
                frame[i] = blinkFrames[k];

                framer[i] = blinkFramesr[k];
                frameg[i] = blinkFramesg[k];
                frameb[i] = blinkFramesb[k];
            }
            // read the value and store it.
        }

//        System.out.println("Before FFT");
//        for (int i = 0; i < fftSize; i++) {
//            System.out.println("b r: " + frame[i] + " " + im[i]);
//        }
        fft.fft(frame, im);
        fft.fft(framer, imr);
        fft.fft(frameg, img);
        fft.fft(frameb, imb);
//        System.out.println("after FFT");
//        for (int i = 0; i < fftSize; i++) {
//            System.out.println("a r: " + frame[i] + ", i:" + im[i]);
//        }

        re = frame;
        ima = im;
        rer = framer;
        imar = imr;
        reg = frameg;
        imag = img;
        reb = frameb;
        imab = imb;
    }
    private float[] re, ima;
    private float[] rer, imar;
    private float[] reg, imag;
    private float[] reb, imab;

    public float[] re() {
        return re;
    }

    public float[] im() {
        return ima;
    }

    public float[] rer() {
        return rer;
    }

    public float[] imr() {
        return imar;
    }

    public float[] reg() {
        return reg;
    }

    public float[] img() {
        return imag;
    }

    public float[] reb() {
        return reb;
    }

    public float[] imb() {
        return imab;
    }

    public PVector getFreq() {
        return findFreq(re, ima);
    }

    public PVector getFreqR() {
        return findFreq(rer, imar);
    }

    public PVector getFreqG() {
        return findFreq(reg, imag);
    }

    public PVector getFreqB() {
        return findFreq(reb, imab);
    }

    public PVector findFreq(float[] re, float im[]) {
        float max = 0;
        int id = 0;
        for (int i = 2; i < re.length / 2; i++) {
            float v = sqrt(re[i] * re[i] + im[i] * im[i]);
            if (v > max) {
                max = v;
                id = i;
            }
        }
//        System.out.println("id: " + id + " max " + max);
        // -> frameRate * 2  because 60 FPS rendering !
        float f = +(float) id / (float) fftSize * (float) frameRate;
        return new PVector(f, max);
    }

    public void recordBlinkRate() {
        this.computeColor();

        // save light amount
        blinkFrames[currentBlinkIndex] = (float) (avgRed + avgBlue + avgGreen) / (3f * 255f);
//        blinkFrames[currentBlinkIndex++] = (float)(avgRed) / (255f);
        blinkFramesr[currentBlinkIndex] = (float) (avgRed) / (255f);
        blinkFramesg[currentBlinkIndex] = (float) (avgGreen) / (255f);
        blinkFramesb[currentBlinkIndex] = (float) (avgBlue) / (255f);

        currentBlinkIndex++;

        // check bounds
        if (currentBlinkIndex >= blinkFrames.length) {
            currentBlinkIndex = 0;
        }
    }
    
    

    public PVector getCaptureOffset() {
        return captureOffset;
    }

    /**
     * Set the position of the capture in millimeter.
     *
     * @param captureOffset
     */
    public void setCaptureOffset(PVector captureOffset) {
        this.captureOffset = captureOffset;
    }

    /**
     * Set the position of the capture in millimeter.
     *
     * @param x
     * @param y
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
