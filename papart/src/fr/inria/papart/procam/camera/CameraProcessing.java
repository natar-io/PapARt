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

import java.util.concurrent.TimeUnit;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PImage;

/**
 *
 * @author Jeremy Laviole
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
            this.captureIpl = new CaptureIpl(parent, width, height);
        } else {
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
