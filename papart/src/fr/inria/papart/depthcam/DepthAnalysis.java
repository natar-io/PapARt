/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam;

import fr.inria.papart.depthcam.devices.KinectDepthData;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.PlaneAndProjectionCalibration;
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
public abstract class DepthAnalysis {

    protected int[] connexity;  // TODO: check for Byte instead of int
    protected KinectDepthData depthData;

    public static PApplet papplet;

    public static final Vec3D INVALID_POINT = new Vec3D(0,0,0);
    public static final int INVALID_COLOR = -1;

    
    public static final boolean isValidPoint(Vec3D point){
        return point.x != 0 && point.y != 0 && point.z != 0;
    }
    
    public abstract void update(IplImage depth);

    public abstract int getDepthSize();
    public abstract int getDepthWidth();
    public abstract int getDepthHeight();
        
    
    public interface InvalidPointManiplation {

        public void execute(PixelOffset px);
    }

    public interface DepthPointManiplation {

        public void execute(Vec3D p, PixelOffset px);
    }

    class ComputeNormal implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

            depthData.connexity.compute(px.x, px.y);
            Vec3D normal = computeNormalImpl(depthData.depthPoints[px.offset], px);
            depthData.normals[px.offset] = normal;
        }
    }

    private Vec3D computeNormalImpl(Vec3D point, PixelOffset px) {

        Vec3D[] neighbours = depthData.connexity.getNeighbourList(px.x, px.y);
        if (depthData.connexity.connexitySum[px.offset] < 2) {
            return null;
        }

        // TODO: no more allocations here ! 
        
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

    public class DoNothing implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

        }
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
        return depthData.depthPoints;
    }

    public KinectDepthData getDepthData() {
        return this.depthData;
    }

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    static public boolean isInside(PVector v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
