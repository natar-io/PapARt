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

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraOpenKinect2;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class KinectOne extends DepthCameraDevice {

    public KinectOne(PApplet parent, Camera incomingCamera) {
        super(parent);
        if (incomingCamera instanceof CameraOpenKinect2) {
            this.camera = (CameraOpenKinect2) incomingCamera;
            this.anotherCamera = camera.getActingCamera();
        } else {
            initDefaultCamera();
            this.anotherCamera = incomingCamera;
        }
        this.camera.setUseDepth(true);

//        getColorCamera().setCalibration(Papart.calibrationFolder + "camera-kinect2-RGB.yaml");
//        getIRCamera().setCalibration(Papart.calibrationFolder + "camera-kinect2-IR.yaml");
        getDepthCamera().setCalibration(Papart.calibrationFolder + "camera-kinect2-IR.yaml");
        setStereoCalibration(Papart.kinectStereoCalib);

        camera.start();
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.OPEN_KINECT_2;
    }

}
