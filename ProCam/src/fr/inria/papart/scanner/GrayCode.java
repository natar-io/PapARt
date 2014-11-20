/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.scanner;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
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
 * @author jiii
 */
public class GrayCode implements PConstants, Serializable {

    public static final int DECODE_NOT_SET = 0;
    public static final int DECODE_REF = 1;
    public static final int DECODE_ABS = 2;

//    static public float differenceThreshold = 120;
    private PApplet parent;

    // The user sets them
    private PImage[] grayCodesCaptures;
    private PImage refImage = null;

    private final int numCols;
    private final int numRows;

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
    private boolean[] validMask;
    int[] decodedX;
    int[] decodedY;
    boolean isDecoded = false;

    // TODO: rename
    private final int downScale;

    private int displayId = 0;

    public GrayCode(PApplet applet, int width, int height, int downScale) {
        this.parent = applet;
        this.width = width / downScale;
        this.height = height / downScale;
        this.displayWidth = width;
        this.displayHeight = height;
        this.downScale = downScale;

        numCols = (int) ceil(log2(width));
        colShift = (int) floor((pow(2.0f, numCols) - width) / 2);

        numRows = (int) ceil(log2(height));
        rowShift = (int) floor((pow(2.0f, numRows) - height) / 2);

        nbCodes = numCols + numRows + 1;
        grayCodesCaptures = new PImage[nbCodes];
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
        if (validMask == null) {
            validMask = new boolean[img.width * img.height];
            cameraResX = img.width;
            cameraResY = img.height;
        }
        grayCodesCaptures[num] = img;
    }

    private float log2(float x) {
        return log(x) / log(2);
    }

    void initGrayCodeDisplay() {
        this.displayId = 0;
    }

    void display(PGraphicsOpenGL pg) {
        display(pg, this.displayId);
        this.displayId += 1;
        this.displayId = this.displayId % nbCodes;
    }

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
        pg.background(0);
        pg.noStroke();
        pg.rectMode(CORNER);

        if (id == 0) {
            pg.background(255);
            return;
        }

