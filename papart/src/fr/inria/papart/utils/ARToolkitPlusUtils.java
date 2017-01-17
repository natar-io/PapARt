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
package fr.inria.papart.utils;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.Camera.PixelFormat;

import org.bytedeco.javacv.CameraDevice;
import org.bytedeco.javacv.ProjectorDevice;
import org.bytedeco.javacpp.opencv_imgproc;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import processing.core.*;
import static processing.core.PConstants.ARGB;
import static processing.core.PConstants.RGB;
import processing.opengl.Texture;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;
import java.io.*;
import java.nio.FloatBuffer;
import static processing.core.PConstants.ALPHA;
import static processing.core.PConstants.GRAY;
import static processing.core.PConstants.HSB;

/**
 *
 * @author jeremy
 */
public class ARToolkitPlusUtils {


    static int conversionCount = 0;

    static public void convertARParam2(PApplet pa, String inputYAML, String outputDAT) throws Exception {
        CameraDevice cam = null;

        // Hack 
        if (inputYAML.endsWith(".xml")) {
            convertARParamXML(pa, inputYAML, outputDAT);
            return;
        }

        CameraDevice[] c = CameraDevice.read(inputYAML);
        if (c.length > 0) {
            cam = c[0];
        }
        CameraDevice.Settings camSettings = (org.bytedeco.javacv.CameraDevice.Settings) cam.getSettings();
        int w = camSettings.getImageWidth();
        int h = camSettings.getImageHeight();

        double[] proj = cam.cameraMatrix.get();

        PrintWriter pw = pa.createWriter(outputDAT);

        StringBuffer sb = new StringBuffer();

//        byte[] buf = new byte[SIZE_OF_PARAM_SET];
//        ByteBuffer bb = ByteBuffer.wrap(buf);
//        bb.order(ByteOrder.BIG_ENDIAN);
//        bb.putInt(w);
//        bb.putInt(h);
        // From ARToolkitPlus...
//http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
        sb.append("ARToolKitPlus_CamCal_Rev02\n");
        sb.append(w).append(" ").append(h).append(" ");

        // cx cy  fx fy  
        sb.append(proj[2]).append(" ").append(proj[5])
                .append(" ").append(proj[0]).
                append(" ").append(proj[4]).append(" ");

        // alpha_c  // skew factor  
        sb.append("0 ").append(" ");

        if (cam.distortionCoeffs != null) {
            double[] distort = cam.distortionCoeffs.get();
            // alpha_c ?  
//        sb.append("0 ");
            // kc(1 - x)  -> 6 values
            for (int i = 0; i < distort.length; i++) {
                sb.append(distort[i]).append(" ");
            }
            for (int i = distort.length; i < 6; i++) {
                sb.append("0 ");
            }
        } else {
            for (int i = 0; i < 6; i++) {
                sb.append("0 ");
            }
        }
        // undist iterations
        sb.append("10\n");

        pw.print(sb);
        pw.flush();
        pw.close();
    }

    static public void convertARParamFromDevice(PApplet pa, ProjectiveDeviceP pdp, String outputDAT) throws Exception {

        PrintWriter pw = pa.createWriter(outputDAT);
        StringBuffer sb = new StringBuffer();

        // From ARToolkitPlus...
//http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
        sb.append("ARToolKitPlus_CamCal_Rev02\n");
        sb.append(pdp.getWidth()).append(" ").append(pdp.getHeight()).append(" ");

        // cx cy  fx fy  
        sb.append(pdp.getCx()).append(" ").append(pdp.getCy())
                .append(" ").append(pdp.getFx()).
                append(" ").append(pdp.getFy()).append(" ");

        // alpha_c  // skew factor  
        sb.append("0 ").append(" ");

//        if(pdp.handleDistorsions()){
        // TODO: handle the distorsions... 
//            double[] distort = cam.distortionCoeffs.get();
//            // alpha_c ?  
////        sb.append("0 ");
//            // kc(1 - x)  -> 6 values
//            for (int i = 0; i < distort.length; i++) {
//                sb.append(distort[i]).append(" ");
//            }
//            for (int i = distort.length; i < 6; i++) {
//                sb.append("0 ");
//            }
//        } else {
        for (int i = 0; i < 6; i++) {
            sb.append("0 ");
        }
//        }
        // undist iterations
        sb.append("10\n");

        pw.print(sb);
        pw.flush();
        pw.close();
    }

