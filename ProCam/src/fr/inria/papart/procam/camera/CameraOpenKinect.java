/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.Utils;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraOpenKinect extends Camera {

    private TouchInput touchInput;
    private boolean isGrabbingDepth = false;
    private OpenKinectFrameGrabber grabber;
    private IplImage depthImage;

    private int depthFormat = freenect.FREENECT_DEPTH_10BIT;
    // other possibility freenect.FREENECT_DEPTH_MM;

    protected CameraOpenKinect(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
    }

    @Override
    public void start() {
        grabber = new OpenKinectFrameGrabber(this.systemNumber);
        grabber.setImageWidth(width());
        grabber.setImageHeight(height());

        try {
            grabber.start();
            grabber.setVideoFormat(freenect.FREENECT_VIDEO_RGB);
            grabber.setDepthFormat(depthFormat);

        } catch (Exception e) {
            System.err.println("Could not Kinect start frameGrabber... " + e);
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }

        try {
            IplImage img = grabber.grabVideo();

            if (isGrabbingDepth) {
                IplImage dImage = grabber.grabDepth();

                this.depthImage = dImage;
                if (touchInput != null) {
                    touchInput.lock();
                    touchInput.updateTouch(dImage);
                    touchInput.getTouch2DColors(img);
                    touchInput.unlock();
                }
            } else {
                if (touchInput != null) {
                    System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
                }
            }
            updateCurrentImage(img);

        } catch (Exception e) {
            System.err.println("Camera: Kinect Grab() Error ! " + e);
            e.printStackTrace();
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
        setClosing();
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }

    }

    public void setTouch(TouchInput touchInput) {
        this.setGrabDepth(true);
        this.touchInput = touchInput;
    }

    public IplImage getDepthIplImage() {
        if(!this.isGrabbingDepth){
            throw new RuntimeException("Cannot grab depth, setGrabdepth to true to enable it.");
        }
        return depthImage;
    }

    public PImage getDepthPImage() {
        assert (this.isGrabbingDepth);
        if (depthPImage == null) {
            depthPImage = parent.createImage(width, height, PApplet.ALPHA);
        }

        if (depthImage != null) {
            Utils.IplImageToPImageKinect(depthImage, false, depthPImage);
        }
        return depthPImage;
    }

    public void setDepthFormat(int depthFormat) {
        this.depthFormat = depthFormat;
        this.setGrabDepth(true);
    }

    public void setGrabDepth(boolean grabDepth) {
        this.isGrabbingDepth = grabDepth;
    }

}
