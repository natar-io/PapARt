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

import fr.inria.papart.tracking.DetectedMarker;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole
 */
public class CameraNectar extends CameraRGBIRDepth {

    private boolean getMode = false;

    public String DEFAULT_REDIS_HOST = "localhost";
    public int DEFAULT_REDIS_PORT = 6379;
    private DetectedMarker[] currentMarkers;
    Jedis redisGet;

    protected CameraNectar(String cameraName) {
        this.cameraDescription = cameraName;
    }

    @Override
    public void start() {
        try {

            if (useColor) {
                startRGB();
                startMarkerTracking();
            }
            if (useDepth) {
                System.out.println("NECTAR get depth.");
                startDepth();
            }
            if (getMode) {
                redisGet = new Jedis(DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT);
            }
            this.isConnected = true;
        } catch (NumberFormatException e) {
            System.err.println("Could not start Nectar camera: " + cameraDescription + ". " + e);
            System.err.println("Maybe the input key is not correct.");
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("Could not start Nectar camera: " + cameraDescription + ". " + e);
            System.err.println("Check cable connection, ID and resolution asked.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void startRGB() {
        Jedis redis = createConnection();

        int w = Integer.parseInt(redis.get(cameraDescription + ":width"));
        int h = Integer.parseInt(redis.get(cameraDescription + ":height"));
        String format = redis.get(cameraDescription + ":pixelformat");
        colorCamera.setSize(w, h);

        if (format != null) {
            colorCamera.setPixelFormat(PixelFormat.valueOf(format));
        } else {
            colorCamera.setPixelFormat(PixelFormat.RGB);
        }
        colorCamera.setFrameRate(30);
        colorCamera.isConnected = true;
        if (!getMode) {
            new RedisThread(redis, new ImageListener(colorCamera.getPixelFormat()), cameraDescription).start();
        }
    }
    
    public Jedis createConnection(){
        return new Jedis(DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT);
    }

    private void startDepth() {
        Jedis redis2 = new Jedis(DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT);

        String v = redis2.get(cameraDescription + ":depth:width");
        if (v != null) {

//        if (redis.exists(cameraDescription + ":depth:width")) {
            int w = Integer.parseInt(redis2.get(cameraDescription + ":depth:width"));
            int h = Integer.parseInt(redis2.get(cameraDescription + ":depth:height"));
            depthCamera.setSize(w, h);
            depthCamera.setFrameRate(30);
            depthCamera.isConnected = true;
            // TODO: Standard Depth format
            depthCamera.setPixelFormat(PixelFormat.OPENNI_2_DEPTH);
            if (!getMode) {
                new RedisThread(redis2, new ImageListener(depthCamera.getPixelFormat()), cameraDescription + ":depth:raw").start();
            }
        }
    }

    public void startMarkerTracking() {
        Jedis redis = new Jedis(DEFAULT_REDIS_HOST, DEFAULT_REDIS_PORT);
        if (!getMode) {
            new RedisThread(redis, new MarkerListener(), cameraDescription + ":markers").start();
        }
    }

    private void setMarkers(byte[] message) {
        currentMarkers = parseMarkerList(new String(message));
//        lastMarkers = currentMarkers;
        super.setMarkers(currentMarkers);
//        System.out.println("Markers found: " + currentMarkers.length);
    }

    public DetectedMarker[] getMarkers() {
        if (currentMarkers == null) {
            return new DetectedMarker[0];
        }
        return currentMarkers;
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

                if (useColor) {
                    setMarkers(redisGet.get((cameraDescription + ":markers").getBytes()));
                    setColorImage(redisGet.get(cameraDescription.getBytes()));
                }
                if (useDepth) {
                    setDepthImage(redisGet.get((cameraDescription + ":depth:raw").getBytes()));
                }
                // sleep only
                Thread.sleep(15);
            } else {
                //..nothing the princess is in another thread.
                Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            System.err.println("CameraNectar grab Error ! " + e);
        } catch (Exception e) {
            System.err.println("Camera Nectar error:  " + e);
            e.printStackTrace();
        }
    }

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

        this.setChanged();
        this.notifyObservers("image");
    }

    protected void setDepthImage(byte[] message) {
        int iplDepth = IPL_DEPTH_8U;
        int channels = 2;

        int frameSize = depthCamera.width * depthCamera.height * channels;
        // TODO: Handle as a sort buffer instead of byte.
        if (rawDepthImage == null || rawDepthImage.width() != depthCamera.width || rawDepthImage.height() != depthCamera.height) {
            rawDepthImage = opencv_core.IplImage.create(depthCamera.width, depthCamera.height, iplDepth, channels);
        }
        rawDepthImage.getByteBuffer().put(message, 0, frameSize);
        depthCamera.updateCurrentImage(rawDepthImage);

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
            while (!isClosing()) {
                try {
                    byte[] id = key.getBytes();
                    client.subscribe(listener, id);
                } catch (Exception e) {
                    System.out.println("Redis connection error: " + e);
                    System.out.println("Retrying to connect...");
                    client.close();
                    client = createConnection();
                }
            }
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
//                System.out.println("received depth message image");

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

    class MarkerListener extends BinaryJedisPubSub {

        public MarkerListener() {
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            setMarkers(message);
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

    public static DetectedMarker[] parseMarkerList(String jsonMessage) {

        DetectedMarker detectedMarkers[] = new DetectedMarker[0];
//        Marker m = new Marker(0, corners);
        JSONObject msg = null;
        try {
            msg = JSONObject.parse(jsonMessage);
        } catch (Exception e) {
            System.err.println("Exception while parsing json." + e.toString() + " \nMessage: " + jsonMessage);
        }
        if (msg == null) {
            return detectedMarkers;
        }
//        System.out.println("json: " + msg.getJSONArray("markers").size());

        JSONArray markers = msg.getJSONArray("markers");

        if (markers != null && markers.size() > 0) {
            detectedMarkers = new DetectedMarker[markers.size()];
            for (int i = 0; i < markers.size(); i++) {
                JSONObject m = markers.getJSONObject(i);

                int id = m.getInt("id");
                JSONArray corners = m.getJSONArray("corners");

                assert (corners.size() == 8);
//                System.out.println("Corners size: " + corners.size());
                DetectedMarker dm = new DetectedMarker(id,
                        corners.getFloat(0),
                        corners.getFloat(1),
                        corners.getFloat(2),
                        corners.getFloat(3),
                        corners.getFloat(4),
                        corners.getFloat(5),
                        corners.getFloat(6),
                        corners.getFloat(7));

                detectedMarkers[i] = dm;
            }
        }
        return detectedMarkers;
    }

}
