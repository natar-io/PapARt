/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

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
public class Kinect {

    public static PApplet parent;
    public float closeThreshold = 300f, farThreshold = 800f;
    private Vec3D[] kinectPoints;
    private int[] colorPoints;
    private boolean[] validPoints;
    private PImage validPointsPImage;
    private byte[] depthRaw;
    private byte[] colorRaw;
    private byte[] validPointsRaw;
    private IplImage validPointsIpl;
    private int id;
    static float[] depthLookUp = null;

//  Kinect with the standard calibration
    public Kinect(PApplet parent, int id) {
        this.parent = parent;
        KinectCst.init(parent);
        init(id);
    }

    // Kinect with advanced calibration 
    // Not ready yet
//    public Kinect(PApplet parent, int id, String calibrationFile) {
//        init(id);
//    }
    private void init(int id) {
        this.id = id;


        // TODO: create them at first use !!
        kinectPoints = new Vec3D[KinectCst.size];
        validPoints = new boolean[KinectCst.size];

        colorRaw = new byte[KinectCst.size * 3];
        depthRaw = new byte[KinectCst.size * 2];

        // For Processing output
        colorPoints = new int[KinectCst.size];
        validPointsPImage = parent.createImage(KinectCst.w, KinectCst.h, PConstants.RGB);

        // For OpenCV Image output
        validPointsIpl = IplImage.create(new CvSize(KinectCst.w, KinectCst.h), opencv_core.IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[KinectCst.w * KinectCst.h * 3];

        if (depthLookUp == null) {
            depthLookUp = new float[2048];
            for (int i = 0; i < depthLookUp.length; i++) {
                depthLookUp[i] = rawDepthToMeters(i);
            }
        }
    }

    public int getId() {
        return this.id;
    }

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

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                d = 1000 * depthLookUp[(int) d];

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

    public PImage updateP(IplImage depth, IplImage color, KinectScreenCalibration calib) {

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        int off = 0;
        for (int y = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {

                int offset = off++;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                d = 1000 * depthLookUp[(int) d];

                boolean good = isGoodDepth(d);
               validPoints[offset] = false;

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);

                if (good) {

                    Vec3D p = KinectCst.depthToWorld(x, y, d);
                    kinectPoints[offset] = p;
                    colorPoints[offset] = KinectCst.WorldToColor(x, y, kinectPoints[offset]);


                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        if (isInside(calib.project(p), 0.f, 1.f, 0.05f)) {


                            int colorOffset = colorPoints[offset] * 3;
                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                                    | (colorRaw[colorOffset + 0] & 0xFF);

                            validPointsPImage.pixels[offset] = c;
                            
                              validPoints[offset] = true;
                        }
                    }
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

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                d = 1000 * depthLookUp[(int) d];

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

    public void updateMT(IplImage depth, KinectScreenCalibration calib) {

        ByteBuffer depthBuff = depth.getByteBuffer();

        depthBuff.get(depthRaw);

        int off = 0;
        for (int y = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {

                int offset = off++;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;

                if (isGoodDepth(d)) {
                    Vec3D p = KinectCst.depthToWorld(x, y, d);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {
                        kinectPoints[offset] = p;
                        validPoints[offset] = true;

                    }

                }
            }
        }
    }

    public void updateMT(IplImage depth, KinectScreenCalibration calib, byte[] validPoints2, Vec3D[] projectedPoints) {
        updateMT(depth, calib, validPoints2, projectedPoints, 1);
    }

    public ArrayList<Integer> updateMT(IplImage depth, KinectScreenCalibration calib, byte[] validPoints2, Vec3D[] projectedPoints, int skip) {

        ByteBuffer depthBuff = depth.getByteBuffer();
        ArrayList<Integer> points = new ArrayList<Integer>();
        depthBuff.get(depthRaw);

        int off = 0;
        for (int y = 0; y < KinectCst.h; y += skip) {
            for (int x = 0; x < KinectCst.w; x += skip) {

                int offset = off;
                off += skip;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;
                validPoints2[offset] = -1;

                if (isGoodDepth(d)) {
                    Vec3D p = KinectCst.depthToWorld(x, y, d);

                    validPoints2[offset] = 0;

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(calib.project(p), 0.f, 1.f, 0.05f)) {

                            kinectPoints[offset] = p;
                            validPoints[offset] = true;
                            validPoints2[offset] = 1;
                            points.add(offset);
                            // Projection
                            projectedPoints[offset] = project;
                        }
                    }
                }
            }

            off += KinectCst.w * (skip - 1);
        }

        return points;
    }

    public PImage getDepthColor() {
        return validPointsPImage;
    }

    public boolean[] getValidPoints() {
        return validPoints;
    }

    public Vec3D[] getDepthPoints() {
        return kinectPoints;
    }

    public static float rawDepthToMeters(int depthValue) {
        if (depthValue < 2047) {
            return (float) (1.0 / ((float) (depthValue) * -0.0030711016f + 3.3309495161f));
        }
        return 0.0f;
    }

    private boolean isGoodDepth(float rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
