/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Camera;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraOpenCV extends Camera {

    private FrameGrabber grabber;

    protected CameraOpenCV(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
    }

    @Override
    public void start() {
        OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(this.systemNumber);
        grabberCV.setImageWidth(width());
        grabberCV.setImageHeight(height());
        grabberCV.setImageMode(FrameGrabber.ImageMode.COLOR);

        try {
            grabberCV.start();
        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);
        }
        this.grabber = grabberCV;
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            IplImage img = grabber.grab();
            if (img != null) {
                this.updateCurrentImage(img);
            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
    }

    public PImage getPImage() {
        this.checkCamImage();
        if (currentImage != null) {
            camImage.update(currentImage);
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }
    }

}
