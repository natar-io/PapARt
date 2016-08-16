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
package fr.inria.papart.scanner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import processing.core.PApplet;
import static processing.core.PApplet.ceil;
import static processing.core.PApplet.floor;
import static processing.core.PApplet.log;
import static processing.core.PApplet.pow;
import processing.core.PConstants;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author Jeremy Laviole
 */
public class GrayCode implements PConstants {

    public static final int DECODE_NOT_SET = 0;
    public static final int DECODE_REF = 1;
    public static final int DECODE_ABS = 2;

//    static public float differenceThreshold = 120;
    private final PApplet parent;

    // The user sets them
    private final PImage[] grayCodesCaptures;
    private PImage refImage = null;

    private final int nbCols;
    private final int nbRows;

    private final int colShift;
    private final int rowShift;

    private final int nbCodes;

    // Image parameters (to project)
    private final int width;
    private final int height;
    private final int displayWidth;
    private final int displayHeight;

    // Camera resolution (the one which observes the gray Code)
    private int cameraResX;
    private int cameraResY;

    DecodedCode decodedCode;
    boolean isDecoded = false;

    // TODO: rename
    private final int downScale;

    private int blackColor = 0, whiteColor = 255;

    public GrayCode(PApplet applet, int width, int height, int downScale) {
        this.parent = applet;
        this.width = width / downScale;
        this.height = height / downScale;
        this.displayWidth = width;
        this.displayHeight = height;
        this.downScale = downScale;

        nbCols = (int) ceil(log2(width));
        colShift = (int) floor((pow(2.0f, nbCols) - width) / 2);

        nbRows = (int) ceil(log2(height));
        rowShift = (int) floor((pow(2.0f, nbRows) - height) / 2);

        nbCodes = nbCols + nbRows + 2;
        grayCodesCaptures = new PImage[nbCodes];
    }

    public void setBlackWhiteColors(int black, int white) {
        this.blackColor = black;
        this.whiteColor = white;
    }

    public int nbCodes() {
        return this.nbCodes;
    }

    public boolean isDecoded() {
        return this.isDecoded;
    }

    public void reset() {
        this.isDecoded = false;
    }

    public void setRefImage(PImage img) {
        this.refImage = img;
    }

    public void addCapture(PImage img, int num) {
        cameraResX = img.width;
        cameraResY = img.height;
        grayCodesCaptures[num] = img;
    }

    private float log2(float x) {
        return log(x) / log(2);
    }

//    void display(PGraphicsOpenGL pg) {
//        display(pg, this.displayId);
//        this.displayId += 1;
//        this.displayId = this.displayId % nbCodes;
//    }
    /**
     * *
     * Render the gray code in the graphics pg.
     *
     * @param pg
     * @param id
     */
    public void display(PGraphicsOpenGL pg, int id) {

//        assert(pg.width == this.width);
//        assert(pg.height == this.height);
        pg.fill(blackColor);
        pg.noStroke();
        pg.rectMode(CORNER);
//        pg.rect(0, 0, displayWidth, displayHeight);

        if (id < nbCols) {
            drawCols(pg, id);
            return;
        }

        id -= nbCols;

        if (id < nbRows) {
            drawRows(pg, id);
            return;
        }

        id -= nbRows;

//        System.out.println("NbCols " + nbCols + " nbRows " + nbRows + " id " + id);
        if (id == 0) {
            pg.fill(blackColor);
            pg.noStroke();
            pg.rectMode(CORNER);
            pg.rect(0, 0, displayWidth, displayHeight);
        }
    }

    private void drawCols(PGraphicsOpenGL pg, int i) {
        for (int c = 0; c < width; c++) {
            int binary;
            if (i > 0) {
                binary = (((c + colShift) >> (nbCols - i - 1)) & 1) ^ (((c + colShift) >> (nbCols - i)) & 1);
            } else {
                binary = (((c + colShift) >> (nbCols - i - 1)) & 1);
            }
            pg.fill(binary == 0 ? blackColor : whiteColor);
            pg.rect(c * downScale, 0, c * downScale + downScale, displayHeight);
        }
    }

