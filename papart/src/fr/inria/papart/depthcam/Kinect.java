/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.depthcam;

import fr.inria.papart.depthcam.calibration.PlaneAndProjectionCalibration;
import org.bytedeco.javacpp.opencv_core.IplImage;
import fr.inria.papart.procam.ProjectiveDeviceP;
import java.nio.ByteBuffer;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 * TODO: Kinect - Kinect 4 Processing - Kinect OpenCV - Kinect Multi-Touch With
 * inheritance !
 *
 * @author jeremy
 */
// TODO: 
//   use the Hardware calibration. 
public class Kinect {

// TODO: check theses...
    private float closeThreshold = 300f, farThreshold = 4000f;

    // Configuration 
    public ProjectiveDeviceP kinectCalibIR, kinectCalibRGB;

    // Protected values, important data. 
    protected float[] depthLookUp = null;

    // Raw data from the Kinect Sensor
    public int id;
    protected byte[] depthRaw;
    protected byte[] colorRaw;

    protected int[] connexity;  // TODO: check for Byte instead of int
    protected DepthData depthData;

    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;
    public static final int SIZE = WIDTH * HEIGHT;

    public static PApplet papplet;

    public static final Vec3D INVALID_POINT = new Vec3D();
    public static final int INVALID_COLOR = -1;

    ///// Modes
    public static final int KINECT_MM = 1;
    public static final int KINECT_10BIT = 0;
    private int mode;

    public Kinect(PApplet parent, String calibIR, String calibRGB) {
        this(parent, calibIR, calibRGB, KINECT_10BIT);
    }

    public Kinect(PApplet parent, String calibIR, String calibRGB, int mode) {
        Kinect.papplet = parent;
        this.mode = mode;
        try {
            kinectCalibRGB = ProjectiveDeviceP.loadCameraDevice(calibRGB, 0);
            kinectCalibIR = ProjectiveDeviceP.loadCameraDevice(calibIR, 0);
        } catch (Exception e) {
            System.out.println("Kinect init exception." + e);
        }
        init();
    }

    // TODO: Put this in a file,  or make it easy to tweak...
    public PMatrix3D KinectRGBIRCalibration = new PMatrix3D(1, 0, 0, 15,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1);

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
        return kinectCalibRGB.worldToPixel(new Vec3D(vt2.x, vt2.y, vt2.z));
    }

