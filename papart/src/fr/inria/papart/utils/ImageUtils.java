/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.utils;

import fr.inria.papart.procam.PaperTouchScreen;
import fr.inria.papart.procam.camera.Camera;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import static org.bytedeco.javacpp.opencv_calib3d.cvFindHomography;
import org.bytedeco.javacpp.opencv_core;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvCreateMat;
import org.bytedeco.javacpp.opencv_imgproc;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import processing.opengl.Texture;

/**
 *
 * @author Jeremy Laviole
 */
public class ImageUtils {

    public static void byteBufferDepthK1MMtoARGB(ByteBuffer gray, ByteBuffer argb) {
        byte[] depthRaw = new byte[2];
        for (int i = 0; i < argb.capacity(); i += 4) {
            gray.get(depthRaw);
            int d = (depthRaw[0] & 255) << 8 | (depthRaw[1] & 255);
            // min depth: 400
            byte dValue = (byte) ((d - 300.0F) / 3000.0F * 255.0F);
            argb.put(dValue);
            argb.put(dValue);
            argb.put(dValue);
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    public static opencv_core.CvMat createHomography(List<PVector> in, List<PVector> out) {
        opencv_core.CvMat srcPoints;
        opencv_core.CvMat dstPoints;
        int nbPoints = in.size();
        opencv_core.CvMat homography;
        // TODO: no create map
        srcPoints = cvCreateMat(2, in.size(), opencv_core.CV_32FC1);
        dstPoints = cvCreateMat(2, in.size(), opencv_core.CV_32FC1);
        homography = cvCreateMat(3, 3, opencv_core.CV_32FC1);
        for (int i = 0; i < in.size(); i++) {
            srcPoints.put(i, in.get(i).x);
            srcPoints.put(i + nbPoints, in.get(i).y);
            dstPoints.put(i, out.get(i).x);
            dstPoints.put(i + nbPoints, out.get(i).y);
        }
        cvFindHomography(srcPoints, dstPoints, homography);
        //       It is better to use : GetPerspectiveTransform
        return homography;
    }
    public static opencv_core.CvMat createHomography(PVector[] in, PVector[] out) {
        opencv_core.CvMat srcPoints;
        opencv_core.CvMat dstPoints;
        int nbPoints = in.length;
        opencv_core.CvMat homography;
        // TODO: no create map
        srcPoints = cvCreateMat(2, in.length, opencv_core.CV_32FC1);
        dstPoints = cvCreateMat(2, in.length, opencv_core.CV_32FC1);
        homography = cvCreateMat(3, 3, opencv_core.CV_32FC1);
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

    public static void createAnaglyph(PImage imgL, PImage imgR, PImage imgOut) {
        imgL.loadPixels();
        imgR.loadPixels();
        imgOut.loadPixels();
        int[] pL = imgL.pixels;
        int[] pR = imgR.pixels;
        int[] pO = imgOut.pixels;
        for (int i = 0; i < pL.length; i++) {
            pO[i] = (pR[i] >> 16) << 16 | (pL[i] >> 8) & 255 << 8 | pL[i] & 255;
            //            pO[i] = pL[i];
        }
        imgOut.updatePixels();
        //        imgL.updatePixels();
    }

    // TODO wtf
    private static byte[] kinectByteArray = null;

    public static void IplImageToPImageKinect(opencv_core.IplImage img, boolean RGB, PImage ret) {
        //        conversionCount++;
        //        if (conversionCount % 30 == 0) {
        //            System.gc();
        //        }
        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
        //        BufferedImage bimg = new BufferedImage();
        if (img.nChannels() == 3) {
//            System.out.println("3 channels");
            ByteBuffer buff = img.getByteBuffer();
            //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
            ret.loadPixels();
            if (RGB) {
                for (int i = 0; i < img.width() * img.height(); i++) {
                    int offset = i * 3;
                    //            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
                    ret.pixels[i] = (buff.get(offset) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset + 2) & 255);
                }
            } else {
                for (int i = 0; i < img.width() * img.height(); i++) {
                    int offset = i * 3;
                    //            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
                    ret.pixels[i] = (buff.get(offset + 2) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset) & 255);
                }
            }
        } else if (img.nChannels() == 1) {
            ////////////// Kinect Depth //////////////
            ByteBuffer buff = img.getByteBuffer();
            if (kinectByteArray == null) {
                kinectByteArray = new byte[2 * img.width() * img.height()];
            }
            //                else {
            //                    Arrays.fill(kinectByteArray, (byte) 0);
            //                }
            buff.get(kinectByteArray);
            for (int i = 0; i < img.width() * img.height() * 2; i += 2) {
                int d = (kinectByteArray[i] & 255) << 8 | (kinectByteArray[i + 1] & 255);
                ret.pixels[i / 2] = d;
                //                    ret.pixels[i] =
                //                            (buff.get(i) & 0xFF) << 16
                //                            | (buff.get(i) & 0xFF) << 8
                //                            | (buff.get(i) & 0xFF);
            }
        }
        //        buff = null;
        ret.updatePixels();
    }

    public static void byteBufferZ16toARGB(ByteBuffer gray, ByteBuffer argb) {
        byte[] tmpArr = new byte[2];
        gray.rewind();
        for (int i = 0; i < argb.capacity(); i += 4) {
            gray.get(tmpArr);
            argb.put(tmpArr[0]);
            argb.put(tmpArr[1]);
            argb.put((byte) 128);
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    // For OpenNI -- test
    public static void byteBufferShorttoARGB(ByteBuffer gray, ByteBuffer argb) {
        byte[] depthRaw = new byte[2];
        for (int i = 0; i < argb.capacity(); i += 4) {
            gray.get(depthRaw);
            int d = (depthRaw[0] & 255) << 8 | (depthRaw[1] & 255);
            // min depth: 400
            byte dValue = (byte) ((d - 300.0F) / 3000.0F * 255.0F);
            argb.put(dValue);
            argb.put(dValue);
            argb.put(dValue);
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    public static void byteBufferGRAYtoARGB(ByteBuffer gray, ByteBuffer argb) {
        byte[] tmpArr = new byte[1];
        for (int i = 0; i < gray.capacity(); i++) {
            gray.get(tmpArr);
            argb.put(tmpArr[0]);
            argb.put(tmpArr[0]);
            argb.put(tmpArr[0]);
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    public static void remapImageIpl(opencv_core.CvMat homography, opencv_core.IplImage imgIn, opencv_core.IplImage imgOut) {
        opencv_imgproc.cvWarpPerspective(imgIn, imgOut, homography);
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
        //                getFillColor());
    }

    public static opencv_core.IplImage createImageFrom(opencv_core.IplImage imgIn, PImage Pout) {
        // TODO: avoid this creation !!
        opencv_core.CvSize outSize = new opencv_core.CvSize();
        outSize.width(Pout.width);
        outSize.height(Pout.height);
        opencv_core.IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                imgIn.nChannels());
        //        imgIn.w
        return imgOut;
    }

    public static opencv_core.IplImage createImageFrom(opencv_core.IplImage imgIn) {
        // TODO: avoid this creation !!
        opencv_core.CvSize outSize = new opencv_core.CvSize();
        outSize.width(imgIn.width());
        outSize.height(imgIn.height());
        opencv_core.IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                imgIn.nChannels());
        //        imgIn.w
        return imgOut;
    }

    public static opencv_core.IplImage createImageFrom(PImage in) {
        // TODO: avoid this creation !!
        opencv_core.CvSize outSize = new opencv_core.CvSize();
        outSize.width(in.width);
        outSize.height(in.height);
//        System.out.println("inputImage to create an IPL:" + in.width + " " + in.height + " " + in.format);
        opencv_core.IplImage imgOut = null;
        if (in.format == PConstants.RGB) {
            imgOut = cvCreateImage(outSize, opencv_core.IPL_DEPTH_8U, // depth
                    3);
        }
        if (in.format == PConstants.ALPHA || in.format == PConstants.GRAY) {
            imgOut = cvCreateImage(outSize, opencv_core.IPL_DEPTH_8U, // depth
                    1);
        }
        if (in.format == PConstants.ARGB) {
            imgOut = cvCreateImage(outSize, opencv_core.IPL_DEPTH_8U, // depth
                    4);
        }
        //        imgIn.w
        return imgOut;
    }

    public static void byteBufferBRGtoARGB(ByteBuffer bgr, ByteBuffer argb) {
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

    /**
     *
     * Deprecated
     */
    //    static public void PImageToIplImage(PImage src, IplImage dst) {
    //        dst.copyFrom((BufferedImage) src.getImage());
    //    }
    public static void PImageToIplImage2(opencv_core.IplImage img, boolean RGB, PImage ret) {
        ByteBuffer buff = img.getByteBuffer();
        ret.loadPixels();
        if (RGB) {
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 3;
                ret.pixels[i] = (buff.get(offset) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset + 2) & 255);
            }
        } else {
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 3;
                ret.pixels[i] = (buff.get(offset + 2) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset) & 255);
            }
        }
        ret.updatePixels();
    }

    public static opencv_core.IplImage createNewSizeImageFrom(opencv_core.IplImage imgIn, int width, int height) {
        // TODO: avoid this creation !!
        opencv_core.CvSize outSize = new opencv_core.CvSize();
        outSize.width(width);
        outSize.height(height);
        opencv_core.IplImage imgOut = cvCreateImage(outSize, // size
                imgIn.depth(), // depth
                imgIn.nChannels());
        //        imgIn.w
        return imgOut;
    }

    public static void byteBufferRGBtoARGB(ByteBuffer bgr, ByteBuffer argb) {
        byte[] tmpArr = new byte[3];
        for (int i = 0; i < bgr.capacity(); i += 3) {
            bgr.get(tmpArr);
            argb.put(tmpArr[0]);
            argb.put(tmpArr[1]);
            argb.put(tmpArr[2]);
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    public static void remapImage(PVector[] in, PVector[] out, opencv_core.IplImage imgIn, opencv_core.IplImage imgTmp, PImage Pout) {
        opencv_core.CvMat srcPoints;
        opencv_core.CvMat dstPoints;
        int nbPoints = in.length;
        opencv_core.CvMat homography;
        // TODO: no create map
        srcPoints = cvCreateMat(2, in.length, opencv_core.CV_32FC1);
        dstPoints = cvCreateMat(2, in.length, opencv_core.CV_32FC1);
        homography = cvCreateMat(3, 3, opencv_core.CV_32FC1);
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

    public static void remapImage(opencv_core.CvMat homography, opencv_core.IplImage imgIn, opencv_core.IplImage imgTmp, PImage Pout) {
        remapImage(homography, imgIn, imgTmp, Pout, false);
    }

    public static void remapImage(opencv_core.CvMat homography, opencv_core.IplImage imgIn, opencv_core.IplImage imgTmp, PImage Pout, boolean isRgb) {
        opencv_imgproc.cvWarpPerspective(imgIn, imgTmp, homography);
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
        //                getFillColor());
        IplImageToPImage(imgTmp, isRgb, Pout);
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
    public static void updateTexture(opencv_core.IplImage img, Texture tex) {
        System.out.println("Update Texture broken ? May Require CustomTexture...");
        //        if (img.nChannels() == 3) {
        //            tex.putBuffer(GL.GL_BGR, GL.GL_UNSIGNED_BYTE, img.getIntBuffer());
        //        }
        //        if (img.nChannels() == 4) {
        //            tex.putBuffer(GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, img.getIntBuffer());
        //        }
    }

    public static void byteBufferDepthK2toARGB(ByteBuffer gray, ByteBuffer argb) {
        FloatBuffer floatGray = gray.asFloatBuffer();
        float[] tmpArr = new float[1];
        for (int i = 0; i < argb.capacity(); i += 4) {
            floatGray.get(tmpArr);
            // 8 meters
            byte v = (byte) (tmpArr[0] / 8000 * 255);
            //            byte v = (byte) (tmpArr[0] << 8 | tmpArr[1]);
            argb.put((byte) (v));
            argb.put((byte) (v));
            argb.put((byte) (v));
            argb.put((byte) 255);
        }
        argb.rewind();
    }

    // TODO: clean all this !
    public static void IplImageToPImage(opencv_core.IplImage img, PApplet applet, boolean RGB, PImage ret) {
        IplImageToPImage(img, RGB, ret);
    }

    public static void IplImageToPImage(opencv_core.IplImage img, Camera.PixelFormat format, PImage ret) {
        if (format == Camera.PixelFormat.RGB) {
            IplImageToPImage(img, true, ret);
        }
        if (format == Camera.PixelFormat.BGR) {
            IplImageToPImage(img, false, ret);
        }
        if (format == Camera.PixelFormat.GRAY) {
            IplImageToPImage(img, false, ret);
        }
    }

    public static void IplImageToPImage(opencv_core.IplImage img, PImage ret) {
        IplImageToPImage(img, true, ret);
    }

    public static void IplImageToPImage(opencv_core.IplImage img, boolean RGB, PImage ret) {
        //        conversionCount++;
        //        if (conversionCount % 600 == 0) {
        //            System.gc();
        //        }
        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
        ret.loadPixels();
        if (img.nChannels() == 3) {
            ByteBuffer buff = img.getByteBuffer();
            //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
            if (RGB) {
                for (int i = 0; i < img.width() * img.height(); i++) {
                    int offset = i * 3;
                    ret.pixels[i] = (buff.get(offset) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset + 2) & 255);
                }
            } else {
                for (int i = 0; i < img.width() * img.height(); i++) {
                    int offset = i * 3;
                    ret.pixels[i] = (buff.get(offset + 2) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset) & 255);
                }
            }
        }
        if (img.nChannels() == 4) {
            ByteBuffer buff = img.getByteBuffer();
            //  PImage ret = new PImage(img.width(), img.height(), PApplet.RGB);
            for (int i = 0; i < img.width() * img.height(); i++) {
                int offset = i * 4;
                //            ret.pixels[i] = applet.color(buff.get(offset + 0) & 0xff, buff.get(offset + 1) & 0xFF, buff.get(offset + 2) & 0xff);
                ret.pixels[i] = (255) << 24 | (buff.get(offset) & 255) << 16 | (buff.get(offset + 1) & 255) << 8 | (buff.get(offset + 2) & 255);
            }
        }
        // Depth is 8_U 
        if (img.nChannels() == 1) {
            // TODO: no more allocations.
           
            ByteBuffer buff = img.getByteBuffer();
//            ByteBuffer buff =  img.imageData().
            byte[] tmpArr = new byte[1];
//            byte[] tmpArr = new byte[img.width() * img.height()];
//            buff.get(tmpArr);
            
            //            byte[] arr = new byte[img.width() * img.height()];
            //            buff.get(arr);
            for (int i = 0; i < img.width() * img.height(); i++) {
                            buff.get(tmpArr);
                byte d = tmpArr[0];
                //                int d = (arr[i] & 0xFF);
                ret.pixels[i] = (255) << 24 | (d & 255) << 16 | (d & 255) << 8 | (d & 255);
            }
        }
        ret.updatePixels();
    }

    public static void byteBufferGRAY32toARGB(ByteBuffer gray, ByteBuffer argb) {
        FloatBuffer floatGray = gray.asFloatBuffer();
        float[] tmpArr = new float[1];
        for (int i = 0; i < argb.capacity(); i += 4) {
            floatGray.get(tmpArr);
            byte v = (byte) (tmpArr[0] / 65535.0 * 255);
            //            byte v = (byte) (tmpArr[0] << 8 | tmpArr[1]);
            argb.put((byte) (v));
            argb.put((byte) (v));
            argb.put((byte) (v));
            argb.put((byte) 255);
        }
        argb.rewind();
    }

}