    private void drawRows(PGraphicsOpenGL pg, int i) {

        for (int r = 0; r < height; r++) {
            int binary;
            if (i > 0) {
                binary = (((r + rowShift) >> (nbRows - i - 1)) & 1) ^ (((r + rowShift) >> (nbRows - i)) & 1);
            } else {
                binary = (((r + rowShift) >> (nbRows - i - 1)) & 1);
            }
            pg.fill(binary == 0 ? blackColor : whiteColor);
//                pg.rect(0, r, width, r + 1);
            pg.rect(0, r * downScale, displayWidth, r * downScale + downScale);
        }
    }

    /**
     * *NOT TESTED -- TODO Generate the gray codes as a list of images to
     * display.
     *
     * @return Array of the images to display
     */
    public PImage[] generateGrayCodeImages() {

        // Allocate Gray codes.
        PImage[] grayCodeImages = new PImage[nbCodes];

        for (int i = 0; i < grayCodeImages.length; i++) {
            grayCodeImages[i] = parent.createImage(width, height, RGB);
        }

        // set the first image 
        {
            grayCodeImages[0] = parent.createImage(width, height, RGB);
            grayCodeImages[0].loadPixels();
            int[] px = grayCodeImages[0].pixels;
            for (int i = 0; i < px.length; i++) {
                px[i] = parent.color(255);
            }
            grayCodeImages[0].updatePixels();
        }

        // // Define Gray codes for projector columns.
        for (int c = 0; c < width; c++) {
            for (int i = 0; i < nbCols; i++) {

                int binary = 0;

                if (i > 0) {
                    binary = (((c + colShift) >> (nbCols - i - 1)) & 1) ^ (((c + colShift) >> (nbCols - i)) & 1);
                } else {
                    binary = (((c + colShift) >> (nbCols - i - 1)) & 1);
                }

                if (binary == 1) {
                    binary = parent.color(255);
                }

                PImage img = grayCodeImages[i + 1];
                img.loadPixels();
                int[] px = img.pixels;
                for (int r = 0; r < height; r++) {
                    px[r * width + c] = binary;
                }
                img.updatePixels();
            }
        }

        // Define Gray codes for projector rows.
        for (int r = 0; r < height; r++) {
            for (int i = 0; i < nbRows; i++) {

                int binary;

                if (i > 0) {
                    binary = (((r + rowShift) >> (nbRows - i - 1)) & 1) ^ (((r + rowShift) >> (nbRows - i)) & 1);
                } else {
                    binary = (((r + rowShift) >> (nbRows - i - 1)) & 1);
                }

                if (binary == 1) {
                    binary = parent.color(255);
                }

                PImage img = grayCodeImages[i + nbCols + 1];
                img.loadPixels();
                int[] px = img.pixels;
                for (int c = 0; c < width; c++) {
                    px[r * width + c] = binary;
                }

                img.updatePixels();
            }
        }

        return grayCodeImages;
    }

    private int mode;
    private int threshold;

    public void decode(int mode, int threshold) {
        this.mode = mode;
        this.threshold = threshold;

        decodeImpl();
    }

    public PImage getImageDecoded(int imageId, int mode, int differenceThreshold) {

        PImage out = parent.createImage(cameraResX, cameraResY, RGB);
        out.loadPixels();
        this.mode = mode;
        this.threshold = differenceThreshold;

        for (int y = 0; y < cameraResY; y += 1) {
            for (int x = 0; x < cameraResX; x += 1) {
                int offset = x + y * cameraResX;
                boolean newValue = decodePixel(imageId, offset);

                // TODO: bug here ?!
//                if (mode == GrayCode.DECODE_REF) {
//                    newValue = !newValue;
//                }
                out.pixels[offset] = newValue ? 255 : 0;
            }
        }

        return out;

    }

    public PImage getProjectorImage() {
        assert (isDecoded());
        return decodedCode.getProjectorImage(parent, displayWidth, displayHeight);
    }

    public PImage getProjectorImageScaled(int scale) {
        assert (isDecoded());
        return decodedCode.getProjectorImageScaled(parent, displayWidth, displayHeight, scale);
    }

