package fr.inria.papart.apps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.tracking.MarkerBoardSvg;
import fr.inria.papart.utils.MathUtils;
import processing.core.*;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.data.XML;
import redis.clients.jedis.Jedis;

/**
 * Load a configuration file: Projective Device, 3D Transformation (matrix), and
 * markerboards into Redis for Nectar.
 *
 * @author Jeremy Laviole, poqudrof@gmail.com
 */
@SuppressWarnings("serial")
public class ConfigurationLoader extends NectarApplication {

    static private Jedis redis;
    static private String pathName = "";
    static private String fileName = "";
    static private String output = "config";
    static private String path;

    // Type of configuration to load
    static private boolean isMarkerboard = false;
    static private boolean isMarkerboardXML = false;
    static private boolean isMatrix = false;
    static private boolean isMatrixInverted = false;
    static private boolean isProjectiveDevice = false;
    static private boolean isProjector = false;

    static public void main(String[] passedArgs) {

        options = new Options();
        addCLIArgs(options);
        addDefaultOptions(options);

        parseCLI(passedArgs);
        redis = connectRedis();

        Path currentRelativePath = Paths.get(pathName);
        path = currentRelativePath.toAbsolutePath().toString();

        if (isMatrix) {
            loadMatrix();
        }
        if (isMarkerboard) {
            loadMarkerboard();
        }

        if (isProjectiveDevice) {
            loadProjectiveDevice();
        }
    }

    private static void addCLIArgs(Options options) {
        options.addRequiredOption("f", "file", true, "filename.");
        options.addOption("p", "path", true, "Optionnal path.");
        options.addOption("i", "invert-matrix", false, "Invert matrix.");
        options.addOption("m", "matrix", false, "Activate when the file is a matrix.");
        options.addOption("pd", "projective-device", false, "Activate when the file is projective device.");
        options.addOption("pr", "projector", false, "Load a projector configuration, instead of camera.");
        options.addOption("mb", "markerboard", false, "Load a markerboard file.");
        options.addRequiredOption("o", "output", true, "Output key.");
    }

    private static void parseCLI(String[] passedArgs) {

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, passedArgs);
            ConfigurationLoader.parseDefaultOptions(cmd);

            if (cmd.hasOption("f")) {
                fileName = cmd.getOptionValue("f");
            } else {
                die("Please input a filename with -f of --file option.");
            }
            if (cmd.hasOption("o")) {
                output = cmd.getOptionValue("o");
            } else {
                die("Please set an output key with -o or --output ");
            }
            if (cmd.hasOption("p")) {
                pathName = cmd.getOptionValue("p");
                System.out.println("PATH: " + pathName);
            }
            if (cmd.hasOption("m")) {
                isMatrix = true;
            }
            if (cmd.hasOption("mb")) {
                isMarkerboard = true;
            }

            if (cmd.hasOption("i")) {
                isMatrixInverted = true;
            }
            if (cmd.hasOption("pd")) {
                isProjectiveDevice = true;
            }
            if (cmd.hasOption("pr")) {
                isProjector = true;
            }
            if (!(isMatrix || isProjectiveDevice || isMarkerboard )) {
                die("Please specifiy the type of file: matrix, markerboard, or projective device.");
            }

            int s = (isMatrix ? 1 : 0)
                    + (isProjectiveDevice ? 1 : 0)
                    + (isMarkerboard ? 1 : 0);
            if (s > 1) {
                die("Please specifiy the type of file: matrix, markerboard or projective device. It can be only one.");
            }

        } catch (ParseException ex) {
            die(ex.toString(), true);
        }

    }

    private static void loadProjectiveDevice() {
        ProjectiveDeviceP pdp;
        try {
            if (isProjector) {
                pdp = ProjectiveDeviceP.loadProjectorDevice(path + "/" + fileName);
            } else {
                pdp = ProjectiveDeviceP.loadCameraDevice(path + "/" + fileName);
            }
            redis.set(output, pdp.toJSON().toString());
            log(fileName + " loaded to " + output, pdp.toJSON().toString());
        } catch (Exception ex) {
            Logger.getLogger(ConfigurationLoader.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadMarkerboard() {
        MarkerBoardSvg markerboard = new MarkerBoardSvg(path + "/" + fileName, 200, 200);
        String markers = markerboard.getMarkerList().toJSON().toString();
        String outputKey = "markerboards:json:" + output;
        redis.set(outputKey, markers);
        redis.sadd("markerboards", output);
        log(fileName + " loaded to " + outputKey, markers);

        try {
            XML xml;
            xml = new XML(new File(path + "/" + fileName));
            String markers2 = xml.toString();
            outputKey = "markerboards:xml:" + output;
            redis.set(outputKey, markers2);
            log(fileName + " -XML- loaded to " + outputKey, markers);
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ConfigurationLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(ConfigurationLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void loadMatrix() {
        PMatrix3D mat = loadCalibration(path + "/" + fileName);
        if (mat == null) {
            die("Cannot read the matrix from: " + fileName);
        }
        if (isMatrixInverted) {
            mat.invert();
        }
        JSONObject cp = new JSONObject();
        cp.setJSONArray("matrix", MathUtils.PMatrixToJSON(mat));
        redis.set(output, cp.toString());
        log(fileName + " loaded to " + output, cp.toString());
    }

    public static PMatrix3D loadCalibration(String fileName) {
//        File f = new File(sketchPath() + "/" + fileName);
        File f = new File(fileName);
        if (f.exists()) {
            try {
                //            return HomographyCalibration.getMatFrom(this, sketchPath() + "/" + fileName);
                return HomographyCalibration.getMatFrom(fileName);
            } catch (Exception ex) {
                Logger.getLogger(ConfigurationLoader.class
                        .getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        } else {
            return null;
        }
    }

}
