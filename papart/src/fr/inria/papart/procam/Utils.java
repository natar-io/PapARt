/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.calibration.PlaneCreator;
import fr.inria.papart.procam.camera.Camera.PixelFormat;
import fr.inria.papart.procam.display.ARDisplay;
import org.bytedeco.javacpp.opencv_imgproc.*;

import org.bytedeco.javacv.CameraDevice;
import org.bytedeco.javacv.CameraDevice.Settings;
import org.bytedeco.javacv.ProjectorDevice;
import org.bytedeco.javacpp.opencv_imgproc;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import processing.core.*;
import static processing.core.PConstants.ARGB;
import static processing.core.PConstants.RGB;
import processing.opengl.Texture;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class Utils {

    static public final String LibraryName = "PapARt";

    static public String getSketchbookFolder() {

        String sketchbook = java.lang.System.getenv("SKETCHBOOK");
        if (sketchbook != null) {
            return sketchbook;
        }

        URL main = Matrix4x4.class.getResource("Matrix4x4.class");
        String tmp = main.getPath();

        tmp = tmp.substring(0, tmp.indexOf('!'));
        tmp = tmp.replace("file:", "");
//        tmp = tmp.replace("file:/", "");  TODO: OS check ?

        File f = new File(tmp);
        if (!f.exists()) {
            System.err.println("Error in loading the Sketchbook folder.");
        }

        // if the file is within a library/lib folder
        if (f.getParentFile().getAbsolutePath().endsWith(("/lib"))) {
            //             pathToSketchbook/libraries/myLib/library/lib/myLib.jar
            f = f.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
        } else {
            //             pathToSketchbook/libraries/myLib/library/myLib.jar
            f = f.getParentFile().getParentFile().getParentFile().getParentFile();
        }

        return f.getAbsolutePath();
    }

    static public String getLibraryFolder(String libname) {
        return getLibrariesFolder() + "/libraries/" + libname;
    }

    static public String getLibrariesFolder() {
        
        // This is used, as Papart classes are often linked to another folder...
        URL main = Matrix4x4.class.getResource("Matrix4x4.class");
//        URL main = ARDisplay.class.getResource("ARDisplay.class");
        String tmp = main.getPath();

        System.out.println("path  " + tmp);

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
        if (f.getParentFile().getAbsolutePath().endsWith(("/lib"))) {
            //     pathToSketchbook/libraries/myLib/library/lib/myLib.jar
            f = f.getParentFile().getParentFile().getParentFile().getParentFile();
        } else {
            //     pathToSketchbook/libraries/myLib/library/myLib.jar
            f = f.getParentFile().getParentFile().getParentFile();
        }

        return f.getAbsolutePath();
    }

    static public String getPapartFolder() {

        String sketchbook = java.lang.System.getenv("SKETCHBOOK");
        if (sketchbook != null) {
            System.out.println("Found  SKETCHBOOK environment variable.");
            return sketchbook + "/libraries/" + LibraryName;
        }

        return getLibrariesFolder() + "/" + LibraryName;
    }

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

    static public Vec3D toVec(PVector p) {
        return new Vec3D(p.x, p.y, p.z);
    }

    static public boolean colorDist(int c1, int c2, int threshold) {
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 >> 0 & 0xFF;

        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 >> 0 & 0xFF;

        int dr = PApplet.abs(r1 - r2);
        int dg = PApplet.abs(g1 - g2);
        int db = PApplet.abs(b1 - b2);
        return dr < threshold && dg < threshold && db < threshold;
    }

    // TODO:  throws ...
    public static void savePMatrix3D(PApplet pa, PMatrix3D mat, String filename) {
        String[] lines = new String[16];

        lines[0] = Float.toString(mat.m00);
        lines[1] = Float.toString(mat.m01);
        lines[2] = Float.toString(mat.m02);
        lines[3] = Float.toString(mat.m03);
        lines[4] = Float.toString(mat.m10);
        lines[5] = Float.toString(mat.m11);
        lines[6] = Float.toString(mat.m12);
        lines[7] = Float.toString(mat.m13);
        lines[8] = Float.toString(mat.m20);
        lines[9] = Float.toString(mat.m21);
        lines[10] = Float.toString(mat.m22);
        lines[11] = Float.toString(mat.m23);
        lines[12] = Float.toString(mat.m30);
        lines[13] = Float.toString(mat.m31);
        lines[14] = Float.toString(mat.m32);
        lines[15] = Float.toString(mat.m33);

        pa.saveStrings(filename, lines);
    }

    public static void addPMatrix3D(PMatrix3D src, PMatrix3D toAdd) {
        src.m00 += toAdd.m00;
        src.m01 += toAdd.m01;
        src.m02 += toAdd.m02;
        src.m03 += toAdd.m03;

        src.m10 += toAdd.m10;
        src.m11 += toAdd.m11;
        src.m12 += toAdd.m12;
        src.m13 += toAdd.m13;

        src.m20 += toAdd.m20;
        src.m21 += toAdd.m21;
        src.m22 += toAdd.m22;
        src.m23 += toAdd.m23;

        src.m30 += toAdd.m30;
        src.m31 += toAdd.m31;
        src.m32 += toAdd.m32;
        src.m33 += toAdd.m33;

    }

    static public void scaleMat(PMatrix3D mat, float scale) {
        //applscale(scale, 0, 0, 0,  0, scale, 0, 0,  0, 0, scale, 0,  0, 0, 0, 1);
        mat.m00 *= scale;
        mat.m01 *= scale;
        mat.m02 *= scale;
        mat.m03 *= scale;
        mat.m10 *= scale;
        mat.m11 *= scale;
        mat.m12 *= scale;
        mat.m13 *= scale;
        mat.m20 *= scale;
        mat.m21 *= scale;
        mat.m22 *= scale;
        mat.m23 *= scale;
        mat.m30 *= scale;
        mat.m31 *= scale;
        mat.m32 *= scale;
        mat.m33 *= scale;
    }

    public static PMatrix3D loadPMatrix3D(PApplet pa, String filename) throws FileNotFoundException {
        String[] lines = pa.loadStrings(filename);
        if (lines == null) {
            throw new FileNotFoundException(filename);
        }

        PMatrix3D mat = new PMatrix3D(Float.parseFloat(lines[0]), Float.parseFloat(lines[1]), Float.parseFloat(lines[2]), Float.parseFloat(lines[3]),
                Float.parseFloat(lines[4]), Float.parseFloat(lines[5]), Float.parseFloat(lines[6]), Float.parseFloat(lines[7]),
                Float.parseFloat(lines[8]), Float.parseFloat(lines[9]), Float.parseFloat(lines[10]), Float.parseFloat(lines[11]),
                Float.parseFloat(lines[12]), Float.parseFloat(lines[13]), Float.parseFloat(lines[14]), Float.parseFloat(lines[15]));
        return mat;
    }

    static public IplImage createImageFrom(IplImage imgIn, PImage Pout) {
        // TODO: avoid this creation !!
        CvSize outSize = new CvSize();
        outSize.width(Pout.width);
        outSize.height(Pout.height);
        IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                imgIn.nChannels());
//        imgIn.w
        return imgOut;
    }

    static public IplImage createImageFrom(IplImage imgIn) {
        // TODO: avoid this creation !!
        CvSize outSize = new CvSize();
        outSize.width(imgIn.width());
        outSize.height(imgIn.height());
        IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                imgIn.nChannels());
