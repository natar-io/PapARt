package fr.inria.papart.apps;

import processing.core.*;
import java.nio.ByteBuffer;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import fr.inria.papart.procam.RedisClientImpl;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.camera.CameraFactory;
import fr.inria.papart.procam.camera.CameraOpenNI2;
import fr.inria.papart.procam.camera.CameraRGBIRDepth;
import fr.inria.papart.procam.camera.CannotCreateCameraException;
import redis.clients.jedis.Jedis;

import processing.data.JSONObject;


/**
 *
 * @author Jeremy Laviole, poqudrof@gmail.com
 */
public class CameraServerImpl extends NectarApplication implements Runnable {

    Jedis redis, redisDepth, redisSend;
    Camera camera;

    // Arguments
    private long millisOffset = System.currentTimeMillis();

    private String driverName = "";
    private String description = "0";
    private String format = "";
    private int width = 640, height = 480;
    private String output = "pose";
    private boolean isStreamSet = false;
    private boolean isStreamPublish = true;
    private Camera.Type type;

    boolean running = true;

    private boolean useDepth = false;

    private long colorImageCount = 0;
    private long depthImageCount = 0;

    private CameraRGBIRDepth dcamera;

    public CameraServerImpl(String[] args) {
        checkArguments(args);
        redis = connectRedis();
        redisDepth = connectRedis();

        RedisClientImpl redisClient = new RedisClientImpl();
        redisClient.setRedisHost(host);
        redisClient.setRedisPort(Integer.parseInt(port));
        redisClient.setRedisAuth("");
        
        try {
            // application only using a camera
            // screen rendering
//            camera = CameraFactory.createCamera(Camera.Type.OPENCV, "0", "");
            camera = CameraFactory.createCamera(type, description, format);
            camera.setSize(width, height);
            System.out.println("Start camera");

            boolean simpleCam = true;
            if (useDepth) {
                if (camera instanceof CameraRGBIRDepth) {

                    System.out.println("Start OpenNI depth camera");

                    dcamera = (CameraRGBIRDepth) camera;
                    dcamera.setUseColor(true);
                    dcamera.setUseDepth(true);
                    sendDepthParams(dcamera.getDepthCamera());
                    initDepthMemory(
                            dcamera.getDepthCamera().width(),
                            dcamera.getDepthCamera().height(),
                            2, 1);

//                    dcamera.getColorCamera().addObserver(new ImageObserver());
//                    dcamera.getDepthCamera().addObserver(new DepthImageObserver());
                    dcamera.actAsColorCamera();

                    simpleCam = false;
                } else {
                    die("Camera not recognized as a depth camera.");
                }
            }

            if (camera instanceof CameraOpenNI2) {
                CameraOpenNI2 cameraNI = (CameraOpenNI2) camera;
                cameraNI.sendToRedis(redisClient, output, output + ":depth");
            }

//            if (simpleCam) {
//                camera.addObserver(new ImageObserver());
//            }

            camera.start();
            sendParams(camera);
            initMemory(width, height, 3, 1);

//            camera.setParent(applet);
//            camera.setCalibration(cameraCalib);
        } catch (CannotCreateCameraException ex) {
            Logger.getLogger(CameraServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            die(ex.toString());
        }

    }

    public Jedis createRedisConnection() {
        return connectRedis();
    }

    private static Options options;

    private String buildDriverNames() {
        Camera.Type[] values = Camera.Type.values();
        StringBuilder driversText = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            driversText.append(values[i]);
            if (i != values.length - 1) {
                driversText.append(", ");
            }
        }
        return driversText.toString();
    }

    private void addCLIArgs(Options options) {
//        options.addRequiredOption("i", "input", true, "Input key of marker locations.");
        options.addRequiredOption("d", "driver", true, "Driver to use amongst: " + buildDriverNames());
        options.addRequiredOption("id", "device-id", true, "Device id, path or name (driver dependant).");
        options.addOption("f", "format", true, "Format, e.g.: for depth cameras rgb, ir, depth.");
        options.addOption("r", "resolution", true, "Image size, can be used instead of width and height, default 640x480.");

        // Generic options
        options.addOption("s", "stream", false, " stream mode (PUBLISH).");
        options.addOption("sg", "stream-set", false, " stream mode (SET).");
        options.addOption("u", "unique", false, "Unique mode, run only once and use get/set instead of pub/sub");
        options.addOption("dc", "depth-camera", false, "Load the depth video when available.");

        options.addRequiredOption("o", "output", true, "Output key.");
    }

