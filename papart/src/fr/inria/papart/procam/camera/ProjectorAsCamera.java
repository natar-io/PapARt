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

import fr.inria.papart.calibration.MultiCalibrator;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.display.ProjectorDisplay;
import fr.inria.papart.utils.ImageUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import org.xml.sax.SAXException;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import tech.lity.rea.nectar.markers.DetectedMarker;
import processing.data.JSONObject;
import tech.lity.rea.javacvprocessing.ProjectiveDeviceP;
import tech.lity.rea.nectar.calibration.HomographyCalibration;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.CameraNectar;
import tech.lity.rea.nectar.camera.RedisClientImpl;

/**
 *
 * @author Jérémy Laviole - laviole@rea.lity.tech
 */
public class ProjectorAsCamera extends Camera {

    private final TrackedViewPapart projectorView;
    private final Camera cameraTracking;
    private IplImage grayImage;
    private final ProjectorDisplay projector;
    private PImage pimage;
//    private MarkerListener markerListener;

    public ProjectorAsCamera(ProjectorDisplay projector, Camera cameraTracking, TrackedViewPapart view) {
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
    private String cameraDescription;
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
        this.cameraDescription = projName;
        this.format = PixelFormat.BGR;

        redis = cam.createConnection();

        String key = this.cameraDescription + ":calibration";
        if (redis.exists(key)) {
            this.setCalibration(JSONObject.parse(redis.get(key)));
            System.out.println("Setting " + cameraDescription + " calibration... ");
        }

        // TODO: check nbhchannels
        sendParams(cameraDescription);
        
        initMemory(width, height, 3, 1);
    }

    private void sendParams(String cameraDescription) {
        redis.set(cameraDescription + ":width", Integer.toString(width));
        redis.set(cameraDescription + ":height", Integer.toString(height));
        redis.set(cameraDescription + ":channels", Integer.toString(3));
        redis.set(cameraDescription + ":pixelformat", "RGB");
    }

    byte[] imageData;

    private void initMemory(int width, int height, int nChannels, int bytePerChannel) {
        System.out.println("Init Projector as camera nectar memory: " + width + " " + height + " " + nChannels + " " + bytePerChannel);
        imageData = new byte[nChannels * width * height * bytePerChannel];
    }

    /**
     * Warning check nb channels.
     *
     * @param i
     */
    public void sendImageToNectar(int i) {
        sendColorImage(currentImage, i);
    }

    public void republish() {
        byte[] id = cameraDescription.getBytes();
        JSONObject imageInfo = new JSONObject();
        imageInfo.setLong("timestamp", System.currentTimeMillis());
        imageInfo.setLong("imageCount", imgID++);
        redis.publish(id, imageInfo.toString().getBytes());
    }

    boolean isStreamSet = true;
    boolean isStreamPublish = true;

    static int imgID = 0;

    private void sendColorImage(IplImage img, int i) {

        ByteBuffer byteBuffer;
        byteBuffer = img.getByteBuffer();
        byteBuffer.get(imageData);
        String name = cameraDescription;
        byte[] id = (name + ":" + i).getBytes();
        String time = Long.toString(parent.millis());
        redis.set(id, imageData);
        redis.set((name + ":timestamp"), time);

        JSONObject imageInfo = new JSONObject();
        imageInfo.setLong("timestamp", System.currentTimeMillis());
        imageInfo.setLong("imageCount", imgID++);
        redis.publish(id, imageInfo.toString().getBytes());
    }

    MultiCalibrator calibrator;
    private boolean allMarkersFound = false;
    private int nbMarkers;
    
    public void startMarkerTracking(int nbMarkers, MultiCalibrator calibrator) {
        Jedis redisSub = nectarCamera.createConnection();
        this.nbMarkers = nbMarkers;
        this.calibrator = calibrator;
        this.detectedMarkers = new ArrayList<>();
        allMarkersFound = false;
        System.out.println("Listening to :  " + cameraDescription + ":markers");

        new RedisThread(redisSub, new MarkerListener(), cameraDescription + ":markers").start();
    }

    int currentMarker = 0;
    
    private void setMarkers(byte[] message) {
        System.out.println("Setting markers: " + currentMarker);
        this.detectedMarkers.add(currentMarker, CameraNectar.parseMarkerList(new String(message)));
        currentMarker++;
    }


    public DetectedMarker[] getMarkers(int id) {
        return this.detectedMarkers.get(id);
    }

    public boolean allMarkersFound() {
        return currentMarker == nbMarkers;
    }
    public int getLastMarkerFound() {
        return currentMarker;
    }
    public void saveExtrinsics(HomographyCalibration homography) {
        String json = homography.exportToJSONString();
        String key = this.cameraDescription + ":extrinsics";
        Jedis redis;
        if (nectarCamera != null) {
            redis = nectarCamera.createConnection();
        } else {
            redis = RedisClientImpl.getMainConnection().createConnection();
        }
        redis.set(key, json);
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

        public MarkerListener() {
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            setMarkers(message);
            System.out.println("Got markers: " + currentMarker);
//            this.unsubscribe();
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

    /**
     * *
     * @param fileName
     */
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
//                currentImage = greyProjectorImage(img);
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
//                currentImage = greyProjectorImage(img);
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
