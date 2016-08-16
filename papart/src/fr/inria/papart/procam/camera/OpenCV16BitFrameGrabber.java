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

import java.io.File;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.CV_16UC1;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_GRAY2BGR;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import org.bytedeco.javacpp.opencv_videoio;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_CONVERT_RGB;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FORMAT;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FOURCC;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FPS;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_COUNT;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_FRAME_WIDTH;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_MODE;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_POS_FRAMES;
import static org.bytedeco.javacpp.opencv_videoio.CV_CAP_PROP_POS_MSEC;
import static org.bytedeco.javacpp.opencv_videoio.cvCreateCameraCapture;
import static org.bytedeco.javacpp.opencv_videoio.cvCreateFileCapture;
import static org.bytedeco.javacpp.opencv_videoio.cvGetCaptureProperty;
import static org.bytedeco.javacpp.opencv_videoio.cvGrabFrame;
import static org.bytedeco.javacpp.opencv_videoio.cvQueryFrame;
import static org.bytedeco.javacpp.opencv_videoio.cvReleaseCapture;
import static org.bytedeco.javacpp.opencv_videoio.cvRetrieveFrame;
import static org.bytedeco.javacpp.opencv_videoio.cvSetCaptureProperty;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 *
 * @author Samuel Audet
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class OpenCV16BitFrameGrabber extends FrameGrabber {

    public static String[] getDeviceDescriptions() throws FrameGrabber.Exception {
        tryLoad();
        throw new UnsupportedOperationException("Device enumeration not support by OpenCV.");
    }

    public static OpenCV16BitFrameGrabber createDefault(File deviceFile) throws FrameGrabber.Exception {
        return new OpenCV16BitFrameGrabber(deviceFile);
    }

    public static OpenCV16BitFrameGrabber createDefault(String devicePath) throws FrameGrabber.Exception {
        return new OpenCV16BitFrameGrabber(devicePath);
    }

    public static OpenCV16BitFrameGrabber createDefault(int deviceNumber) throws FrameGrabber.Exception {
        return new OpenCV16BitFrameGrabber(deviceNumber);
    }

    private static FrameGrabber.Exception loadingException = null;

    public static void tryLoad() throws FrameGrabber.Exception {
        if (loadingException != null) {
            throw loadingException;
        } else {
            try {
                Loader.load(org.bytedeco.javacpp.opencv_highgui.class);
            } catch (Throwable t) {
                throw loadingException = new FrameGrabber.Exception("Failed to load " + OpenCV16BitFrameGrabber.class, t);
            }
        }
    }

    public OpenCV16BitFrameGrabber(int deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public OpenCV16BitFrameGrabber(File file) {
        this(file.getAbsolutePath());
    }

    public OpenCV16BitFrameGrabber(String filename) {
        this.filename = filename;
    }

    public void release() throws FrameGrabber.Exception {
        stop();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    private int deviceNumber = 0;
    private String filename = null;
    private opencv_videoio.CvCapture capture = null;
    private opencv_core.IplImage return_image = null;
    private FrameConverter converter = new OpenCVFrameConverter.ToIplImage();

    @Override
    public double getGamma() {
        // default to a gamma of 2.2 for cheap Webcams, DV cameras, etc.
        if (gamma == 0.0) {
            return 2.2;
        } else {
            return gamma;
        }
    }

    @Override
    public String getFormat() {
        if (capture == null) {
            return super.getFormat();
        } else {
            int fourcc = (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FOURCC);
            return "" + (char) (fourcc & 0xFF)
                    + (char) ((fourcc >> 8) & 0xFF)
                    + (char) ((fourcc >> 16) & 0xFF)
                    + (char) ((fourcc >> 24) & 0xFF);
        }
    }

    @Override
    public int getImageWidth() {
        if (return_image != null) {
            return return_image.width();
        } else {
            return capture == null ? super.getImageWidth() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH);
        }
    }

    @Override
    public int getImageHeight() {
        if (return_image != null) {
            return return_image.height();
        } else {
            return capture == null ? super.getImageHeight() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT);
        }
    }

    @Override
    public int getPixelFormat() {
        return capture == null ? super.getPixelFormat() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB);
    }

    @Override
    public double getFrameRate() {
        return capture == null ? super.getFrameRate() : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FPS);
    }

    @Override
    public void setImageMode(FrameGrabber.ImageMode imageMode) {
        if (imageMode != this.imageMode) {
            return_image = null;
        }
        super.setImageMode(imageMode);
    }

    @Override
    public int getFrameNumber() {
        return capture == null ? super.getFrameNumber()
                : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_POS_FRAMES);
    }

    @Override
    public void setFrameNumber(int frameNumber) throws FrameGrabber.Exception {
        if (capture == null) {
            super.setFrameNumber(frameNumber);
        } else {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_POS_FRAMES, frameNumber) == 0) {
                throw new FrameGrabber.Exception("cvSetCaptureProperty() Error: Could not set CV_CAP_PROP_POS_FRAMES to " + frameNumber + ".");
            }
        }
    }

    @Override
    public long getTimestamp() {
        return capture == null ? super.getTimestamp()
                : Math.round(cvGetCaptureProperty(capture, CV_CAP_PROP_POS_MSEC) * 1000);
    }

    @Override
    public void setTimestamp(long timestamp) throws FrameGrabber.Exception {
        if (capture == null) {
            super.setTimestamp(timestamp);
        } else {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_POS_MSEC, timestamp / 1000.0) == 0) {
                throw new FrameGrabber.Exception("cvSetCaptureProperty() Error: Could not set CV_CAP_PROP_POS_MSEC to " + timestamp / 1000.0 + ".");
            }
        }
    }

    @Override
    public int getLengthInFrames() {
        return capture == null ? super.getLengthInFrames()
                : (int) cvGetCaptureProperty(capture, CV_CAP_PROP_FRAME_COUNT);
    }

    @Override
    public long getLengthInTime() {
        return Math.round(getLengthInFrames() * 1000000L / getFrameRate());
    }

    public void start() throws FrameGrabber.Exception {
        if (filename != null && filename.length() > 0) {
            capture = cvCreateFileCapture(filename);
            if (capture == null) {
                throw new FrameGrabber.Exception("cvCreateFileCapture() Error: Could not create camera capture.");
            }
        } else {
            capture = cvCreateCameraCapture(deviceNumber);
            if (capture == null) {
                throw new FrameGrabber.Exception("cvCreateCameraCapture() Error: Could not create camera capture.");
            }
        }
        if (imageWidth > 0) {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, imageWidth) == 0) {
                cvSetCaptureProperty(capture, CV_CAP_PROP_MODE, imageWidth); // ??
            }
        }
        if (imageHeight > 0) {
            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, imageHeight) == 0) {
                cvSetCaptureProperty(capture, CV_CAP_PROP_MODE, imageHeight); // ??
            }
        }
        if (frameRate > 0) {
            cvSetCaptureProperty(capture, CV_CAP_PROP_FPS, frameRate);
        }

        // set the fourCC to Y16 ->  V4L2_PIX_FMT_Y16; 
        cvSetCaptureProperty(capture, CV_CAP_PROP_FOURCC, new FourCC("Y16 ").toInt());
        cvSetCaptureProperty(capture, CV_CAP_PROP_FORMAT, CV_16UC1); 

        // Take the gray images...
