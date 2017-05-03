/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.utils;

import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class SimpleSize implements WithSize {

    int w, h;

    public SimpleSize(PVector v) {
        this((int) v.x,(int)  v.y);
    }

    public SimpleSize(int w, int h) {
        this.w = w;
        this.h = h;
    }

    @Override
    public int getSize() {
        return w * h;
    }

    @Override
    public int getWidth() {
        return w;
    }

    @Override
    public int getHeight() {
        return h;
    }

}
