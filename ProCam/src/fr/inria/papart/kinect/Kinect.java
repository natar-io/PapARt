/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Triangle3D;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
// TODO: 
//  Change every Update Function, to use the Hardware calibration. 
public class Kinect {

    public static PApplet parent;
    private float closeThreshold = 300f, farThreshold = 1800f;
    private Vec3D[] kinectPoints;
    private int[] colorPoints;
    private boolean[] validPoints;
    private int[] connexity;  // TODO: check for Byte instead of int
    private boolean[] computedPoints;
    private PImage validPointsPImage;
    private byte[] depthRaw;
    private byte[] colorRaw;
    private byte[] validPointsRaw;
    private IplImage validPointsIpl;
    private int id;
    private int currentSkip = 1;
    private ProjectiveDeviceP kinectCalibIR, kinectCalibRGB;
    static float[] depthLookUp = null;
    // Debug purposes
    public static byte[] connectedComponent;
    public static byte currentCompo = 1;
    public static final int KINECT_WIDTH = 640;
    public static final int KINECT_HEIGHT = 480;
    public static final int KINECT_SIZE = KINECT_WIDTH * KINECT_HEIGHT;
    static PApplet CURRENTPAPPLET = null;
    // Threading for depth and color computation
    private ExecutorService threadPool;
    public int nbThreads = 8;
    public int threadLoad = 40;

    static public void initApplet(PApplet applet) {
        CURRENTPAPPLET = applet;
    }

    static public PApplet getApplet() {
        return CURRENTPAPPLET;
    }

//  Kinect with the standard calibration
    // DEPRECATED  (already...)
    public Kinect(PApplet parent, String calib, int id) {
        Kinect.parent = parent;
        initApplet(parent);

        try {
            kinectCalibRGB = ProjectiveDeviceP.loadCameraDevice(calib, 0);
            kinectCalibIR = ProjectiveDeviceP.loadCameraDevice(calib, 1);
        } catch (Exception e) {
            System.out.println("Use IR kinect calibration only.");
        }

        try {
            kinectCalibIR = ProjectiveDeviceP.loadCameraDevice(calib, 0);
        } catch (Exception e) {
            System.err.println("Error loading IR Kinect Calibration: " + e);
            e.printStackTrace();
        }

        init(id);
    }

    public Kinect(PApplet parent, String calibIR, String calibRGB, int id) {
        Kinect.parent = parent;

        try {
            kinectCalibRGB = ProjectiveDeviceP.loadCameraDevice(calibRGB, 0);
            kinectCalibIR = ProjectiveDeviceP.loadCameraDevice(calibIR, 0);
        } catch (Exception e) {
            System.out.println("Kinect init exception." + e);
        }

        init(id);
    }

