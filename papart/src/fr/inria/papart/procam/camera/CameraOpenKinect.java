/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.graph.Displayable;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Utils;
import java.util.HashMap;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraOpenKinect extends Camera implements Displayable {

    private boolean isGrabbingDepth = false;
    protected OpenKinectFrameGrabber grabber;

    private CameraOpenKinectDepth depthCamera;

    protected CameraOpenKinect(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);

        depthCamera = new CameraOpenKinectDepth(this);
    }

    @Override
    public void start() {
        grabber = new OpenKinectFrameGrabber(this.systemNumber);
        grabber.setImageWidth(width());
        grabber.setImageHeight(height());

        try {
            grabber.start();
            grabber.setVideoFormat(freenect.FREENECT_VIDEO_RGB);

            depthCamera.start();

            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not Kinect start frameGrabber... " + e);
            System.err.println("Kinect ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection and ID.");
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }

        try {
            IplImage img = grabber.grabVideo();
            updateCurrentImage(img);

            if (this.isGrabbingDepth) {
                depthCamera.grab();
            }

        } catch (Exception e) {
            System.err.println("Camera: Kinect Grab() Error ! " + e);
            e.printStackTrace();
        }
    }

    @Override
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

    public void setTouch(KinectTouchInput touchInput) {
        this.setGrabDepth(true);
        depthCamera.setTouchInput(touchInput);
    }

    public CameraOpenKinectDepth getDepthCamera() {
        this.setGrabDepth(true);
        return this.depthCamera;
    }

    public void setGrabDepth(boolean grabDepth) {
        this.isGrabbingDepth = grabDepth;
    }

    HashMap<PApplet, PImage> imageMap = new HashMap();

    @Override
    public void prepareToDisplayOn(PApplet display) {
        PImage image = display.createImage(this.width, this.height, RGB);
        imageMap.put(display, image);
    }

    @Override
    public PImage getDisplayedOn(PApplet display) {
        PImage image = imageMap.get(display);
        Utils.IplImageToPImage(currentImage, false, image);
        return image;
    }

    @Override
    public boolean canBeDisplayedOn(PApplet display) {
        return imageMap.containsKey(display);
    }
    

}
