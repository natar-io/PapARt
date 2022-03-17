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
// import static org.bytedeco.opencv.global.opencv_calib3d.cvFindHomography;
import org.bytedeco.opencv.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import org.bytedeco.opencv.opencv_core.CvMat;
import org.bytedeco.opencv.opencv_calib3d.*;
import static org.bytedeco.opencv.global.opencv_core.CV_32FC1;
import static org.bytedeco.opencv.global.opencv_core.cvCreateImage;
import static org.bytedeco.opencv.global.opencv_core.cvCreateMat;
import static org.bytedeco.opencv.global.opencv_core.cvSolve;
import static org.bytedeco.opencv.global.opencv_calib3d.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacv.JavaCV;
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

    public static Mat createHomography(List<PVector> in, List<PVector> out) {
        Mat srcPoints;
        Mat dstPoints;
        int nbPoints = in.size();
        // Mat homography;

        // TODO: no create map
        srcPoints = new Mat(2, in.size(), CV_32FC1);
        dstPoints = new Mat(2, in.size(), CV_32FC1);
        // homography = new Mat(3, 3, CV_32FC1);

//         if (in.size() == 4 && out.size() == 4) {
//             double[] src = new double[8];
//             double[] dst = new double[8];
// //            CvMat map = CvMat.create(3, 3);
//             for (int i = 0; i < 4; i++) {
//                 src[i * 2] = in.get(i).x;
//                 src[i * 2 + 1] = in.get(i).y;
//                 dst[i * 2] = out.get(i).x;
//                 dst[i * 2 + 1] = out.get(i).y;
//             }
// //            System.out.println("JavaCV perspective...");
//             JavaCV.getPerspectiveTransform(src, dst, homography);
//             return homography;
//         }

        FloatIndexer srcPointsIdx = srcPoints.createIndexer();
        FloatIndexer dstPointsIdx = dstPoints.createIndexer();
        
        for (int i = 0; i < in.size(); i++) {
 
            srcPointsIdx.put(i, in.get(i).x);
            srcPointsIdx.put(i + nbPoints, in.get(i).y);
            dstPointsIdx.put(i, out.get(i).x);
            dstPointsIdx.put(i + nbPoints, out.get(i).y);
        }
        Mat homography2 = findHomography(srcPoints, dstPoints);
       
//        opencv_imgproc.cvGetPerspectiveTransform(cpd, cpd1, srcPoints) //       It is better to use : GetPerspectiveTransform
        return homography2;
    }

    public static Mat createHomography(PVector[] in, PVector[] out) {
        Mat srcPoints;
        Mat dstPoints;
        int nbPoints = in.length;
        Mat homography;
        // TODO: no create map
        srcPoints = new Mat(2, in.length, CV_32FC1);
        dstPoints = new Mat(2, in.length, CV_32FC1);
        // homography = new Mat(3, 3, CV_32FC1);

        FloatIndexer srcPointsIdx = srcPoints.createIndexer();
        FloatIndexer dstPointsIdx = dstPoints.createIndexer();
        
        for (int i = 0; i < in.length; i++) {
            srcPointsIdx.put(i, in[i].x);
            srcPointsIdx.put(i + nbPoints, in[i].y);
            dstPointsIdx.put(i, out[i].x);
            dstPointsIdx.put(i + nbPoints, out[i].y);
        }
        homography = findHomography(srcPoints, dstPoints); // , homography);
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

    public static void IplImageToPImageKinect(IplImage img, boolean RGB, PImage ret) {
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

    public static void remapImageIpl(Mat homography, IplImage imgIn, IplImage imgOut) {
//        System.out.println("cam: " + imgIn.width() + " " + imgIn.height() + " " + imgIn.depth());
//        System.out.println("extr: " + imgOut.width() + " " + imgOut.height() + " " + imgOut.depth());
       // cvWarpPerspective(imgIn, imgOut, homography);

        warpPerspective(new Mat(imgIn), new Mat(imgOut),
                        homography, new Size(imgOut.width(), imgOut.height()));
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
        //                getFillColor());
    }

    public static IplImage createImageFrom(IplImage imgIn, PImage Pout) {
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

    public static IplImage createImageFrom(IplImage imgIn) {
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

    public static IplImage createImageFrom(PImage in) {
        // TODO: avoid this creation !!
        CvSize outSize = new CvSize();
        outSize.width(in.width);
        outSize.height(in.height);
//        System.out.println("inputImage to create an IPL:" + in.width + " " + in.height + " " + in.format);
        IplImage imgOut = null;
        if (in.format == PConstants.RGB) {
            imgOut = cvCreateImage(outSize, IPL_DEPTH_8U, // depth
                    3);
        }
        if (in.format == PConstants.ALPHA || in.format == PConstants.GRAY) {
            imgOut = cvCreateImage(outSize, IPL_DEPTH_8U, // depth
                    1);
        }
        if (in.format == PConstants.ARGB) {
            imgOut = cvCreateImage(outSize, IPL_DEPTH_8U, // depth
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
    public static void PImageToIplImage2(IplImage img, boolean RGB, PImage ret) {
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

    public static IplImage createNewSizeImageFrom(IplImage imgIn, int width, int height) {
        // TODO: avoid this creation !!
        CvSize outSize = new CvSize();
        outSize.width(width);
        outSize.height(height);
        IplImage imgOut = cvCreateImage(outSize, // size
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

    @Deprecated
    public static void remapImage(PVector[] in, PVector[] out, IplImage imgIn, IplImage imgTmp, PImage Pout) {
        Mat srcPoints;
        Mat dstPoints;
        int nbPoints = in.length;
        Mat homography;
        // TODO: no create map
        srcPoints = new Mat(2, in.length, CV_32FC1);
        dstPoints = new Mat(2, in.length, CV_32FC1);
        // homography = new Mat(3, 3, CV_32FC1);

        FloatIndexer srcPointsIdx = srcPoints.createIndexer(); 
        FloatIndexer dstPointsIdx = dstPoints.createIndexer(); 
        
        for (int i = 0; i < in.length; i++) {
            srcPointsIdx.put(i, in[i].x);
            srcPointsIdx.put(i + nbPoints, in[i].y);
            dstPointsIdx.put(i, out[i].x);
            dstPointsIdx.put(i + nbPoints, out[i].y);
        }

        homography = findHomography(srcPoints, dstPoints);

        //       It is better to use : GetPerspectiveTransform
        // cvWarpPerspective(imgIn, imgTmp, homography);
        warpPerspective(new Mat(imgIn), new Mat(imgTmp),
                        homography, new Size(imgTmp.width(), imgTmp.height()));
        // opencv_imgproc.CV_INTER_LINEAR ); //                opencv_imgproc.CV_WARP_FILL_OUTLIERS);
        //                getFillColor());
        IplImageToPImage(imgTmp, false, Pout);
    }

    public static void remapImage(Mat homography, IplImage imgIn, IplImage imgTmp, PImage Pout) {
        remapImage(homography, imgIn, imgTmp, Pout, false);
    }

    public static void remapImage(Mat homography, IplImage imgIn, IplImage imgTmp, PImage Pout, boolean isRgb) {
        // cvWarpPerspective(imgIn, imgTmp, homography);

        Size outSize = new Size(imgTmp.width(), imgTmp.height());

        warpPerspective(new Mat(imgIn), new Mat(imgTmp), homography, outSize);

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
    public static void updateTexture(IplImage img, Texture tex) {
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
    public static void IplImageToPImage(IplImage img, PApplet applet, boolean RGB, PImage ret) {
        IplImageToPImage(img, RGB, ret);
    }

    public static void IplImageToPImage(IplImage img, Camera.PixelFormat format, PImage ret) {
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

    public static void IplImageToPImage(IplImage img, PImage ret) {
        IplImageToPImage(img, true, ret);
    }

    /**
     * Buggy with 3 channels and some sizes... very strange.
     *
     * @param img
     * @param RGB
     * @param ret
     */
    public static void IplImageToPImage(IplImage img, boolean RGB, PImage ret) {
        //        conversionCount++;
        //        if (conversionCount % 600 == 0) {
        //            System.gc();
        //        }
        assert (img.width() == ret.width);
        assert (img.height() == ret.height);
        ret.loadPixels();
        ByteBuffer buff = img.getByteBuffer();

        int w = img.widthStep();
        int nChannels = img.nChannels();
        byte[] lineArray = new byte[w];
        int k = 0;
        for (int j = 0; j < img.height(); j++) {
            buff.get(lineArray);
            if (img.nChannels() == 1) {
                for (int i = 0; i < img.width(); i++) {
                    byte r = lineArray[i];
                    byte g = r;
                    byte b = r;
                    ret.pixels[k++] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);
                }
            } else {

                if (RGB) {
                    for (int i = 0; i < img.width(); i++) {
                        byte r = lineArray[i * nChannels + 0];
                        byte g = lineArray[i * nChannels + 1];
                        byte b = lineArray[i * nChannels + 2];

                        ret.pixels[k++] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);
                    }
                } else {
                    for (int i = 0; i < img.width(); i++) {
                        byte r = lineArray[i * nChannels + 0];
                        byte g = lineArray[i * nChannels + 1];
                        byte b = lineArray[i * nChannels + 2];

                        ret.pixels[k++] = (b & 255) << 16 | (g & 255) << 8 | (r & 255);
                    }
                }
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