    public int getCurrentSkip() {
        return currentSkip;
    }
    private PMatrix3D translateCam = new PMatrix3D(1, 0, 0, 5,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);

//    PMatrix3D translateCam = new PMatrix3D(1, 0, 0, 27,
//            0, 1, 0, -4.5f,
//            0, 0, 1, -2.65f,
//            0, 0, 0, 1);
    public int findColorOffset(Vec3D v) {
        PVector vt = new PVector(v.x, v.y, v.z);
        PVector vt2 = new PVector();
        //  Ideally use a calibration... 
//        kinectCalibRGB.getExtrinsics().mult(vt, vt2);       
        translateCam.mult(vt, vt2);
        return kinectCalibRGB.worldToPixel(new Vec3D(vt2.x, vt2.y, vt2.z));
    }

//    public static 
    private void init(int id) {
        this.id = id;

        connectedComponent = new byte[kinectCalibIR.getSize()];

        // TODO: create them at first use !!
        kinectPoints = new Vec3D[kinectCalibIR.getSize()];
        validPoints = new boolean[kinectCalibIR.getSize()];
        computedPoints = new boolean[kinectCalibIR.getSize()];

        colorRaw = new byte[kinectCalibIR.getSize() * 3];
        depthRaw = new byte[kinectCalibIR.getSize() * 2];

        connexity = new int[kinectCalibIR.getSize()];

        // For Processing output
        colorPoints = new int[kinectCalibIR.getSize()];
        validPointsPImage = parent.createImage(kinectCalibIR.getWidth(), kinectCalibIR.getHeight(), PConstants.RGB);

        // For OpenCV Image output
        validPointsIpl = IplImage.create(new CvSize(kinectCalibIR.getWidth(), kinectCalibIR.getHeight()), opencv_core.IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[kinectCalibIR.getWidth() * kinectCalibIR.getHeight() * 3];

        if (depthLookUp == null) {
            depthLookUp = new float[2048];
            for (int i = 0; i < depthLookUp.length; i++) {
                depthLookUp[i] = rawDepthToMeters(i);
            }
        }

        threadPool = Executors.newFixedThreadPool(8);

    }

    public int getId() {
        return this.id;
    }

    public byte[] getColorBuffer() {
        return this.colorRaw;
    }

    public void undistortRGB(IplImage rgb, IplImage out) {
        kinectCalibRGB.getDevice().undistort(rgb, out);
    }

    // Not Working ! 
    public void undistortIR(IplImage ir, IplImage out) {
        kinectCalibIR.getDevice().undistort(ir, out);
    }

    public void setSkipValue(int skip) {
        this.currentSkip = skip;
    }

    public void setNearFarValue(float near, float far) {
        this.closeThreshold = near;
        this.farThreshold = far;
    }

    public void computeDepth(IplImage depth) {
        // Get the depth as an array 
        ByteBuffer depthBuff = depth.getByteBuffer();
        depthBuff.get(depthRaw);

        float begin = parent.millis();

        // for each point

//            for (int iter = 0; iter < 200; iter++) {
        for (int y = 0; y < kinectCalibIR.getHeight(); y += currentSkip * threadLoad) {
            Runnable worker = new DepthComputation(y);
            threadPool.execute(worker);
        }
//            }

        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        // threadPool.shutdown();
        // Wait until all threads are finish
//            threadPool.
//            threadPool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Temps1 " + (parent.millis() - begin));

//            begin = parent.millis();
//            for (int iter = 0; iter < 200; iter++) {
//                // for each point
//                for (int y = 0; y < kinectCalibIR.getHeight(); y += currentSkip) {
//                    for (int x = 0; x < kinectCalibIR.getWidth(); x += currentSkip) {
//
//                        // current point
//                        int offset = y * kinectCalibIR.getWidth() + x;
//
//                        // raw depth
//                        float d = (depthRaw[offset * 2] & 0xFF) << 8
//                                | (depthRaw[offset * 2 + 1] & 0xFF);
//
//                        // clear out invalid depth
//                        if (d >= 2047) {
//                            validPoints[offset] = false;
//                            break;
//                        }
//                        // compute the depth 
//                        d = 1000 * depthLookUp[(int) d];
//
//                        // TODO: check perfs with this...
//                        boolean goodDepth = (d >= closeThreshold && d < farThreshold);
//                        validPoints[offset] = goodDepth;
//
//    //                validPoints[offset] = true;
//                        kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
//                    }
//                }
//            }
//            System.out.println("Temps2 " + (parent.millis() - begin));

    }

    class DepthComputation implements Runnable {

        private final int startY;

        DepthComputation(int y) {
            this.startY = y;
        }

        @Override
        public void run() {

            for (int y = startY; y <= startY + threadLoad; y++) {
                for (int x = 0; x < kinectCalibIR.getWidth(); x += currentSkip) {

                    // current point
                    int offset = y * kinectCalibIR.getWidth() + x;

                    // raw depth
                    float d = (depthRaw[offset * 2] & 0xFF) << 8
                            | (depthRaw[offset * 2 + 1] & 0xFF);

                    // clear out invalid depth
                    if (d >= 2047) {
                        validPoints[offset] = false;
                        break;
                    }
                    // compute the depth 
                    d = 1000 * depthLookUp[(int) d];

                    // TODO: check perfs with this...
                    boolean goodDepth = (d >= closeThreshold && d < farThreshold);
                    validPoints[offset] = goodDepth;

//                validPoints[offset] = true;
                    kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
                }
            }

        }
    }

    public void update(IplImage depth, int skip) {
        this.currentSkip = skip;
        ByteBuffer depthBuff = depth.getByteBuffer();
        depthBuff.get(depthRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                boolean good = isGoodDepth(d);
                validPoints[offset] = good;

//                if (good) {
                kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
//                }
            }
        }
    }

