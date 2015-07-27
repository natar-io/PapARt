/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvSize;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.FlyCapture2FrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraFlyCapture extends Camera {

    private FrameGrabber grabber;
    private boolean useBayerDecode = false;

    protected CameraFlyCapture(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
    }

    @Override
    public void start() {
        try {
            FlyCapture2FrameGrabber grabberFly = new FlyCapture2FrameGrabber(this.systemNumber);
            grabberFly.setImageWidth(width());
            grabberFly.setImageHeight(height());

            if (useBayerDecode) {
                grabberFly.setImageMode(FrameGrabber.ImageMode.GRAY);

            } else {
                // Hack for now ... 
                // real Gray colors are not supported by Processing anyway !
                grabberFly.setImageMode(FrameGrabber.ImageMode.COLOR);
            }
            this.grabber = grabberFly;
            grabberFly.start();
            this.isConnected = true;
            
        } catch (Exception e) {
            System.err.println("Could not start FlyCapture frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            IplImage img = grabber.grab();

            img = checkBayer(img);

            if (img != null) {
                this.updateCurrentImage(img);
            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
    }

    private IplImage debayer = null;

    private IplImage checkBayer(IplImage source) {
        if (!useBayerDecode) {
            return source;
        }

        if (debayer == null) {
            CvSize outSize = new CvSize();
            outSize.width(source.width());
            outSize.height(source.height());
            debayer = opencv_core.cvCreateImage(outSize, opencv_core.IPL_DEPTH_8U, 3);
        }

        opencv_imgproc.cvCvtColor(source, debayer, opencv_imgproc.CV_BayerBG2BGR);
        return debayer;
    }

    @Override
    public PImage getPImage() {

        if (currentImage != null) {
            this.checkCamImage();
            camImage.update(currentImage);
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }

    public void setBayerDecode(boolean isBayer) {
        this.useBayerDecode = isBayer;
    }

    @Override
    public void close() {
        this.setClosing();
        if (debayer != null) {
            debayer.deallocate();
        }
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
            }
        }
    }

}
