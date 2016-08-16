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
import fr.inria.papart.procam.camera.CameraOpenKinect;
import processing.core.PApplet;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole
 */
public class Kinect360 extends KinectDevice {

    public static final int KINECT_MM = 1;
    public static final int KINECT_10BIT = 0;
    private final CameraOpenKinect camera;

    public static final int CAMERA_WIDTH = 640;
    public static final int CAMERA_HEIGHT = 480;

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

    @Override
    public Type type() {
        return Type.X360;
    }
}