    public PImage updateP(IplImage depth, IplImage color) {
        return updateP(depth, color, 1);
    }

    public PImage updateP(IplImage depth, IplImage color, int skip) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];
                boolean good = isGoodDepth(d);
                validPoints[offset] = good;

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);
                if (good) {

                    kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
                    colorPoints[offset] = this.findColorOffset(kinectPoints[offset]);
                    int colorOffset = colorPoints[offset] * 3;
                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
                            | (colorRaw[colorOffset + 0] & 0xFF);

                    validPointsPImage.pixels[offset] = c;

//                    int colorOffset = offset * 3;
//                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                            | (colorRaw[colorOffset + 0] & 0xFF);
//
//                    validPointsPImage.pixels[offset] = c;
                }

            }
        }

        Arrays.fill(connexity, 0);
        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {
                int offset = y * kinectCalibIR.getWidth() + x;
                if (validPoints[offset]) {
                    computeConnexity(x, y, skip);
                }
            }
        }

        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public PImage updateP(IplImage depth, IplImage color, KinectScreenCalibration calib) {
        return updateP(depth, color, 1, calib);
    }

    public PImage updateP(IplImage depth, IplImage color, int skip, KinectScreenCalibration calib) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];


                validPoints[offset] = false;
                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    kinectPoints[offset] = p;
//                    colorPoints[offset] = this.findColorOffset(p);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        if (isInside(calib.project(p), 0.f, 1.f, 0.1f)) {

                            kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
                            colorPoints[offset] = this.findColorOffset(kinectPoints[offset]);
                            int colorOffset = colorPoints[offset] * 3;
                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                                    | (colorRaw[colorOffset + 0] & 0xFF);

                            validPointsPImage.pixels[offset] = c;

