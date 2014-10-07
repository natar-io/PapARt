/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.Camera;
import java.util.concurrent.TimeUnit;
import org.bytedeco.javacpp.opencv_core;
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
    }

    public void grab() {
        if (this.isClosing()) {
            return;
        }

        if (this.captureIpl.available()) {
            captureIpl.read();
            IplImage img = captureIpl.getIplImage();
            if (img != null) {
                updateCurrentImage(img);
            }
        } else {  // sleep for a short time..
            waitForNextFrame();
        }
    }

    @Override
    public PImage getPImage() {
        return this.captureIpl;
    }

    private void waitForNextFrame() {
        try {
            // TimeUnit.MILLISECONDS.sleep((long) (1f / frameRate));
            TimeUnit.MILLISECONDS.sleep((long) (10));
        } catch (Exception e) {
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
