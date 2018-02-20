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

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenKinect2FrameGrabber;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinect2 extends CameraRGBIRDepth {

    protected OpenKinect2FrameGrabber grabber;

    class Kinect2SubCamera extends SubCamera {

        public Kinect2SubCamera(CameraRGBIRDepth mainCamera) {
            super(mainCamera);
        }

        @Override
        public PImage getPImage() {
            mainCamera.grab();
            return super.getPImage();
        }
    }

    protected CameraOpenKinect2(int cameraNo) {
        this.systemNumber = cameraNo;
        colorCamera = new Kinect2SubCamera(this);
        grabber = new OpenKinect2FrameGrabber(this.systemNumber);
    }

    @Override
    public void internalStart() throws FrameGrabber.Exception {
        grabber.start();
    }

    /**
     * *
     * Warning BUG: cannot grab in a thread and display as PImage in another.
     *
     * @throws Exception
     */
    @Override
    protected void internalGrab() throws Exception {
        grabber.grab();
    }

    @Override
    public void close() {
        setClosing();
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void setUseIR(boolean use) {
        if (use) {
            IRCamera.setPixelFormat(PixelFormat.GRAY_32);
            IRCamera.type = SubCamera.Type.IR;
            IRCamera.setSize(512, 424);
            grabber.enableIRStream();
        }
        this.useIR = use;
    }

    @Override
    public void setUseDepth(boolean use) {
        if (use) {
            depthCamera.setPixelFormat(PixelFormat.FLOAT_DEPTH_KINECT2);
            depthCamera.type = SubCamera.Type.DEPTH;
            depthCamera.setSize(512, 424);
            grabber.enableDepthStream();
        }
             this.useDepth = use;
    }

    @Override
    public void setUseColor(boolean use) {
        if (use) {
            colorCamera.setPixelFormat(PixelFormat.ARGB);
            colorCamera.type = SubCamera.Type.COLOR;
            colorCamera.setSize(1920, 1080);
            grabber.enableColorStream();
        }
             this.useColor = use;
    }

    @Override
    public void grabIR() {
        IRCamera.updateCurrentImage(grabber.getIRImage());
    }

    @Override
    public void grabDepth() {
        depthCamera.currentImage = grabber.getDepthImage();
        ((WithTouchInput) depthCamera).newTouchImageWithColor(colorCamera.currentImage);
    }

    @Override
    public void grabColor() {
        opencv_core.IplImage videoImage = grabber.getVideoImage();
        if (videoImage != null) {
            colorCamera.updateCurrentImage(videoImage);
        }
    }

}
