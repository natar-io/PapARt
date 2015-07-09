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
package fr.inria.papart.depthcam;

import static fr.inria.papart.depthcam.DepthAnalysis.papplet;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import static fr.inria.papart.depthcam.DepthAnalysis.papplet;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.util.Arrays;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectProcessing extends KinectDepthAnalysis {

    public PImage validPointsPImage;

    public KinectProcessing(PApplet parent, CameraOpenKinect camera) {
        super(parent, camera);
        init();
    }

    private void init() {
        validPointsPImage = papplet.createImage(width, height, PConstants.RGB);
    }


    public void updateMT(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip2D) {
        updateRawDepth(depth);
        // optimisation no Color. 
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip2D, new Select2DPointPlaneProjection());
//        doForEachPoint(skip2D, new Select2DPointPlaneProjection());
        doForEachPoint(skip2D, new Select3DPointPlaneProjection());

        validPointsPImage.loadPixels();
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        doForEachValidPoint(skip2D, new SetImageData());
        validPointsPImage.updatePixels();
    }

    public PImage update(IplImage depth, IplImage color,
            PlaneAndProjectionCalibration planeProjCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.planeAndProjectionCalibration = planeProjCalibration;
        computeDepthAndDo(skip, new Select2DPointPlaneProjection());
//        computeDepthAndDo(skip, new Select2DPointOverPlane());

        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }
    
    /**
     * Simple visualization
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
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        computeDepthAndDo(skip, new SetImageData());
//        computeDepthAndDo(skip, new Select2DPointOverPlane());

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

    class SetImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {
            depthData.validPointsMask[px.offset] = true;
            setPixelColor(px.offset);
        }
    }

    private void setFakeColor(int offset, int r, int g, int b) {
        int c = (r & 0xFF) << 16
                | (g & 0xFF) << 8
                | (b & 0xFF);
        validPointsPImage.pixels[offset] = c;
    }

    private void setPixelColor(int offset) {
        int colorOffset = this.findColorOffset(depthData.depthPoints[offset]) * 3;
        int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                | (colorRaw[colorOffset + 1] & 0xFF) << 8
                | (colorRaw[colorOffset + 0] & 0xFF);

        validPointsPImage.pixels[offset] = c;
    }

    public PImage getColouredDepthImage() {
        return validPointsPImage;
    }

}
