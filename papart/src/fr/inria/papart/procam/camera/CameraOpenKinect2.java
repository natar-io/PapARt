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

import org.bytedeco.javacv.OpenKinect2FrameGrabber;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinect2 extends CameraRGBIRDepth {

    protected OpenKinect2FrameGrabber grabber;

    protected CameraOpenKinect2(int cameraNo) {
        this.systemNumber = cameraNo;

        depthCamera = new SubDepthCamera(this);
        depthCamera.setPixelFormat(PixelFormat.FLOAT_DEPTH_KINECT2);
        depthCamera.type = SubCamera.Type.DEPTH;
        depthCamera.setSize(512, 424);

        colorCamera = new SubCamera(this);
        colorCamera.setPixelFormat(PixelFormat.ARGB);
        colorCamera.type = SubCamera.Type.COLOR;
        colorCamera.setSize(1920, 1080);

        IRCamera = new SubCamera(this);
        IRCamera.setPixelFormat(PixelFormat.GRAY_32);
        IRCamera.type = SubCamera.Type.IR;
        IRCamera.setSize(512, 424);
    }

    @Override
    public void start() {
        grabber = new OpenKinect2FrameGrabber(this.systemNumber);

        colorCamera.start();
        IRCamera.start();
        depthCamera.start();
        
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
    protected void internalGrab() throws Exception {
        grabber.grab();
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

    @Override
    public void enableIR() {
        grabber.enableIRStream();
    }

    @Override
    public void disableIR() {
        // Not implemented yet.
    }

    @Override
    public void enableDepth() {
        grabber.enableDepthStream();
    }

    @Override
    public void disableDepth() {
        // Not implemented yet.
    }

    @Override
    public void enableColor() {
        grabber.enableColorStream();
    }

    @Override
    public void disableColor() {
        // Not implemented yet.
    }

    @Override
    public void grabIR() {
        IRCamera.updateCurrentImage(grabber.getIRImage());
    }

    @Override
    public void grabDepth() {
        depthCamera.currentImage = grabber.getDepthImage();
    }

    @Override
    public void grabColor() {
        colorCamera.updateCurrentImage(grabber.getVideoImage());
    }

}
