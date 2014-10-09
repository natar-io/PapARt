/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam;

import static fr.inria.papart.depthcam.Kinect.papplet;
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
        Arrays.fill(validPointsPImage.pixels,  papplet.color(0, 0, 255));
        computeDepthAndDo(skip, new SetImageData());
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
