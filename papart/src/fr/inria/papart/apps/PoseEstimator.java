package fr.inria.papart.apps;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.RedisClientImpl;
import fr.inria.papart.procam.camera.CameraNectar;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.tracking.MarkerBoardFactory;
import fr.inria.papart.tracking.MarkerBoardSvgNectar;
import fr.inria.papart.tracking.MarkerList;
import fr.inria.papart.utils.MathUtils;
import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * TODO: Extend NectarApplication
 *
 * @author Jeremy Laviole, poqudrof@gmail.com
 */
@SuppressWarnings("serial")
public class PoseEstimator extends NectarApplication {

    static Jedis redis;
    static Jedis redisSend;

    // TODO: add hostname ?
    public static final String OUTPUT_PREFIX = "nectar:";
    public static final String OUTPUT_PREFIX2 = ":camera-server:camera";

    static private String pathName = "";
    static private String cameraFileName = "";
    static private String cameraName = "";
    static private String markerFileName = "";
    static private String input = "marker";
    static private String output = "pose";
    private static boolean isStreamSet = false;

    static ProjectiveDeviceP cameraDevice;
    static MarkerList markersFromSVG;

    static private CameraNectar cam;

    static MarkerBoard board;

    static protected void addAppOptions(Options options) {
        options.addRequiredOption("i", "input", true, "Camera input key .");
//        options.addRequiredOption("cc", "camera-configuration", true, "Camera calibration file.");
//        options.addRequiredOption("mc", "marker-configuration", true, "Marker configuration file.");
        options.addOption("p", "path", true, "Optionnal path.");

        options.addOption("s", "stream", false, " stream mode (PUBLISH).");
        options.addOption("sg", "stream-set", false, " stream mode (SET).");
        options.addOption("u", "unique", false, "Unique mode, run only once and use get/set instead of pub/sub");

        // Generic options
//        options.addRequiredOption("o", "output", true, "Output key.");
    }

    static protected void parseOptions(CommandLine cmd) {
        if (cmd.hasOption("i")) {
            cameraName = cmd.getOptionValue("i");
        }

//        if (cmd.hasOption("cc")) {
//            cameraFileName = cmd.getOptionValue("cc");
//        }
//        if (cmd.hasOption("mc")) {
//            markerFileName = cmd.getOptionValue("mc");
//        }
//        if (cmd.hasOption("o")) {
//            output = cmd.getOptionValue("o");
//        } else {
//            die("Please set an output key with -o or --output ", true);
//        }
        if (cmd.hasOption("p")) {
            pathName = cmd.getOptionValue("p");
        }

        if (cmd.hasOption("sg")) {
            isStreamSet = true;
        }
    }

    static public void main(String[] passedArgs) {

        options = new Options();
        addDefaultOptions(options);
        addAppOptions(options);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, passedArgs);
            parseDefaultOptions(cmd);
            parseOptions(cmd);
        } catch (ParseException ex) {
            die(ex.toString(), true);
        }
        redis = connectRedis();
        redisSend = connectRedis();

        startEstimator();
        System.out.println("Waiting for something.");
    }

    static protected void startEstimator() {
        try {
            Path currentRelativePath = Paths.get(pathName);
            String path = currentRelativePath.toAbsolutePath().toString();
            // Only rgb camera can track markers for now.
            cam = new CameraNectar(cameraName);
            cam.setUseColor(true);
            cam.actAsColorCamera();
//            cam.setCalibration(path + "/" + cameraFileName);

            cam.DEFAULT_REDIS_HOST = host;
            cam.DEFAULT_REDIS_PORT = Integer.parseInt(port);
            
            RedisClientImpl.getMainConnection().setRedisHost(host);
            RedisClientImpl.getMainConnection().setRedisPort(Integer.parseInt(port));

            if (isStreamSet) {
                cam.setGetMode(isStreamSet);
            }
            cam.start();

            cam.loadCalibrations(); // Load calibration from Redis.
//            board = MarkerBoardFactory.create(path + "/" + markerFileName);

            markerboardNames = redis.smembers(cameraName + ":markerboards");
            boards = new HashMap<String, MarkerBoard>();
            for (String mbName : markerboardNames) {

                // Load board from file system if contains a '.' (dot) !
                if (mbName.contains(".")) {
                    MarkerBoard board = MarkerBoardFactory.create(path + "/" + mbName);
                    boards.put(mbName, board);

                    board.addTracker(null, cam.getColorCamera());
                } else {
                    MarkerBoard board = new MarkerBoardSvgNectar(mbName);
                    boards.put(mbName, board);

                    board.addTracker(null, cam.getColorCamera());
                }
            }

            cam.addObserver(new ImageObserver());

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(PoseEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Set<String> markerboardNames;
    private static HashMap<String, MarkerBoard> boards;

    static class ImageObserver implements Observer {

        @Override
        public void update(Observable o, Object o1) {

            for (HashMap.Entry<String, MarkerBoard> entry : boards.entrySet()) {
                String key = entry.getKey();
                MarkerBoard board = entry.getValue();

                
                // USE JSON or XML ???
                String output = cameraName + ":markerboards:" + key;

                board.updateLocation(cam, cam.getIplImage(), null);

                PMatrix3D position = board.getPosition(cam);
                JSONObject matrix = new JSONObject();
                JSONArray poseJson = MathUtils.PMatrixToJSON(position);

                matrix.setJSONArray("matrix", poseJson);
                if (isVerbose) {
                    position.print();
                }
                if(!isSilent){
                    System.out.println("Set:/publish to: " + output);
                }
                if (isStreamSet) {
                    redisSend.set(output, matrix.toString());
                } else {
                    redisSend.publish(output, matrix.toString());
                }

            }

//            board.updateLocation(cam, cam.getIplImage(), null);
//            PMatrix3D position = board.getPosition(cam);
//
//            JSONObject matrix = new JSONObject();
//            JSONArray poseJson = PMatrixToJSON(position);
//
//            matrix.setJSONArray("matrix", poseJson);
//            if (isVerbose) {
//                position.print();
//            }
//            if (isStreamSet) {
//                redisSend.set(output, matrix.toString());
//            } else {
//                redisSend.publish(output, matrix.toString());
//            }
//            Markerboard update... send pose...
        }

    }

    static void sendPose(String message, boolean set) {

        PMatrix3D pose = new PMatrix3D();

        if (pose == null) {
            log("Cannot find pose " + message, "");
            return;
        }
        JSONArray poseJson = MathUtils.PMatrixToJSON(pose);
        if (set) {
            redisSend.set(output, poseJson.toString());
            log("Pose set to " + output, " set " + poseJson.toString());
        } else {
            redisSend.publish(output, poseJson.toString());
            log("Pose updated to " + output, "published " + poseJson.toString());
        }
    }

    static String markersChannels = "custom:image:detected-markers";

    static class MyListener extends JedisPubSub {

        // Listen to "camera
        @Override
        public void onMessage(String channel, String message) {

            log(null, "received " + message);
            sendPose(message, false);
        }

        @Override
        public void onSubscribe(String channel, int subscribedChannels) {
            System.out.println("Subscribe to: " + channel);
        }

        @Override
        public void onUnsubscribe(String channel, int subscribedChannels) {
            System.out.println("CHANNEL: " + channel);
        }

        public void onPSubscribe(String pattern, int subscribedChannels) {
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels) {
        }

        @Override
        public void onPMessage(String pattern, String channel, String message) {
            System.out.println("CHANNEL: " + channel);
        }
    }

}
