package fr.inria.papart.apps;

import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import fr.inria.papart.procam.camera.VideoReceiver;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Jeremy Laviole, poqudrof@gmail.com
 */
@SuppressWarnings("serial")
public class CameraTest extends PApplet {

    private PImage receivedPx;

    @Override
    public void settings() {
        // the application will be rendered in full screen, and using a 3Dengine.
        if (isFullScreen) {
            fullScreen(P3D);
        } else {
            size(640, 480, P3D);

        }
    }
    VideoReceiver videoReceiver;

    @Override
    public void setup() {

        videoReceiver = new VideoReceiver(this, input, createRedis());

        if (isUnique) {
            try {
                receivedPx = videoReceiver.getOnce();
            } catch (Exception ex) {
                Logger.getLogger(CameraTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            videoReceiver.start(createRedis());
        }
    }

    private Jedis createRedis() {
        return new Jedis(host, port);
    }

    @Override
    public void draw() {
        background(255);
        image(videoReceiver.getReceivedPx(), 0, 0, width, height);
    }

    static String defaultName = "camera";
    static Options options = new Options();

    public static final int REDIS_PORT = 6379;
    public static final String REDIS_HOST = "localhost";
    static private String input = "marker";
    static private String host = REDIS_HOST;
    static private int port = REDIS_PORT;
    static private boolean isUnique = false;

    static boolean isVerbose = false;
    static boolean isSilent = false;
    static boolean isFullScreen = false;

    /**
     * @param passedArgs the command line arguments
     */
    static public void main(String[] passedArgs) {
        checkArguments(passedArgs);

        String[] appletArgs = new String[]{CameraTest.class.getName()};
        PApplet.main(appletArgs);
    }

    private static void checkArguments(String[] passedArgs) {
        options = new Options();

//        public static Camera createCamera(Camera.Type type, String description, String format)
//        options.addRequiredOption("i", "input", true, "Input key of marker locations.");
        // Generic options
        options.addOption("h", "help", false, "print this help.");
        options.addOption("v", "verbose", false, "Verbose activated.");
        options.addOption("s", "silent", false, "Silent activated.");
        options.addOption("f", "fullscreen", false, "Fullscreen mode.");
        options.addOption("u", "unique", false, "Unique mode, run only once and use get/set instead of pub/sub");
        options.addRequiredOption("i", "input", true, "Inpput key for the image.");
        options.addOption("rp", "redisport", true, "Redis port, default is: " + REDIS_PORT);
        options.addOption("rh", "redishost", true, "Redis host, default is: " + REDIS_HOST);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        // -u -i markers -cc data/calibration-AstraS-rgb.yaml -mc data/A4-default.svg -o pose
        try {
            cmd = parser.parse(options, passedArgs);

            if (cmd.hasOption("i")) {
                input = cmd.getOptionValue("i");
            }

            if (cmd.hasOption("h")) {
                die("", true);
            }

            if (cmd.hasOption("u")) {
                isUnique = true;
            }
            if (cmd.hasOption("v")) {
                isVerbose = true;
            }
            if (cmd.hasOption("s")) {
                isSilent = true;
            }
            if (cmd.hasOption("f")) {
                isFullScreen = true;
            }
            if (cmd.hasOption("rh")) {
                host = cmd.getOptionValue("rh");
            }
            if (cmd.hasOption("rp")) {
                port = Integer.parseInt(cmd.getOptionValue("rp"));
            }
        } catch (ParseException ex) {
            die(ex.toString(), true);
        }

    }

    public static void die(String why, boolean usage) {
        if (usage) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CameraTest", options);
        }
        System.out.println(why);
        System.exit(-1);
    }

    public static void log(String normal, String verbose) {
        if (isSilent) {
            return;
        }
        if (normal != null) {
            System.out.println(normal);
        }
        if (isVerbose && verbose != null) {
            System.out.println(verbose);
        }
    }

}
