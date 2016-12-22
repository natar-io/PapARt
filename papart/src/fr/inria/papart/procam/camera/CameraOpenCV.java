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
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenCV extends Camera {

    private OpenCVFrameGrabber grabber;
    private final OpenCVFrameConverter.ToIplImage converter;

    protected CameraOpenCV(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
        converter = new OpenCVFrameConverter.ToIplImage();
    }

    @Override
    public void start() {
        OpenCVFrameGrabber grabberCV = new OpenCVFrameGrabber(this.systemNumber);
        grabberCV.setImageWidth(width());
        grabberCV.setImageHeight(height());
        grabberCV.setFrameRate(frameRate);
        grabberCV.setImageMode(FrameGrabber.ImageMode.COLOR);
 
        try {
            grabberCV.start();
            this.grabber = grabberCV;
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
        try {
            IplImage img = converter.convertToIplImage(grabber.grab());
            if (img != null) {
                this.updateCurrentImage(img);
            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
    }

    @Override
    public PImage getPImage() {

        if (currentImage != null) {
            this.checkCamImage();
            camImage.update(currentImage);
            return camImage;
        }
        // TODO: exceptions !!!
        return null;
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                grabber.stop();
                System.out.println("Stopping grabber (OpencV)");
               
            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

}
