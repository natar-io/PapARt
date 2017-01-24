/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.utils;

import fr.inria.papart.procam.Papart;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import toxi.geom.Matrix4x4;

/**
 *
 * @author Jeremy Laviole
 */
public class LibraryUtils {

    public static final String LIBRARY_NAME = "PapARt";

    public static Process runExample(String exampleName, boolean silent) {
        try {
            StringBuilder commandLine = new StringBuilder();
            String papartFolder = getPapartFolder();
            String sketchFolder = "/examples/" + exampleName + "/";
            // On linux only
            commandLine.append("nohup ");
            // TODO: find processing-java even when not installed !
            // Or make an easy install...
            commandLine.append("processing-java ");
            commandLine.append("--sketch=").append(papartFolder).append(sketchFolder).append(" --output=").append(papartFolder).append(sketchFolder).append("build").append(" --force --run");
            //        commandLine.append("\"");
            // processing-java --sketch=/home/jiii/papart/sketches/papartExamples/Kinect/MultiTouchKinect/ --output=/home/jiii/papart/sketches/papartExamples/Kinect/MultiTouchKinect/build --force --run
            //            println("Starting... \n" + commandLine.toString());
            // TODO: Alternative on Windows... when /bin/sh is not installed.
            Process p = Runtime.getRuntime().exec(commandLine.toString());
            //            Process p = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", commandLine.toString()});
            //            Process p = Runtime.getRuntime().exec(new String[]{"nohup", commandLine.toString()});
            if (!silent) {
                String line;
                BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((line = bri.readLine()) != null) {
                    System.out.println(line);
                }
                bri.close();
                while ((line = bre.readLine()) != null) {
                    System.out.println(line);
                }
                bre.close();
            }
            return p;
            // p.waitFor();
        } catch (Exception e) {
            System.out.println("Could not start the Papart example : " + exampleName + "\n" + e);
        }
        ;
        return null;
    }
    // sketchbook

    public static String getPapartFolder() {
        String sketchbook = System.getenv("SKETCHBOOK");
        if (sketchbook != null) {
//            System.out.println("Found  SKETCHBOOK environment variable.");
            return sketchbook + "/libraries/" + LIBRARY_NAME;
        }
        return getLibrariesFolder() + "/" + LIBRARY_NAME;
    }

    public static String getLibrariesFolder() {
        // This is used, as Papart classes are often linked to another folder...
        URL main = Matrix4x4.class.getResource("Matrix4x4.class");
//        URL main = Papart.class.getResource("Papart.class");
        String tmp = main.getPath();
//        System.out.println("path  " + tmp);
        // its in a jar
        if (tmp.contains("!")) {
            tmp = tmp.substring(0, tmp.indexOf('!'));
            tmp = tmp.replace("file:", "");
            //        tmp = tmp.replace("file:/", "");  TODO: OS check ?
        }
        File f = new File(tmp);
        if (!f.exists()) {
            System.err.println("Error in loading the Sketchbook folder.");
        }
        // if the file is within a library/lib folder
        if (f.getParentFile().getAbsolutePath().endsWith("/lib")) {
            //     pathToSketchbook/libraries/myLib/library/lib/myLib.jar
            f = f.getParentFile().getParentFile().getParentFile().getParentFile();
        } else {
            //     pathToSketchbook/libraries/myLib/library/myLib.jar
            f = f.getParentFile().getParentFile().getParentFile();
        }
        return f.getAbsolutePath();
    }

    // processing-java --sketch=/home/jiii/papart/sketches/papartExamples/Kinect/MultiTouchKinect/ --output=/home/jiii/papart/sketches/papartExamples/Kinect/MultiTouchKinect/build --force --run
    public static String getLibraryFolder(String libname) {
        return getLibrariesFolder() + "/libraries/" + libname;
    }
    
}
