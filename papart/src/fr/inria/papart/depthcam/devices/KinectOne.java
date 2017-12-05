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

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import static fr.inria.papart.depthcam.analysis.DepthAnalysisImpl.KINECT_ONE_DEPTH_RATIO;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraOpenKinect2;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import org.bytedeco.javacpp.opencv_core;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class KinectOne extends DepthCameraDevice {

    public KinectOne(PApplet parent, Camera incomingCamera) throws CannotCreateCameraException {
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

    @Override
    public void loadDataFromDevice() {
        // Nothing here yet. maybe get calibrations ?
    }

    @Override
    public DepthAnalysis.DepthComputation createDepthComputation() {
        return new KinectOneDepth();
    }

    public class KinectOneDepth implements DepthAnalysis.DepthComputation {

        byte[] depthRaw;

        public KinectOneDepth() {
            depthRaw = new byte[rawDepthSize()];
        }

        @Override
        public float findDepth(int offset) {
            float d = (depthRaw[offset * 3 + 1] & 0xFF) * 256
                    + (depthRaw[offset * 3] & 0xFF);

            return d / KINECT_ONE_DEPTH_RATIO; // / 65535f * 10000f;
        }

        @Override
        public void updateDepth(opencv_core.IplImage depthImage) {
            depthImage.getByteBuffer().get(depthRaw);
        }
    }

}
