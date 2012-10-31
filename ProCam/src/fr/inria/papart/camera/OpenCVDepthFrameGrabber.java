/*
 * Copyright (C) 2009,2010,2011,2012 Samuel Audet
 *
 * This file is part of JavaCV.
 *
 * JavaCV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * JavaCV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JavaCV.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.camera;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

/**
 *
 * @author Samuel Audet
 */
public class OpenCVDepthFrameGrabber extends FrameGrabber {

    public static String[] getDeviceDescriptions() throws FrameGrabber.Exception {
        tryLoad();
        throw new UnsupportedOperationException("Device enumeration not support by OpenCV.");
    }
    private static FrameGrabber.Exception loadingException = null;

    public static void tryLoad() throws FrameGrabber.Exception {
        if (loadingException != null) {
            throw loadingException;
        } else {
            try {
                Loader.load(com.googlecode.javacv.cpp.opencv_highgui.class);
            } catch (Throwable t) {
                // TODO: add this class in JavaCV
                //  throw loadingException = new FrameGrabber.Exception("Failed to load " + com.googlecode.javacv.OpenCVDepthFrameGrabber.class, t);
            }
        }
    }

    public enum DepthCameraType {

        KINECT, XTION
    }

    public enum DepthImageMode {

        RGB, DEPTH, IR
    }

    public OpenCVDepthFrameGrabber(DepthCameraType type) {
        switch (type) {
            case KINECT:
                this.deviceNumber = opencv_highgui.CV_CAP_OPENNI;
                break;
            case XTION:
                this.deviceNumber = opencv_highgui.CV_CAP_OPENNI; // _ASUS;
                break;
        }

        this.imageWidth = 640;
        this.imageHeight = 480;
    }

    // Not supported yet
//    public OpenCVDepthFrameGrabber(File file) {
//        this(file.getAbsolutePath());
//    }
    // Not supported yet
//    public OpenCVDepthFrameGrabber(String filename) {
//        this.filename = filename;
//    }
    public void release() throws FrameGrabber.Exception {
        stop();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }
    private static final boolean macosx = Loader.getPlatformName().startsWith("macosx");
    private int deviceNumber = 0;
    private String filename = null;
    private CvCapture capture = null;
    private IplImage return_image = null;
    private DepthImageMode depthImageMode = DepthImageMode.DEPTH;

    @Override
    public double getGamma() {
        // default to a gamma of 2.2 for cheap Webcams, DV cameras, etc.
        if (gamma == 0.0) {
            return 2.2;
        } else {
            return gamma;
        }
    }

    // TODO: check the format
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

    public void setDepthImageMode(DepthImageMode imageMode) {
        this.depthImageMode = imageMode;
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
        return capture == null ? super.getFrameNumber()
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
        return Math.round(getLengthInFrames() * 1000000 / getFrameRate());
    }

