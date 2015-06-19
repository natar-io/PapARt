/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Utils;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraOpenKinectDepth extends Camera {

    private CameraOpenKinect parent;
    private int depthFormat = freenect.FREENECT_DEPTH_MM;
    // other possibility freenect.FREENECT_DEPTH_10_BIT -> Obselete;

    private opencv_core.IplImage depthImage;
    private KinectTouchInput touchInput;
    private PImage camImageDepth = null;

    protected CameraOpenKinectDepth(CameraOpenKinect parent) {
        this.parent = parent;
        this.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
    }

    // Nothing, this is virtual...
    @Override
    public void start() {
        parent.grabber.setDepthFormat(depthFormat);
    }

    @Override
    public void grab() {
        try {
            opencv_core.IplImage img = parent.grabber.grabDepth();

            this.currentImage = img;
            if (touchInput != null) {
                touchInput.lock();
                touchInput.update();
                touchInput.getTouch2DColors(parent.currentImage);
                touchInput.unlock();
            } else {
                if (touchInput != null) {
                    System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
                }
            }
        } catch (Exception e) {
            System.err.println("Camera: Kinect Grab depth Error ! " + e);
            e.printStackTrace();
        }

    }

    @Override
    public PImage getPImage() {
        if (camImageDepth == null) {
            camImageDepth = parent.parent.createImage(width, height, PApplet.ALPHA);
        }

        if (depthImage != null) {
            Utils.IplImageToPImageKinect(depthImage, false, camImageDepth);
        }
        return camImageDepth;
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public KinectTouchInput getTouchInput() {
        return touchInput;
    }

    public void setTouchInput(KinectTouchInput touchInput) {
        this.touchInput = touchInput;
    }

    public int getDepthFormat() {
        return depthFormat;
    }

    public void setDepthFormat(int depthFormat) {
        this.depthFormat = depthFormat;
    }

}
