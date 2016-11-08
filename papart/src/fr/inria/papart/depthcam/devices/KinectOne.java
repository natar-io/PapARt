/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenCVDepth;
import fr.inria.papart.procam.camera.CameraOpenKinectDepth;
import fr.inria.papart.procam.camera.SubCamera;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class KinectOne extends KinectDevice {

    protected Camera cameraRGB, cameraIR, cameraDepth;

    public KinectOne(PApplet parent) {

        initSize();
        this.parent = parent;
        initRGB();
        initIR();
        initDepth();

        setStereoCalibration(Papart.kinectStereoCalib);
    }

    public KinectOne(PApplet parent, Camera cameraRGB) {
        initSize();
        this.parent = parent;
        this.cameraRGB = cameraRGB;
        initIR();
        initDepth();

        setStereoCalibration(Papart.kinectStereoCalib);
    }

    public static final int CAMERA_WIDTH = 512;
    public static final int CAMERA_WIDTH_RGB = 1920;
    public static final int CAMERA_HEIGHT = 424;
    public static final int CAMERA_HEIGHT_RGB = 1080;

    private void initSize() {
        // IR and Depth image size 
        WIDTH = 512;
        HEIGHT = 424;
        SIZE = WIDTH * HEIGHT;

        // RGB image size
        RGB_WIDTH = 1920;
        RGB_HEIGHT = 1080;
        RGB_SIZE = RGB_WIDTH * RGB_HEIGHT;
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
        ((CameraOpenCVDepth) cameraDepth).setColorCamera(cameraRGB);

        cameraDepth.start();
    }

    @Override
    public SubCamera getCameraRGB() {
        return cameraRGB;
    }

    public SubCamera getCameraIR() {
        return cameraIR;
    }

    public SubDepthCamera getCameraDepth() {
        return cameraDepth;
    }

    @Override
    public int rawDepthSize() {
        return SIZE * 3;
    }

    @Override
    public void setTouch(KinectTouchInput kinectTouchInput) {
        ((CameraOpenCVDepth) cameraDepth).setTouchInput(kinectTouchInput);
    }

    @Override
    public Type type() {
        return Type.ONE;
    }

}
