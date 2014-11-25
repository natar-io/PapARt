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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import processing.core.PApplet;
import static processing.core.PConstants.RGB;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
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

    public void setRefImage(PImage refImage) {
        this.refImage = refImage;
    }

    public PImage getProjectorImage(PApplet applet, int projWidth, int projHeight) {

        PImage projectorImage = applet.createImage(projWidth, projHeight, RGB);
        projectorImage.loadPixels();
        refImage.loadPixels();

        int imSize = width * height;
        for (int i = 0; i <  imSize; i++) {
            if (validMask[i]) {
                int x = decodedX[i];
                int y = decodedY[i];
                
                int offset = y * width + x;
                projectorImage.pixels[offset] = refImage.pixels[i];
            }
        }
        
        projectorImage.updatePixels();
        return projectorImage;
    }

    public void loadFrom(PApplet applet, String fileName) {
        this.applet = applet;
        refImage = applet.loadImage(fileName + SEPARATION + REF_NAME + EXTENSION_IMG);
        this.width = refImage.width;
        this.height = refImage.height;

        decodedX = loadIntArray(fileName + SEPARATION + X_NAME + EXTENSION_BYTE, decodedX);
        decodedY = loadIntArray(fileName + SEPARATION + Y_NAME + EXTENSION_BYTE, decodedY);
        validMask = loadBoolArray(fileName + SEPARATION + MASK_NAME + EXTENSION_BYTE, validMask);
    }

    private int[] loadIntArray(String name, int[] array) {
        byte[] byteArr = applet.loadBytes(name);
        IntBuffer bb = java.nio.ByteBuffer
                .wrap(byteArr)
                .order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer();
        array = new int[bb.remaining()];
        bb.get(array);
        return array;
    }

    private boolean[] loadBoolArray(String name, boolean[] array) {

        byte[] byteArr = applet.loadBytes(name);

        if (array.length != byteArr.length) {
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
        IntBuffer ib = IntBuffer.wrap(decodedX);
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

}
