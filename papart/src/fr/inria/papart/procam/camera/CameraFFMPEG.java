/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraFFMPEG extends Camera {

    private FFmpegFrameGrabber grabber;
    private final OpenCVFrameConverter.ToIplImage converter;
    private final String imageFormat;

    protected CameraFFMPEG(String description, String imFormat) {
        this.cameraDescription = description;
//        this.setPixelFormat(Camera.PixelFormat.RGB);
        this.imageFormat = imFormat;
        converter = new OpenCVFrameConverter.ToIplImage();
    }
    
    /**
     * TODO: Check the use of this. 
     * @deprecated
     */
    @Deprecated
    public void startVideo() {
        FFmpegFrameGrabber grabberFF = new FFmpegFrameGrabber(this.cameraDescription);
        try {
            grabberFF.setFrameRate(30);
            this.setPixelFormat(PixelFormat.BGR);
            grabberFF.start();
            this.grabber = grabberFF;
            this.setSize(grabber.getImageWidth(), grabber.getImageHeight());
//            this.setFrameRate((int) grabberFF.getFrameRate());
            grabberFF.setFrameRate(30);
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not FFMPEG frameGrabber... " + e);
            System.err.println("Camera ID " + this.cameraDescription + ":" + this.imageFormat + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");
            this.grabber = null;
        }
    }

    @Override
    public void start() {
        FFmpegFrameGrabber grabberFF = new FFmpegFrameGrabber(this.cameraDescription);

        grabberFF.setImageMode(FrameGrabber.ImageMode.COLOR);

        this.setPixelFormat(PixelFormat.BGR);

        grabberFF.setFormat(this.imageFormat);
        grabberFF.setImageWidth(width());
        grabberFF.setImageHeight(height());
        grabberFF.setFrameRate(frameRate);

        try {
            grabberFF.start();
            this.grabber = grabberFF;
            this.isConnected = true;
        } catch (FrameGrabber.Exception e) {
            System.err.println("Could not FFMPEG frameGrabber... " + e);
            System.err.println("Camera ID " + this.cameraDescription + ":" + this.imageFormat + " could not start.");
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

            if (grabber.getLengthInFrames() != 0) {
                checkEndOfVideo();
            }
            this.updateCurrentImage(converter.convertToIplImage(grabber.grab()));

        } catch (Exception e) {
            if (this.isClosing()) {

            } else {
                System.err.println("Camera: FFMPEG Grab() Error ! " + e);
                e.printStackTrace();
            }
        }
    }

    private void checkEndOfVideo() {
        // 10 frames from the end.
        if (grabber.getFrameNumber() + 10 > grabber.getLengthInFrames()) {
            try {
                grabber.setFrameNumber(0);
            } catch (FrameGrabber.Exception ex) {
                Logger.getLogger(CameraFFMPEG.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                System.out.println("Stopping grabber (FFMPEG)");

            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

}
