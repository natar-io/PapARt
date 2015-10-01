/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import java.util.concurrent.TimeUnit;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraProcessing extends Camera {

    protected CaptureIpl captureIpl;

    protected CameraProcessing(String description) {
        this.cameraDescription = description;
        this.setPixelFormat(PixelFormat.ARGB);
    }

    @Override
    public void start() {

        if (cameraDescription == null) {
            System.out.println("Starting capture !");
            this.captureIpl = new CaptureIpl(parent, width, height);
        } else {

            System.out.println("Starting capture on device " + cameraDescription);
            this.captureIpl = new CaptureIpl(parent, width, height, cameraDescription);
        }

        this.captureIpl.start();
        this.isConnected = true;
    }

    @Override
    public void grab() {
        if (this.isClosing()) {
            return;
        }
        while (!this.captureIpl.available()) {
            waitForNextFrame();
        }

        captureIpl.read();
        IplImage img = captureIpl.getIplImage();
        if (img != null) {
            updateCurrentImage(img);
        }
    }

    @Override
    public PImage getPImage() {
        return this.captureIpl;
    }

    private void waitForNextFrame() {
        try {
            // TimeUnit.MILLISECONDS.sleep((long) (1f / frameRate));
            TimeUnit.MILLISECONDS.sleep((long) (5));
        } catch (Exception e) {
            System.out.println("Sleep interrupted." + e);
        }
    }

    @Override
    public void close() {
        this.setClosing();
        if (captureIpl != null) {
            captureIpl.stop();
        }
    }
}
