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
import java.util.Locale;

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

    /**
     * Used for tests only, it requires environment variables.
     *
     * @return
     */
    public static String getPapartFolder() {
//        String sketchbook = System.getenv("SKETCHBOOK");
//        if (sketchbook != null) {
//            return sketchbook + "/libraries/" + LIBRARY_NAME;
//        }
        String home = System.getenv("HOME");
        OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
        switch (ostype) {
            case Windows:
                break;
            case MacOS:
                return home + "/Processing/libraries/" + LIBRARY_NAME;
            case Linux:
                return home + "/sketchbook/libraries/" + LIBRARY_NAME;
            case Other:
                break;
        }
        return "";
    }

    public static String getPapartDataFolder() {

        // Sketchbook env variable -- Developpers.
        String sketchbook = System.getenv("SKETCHBOOK");
        if (sketchbook != null) {
        System.out.println("Papart data path: " + sketchbook + "/libraries/" + LIBRARY_NAME + "/data");
            return sketchbook + "/libraries/" + LIBRARY_NAME + "/data";
        }
//         Home env variable -- Linux / OSX.
        File candidate = null;
        String home = System.getenv("HOME");
        if (home != null) {
            OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
            switch (ostype) {
                case Windows:
                    break;
                case MacOS:
                    System.out.println("PapARt data folder: " + home + "/Processing/libraries/" + LIBRARY_NAME);
                    candidate = new File(home + "/Processing/libraries/" + LIBRARY_NAME + "/data");
                    break;
                case Linux:
                    candidate = new File(home + "/sketchbook/libraries/" + LIBRARY_NAME + "/data");
                    break;
                case Other:
                    break;
            }
        }

        if (candidate != null && candidate.exists()) {
            System.out.println("Papart data path: " + candidate.getAbsolutePath());
            return candidate.getAbsolutePath();
        }
        // Use the jar to find the data folder -- any OS.
        URL main = Papart.class.getResource("Papart.class");
        String tmp = main.getPath();
        // its in a jar
        if (tmp.contains("!")) {
            tmp = tmp.substring(0, tmp.indexOf('!'));
            tmp = tmp.replace("file:", "");
        }
        File f = new File(tmp);
        if (!f.exists()) {
            System.err.println("Error in loading the Sketchbook folder.");
        }
        //     pathToSketchbook/libraries/PapARt/library/PapARt.jar
        f = f.getParentFile();

        // Either there is a data folder, or it is in a subfolder
//        File data1 = new File(f.getAbsolutePath() + "/data");
        File data2 = new File(f.getParentFile().getAbsolutePath() + "/data");
        
        if (data2.exists()) {
            System.out.println("Papart data path: " + data2.getAbsolutePath());
            return data2.getAbsolutePath();
        }

        System.out.println("Papart: Cannot find the data folder. It must be installed with the library.");
        System.exit(0);
        return "";
    }

    public static String getLibrariesFolder() {
        // This is used, as Papart classes are often linked to another folder...
//        URL main = Matrix4x4.class.getResource("Matrix4x4.class");
        URL main = Papart.class.getResource("Papart.class");
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

    /**
     * helper class to check the operating system this Java VM runs in
     *
     * please keep the notes below as a pseudo-license
     * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
     * compare to
     * http://svn.terracotta.org/svn/tc/dso/tags/2.6.4/code/base/common/src/com/tc/util/runtime/Os.java
     * http://www.docjar.com/html/api/org/apache/commons/lang/SystemUtils.java.html
     */
    public static final class OsCheck {

        /**
         * types of Operating Systems
         */
        public enum OSType {
            Windows, MacOS, Linux, Other
        };

        // cached result of OS detection
        protected static OSType detectedOS;

        /**
         * detect the operating system from the os.name System property and
         * cache the result
         *
         * @returns - the operating system detected
         */
        public static OSType getOperatingSystemType() {
            if (detectedOS == null) {
                String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
                if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
                    detectedOS = OSType.MacOS;
                } else if (OS.indexOf("win") >= 0) {
                    detectedOS = OSType.Windows;
                } else if (OS.indexOf("nux") >= 0) {
                    detectedOS = OSType.Linux;
                } else {
                    detectedOS = OSType.Other;
                }
            }
            return detectedOS;
        }
    }

}
