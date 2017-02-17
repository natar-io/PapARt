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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.RealSense;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.RealSenseFrameGrabber;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSense extends CameraRGBIRDepth {

    protected RealSenseFrameGrabber grabber;
    private boolean useHardwareIntrinsics = true;

    protected CameraRealSense(int cameraNo) {
        try {
            RealSenseFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraRealSense.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.systemNumber = cameraNo;
        grabber = new RealSenseFrameGrabber(this.systemNumber);

    }

    public RealSenseFrameGrabber getFrameGrabber() {
        return this.grabber;
    }

    public void useHardwareIntrinsics(boolean use) {
        this.useHardwareIntrinsics = use;
    }

    @Override
    public void internalStart() throws FrameGrabber.Exception {

        if (useColor) {
            grabber.setImageWidth(colorCamera.width());
            grabber.setImageHeight(colorCamera.height());
            grabber.setFrameRate(colorCamera.getFrameRate());
            grabber.enableColorStream();
        }

        if (useIR) {
            grabber.setIRImageWidth(IRCamera.width());
            grabber.setIRImageHeight(IRCamera.height());
            grabber.setIRFrameRate(IRCamera.getFrameRate());
            grabber.enableIRStream();
        }

        if (useDepth) {
            grabber.setDepthImageWidth(depthCamera.width());
            grabber.setDepthImageHeight(depthCamera.height());
            grabber.setDepthFrameRate(depthCamera.getFrameRate());
            grabber.setPreset(5);
            grabber.enableDepthStream();
        }
        grabber.start();

        // Override the calibration... 
        if (useHardwareIntrinsics) {
            if (useColor) {
                useHarwareIntrinsics(colorCamera, grabber);
            }
            if (useIR) {
                useHarwareIntrinsics(IRCamera, grabber);
            }
            if (useDepth) {
                useHarwareIntrinsics(depthCamera, grabber);
            }
        }

    }

    @Override
    public void internalGrab() throws Exception {
        grabber.grab();
    }

    public Frame grabFrame() {
        try {
            return grabber.grab();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraRealSense.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public float getDepthScale() {
        return grabber.getDepthScale();
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                grabber.stop();
                System.out.println("Stopping grabber (RealSense)");

            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

    @Override
    public void grabColor() {
        opencv_core.IplImage video = grabber.grabVideo();
        if (video != null) {
            colorCamera.updateCurrentImage(video);
        }
    }

    @Override
    public void grabIR() {
        IRCamera.updateCurrentImage(grabber.grabIR());
    }

    @Override
    public void grabDepth() {
        depthCamera.updateCurrentImage(grabber.grabDepth());
        // update the touch input

        if (getActingCamera() == IRCamera) {
            ((WithTouchInput) depthCamera).newTouchImageWithColor(IRCamera.currentImage);
            return;
        }
        if (getActingCamera() == colorCamera || useColor) {
            ((WithTouchInput) depthCamera).newTouchImageWithColor(colorCamera.currentImage);
            return;
        }
        ((WithTouchInput) depthCamera).newTouchImage();
    }

    @Override
    public void setUseIR(boolean use) {
        if (use) {
            IRCamera.setSize(640, 480);
            IRCamera.setPixelFormat(PixelFormat.GRAY);
            IRCamera.setFrameRate(30);
        } else {
            grabber.disableIRStream();
        }
        this.useIR = use;
    }

    @Override
    public void setUseDepth(boolean use) {
        if (use) {
            depthCamera.setPixelFormat(PixelFormat.REALSENSE_Z16);
            depthCamera.setSize(640, 480);
            depthCamera.setFrameRate(30);
        } else {
            grabber.disableDepthStream();
        }
        this.useDepth = use;
    }

    @Override
    public void setUseColor(boolean use) {
        // todo: boolean or not ?
        if (!use) {
            grabber.disableColorStream();
        } else {
            colorCamera.setPixelFormat(PixelFormat.RGB);

            // default values
//            colorCamera.setSize(1280, 720);
            if (colorCamera.width() == 1280) {
                grabber.setFrameRate(60);
            }

            // Default values to get Color with multi-touch depth tracking. 
//            this.setSize(1280, 720);
        }
        this.useColor = use;
    }

    @Override
    public void setSize(int w, int h) {
        Camera act = getActingCamera();

        if (act == null) {

            // it is likely that the set size was for the color camera then.
            if (useColor) {
                colorCamera.setSize(w, h);
            }
            return;
        } else {
            act.setSize(w, h);
        }
    }

    public PMatrix3D getHardwareExtrinsics() {
        RealSense.extrinsics extrinsics = grabber.getRealSenseDevice().get_extrinsics(RealSense.color, RealSense.depth);
        FloatBuffer fb = extrinsics.position(0).asByteBuffer().asFloatBuffer();
        return new PMatrix3D(
                fb.get(0), fb.get(3), fb.get(6), -fb.get(9) * 1000f,
                fb.get(1), fb.get(4), fb.get(7), fb.get(10) * 1000f,
                fb.get(2), fb.get(5), fb.get(8), fb.get(11) * 1000f,
                0, 0, 0, 1);
    }

    public static void useHarwareIntrinsics(SubCamera camera, RealSenseFrameGrabber grabber) {
        if (camera == null || camera == Camera.INVALID_CAMERA) {
            return;
        }
        int camType = 0;
        if (camera.type == SubCamera.Type.COLOR) {
            camType = RealSense.color;
        }
        if (camera.type == SubCamera.Type.IR) {
            camType = RealSense.infrared;
        }
        if (camera.type == SubCamera.Type.DEPTH) {
            camType = RealSense.depth;
        }
        RealSense.intrinsics intrinsics = grabber.getRealSenseDevice().get_stream_intrinsics(camType);
        FloatBuffer fb = intrinsics.position(0).asByteBuffer().asFloatBuffer();
        float cx = fb.get(2);
        float cy = fb.get(3);
        float fx = fb.get(4);
        float fy = fb.get(5);
        camera.setSimpleCalibration(fx, fy, cx, cy);
    }
}