//    public static 
    protected void init() {
        colorRaw = new byte[kinectCalibIR.getSize() * 3];
        depthRaw = new byte[kinectCalibIR.getSize() * 2];
        depthData = new DepthData(WIDTH, HEIGHT);
        depthData.projectiveDevice = this.kinectCalibIR;

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

    public void setNearFarValue(float near, float far) {
        this.closeThreshold = near;
        this.farThreshold = far;
    }

    public void update(IplImage depth, int skip) {
        depthData.clearDepth();
        updateRawDepth(depth);
        computeDepthAndDo(skip, new DoNothing());
    }

    public void updateMT(IplImage depth, IplImage color, PlaneAndProjectionCalibration calib, int skip2D, int skip3D) {
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

    public void updateMT2D(IplImage depth, IplImage color, PlaneAndProjectionCalibration calib, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clearDepth();
        depthData.clear2D();
        depthData.clearColor();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip, new Select2DPointPlaneProjection());
        doForEachValidPoint(skip, new SetImageData());
    }

    public void updateMT3D(IplImage depth, IplImage color, PlaneAndProjectionCalibration calib, int skip) {
        updateRawDepth(depth);
        updateRawColor(color);
        depthData.clearDepth();
        depthData.clear3D();
        depthData.clearColor();
        depthData.timeStamp = papplet.millis();
        depthData.planeAndProjectionCalibration = calib;
        computeDepthAndDo(skip, new Select3DPointPlaneProjection());
        doForEachValidPoint(skip, new SetImageData());
    }

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < kinectCalibIR.getHeight(); y += precision) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += precision) {

                int offset = y * kinectCalibIR.getWidth() + x;
                float d = getDepth(offset);
                if (d != INVALID_DEPTH) {
                    Vec3D pKinect = kinectCalibIR.pixelToWorld(x, y, d);
                    depthData.kinectPoints[offset] = pKinect;
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void computeDepthAndDo(int precision, DepthPointManiplation manip, InvalidPointManiplation invalidManip) {
        for (int y = 0; y < kinectCalibIR.getHeight(); y += precision) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += precision) {

                int offset = y * kinectCalibIR.getWidth() + x;
                float d = getDepth(offset);
                if (d != INVALID_DEPTH) {
                    Vec3D pKinect = kinectCalibIR.pixelToWorld(x, y, d);
                    depthData.kinectPoints[offset] = pKinect;
                    manip.execute(pKinect, x, y, offset);
                } else {
                    invalidManip.execute(x, y, offset);
                }
            }
        }
    }

    protected void doForEachPoint(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < kinectCalibIR.getHeight(); y += precision) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += precision) {
                int offset = y * kinectCalibIR.getWidth() + x;
                Vec3D pKinect = depthData.kinectPoints[offset];
                if (pKinect != INVALID_POINT) {
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void doForEachValidPoint(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < kinectCalibIR.getHeight(); y += precision) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += precision) {
                int offset = y * kinectCalibIR.getWidth() + x;
                Vec3D pKinect = depthData.kinectPoints[offset];
                if (pKinect != INVALID_POINT && depthData.validPointsMask[offset] == true) {
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void doForEachValid3DPoint(int precision, DepthPointManiplation manip) {
        for (int y = 0; y < kinectCalibIR.getHeight(); y += precision) {
            for (int x = 0; x < kinectCalibIR.getWidth(); x += precision) {
                int offset = y * kinectCalibIR.getWidth() + x;
                Vec3D pKinect = depthData.kinectPoints[offset];
                if (pKinect != INVALID_POINT && depthData.validPointsMask3D[offset] == true) {
                    manip.execute(pKinect, x, y, offset);
                }
            }
        }
    }

    protected void updateRawDepth(IplImage depthImage) {
        ByteBuffer depthBuff = depthImage.getByteBuffer();
        depthBuff.get(depthRaw);
    }

    protected void updateRawColor(IplImage colorImage) {
        ByteBuffer colBuff = colorImage.getByteBuffer();
        colBuff.get(colorRaw);
    }

    static protected final float INVALID_DEPTH = -1;

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

    public interface InvalidPointManiplation {

        public void execute(int x, int y, int offset);
    }

    public interface DepthPointManiplation {

        public void execute(Vec3D p, int x, int y, int offset);
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
        int colorOffset = this.findColorOffset(depthData.kinectPoints[offset]) * 3;
        int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                | (colorRaw[colorOffset + 1] & 0xFF) << 8
                | (colorRaw[colorOffset + 0] & 0xFF);
        return c;
    }

    class ComputeNormal implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {

            depthData.connexity.compute(x, y);
            Vec3D normal = computeNormalImpl(depthData.kinectPoints[offset], offset, x, y);
            depthData.kinectNormals[offset] = normal;
        }
    }

    private Vec3D computeNormalImpl(Vec3D point, int offset, int x, int y) {

        Vec3D[] neighbours = depthData.connexity.getNeighbourList(x, y);
        if (depthData.connexity.connexitySum[offset] < 2) {
            return null;
        }

//        Vec3D normal = computeNormal(point, neighbours[0], neighbours[1]);
        Vec3D normal = new Vec3D();
        // BIG  square around the point. 

        boolean large = tryComputeLarge(neighbours, normal);
        if (!large) {
            boolean medium = tryComputeMediumSquare(neighbours, normal);
            if (!medium) {
                boolean small = tryComputeOneTriangle(neighbours, point, normal);
                if (!small) {
                    return null;
                }
            }
        }
   
//        tryComputeMediumSquare(neighbours, normal);
       // tryComputeOneTriangle(neighbours, point, normal);

        
        normal.normalize();
        return normal;
    }

    private boolean tryComputeLarge(Vec3D[] neighbours, Vec3D normal) {
        if (neighbours[Connexity.TOPLEFT] != null
                && neighbours[Connexity.TOPRIGHT] != null
                && neighbours[Connexity.BOTLEFT] != null
                && neighbours[Connexity.BOTRIGHT] != null) {

            Vec3D n1 = computeNormal(
                    neighbours[Connexity.TOPLEFT],
                    neighbours[Connexity.TOPRIGHT],
                    neighbours[Connexity.BOTLEFT]);

            Vec3D n2 = computeNormal(
                    neighbours[Connexity.BOTLEFT],
                    neighbours[Connexity.TOPRIGHT],
                    neighbours[Connexity.BOTRIGHT]);
            normal.set(n1.add(n2));
            return true;
        }
        return false;
    }

    private boolean tryComputeMediumSquare(Vec3D[] neighbours, Vec3D normal) {
        // small square around the point
        if (neighbours[Connexity.LEFT] != null
                && neighbours[Connexity.TOP] != null
                && neighbours[Connexity.RIGHT] != null
                && neighbours[Connexity.BOT] != null) {

            Vec3D n1 = computeNormal(
                    neighbours[Connexity.LEFT],
                    neighbours[Connexity.TOP],
                    neighbours[Connexity.RIGHT]);

            Vec3D n2 = computeNormal(
                    neighbours[Connexity.LEFT],
                    neighbours[Connexity.RIGHT],
                    neighbours[Connexity.BOT]);
            normal.set(n1.add(n2));
            return true;
        }
        return false;
    }

    private boolean tryComputeOneTriangle(Vec3D[] neighbours, Vec3D point, Vec3D normal) {
        // One triangle only. 
        // Left. 
        if (neighbours[Connexity.LEFT] != null) {
            if (neighbours[Connexity.TOP] != null) {
                normal.set(computeNormal(
                        neighbours[Connexity.LEFT],
                        neighbours[Connexity.TOP],
                        point));
                return true;
            } else {
                if (neighbours[Connexity.BOT] != null) {
                    normal.set(computeNormal(
                            neighbours[Connexity.LEFT],
                            point,
                            neighbours[Connexity.BOT]));
                    return true;
                }
            }
        } else {

            if (neighbours[Connexity.RIGHT] != null) {
                if (neighbours[Connexity.TOP] != null) {
                    normal.set(computeNormal(
                            neighbours[Connexity.TOP],
                            neighbours[Connexity.RIGHT],
                            point));
                    return true;
                } else {
                    if (neighbours[Connexity.BOT] != null) {
                        normal.set(computeNormal(
                                neighbours[Connexity.RIGHT],
                                neighbours[Connexity.BOT],
                                point));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // https://www.opengl.org/wiki/Calculating_a_Surface_Normal
    public Vec3D computeNormal(Vec3D a, Vec3D b, Vec3D c) {

        Vec3D U = b.sub(a);
        Vec3D V = c.sub(a);
        float x = U.y * V.z - U.z * V.y;
        float y = U.z * V.x - U.x * V.z;
        float z = U.x * V.y - U.y * V.x;
        return new Vec3D(x, y, z);
    }

// Toxiclibs
    public Vec3D computeNormal2(Vec3D a, Vec3D b, Vec3D c) {
        Vec3D normal = a.sub(c).crossSelf(a.sub(b)); // .normalize();
        return normal;
    }

    class DoNothing implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, int x, int y, int offset) {

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
     * Return the 3D points of the depth. 3D values in millimeters
     *
     * @return the array of 3D points.
     */
    public Vec3D[] getDepthPoints() {
        return depthData.kinectPoints;
    }

    public DepthData getDepthData() {
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

    static public boolean isInside(PVector v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
