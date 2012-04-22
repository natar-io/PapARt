/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

import com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectorDevice;
import com.googlecode.javacv.cpp.ARToolKitPlus.Tracker;
import com.googlecode.javacv.cpp.opencv_imgproc;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import processing.core.*;

/**
 *
 * @author jeremy
 */
public class Utils {

    static public PVector mult(PMatrix3D mat, PVector source, PVector target) {
        if (target == null) {
            target = new PVector();
        }
        target.x = mat.m00 * source.x + mat.m01 * source.y + mat.m02 * source.z + mat.m03;
        target.y = mat.m10 * source.x + mat.m11 * source.y + mat.m12 * source.z + mat.m13;
        target.z = mat.m20 * source.x + mat.m21 * source.y + mat.m22 * source.z + mat.m23;
        float tw = mat.m30 * source.x + mat.m31 * source.y + mat.m32 * source.z + mat.m33;
        if (tw != 0 && tw != 1) {
            target.div(tw);
        }
        return target;
    }

    static public IplImage createImageFrom(IplImage imgIn, PImage Pout) {
        // TODO: avoid this creation !!
        CvSize outSize = new CvSize();
        outSize.width(Pout.width);
        outSize.height(Pout.height);
        IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                3);
        return imgOut;
    }

    static public void createAnaglyph(PImage imgL, PImage imgR, PImage imgOut) {

        imgL.loadPixels();
        imgR.loadPixels();
        imgOut.loadPixels();

        int[] pL = imgL.pixels;
        int[] pR = imgR.pixels;
        int[] pO = imgOut.pixels;
        for (int i = 0; i < pL.length; i++) {
            pO[i] = (pR[i] >> 16) << 16
                    | (pL[i] >> 8) & 0xFF << 8
                    | pL[i] & 0xFF;

//            pO[i] = pL[i];
        }
        imgOut.updatePixels();
        imgL.updatePixels();

    }

    static public void remapImage(PVector[] in, PVector[] out, IplImage imgIn, IplImage imgTmp, PImage Pout) {

        CvMat srcPoints;
        CvMat dstPoints;
        int nbPoints = in.length;
        CvMat homography;

        srcPoints = cvCreateMat(2, in.length, CV_32FC1);
        dstPoints = cvCreateMat(2, in.length, CV_32FC1);
        homography = cvCreateMat(3, 3, CV_32FC1);

        for (int i = 0; i < in.length; i++) {
            srcPoints.put(i, in[i].x);
            srcPoints.put(i + nbPoints, in[i].y);
            dstPoints.put(i, out[i].x);
            dstPoints.put(i + nbPoints, out[i].y);
        }

        cvFindHomography(srcPoints, dstPoints, homography);
//       It is better to use : GetPerspectiveTransform


        opencv_imgproc.cvWarpPerspective(imgIn, imgTmp, homography);
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
//                getFillColor());
        IplImageToPImage(imgTmp, false, Pout);

    }

    static public void IplImageToPImage(IplImage img, PApplet applet, boolean RGB, PImage ret) {

        IplImageToPImage(img, RGB, ret);

//        assert (img.width() == ret.width);
//        assert (img.height() == ret.height);
//
////        BufferedImage bimg = new BufferedImage();
//        ByteBuffer buff = img.getByteBuffer();
//
//        //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
//        ret.loadPixels();
//        if (RGB) {
//            for (int i = 0; i < img.width() * img.height(); i++) {
//                int offset = i * 3;
////            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
//
//                ret.pixels[i] = (buff.get(offset) & 0xFF) << 16
//                        | (buff.get(offset + 1) & 0xFF) << 8
//                        | (buff.get(offset + 2) & 0xFF);
//
//            }
//        } else {
//            for (int i = 0; i < img.width() * img.height(); i++) {
//                int offset = i * 3;
////            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
//
//                ret.pixels[i] = (buff.get(offset + 2) & 0xFF) << 16
//                        | (buff.get(offset + 1) & 0xFF) << 8
//                        | (buff.get(offset) & 0xFF);
//
//            }
//
//        }
//        ret.updatePixels();
//        //   return ret;
    }

    static public void IplImageToPImage(IplImage img, boolean RGB, PImage ret) {

        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
//        BufferedImage bimg = new BufferedImage();
        ByteBuffer buff = img.getByteBuffer();

        //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
        ret.loadPixels();
        if (RGB) {
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 3;
//            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);

                ret.pixels[i] = (buff.get(offset) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset + 2) & 0xFF);

            }
        } else {
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 3;
//            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);

                ret.pixels[i] = (buff.get(offset + 2) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset) & 0xFF);

            }

        }
        ret.updatePixels();
        //   return ret;
    }

    /*
     * Deprecated
     */
    static public void PImageToIplImage(IplImage img, PApplet applet, boolean RGB, PImage ret) {

        ByteBuffer buff = img.getByteBuffer();

        ret.loadPixels();
        if (RGB) {
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 3;
                ret.pixels[i] = (buff.get(offset) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset + 2) & 0xFF);

            }
        } else {
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 3;
                ret.pixels[i] = (buff.get(offset + 2) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset) & 0xFF);

            }

        }
        ret.updatePixels();
    }

    void test(IplImage img) {
        Tracker track = new Tracker();
//        Camera cam = new Camera(img.imageData());

    }
    //                                   int int  12 double  4 double
    static final int SIZE_OF_PARAM_SET = 4 + 4 + (3 * 4 * 8) + (4 * 8);

    static public void convertARParam(PApplet pa, String inputYAML, String outputDAT, int w, int h) throws Exception {

        CameraDevice cam = null;

        CameraDevice[] c = CameraDevice.read(inputYAML);
        if (c.length > 0) {
            cam = c[0];
        }

        double[] proj = cam.cameraMatrix.get();
        double[] distort = cam.distortionCoeffs.get();

        OutputStream os = pa.createOutput(outputDAT);

        byte[] buf = new byte[SIZE_OF_PARAM_SET];
        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(ByteOrder.BIG_ENDIAN);

        bb.putInt(w);
        bb.putInt(h);

        //Projection
        int k = 0;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                bb.putDouble(proj[k++]);
            }
            bb.putDouble(0);
        }

        bb.putDouble(proj[2]);
        bb.putDouble(proj[5]);
        bb.putDouble(100);
        bb.putDouble(1d);

        os.write(buf);
        os.flush();
        os.close();

        pa.println("Conversion done !");
        return;
    }

    static public void convertProjParam(PApplet pa, String inputYAML, String outputDAT, int w, int h) throws Exception {

        ProjectorDevice proj = null;

        ProjectorDevice[] p = ProjectorDevice.read(inputYAML);
        if (p.length > 0) {
            proj = p[0];
        }

        double[] projM = proj.cameraMatrix.get();
        double[] distort = proj.distortionCoeffs.get();

        OutputStream os = pa.createOutput(outputDAT);

        byte[] buf = new byte[SIZE_OF_PARAM_SET];
        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.order(ByteOrder.BIG_ENDIAN);

        bb.putInt(w);
        bb.putInt(h);

        //projection
        int k = 0;
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                bb.putDouble(projM[k++]);
            }
            bb.putDouble(0);
        }

