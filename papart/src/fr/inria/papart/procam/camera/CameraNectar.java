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

import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.procam.RedisClient;
import fr.inria.papart.procam.RedisClientImpl;
import fr.inria.papart.tracking.DetectedMarker;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.IPL_DEPTH_8U;

import processing.core.PMatrix3D;
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
    private String redisHost = DEFAULT_REDIS_HOST;
    private int redisPort = DEFAULT_REDIS_PORT;
    private DetectedMarker[] currentMarkers;

    protected RedisClient redisClientGenerator = new RedisClientImpl();
    Jedis redisGet;

    protected final RedisClientImpl RedisClientGenerator = new RedisClientImpl();

    public CameraNectar(String cameraName) {
        this.cameraDescription = cameraName;
    }
    
    public String getCameraKey(){
        return this.cameraDescription;
    }

    public RedisClient getRedisClient() {
        return this.redisClientGenerator;
    }

    public void setRedisClient(RedisClient client) {
        this.redisClientGenerator = client;
    }


    @Override
    public void start() {
        try {
            redisGet = createConnection();
            if (useColor) {
                startRGB();
                startMarkerTracking();
            }
            if (useDepth) {
                System.out.println("NECTAR get depth.");
                startDepth();
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

    private void startDepth() {
        Jedis redis2 = createConnection();

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
        Jedis redis = createConnection();
        int id = (int) (Math.abs(Math.random()));

        // New: set a name, with an ID.
        redis.clientSetname("CameraClient:" + id);

        // Link the ID to a description
        redis.set("clients:CameraClient:" + id, cameraDescription);

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
     * Fetch the calibrations(intrinsics) from Redis / Nectar.
     */
    public void loadCalibrations() {
      redisGet = createConnection();
      try {
          JSONObject calib = JSONObject.parse(redisGet.get(cameraDescription + ":calibration"));
          colorCamera.setCalibration(calib);
      } catch (Exception e) {
          System.out.println("cannot load camera (" + cameraDescription + ") calibration: " + e);
          e.printStackTrace();
      }
      try {
          JSONObject calib2 = JSONObject.parse(redisGet.get(cameraDescription + ":depth:calibration"));
          depthCamera.setCalibration(calib2);
      } catch (Exception e) {
          System.out.println("cannot load depth camera  (" + cameraDescription + ") calibrations: " + e);
          e.printStackTrace();
      }
  }

    /**
     * Fetch the extrinsics (color-depth) from Redis / Nectar.
     */
    public void loadStereoExtrinsics() {
        Jedis connection = redisClientGenerator.createConnection();
        String data = connection.get(cameraDescription + ":extrinsics:depth");
        PMatrix3D extr = HomographyCalibration.CreateMatrixFrom(data);
        depthCamera.setExtrinsics(extr);
    }

    /**
     * Fetch the extrinsics (color-depth) from Redis / Nectar.
     *
     * @param key
     */
    public void loadExtrinsics(String key) {
        Jedis connection = redisClientGenerator.createConnection();
        String data = connection.get(cameraDescription + ":extrinsics:" + key);
        PMatrix3D extr = HomographyCalibration.CreateMatrixFrom(data);
        this.setExtrinsics(extr);
    }

    /**
     * Fetch the extrinsics (color-depth) from Redis / Nectar.
     *
     * @param key
     */
    public void saveExtrinsics(String key) {
        PMatrix3D extr = this.getExtrinsics();
        HomographyCalibration hc = new HomographyCalibration();
        hc.setMatrix(extr);
        hc.saveToXML(redisClientGenerator, this.getCameraDescription() + ":extrinsics:" + key);
    }

    public void setTableLocation(PMatrix3D tableCenter) {
        HomographyCalibration hc = new HomographyCalibration();
        hc.setMatrix(tableCenter);
        hc.saveToXML(redisClientGenerator, this.getCameraDescription() + ":table");
    }

    public PMatrix3D loadTableLocation() {
        Jedis connection = redisClientGenerator.createConnection();
        String data = connection.get(cameraDescription + ":table");
        return HomographyCalibration.CreateMatrixFrom(data);
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
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            System.err.println("CameraNectar grab Error ! " + e);
        } catch (Exception e) {
            System.err.println("Camera Nectar error:  " + e);
            e.printStackTrace();
        }
    }

    private IplImage rawVideoImage = null;
    private IplImage rawDepthImage = null;

    protected void setColorImage(byte[] message) {
        int channels = 3;
        if (rawVideoImage == null || rawVideoImage.width() != colorCamera.width || rawVideoImage.height() != colorCamera.height) {
            rawVideoImage = IplImage.create(colorCamera.width, colorCamera.height, IPL_DEPTH_8U, 3);
        }
        int frameSize = colorCamera.width * colorCamera.height * channels;

        // TODO: Check if there is a copy here
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
            rawDepthImage = IplImage.create(depthCamera.width, depthCamera.height, iplDepth, channels);
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

    private Jedis checkConnection(Jedis connection) {
        if (connection == null || !connection.isConnected()) {
            System.out.println("Camera: " + this.cameraDescription + ". Trying to reconnect.");
            connection = createConnection();
        }
        return connection;
    }

    class ImageListener extends BinaryJedisPubSub {

        PixelFormat format;
        Jedis getConnection;

        public ImageListener(PixelFormat format) {
            this.format = format;
            getConnection = createConnection();
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            try {
                getConnection = checkConnection(getConnection);
                if (this.format == PixelFormat.BGR || this.format == PixelFormat.RGB) {
                    byte[] data = getConnection.get(channel);
                    setColorImage(data);
                }
                if (this.format == PixelFormat.OPENNI_2_DEPTH) {
                    byte[] data = getConnection.get(channel);
                    setDepthImage(data);
//                System.out.println("received depth message image");

                }
            } catch (Exception e) {
                System.out.println("Exception reading data: ");
                e.printStackTrace();
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

    public Jedis createConnection() {
        return RedisClientGenerator.createConnection();
    }

    public String getRedisHost() {
        return RedisClientGenerator.getRedisHost();
    }

    public void setRedisHost(String redisHost) {
        RedisClientGenerator.setRedisHost(redisHost);
    }

    public void setRedisAuth(String redisAuth) {
        RedisClientGenerator.setRedisAuth(redisAuth);
    }

    public int getRedisPort() {
        return RedisClientGenerator.getRedisPort();
    }

    public void setRedisPort(int redisPort) {
        RedisClientGenerator.setRedisPort(redisPort);
    }

}
