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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import static processing.core.PConstants.RGB;
import processing.core.PImage;
import static org.bytedeco.javacpp.opencv_highgui.*;

/**
 *
 * @author Jeremy Laviole jeremy.laviole@inria.fr
 */
public class DecodedCode implements Serializable {

    private static final String EXTENSION_BYTE = ".dat";
    private static final String EXTENSION_IMG = ".bmp";
    private static final String SEPARATION = "-";
    private static final String X_NAME = "decodedX";
    private static final String Y_NAME = "decodedY";
    private static final String REF_NAME = "ref";
    private static final String MASK_NAME = "mask";

    protected PImage refImage;
//    protected IplImage refImageIpl;

    protected boolean[] validMask;

    // Rename to decoded CameraX 
    protected int[] decodedX;
    protected int[] decodedY;

    // TODO: Create the same for projector view !
    private int width, height;

    private PApplet applet;

    public DecodedCode(int width, int height) {
        this.width = width;
        this.height = height;

        validMask = new boolean[width * height];
        decodedX = new int[width * height];
        decodedY = new int[width * height];
    }

    private DecodedCode() {
    }

    public void setRefImage(PImage refImage) {
        this.refImage = refImage;
    }

    public PImage getRefImage() {
        return this.refImage;
    }

//    public opencv_core.IplImage getRefImageIpl(){
//        return this.refImageIpl;
//    }
    public int[] getDecodedX() {
        return decodedX;
    }

    public int[] getDecodedY() {
        return decodedY;
    }

    public boolean[] getMask() {
        return validMask;
    }

    public PImage getProjectorImage(PApplet applet, int projWidth, int projHeight) {

        PImage projectorImage = applet.createImage(projWidth, projHeight, RGB);
        projectorImage.loadPixels();
        refImage.loadPixels();

        int imSize = width * height;
        for (int i = 0; i < imSize; i++) {
            if (validMask[i]) {
                int x = decodedX[i];
                int y = decodedY[i];

                int offset = y * projWidth + x;
                projectorImage.pixels[offset] = refImage.pixels[i];
            }
        }

        projectorImage.updatePixels();
        return projectorImage;
    }

    public PImage getProjectorImageScaled(PApplet applet, int projWidth, int projHeight, int precision) {

        PImage projectorImage = applet.createImage(projWidth / precision, projHeight / precision, RGB);
        projectorImage.loadPixels();
        refImage.loadPixels();

        int imSize = width * height;
        for (int i = 0; i < imSize; i++) {
            if (validMask[i]) {
                int x = decodedX[i];
                int y = decodedY[i];

                x = x / precision;
                y = y / precision;
                int offset = y * projWidth / precision + x;
                projectorImage.pixels[offset] = refImage.pixels[i];
            }
        }

        projectorImage.updatePixels();
        return projectorImage;
    }

    public static DecodedCode loadFrom(PApplet applet, String fileName) {
        DecodedCode decodedCode = new DecodedCode();
        decodedCode.refImage = applet.loadImage(fileName + SEPARATION + REF_NAME + EXTENSION_IMG);

//        String filePath = applet.sketchPath + "/"+ fileName + SEPARATION + REF_NAME + EXTENSION_IMG;
//        System.out.println("Loading .. " + filePath);
//        decodedCode.refImageIpl = cvLoadImage(filePath);
        decodedCode.width = decodedCode.refImage.width;
        decodedCode.height = decodedCode.refImage.height;

        decodedCode.applet = applet;

        decodedCode.decodedX = decodedCode.loadIntArray(fileName + SEPARATION + X_NAME + EXTENSION_BYTE);
        decodedCode.decodedY = decodedCode.loadIntArray(fileName + SEPARATION + Y_NAME + EXTENSION_BYTE);
        decodedCode.validMask = decodedCode.loadBoolArray(fileName + SEPARATION + MASK_NAME + EXTENSION_BYTE, null);
        return decodedCode;
    }

    private int[] loadIntArray(String name) {
        byte[] byteArr = applet.loadBytes(name);
        IntBuffer bb = java.nio.ByteBuffer
                .wrap(byteArr)
                .order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer();
        int[] array = new int[bb.remaining()];
        bb.get(array);
        return array;
    }

    private boolean[] loadBoolArray(String name, boolean[] array) {

        byte[] byteArr = applet.loadBytes(name);

        if (array == null || array.length != byteArr.length) {
            array = new boolean[byteArr.length];
        }

        for (int i = 0; i < byteArr.length; i++) {
            array[i] = byteArr[i] > 0;
        }
        return array;
    }

    private String fileName;

    public void saveTo(PApplet applet, String fileName) {

        this.applet = applet;
        this.fileName = fileName;

        saveRef();
        saveDecoded();
        saveMask();
    }

    private void saveRef() {
        refImage.save(fileName + SEPARATION + REF_NAME + EXTENSION_IMG);
    }

    private void saveDecoded() {
        byte[] decX = createByteArrayFrom(decodedX);
        applet.saveBytes(fileName + SEPARATION + X_NAME + EXTENSION_BYTE, decX);

        byte[] decY = createByteArrayFrom(decodedY);
        applet.saveBytes(fileName + SEPARATION + Y_NAME + EXTENSION_BYTE, decY);
    }

    private void saveMask() {
        byte[] mask = createByteArrayFrom(validMask);
        applet.saveBytes(fileName + SEPARATION + MASK_NAME + EXTENSION_BYTE, mask);
    }


    private byte[] createByteArrayFrom(int[] array) {
        ByteBuffer bb = java.nio.ByteBuffer.allocate(array.length * Integer.SIZE / 8);
        IntBuffer ib = IntBuffer.wrap(array);
        bb.asIntBuffer().put(ib);
        return bb.array();
    }

    private byte[] createByteArrayFrom(boolean[] array) {
        byte[] output = new byte[array.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = array[i] ? (byte) 1 : (byte) 0;
        }
        return output;
    }
    
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
