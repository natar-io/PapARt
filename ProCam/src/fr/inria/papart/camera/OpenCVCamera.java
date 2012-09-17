/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.camera;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.MarkerDetector;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.ARToolKitPlus;
import com.googlecode.javacv.cpp.opencv_core;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class OpenCVCamera {

    protected FrameGrabber grabber;
    protected opencv_core.IplImage iimg;
    protected boolean copyToProcessing = false;
    protected PImage pimg = null;
    protected int width, height;

    protected OpenCVCamera() {
    }

    public OpenCVCamera(int device, int width, int height) {

        grabber = new OpenCVFrameGrabber(device);

        this.width = width;
        this.height = height;
        grabber.setImageMode(FrameGrabber.ImageMode.RAW);
        grabber.setDeinterlace(true);
//                grabber.setTriggerMode(false);
//                grabber.setNumBuffers(6);
//                grabber.setTriggerMode(true);
//                grabber.flush();

    }

    public void grab() {
        try {
//            grabber.trigger();
            iimg = grabber.grab();

            // Image drawing
            if (copyToProcessing) {
                ByteBuffer buff1 = iimg.getByteBuffer();
                pimg.loadPixels();
                for (int i = 0; i
                        < iimg.width() * iimg.height(); i++) {
                    int offset = i * 3;
                    pimg.pixels[i] = (buff1.get(offset + 2) & 0xFF) << 16
                            | (buff1.get(offset + 1) & 0xFF) << 8
                            | (buff1.get(offset) & 0xFF);
                }
                pimg.updatePixels();
            }

        } catch (Exception e) {
            System.out.println("Exception in calibrated image grabbing " + e);
        }
    }

    public void setCopyToProcessing(boolean isCopy) {
        this.copyToProcessing = isCopy;
        if (isCopy && pimg == null) {
            initPimg();
        }
    }

    protected void initPimg() {
        this.pimg = new PImage(grabber.getImageWidth(), grabber.getImageHeight(), PApplet.RGB);
    }

    public PImage getPImage() {
        return pimg;
    }

    public void setFrameRate(int frameRate) {
        grabber.setFrameRate(frameRate);
    }
    
    // public PVector getSize() {
    // return new PVector(cam.imageWidth, cam.imageHeight);
    // }
}
