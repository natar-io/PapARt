/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import processing.core.PApplet;

/**
 *
 * @author jiii
 */
public final class KinectOne extends KinectDevice {

    protected Camera cameraRGB, cameraIR, cameraDepth;
    protected PApplet parent;

    public KinectOne(PApplet parent) {
        // IR and Depth image size 
        WIDTH = 512;
        HEIGHT = 424;
        SIZE = WIDTH * HEIGHT;

        // RGB image size
        RGB_WIDTH = 1920;
        RGB_HEIGHT = 1080;
        RGB_SIZE = RGB_WIDTH * RGB_HEIGHT;

        this.parent = parent;
        initRGB();
        initIR();
        initDepth();
    }

    public void close() {
        cameraRGB.close();
        cameraIR.close();
        cameraDepth.close();
    }

    final void initRGB() {
        cameraRGB = CameraFactory.createCamera(Camera.Type.OPENCV, 0);
        cameraRGB.setParent(parent);
//        cameraRGB.setSize(RGB_WIDTH, RGB_HEIGHT);
        cameraRGB.setCalibration(Papart.calibrationFolder + "camera-kinect2-rgb.yaml");
        cameraRGB.start();
    }

    final void initIR() {
        cameraIR = CameraFactory.createCamera(Camera.Type.OPENCV, 1);
        cameraIR.setParent(parent);
//        cameraIR.setSize(WIDTH, HEIGHT);
        cameraIR.setCalibration(Papart.calibrationFolder + "camera-kinect2-IR.yaml");
        cameraIR.start();
    }

    final void initDepth() {
        cameraDepth = CameraFactory.createCamera(Camera.Type.OPENCV_DEPTH, 2);
        cameraDepth.setParent(parent);
//        cameraDepth.setSize(WIDTH, HEIGHT);
        cameraDepth.setCalibration(Papart.calibrationFolder + "camera-kinect2-IR.yaml");
        cameraDepth.start();
    }

    public Camera getCameraRGB() {
        return cameraRGB;
    }

    public Camera getCameraIR() {
        return cameraIR;
    }

    public Camera getCameraDepth() {
        return cameraDepth;
    }

}
