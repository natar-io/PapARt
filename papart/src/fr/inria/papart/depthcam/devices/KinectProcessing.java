/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

import static fr.inria.papart.depthcam.DepthAnalysis.papplet;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.depthcam.PixelOffset;
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

    public KinectProcessing(PApplet parent, KinectOne kinectOne) {
        super(parent, kinectOne);
        init();
    }

    private void init() {
        validPointsPImage = papplet.createImage(kinectDevice().depthWidth(), kinectDevice().depthHeight(), PConstants.RGB);
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

//        computeDepthAndDo(skip, new DoNothing());
        
        // TODO: get the color with Kinect2... 
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