    static public void convertProjParam(PApplet pa, String inputYAML, String outputDAT) throws Exception {

        ProjectorDevice cam = null;

        ProjectorDevice[] c = ProjectorDevice.read(inputYAML);
        if (c.length > 0) {
            cam = c[0];
        }
        ProjectorDevice.Settings projSettings = (org.bytedeco.javacv.ProjectorDevice.Settings) cam.getSettings();
        int w = projSettings.getImageWidth();
        int h = projSettings.getImageHeight();

        double[] mat = cam.cameraMatrix.get();
        double[] distort = cam.distortionCoeffs.get();

        OutputStream os = pa.createOutput(outputDAT);

        PrintWriter pw = pa.createWriter(outputDAT);

        StringBuffer sb = new StringBuffer();

//        byte[] buf = new byte[SIZE_OF_PARAM_SET];
//        ByteBuffer bb = ByteBuffer.wrap(buf);
//        bb.order(ByteOrder.BIG_ENDIAN);
//        bb.putInt(w);
//        bb.putInt(h);
        // From ARToolkitPlus...
//http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
        sb.append("ARToolKitPlus_CamCal_Rev02\n");
        sb.append(w).append(" ").append(h).append(" ");

        // cx cy  fx fy  
        sb.append(mat[2]).append(" ").append(mat[5])
                .append(" ").append(mat[0]).
                append(" ").append(mat[4]).append(" ");

        // alpha_c  // skew factor  
        sb.append("0 ").append(" ");

        // alpha_c ?  
//        sb.append("0 ");
        // kc(1 - x)  -> 6 values
        for (int i = 0; i < distort.length; i++) {
            sb.append(distort[i]).append(" ");
        }
        for (int i = distort.length; i < 6; i++) {
            sb.append("0 ");
        }

        // undist iterations
        sb.append("10\n");

        pw.print(sb);
        pw.flush();
        pw.close();
    }

    static public void convertARParamXML(PApplet pa, String fileName, String outputDAT) throws Exception {

//        System.out.println("Convert AR Param XML");
        CameraDevice cam = null;

        ProjectiveDeviceP pdp = ProjectiveDeviceP.loadCameraDevice(pa, fileName);

//        CameraDevice[] c = CameraDevice.read(fileName);
//        if (c.length > 0) {
//            cam = c[0];
//        }
        OutputStream os = pa.createOutput(outputDAT);
        PrintWriter pw = pa.createWriter(outputDAT);
        StringBuffer sb = new StringBuffer();

        // From ARToolkitPlus...
        //http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
        sb.append("ARToolKitPlus_CamCal_Rev02\n");
        sb.append(pdp.getWidth()).append(" ").append(pdp.getHeight()).append(" ");

        // cx cy fx fy  
        sb.append(pdp.getCx()).append(" ").append(pdp.getCy())
                .append(" ").append(pdp.getFx()).
                append(" ").append(pdp.getFy()).append(" ");

        // alpha_c  // skew factor  
        sb.append("0 ").append(" ");

        // kc(1 - x)  -> 6 values
        if (pdp.handleDistorsions()) {
            double[] distort = ((CameraDevice) pdp.getDevice()).distortionCoeffs.get();

            for (int i = 0; i < distort.length; i++) {
                sb.append(distort[i]).append(" ");
            }
            // fill with 0s the end. 
            for (int i = distort.length; i < 5; i++) {
                sb.append("0 ");
            }
        } else {
            for (int i = 0; i < 5; i++) {
                sb.append("0 ");
            }
        }

        // undist iterations
        sb.append("10\n");

        pw.print(sb);
        pw.flush();
        pw.close();
    }

}
