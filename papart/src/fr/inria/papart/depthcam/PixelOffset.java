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
    public static int width;

    public PixelOffset(int x, int y, int offset) {
        this.x = x;
        this.y = y;
        this.offset = offset;
    }

    private static PixelOffset[] offsets;

    public static void initStaticMode(int width, int height) {
        offsets = new PixelOffset[width * height];
        int off = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                offsets[off] = new PixelOffset(x, y, off);
                off++;
            }
        }
    }

    public static PixelOffset get(int offset) {
        return offsets[offset];
    }

}