//        imgIn.w
        return imgOut;
    }

    static public IplImage createImageFrom(PImage in) {
        // TODO: avoid this creation !!
        CvSize outSize = new CvSize();
        outSize.width(in.width);
        outSize.height(in.height);

        IplImage imgOut = null;
        if (in.format == RGB) {
            imgOut = cvCreateImage(outSize, // size
                    IPL_DEPTH_8U, // depth
                    3);
        }

        if (in.format == ARGB) {
            imgOut = cvCreateImage(outSize, // size
                    IPL_DEPTH_8U, // depth
                    4);
        }

//        imgIn.w
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
    // TODO: Experimental -> To validate...
//    static public Texture createTextureFrom(PApplet parent, IplImage img) {
//        Texture tex = null;
//
//        // We suppose...  Depth = 3 : BGR and Depth = 4 :  RGBA  (even though it is written ARGB for Processing...)
//        if (img.nChannels() == 3) {
//            tex = new Texture(img.width(), img.height(), PApplet.RGB);
//        }
//        if (img.nChannels() == 4) {
//            tex = new Texture(img.width(), img.height(), PApplet.ARGB);
//        }
//        return tex;
//    }
    static public void updateTexture(IplImage img, Texture tex) {

        System.out.println("Update Texture broken ? May Require CustomTexture...");

//        if (img.nChannels() == 3) {
//            tex.putBuffer(GL.GL_BGR, GL.GL_UNSIGNED_BYTE, img.getIntBuffer());
//        }
//        if (img.nChannels() == 4) {
//            tex.putBuffer(GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, img.getIntBuffer());
//        }
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

    // TODO: clean all this !
    static public void IplImageToPImage(IplImage img, PApplet applet, boolean RGB, PImage ret) {
        IplImageToPImage(img, RGB, ret);
    }

    static public void IplImageToPImage(IplImage img, PixelFormat format, PImage ret) {
        if (format == PixelFormat.RGB) {
            IplImageToPImage(img, true, ret);
        }
        if (format == PixelFormat.BGR) {
            IplImageToPImage(img, false, ret);
        }
    }

    static public void IplImageToPImage(IplImage img, PImage ret) {
        IplImageToPImage(img, true, ret);
    }
    static int conversionCount = 0;

    static public void IplImageToPImage(IplImage img, boolean RGB, PImage ret) {

//        conversionCount++;
//        if (conversionCount % 600 == 0) {
//            System.gc();
//        }
        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
        //= new BufferedImage();

        if (img.nChannels() == 3) {
            ByteBuffer buff = img.getByteBuffer();

            //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
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
            }

        }
        ret.updatePixels();
    }

    static public void byteBufferBRGtoARGB(ByteBuffer bgr, ByteBuffer argb) {
        byte[] tmpArr = new byte[3];

        for (int i = 0; i < bgr.capacity(); i += 3) {
            bgr.get(tmpArr);

            argb.put(tmpArr[2]);
            argb.put(tmpArr[1]);
            argb.put(tmpArr[0]);
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    // TODO
    private static byte[] kinectByteArray = null;

    static public void IplImageToPImageKinect(IplImage img, boolean RGB, PImage ret) {

//        conversionCount++;
//        if (conversionCount % 30 == 0) {
//            System.gc();
//        }
        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
//        BufferedImage bimg = new BufferedImage();

        if (img.nChannels() == 3) {

            System.out.println("3 channels");
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
                ByteBuffer buff = img.getByteBuffer();

                if (Utils.kinectByteArray == null) {
                    kinectByteArray = new byte[2 * img.width() * img.height()];
                }
//                else {
//                    Arrays.fill(kinectByteArray, (byte) 0);
//                }

                buff.get(kinectByteArray);

                for (int i = 0; i < img.width() * img.height() * 2; i += 2) {
                    int d = (kinectByteArray[i] & 0xFF) << 8
                            | (kinectByteArray[i + 1] & 0xFF);

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
    static public void PImageToIplImage(PImage src, IplImage dst) {
        dst.copyFrom((BufferedImage) src.getImage());
    }

    static public void PImageToIplImage2(IplImage img, boolean RGB, PImage ret) {

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

    static public void convertARParam2(PApplet pa, String inputYAML, String outputDAT) throws Exception {

        CameraDevice cam = null;

        CameraDevice[] c = CameraDevice.read(inputYAML);
        if (c.length > 0) {
            cam = c[0];
        }
        Settings camSettings = (org.bytedeco.javacv.CameraDevice.Settings) cam.getSettings();
        int w = camSettings.getImageWidth();
        int h = camSettings.getImageHeight();

        double[] proj = cam.cameraMatrix.get();
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
        sb.append(proj[2]).append(" ").append(proj[5])
                .append(" ").append(proj[0]).
                append(" ").append(proj[4]).append(" ");

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
