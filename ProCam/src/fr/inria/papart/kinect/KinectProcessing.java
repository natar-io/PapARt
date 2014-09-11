/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import org.bytedeco.javacpp.opencv_core;
import static fr.inria.papart.kinect.Kinect.isInside;
import static fr.inria.papart.kinect.Kinect.papplet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectProcessing extends Kinect {

    public PImage validPointsPImage;

    public KinectProcessing(PApplet parent, String calibIR, String calibRGB, int id, int mode) {
        super(parent, calibIR, calibRGB, id, mode);
    }

    protected void init() {
        super.init();
        validPointsPImage = papplet.createImage(kinectCalibIR.getWidth(), kinectCalibIR.getHeight(), PConstants.RGB);
    }

    public PImage update(opencv_core.IplImage depth, opencv_core.IplImage color) {
        return update(depth, color, 1);
    }

    public PImage update(opencv_core.IplImage depth, opencv_core.IplImage color, int skip) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];
                boolean good = isGoodDepth(d);
                depthData.validPointsMask[offset] = good;

                validPointsPImage.pixels[offset] = papplet.color(0, 0, 255);
                if (good) {
                    depthData.kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);

                    int colorOffset = this.findColorOffset(depthData.kinectPoints[offset]) * 3;

                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
                            | (colorRaw[colorOffset + 0] & 0xFF);

                    validPointsPImage.pixels[offset] = c;

//                    int colorOffset = offset * 3;
//                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                            | (colorRaw[colorOffset + 0] & 0xFF);
//
//                    validPointsPImage.pixels[offset] = c;
                }

            }
        }

        Arrays.fill(connexity, 0);
        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {
                int offset = y * kinectCalibIR.getWidth() + x;
                if (depthData.validPointsMask[offset]) {
                    computeConnexity(x, y, skip);
                }
            }
        }

        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage update(opencv_core.IplImage depth, opencv_core.IplImage color, KinectScreenCalibration calib) {
        return update(depth, color, 1, calib);
    }

    public PImage update(opencv_core.IplImage depth, opencv_core.IplImage color, int skip, KinectScreenCalibration calib) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        Arrays.fill(depthData.validPointsMask, false);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                depthData.validPointsMask[offset] = false;
                validPointsPImage.pixels[offset] = papplet.color(0, 0, 255);

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    depthData.kinectPoints[offset] = p;
//                    colorPoints[offset] = this.findColorOffset(p);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        if (isInside(calib.project(p), 0.f, 1.f, 0.1f)) {

                            depthData.kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);

                            int colorOffset = this.findColorOffset(depthData.kinectPoints[offset]) * 3;

                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                                    | (colorRaw[colorOffset + 0] & 0xFF);

                            validPointsPImage.pixels[offset] = c;

//                            int colorOffset = offset * 3;
//                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                                    | (colorRaw[colorOffset + 0] & 0xFF);
//                            validPointsPImage.pixels[offset] = c;
                            depthData.validPointsMask[offset] = true;
                        }
                    }
                }

            }

        }

        Arrays.fill(connexity, 0);
        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {
                int offset = y * kinectCalibIR.getWidth() + x;
                if (depthData.validPointsMask[offset]) {
                    computeConnexity(x, y, skip);
                }
            }
        }

        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    /**
     * TODO: CHECK This function, what was the purpose ?! Why IPL & PImage...
     *
     * @param depth
     * @param color
     * @param calib
     * @param projectedPoints
     * @param skip
     * @return
     * @deprecated
     */
    @Deprecated
    public PImage updateProj(opencv_core.IplImage depth, opencv_core.IplImage color, KinectScreenCalibration calib, Vec3D[] projectedPoints, int skip) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                validPointsPImage.pixels[offset] = papplet.color(0, 0, 255);
                depthData.validPointsMask[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    depthData.kinectPoints[offset] = p;
//                    colorPoints[offset] = this.findColorOffset(p);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(project, 0.f, 1.f, 0.0f)) {

                            depthData.kinectPoints[offset] = p;
                            depthData.validPointsMask[offset] = true;
                            // Projection
                            projectedPoints[offset] = project;

//                            int colorOffset = colorPoints[offset] * 3;
//                            int colorOffset = offset * 3;
//
//                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                                    | (colorRaw[colorOffset + 0] & 0xFF);
//
//                            validPointsPImage.pixels[offset] = c;
                            depthData.kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
                            
                            int colorOffset = this.findColorOffset(depthData.kinectPoints[offset]) * 3;
                            
                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                                    | (colorRaw[colorOffset + 0] & 0xFF);

                            validPointsPImage.pixels[offset] = c;

                        }
                    }
                }
            }
        }

        validPointsPImage.updatePixels();

        return validPointsPImage;
    }

    public PImage getDepthColor() {
        return validPointsPImage;
    }

}
