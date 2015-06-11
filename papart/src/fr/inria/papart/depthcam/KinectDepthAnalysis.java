/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam;

import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
import static fr.inria.papart.depthcam.DepthAnalysis.INVALID_POINT;
import static fr.inria.papart.depthcam.DepthAnalysis.papplet;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.camera.CameraOpenKinect;
import java.nio.ByteBuffer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class KinectDepthAnalysis extends DepthAnalysis {

    // Protected values, important data. 
    protected float[] depthLookUp = null;

 
    private int mode;

    // Configuration 
    private float closeThreshold = 300f, farThreshold = 4000f;
    protected ProjectiveDeviceP calibIR, calibRGB;
    private final PMatrix3D KinectRGBIRCalibration = new PMatrix3D(1, 0, 0, 15,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);

    // private variables 
    // Raw data from the Kinect Sensor
    protected byte[] depthRaw;
    protected byte[] colorRaw;

    // static values
    protected static final float INVALID_DEPTH = -1;

    public KinectDepthAnalysis(PApplet parent, CameraOpenKinect camera) {
        this.width = Kinect.WIDTH;
        this.height = Kinect.HEIGHT;
        this.size = Kinect.WIDTH * Kinect.HEIGHT;

        DepthAnalysis.papplet = parent;
        this.mode = Kinect.KINECT_MM;

        calibRGB = camera.getProjectiveDevice();
        calibIR = camera.getDepthCamera().getProjectiveDevice();
        initMemory();
    }

    private void initMemory() {
        colorRaw = new byte[size * 3];
        depthRaw = new byte[size * 2];
        depthData = new KinectDepthData(this);
        depthData.projectiveDevice = this.calibIR;

        if (depthLookUp == null) {
            depthLookUp = new float[2048];
            if (this.mode == Kinect.KINECT_10BIT) {
                for (int i = 0; i < depthLookUp.length; i++) {
                    depthLookUp[i] = rawDepthToMeters10Bits(i);
                }
            }
            if (this.mode == Kinect.KINECT_MM) {
                for (int i = 0; i < depthLookUp.length; i++) {
                    depthLookUp[i] = rawDepthToMetersNoChange(i);
                }
            }
        }
    }

    public void update(IplImage depth) {
        update(depth, 1);
    }

    public void update(opencv_core.IplImage depth, int skip) {
        depthData.clearDepth();
        updateRawDepth(depth);
        computeDepthAndDo(skip, new DoNothing());
    }

    public void updateMT(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip2D, int skip3D) {
        updateRawDepth(depth);
        // optimisation no Color. 
        //        updateRawColor(color);
        depthData.clear();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip2D, new DoNothing());
        doForEachPoint(skip2D, new Select2DPointPlaneProjection());
        doForEachPoint(skip3D, new Select3DPointPlaneProjection());

        // Optimisations -- for demos
        //        depthData.connexity.setPrecision(skip3D);
        //        doForEachValid3DPoint(skip3D, new ComputeNormal());
//        depthData.connexity.computeAll();
//        doForEachPoint(1, new ComputeNormal());
//        doForEachPoint(skip2D, new SetImageData());
        // Optimisation no Color
        // doForEachValidPoint(skip2D, new SetImageData());
        // doForEachValid3DPoint(skip3D, new SetImageData());
    }

    public void updateMT2D(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip) {
        updateRawDepth(depth);

        // TechFest Hacks
//        updateRawColor(color);
        depthData.clearDepth();
        depthData.clear2D();
//        depthData.clearColor();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip, new Select2DPointPlaneProjection());

        // TechFest Hacks
//        doForEachValidPoint(skip, new SetImageData());
    }

    public void updateMT3D(opencv_core.IplImage depth, opencv_core.IplImage color, PlaneAndProjectionCalibration calib, int skip) {
        updateRawDepth(depth);
        // TechFest Hack
//        updateRawColor(color);
        depthData.clearDepth();
        depthData.clear3D();
//        depthData.clearColor();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip, new Select3DPointPlaneProjection());
//        doForEachValidPoint(skip, new SetImageData());
    }

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < calibIR.getHeight(); y += precision) {
            for (int x = 0; x < calibIR.getWidth(); x += precision) {

                int offset = y * calibIR.getWidth() + x;
                float d = getDepth(offset);
                if (d != INVALID_DEPTH) {
                    Vec3D pKinect = calibIR.pixelToWorld(x, y, d);
                    depthData.depthPoints[offset] = pKinect;
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip, InvalidPointManiplation invalidManip) {
        for (int y = 0; y < calibIR.getHeight(); y += precision) {
            for (int x = 0; x < calibIR.getWidth(); x += precision) {

                int offset = y * calibIR.getWidth() + x;
                float d = getDepth(offset);
                if (d != INVALID_DEPTH) {
                    Vec3D pKinect = calibIR.pixelToWorld(x, y, d);
                    depthData.depthPoints[offset] = pKinect;
                    manip.execute(pKinect, x, y, offset);
                } else {
                    invalidManip.execute(x, y, offset);
                }
            }
        }
    }

    protected void doForEachPoint(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < calibIR.getHeight(); y += precision) {
            for (int x = 0; x < calibIR.getWidth(); x += precision) {
                int offset = y * calibIR.getWidth() + x;
                Vec3D pKinect = depthData.depthPoints[offset];
                if (pKinect != INVALID_POINT) {
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void doForEachValidPoint(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < calibIR.getHeight(); y += precision) {
            for (int x = 0; x < calibIR.getWidth(); x += precision) {
                int offset = y * calibIR.getWidth() + x;
                Vec3D pKinect = depthData.depthPoints[offset];
                if (pKinect != INVALID_POINT && depthData.validPointsMask[offset] == true) {
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void doForEachValid3DPoint(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < calibIR.getHeight(); y += precision) {
            for (int x = 0; x < calibIR.getWidth(); x += precision) {
                int offset = y * calibIR.getWidth() + x;
                Vec3D pKinect = depthData.depthPoints[offset];
                if (pKinect != INVALID_POINT && depthData.validPointsMask3D[offset] == true) {
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void updateRawDepth(opencv_core.IplImage depthImage) {
        ByteBuffer depthBuff = depthImage.getByteBuffer();
        depthBuff.get(depthRaw);
    }

    protected void updateRawColor(opencv_core.IplImage colorImage) {
        ByteBuffer colBuff = colorImage.getByteBuffer();
        colBuff.get(colorRaw);
    }

    public static int getKinectSize() {
        return Kinect.WIDTH * Kinect.HEIGHT;
    }

    public void setStereoCalibration(String fileName) {
        HomographyCalibration calib = new HomographyCalibration();
        calib.loadFrom(papplet, fileName);
        setStereoCalibration(calib.getHomography());
    }

    public void setStereoCalibration(PMatrix3D matrix) {
        KinectRGBIRCalibration.set(matrix);
    }

    public PMatrix3D getStereoCalibration() {
        return KinectRGBIRCalibration;
    }

    public int findColorOffset(Vec3D v) {
        return findColorOffset(v.x, v.y, v.z);
    }

    public int findColorOffset(PVector v) {
        return findColorOffset(v.x, v.y, v.z);
    }

    public int findColorOffset(float x, float y, float z) {
        PVector vt = new PVector(x, y, z);
        PVector vt2 = new PVector();
        //  Ideally use a calibration... 
//        kinectCalibRGB.getExtrinsics().mult(vt, vt2);       
        KinectRGBIRCalibration.mult(vt, vt2);
        return calibRGB.worldToPixel(new Vec3D(vt2.x, vt2.y, vt2.z));
    }

    /**
     * @param offset
     * @return the depth (float) or INVALID_DEPTH if it failed.
     */
    protected float getDepth(int offset) {
        float d = (depthRaw[offset * 2] & 0xFF) << 8
                | (depthRaw[offset * 2 + 1] & 0xFF);
        if (d >= 2047) {
            return INVALID_DEPTH;
        }
        d = 1000 * depthLookUp[(int) d];
        if (isGoodDepth(d)) {
            return d;
        } else {
            return INVALID_DEPTH;
        }
    }

    public void setNearFarValue(float near, float far) {
        this.closeThreshold = near;
        this.farThreshold = far;
    }

    class Select2DPointPlaneProjection implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p)) {
                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
                depthData.projectedPoints[offset] = projected;
                if (isInside(projected, 0.f, 1.f, 0.0f)) {
                    depthData.validPointsMask[offset] = true;
                    depthData.validPointsList.add(offset);
                }
            }
        }
    }

    class SelectPlaneTouchHand implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {

            boolean overTouch = depthData.planeAndProjectionCalibration.hasGoodOrientation(p);
            boolean underTouch = depthData.planeAndProjectionCalibration.isUnderPlane(p);
            boolean touchSurface = depthData.planeAndProjectionCalibration.hasGoodOrientationAndDistance(p);

            Vec3D projected = depthData.planeAndProjectionCalibration.project(p);

            if (isInside(projected, 0.f, 1.f, 0.0f)) {

                depthData.projectedPoints[offset] = projected;
                depthData.touchAttributes[offset] = new TouchAttributes(touchSurface, underTouch, overTouch);
                depthData.validPointsMask[offset] = touchSurface;

                if (touchSurface) {
                    depthData.validPointsList.add(offset);
                }
            }
        }
    }

    class Select2DPointOverPlane implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            if (depthData.planeCalibration.hasGoodOrientation(p)) {
                depthData.validPointsMask[offset] = true;
                depthData.validPointsList.add(offset);
            }
        }
    }

    class Select2DPointOverPlaneDist implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            if (depthData.planeCalibration.hasGoodOrientationAndDistance(p)) {
                depthData.validPointsMask[offset] = true;
                depthData.validPointsList.add(offset);
            }
        }
    }

    class Select2DPointCalibratedHomography implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {

            PVector projected = new PVector();
            PVector init = new PVector(p.x, p.y, p.z);

            depthData.homographyCalibration.getHomographyInv().mult(init, projected);

            // TODO: Find how to select the points... 
            if (projected.z > 10 && projected.x > 0 && projected.y > 0) {
                depthData.validPointsMask[offset] = true;
                depthData.validPointsList.add(offset);
            }
        }
    }

    class Select3DPointPlaneProjection implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            if (depthData.planeAndProjectionCalibration.hasGoodOrientation(p)) {
                Vec3D projected = depthData.planeAndProjectionCalibration.project(p);
                depthData.projectedPoints[offset] = projected;
                if (isInside(projected, 0.f, 1.f, 0.2f)) {
                    depthData.validPointsMask3D[offset] = true;
                    depthData.validPointsList3D.add(offset);
                }
            }
        }
    }

    class SetImageData implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {
            depthData.pointColors[offset] = getPixelColor(offset);

        }
    }

    protected int getPixelColor(int offset) {
        int colorOffset = this.findColorOffset(depthData.depthPoints[offset]) * 3;
        int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                | (colorRaw[colorOffset + 1] & 0xFF) << 8
                | (colorRaw[colorOffset + 0] & 0xFF);
        return c;
    }

    public void undistortRGB(opencv_core.IplImage rgb, opencv_core.IplImage out) {
        calibRGB.getDevice().undistort(rgb, out);
    }

    // Not Working ! 
    /**
     * DO NOT USE - not working (distorsion estimation fail ?).
     *
     * @param ir
     * @param out
     */
    public void undistortIR(opencv_core.IplImage ir, opencv_core.IplImage out) {
        calibIR.getDevice().undistort(ir, out);
    }

    public ProjectiveDeviceP getColorProjectiveDevice() {
        return calibRGB;
    }

    public ProjectiveDeviceP getDepthProjectiveDevice() {
        return calibIR;
    }

    public byte[] getColorBuffer() {
        return this.colorRaw;
    }

    protected boolean isGoodDepth(float rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
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

}
