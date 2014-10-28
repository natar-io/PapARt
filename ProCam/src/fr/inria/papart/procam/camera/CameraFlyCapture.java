/*
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.Utils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FlyCaptureFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import processing.core.PImage;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.FlyCapture2.Error;
import static org.bytedeco.javacpp.FlyCapture2.*;
import org.bytedeco.javacv.FlyCapture2FrameGrabber;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public class CameraFlyCapture extends Camera {

    Error error;
    BusManager busMgr;
    private int nbCameras = 0;
    FlyCapture2.Camera cam = new FlyCapture2.Camera();
    Image rawImage = new Image();
    FlyCapture2FrameGrabber grabber;

    protected CameraFlyCapture(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
    }

    static void PrintError(Error error) {
        error.PrintErrorTrace();
    }

    static void PrintCameraInfo(CameraInfo pCamInfo) {
        System.out.println(
                "\n*** CAMERA INFORMATION ***\n"
                + "Serial number - " + pCamInfo.serialNumber() + "\n"
                + "Camera model - " + pCamInfo.modelName().getString() + "\n"
                + "Camera vendor - " + pCamInfo.vendorName().getString() + "\n"
                + "Sensor - " + pCamInfo.sensorInfo().getString() + "\n"
                + "Resolution - " + pCamInfo.sensorResolution().getString() + "\n"
                + "Firmware version - " + pCamInfo.firmwareVersion().getString() + "\n"
                + "Firmware build time - " + pCamInfo.firmwareBuildTime().getString() + "\n");
    }

    @Override
    public void start() {
        try {
            grabber = new FlyCapture2FrameGrabber(0);
            grabber.start();
        } catch (Exception e) {
            System.err.println("FlyCapture camera error: " + e);
        }
    }

    @Override
    public void grab() {
        if (this.isClosing()) {
            return;
        }
        try {
            opencv_core.IplImage img = grabber.grab();
            if (img != null) {
                this.updateCurrentImage(img);
            }

        } catch (Exception e) {
            System.err.println("Camera: FlyCapture Grab() Error ! " + e);
        }
    }

    @Override
    public opencv_core.IplImage getIplImage() {
        return currentImage;
    }

    @Override
    public PImage getPImage() {
        // currentImage must be set ...
        if (currentImage != null) {
            this.checkCamImage();
            camImage.update(currentImage);
            return camImage;
        }
        return null;
    }


    
    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                this.stopThread();
                grabber.stop();
            } catch (Exception e) {
            }
        }
    }

}
