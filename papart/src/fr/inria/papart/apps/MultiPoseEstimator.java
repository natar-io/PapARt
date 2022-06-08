package fr.inria.papart.apps;

import java.util.ArrayList;
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
import fr.inria.papart.tracking.MarkerBoardSvgNectar;
import fr.inria.papart.tracking.MarkerBoardSvg;
import fr.inria.papart.tracking.MarkerList;
import fr.inria.papart.utils.MathUtils;
import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole, poqudrof@gmail.com
 */
@SuppressWarnings("serial")
public class MultiPoseEstimator extends NectarApplication {

    static private Jedis redis;

    static private String cameraName = "";
    static private boolean noFiltering = false;

    static ProjectiveDeviceP cameraDevice;
    static MarkerList markersFromSVG;

    static private CameraNectar cam;
    static final private ArrayList<MarkerBoard> markerboards = new ArrayList<>();
    private static boolean useXML = false;

    static public void main(String[] passedArgs) {

        parseCLI(passedArgs);

        RedisClientImpl mainConnection = RedisClientImpl.getMainConnection();
        mainConnection.setRedisHost(host);
        mainConnection.setRedisPort(Integer.parseInt(port));

        redis = connectRedis();
        try {
            /**
             * 1. Load all MarkerBoards. 2. Substribe to markers. 3. When a
             * markerList is received -> Find the pose if possible. 4. Send
             * (publish & Set) new location.
             */
            // Subscribe to markers 
            // Only rgb camera can track markers for now.
            // Camera is used to obtain the projective Device (calibration)
            cam = new CameraNectar(cameraName);
            cam.setUseColor(true);
            cam.actAsColorCamera();

            cam.setRedisClient(mainConnection);
            // Connect to camera
            cam.start();

            // Get all all markerboards... 
//            redis.get(host)
            Set<String> boardKeys = redis.smembers("markerboards");
            for (String boardName : boardKeys) {
//                System.out.println("Key: " + key);
                MarkerBoard board;

                if (useXML) {
                    MarkerBoardSvgNectar boardNectar = new MarkerBoardSvgNectar(boardName);
                    boardNectar.load(mainConnection);
                    board = boardNectar;
                    board.addTracker(null, cam.getPublicCamera());
                    if (noFiltering) {
                        board.removeFiltering(cam);
                    }

                } else {
                    String key = "markerboards:json:" + boardName;
                    JSONArray markersJson = JSONArray.parse(redis.get(key));  // TODO: check that the get succeeded
                    if (markersJson == null) {
                        System.out.println("Cannot read marker configuration: " + boardName);
                        return;
                    }
                    MarkerList markers = MarkerList.createFromJSON(markersJson);
                    board = new MarkerBoardSvg(boardName, markers);
                    board.addTracker(null, cam.getPublicCamera());
                    if (noFiltering) {
                        board.removeFiltering(cam);
                    }
                }
                System.out.println("Tracking: " + boardName);
                markerboards.add(board);
            }
            System.out.println("ProjectiveDevice: " + cam.getProjectiveDevice());
            cam.addObserver(new ImageObserver());
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(MultiPoseEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Waiting for images.");
    }

    private static void addCLIArgs(Options options) {
    }

    static class ImageObserver implements Observer {

        @Override
        public void update(Observable o, Object o1) {
            log("", "New Image.");
            for (MarkerBoard board : markerboards) {
                board.updateLocation(cam, cam.getIplImage(), null);

                String name = board.getFileName();
                String outputKey = cam.getCameraDescription() + ":markerboards:" + name;

                PMatrix3D position = board.getPosition(cam);
                JSONObject matrix = new JSONObject();
                JSONArray poseJson = MathUtils.PMatrixToJSON(position);

                matrix.setJSONArray("matrix", poseJson);

                redis.set(outputKey, matrix.toString());
                redis.publish(outputKey, matrix.toString());

                log("Set/Publish to " + outputKey + ".", matrix.toString());
            }
            log("", "New Image - end.");
        }

    }

// <editor-fold defaultstate="collapsed" desc="Command line parsing">
    static private void parseCLI(String[] passedArgs) {

        addCLIArgs(options);
        addDefaultOptions(options);

        options = new Options();
        addDefaultOptions(options);

        options.addRequiredOption("i", "input", true, "Camera input key .");
        options.addOption("rf", "remove-filtering", false, "Remove the filtering.");
        options.addOption("x", "xml", false, "Use XML source instead of JSON.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        // -u -i markers -cc data/calibration-AstraS-rgb.yaml -mc data/A4-default.svg -o pose
        try {
            cmd = parser.parse(options, passedArgs);
            parseDefaultOptions(cmd);

            if (cmd.hasOption("i")) {
                cameraName = cmd.getOptionValue("i");
            }
            if (cmd.hasOption("x")) {
                useXML = true;
                System.out.println("Using XML file, instead of JSON.");
            }

            noFiltering = cmd.hasOption("rf");

        } catch (ParseException ex) {
            die(ex.toString(), true);
        }
    }
    // </editor-fold>
}
