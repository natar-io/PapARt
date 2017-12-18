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

import fr.inria.papart.depthcam.analysis.DepthComputation;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public class Kinect360 extends DepthCameraDevice {

//    public Kinect360(PApplet parent) {
//        this.parent = parent;
//        camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, "0");
//        camera.setParent(parent);
//        camera.setCalibration(Papart.kinectRGBCalib);
//        camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
//        setStereoCalibration(Papart.kinectStereoCalib);
//        camera.start();
//    }
    public Kinect360(PApplet parent, Camera anotherCamera) throws CannotCreateCameraException {
        super(parent);

        if (anotherCamera instanceof CameraOpenKinect) {
            this.camera = (CameraOpenKinect) anotherCamera;
            this.anotherCamera = camera.getActingCamera();
        } else {
            initDefaultCamera();
            this.anotherCamera = anotherCamera;
        }
        this.camera.setUseDepth(true);
        camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
        camera.getColorCamera().setCalibration(Papart.kinectRGBCalib);
        setStereoCalibration(Papart.kinectStereoCalib);
        camera.start();
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.OPEN_KINECT;
    }

    @Override
    public void loadDataFromDevice() {
        // Nothing here yet. maybe get calibrations ?
    }

    @Override
    public DepthAnalysis.DepthComputation createDepthComputation() {
        return new Kinect360Depth();
    }

    public class Kinect360Depth implements DepthAnalysis.DepthComputation {

        byte[] depthRaw;

        public Kinect360Depth() {
            depthRaw = new byte[rawDepthSize()];
        }

        @Override
        public float findDepth(int offset) {
            float d = (depthRaw[offset * 2] & 0xFF) << 8
                    | (depthRaw[offset * 2 + 1] & 0xFF);

            return d;
        }

        @Override
        public void updateDepth(IplImage depthImage) {
            depthImage.getByteBuffer().get(depthRaw);
        }
    }
}
