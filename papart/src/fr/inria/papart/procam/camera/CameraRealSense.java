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
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.RealSenseFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSense extends Camera {

    protected RealSenseFrameGrabber grabber;
    protected CameraRealSenseDepth depthCamera;
    protected CameraRealSenseColor colorCamera;
    protected CameraRealSenseIR IRCamera;
    private boolean useIR = true;
    private boolean useDepth = true;
    private boolean useColor = true;

    protected CameraRealSense(int cameraNo) {
        try {
            RealSenseFrameGrabber.tryLoad();
        } catch (FrameGrabber.Exception ex) {
            Logger.getLogger(CameraRealSense.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.systemNumber = cameraNo;

        grabber = new RealSenseFrameGrabber(this.systemNumber);

        depthCamera = new CameraRealSenseDepth(this);
        colorCamera = new CameraRealSenseColor(this);
        IRCamera = new CameraRealSenseIR(this);
    }

    @Override
    public void setParent(PApplet parent) {
        super.setParent(parent);
        depthCamera.setParent(parent);
        IRCamera.setParent(parent);
        colorCamera.setParent(parent);
    }

    public RealSenseFrameGrabber getFrameGrabber() {
        return this.grabber;
    }

    public CameraRealSenseDepth getDepthCamera() {
        return this.depthCamera;
    }

    public CameraRealSenseIR getIRCamera() {
        return this.IRCamera;
    }

    public CameraRealSenseColor getColorCamera() {
        return this.colorCamera;
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

    public void useDepth(boolean useDepth) {
        this.useDepth = useDepth;
    }

    public void useIR(boolean useIR) {
        this.useIR = useIR;
    }

    public void useColor(boolean useColor) {
        this.useColor = useColor;
    }

    public void setSize(int w, int h) {
        colorCamera.setSize(w, h);
    }

    @Override
    public void start() {

        if (useDepth) {
            depthCamera.start();
        }
        if (useColor) {
            colorCamera.start();
        }
        if (useIR) {
            IRCamera.start();
        }

        try {
            grabber.start();
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);
            System.err.println("Could not camera start frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

            this.grabber = null;
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        // update the images.
        try {
            grabber.grab();

            // get the color
//            IplImage img = grabber.grabVideo();
//            updateCurrentImage(img);
//            currentImage = img;
//             Auto grab ? 
//            
            if (useDepth) {
                depthCamera.grab();
            }
            if (useColor) {
                colorCamera.grab();
            }
            if (useIR) {
                IRCamera.grab();
            }
        } catch (Exception e) {
            System.out.println("Exception :" + e);
            e.printStackTrace();
        }
    }

    public IplImage getDepthImage() {
        return depthCamera.getIplImage();
    }

    public PImage getDepthPImage() {
        return depthCamera.getPImage();
    }

    public PImage getColorImage() {
        return colorCamera.getPImage();
    }

    public PImage getIRImage() {
        return IRCamera.getPImage();
    }

    @Override
    public PImage getPImage() {
        if (useColor) {
            return colorCamera.getPImage();
        }
        if (useDepth) {
            return IRCamera.getPImage();
        }
        return null;
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

}
