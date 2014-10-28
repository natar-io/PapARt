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

import fr.inria.papart.depthcam.calibration.KinectScreenCalibration;
import static fr.inria.papart.depthcam.Kinect.papplet;
import fr.inria.papart.depthcam.calibration.HomographyCalibration;
import fr.inria.papart.depthcam.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.calibration.PlaneCalibration;
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
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));
        computeDepthAndDo(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    @Deprecated
    public PImage update(IplImage depth, IplImage color,
            KinectScreenCalibration calib, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.calibration = calib;
        computeDepthAndDo(skip, new Select2DPoint());
        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage update(IplImage depth, IplImage color,
            PlaneAndProjectionCalibration planeProjCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.planeAndProjectionCalibration = planeProjCalibration;
        computeDepthAndDo(skip, new Select2DPointCalibrated());
        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage update(IplImage depth, IplImage color,
            PlaneCalibration planeCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.planeCalibration = planeCalibration;
        computeDepthAndDo(skip, new Select2DPointOverPlane());

        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage update(IplImage depth, IplImage color,
            HomographyCalibration homographyCalibration, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clear();
        validPointsPImage.loadPixels();
        // set a default color. 
        Arrays.fill(validPointsPImage.pixels, papplet.color(0, 0, 255));

        depthData.homographyCalibration = homographyCalibration;
        computeDepthAndDo(skip, new Select2DPointCalibratedHomography());
        doForEachValidPoint(skip, new SetImageData());
        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    class SetImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            depthData.validPointsMask[offset] = true;
            setPixelColor(offset);
        }
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
