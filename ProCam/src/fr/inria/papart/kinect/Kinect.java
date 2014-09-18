/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import org.bytedeco.javacpp.opencv_core.IplImage;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.nio.ByteBuffer;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * TODO: Kinect - Kinect 4 Processing - Kinect OpenCV - Kinect Multi-Touch With
 * inheritance !
 *
 * @author jeremy
 */
// TODO: 
//  Change every Update Function, to use the Hardware calibration. 
public class Kinect {

// TODO: check theses...
    public float closeThreshold = 300f, farThreshold = 4000f;

    // Configuration 
    public int currentSkip = 1;
    public ProjectiveDeviceP kinectCalibIR, kinectCalibRGB;

    // Protected values, important data. 
    protected float[] depthLookUp = null;

    // Raw data from the Kinect Sensor
    public int id;
    protected byte[] depthRaw;
    protected byte[] colorRaw;

    protected int[] connexity;  // TODO: check for Byte instead of int
    protected DepthData depthData;

    public static final int KINECT_WIDTH = 640;
    public static final int KINECT_HEIGHT = 480;
    public static final int KINECT_SIZE = KINECT_WIDTH * KINECT_HEIGHT;

    public static PApplet papplet;

    ///// Modes
    public static final int KINECT_MM = 1;
    public static final int KINECT_10BIT = 0;
    private int mode;

    @Deprecated
    public Kinect(PApplet parent, String calib, int id, int mode) {
        Kinect.papplet = parent;

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

        this.id = id;
        this.mode = KINECT_10BIT;
        init();
    }

    public Kinect(PApplet parent, String calibIR, String calibRGB, int id) {
        this(parent, calibIR, calibRGB, id, KINECT_10BIT);
    }

