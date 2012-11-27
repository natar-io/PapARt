/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class RawKinect {

    PApplet parent;
    Vec3D[] kinectPoints;
    int[] colorPoints;
    boolean[] validPoints;
    int[] depth;
    PImage colorImage;
    PImage validPointsImage;

    public RawKinect(PApplet parent) {
        this.parent = parent;
        KinectCst.initKinect();
        kinectPoints = new Vec3D[KinectCst.w * KinectCst.h];
        colorPoints = new int[KinectCst.w * KinectCst.h];
        validPoints = new boolean[KinectCst.w * KinectCst.h];

        validPointsImage = parent.createImage(KinectCst.w, KinectCst.h, PConstants.RGB);
    }

    public void update(int[] depth, PImage color) {
        this.depth = depth;
        this.colorImage = color;

        compute3DPos();
    }

    protected void compute3DPos() {

        colorImage.loadPixels();
        validPointsImage.loadPixels();

        for (int y = 0, i = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {
                int offset = (x + y * KinectCst.w);

                boolean good = isGoodDepth(depth[x + y * KinectCst.w]);
                validPoints[offset] = good;

                if (good) {
                    kinectPoints[offset] = KinectCst.depthToWorld(x, y, depth[offset]);
                    colorPoints[offset] = KinectCst.WorldToColor(kinectPoints[offset]);
                    validPointsImage.pixels[offset] = colorImage.pixels[colorPoints[offset]];
                } else {

                    validPointsImage.pixels[offset] = parent.color(0, 0, 255);
                }
            }
        }

        validPointsImage.updatePixels();
    }

    public PImage getDepthColor() {
        return validPointsImage;
    }

    public PVector findCloseColor(int c1, float error) {

        colorImage.loadPixels();

        int meanX = 0;
        int meanY = 0;
        int count = 0;

        for (int i = 0; i < KinectCst.w * KinectCst.h; i++) {
            if (!validPoints[i]) {
                continue;
            }

//            int c2 = colorImage.pixels[(int) (colorPoints[i].y * KinectCst.w + colorPoints[i].x)];

            int c2 = validPointsImage.pixels[i];
//            parent.hue(c);
//            Vec3D c2 = new Vec3D(
//                    (colorProcessing >> 16 & 0xFF) / 255f,
//                    (colorProcessing >> 8 & 0xFF) / 255f,
//                    ((colorProcessing & 0xFF)) / 255f);
            float hueDiff = parent.abs(parent.hue(c1) - parent.hue(c2));
            float intensDiff = parent.abs(parent.brightness(c1) - parent.brightness(c2));
             float saturationDiff = parent.abs(parent.saturation(c1) - parent.saturation(c2));
            
            
            
            // Check the hue difference
            if (hueDiff + intensDiff + saturationDiff < error) {
                System.out.println("Diffs: " +  hueDiff + " " + intensDiff + " "+ saturationDiff );
                int x = i % KinectCst.w;
                int y = i / KinectCst.w;
                meanX += x;
                meanY += y;
                count++;
            }
        }

        if (count < 10) {
            return null;
        }

        return new PVector(meanX / count, meanY / count, count);
    }

// TODO: better depth test... 
    private boolean isGoodDepth(int rawDepth) {
        return (rawDepth >= 200 && rawDepth < 800);
    }
}
