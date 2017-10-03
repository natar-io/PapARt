/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016 Jérémy Laviole
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
package fr.inria.papart.depthcam.analysis;

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.PixelOffset;
import fr.inria.papart.depthcam.devices.DepthCameraDevice;
import fr.inria.papart.depthcam.devices.KinectOne;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.papplet;
import fr.inria.papart.procam.camera.Camera;
import java.util.ArrayList;
import java.util.Arrays;
import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class DepthAnalysisPImageView extends DepthAnalysisImpl {

    public PImage validPointsPImage;
    IplImage nativeArrayToErode;
    UByteIndexer erosionIndexer;
//    boolean[] validCopy;
    private TimeFilterDepth timeFilterDepth;

    public DepthAnalysisPImageView(PApplet parent, DepthCameraDevice kinect) {
        super(parent, kinect);
        init();
        timeFilterDepth = new TimeFilterDepth(4);
    }

    public DepthAnalysisPImageView(PApplet parent, KinectOne kinectOne) {
        super(parent, kinectOne);
        init();
    }

    private void init() {
        validPointsPImage = papplet.createImage(getWidth(), getHeight(), PConstants.RGB);
        nativeArrayToErode = IplImage.create(getWidth(), getHeight(), IPL_DEPTH_8U, 1);
        erosionIndexer = (UByteIndexer) nativeArrayToErode.createIndexer();
//        validCopy = Arrays.copyOf(depthData.validPointsMask, depthData.validPointsMask.length);
    }

    /// Erosion tests on binary images.
////        erodePoints(depthData.validPointsMask);
////        erodePoints2(depthData.validPointsList, depthData.validPointsMask, skip2D);
////        erodePoints(depthData.validPointsMask3D);
////        erodePoints(depthData.validPointsMask);
////        erodePoints2(depthData.validPointsList, depthData.validPointsMask, skip2D);
////        erodePoints(depthData.validPointsMask3D);
    private void erodePoints(boolean[] arrayToErode) {

        for (int i = 0; i < getWidth() * getHeight(); i++) {
            erosionIndexer.put(i, arrayToErode[i] ? 1 : 0);
        }
        cvErode(nativeArrayToErode, nativeArrayToErode);
        for (int i = 0; i < getWidth() * getHeight(); i++) {
            arrayToErode[i] = erosionIndexer.get(i) == 1;
        }
    }

//    class ErodeValidPoints implements DepthPointManiplation {
//
//        @Override
//        public void execute(Vec3D p, PixelOffset px) {
//            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)) {
//
////                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
////                depthData.projectedPoints[px.offset] = projected;
//                depthData.planeAndProjectionCalibration.project(p, depthData.projectedPoints[px.offset]);
//
//                if (isInside(depthData.projectedPoints[px.offset], 0.f, 1.f, 0.0f)) {
//                    depthData.validPointsMask[px.offset] = true;
//                    depthData.validPointsList.add(px.offset);
//                }
//            }
//        }
//    }
    /**
     * Simple visualization
     *
     * @param depth
     * @param color
     * @param skip
     * @return
     */
    public PImage update(IplImage depth, IplImage color, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 

        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 0));

        // TODO: get the color with Kinect2... 
        if (this.colorCamera.getPixelFormat() == Camera.PixelFormat.RGB) {
            computeDepthAndDo(skip, new SetImageDataRGB());
        }
        if (this.colorCamera.getPixelFormat() == Camera.PixelFormat.BGR) {
            computeDepthAndDo(skip, new SetImageData());
        }
        validPointsPImage.updatePixels();
        return validPointsPImage;

    }

    class SetTouchInformation implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
//            depthData.validPointsMask[px.offset] = true;
            int r = depthData.touchAttributes[px.offset].isInTouch() ? 100 : 0;
            int g = depthData.touchAttributes[px.offset].isOverTouch() ? 100 : 0;
            int b = depthData.touchAttributes[px.offset].isUnderTouch() ? 100 : 0;
            setFakeColor(px.offset, r, g, b);
        }
    }

    private void setFakeColor(int offset, int r, int g, int b) {
        int c = (r & 0xFF) << 16
                | (g & 0xFF) << 8
                | (b & 0xFF);
        validPointsPImage.pixels[offset] = c;
    }

    // TODO: Generalization here, same functions as those to convert the pixels for OpenGL. 
    @Override
    protected int setPixelColor(int offset) {
        int c = super.setPixelColor(offset);
        validPointsPImage.pixels[offset] = c;
        return c;
    }

    @Override
    protected int setPixelColorRGB(int offset) {
        int c = super.setPixelColorRGB(offset);
        validPointsPImage.pixels[offset] = c;
        return c;
    }

    public PImage getColouredDepthImage() {
        return validPointsPImage;
    }

}
