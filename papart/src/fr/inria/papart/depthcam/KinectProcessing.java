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

import static fr.inria.papart.depthcam.Kinect.papplet;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import java.util.Arrays;
import org.bytedeco.javacpp.opencv_core.IplImage;
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

    public KinectProcessing(PApplet parent, String calibIR, String calibRGB, int mode) {
        super(parent, calibIR, calibRGB, mode);
    }

    @Override
    protected void init() {
        super.init();
        validPointsPImage = papplet.createImage(kinectCalibIR.getWidth(), kinectCalibIR.getHeight(), PConstants.RGB);
    }

    public PImage update(IplImage depth, IplImage color) {
        return update(depth, color, 1);
    }

    public PImage update(IplImage depth, IplImage color, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));
        computeDepthAndDo(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

//    //////////// Default FUNCTION ///////////////
    public PImage updateTest(IplImage depth, IplImage color,
            PlaneAndProjectionCalibration planeProjCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.planeAndProjectionCalibration = planeProjCalibration;

        computeDepthAndDo(skip, new DoNothing());
        doForEachPoint(skip, new Select2DPointPlaneProjection());
        doForEachPoint(skip, new Select3DPointPlaneProjection());

//        computeDepthAndDo(skip, new Select2DPointPlaneProjection());
        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }
    /**
     * Work in progress tests...
     * @param depth
     * @param color
     * @param planeCalibration
     * @param skip
     * @return 
     */
    public PImage updateTest(IplImage depth, IplImage color,
            PlaneCalibration planeCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.planeCalibration = planeCalibration;

        computeDepthAndDo(skip, new DoNothing());
//        doForEachPoint(skip, new Select2DPointOverPlaneDist());
        doForEachPoint(skip, new Select2DPointOverPlane());
//        doForEachPoint(skip, new Select3DPointPlaneProjection());

//        computeDepthAndDo(skip, new Select2DPointPlaneProjection());
        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }
    //////////// WORK IN PROGRESS FUNCTION ///////////////
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

        computeDepthAndDo(skip, new DoNothing());
        doForEachPoint(skip, new SelectPlaneTouchHand());
        doForEachPoint(skip, new SetTouchInformation());

//        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage update(IplImage depth, IplImage color,
            PlaneCalibration planeCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.planeCalibration = planeCalibration;
        computeDepthAndDo(skip, new Select2DPointOverPlaneDist());
//        computeDepthAndDo(skip, new Select2DPointOverPlane());

        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage update(IplImage depth, IplImage color,
            HomographyCalibration homographyCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.homographyCalibration = homographyCalibration;
        computeDepthAndDo(skip, new Select2DPointCalibratedHomography());
        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    class SetTouchInformation implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
//            depthData.validPointsMask[offset] = true;
            int r = depthData.touchAttributes[offset].isInTouch() ? 100 : 0;
            int g = depthData.touchAttributes[offset].isOverTouch()? 100 : 0;
            int b = depthData.touchAttributes[offset].isUnderTouch() ? 100 : 0;
            setFakeColor(offset, r, g, b);
        }
    }

    class SetImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            depthData.validPointsMask[offset] = true;
            setPixelColor(offset);
        }
    }

    private void setFakeColor(int offset, int r, int g, int b) {
        int c = (r & 0xFF) << 16
                | (g & 0xFF) << 8
                | (b & 0xFF);
        validPointsPImage.pixels[offset] = c;
    }

    private void setPixelColor(int offset) {
        int colorOffset = this.findColorOffset(depthData.kinectPoints[offset]) * 3;
        int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                | (colorRaw[colorOffset + 1] & 0xFF) << 8
                | (colorRaw[colorOffset + 0] & 0xFF);

        validPointsPImage.pixels[offset] = c;
    }

    public PImage getColouredDepthImage() {
        return validPointsPImage;
    }

}
