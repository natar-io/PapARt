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
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.RealSenseFrameGrabber;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSense extends CameraRGBIRDepth {

    protected RealSenseFrameGrabber grabber;

    protected CameraRealSense(int cameraNo) {
        try {
            RealSenseFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraRealSense.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.systemNumber = cameraNo;

    }

    public RealSenseFrameGrabber getFrameGrabber() {
        return this.grabber;
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

    @Override
    public void internalInit() {

        if (isUseDepth()) {
            depthCamera.setPixelFormat(PixelFormat.REALSENSE_Z16);
            depthCamera.setSize(640, 480);
            depthCamera.type = SubCamera.Type.DEPTH;
        }
        if (isUseColor()) {
            colorCamera.setPixelFormat(PixelFormat.RGB);
            colorCamera.type = SubCamera.Type.COLOR;
            colorCamera.setSize(960, 540);
        }
        if (isUseIR()) {
            IRCamera.setSize(640, 480);
            IRCamera.setPixelFormat(PixelFormat.GRAY);
            IRCamera.type = SubCamera.Type.IR;
        }
        grabber = new RealSenseFrameGrabber(this.systemNumber);

    }

    @Override
    public void internalStart() throws FrameGrabber.Exception {
        grabber.start();
    }

    @Override
    public void internalGrab() throws Exception {
        grabber.grab();
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

    public static void useHarwareIntrinsics(SubCamera camera, RealSenseFrameGrabber grabber) {
        if(camera == null || camera == Camera.INVALID_CAMERA){
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

    @Override
    public void grabColor() {
        System.out.println("Realsense: Grab Color");
        colorCamera.updateCurrentImage(grabber.grabVideo());
    }

    @Override
    public void grabIR() {
        IRCamera.updateCurrentImage(grabber.grabIR());
    }

    @Override
    public void grabDepth() {
        depthCamera.updateCurrentImage(grabber.grabDepth());
        // update the touch input
        ((WithTouchInput) depthCamera).newTouchImageWithColor(colorCamera.currentImage);
    }

    @Override
    public void enableIR() {
        grabber.setIRImageWidth(IRCamera.width());
        grabber.setIRImageHeight(IRCamera.height());
        grabber.enableIRStream();
    }

    @Override
    public void disableIR() {
        grabber.disableIRStream();
    }

    @Override
    public void enableDepth() {
        grabber.setDepthImageWidth(depthCamera.width());
        grabber.setDepthImageHeight(depthCamera.height());
        grabber.enableDepthStream();
    }

    @Override
    public void disableDepth() {
        grabber.disableDepthStream();
    }

    @Override
    public void enableColor() {
        grabber.setImageWidth(colorCamera.width());
        grabber.setImageHeight(colorCamera.height());
        grabber.enableColorStream();
    }

    @Override
    public void disableColor() {
        grabber.disableColorStream();
    }

}