//        cvSetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB, imageMode == FrameGrabber.ImageMode.COLOR ? 1 : 0);
        cvSetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB, -1);
//        cvSetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB, 0);

        // Before cvRetrieveFrame() starts returning something else then null
        // QTKit sometimes requires some "warm-up" time for some reason...
        // The first frame on Linux is sometimes null as well, 
        // so it's probably a good idea to run this for all platforms... ?
        int count = 0;
        while (count++ < 100 && cvGrabFrame(capture) != 0 && cvRetrieveFrame(capture) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }

        if (!triggerMode) {
            int err = cvGrabFrame(capture);
            if (err == 0) {
                throw new FrameGrabber.Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
            }
        }
    }

    public void stop() throws FrameGrabber.Exception {
        if (capture != null) {
            cvReleaseCapture(capture);
            capture = null;
        }
    }

    public void trigger() throws FrameGrabber.Exception {
        for (int i = 0; i < numBuffers + 1; i++) {
            cvQueryFrame(capture);
        }
        int err = cvGrabFrame(capture);
        if (err == 0) {
            throw new FrameGrabber.Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
        }
    }

    public Frame grab() throws FrameGrabber.Exception {
        opencv_core.IplImage image = cvRetrieveFrame(capture);
        
//        System.out.println("Image data size: " + image.getByteBuffer().capacity());
        
        if (image == null) {
            throw new FrameGrabber.Exception("cvRetrieveFrame() Error: Could not retrieve frame. (Has start() been called?)");
        }
        if (!triggerMode) {
            int err = cvGrabFrame(capture);
            if (err == 0) {
                throw new FrameGrabber.Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
            }
        }

        if (return_image == null) {
            System.out.println("Creating in image with w, h: " + image.width() + " " + image.height());
            System.out.println("Creating in image with depth: " + image.depth());
            return_image = opencv_core.IplImage.create(image.width(), image.height(), image.depth(), 3);
        }
        return_image = image;
        return converter.convert(return_image);
    }
}
