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
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.OpenKinectFrameGrabber;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenKinect extends Camera implements Displayable {

    private boolean isGrabbingDepth = false;
    protected OpenKinectFrameGrabber grabber;

    int kinectVideoFormat;
    private final CameraOpenKinectDepth depthCamera;

    protected CameraOpenKinect(int cameraNo) {
        this.systemNumber = cameraNo;

        depthCamera = new CameraOpenKinectDepth(this);
        getRGBVideo();
    }

    public void getIRVideo() {
        this.setPixelFormat(PixelFormat.GRAY);
        kinectVideoFormat = freenect.FREENECT_VIDEO_IR_8BIT;
    }

    public void getRGBVideo() {
        this.setPixelFormat(PixelFormat.BGR);
        kinectVideoFormat = freenect.FREENECT_VIDEO_RGB;
    }

    @Override
    public void start() {
        grabber = new OpenKinectFrameGrabber(this.systemNumber);
        grabber.setImageWidth(width());
        grabber.setImageHeight(height());

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
    public void grab() {

        if (this.isClosing()) {
            return;
        }

        try {
            IplImage img = grabber.grabVideo();
            updateCurrentImage(img);

            if (this.isGrabbingDepth) {
                depthCamera.grab();
            }

        } catch (Exception e) {
            System.err.println("Camera: Kinect Grab() Error ! " + e);
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
                System.out.println("Stopping KinectGrabber");
                this.stopThread();
                grabber.stop();
                if (this.isGrabbingDepth) {
                    depthCamera.close();
                }
            } catch (Exception e) {
            }
        }

    }

    public void setTouch(KinectTouchInput touchInput) {
        this.setGrabDepth(true);
        depthCamera.setTouchInput(touchInput);
    }

    public CameraOpenKinectDepth getDepthCamera() {
        this.setGrabDepth(true);
        return this.depthCamera;
    }

    public void setGrabDepth(boolean grabDepth) {
        this.isGrabbingDepth = grabDepth;
    }

    HashMap<PApplet, PImage> imageMap = new HashMap();

    @Override
    public void prepareToDisplayOn(PApplet display) {
        PImage image = display.createImage(this.width, this.height, RGB);
        imageMap.put(display, image);
    }

    @Override
    public PImage getDisplayedOn(PApplet display) {
        PImage image = imageMap.get(display);
        Utils.IplImageToPImage(currentImage, false, image);
        return image;
    }

    @Override
    public boolean canBeDisplayedOn(PApplet display) {
        return imageMap.containsKey(display);
    }

}
