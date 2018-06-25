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
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import processing.core.PImage;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraNectar extends Camera {

    private Jedis redis;
    private boolean getMode = false;

    protected CameraNectar(String cameraName) {
        this.cameraDescription = cameraName;
        this.setPixelFormat(PixelFormat.BGR);
        redis = new Jedis("localhost", 6379);
    }

    @Override
    public void start() {

        try {
            redis = new Jedis("localhost", 6379);

            System.out.println("Starting thread Nectar camera.");
            if (!getMode) {
                new RedisThread().start();
            }
            this.isConnected = true;
        } catch (Exception e) {
            System.err.println("Could not start Necatar camera... " + e);
            System.err.println("Check cable connection, ID and resolution asked.");
            redis = null;
        }
    }

    /**
     * Switch to get Mode. It will read the key using the redis get command, 
     * instead of pub/sub.
     * @param get 
     */
    public void setGetMode(boolean get){
        this.getMode = get;
    }
    @Override
    public void grab() {

        if (this.isClosing()) {
            return;
        }
        try {
            if (getMode) {
                setImage(redis.get(cameraDescription.getBytes()));
                // sleep only
                Thread.sleep(20);
            } else {
                //..nothing the princess is in another thread.
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
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

    private opencv_core.IplImage rawVideoImage = null;

    protected void setImage(byte[] message) {
        int channels = 3;
        if (rawVideoImage == null || rawVideoImage.width() != this.width || rawVideoImage.height() != this.height) {
            rawVideoImage = opencv_core.IplImage.create(width, height, IPL_DEPTH_8U, 3);
        }
        int frameSize = width * height * channels;
        rawVideoImage.getByteBuffer().put(message, 0, frameSize);
        this.updateCurrentImage(rawVideoImage);
    }

    @Override
    public void close() {
        this.setClosing();
    }

    class RedisThread extends Thread {

        public void run() {
            byte[] id = cameraDescription.getBytes();
            MyListener l = new MyListener();
            redis.subscribe(l, id);
        }
    }

    class MyListener extends BinaryJedisPubSub {

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            setImage(message);
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
