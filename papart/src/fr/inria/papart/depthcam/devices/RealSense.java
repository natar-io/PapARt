/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2016 Jérémy Laviole
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
import fr.inria.papart.procam.camera.CameraRealSense;
import fr.inria.papart.procam.camera.SubCamera;
import fr.inria.papart.procam.camera.SubDepthCamera;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class RealSense extends KinectDevice {

    protected CameraRealSense cameraRS;
    protected SubDepthCamera cameraDepth;
    protected SubCamera cameraColor;
    protected SubCamera cameraIR;

    public RealSense(PApplet parent) {
        initSize();
        this.parent = parent;
        initCameras();

        // Warning Start both depth & color camera for now.
        cameraRS.start();

        setStereoCalibration(cameraRS.getHardwareExtrinsics());
        useHardwareIntrinsics();
    }

    public RealSense(PApplet parent, CameraRealSense cameraRGB) {
        initSize();
        this.parent = parent;

        cameraColor = cameraRS.getColorCamera();
        cameraDepth = cameraRS.getDepthCamera();
        cameraIR = cameraRS.getIRCamera();

        cameraDepth.setSize(WIDTH, HEIGHT);
        cameraIR.setSize(WIDTH, HEIGHT);

        // Warning Start both depth & color camera for now.
        cameraRS.start();

        setStereoCalibration(cameraRS.getHardwareExtrinsics());
        useHardwareIntrinsics();
    }

    private void useHardwareIntrinsics() {
        CameraRealSense.useHarwareIntrinsics(cameraColor, cameraRS.getFrameGrabber());
        CameraRealSense.useHarwareIntrinsics(cameraDepth, cameraRS.getFrameGrabber());
        CameraRealSense.useHarwareIntrinsics(cameraIR, cameraRS.getFrameGrabber());
    }

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
        if (papart.cameraConfiguration.getCameraType() == Camera.Type.REALSENSE_RGB) {
            System.out.println("REALSENSE: Using configuration ID & Resolution.");
            // use the ID
            int id = Integer.parseInt(papart.cameraConfiguration.getCameraName());

            cameraColor = cameraRS.getColorCamera();
            cameraColor.setCalibration(Papart.cameraCalib);
        } else {
            cameraRS = (CameraRealSense) CameraFactory.createCamera(Camera.Type.REALSENSE_RGB, 0);
            cameraRS.setParent(parent);

            cameraColor = cameraRS.getColorCamera();
            cameraColor.setSize(RGB_WIDTH, RGB_HEIGHT);
            cameraColor.setCalibration(Papart.calibrationFolder + "saved/camera-SR300.yaml");
        }

        cameraDepth = cameraRS.getDepthCamera();
        cameraDepth.setSize(WIDTH, HEIGHT);

        cameraIR = cameraRS.getIRCamera();
        cameraIR.setSize(WIDTH, HEIGHT);
    }

    public void grab() {
        cameraRS.grab();
    }

    public CameraRealSense getMainCamera() {
        return cameraRS;
    }

    @Override
    public SubCamera getCameraRGB() {
        return cameraColor;
    }

    @Override
    public SubCamera getCameraIR() {
        return cameraIR;
    }

    @Override
    public SubDepthCamera getCameraDepth() {
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