// ARtoolkit distortion
        bb.putDouble(projM[2]);
        bb.putDouble(projM[5]);
        bb.putDouble(0);
        bb.putDouble(1d);


        os.write(buf);
        os.flush();
        os.close();

        pa.println("Conversion done !");
        return;
    }
//     static public void convertARParam(PApplet pa, String inputYAML, String outputDAT, int w, int h) throws Exception {
//
//        CameraDevice cam = null;
//
//        CameraDevice[] c = CameraDevice.read(inputYAML);
//        if (c.length > 0) {
//            cam = c[0];
//        }
//
//        double[] proj = cam.cameraMatrix.get();
//        double[] distort = cam.distortionCoeffs.get();
//
//        OutputStream os = pa.createOutput(outputDAT);
//
//        byte[] buf = new byte[SIZE_OF_PARAM_SET];
//        ByteBuffer bb = ByteBuffer.wrap(buf);
//        bb.order(ByteOrder.BIG_ENDIAN);
//
//        bb.putInt(w);
//        bb.putInt(h);
//
//        //Projection
//        int k = 0;
//        for (int j = 0; j < 3; j++) {
//            for (int i = 0; i < 3; i++) {
//                bb.putDouble(proj[k++]);
//            }
//            bb.putDouble(0);
//        }
//
//        //distortion
//        for (int i = 0; i < 4; i++) {
//            bb.putDouble(distort[i]);
//        }
//
//        os.write(buf);
//        os.flush();
//        os.close();
//
//        pa.println("Conversion done !");
//        return;
//    }
//
//    static public void convertProjParam(PApplet pa, String inputYAML, String outputDAT, int w, int h) throws Exception {
//
//        ProjectorDevice proj = null;
//
//        ProjectorDevice[] p = ProjectorDevice.read(inputYAML);
//        if (p.length > 0) {
//            proj = p[0];
//        }
//
//        double[] projM = proj.cameraMatrix.get();
//        double[] distort = proj.distortionCoeffs.get();
//
//        OutputStream os = pa.createOutput(outputDAT);
//
//        byte[] buf = new byte[SIZE_OF_PARAM_SET];
//        ByteBuffer bb = ByteBuffer.wrap(buf);
//        bb.order(ByteOrder.BIG_ENDIAN);
//
//        bb.putInt(w);
//        bb.putInt(h);
//
//        //projection
//        int k = 0;
//        for (int j = 0; j < 3; j++) {
//            for (int i = 0; i < 3; i++) {
//                bb.putDouble(projM[k++]);
//            }
//            bb.putDouble(0);
//        }
//
//        //distortion
//        for (int i = 0; i < 4; i++) {
//            bb.putDouble(distort[i]);
//        }
//
//        os.write(buf);
//        os.flush();
//        os.close();
//
//        pa.println("Conversion done !");
//        return;
//    }
}