        if (id <= numCols) {
            int i = id - 1;

            // // Define Gray codes for projector columns.
            for (int c = 0; c < width; c++) {
                int binary;
                if (i > 0) {
                    binary = (((c + colShift) >> (numCols - i - 1)) & 1) ^ (((c + colShift) >> (numCols - i)) & 1);
                } else {
                    binary = (((c + colShift) >> (numCols - i - 1)) & 1);
                }
                pg.fill(binary == 0 ? 0 : 255);
                pg.rect(c * downScale, 0, c * downScale + downScale, displayHeight);
            }
        } else {

            int i = id - numCols - 1;
            for (int r = 0; r < height; r++) {
                int binary;
                if (i > 0) {
                    binary = (((r + rowShift) >> (numRows - i - 1)) & 1) ^ (((r + rowShift) >> (numRows - i)) & 1);
                } else {
                    binary = (((r + rowShift) >> (numRows - i - 1)) & 1);
                }
                pg.fill(binary == 0 ? 0 : 255);
//                pg.rect(0, r, width, r + 1);
                pg.rect(0, r * downScale, displayWidth, r * downScale + downScale);
            }
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
            for (int i = 0; i < numCols; i++) {

                int binary = 0;

                if (i > 0) {
                    binary = (((c + colShift) >> (numCols - i - 1)) & 1) ^ (((c + colShift) >> (numCols - i)) & 1);
                } else {
                    binary = (((c + colShift) >> (numCols - i - 1)) & 1);
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
            for (int i = 0; i < numRows; i++) {

                int binary;

                if (i > 0) {
                    binary = (((r + rowShift) >> (numRows - i - 1)) & 1) ^ (((r + rowShift) >> (numRows - i)) & 1);
                } else {
                    binary = (((r + rowShift) >> (numRows - i - 1)) & 1);
                }

                if (binary == 1) {
                    binary = parent.color(255);
                }

                PImage img = grayCodeImages[i + numCols + 1];
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
                if(mode == GrayCode.DECODE_REF)
                    newValue = !newValue;
                
                out.pixels[offset] = newValue ? 255 : 0;
            }
        }

        return out;

    }

    public void decodeImpl() {
        convertImagesToGray();
        prepareDecodeMemory();

        for (int y = 0; y < cameraResY; y += 1) {
            for (int x = 0; x < cameraResX; x += 1) {

                int offset = x + y * cameraResX;

                validMask[offset] = false;

                decodeColumns(offset);
                decodeRows(offset);

                decodedX[offset] = (decodedX[offset] - colShift) * downScale;
                decodedY[offset] = (decodedY[offset] - rowShift) * downScale;

                if (decodedX[offset] >= displayWidth || decodedY[offset] >= displayHeight) {
                    validMask[offset] = false;
                }
            }
        }
        isDecoded = true;

    }

    void decodeColumns(int offset) {

        boolean prevValue = false;

        for (int i = 0; i < numCols; i++) {
            boolean newValue = decodePixel(i + 1, offset);

            validMask[offset] |= newValue;
            newValue = newValue ^ prevValue;
            prevValue = newValue;
            if (newValue) {
                decodedX[offset] += pow(2f, (numCols - i - 1));
            }
        }
    }

    void decodeRows(int offset) {

        boolean prevValue = false;

        for (int i = 0; i < numRows; i++) {
            boolean newValue = decodePixel(i + numCols + 1, offset);

            validMask[offset] |= newValue;
            newValue = newValue ^ prevValue;
            prevValue = newValue;

            if (newValue) {
                decodedY[offset] += pow(2f, (numRows - i - 1));
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
        return (Math.abs(currentLight - referenceLight) < threshold);
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
            refImage = grayCodesCaptures[0];
        }

    }

    private void prepareDecodeMemory() {
        decodedX = new int[cameraResX * cameraResY];
        decodedY = new int[cameraResX * cameraResY];
        Arrays.fill(validMask, Boolean.FALSE);
    }

    public void decodeRef(int differenceThreshold) {

        decodedX = new int[cameraResX * cameraResY];
        decodedY = new int[cameraResX * cameraResY];

        // Convert to grayscale
        for (int i = 0; i < nbCodes; i++) {
            grayCodesCaptures[i].filter(GRAY);
        }

        for (int i = 0; i < grayCodesCaptures.length; i++) {
            grayCodesCaptures[i].loadPixels();
        }

        if (refImage != null) {
            refImage.filter(GRAY);
            refImage.loadPixels();
        } else {
            refImage = grayCodesCaptures[0];
        }
        Arrays.fill(validMask, Boolean.FALSE);

        for (int y = 0; y < cameraResY; y += 1) {
            for (int x = 0; x < cameraResX; x += 1) {

                // for(int y = halfSc; y < h; y+= sc) {
                // 	for(int x = halfSc; x < w; x+= sc) {
                boolean prevValue = false;
                int offset = x + y * cameraResX;

                validMask[offset] = false;

                float referenceLight = parent.brightness(refImage.pixels[offset]);

                for (int i = 0; i < numCols; i++) {
                    float currentLight = parent.brightness(grayCodesCaptures[i + 1].pixels[offset]);
                    boolean newValue = Math.abs(currentLight - referenceLight) > differenceThreshold;

                    validMask[offset] |= newValue;
                    newValue = newValue ^ prevValue;
                    prevValue = newValue;
                    if (newValue) {
                        decodedX[offset] += pow(2f, (numCols - i - 1));
                    }
                }

                prevValue = false;
                for (int i = 0; i < numRows; i++) {

                    float currentLight = parent.brightness(grayCodesCaptures[i + numCols + 1].pixels[offset]);
                    boolean newValue = Math.abs(currentLight - referenceLight) > differenceThreshold;

                    validMask[offset] |= newValue;

                    newValue = newValue ^ prevValue;
                    prevValue = newValue;

                    if (newValue) {
                        decodedY[offset] += pow(2f, (numRows - i - 1));
                    }
                }
                decodedX[offset] = (decodedX[offset] - colShift) * downScale;
                decodedY[offset] = (decodedY[offset] - rowShift) * downScale;

                if (decodedX[offset] >= displayWidth || decodedY[offset] >= displayHeight) {
                    validMask[offset] = false;
                }
            }
        }
        isDecoded = true;
    }

    // TODO: better than this.
    public int[] decodedX() {
        assert (this.isDecoded());
        return this.decodedX;
    }

    public int[] decodedY() {
        assert (this.isDecoded());
        return this.decodedY;
    }

    public boolean[] mask() {
        assert (this.isDecoded());
        return this.validMask;
    }

    public void save(String path) {
        try {
            this.grayCodesCaptures = null;
            this.refImage = null;
            this.parent = null;
            FileOutputStream fileOut = new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
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
