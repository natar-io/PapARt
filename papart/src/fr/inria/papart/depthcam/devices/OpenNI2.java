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

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import static fr.inria.papart.depthcam.analysis.DepthAnalysisImpl.INVALID_DEPTH;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraOpenNI2;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.bytedeco.javacpp.opencv_core;
import org.openni.VideoStream;
import processing.core.PApplet;

/**
 *
 * @author Jeremy Laviole
 */
public final class OpenNI2 extends DepthCameraDevice {

    private final CameraOpenNI2 cameraNI;

    public OpenNI2(PApplet parent, Camera anotherCam) throws CannotCreateCameraException {
        super(parent);

        if (anotherCam instanceof CameraOpenNI2) {
            this.camera = (CameraOpenNI2) anotherCam;
            this.camera.setUseDepth(true);
        } else {
            initDefaultCamera();
            this.anotherCamera = anotherCam;
        }

        if (this.anotherCamera == null) {
            this.anotherCamera = getColorCamera();
        }

        cameraNI = (CameraOpenNI2) camera;

        camera.getDepthCamera().setCalibration(Papart.AstraSDepthCalib);
        camera.getColorCamera().setCalibration(Papart.AstraSRGBCalib);
        setStereoCalibration(Papart.AstraSStereoCalib);

//        setStereoCalibration(Papart.kinectStereoCalib);
        // TODO: Hacks to try to handle the SR300 distorsions
//        camera.getDepthCamera().setCalibration(Papart.SR300IRCalib);
//        camera.getIRCamera().setCalibration(Papart.SR300IRCalib);
    }

    public CameraOpenNI2 getMainCamera() {
        return cameraNI;
    }

    @Override
    public int rawDepthSize() {
        return getDepthCamera().width() * getDepthCamera().height() * 2;
    }

    @Override
    public Camera.Type type() {
        return Camera.Type.OPENNI2;
    }

    @Override
    public void loadDataFromDevice() {
//        setStereoCalibration(cameraNI.getHardwareExtrinsics());
    }

    @Override
    public DepthAnalysis.DepthComputation createDepthComputation() {
        return new OpenNIDepth();
    }

    protected class OpenNIDepth implements DepthAnalysis.DepthComputation {

//        private ByteBuffer frameData;
        private ShortBuffer frameData;
        float[] histogram;

        public OpenNIDepth() {
        }

        @Override
        public float findDepth(int offset) {
//            int depth = (int) (frameData.getShort(offset) & 0xFFFF);
            int depth = (int) (frameData.get(offset) & 0xFFFF);

//            short pixel = (short) histogram[depth];
//            //ImagePixels in integer -> Processing
//            int d = 0xFF000000 | (pixel << 16) | (pixel << 8);

            if (depth == 0) {
                return INVALID_DEPTH;
            }
//                        System.out.println(depth);
            return depth;
        }

        @Override
        public void updateDepth(opencv_core.IplImage depthImage) {
//            frameData = depthImage.getByteBuffer();
            frameData = depthImage.getShortBuffer();
//            calcHist(frameData);
        }

        private void calcHist(ByteBuffer depthBuffer) {
            // make sure we have enough room

            if (histogram == null) {
                histogram = new float[getDepthCamera().width() * getDepthCamera().height()];
            }

            // reset
            for (int i = 0; i < histogram.length; ++i) {
                histogram[i] = 0;
            }

            int points = 0;
            while (depthBuffer.remaining() > 0) {
                int depth = depthBuffer.getShort() & 0xFFFF;
                if (depth != 0) {
                    histogram[depth]++;
                    points++;
                }
            }

            for (int i = 1; i < histogram.length; i++) {
                histogram[i] += histogram[i - 1];
            }

            if (points > 0) {
                for (int i = 1; i < histogram.length; i++) {
                    histogram[i] = (int) (256 * (1.0f - (histogram[i] / (float) points)));
                }
            }
            depthBuffer.rewind();
        }

    }

}