    private void checkArguments(String[] passedArgs) {
        options = new Options();

        addCLIArgs(options);
        addDefaultOptions(options);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        // -u -i markers -cc data/calibration-AstraS-rgb.yaml -mc data/A4-default.svg -o pose
        try {
            cmd = parser.parse(options, passedArgs);

            parseDefaultOptions(cmd);

            driverName = cmd.getOptionValue("d");
            type = Camera.Type.valueOf(driverName);
            description = cmd.getOptionValue("id");
            output = cmd.getOptionValue("o");
            format = cmd.hasOption("f") ? cmd.getOptionValue("f") : format;

            if (cmd.hasOption("r")) {
                String resolution = cmd.getOptionValue("r");
                String[] split = resolution.split("x");
                width = Integer.parseInt(split[0]);
                height = Integer.parseInt(split[1]);
            }

            if (cmd.hasOption("sg")) {
                isStreamSet = true;
                isStreamPublish = false;
            }

            useDepth = cmd.hasOption("dc");

        } catch (ParseException ex) {
            die(ex.toString(), true);
//            Logger.getLogger(PoseEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void sendParams(Camera cam) {
        redis.set(output + ":width", Integer.toString(cam.width()));
        redis.set(output + ":height", Integer.toString(cam.height()));
        redis.set(output + ":channels", Integer.toString(3));
        redis.set(output + ":pixelformat", cam.getPixelFormat().toString());
        redis.clientSetname("CameraServer");
    }

    private void sendDepthParams(Camera cam) {
        redisDepth.set(output + ":depth:width", Integer.toString(cam.width()));
        redisDepth.set(output + ":depth:height", Integer.toString(cam.height()));
        redisDepth.set(output + ":depth:channels", Integer.toString(2));
        redisDepth.set(output + ":depth:pixelformat", cam.getPixelFormat().toString());
        redisDepth.clientSetname("DepthCameraServer");
    }

    @Override
    public void run() {
        while (running) {
            sendImage();
        }
    }

    PImage copy = null;
    byte[] imageData;
    byte[] depthImageData;

    private void initMemory(int width, int height, int nChannels, int bytePerChannel) {
        imageData = new byte[nChannels * width * height * bytePerChannel];
    }

    private void initDepthMemory(int width, int height, int nChannels, int bytePerChannel) {
        depthImageData = new byte[nChannels * width * height * bytePerChannel];
    }

    int lastTime = 0;
//
//    class ImageObserver implements Observer {
//
//        @Override
//        public void update(Observable o, Object o1) {
//            sendColorImage();
//        }
//    }
//
//    class DepthImageObserver implements Observer {
//
//        @Override
//        public void update(Observable o, Object o1) {
//            log("", "New Depth Image.");
//            sendDepthImage();
//        }
//    }

    public void sendImage() {
        if (camera != null) {
//            // OpenNI2 camera is not grabbed here
            if (camera instanceof CameraOpenNI2) {
                try {
                    Thread.sleep(100);
                    return;
                } catch (InterruptedException ex) {
                    Logger.getLogger(CameraServerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                // warning, some cameras do not grab ?
                camera.grab();

                sendColorImage();
                if (useDepth) {
                    sendDepthImage();
                }
            }
        }
    }

    private void sendColorImage() {
        ByteBuffer byteBuffer;

//        byteBuffer = dcamera.getColorCamera().getIplImage();
//        IplImage smaller = dcamera.getColorCamera().getIplImage().sc
        if (useDepth) {
            if (dcamera.getColorCamera().getIplImage() == null) {
                log("null color Image -d", "");
                return;
            }
            byteBuffer = dcamera.getColorCamera().getIplImage().getByteBuffer();
        } else {

            // get the pixels from native memory
            if (camera.getIplImage() == null) {
                log("null color Image", "");
                return;
            }
            byteBuffer = camera.getIplImage().getByteBuffer();
        }
        colorImageCount++;
        byteBuffer.get(imageData);
        String name = output;
        byte[] id = name.getBytes();
        String time = Long.toString(time());
        if (isUnique) {
            redis.set(id, imageData);
            running = false;
            log("Sending (SET) image to: " + output, "");
            redis.set((name + ":timestamp"), time);
        } else {
            if (isStreamSet) {
                redis.set(id, imageData);
                redis.set((name + ":timestamp"), time);
                log("Sending (SET) image to: " + output, "");
            }
            if (isStreamPublish) {
                JSONObject imageInfo = new JSONObject();
                imageInfo.setLong("timestamp", time());
                imageInfo.setLong("imageCount", colorImageCount);
                redis.set(id, imageData);
                redis.publish(id, imageInfo.toString().getBytes());
                log("Sending (PUBLISH) image to: " + output, "");
            }
        }

    }

    private void sendDepthImage() {
        if (dcamera.getDepthCamera().getIplImage() == null) {
            log("null depth Image", "");
            return;
        }
        depthImageCount++;
        ByteBuffer byteBuffer = dcamera.getDepthCamera().getIplImage().getByteBuffer();
        byteBuffer.get(depthImageData);
        String name = output + ":depth:raw";
        byte[] id = (name).getBytes();
        String time = Long.toString(time());

        if (isUnique) {
            redisDepth.set(id, depthImageData);
            redisDepth.set((name + ":timestamp"), time);

            running = false;
            log("Sending (SET) image to: " + name, "");
        } else {
            if (isStreamSet) {

                redisDepth.set(id, depthImageData);
                redisDepth.set((name + ":timestamp"), time);

                log("Sending (SET) image to: " + name, "");
            }
            if (isStreamPublish) {
                JSONObject imageInfo = new JSONObject();
                imageInfo.setLong("timestamp", time());
                imageInfo.setLong("imageCount", depthImageCount);
                redisDepth.set(id, depthImageData);
                redisDepth.publish(id, imageInfo.toString().getBytes());
                log("Sending (PUBLISH) image to: " + name, "");
            }

        }

    }

    public long time() {
        return System.currentTimeMillis() - millisOffset;
    }

    /**
     * @param passedArgs the command line arguments
     */
    static public void main(String[] passedArgs) {

        CameraServerImpl cameraServer = new CameraServerImpl(passedArgs);
        System.out.println("Start in main Thread...");
        new Thread(cameraServer).start();
    }

    public String getOutput() {
        return this.output;
    }

}
