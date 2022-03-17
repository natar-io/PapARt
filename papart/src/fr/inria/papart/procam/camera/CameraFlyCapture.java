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

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_core.CvSize;
import org.bytedeco.opencv.opencv_core.IplImage;
import static org.bytedeco.opencv.global.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.opencv.opencv_imgproc.*;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

import org.bytedeco.javacv.*; // FlyCapture2FrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraFlyCapture extends Camera {

    private org.bytedeco.javacv.FrameGrabber grabber;
    private boolean useBayerDecode = true;
    private final org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage converter;

    protected CameraFlyCapture(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
        converter = new org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage();
    }

    @Override
    public void start() {
        try {
            FlyCapture2FrameGrabber grabberFly = new FlyCapture2FrameGrabber(this.systemNumber);
            grabberFly.setImageWidth(width());
            grabberFly.setImageHeight(height());

            if (useBayerDecode) {
                grabberFly.setImageMode(org.bytedeco.javacv.FrameGrabber.ImageMode.GRAY);

            } else {
                // Hack for now ... 
                // real Gray colors are not supported by Processing anyway !
                grabberFly.setImageMode(org.bytedeco.javacv.FrameGrabber.ImageMode.COLOR);
            }
            this.grabber = grabberFly;
            grabberFly.start();
            this.isConnected = true;
            
        } catch (Exception e) {
            System.err.println("Could not start FlyCapture frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            IplImage img = converter.convert(grabber.grab());

            img = checkBayer(img);

            if (img != null) {
                this.updateCurrentImage(img);
            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
    }

    private IplImage debayer = null;

    private IplImage checkBayer(IplImage source) {
        if (!useBayerDecode) {
            return source;
        }

        if (debayer == null) {
            CvSize outSize = new CvSize();
            outSize.width(source.width());
            outSize.height(source.height());
            debayer = cvCreateImage(outSize, IPL_DEPTH_8U, 3);
        }

        cvCvtColor(source, debayer, CV_BayerBG2BGR);
        return debayer;
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

    public void setBayerDecode(boolean isBayer) {
        this.useBayerDecode = isBayer;
    }

    @Override
    public void close() {
        this.setClosing();
        if (debayer != null) {
            debayer.deallocate();
        }
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
            }
        }
    }

}
