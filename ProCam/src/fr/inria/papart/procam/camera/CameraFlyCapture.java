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

        busMgr = new BusManager();
        int[] numCameras = new int[1];
        error = busMgr.GetNumOfCameras(numCameras);
        if (error.notEquals(PGRERROR_OK)) {
            throw new RuntimeException("Impossible to get the number of Fly Capture cameras.");
        }
        System.out.println("Camera Fly Capture: Number of cameras detected: " + numCameras[0]);
        nbCameras = numCameras[0];

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

            this.setPixelFormat(PixelFormat.ARGB);
            System.out.println("init grabber");
            grabber = new FlyCapture2FrameGrabber(0);
            System.out.println("init grabber OK. Start now ...");
            grabber.start();
            System.out.println("Start OK .");

           
            
//            if (this.nbCameras == 0) {
//                throw new Exception("No camera found.");
//            }
//
//            if (this.systemNumber >= this.nbCameras) {
//                throw new Exception("The camera number is not valid.");
//            }
//            PGRGuid guid = new PGRGuid();
//            error = busMgr.GetCameraFromIndex(this.systemNumber, guid);
//            if (error.notEquals(PGRERROR_OK)) {
//                throw new Exception("Impossible to get the camera. ");
//            }
//
//            // Connect to a camera
//            error = cam.Connect(guid);
//            if (error.notEquals(PGRERROR_OK)) {
//                throw new Exception("Impossible to connect to the camera.");
//            }
//
//            // Get the camera information
//            CameraInfo camInfo = new CameraInfo();
//            error = cam.GetCameraInfo(camInfo);
//            if (error.notEquals(PGRERROR_OK)) {
//                throw new Exception("Impossible to get the camera Infos");
//            }
//            PrintCameraInfo(camInfo);
//
//            allocateIplImage();
//            this.setResolution(camInfo);
//            // TODO get the pixel format.
//            this.setPixelFormat(Camera.PixelFormat.GRAY);
//
//            // Start capturing images
//            error = cam.StartCapture();
//            if (error.notEquals(PGRERROR_OK)) {
//                throw new Exception("Impossible to start the camera.");
//            }
        } catch (Exception e) {
            System.err.println("FlyCapture camera error: " + e);
        }

    }

    private void allocateIplImage() {
        this.currentImage = opencv_core.cvCreateImage(
                new opencv_core.CvSize(this.width * this.height),
                opencv_core.IPL_DEPTH_8U, 1);
        this.currentImage.width(this.width);
        this.currentImage.height(this.height);
    }

    private void setCurrentImage(Image source) {
        currentImage.imageData(source.GetData());
        updateCurrentImage(currentImage);
    }

    private void setResolution(CameraInfo camInfo) {
        String resolution = camInfo.sensorResolution().getString();
        int delimiterX = camInfo.sensorResolution().getString().lastIndexOf("x");
        this.width = Integer.parseInt(resolution.substring(0, delimiterX));
        this.height = Integer.parseInt(resolution.substring(delimiterX + 1));
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
            
//            error = cam.RetrieveBuffer(rawImage);
//            if (error.notEquals(PGRERROR_OK)) {
//                throw new Exception("Impossible to retreive an image.");
//            }
//            // Create a converted image
//            Image convertedImage = new Image();
//
//            // Convert the raw image
//            error = rawImage.Convert(PIXEL_FORMAT_MONO8, convertedImage);
//            if (error.notEquals(PGRERROR_OK)) {
//                throw new Exception("Conversion error");
//            }
//
//            setCurrentImage(rawImage);
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
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

        // Stop capturing images
        error = cam.StopCapture();
        if (error.notEquals(PGRERROR_OK)) {
            PrintError(error);
        }

        // Disconnect the camera
        error = cam.Disconnect();
        if (error.notEquals(PGRERROR_OK)) {
            PrintError(error);
        }
    }

}
