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

import fr.inria.papart.graph.Displayable;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.procam.Utils;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinect extends CameraRGBIRDepth {

    protected OpenKinectFrameGrabber grabber;
    int kinectVideoFormat;

    protected CameraOpenKinect(int cameraNo) {
        this.systemNumber = cameraNo;

        depthCamera = new SubDepthCamera(this);
        depthCamera.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
        depthCamera.type = SubCamera.Type.DEPTH;
        depthCamera.setSize(640, 480);

        colorCamera = new SubCamera(this);
        colorCamera.setPixelFormat(PixelFormat.BGR);
        colorCamera.type = SubCamera.Type.COLOR;
        colorCamera.setSize(640, 480);
        getRGBVideo();

        useIR = false;
    }

    public void getIRVideo() {
        colorCamera.setPixelFormat(PixelFormat.GRAY);
        kinectVideoFormat = freenect.FREENECT_VIDEO_IR_8BIT;
    }

    public void getRGBVideo() {
        colorCamera.setPixelFormat(PixelFormat.BGR);
        kinectVideoFormat = freenect.FREENECT_VIDEO_RGB;
    }

    @Override
    public void start() {
        grabber = new OpenKinectFrameGrabber(this.systemNumber);

        colorCamera.start();
        depthCamera.start();

        try {
            grabber.start();
            grabber.setVideoFormat(kinectVideoFormat);

            depthCamera.start();

            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not Kinect start frameGrabber... " + e);
            System.err.println("Kinect ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection and ID.");
        }
    }

    @Override
    public void close() {
        setClosing();
        if (grabber != null) {
            try {
                System.out.println("Stopping KinectGrabber");
                this.stopThread();
                grabber.stop();
                depthCamera.close();
            } catch (Exception e) {
            }
        }

    }

    @Override
    public void enableIR() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void disableIR() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void enableDepth() {
    }

    @Override
    public void disableDepth() {
    }

    @Override
    public void enableColor() {
        grabber.setImageWidth(width());
        grabber.setImageHeight(height());
    }

    @Override
    public void disableColor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void grabIR() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void grabDepth() {
        try {
            depthCamera.currentImage = grabber.grabDepth();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraOpenKinect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void grabColor() {
        try {
            colorCamera.updateCurrentImage(grabber.grabVideo());
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraOpenKinect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void internalGrab() throws Exception {
    }

}