//                            int colorOffset = offset * 3;
//                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                                    | (colorRaw[colorOffset + 0] & 0xFF);
//                            validPointsPImage.pixels[offset] = c;
                            validPoints[offset] = true;
                        }
                    }
                }

            }

        }

        Arrays.fill(connexity, 0);
        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {
                int offset = y * kinectCalibIR.getWidth() + x;
                if (validPoints[offset]) {
                    computeConnexity(x, y, skip);
                }
            }
        }

        validPointsPImage.updatePixels();
        return validPointsPImage;
    }

    public IplImage updateIpl(IplImage depth, IplImage color) {
        return updateIpl(depth, color, 1);
    }

    public IplImage updateIpl(IplImage depth, IplImage color, int skip) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);
        colorBuff.get(colorRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;
                int outputOffset = offset * 3;


                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;
                validPointsRaw[outputOffset + 2] = 0;
                validPointsRaw[outputOffset + 1] = 0;
                validPointsRaw[outputOffset + 0] = 0;

                if (isGoodDepth(d)) {
                    validPoints[offset] = true;

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    kinectPoints[offset] = p;

                    colorPoints[offset] = this.findColorOffset(p);
                    int colorOffset = colorPoints[offset] * 3;

//                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                            | (colorRaw[colorOffset + 0] & 0xFF);
//                    validPointsPImage.pixels[offset] = c;

                    validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
                    validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
                    validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];

//                    int colorOffset = offset * 3;
//                    validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
//                    validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
//                    validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];
                }

            }
        }

        outputBuff = (ByteBuffer) outputBuff.rewind();
        outputBuff.put(validPointsRaw);

        return validPointsIpl;
    }

    public PImage updateProj(IplImage depth, IplImage color, KinectScreenCalibration calib, Vec3D[] projectedPoints, int skip) {

        this.currentSkip = skip;

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);
                validPoints[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    kinectPoints[offset] = p;
//                    colorPoints[offset] = this.findColorOffset(p);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(project, 0.f, 1.f, 0.0f)) {


                            kinectPoints[offset] = p;
                            validPoints[offset] = true;
                            // Projection
                            projectedPoints[offset] = project;

//                            int colorOffset = colorPoints[offset] * 3;
//                            int colorOffset = offset * 3;
//
//                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
//                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
//                                    | (colorRaw[colorOffset + 0] & 0xFF);
//
//                            validPointsPImage.pixels[offset] = c;


                            kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
                            colorPoints[offset] = this.findColorOffset(kinectPoints[offset]);
                            int colorOffset = colorPoints[offset] * 3;
                            int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                                    | (colorRaw[colorOffset + 1] & 0xFF) << 8
                                    | (colorRaw[colorOffset + 0] & 0xFF);

                            validPointsPImage.pixels[offset] = c;



                        }
                    }
                }
            }
        }

        validPointsPImage.updatePixels();

        return validPointsPImage;
    }

    public void updateMT(IplImage depth, IplImage color, KinectScreenCalibration calib, Vec3D[] projectedPoints) {
        updateMT(depth, color, calib, projectedPoints, 1);
    }

    public ArrayList<Integer> updateMT(IplImage depth, IplImage color, KinectScreenCalibration calib, Vec3D[] projectedPoints, int skip) {

        this.currentSkip = skip;

        ArrayList<Integer> points = new ArrayList<Integer>();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);
        colorBuff.get(colorRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                int colorOutputOffset = offset * 3;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;

                if (isGoodDepth(d)) {
                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    kinectPoints[offset] = p;
//                    colorPoints[offset] = this.findColorOffset(p);


                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(project, 0.f, 1.f, 0.1f)) {


                            kinectPoints[offset] = p;
                            validPoints[offset] = true;
                            points.add(offset);
                            // Projection
                            projectedPoints[offset] = project;

//                            int colorOffset = offset * 3;
//
//                            validPointsRaw[colorOutputOffset + 2] = colorRaw[colorOffset + 2];
//                            validPointsRaw[colorOutputOffset + 1] = colorRaw[colorOffset + 1];
//                            validPointsRaw[colorOutputOffset + 0] = colorRaw[colorOffset + 0];


                            kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
                            colorPoints[offset] = this.findColorOffset(kinectPoints[offset]);
                            int colorOffset = colorPoints[offset] * 3;

                            validPointsRaw[colorOutputOffset + 2] = colorRaw[colorOffset + 2];
                            validPointsRaw[colorOutputOffset + 1] = colorRaw[colorOffset + 1];
                            validPointsRaw[colorOutputOffset + 0] = colorRaw[colorOffset + 0];


                        }
                    }
                }
            }

        }

        return points;
    }

    public ArrayList<Integer> updateMT(IplImage depth, KinectScreenCalibration calib, Vec3D[] projectedPoints, int skip) {

        this.currentSkip = skip;
        ArrayList<Integer> points = new ArrayList<Integer>();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        ByteBuffer depthBuff = depth.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(project, 0.f, 1.f, 0.0f)) {

                            kinectPoints[offset] = p;
                            validPoints[offset] = true;
                            points.add(offset);
                            // Projection
                            projectedPoints[offset] = project;
                        }
                    }
                }
            }

        }

        return points;
    }

    public ArrayList<Integer> updateMT3D(IplImage depth, KinectScreenCalibration calib, Vec3D[] projectedPoints, int skip) {

        this.currentSkip = skip;
        ArrayList<Integer> points = new ArrayList<Integer>();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        ByteBuffer depthBuff = depth.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                if (d >= 2047) {
                    validPoints[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);

                    if (calib.plane().hasGoodOrientation(p)) {

                        Vec3D project = calib.project(p);
//                        if (isInside(project, 0.f, 1.f, 0.8f)) {
                        if (isInside(project, 0.f, 1.f, 0.2f)) {

                            kinectPoints[offset] = p;
                            validPoints[offset] = true;
                            points.add(offset);
                            // Projection
                            projectedPoints[offset] = project;
                        }
                    }
                }
            }

        }

        return points;
    }

    // TO IMPLEMENT
    public ArrayList<Integer> updateOptimized3D(IplImage depth, KinectScreenCalibration calib, Vec3D[] projectedPoints, int skip) {

        this.currentSkip = skip;
        ArrayList<Integer> points = new ArrayList<Integer>();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();
        ByteBuffer depthBuff = depth.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                d = 1000 * depthLookUp[(int) d];

                validPoints[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);

                    if (calib.plane().hasGoodOrientation(p)) {

                        Vec3D project = calib.project(p);
//                        if (isInside(project, 0.f, 1.f, 0.8f)) {
                        if (isInside(project, 0.f, 1.f, 0.2f)) {

                            kinectPoints[offset] = p;
                            validPoints[offset] = true;
                            points.add(offset);
                            // Projection
                            projectedPoints[offset] = project;
                        }
                    }
                }
            }

        }
        return points;
    }
    public static final int TOPLEFT = 1;
    public static final int TOP = 1 << 1;
    public static final int TOPRIGHT = 1 << 2;
    public static final int LEFT = 1 << 3;
    public static final int RIGHT = 1 << 4;
    public static final int BOTLEFT = 1 << 5;
    public static final int BOT = 1 << 6;
    public static final int BOTRIGHT = 1 << 7;

    //    public static enum ConnexityType {