    protected void decodeImpl() {
        convertImagesToGray();
        this.decodedCode = new DecodedCode(cameraResX, cameraResY);
        decodedCode.setRefImage(this.refImage);

        boolean[] validMask = decodedCode.validMask;
        int[] decodedCameraX = decodedCode.decodedX;
        int[] decodedCameraY = decodedCode.decodedY;

        for (int y = 0; y < cameraResY; y += 1) {
            for (int x = 0; x < cameraResX; x += 1) {

                int offset = x + y * cameraResX;

                validMask[offset] = false;

                decodeColumns(offset);
                decodeRows(offset);

                decodedCameraX[offset] = (decodedCameraX[offset] - colShift) * downScale;
                decodedCameraY[offset] = (decodedCameraY[offset] - rowShift) * downScale;

                if (decodedCameraX[offset] >= displayWidth
                        || decodedCameraY[offset] >= displayHeight
                        || decodedCameraX[offset] < 0
                        || decodedCameraY[offset] < 0) {
                    validMask[offset] = false;
                }
            }
        }
        isDecoded = true;

    }

    void decodeColumns(int offset) {

        boolean prevValue = false;

        for (int i = 0; i < nbCols; i++) {
            boolean newValue = decodePixel(i, offset);

            decodedCode.validMask[offset] |= newValue;
            newValue = newValue ^ prevValue;
            prevValue = newValue;
            if (newValue) {
                decodedCode.decodedX[offset] += pow(2f, (nbCols - i - 1));
            }
        }
    }

    void decodeRows(int offset) {

        boolean prevValue = false;

        for (int i = 0; i < nbRows; i++) {
            boolean newValue = decodePixel(i + nbCols, offset);

            decodedCode.validMask[offset] |= newValue;
            newValue = newValue ^ prevValue;
            prevValue = newValue;

            if (newValue) {
                decodedCode.decodedY[offset] += pow(2f, (nbRows - i - 1));
            }
        }
    }

    private boolean decodePixel(int imageId, int offset) {
        if (mode == GrayCode.DECODE_ABS) {
            return decodePixelAbs(imageId, offset);
        } else {
            if (mode == GrayCode.DECODE_REF) {
                return decodePixelRef(imageId, offset);
            } else {
                // TODO: error !
                assert (true);
                return false;
            }
        }
    }

    private boolean decodePixelAbs(int imageId, int offset) {
        return parent.brightness(grayCodesCaptures[imageId].pixels[offset]) > threshold;
    }

    private boolean decodePixelRef(int imageId, int offset) {
        float referenceLight = parent.brightness(refImage.pixels[offset]);
        float currentLight = parent.brightness(grayCodesCaptures[imageId].pixels[offset]);
        return (Math.abs(currentLight - referenceLight) > threshold);
    }

    private void convertImagesToGray() {
        for (int i = 0; i < nbCodes; i++) {
            grayCodesCaptures[i].filter(GRAY);
            grayCodesCaptures[i].loadPixels();
        }

        if (refImage != null) {
            refImage.filter(GRAY);
            refImage.loadPixels();
        } else {
            refImage = grayCodesCaptures[nbCodes - 1];
        }

    }

    // TODO: better than this.
    public int[] decodedX() {
        assert (this.isDecoded());
        return this.decodedCode.decodedX;
    }

    public int[] decodedY() {
        assert (this.isDecoded());
        return this.decodedCode.decodedY;
    }

    public boolean[] mask() {
        assert (this.isDecoded());
        return this.decodedCode.validMask;
    }

    public void save(String path) {

        decodedCode.saveTo(parent, path);
//        try {
//            this.grayCodesCaptures = null;
//            this.refImage = null;
//            this.parent = null;
//            FileOutputStream fileOut = new FileOutputStream(path);
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//            out.writeObject(this);
//            out.close();
//            fileOut.close();
//        } catch (IOException i) {
//            i.printStackTrace();
//        }
    }

    public static GrayCode load(String path) {

        GrayCode grayCode = null;
        try {
            FileInputStream fileIn = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            grayCode = (GrayCode) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return null;
        }

        return grayCode;
    }

}
