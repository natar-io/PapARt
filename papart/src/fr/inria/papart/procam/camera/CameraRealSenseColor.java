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
package fr.inria.papart.procam.camera;

import java.nio.FloatBuffer;
import org.bytedeco.javacpp.RealSense;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSenseColor extends Camera {

    CameraRealSense mainCamera;

    protected CameraRealSenseColor(CameraRealSense mainCamera) {
        setPixelFormat(PixelFormat.RGB);
        this.mainCamera = mainCamera;
    }

    public void useHarwareIntrinsics() {
        RealSense.intrinsics intrinsics = mainCamera.grabber.getRealSenseDevice().get_stream_intrinsics(RealSense.color);
        FloatBuffer fb = intrinsics.position(0).asByteBuffer().asFloatBuffer();
        float cx = fb.get(2);
        float cy = fb.get(3);
        float fx = fb.get(4);
        float fy = fb.get(5);
        setSimpleCalibration(fx, fy, cx, cy, width(), height());
    }

    @Override
    public void start() {
        mainCamera.grabber.setImageWidth(width());
        mainCamera.grabber.setImageHeight(height());
        mainCamera.grabber.enableColorStream();
        this.isConnected = true;
    }

    @Override
    public void grab() {
        if (this.isClosing()) {
            return;
        }
        // update the images.
        try {
            System.out.println("Grabbing color");
            currentImage = mainCamera.grabber.grabVideo();
            
        } catch (Exception e) {
            System.out.println("Exception :" + e);
            e.printStackTrace();
        }
    }

    @Override
    public PImage getPImage() {
        this.checkCamImage();
        if (currentImage != null) {
            camImage.update(currentImage);
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }

    @Override
    public void close() {
        this.setClosing();
        mainCamera.useColor(false);
        mainCamera.grabber.disableColorStream();

    }

}