    public void start() throws FrameGrabber.Exception {

        // Files not supported yet
//        if (filename != null && filename.length() > 0) {
//            capture = cvCreateFileCapture(filename);
//            if (capture == null) {
//                throw new FrameGrabber.Exception("cvCreateFileCapture() Error: Could not create camera capture.");
//            }
//        } else {



        capture = cvCreateCameraCapture(deviceNumber);
        capture = cvCreateCameraCapture(deviceNumber);
        if (capture == null) {
            throw new FrameGrabber.Exception("cvCreateCameraCapture() Error: Could not create camera capture.");
        }

        // Set the capture to VGA 30Hz
        cvSetCaptureProperty(capture, CV_CAP_OPENNI_IMAGE_GENERATOR_OUTPUT_MODE, CV_CAP_OPENNI_VGA_30HZ);

        // resolutions from the example
//                modeRes = capture.set( CV_CAP_OPENNI_IMAGE_GENERATOR_OUTPUT_MODE, CV_CAP_OPENNI_VGA_30HZ );
//                modeRes = capture.set( CV_CAP_OPENNI_IMAGE_GENERATOR_OUTPUT_MODE, CV_CAP_OPENNI_SXGA_15HZ );
//                modeRes = capture.set( CV_CAP_OPENNI_IMAGE_GENERATOR_OUTPUT_MODE, CV_CAP_OPENNI_SXGA_30HZ );


//        }
//        if (imageWidth > 0) {
//            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_WIDTH, imageWidth) == 0) {
//                cvSetCaptureProperty(capture, CV_CAP_PROP_MODE, imageWidth); // ??
//            }
//        }
//        if (imageHeight > 0) {
//            if (cvSetCaptureProperty(capture, CV_CAP_PROP_FRAME_HEIGHT, imageHeight) == 0) {
//                cvSetCaptureProperty(capture, CV_CAP_PROP_MODE, imageHeight); // ??
//            }
//        }
//        if (frameRate > 0) {
//            cvSetCaptureProperty(capture, CV_CAP_PROP_FPS, frameRate);
//        }
//        if (bpp > 0) {
//            cvSetCaptureProperty(capture, CV_CAP_PROP_FORMAT, bpp); // ??
//        }
//        cvSetCaptureProperty(capture, CV_CAP_PROP_CONVERT_RGB, imageMode == FrameGrabber.ImageMode.COLOR ? 1 : 0);



        if (macosx) {
            // Before cvRetrieveFrame() starts returning something else then null
            // QTKit sometimes requires some "warm-up" time for some reason...
            int count = 0;
            while (count++ < 100 && cvGrabFrame(capture) != 0 && cvRetrieveFrame(capture) == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
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

    public IplImage grab() throws FrameGrabber.Exception {

//        IplImage image = cvRetrieveFrame(capture);


//        if (image == null) {
//            throw new FrameGrabber.Exception("cvRetrieveFrame() Error: Could not retrieve frame. (Has start() been called?)");
//        }
//        if (!triggerMode) {
//            int err = cvGrabFrame(capture);
//            if (err == 0) {
//                throw new FrameGrabber.Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
//            }
//        }

        IplImage image = null;

        // TODO: all image modes, with flags instead. 
        switch (this.depthImageMode) {
            case DEPTH:

                image = cvRetrieveFrame(capture, CV_CAP_OPENNI_DEPTH_MAP);

                int err = cvGrabFrame(capture);
                if (err == 0) {
                    throw new Exception("cvGrabFrame() Error: Could not grab frame. (Has start() been called?)");
                }

                break;
        }

        return image;

//            if( retrievedImageFlags[0] && capture.retrieve( depthMap, CV_CAP_OPENNI_DEPTH_MAP ) )
//            {
//                const float scaleFactor = 0.05f;
//                Mat show; depthMap.convertTo( show, CV_8UC1, scaleFactor );
//                imshow( "depth map", show );
//            }
//
//            if( retrievedImageFlags[1] && capture.retrieve( disparityMap, CV_CAP_OPENNI_DISPARITY_MAP ) )
//            {
//                if( isColorizeDisp )
//                {
//                    Mat colorDisparityMap;
//                    colorizeDisparity( disparityMap, colorDisparityMap, isFixedMaxDisp ? getMaxDisparity(capture) : -1 );
//                    Mat validColorDisparityMap;
//                    colorDisparityMap.copyTo( validColorDisparityMap, disparityMap != 0 );
//                    imshow( "colorized disparity map", validColorDisparityMap );
//                }
//                else
//                {
//                    imshow( "original disparity map", disparityMap );
//                }
//            }
//
//            if( retrievedImageFlags[2] && capture.retrieve( validDepthMap, CV_CAP_OPENNI_VALID_DEPTH_MASK ) )
//                imshow( "valid depth mask", validDepthMap );
//
//            if( retrievedImageFlags[3] && capture.retrieve( bgrImage, CV_CAP_OPENNI_BGR_IMAGE ) )
//                imshow( "rgb image", bgrImage );
//
//            if( retrievedImageFlags[4] && capture.retrieve( grayImage, CV_CAP_OPENNI_GRAY_IMAGE ) )
//                imshow( "gray image", grayImage );
//        }




//        if (imageMode == FrameGrabber.ImageMode.GRAY && image.nChannels() > 1) {
//            if (return_image == null) {
//                return_image = IplImage.create(image.width(), image.height(), image.depth(), 1);
//            }
//            cvCvtColor(image, return_image, CV_BGR2GRAY);
//        } else if (imageMode == FrameGrabber.ImageMode.COLOR && image.nChannels() == 1) {
//            if (return_image == null) {
//                return_image = IplImage.create(image.width(), image.height(), image.depth(), 3);
//            }
//            cvCvtColor(image, return_image, CV_GRAY2BGR);
//        } else {
//            return_image = image;
//        }
//        return return_image;
    }
}
