/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
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
    PImage colorImage;
    PImage validPointsPImage;
    byte[] depthRaw;
    byte[] colorRaw;
    byte[] validPointsRaw;
    IplImage validPointsIpl;
    
    public float closeThreshold = 300f, farThreshold = 800f;

    // TODO: Too many allocations ?
    // TODO: release all this sometime ?
    public RawKinect(PApplet parent) {
        this.parent = parent;
        KinectCst.initKinect();
        kinectPoints = new Vec3D[KinectCst.w * KinectCst.h];
        colorPoints = new int[KinectCst.w * KinectCst.h];
        validPoints = new boolean[KinectCst.w * KinectCst.h];

        validPointsPImage = parent.createImage(KinectCst.w, KinectCst.h, PConstants.RGB);


        colorRaw = new byte[KinectCst.w * KinectCst.h * 3];
        depthRaw = new byte[KinectCst.w * KinectCst.h * 2];

        // TODO: look for faster methods ?
        validPointsIpl = IplImage.create(new CvSize(KinectCst.w, KinectCst.h), opencv_core.IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[KinectCst.w * KinectCst.h * 3];

    }

    // TODO: use Calibration files etc...
    public PImage updateP(IplImage depth, IplImage color) {

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        int off = 0;
        for (int y = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {

                int offset = off++;

                int d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                boolean good = isGoodDepth(d);
                validPoints[offset] = good;

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);

                if (good) {
                    kinectPoints[offset] = KinectCst.depthToWorld(x, y, d);
                    colorPoints[offset] = KinectCst.WorldToColor(x, y, kinectPoints[offset]);

                    int colorOffset = colorPoints[offset] * 3;
                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
                            | (colorRaw[colorOffset + 0] & 0xFF);

                    validPointsPImage.pixels[offset] = c;
                }

            }
        }

        validPointsPImage.updatePixels();

        return validPointsPImage;
    }

    public IplImage updateIpl(IplImage depth, IplImage color) {

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);
        colorBuff.get(colorRaw);

        int off = 0;
        for (int y = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {

                int offset = off++;
                int outputOffset = offset * 3;
                
                int d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                boolean good = isGoodDepth(d);
                validPoints[offset] = good;

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);

                if (good) {
                    kinectPoints[offset] = KinectCst.depthToWorld(x, y, d);
                    colorPoints[offset] = KinectCst.WorldToColor(x, y, kinectPoints[offset]);

                    int colorOffset = colorPoints[offset] * 3;


                    validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
                    validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
                    validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];

                } else {
                    validPointsRaw[outputOffset + 2] = 0;
                    validPointsRaw[outputOffset + 1] = 0;
                    validPointsRaw[outputOffset + 0] = 0;

                }
            }
        }

        outputBuff = (ByteBuffer) outputBuff.rewind();
        outputBuff.put(validPointsRaw);

        return validPointsIpl;
    }

    public PImage getDepthColor() {
        return validPointsPImage;
    }

    public boolean[] getValidPoints() {
        return validPoints;
    }

    public Vec3D[] getDepthPoints(){
        return kinectPoints;
    }
    
    // TODO: remove this !
    public PVector findCloseColor(int c1, float error) {

//        colorImage.loadPixels();

        int meanX = 0;
        int meanY = 0;
        int count = 0;

        for (int i = 0; i < KinectCst.w * KinectCst.h; i++) {
            if (!validPoints[i]) {
                continue;
            }

//            int c2 = colorImage.pixels[(int) (colorPoints[i].y * KinectCst.w + colorPoints[i].x)];

            int c2 = validPointsPImage.pixels[i];
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
                System.out.println("Diffs: " + hueDiff + " " + intensDiff + " " + saturationDiff);
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
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }
}
