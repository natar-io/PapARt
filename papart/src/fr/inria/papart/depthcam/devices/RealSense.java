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
import fr.inria.papart.procam.camera.CameraRealSense;
import fr.inria.papart.procam.camera.CameraRealSenseColor;
import fr.inria.papart.procam.camera.CameraRealSenseDepth;
import fr.inria.papart.procam.camera.CameraRealSenseIR;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class RealSense extends KinectDevice {

    protected CameraRealSense cameraRS;
    protected CameraRealSenseDepth cameraDepth;
    protected CameraRealSenseColor cameraColor;
    protected CameraRealSenseIR cameraIR;

    public RealSense(PApplet parent) {
        initSize();
        this.parent = parent;
        initCameras();

        // Warning Start both depth & color camera for now.
        cameraRS.start();

        setStereoCalibration(cameraRS.getHardwareExtrinsics());
        cameraColor.useHarwareIntrinsics();
        cameraDepth.useHarwareIntrinsics();
    }

    public RealSense(PApplet parent, CameraRealSense cameraRGB) {
        throw new UnsupportedOperationException("Not supported yet: RealSense(PApplet parent, CameraRealSense cameraRGB)");
//        initSize();
//        this.parent = parent;
//        this.cameraRGB = cameraRGB;
//
//        cameraDepth = cameraRGB.getDepthCamera();
//        cameraDepth.setParent(parent);
//        // TODO: calibration... 
//        cameraDepth.setCalibration(Papart.calibrationFolder + "camera-kinect2-IR.yaml");
//
//        // TODO: get the extrinsics !
//        setStereoCalibration(cameraRGB.getHardwareExtrinsics());
////        setStereoCalibration(Papart.kinectStereoCalib);
    }

    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 480;

//    public static final int CAMERA_WIDTH_RGB = 1920;
//    public static final int CAMERA_HEIGHT_RGB = 1080;
    private void initSize() {
        // IR and Depth image size 
        WIDTH = 640;
        HEIGHT = 480;
        SIZE = WIDTH * HEIGHT;

        // RGB image size
        RGB_WIDTH = 960;
        RGB_HEIGHT = 540;
        RGB_SIZE = RGB_WIDTH * RGB_HEIGHT;
    }

    public void close() {
        cameraRS.close();
        cameraDepth.close();
    }

    final void initCameras() {
        // Check if it is the default camera... 
        Papart papart = Papart.getPapart();

        if (papart.cameraConfiguration.getCameraType() == Camera.Type.REALSENSE) {
            System.out.println("REALSENSE: Using configuration ID & Resolution.");
            // use the ID
            int id = Integer.parseInt(papart.cameraConfiguration.getCameraName());

            cameraRS = (CameraRealSense) CameraFactory.createCamera(Camera.Type.REALSENSE, id);
            cameraRS.setParent(parent);

            cameraColor = cameraRS.getColorCamera();
            cameraColor.setCalibration(Papart.cameraCalib);
        } else {

            System.out.println("REALSENSE: Using DEFAULT configuration.");
            cameraRS = (CameraRealSense) CameraFactory.createCamera(Camera.Type.REALSENSE, 0);
            cameraRS.setParent(parent);

            cameraColor = cameraRS.getColorCamera();
            cameraColor.setSize(RGB_WIDTH, RGB_HEIGHT);
            cameraColor.setCalibration(Papart.calibrationFolder + "saved/camera-SR300.yaml");
        }

        cameraDepth = cameraRS.getDepthCamera();
        cameraDepth.setSize(WIDTH, HEIGHT);
    }

    final void initIR() {
        // NO IR yet.

//        cameraIR = CameraFactory.createCamera(Camera.Type.OPENCV, 1);
//        cameraIR.setParent(parent);
////        cameraIR.setSize(WIDTH, HEIGHT);
//        cameraIR.setCalibration(Papart.calibrationFolder + "camera-kinect2-IR.yaml");
//        cameraIR.start();
    }

    @Override
    public Camera getCameraRGB() {
        return cameraColor;
    }

    public Camera getCameraIR() {
        return cameraIR;
    }

    public Camera getCameraDepth() {
        return cameraDepth;
    }

    @Override
    public int rawDepthSize() {
        return SIZE * 2;
    }

    @Override
    public void setTouch(KinectTouchInput kinectTouchInput) {
        cameraDepth.setTouchInput(kinectTouchInput);
    }

    @Override
    public Type type() {
        return Type.REALSENSE;
    }

}
