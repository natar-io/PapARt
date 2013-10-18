/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.imanalysis;

import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class Pixel implements IPixel {

    private int px, py;
    private PImage image;

    public Pixel(PImage image, int px, int py) {
        this.image = image;
        this.px = px;
        this.py = py;
    }

    @Override
    public float hue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float saturation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float brightness() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float red() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float green() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float blue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float X() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float Y() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
