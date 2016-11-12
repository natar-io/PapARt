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
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public class Kinect360 extends KinectDevice {

//    public Kinect360(PApplet parent) {
//        this.parent = parent;
//        camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, "0");
//        camera.setParent(parent);
//        camera.setCalibration(Papart.kinectRGBCalib);
//        camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
//        setStereoCalibration(Papart.kinectStereoCalib);
//        camera.start();
//    }
    
    public Kinect360(PApplet parent, CameraOpenKinect camera) {
        super(parent, (CameraRGBIRDepth) camera);
        camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
        setStereoCalibration(Papart.kinectStereoCalib);
        
        // --------------------------
        // TODO: find when to start()
//        camera.start();
    }
    
    public Kinect360(PApplet parent, Camera anotherCamera) {
        super(parent, anotherCamera);
        this.anotherCamera = anotherCamera;
        
        initDefaultCamera();
        camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
        setStereoCalibration(Papart.kinectStereoCalib);
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.OPEN_KINECT;
    }
}
