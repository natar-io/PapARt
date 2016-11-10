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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinect extends CameraRGBIRDepth {

    protected OpenKinectFrameGrabber grabber;
    int kinectVideoFormat;

    protected CameraOpenKinect(int cameraNo) {
        this.systemNumber = cameraNo;
    }

    @Override
    public void internalInit() {
        if (isUseDepth()) {
            depthCamera.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
            depthCamera.type = SubCamera.Type.DEPTH;
            depthCamera.setSize(640, 480);
        }
        if (isUseColor()) {
            colorCamera.setPixelFormat(PixelFormat.BGR);
            colorCamera.type = SubCamera.Type.COLOR;
            colorCamera.setSize(640, 480);
        }
        if (isUseIR()) {
            IRCamera.setPixelFormat(PixelFormat.GRAY);
            IRCamera.type = SubCamera.Type.IR;
            IRCamera.setSize(640, 480);
        }
        grabber = new OpenKinectFrameGrabber(this.systemNumber);
    }

    @Override
    public void internalStart() throws FrameGrabber.Exception {
        grabber.start();
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
    }

    @Override
    public void disableIR() {
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
        kinectVideoFormat = freenect.FREENECT_VIDEO_RGB;
        grabber.setVideoFormat(kinectVideoFormat);
    }

    @Override
    public void disableColor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void grabIR() {
        try {
            IRCamera.updateCurrentImage(grabber.grabIR());
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraOpenKinect.class.getName()).log(Level.SEVERE, null, ex);
        }
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