//        TOPLEFT, TOP, TOPRIGHT, LEFT, RIGHT, BOTLEFT, BOT, BOTRIGHT;
//        public int getFlagValue() {
//            return 1 << this.ordinal();
//        }
//    }
    private void computeConnexity(int x, int y, int skip) {

        // Connexity map 
        //  0 1 2 
        //  3 x 4
        //  5 6 7

        final float maxDist = 8.0f;


        int minX = PApplet.constrain(x - skip, 0, Kinect.KINECT_WIDTH - skip);
        int maxX = PApplet.constrain(x + skip, 0, Kinect.KINECT_WIDTH - skip);
        int minY = PApplet.constrain(y - skip, 0, Kinect.KINECT_HEIGHT - skip);
        int maxY = PApplet.constrain(y + skip, 0, Kinect.KINECT_HEIGHT - skip);

        // Todo: Unroll these for loops for optimisation...
        int currentOffset = y * Kinect.KINECT_WIDTH + x;

        int type = 0;

        for (int j = minY, k = 0; j <= maxY; j += skip, k++) {
            for (int i = minX, l = 0; i <= maxX; i += skip, l++) {

                // Do not try the current point
                if (k == 1 && l == 1) {
                    continue;
                }
                int offset = j * Kinect.KINECT_WIDTH + i;
                int connNo = k * 3 + l;

                // See map in the comments. 
                if (connNo >= 5) {
                    connNo--;
                }

//                System.out.println("ConnNo " + connNo);

                if (validPoints[offset] && kinectPoints[currentOffset].distanceTo(kinectPoints[offset]) < maxDist) {
                    type = type | (1 << connNo);
                }

            }
        }

        connexity[currentOffset] = type;
    }

    public PImage getDepthColor() {
        return validPointsPImage;
    }

    public IplImage getDepthColorIpl() {
        return validPointsIpl;
    }

    public boolean[] getValidPoints() {
        return validPoints;
    }

    public int[] getConnexity() {
        return this.connexity;
    }

    public Vec3D[] getDepthPoints() {
        return kinectPoints;
    }

//     public static float rawDepthToMeters(int depthValue) {
//        if (depthValue < 2047) {
//            return (float) (1.0 / ((float) (depthValue) * -0.0030711016f + 3.3309495161f));
//        }
//        return 0.0f;
//    }
//    public static float rawDepthToMeters(int depthValue) {
//        if (depthValue < 2047) {
//            return 0.1236f * (float) Math.tan((double) depthValue / 2842.5 + 1.1863);
//        }
//        return 0.0f;
//    }
    ////////////// WORKS WITH   DEPTH- REGISTERED - MM ////////
    public static float rawDepthToMeters(int depthValue) {
        if (depthValue < 2047) {
            return (float) depthValue / 1000f;
        }
        return 0.0f;
    }

    private boolean isGoodDepth(float rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
