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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenKinectFrameGrabber;

import java.awt.*;
import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.RealSense;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvSetData;

import org.openni.*;
import static org.openni.PixelFormat.RGB888;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraOpenNI2 extends CameraRGBIRDepth implements VideoStream.NewFrameListener {

    private VideoStream colorStream, irStream, depthStream;

    protected CameraOpenNI2(int cameraNo) {
        this.systemNumber = cameraNo;
//        grabber = new OpenKinectFrameGrabber(this.systemNumber);
        initDevice();
    }

    @Override
    public void internalStart() throws FrameGrabber.Exception {
//        grabber.start();

        setUseColor(true);
        colorCamera.setPixelFormat(PixelFormat.RGB);
        actAsColorCamera();
        colorCamera.setSize(640, 480);
        colorCamera.setFrameRate(30);

//        colorStream = VideoStream.create(device, SensorType.DEPTH);
        irStream = VideoStream.create(device, SensorType.IR);
        
        colorStream = VideoStream.create(device, SensorType.COLOR);
        org.openni.PixelFormat fmt = org.openni.PixelFormat.RGB888;
        // Video mode, w, h, fps, pixel fmt
        VideoMode vm = new VideoMode(640, 480, 30, fmt.toNative());
        colorStream.setVideoMode(vm);

        colorStream.start();
        setStream(colorStream);

    }

    @Override
    public void setSize(int w, int h) {
//        Camera act = getActingCamera();
//        if (act == null) {
//            // it is likely that the set size was for the color camera then.
//            if (useColor) {
//                colorCamera.setSize(w, h);
//            }
//            return;
//        } else {
//            act.setSize(w, h);
//        }
    }

    @Override
    public void close() {
        setClosing();
//        if (grabber != null) {
//            try {
//                System.out.println("Stopping KinectGrabber");
//                this.stopThread();
//                grabber.stop();
//                depthCamera.close();
//            } catch (Exception e) {
//            }
//        }

    }

    @Override
    public void grabIR() {
//            IRCamera.updateCurrentImage(grabber.grabIR());
    }

    @Override
    public void grabDepth() {
    }

    @Override
    public void grabColor() {
//         opencv_core.IplImage video = grabber.grabVideo();
//         
//        if (video != null) {
//            colorCamera.updateCurrentImage(video);
//        }
        System.out.println("Grab color.");
    }

    private Pointer rawDepthImageData = new Pointer((Pointer) null),
            rawVideoImageData = new Pointer((Pointer) null),
            rawIRImageData = new Pointer((Pointer) null);
    private opencv_core.IplImage rawDepthImage = null, rawVideoImage = null, rawIRImage = null, returnImage = null;

    @Override
    public void setUseDepth(boolean use) {
        if (use) {
//            depthCamera.setPixelFormat(PixelFormat.DEPTH_KINECT_MM);
//            depthCamera.type = SubCamera.Type.DEPTH;
//            depthCamera.setSize(640, 480);
//            grabber.setDepthFormat(freenect.FREENECT_DEPTH_MM);
        }
        this.useDepth = use;
    }

    @Override
    public void setUseIR(boolean use) {
        if (use) {
//            IRCamera.setPixelFormat(PixelFormat.GRAY);
//            IRCamera.type = SubCamera.Type.IR;
//            IRCamera.setSize(640, 480);
//            // grabber.setvideoformat ?
        }
        this.useIR = use;
    }

    @Override
    public void setUseColor(boolean use) {
        if (use) {
//            colorCamera.setPixelFormat(PixelFormat.BGR);
//            colorCamera.type = SubCamera.Type.COLOR;
//            colorCamera.setSize(640, 480);
//
//            grabber.setImageWidth(colorCamera.width());
//            grabber.setImageHeight(colorCamera.height());
//            kinectVideoFormat = freenect.FREENECT_VIDEO_RGB;
//            grabber.setVideoFormat(kinectVideoFormat);
        }
        this.useColor = use;
    }

    @Override
    protected void internalGrab() throws Exception {
        System.out.println("Grab global.");

    }

    // From the OpenNI example.
    float mHistogram[];
    int[] mImagePixels;
    int mMaxGray16Value = 0;
    VideoStream mVideoStream;
    VideoFrameRef mLastFrame;
    BufferedImage mBufferedImage;
    Device device;

    private ArrayList<SensorType> mDeviceSensors;
    private ArrayList<VideoMode> mSupportedModes;

    private void initDevice() {

        // initialize OpenNI
        OpenNI.initialize();

        String uri;

        java.util.List<DeviceInfo> devicesInfo = OpenNI.enumerateDevices();
        if (devicesInfo.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No device is connected", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        uri = devicesInfo.get(0).getUri();
        System.out.println("OpenNI URI: " + uri.toString());

        device = Device.open(uri);
        mDeviceSensors = new ArrayList<SensorType>();

        if (device.getSensorInfo(SensorType.COLOR) != null) {
            mDeviceSensors.add(SensorType.COLOR);
            System.out.println("Sensor can do color.");
        }

        if (device.getSensorInfo(SensorType.DEPTH) != null) {
            mDeviceSensors.add(SensorType.DEPTH);
            System.out.println("Sensor can do depth.");
        }

        if (device.getSensorInfo(SensorType.IR) != null) {
            mDeviceSensors.add(SensorType.IR);
            System.out.println("Sensor can do ir.");
        }

//        if (mVideoStream != null) {
//            mVideoStream.stop();
//            setStream(null);
//            mVideoStream.destroy();
//            mVideoStream = null;
//        }
//        VideoFrameRef readFrame = mVideoStream.readFrame();
//        SensorType type = mDeviceSensors.get(0);
//        mVideoStream = VideoStream.create(device, type);
//        java.util.List<VideoMode> supportedModes = mVideoStream.getSensorInfo().getSupportedVideoModes();
//        mSupportedModes = new ArrayList<VideoMode>();
//        // now only keeo the ones that our application supports
//        for (VideoMode mode : supportedModes) {
//            switch (mode.getPixelFormat()) {
//                case DEPTH_1_MM:
//                case DEPTH_100_UM:
//                case SHIFT_9_2:
//                case SHIFT_9_3:
//                case RGB888:
//                case GRAY8:
//                case GRAY16:
//                    mSupportedModes.add(mode);
//                    break;
//            }
//        }
        // now only keep the ones that our application supports
    }

    void selectedVideoModeChanged() {
        mVideoStream.stop();
        int modeIndex = 0;
        VideoMode mode = mSupportedModes.get(modeIndex);
        mVideoStream.setVideoMode(mode);
//        mViewer.setStream(mVideoStream);
//        mViewer.setSize(mode.getResolutionX(), mode.getResolutionY());
//        mFrame.setSize(mViewer.getWidth() + 20, mViewer.getHeight() + 80);
        mVideoStream.start();
    }

    public void setStream(VideoStream videoStream) {
        if (mLastFrame != null) {
            mLastFrame.release();
            mLastFrame = null;
        }

        if (mVideoStream != null) {
            mVideoStream.removeNewFrameListener(this);
        }

        mVideoStream = videoStream;

        if (mVideoStream != null) {
            mVideoStream.addNewFrameListener(this);
        }
    }

    public synchronized void paint(Graphics g) {
        if (mLastFrame == null) {
            return;
        }

        int width = mLastFrame.getWidth();
        int height = mLastFrame.getHeight();

        // The data is here. 
        mLastFrame.getData();

        // make sure we have enough room
        if (mBufferedImage == null || mBufferedImage.getWidth() != width || mBufferedImage.getHeight() != height) {
            mBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        mBufferedImage.setRGB(0, 0, width, height, mImagePixels, 0, width);
    }

    // This gets a frame ?
    @Override
    public synchronized void onFrameReady(VideoStream stream) {
        System.out.println("Getting a frame.");
        if (mLastFrame != null) {
            mLastFrame.release();
            mLastFrame = null;
        }

        mLastFrame = mVideoStream.readFrame();
        ByteBuffer frameData = mLastFrame.getData().order(ByteOrder.LITTLE_ENDIAN);
        // make sure we have enough room
        if (mImagePixels == null || mImagePixels.length < mLastFrame.getWidth() * mLastFrame.getHeight()) {
            mImagePixels = new int[mLastFrame.getWidth() * mLastFrame.getHeight()];
        }

//  To OpenCV        
        int iplDepth = IPL_DEPTH_8U, channels = 3;
//        rawVideoImageData = new BytePointer(frameData);

        byte[] frameDataBytes = new byte[640 * 480 * 3];
        frameData.get(frameDataBytes);

        int deviceWidth = 640;
        int deviceHeight = 480;
//        int deviceWidth = device.get_stream_width(RealSense.color);
//        int deviceHeight = device.get_stream_height(RealSense.color);

        if (rawVideoImage == null || rawVideoImage.width() != deviceWidth || rawVideoImage.height() != deviceHeight) {
//            rawVideoImage = opencv_core.IplImage.createHeader(deviceWidth, deviceHeight, iplDepth, channels);
            rawVideoImage = opencv_core.IplImage.create(deviceWidth, deviceHeight, iplDepth, channels);
        }

        rawVideoImage.getByteBuffer().put(frameDataBytes, 0, 640 * 480 * 3);

//        rawVideoImage.imageData().put(frameDataBytes, 0, deviceWidth * channels * iplDepth / 8);
//        cvSetData(rawVideoImage, rawVideoImageData, deviceWidth * channels * iplDepth / 8);
//
//        // Update the color camera (HACK)
        colorCamera.updateCurrentImage(rawVideoImage);

//        if (iplDepth > 8 && !ByteOrder.nativeOrder().equals(byteOrder)) {
//            // ack, the camera's endianness doesn't correspond to our machine ...
//            // swap bytes of 16-bit images
//            ByteBuffer bb = rawVideoImage.getByteBuffer();
//            ShortBuffer in = bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
//            ShortBuffer out = bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
//            out.put(in);
//        }
//        if (channels == 3) {
//            cvCvtColor(rawVideoImage, rawVideoImage, CV_BGR2RGB);
//        }
        // To Java/Processing -  Int format
        // Not used yet
        switch (mLastFrame.getVideoMode().getPixelFormat()) {
            case DEPTH_1_MM:
            case DEPTH_100_UM:
            case SHIFT_9_2:
            case SHIFT_9_3:
                calcHist(frameData);
                int pos = 0;
                while (frameData.remaining() > 0) {
                    int depth = (int) frameData.getShort() & 0xFFFF;
                    short pixel = (short) mHistogram[depth];
                    mImagePixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8);
                    pos++;
                }
                break;
            case RGB888:
                pos = 0;
                while (frameData.remaining() > 0) {
                    int red = (int) frameData.get() & 0xFF;
                    int green = (int) frameData.get() & 0xFF;
                    int blue = (int) frameData.get() & 0xFF;
                    mImagePixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
                    pos++;
                }
                break;
            case GRAY8:
                pos = 0;
                while (frameData.remaining() > 0) {
                    int pixel = (int) frameData.get() & 0xFF;
                    mImagePixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
                    pos++;
                }
                break;
            case GRAY16:
                calcMaxGray16Value(frameData);
                pos = 0;
                while (frameData.remaining() > 0) {
                    int pixel = (int) frameData.getShort() & 0xFFFF;
                    pixel = (int) (pixel * 255.0 / mMaxGray16Value);
                    mImagePixels[pos] = 0xFF000000 | (pixel << 16) | (pixel << 8) | pixel;
                    pos++;
                }
                break;
            default:
                // don't know how to draw
                mLastFrame.release();
                mLastFrame = null;
        }

//        repaint();
    }

    private String pixelFormatToName(org.openni.PixelFormat format) {
        switch (format) {
            case DEPTH_1_MM:
                return "1 mm";
            case DEPTH_100_UM:
                return "100 um";
            case SHIFT_9_2:
                return "9.2";
            case SHIFT_9_3:
                return "9.3";
            case RGB888:
                return "RGB";
            case GRAY8:
                return "Gray8";
            case GRAY16:
                return "Gray16";
            default:
                return "UNKNOWN";
        }
    }

    private void calcHist(ByteBuffer depthBuffer) {
        // make sure we have enough room
        if (mHistogram == null || mHistogram.length < mVideoStream.getMaxPixelValue()) {
            mHistogram = new float[mVideoStream.getMaxPixelValue()];
        }

        // reset
        for (int i = 0; i < mHistogram.length; ++i) {
            mHistogram[i] = 0;
        }

        int points = 0;
        while (depthBuffer.remaining() > 0) {
            int depth = depthBuffer.getShort() & 0xFFFF;
            if (depth != 0) {
                mHistogram[depth]++;
                points++;
            }
        }

        for (int i = 1; i < mHistogram.length; i++) {
            mHistogram[i] += mHistogram[i - 1];
        }

        if (points > 0) {
            for (int i = 1; i < mHistogram.length; i++) {
                mHistogram[i] = (int) (256 * (1.0f - (mHistogram[i] / (float) points)));
            }
        }
        depthBuffer.rewind();
    }

    private void calcMaxGray16Value(ByteBuffer gray16Buffer) {
        while (gray16Buffer.remaining() > 0) {
            int pixel = (int) gray16Buffer.getShort() & 0xFFFF;
            if (pixel > mMaxGray16Value) {
                mMaxGray16Value = pixel;
            }
        }
        gray16Buffer.rewind();
    }

}
