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
import org.bytedeco.javacpp.FlyCapture2;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvSize;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.FlyCapture2FrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public class CameraFlyCapture extends Camera {

    private FrameGrabber grabber;
    private boolean useBayerDecode = false;

    protected CameraFlyCapture(int cameraNo) {
        this.systemNumber = cameraNo;
        this.setPixelFormat(PixelFormat.BGR);
    }

    @Override
    public void start() {
        try {
            FlyCapture2FrameGrabber grabberFly = new FlyCapture2FrameGrabber(this.systemNumber);
            grabberFly.setImageWidth(width());
            grabberFly.setImageHeight(height());

            if (useBayerDecode) {
                grabberFly.setImageMode(FrameGrabber.ImageMode.GRAY);

            } else {
                // Hack for now ... 
                // real Gray colors are not supported by Processing anyway !
                grabberFly.setImageMode(FrameGrabber.ImageMode.COLOR);
            }
            this.grabber = grabberFly;
            grabberFly.start();
        } catch (Exception e) {
            System.err.println("Could not start FlyCapture frameGrabber... " + e);
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            IplImage img = grabber.grab();

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
            debayer = opencv_core.cvCreateImage(outSize, opencv_core.IPL_DEPTH_8U, 3);
        }

        opencv_imgproc.cvCvtColor(source, debayer, opencv_imgproc.CV_BayerBG2BGR);
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
            } catch (Exception e) {
            }
        }
    }

}
