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

import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import processing.core.PImage;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraNectar extends CameraRGBIRDepth {

    private boolean getMode = false;

    String DEFAULT_HOST = "localhost";
    int DEFAULT_PORT = 6379;

    Jedis redisGet;

    protected CameraNectar(String cameraName) {
        this.cameraDescription = cameraName;
//        redis = new Jedis("localhost", 6379);
    }

//    public CameraNectar(String cameraName, String host, int port) {
//        this.cameraDescription = cameraName;
//        this.actAsColorCamera();
////        redis = new Jedis(host, port);
//    }
    @Override
    public void start() {
        try {

            if (useColor) {
                startRGB();
            }
            if (useDepth) {
                startDepth();
            }
            if (getMode) {
                redisGet = new Jedis(DEFAULT_HOST, DEFAULT_PORT);
            }
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start Nectar camera: " + cameraDescription + ". " + e);
            System.err.println("Check cable connection, ID and resolution asked.");
            e.printStackTrace();
        }
    }

    private void startRGB() {
        Jedis redis = new Jedis(DEFAULT_HOST, DEFAULT_PORT);

        int w = Integer.parseInt(redis.get(cameraDescription + ":width"));
        int h = Integer.parseInt(redis.get(cameraDescription + ":height"));
        colorCamera.setSize(w, h);
        colorCamera.setPixelFormat(PixelFormat.RGB);
        colorCamera.setFrameRate(30);

        System.out.println("Starting thread Nectar camera.");
        if (!getMode) {
            new RedisThread(redis, new ImageListener(colorCamera.getPixelFormat()), cameraDescription).start();
        }
    }

    private void startDepth() {

        System.out.println("Start depth thread ?");
        Jedis redis2 = new Jedis(DEFAULT_HOST, DEFAULT_PORT);

        String v = redis2.get(cameraDescription + ":depth:width");
        if (v != null) {

//        if (redis.exists(cameraDescription + ":depth:width")) {
            int w = Integer.parseInt(redis2.get(cameraDescription + ":depth:width"));
            int h = Integer.parseInt(redis2.get(cameraDescription + ":depth:height"));
            depthCamera.setSize(w, h);
            depthCamera.setFrameRate(30);

            // TODO: Standard Depth format
            depthCamera.setPixelFormat(PixelFormat.OPENNI_2_DEPTH);
            if (!getMode) {

                System.out.println("Start depth thread !");
                new RedisThread(redis2, new ImageListener(depthCamera.getPixelFormat()), cameraDescription + ":depth:raw").start();
            }
        }
    }

    /**
     * Switch to get Mode. It will read the key using the redis get command,
     * instead of pub/sub.
     *
     * @param get
     */
    public void setGetMode(boolean get) {
        this.getMode = get;
    }

    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            if (getMode) {
                setColorImage(redisGet.get(cameraDescription.getBytes()));
                setDepthImage(redisGet.get((cameraDescription + ":depth:raw").getBytes()));
                // sleep only
                Thread.sleep(120);
            } else {
                //..nothing the princess is in another thread.
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            System.err.println("Camera: OpenCV Grab() Error ! " + e);
        }
    }

//    @Override
//    public PImage getPImage() {
//        return colorCamera.getPImage();
//        if (currentImage != null) {
//            this.checkCamImage();
//            camImage.update(currentImage);
//            return camImage;
//        }
//        return null;
//    }
    private opencv_core.IplImage rawVideoImage = null;
    private opencv_core.IplImage rawDepthImage = null;

    protected void setColorImage(byte[] message) {
        int channels = 3;
        if (rawVideoImage == null || rawVideoImage.width() != colorCamera.width || rawVideoImage.height() != colorCamera.height) {
            rawVideoImage = opencv_core.IplImage.create(colorCamera.width, colorCamera.height, IPL_DEPTH_8U, 3);
        }
        int frameSize = colorCamera.width * colorCamera.height * channels;
        rawVideoImage.getByteBuffer().put(message, 0, frameSize);
        colorCamera.updateCurrentImage(rawVideoImage);
//        colorCamera.updateCurrentImage(rawVideoImage);
    }

    protected void setDepthImage(byte[] message) {
        int iplDepth = IPL_DEPTH_8U;
        int channels = 2;

        System.out.println("Set depth image");

        int frameSize = depthCamera.width * depthCamera.height * channels;
        // TODO: Handle as a sort buffer instead of byte.
        if (rawDepthImage == null || rawDepthImage.width() != depthCamera.width || rawDepthImage.height() != depthCamera.height) {
            rawDepthImage = opencv_core.IplImage.create(depthCamera.width, depthCamera.height, iplDepth, channels);
        }
        rawDepthImage.getByteBuffer().put(message, 0, frameSize);
        depthCamera.updateCurrentImage(rawDepthImage);
    }

    @Override
    public void close() {
        this.setClosing();
    }

    @Override
    protected void grabIR() {
    }

    @Override
    protected void grabDepth() {
    }

    @Override
    protected void grabColor() {
    }

    @Override
    protected void internalStart() throws Exception {
    }

    @Override
    protected void internalGrab() throws Exception {
    }

    class RedisThread extends Thread {

        BinaryJedisPubSub listener;
        private final String key;
        Jedis client;

        public RedisThread(Jedis client, BinaryJedisPubSub listener, String key) {
            this.listener = listener;
            this.key = key;
            this.client = client;
        }

        public void run() {
            byte[] id = key.getBytes();
            client.subscribe(listener, id);
        }
    }

    class ImageListener extends BinaryJedisPubSub {

        PixelFormat format;

        public ImageListener(PixelFormat format) {
            this.format = format;
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            if (this.format == PixelFormat.BGR || this.format == PixelFormat.RGB) {
                setColorImage(message);
            }
            if (this.format == PixelFormat.OPENNI_2_DEPTH) {
                setDepthImage(message);
                System.out.println("received depth message image");

            }
        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {
        }

        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
        }

        @Override
        public void onPSubscribe(byte[] pattern, int subscribedChannels) {
        }

        @Override
        public void onPUnsubscribe(byte[] pattern, int subscribedChannels) {
        }

        @Override
        public void onPMessage(byte[] pattern, byte[] channel, byte[] message) {
        }
    }

}