    public Kinect(PApplet parent, String calibIR, String calibRGB, int id, int mode) {
        Kinect.papplet = parent;

        try {
            kinectCalibRGB = ProjectiveDeviceP.loadCameraDevice(calibRGB, 0);
            kinectCalibIR = ProjectiveDeviceP.loadCameraDevice(calibIR, 0);
        } catch (Exception e) {
            System.out.println("Kinect init exception." + e);
        }
        init();
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
    protected void init() {

        colorRaw = new byte[kinectCalibIR.getSize() * 3];
        depthRaw = new byte[kinectCalibIR.getSize() * 2];

        depthData = new DepthData(KINECT_SIZE);

        // TODO: check to remove this.
        connexity = new int[kinectCalibIR.getSize()];

        if (depthLookUp == null) {
            depthLookUp = new float[2048];
            if (this.mode == KINECT_10BIT) {
                for (int i = 0; i < depthLookUp.length; i++) {
                    depthLookUp[i] = rawDepthToMeters10Bits(i);
                }
            }
            if (this.mode == KINECT_MM) {
                for (int i = 0; i < depthLookUp.length; i++) {
                    depthLookUp[i] = rawDepthToMetersNoChange(i);
                }
            }
        }

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
    /**
     * DO NOT USE - not working (distorsion estimation fail ?).
     *
     * @param ir
     * @param out
     */
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
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                boolean good = isGoodDepth(d);
                depthData.validPointsMask[offset] = good;

//                if (good) {
                depthData.kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);
//                }
            }
        }
    }

    public void updateMT(IplImage depth, IplImage color, KinectScreenCalibration calib) {
        updateMT(depth, color, calib, 1);
    }

    public void updateMT(IplImage depth, IplImage color, KinectScreenCalibration calib, int skip) {

        this.currentSkip = skip;

        depthData.validPointsList.clear();

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                int colorOutputOffset = offset * 3;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                depthData.validPointsMask[offset] = false;

                if (isGoodDepth(d)) {
                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);
                    depthData.kinectPoints[offset] = p;

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(project, 0.f, 1.f, 0.1f)) {

                            depthData.kinectPoints[offset] = p;
                            depthData.validPointsMask[offset] = true;
                            depthData.validPointsList.add(offset);
                            // Projection
                            depthData.projectedPoints[offset] = project;

//                            int colorOffset = offset * 3;
//
//                            validPointsRaw[colorOutputOffset + 2] = colorRaw[colorOffset + 2];
//                            validPointsRaw[colorOutputOffset + 1] = colorRaw[colorOffset + 1];
//                            validPointsRaw[colorOutputOffset + 0] = colorRaw[colorOffset + 0];
                            depthData.kinectPoints[offset] = kinectCalibIR.pixelToWorld(x, y, d);

                        }
                    }
                }
            }
        }

    }

    public void updateMT(IplImage depth, KinectScreenCalibration calib, int skip) {

        this.currentSkip = skip;
        depthData.validPointsList.clear();
        ByteBuffer depthBuff = depth.getByteBuffer();
        depthBuff.get(depthRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                depthData.validPointsMask[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);

                    if (calib.plane().hasGoodOrientationAndDistance(p)) {

                        Vec3D project = calib.project(p);
                        if (isInside(project, 0.f, 1.f, 0.0f)) {

                            depthData.kinectPoints[offset] = p;
                            depthData.validPointsMask[offset] = true;
                            depthData.validPointsList.add(offset);
                            // Projection
                            depthData.projectedPoints[offset] = project;
                        }
                    }
                }
            }

        }
    }

    public void updateMT3D(IplImage depth, KinectScreenCalibration calib, int skip) {

        this.currentSkip = skip;
             depthData.validPointsList.clear();

        ByteBuffer depthBuff = depth.getByteBuffer();
        depthBuff.get(depthRaw);

        for (int y = 0; y < kinectCalibIR.getHeight(); y += skip) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += skip) {

                int offset = y * kinectCalibIR.getWidth() + x;

                float d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);
                if (d >= 2047) {
                    depthData.validPointsMask[offset] = false;
                    break;
                }
                d = 1000 * depthLookUp[(int) d];

                depthData.validPointsMask[offset] = false;

                if (isGoodDepth(d)) {

                    Vec3D p = kinectCalibIR.pixelToWorld(x, y, d);

                    if (calib.plane().hasGoodOrientation(p)) {

                        Vec3D project = calib.project(p);
//                        if (isInside(project, 0.f, 1.f, 0.8f)) {
                        if (isInside(project, 0.f, 1.f, 0.2f)) {

                            depthData.kinectPoints[offset] = p;
                            depthData.validPointsMask[offset] = true;
                            depthData.validPointsList.add(offset);
                            // Projection
                            depthData.projectedPoints[offset] = project;
                        }
                    }
                }
            }

        }
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
    private float connexityDist = 10;

    @Deprecated
    public void setConnexityDist(float dist) {
        this.connexityDist = dist;
    }

    @Deprecated
    protected void computeConnexity(int x, int y, int skip) {

        // Connexity map 
        //  0 1 2 
        //  3 x 4
        //  5 6 7
        // Todo: Unroll these for loops for optimisation...
        int currentOffset = y * Kinect.KINECT_WIDTH + x;

        int type = 0;

        for (int y1 = y - skip, connNo = 0; y1 <= y + skip; y1 = y1 + skip) {
            for (int x1 = x - skip; x1 <= x + skip; x1 = x1 + skip) {

                // Do not try the current point
                if (x1 == x && y1 == y) {
                    continue;
                }

                // If the point is not in image
                if (y1 >= Kinect.KINECT_HEIGHT || y1 < 0 || x1 < 0 || x1 >= Kinect.KINECT_WIDTH) {
                    connNo++;
                    continue;
                }

                int offset = y1 * Kinect.KINECT_WIDTH + x1;
                if (depthData.validPointsMask[offset] && depthData.kinectPoints[currentOffset].distanceTo(depthData.kinectPoints[offset]) < connexityDist) {
                    type = type | (1 << connNo);
                }

                connNo++;
            }
        }

        connexity[currentOffset] = type;
    }

    public ProjectiveDeviceP getColorProjectiveDevice() {
        return kinectCalibRGB;
    }

    public ProjectiveDeviceP getDepthProjectiveDevice() {
        return kinectCalibIR;
    }

    public boolean[] getValidPoints() {
        return depthData.validPointsMask;
    }

    public int[] getConnexity() {
        return this.connexity;
    }

    /**
     * Return the 3D points of the depth. 
     * 3D values in millimeters
     * @return the array of 3D points. 
     */
    public Vec3D[] getDepthPoints() {
        return depthData.kinectPoints;
    }

    
    public DepthData getDepthData(){
        return this.depthData;
    }
    
    public static float rawDepthToMeters10Bits(int depthValue) {
        if (depthValue < 2047) {
            return (float) (1.0 / ((float) (depthValue) * -0.0030711016f + 3.3309495161f));
        }
        return 0.0f;
    }

    public static float rawDepthToMeters10Bits2(int depthValue) {
        if (depthValue < 2047) {
            return 0.1236f * (float) Math.tan((double) depthValue / 2842.5 + 1.1863);
        }
        return 0.0f;
    }
    ////////////// WORKS WITH   DEPTH- REGISTERED - MM ////////

    public static float rawDepthToMetersNoChange(int depthValue) {
        if (depthValue < 2047) {
            return (float) depthValue / 1000f;
        }
        return 0.0f;
    }

    protected boolean isGoodDepth(float rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
