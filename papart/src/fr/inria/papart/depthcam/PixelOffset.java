/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam;

/**
 *
 * @author jiii
 */
public class PixelOffset {

    public final int x, y, offset;

    public PixelOffset(int x, int y, int offset) {
        this.x = x;
        this.y = y;
        this.offset = offset;
    }
}
