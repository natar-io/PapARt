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

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import fr.inria.papart.procam.camera.CameraRealSense;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class RealSense extends DepthCameraDevice {

    private final CameraRealSense cameraRS;

    public RealSense(PApplet parent, Camera anotherCam) {
        super(parent);
        if (anotherCam instanceof CameraRealSense) {
            this.camera = (CameraRealSense) anotherCam;
        } else {
            initDefaultCamera();
            this.anotherCamera = anotherCam;
        }
        this.camera.setUseDepth(true);
        cameraRS = (CameraRealSense) camera;
//        cameraRS.setCalibration(Papart.kinectRGBCalib);
//        cameraRS.getDepthCamera().setCalibration(Papart.kinectIRCalib);
//        setStereoCalibration(Papart.kinectStereoCalib);

        camera.start();
        setStereoCalibration(cameraRS.getHardwareExtrinsics());
        useHardwareIntrinsics(cameraRS);
    }

    private void useHardwareIntrinsics(CameraRealSense cameraRS) {
        CameraRealSense.useHarwareIntrinsics(cameraRS.getColorCamera(), cameraRS.getFrameGrabber());
        CameraRealSense.useHarwareIntrinsics(cameraRS.getColorCamera(), cameraRS.getFrameGrabber());
        CameraRealSense.useHarwareIntrinsics(cameraRS.getColorCamera(), cameraRS.getFrameGrabber());
    }

    public CameraRealSense getMainCamera() {
        return cameraRS;
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.REALSENSE;
    }

}
