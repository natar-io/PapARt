/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import processing.core.PApplet;

/**
 *
 * @author jiii
 */
public class Kinect360 extends KinectDevice {

    public static final int KINECT_MM = 1;
    public static final int KINECT_10BIT = 0;
    private final CameraOpenKinect camera;

    public Kinect360(PApplet parent) {
        initSizes(parent);
        camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
        camera.setParent(parent);
        camera.setCalibration(Papart.kinectRGBCalib);
        camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
        camera.start();
    }

    private void initSizes(PApplet parent) {
        this.parent = parent;
        WIDTH = 640;
        HEIGHT = 480;
        SIZE = WIDTH * HEIGHT;
        RGB_WIDTH = 640;
        RGB_HEIGHT = 480;
        RGB_SIZE = WIDTH * HEIGHT;
    }

    @Override
    public Camera getCameraRGB() {
        return camera;
    }

    @Override
    public Camera getCameraIR() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Camera getCameraDepth() {
        return camera.getDepthCamera();
    }

    @Override
    public int rawDepthSize() {
        return SIZE * 2;
    }

    @Override
    public void setTouch(KinectTouchInput kinectTouchInput) {
        ((CameraOpenKinect) this.getCameraRGB()).setTouch(kinectTouchInput);
    }
}
