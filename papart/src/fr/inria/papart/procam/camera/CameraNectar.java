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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import static processing.core.PApplet.println;
import processing.core.PImage;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraNectar extends Camera {

    private OpenCVFrameGrabber grabber;
    private final OpenCVFrameConverter.ToIplImage converter;

    public static final String OUTPUT_PREFIX = "nectar:";
    public static final String OUTPUT_PREFIX2 = ":camera-server:camera";
    public static final String REDIS_PORT = "6379";

    static String defaultHost = "jiii-mi";
    static String defaultName = OUTPUT_PREFIX + defaultHost + OUTPUT_PREFIX2 + "#0";

    private Jedis redis;

    private IplImage rawVideoImage;
    private Semaphore videoSem = new Semaphore(1);
    private boolean hasNewVideoImage = false;
    protected int deviceWidth = 640, deviceHeight = 480;

    protected CameraNectar(String description) {

        // TODO: do something with this.
        this.cameraDescription = description;

        // TODO: Find the pixformat
        this.setPixelFormat(PixelFormat.BGR);
        converter = new OpenCVFrameConverter.ToIplImage();
    }

    private void connectRedist() throws Exception {
        redis = new Jedis("127.0.0.1", 6379);
        // redis.auth("156;2Asatu:AUI?S2T51235AUEAIU");
        if (redis == null) {
            throw new Exception("Cannot connect to camera server.");
        }
    }

    private byte[] nameAsBytes;
    private ImageListener imageListener;
    RedisThread redisThread;

    @Override
    public void start() {

        try {
            this.isConnected = true;
            connectRedist();
//            nameAsBytes = defaultName.getBytes();
//            imageListener = new ImageListener();

            setSize(deviceWidth, deviceHeight);
            setFrameRate(30);
            // TODO: Find the pixformat, width and height
            this.setPixelFormat(PixelFormat.BGR);
            redisThread = new RedisThread();
            redisThread.start();

        } catch (Exception e) {
            System.err.println("Could not start frameGrabber... " + e);

            System.err.println("Could not camera start frameGrabber... " + e);
            System.err.println("Camera ID " + this.systemNumber + " could not start.");
            System.err.println("Check cable connection, ID and resolution asked.");

            this.grabber = null;
        }
    }

    class RedisThread extends Thread {

        public void run() {
            System.out.println("Redis subscribe");
            nameAsBytes = defaultName.getBytes();
            imageListener = new ImageListener();
            redis.subscribe(imageListener, nameAsBytes);
        }
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            // SLEEP
//            if (hasNewVideoImage) {
//                System.out.println("grabbing a new image");
//                videoSem.acquire();
//                this.updateCurrentImage(rawVideoImage);
//                hasNewVideoImage = false;
//                videoSem.release();
//            }
        } catch (Exception e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
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

    protected void readImage(byte[] message) {

        try {
            videoSem.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(CameraNectar.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(message);
            ObjectInput in = new ObjectInputStream(bis);
            Object o = in.readObject();

            byte[] px = (byte[]) o;
            byte[] copy = (byte[]) px.clone();
//            byteToInt(px, true, pixels);
//            ByteBuffer imageBuffer = ByteBuffer.wrap(px);

            int iplDepth = IPL_DEPTH_8U;
            int channels = 3;
            int frameSize = deviceWidth * deviceHeight * channels;

            System.out.println("pixel received: " + copy.length);

//                frameData.get(frameDataBytes);
            if (rawVideoImage == null || rawVideoImage.width() != deviceWidth || rawVideoImage.height() != deviceHeight) {
                System.out.println("Create raw image: " + deviceWidth + " " + deviceHeight);

                rawVideoImage = opencv_core.IplImage.create(deviceWidth, deviceHeight, iplDepth, channels);
//                    rawVideoImageGray = opencv_core.IplImage.create(deviceWidth, deviceHeight, iplDepth, 1);
            }

            rawVideoImage.getByteBuffer().put(copy, 0, frameSize);
            hasNewVideoImage = true;
            this.updateCurrentImage(rawVideoImage);
            System.out.println("Video image saved to ipl");
            in.close();

//                opencv_imgproc.cvCvtColor(rawVideoImage, rawVideoImage, COLOR_RGB2BGR);
//                opencv_imgproc.cvCvtColor(rawVideoImage, rawVideoImageGray, COLOR_BGR2GRAY);
        } catch (IOException ex) {
            println("unpack issue " + ex);
        } catch (Exception ex) {
            println("unpack issue2 " + ex);
            ex.printStackTrace();
        }
        videoSem.release();
    }

    class ImageListener extends BinaryJedisPubSub {

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            try {
                System.out.println("Message received " + message);
                readImage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {
            System.out.println("onSubscribe");
        }

        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
            System.out.println("onUnSubscribe");
        }

        @Override
        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
            System.out.println("onPSubscribe");
        }

        @Override
        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
            System.out.println("onPUnSubscribe");
        }

        @Override
        public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
            System.out.println("onPMessage");
        }
    }

    @Override
    public void close() {
        this.setClosing();
        if (grabber != null) {
            try {
                grabber.stop();
                System.out.println("Stopping grabber (OpencV)");

            } catch (Exception e) {
                System.out.println("Impossible to close " + e);
            }
        }
    }

}
