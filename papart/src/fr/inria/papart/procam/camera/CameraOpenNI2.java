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

import org.bytedeco.javacv.FrameGrabber;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_imgproc;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_RGB2BGR;

import org.openni.*;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenNI2 extends CameraRGBIRDepth {

    private VideoStream colorStream, IRStream, depthStream;
    // From the OpenNI example.
    Device device;

    protected CameraOpenNI2(int cameraNo) {
        this.systemNumber = cameraNo;
//        grabber = new OpenKinectFrameGrabber(this.systemNumber);
        initDevice();
    }

    @Override
    public void internalStart() throws FrameGrabber.Exception {
        if (useColor) {
            colorStream = initStream(
                    //                    PixelFormat.BGR,
                    PixelFormat.RGB,
                    org.openni.PixelFormat.RGB888,
                    SensorType.COLOR,
                    new FrameListener(colorCamera),
                    colorCamera);

            colorStream.setMirroringEnabled(false);
            colorCamera.setUndistort(false);
            colorStream.start();
        }

        if (useIR) {
            IRStream = initStream(
                    PixelFormat.RGB,
                    org.openni.PixelFormat.RGB888,
                    SensorType.IR,
                    new FrameListener(IRCamera),
                    IRCamera);
            IRStream.setMirroringEnabled(false);
            IRCamera.setUndistort(false);
            IRStream.start();

        }

        if (useDepth) {
            depthStream = initStream(
                    PixelFormat.OPENNI_2_DEPTH,
                    org.openni.PixelFormat.DEPTH_1_MM,
                    SensorType.DEPTH,
                    new FrameListener(depthCamera),
                    depthCamera);
            depthStream.setMirroringEnabled(false);
            depthCamera.setUndistort(false);
            depthStream.start();
        }

//        grabber.start();
//
//        // Override the calibration... 
//        if (useHardwareIntrinsics) {
//            if (useColor) {
//                useHarwareIntrinsics(colorCamera, grabber);
//            }
//            if (useIR) {
//                useHarwareIntrinsics(IRCamera, grabber);
//            }
//            if (useDepth) {
//                useHarwareIntrinsics(depthCamera, grabber);
//            }
//        }
    }

    private VideoStream initStream(PixelFormat format1,
            org.openni.PixelFormat format2,
            SensorType type,
            FrameListener listener,
            Camera camera) {

        camera.setPixelFormat(format1);
        VideoStream videoStream = VideoStream.create(device, type);
        videoStream.setMirroringEnabled(false);
        VideoMode vm = new VideoMode(camera.width(),
                camera.height(),
                camera.getFrameRate(), format2.toNative());

        videoStream.setVideoMode(vm);

        videoStream.addNewFrameListener(listener);
        return videoStream;
    }

    @Override
    public void setSize(int w, int h) {
        Camera act = getActingCamera();
        if (act == null) {
            // it is likely that the set size was for the color camera then.
            if (useColor) {
                colorCamera.setSize(w, h);
            }
        } else {
            act.setSize(w, h);
        }
    }

    @Override
    public void close() {
        setClosing();
        try {
            if (this.colorStream != null) {
                colorStream.stop();
            }
            if (this.IRStream != null) {
                IRStream.stop();
            }
            if (this.depthStream != null) {
                depthStream.stop();
            }
            device.close();
            // Closing can fail due to bad thread access management.
        } catch (Exception e) {
        }
    }

    /**
     * *
     * The thread does not start, each sub camera has its own native thread.
     */
    @Override
    public void setThread() {
        if (thread == null) {
            thread = new CameraThread(this);
            thread.setCompute(trackSheets);
        } else {
            System.err.println("Camera: Error Thread already launched");
        }
    }

    @Override
    public void grabIR() {
        try {
//            System.out.println("Sleeping color cam");
//            Thread.sleep((long) ((1.0f / (float) colorCamera.frameRate) * 1000f));
            Thread.sleep((long) ((1.0f / (float) colorCamera.frameRate) * 800f));
//            System.out.println("awake color cam");
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraOpenNI2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void grabDepth() {
        try {
//            System.out.println("Sleeping color cam");
//            Thread.sleep((long) ((1.0f / (float) colorCamera.frameRate) * 1000f));
            Thread.sleep((long) ((1.0f / (float) colorCamera.frameRate) * 800f));
//          System.out.println("awake color cam");
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraOpenNI2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // WARNING, in thread  it does not wait for a new image ?
    @Override
    public void grabColor() {
        try {
         //   System.out.println("Sleeping color cam");
            Thread.sleep((long) ((1.0f / (float) colorCamera.frameRate) * 1000f));
//            Thread.sleep((long) ((1.0f / (float) colorCamera.frameRate) * 5000f));
//            System.out.println("awake color cam");
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraOpenNI2.class.getName()).log(Level.SEVERE, null, ex);
        }

// Try to wait for a new timestamp ?
//        this.getTimeStamp();
    }

    // Similar to grabDepth, as grabDepth is disable here
    private void updateDepth() {
        if (getActingCamera() == IRCamera) {
            ((WithTouchInput) depthCamera).newTouchImageWithColor(IRCamera.currentImage);
            return;
        }
        if (getActingCamera() == colorCamera || useColor && colorCamera.currentImage != null) {
            ((WithTouchInput) depthCamera).newTouchImageWithColor(colorCamera.currentImage);
            return;
        }
        ((WithTouchInput) depthCamera).newTouchImage();
    }

    // Similar to grabDepth, as grabDepth is disable here
    private void updateColor() {
        if (thread != null) {
            if (!colorCamera.getTrackedSheets().isEmpty()) {
//                System.out.println("Tracking... " + colorCamera.getTrackedSheets().get(0));
                thread.setImage(colorCamera.currentImage);
                thread.compute();
            }
        }
    }

    @Override
    public void setUseDepth(boolean use) {
        if (use) {
            depthCamera.setPixelFormat(PixelFormat.OPENNI_2_DEPTH);
            depthCamera.type = SubCamera.Type.DEPTH;
            depthCamera.setSize(640, 480);
            depthCamera.setFrameRate(30);
        }
        this.useDepth = use;
    }

    @Override
    public void setUseIR(boolean use) {
        if (use) {
            IRCamera.setPixelFormat(PixelFormat.GRAY);
            IRCamera.type = SubCamera.Type.IR;
            IRCamera.setSize(640, 480);
            IRCamera.setFrameRate(30);
        }
        this.useIR = use;
    }

    @Override
    public void setUseColor(boolean use) {
        if (use) {
            colorCamera.setPixelFormat(PixelFormat.BGR);
            colorCamera.type = SubCamera.Type.COLOR;
            colorCamera.setSize(640, 480);
            colorCamera.setFrameRate(30);
        }
        this.useColor = use;
    }

    @Override
    protected void internalGrab() throws Exception {
    }

    private void initDevice() {

//        ArrayList<SensorType> mDeviceSensors;
        // initialize OpenNI
        OpenNI.initialize();

        String uri;

        java.util.List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
        if (devicesInfo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        uri = devicesInfo.get(this.systemNumber).getUri();
        device = Device.open(uri);
    }

    int mMaxGray16Value;

    private void calcMaxGray16Value(ByteBuffer gray16Buffer) {
        while (gray16Buffer.remaining() > 0) {
            int pixel = (int) gray16Buffer.getShort() & 0xFFFF;
            if (pixel > mMaxGray16Value) {
                mMaxGray16Value = pixel;
            }
        }
        gray16Buffer.rewind();

    }

    class FrameListener implements VideoStream.NewFrameListener {

        VideoFrameRef lastFrame;
        Camera camera;

        public FrameListener(Camera camera) {
            this.camera = camera;
        }

        private Pointer rawDepthImageData = new Pointer((Pointer) null),
                rawVideoImageData = new Pointer((Pointer) null),
                rawIRImageData = new Pointer((Pointer) null);
        private opencv_core.IplImage rawVideoImage = null;
        private opencv_core.IplImage rawVideoImageGray = null;

        @Override
        public synchronized void onFrameReady(VideoStream stream) {
            Instant initUpdate = Instant.now();

//            if (this.camera == colorCamera) {
//                System.out.println("Getting a color frame.");
//            } else {
//                System.out.println("Getting another frame.");
//            }
            if (lastFrame != null) {
                lastFrame.release();
                lastFrame = null;
            }

            lastFrame = stream.readFrame();
            ByteBuffer frameData = lastFrame.getData().order(ByteOrder.LITTLE_ENDIAN);

            // todo this should not change. 
            // make sure we have enough room
//            if (mImagePixels == null || mImagePixels.length < lastFrame.getWidth() * lastFrame.getHeight()) {
//                mImagePixels = new int[lastFrame.getWidth() * lastFrame.getHeight()];
//            }
            int deviceWidth = camera.width();
            int deviceHeight = camera.height();

            // To Java/Processing -  Int format
            // Not used yet
            if (camera.getPixelFormat() == PixelFormat.RGB
                    || camera.getPixelFormat() == PixelFormat.BGR
                    || camera.getPixelFormat() == PixelFormat.GRAY) {
                int iplDepth = IPL_DEPTH_8U;
                int channels = 3;
                int frameSize = deviceWidth * deviceHeight * channels;

                byte[] frameDataBytes = new byte[frameSize];
                frameData.get(frameDataBytes);
                if (rawVideoImage == null || rawVideoImage.width() != deviceWidth || rawVideoImage.height() != deviceHeight) {
                    rawVideoImage = opencv_core.IplImage.create(deviceWidth, deviceHeight, iplDepth, channels);
                    rawVideoImageGray = opencv_core.IplImage.create(deviceWidth, deviceHeight, iplDepth, 1);
                }
                rawVideoImage.getByteBuffer().put(frameDataBytes, 0, frameSize);
//                opencv_imgproc.cvCvtColor(rawVideoImage, rawVideoImage, COLOR_RGB2BGR);
//                opencv_imgproc.cvCvtColor(rawVideoImage, rawVideoImageGray, COLOR_BGR2GRAY);

                camera.updateCurrentImage(rawVideoImage);
                updateColor();
//                camera.updateCurrentImage(rawVideoImageGray);
            }

            if (camera.getPixelFormat() == PixelFormat.OPENNI_2_DEPTH) {

                int iplDepth = IPL_DEPTH_8U;
                int channels = 2;

                int frameSize = deviceWidth * deviceHeight * channels;
                // TODO: Handle as a sort buffer instead of byte.
                byte[] frameDataBytes = new byte[frameSize];
                frameData.get(frameDataBytes);
                if (rawVideoImage == null || rawVideoImage.width() != deviceWidth || rawVideoImage.height() != deviceHeight) {
                    rawVideoImage = opencv_core.IplImage.create(deviceWidth, deviceHeight, iplDepth, channels);
                }
                rawVideoImage.getByteBuffer().put(frameDataBytes, 0, frameSize);
                camera.updateCurrentImage(rawVideoImage);
                updateDepth();

            }
            Instant endUpdate = Instant.now();
//            System.out.println("CAM TREATMENT: " + Duration.between(initUpdate, endUpdate).toMillis() + " milliseconds");

        }

    }

    @Override
    public void setAutoWhiteBalance(boolean v) {
        this.colorStream.getCameraSettings().setAutoWhiteBalanceEnabled(v);
    }

    @Override
    public void setAutoExposure(boolean v) {
        this.colorStream.getCameraSettings().setAutoExposureEnabled(v);
    }

    @Override
    public boolean isAutoExposure() {
        return this.colorStream.getCameraSettings().getAutoExposureEnabled();
    }

    @Override
    public boolean isAutoWhiteBalance() {
        return this.colorStream.getCameraSettings().getAutoWhiteBalanceEnabled();
    }

    public void test() {
        this.colorStream.getCameraSettings().setAutoExposureEnabled(useIR);
        this.colorStream.getCameraSettings().setAutoWhiteBalanceEnabled(useIR);
    }

//    public CameraSettings getCameraSettings(){
//        
//    }
}
