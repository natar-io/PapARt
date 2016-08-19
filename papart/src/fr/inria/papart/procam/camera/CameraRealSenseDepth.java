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

import fr.inria.papart.multitouch.KinectTouchInput;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraRealSenseDepth extends Camera implements WithTouchInput {

    CameraRealSense colorCamera;
    private KinectTouchInput touchInput;

    protected CameraRealSenseDepth(CameraRealSense colorCamera) {
        this.colorCamera = colorCamera;
    }

    @Override
    public void start() {
        colorCamera.grabber.setDepthImageWidth(width());
        colorCamera.grabber.setDepthImageHeight(height());
        colorCamera.grabber.enableDepthStream();
        this.isConnected = true;
    }

    @Override
    public void grab() {
     System.out.println("grab in realSense depth ");
        if (this.isClosing()) {
            return;
        }

        // update the images.
        try {
            currentImage = colorCamera.grabber.grabDepth();
            
            System.out.println("Raw DepthImage " + currentImage);
            if (touchInput != null) {
                touchInput.lock();
                touchInput.update();
                touchInput.getTouch2DColors(colorCamera.currentImage);
                touchInput.unlock();
            } else if (touchInput != null) {
                System.err.println("Error, the TouchInput is set, but no DepthImg is grabbed.");
            }
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
    public KinectTouchInput getTouchInput() {
        return touchInput;
    }

    @Override
    public void setTouchInput(KinectTouchInput touchInput) {
        this.touchInput = touchInput;
    }

    @Override
    public void close() {
        this.setClosing();
        colorCamera.useDepth(false);
        colorCamera.grabber.disableDepthStream();

    }

}
