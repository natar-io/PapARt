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

import fr.inria.papart.apps.MultiCalibrator;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.ImageUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import processing.core.PApplet;
import processing.core.PImage;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import fr.inria.papart.tracking.DetectedMarker;
import processing.data.JSONObject;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class ProjectorAsCamera extends Camera {

    private final TrackedView projectorView;
    private final Camera cameraTracking;
    private IplImage grayImage;
    private final ProjectorDisplay projector;
    private PImage pimage;
    private MarkerListener markerListener;

    public ProjectorAsCamera(ProjectorDisplay projector, Camera cameraTracking, TrackedView view) {
        this.projectorView = view;
        this.cameraTracking = cameraTracking;
        this.projector = projector;
        this.isConnected = true;
        this.isClosing = false;
    }

    //// TODO: Subclass current class
    private boolean useNectar = false;
    private String projName;
    private CameraNectar nectarCamera;
    private String nectarProjName;
    private Jedis redis;

    private ArrayList<DetectedMarker[]> detectedMarkers;

    public void useNectar(boolean use, CameraNectar cam, String projName) {
        this.useNectar = use;
        if (use) {
            initNectar(cam, projName);
        }
    }

    private void initNectar(CameraNectar cam, String projName) {
        this.nectarCamera = cam;
        this.nectarProjName = projName;
        this.format = PixelFormat.BGR;

        redis = cam.createConnection();
        sendParams(this);
        // TODO: check nbhchannels
        initMemory(width, height, 3, 1);
    }

    private void sendParams(Camera cam) {
        redis.set(nectarProjName + ":width", Integer.toString(cam.width()));
        redis.set(nectarProjName + ":height", Integer.toString(cam.height()));
        redis.set(nectarProjName + ":channels", Integer.toString(3));
        redis.set(nectarProjName + ":pixelformat", cam.getPixelFormat().toString());
    }

    byte[] imageData;

    private void initMemory(int width, int height, int nChannels, int bytePerChannel) {
        imageData = new byte[nChannels * width * height * bytePerChannel];
    }

    /**
     * Warning check nb channels.
     *
     */
    public void sendImageToNectar() {
        sendColorImage(currentImage);
    }

    boolean isStreamSet = true;
    boolean isStreamPublish = true;

    static int imgID = 0;

    private void sendColorImage(IplImage img) {
        ByteBuffer byteBuffer;
        byteBuffer = img.getByteBuffer();
        byteBuffer.get(imageData);
        String name = nectarProjName;
        byte[] id = name.getBytes();
        String time = Long.toString(parent.millis());
        System.out.println("Sending Projector image to Nectar.");
//        if (isStreamSet) {
//            redis.set(id, imageData);
//            redis.set((name + ":timestamp"), time);
//        }
//        if (isStreamPublish) {
        redis.set(id, imageData);
        redis.set((name + ":timestamp"), time);

        JSONObject imageInfo = new JSONObject();
        imageInfo.setLong("timestamp", System.currentTimeMillis());
        imageInfo.setLong("imageCount", imgID++);
        redis.publish(id, imageInfo.toString().getBytes());
//        }
    }

    MultiCalibrator calibrator;
    private boolean allMarkersFound = false;

    public void startMarkerTracking(int nbMarkers, MultiCalibrator calibrator) {
        Jedis redisSub = nectarCamera.createConnection();
        this.calibrator = calibrator;
        this.detectedMarkers = new ArrayList<>();
        allMarkersFound = false;
        System.out.println("Listening to :  " + nectarProjName + ":markers");
        markerListener = new MarkerListener(nbMarkers);
        new RedisThread(redisSub, markerListener, nectarProjName + ":markers").start();
    }

    private void setMarkers(byte[] message, int id) {
        System.out.println("Setting markers: " + id);
        this.detectedMarkers.add(id, CameraNectar.parseMarkerList(new String(message)));
//        System.out.println("Markers found: " + currentMarkers.length);
    }

    public DetectedMarker[] getMarkers(int id) {
        return this.detectedMarkers.get(id);
    }

    public boolean allMarkersFound() {
        return this.allMarkersFound;
    }
    
    public int getLastMarkerFound(){
        return markerListener.currentMarker();
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

        @Override
        public void run() {
            try {
                byte[] id = key.getBytes();
                System.out.println("Subscribed to: " + key);
                client.subscribe(listener, id);
            } catch (Exception e) {
                System.out.println("ProjectorAsCameraThread: Redis connection error: " + e);
            }
        }
    }

    class MarkerListener extends BinaryJedisPubSub {

        private int currentMarker = 0;
        private final int nbMarkersToRead;

        public MarkerListener(int nbMarkersToRead) {
            this.nbMarkersToRead = nbMarkersToRead;
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            setMarkers(message, currentMarker);
            currentMarker++;
            System.out.println("got message:  " + currentMarker);
            if (currentMarker == nbMarkersToRead) {
                allMarkersFound = true;
                this.unsubscribe();
            }
        }
        
        public int currentMarker(){
            return currentMarker;
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

    public void setImage(IplImage image) {
        this.currentImage = image;
    }

    @Override
    public void setCalibration(String fileName) {
        try {
            this.calibrationFile = fileName;
            pdp = ProjectiveDeviceP.loadProjectorDevice(parent, fileName);
            camIntrinsicsP3D = pdp.getIntrinsics();
            this.width = pdp.getWidth();
            this.height = pdp.getHeight();
            this.undistort = pdp.handleDistorsions();
        } catch (Exception e) {
            e.printStackTrace();

            System.err.println("Camera: error reading the calibration " + pdp
                    + "file" + fileName + " \n" + e);
        }

//        System.out.println("Calibration : ");
//        camIntrinsicsP3D.print();
    }

    static public void convertARProjParams(PApplet parent, String calibrationFile,
            String calibrationARtoolkit) {
        try {
            ARToolkitPlusUtils.convertProjParam(parent, calibrationFile, calibrationARtoolkit);
        } catch (Exception ex) {
            System.out.println("Error converting projector to ARToolkit "
                    + calibrationFile + " " + calibrationARtoolkit
                    + ex);
        }
    }

    @Override
    public void start() {
        return;
    }

    /**
     * Not implemented.
     *
     * @return
     */
    @Override
    public PImage getPImage() {

        if (currentImage != null) {
            if (pimage == null) {
                pimage = parent.createImage(currentImage.width(), currentImage.height(), RGB);
            }
            ImageUtils.IplImageToPImage(currentImage, true, pimage);
            return pimage;
        }
        return null;
    }

    /**
     * Grab an image from the camera associated. Not used.
     */
    @Override
    public void grab() {
        IplImage img = projectorView.getIplViewOf(cameraTracking);
        if (img != null) {
            currentImage = img;
            if (this.format == PixelFormat.GRAY) {
                currentImage = greyProjectorImage(img);
            }
        }
    }

    /**
     * Grab a fake (given image).
     *
     * @param source given image.
     */
    public void grab(IplImage source) {
        IplImage img = projectorView.getIplViewOf(cameraTracking, source);
        if (img != null) {
            currentImage = img;
            if (this.format == PixelFormat.GRAY) {
                currentImage = greyProjectorImage(img);
            }
        }
    }

    private opencv_core.IplImage greyProjectorImage(opencv_core.IplImage projImage) {

        if (projImage.nChannels() == 1) {
            grayImage = projImage;
            return grayImage;
        }

        if (grayImage == null) {
            grayImage = opencv_core.IplImage.create(projector.getWidth(),
                    projector.getHeight(),
                    IPL_DEPTH_8U, 1);
        }

        cvCvtColor(projImage, grayImage, CV_BGR2GRAY);
        // if(test){
        //     cvSaveImage( sketchPath() + "/data/projImage.jpg", grayImage);
        //     cvSaveImage( sketchPath() + "/data/camImage.jpg", camera.getIplImage());
        // }
        return grayImage;
    }

    /**
     * Not used.
     */
    @Override
    public void close() {
    }

}
