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

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.OpenKinect2FrameGrabber;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinect2 extends Camera {

    protected OpenKinect2FrameGrabber grabber;

    protected CameraOpenKinect2(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.ARGB);
    }

//    public void getIRVideo() {
//        this.setPixelFormat(PixelFormat.GRAY);
//        kinectVideoFormat = freenect.FREENECT_VIDEO_IR_8BIT;
//    }
    @Override
    public void start() {

        grabber = new OpenKinect2FrameGrabber(this.systemNumber);
        grabber.enableColorStream();
        try {

            grabber.start();
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not Kinect start frameGrabber... " + e);
            System.err.println("Kinect ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection and ID.");
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }

        try {
            grabber.grab();
            IplImage img = grabber.getVideoImage();
            updateCurrentImage(img);

        } catch (Exception e) {
            System.err.println("Camera: OpenKinect2 Grab() Error ! " + e);
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
        setClosing();
        if (grabber != null) {
            try {
                System.out.println("Stopping OpenKinect2Grabber");
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }

    }

//    public void setTouch(KinectTouchInput touchInput) {
//        this.setGrabDepth(true);
//        depthCamera.setTouchInput(touchInput);
//    }
//
//    public CameraOpenKinectDepth getDepthCamera() {
//        this.setGrabDepth(true);
//        return this.depthCamera;
//    }
//
//    public void setGrabDepth(boolean grabDepth) {
//        this.isGrabbingDepth = grabDepth;
//    }
}
