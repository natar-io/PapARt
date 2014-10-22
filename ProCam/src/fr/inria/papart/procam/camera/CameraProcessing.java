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
