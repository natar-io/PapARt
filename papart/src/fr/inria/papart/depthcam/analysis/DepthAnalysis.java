/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.depthcam.analysis;

import fr.inria.papart.depthcam.ProjectedDepthData;
import fr.inria.papart.depthcam.PixelOffset;
import tech.lity.rea.utils.WithSize;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * This class compute 3D data from depth images. It handles low level information 
 * from depth cameras such as native buffers. 
 * It also provides methods to extract information about depth such as normals.
 *
 * @author Jeremy Laviole - laviole@rea.lity.tech
 */
public abstract class DepthAnalysis implements WithSize {

    protected int[] connexity;  // TODO: check for Byte instead of int
    protected ProjectedDepthData depthData;

    public static PApplet papplet;
    
    /**
     * Method that transforms depth buffer data to 3D points.
     */
    protected DepthComputation depthComputationMethod;
    /**
     * Link to the depth data
     */
    protected Object depthBuffer;

    /**
     * Invalid point, to compare with.
     */
    public static final Vec3D INVALID_POINT = new Vec3D(0, 0, 0);
    
    /**
     * Invalid color.
     */
    public static final int INVALID_COLOR = -1;

    public static final boolean isValidPoint(Vec3D point) {
        return point.x != 0 && point.y != 0 && point.z != 0;
    }

    // TODO: Find the use of this!
//    public abstract void update(IplImage depth);

    /**
     * @param offset
     * @return the depth (float).
     */
    protected float getDepth(int offset) {
        return depthComputationMethod.findDepth(offset);
    }
    
    /**
     * Method that transforms depth buffer data to 3D points.
     */
    public interface DepthComputation {

        public void updateDepth(IplImage depthImg);

        public float findDepth(int offset);
    }

    public interface InvalidPointManiplation {

        public void execute(PixelOffset px);
    }

    /**
     * Run a method on a depth point.
     */
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


    public class DoNothing implements DepthPointManiplation {

        @Override
        public void execute(Vec3D p, PixelOffset px) {

        }
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

    public ProjectedDepthData getDepthData() {
        return this.depthData;
    }
    
    /**
     * Normal computing method.
     * @param point
     * @param px
     * @return 
     */
    protected Vec3D computeNormalImpl(Vec3D point, PixelOffset px) {

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
//        System.out.println("Normal computed.");
//        tryComputeMediumSquare(neighbours, normal);
        // tryComputeOneTriangle(neighbours, point, normal);
        normal.normalize();
        return normal;
    }

    // Null of INVALID ?
    /**
     * Compute the normal of a point.
     * @param neighbours
     * @param normal
     * @return 
     */
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

    /**
     * 
     * @param neighbours
     * @param normal
     * @return 
     */
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

    /**
     * Smallest unit of normal computation.
     * @param neighbours
     * @param point
     * @param normal
     * @return 
     */
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

  
    /**
     * Compute the normal from 3 points. 
     *  https://www.opengl.org/wiki/Calculating_a_Surface_Normal
     * @param a
     * @param b
     * @param c
     * @return 
     */
    public Vec3D computeNormal(Vec3D a, Vec3D b, Vec3D c) {

        Vec3D U = b.sub(a);
        Vec3D V = c.sub(a);
        float x = U.y * V.z - U.z * V.y;
        float y = U.z * V.x - U.x * V.z;
        float z = U.x * V.y - U.y * V.x;
        return new Vec3D(x, y, z);
    }

    /** Compute a normal, using toxiclib's method.
     * 
     * @param a
     * @param b
     * @param c
     * @return 
     */
    public Vec3D computeNormal2(Vec3D a, Vec3D b, Vec3D c) {
        Vec3D normal = a.sub(c).crossSelf(a.sub(b)); // .normalize();
        return normal;
    }


    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    static public boolean isInside(PVector v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
