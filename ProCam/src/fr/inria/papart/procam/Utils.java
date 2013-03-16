/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import codeanticode.glgraphics.GLTexture;
import com.googlecode.javacv.cpp.opencv_imgproc.*;

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.ProjectorDevice;
import com.googlecode.javacv.cpp.ARToolKitPlus.Tracker;
import com.googlecode.javacv.cpp.opencv_imgproc;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import javax.media.opengl.GL;
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
                imgIn.nChannels());
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
//        imgL.updatePixels();

    }

    static public CvMat createHomography(PVector[] in, PVector[] out) {

        CvMat srcPoints;
        CvMat dstPoints;
        int nbPoints = in.length;
        CvMat homography;

        // TODO: no create map
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
        return homography;
    }

    // TODO: finish this, find another source...
//    http://planning.cs.uiuc.edu/node103.html
    static public PVector getRotations(PMatrix3D mat) {

        PVector r = new PVector();

        r.z = PApplet.atan(mat.m10 / mat.m00);
        r.y = PApplet.atan(-mat.m21 / PApplet.sqrt(mat.m21 * mat.m21 + mat.m22 * mat.m22));
        r.x = PApplet.atan(-mat.m21 / PApplet.sqrt(mat.m21 * mat.m21 + mat.m22 * mat.m22));

        return null;
    }

    // TO USE INSIDE THE DRAW FUNCTION
    static public GLTexture createTextureFrom(PApplet parent, IplImage img) {
        GLTexture tex = null;

        // We suppose...  Depth = 3 : BGR and Depth = 4 :  RGBA  (even though it is written ARGB for Processing...)
        if (img.nChannels() == 3) {
            tex = new GLTexture(parent, img.width(), img.height(), PApplet.RGB);
        }
        if (img.nChannels() == 4) {
            tex = new GLTexture(parent, img.width(), img.height(), PApplet.ARGB);
        }

        return tex;
    }

    static public void updateTexture(IplImage img, GLTexture tex) {
        if (img.nChannels() == 3) {
            tex.putBuffer(GL.GL_BGR, GL.GL_UNSIGNED_BYTE, img.getIntBuffer());
        }
        if (img.nChannels() == 4) {
            tex.putBuffer(GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, img.getIntBuffer());
        }
    }

    static public void remapImage(PVector[] in, PVector[] out, IplImage imgIn, IplImage imgTmp, PImage Pout) {

        CvMat srcPoints;
        CvMat dstPoints;
        int nbPoints = in.length;
        CvMat homography;

        // TODO: no create map
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

    static public void remapImage(CvMat homography, IplImage imgIn, IplImage imgTmp, PImage Pout) {

        opencv_imgproc.cvWarpPerspective(imgIn, imgTmp, homography);
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
//                getFillColor());
        IplImageToPImage(imgTmp, false, Pout);

    }

    static public void remapImageIpl(CvMat homography, IplImage imgIn, IplImage imgOut) {

        opencv_imgproc.cvWarpPerspective(imgIn, imgOut, homography);
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
//                getFillColor());
    }

    static public int getColor(IplImage img, int x, int y, boolean RGB) {

        if (img.nChannels() == 3) {

            ByteBuffer buff = img.getByteBuffer();
            int offset = (img.width() * y + x) * 3;

            if (RGB) {

                return (buff.get(offset) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset + 2) & 0xFF);
            } else {
                return (buff.get(offset + 2) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset) & 0xFF);
            }
        }

        // Operation not supported
        return 0;
    }

    static public void IplImageToPImage(IplImage img, PApplet applet, boolean RGB, PImage ret) {
        IplImageToPImage(img, RGB, ret);
    }
    static public void IplImageToPImage(IplImage img, PImage ret) {
        IplImageToPImage(img, true, ret);
    }
    static int conversionCount = 0;

    static public void IplImageToPImage(IplImage img, boolean RGB, PImage ret) {

        conversionCount++;
        if (conversionCount % 30 == 0) {
            System.gc();
        }

        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
//        BufferedImage bimg = new BufferedImage();

        if (img.nChannels() == 3) {

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
        }

        if (img.nChannels() == 4) {

            ByteBuffer buff = img.getByteBuffer();
            //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
            ret.loadPixels();
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 4;
//            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);

                ret.pixels[i] = (0xFF) << 24
                        | (buff.get(offset) & 0xFF) << 16
                        | (buff.get(offset + 1) & 0xFF) << 8
                        | (buff.get(offset + 2) & 0xFF);

            }
        }


        if (img.nChannels() == 1) {

            // TODO: no more allocations. 
            ByteBuffer buff = img.getByteBuffer();
            byte[] arr = new byte[img.width() * img.height()];
            buff.get(arr);

            for (int i = 0; i < img.width() * img.height(); i++) {

                int d = (arr[i] & 0xFF);

                ret.pixels[i] = d;
//                    ret.pixels[i] =
//                            (buff.get(i) & 0xFF) << 16
//                            | (buff.get(i) & 0xFF) << 8
//                            | (buff.get(i) & 0xFF);
            }


            ////////////// Kinect Depth //////////////
            //                // TODO: no more allocations. 
//                ByteBuffer buff = img.getByteBuffer();
//                byte[] arr = new byte[2 * img.width() * img.height()];
//                buff.get(arr);
//
//                for (int i = 0; i < img.width() * img.height() * 2; i += 2) {
//                    
//                    int d = (arr[i] & 0xFF) << 8 
//                            | (arr[i+1] & 0xFF);
//                    
//                    ret.pixels[i / 2] = d;
////                    ret.pixels[i] =
////                            (buff.get(i) & 0xFF) << 16
////                            | (buff.get(i) & 0xFF) << 8
////                            | (buff.get(i) & 0xFF);
//                }

        }




//        buff = null;
        ret.updatePixels();
    }

    static public void IplImageToPImageKinect(IplImage img, boolean RGB, PImage ret) {

        conversionCount++;
        if (conversionCount % 30 == 0) {
            System.gc();
        }

        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
//        BufferedImage bimg = new BufferedImage();

        if (img.nChannels() == 3) {

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
        } else {
            if (img.nChannels() == 1) {

                ////////////// Kinect Depth //////////////
                //                // TODO: no more allocations. 
                ByteBuffer buff = img.getByteBuffer();
                byte[] arr = new byte[2 * img.width() * img.height()];
                buff.get(arr);

                for (int i = 0; i < img.width() * img.height() * 2; i += 2) {

                    int d = (arr[i] & 0xFF) << 8
                            | (arr[i + 1] & 0xFF);

                    ret.pixels[i / 2] = d;
//                    ret.pixels[i] =
//                            (buff.get(i) & 0xFF) << 16
//                            | (buff.get(i) & 0xFF) << 8
//                            | (buff.get(i) & 0xFF);
                }

            }
        }



//        buff = null;
        ret.updatePixels();
    }

    /**
     *
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
