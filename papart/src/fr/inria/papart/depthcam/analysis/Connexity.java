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

import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import java.util.Arrays;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class Connexity {

    // Byte description. 
    public static final int TOPLEFT = 0;
    public static final int TOP = 1;
    public static final int TOPRIGHT = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int BOTLEFT = 5;
    public static final int BOT = 6;
    public static final int BOTRIGHT = 7;

//    public static final int TOPLEFT = 1;
//    public static final int TOP = 1 << 1;
//    public static final int TOPRIGHT = 1 << 2;
//    public static final int LEFT = 1 << 3;
//    public static final int RIGHT = 1 << 4;
//    public static final int BOTLEFT = 1 << 5;
//    public static final int BOT = 1 << 6;
//    public static final int BOTRIGHT = 1 << 7;
    private final int width, height;
    private float connexityDist = 10;
    public float DEFAULT_CONNEXITY_DIST = 10;
    public byte[] connexity;
    public byte[] connexitySum;
    private Vec3D[] points;
    private int precision = 1;

    public Connexity(Vec3D[] points, int w, int h) {
        this.width = w;
        this.height = h;
        this.points = points;
        connexity = new byte[w * h];
        connexitySum = new byte[w * h];
    }

    public void setPoints(Vec3D[] points) {
        this.points = points;
    }

    public void reset() {
        Arrays.fill(connexity, (byte) 0);
        Arrays.fill(connexitySum, (byte) 0);
    }

    public byte[] get() {
        return this.connexity;
    }

    public byte[] getSum() {
        return this.connexitySum;
    }

    public Vec3D[] getNeighbourList(int x, int y) {
        int offset = y * width + x;
        int nbNeighbours = connexitySum[offset];
        if (nbNeighbours == 0) {
            return new Vec3D[0];
        }

        byte c = connexity[offset];
//        Vec3D[] output = new Vec3D[nbNeighbours];
        Vec3D[] output = new Vec3D[8];

        int k = 0;
        byte connNo = 0;
//        for (int y1 = y - 1; y1 <= y + 1; y1 = y1 + 1) {
//            for (int x1 = x - 1; x1 <= x + 1; x1 = x1 + 1) {
        for (int y1 = y - precision; y1 <= y + precision; y1 = y1 + precision) {
            for (int x1 = x - precision; x1 <= x + precision; x1 = x1 + precision) {
                if (x1 == x && y1 == y) {
                    continue;
                }

                int direction = 1 << connNo;
                boolean valid = (c & direction) > 0;

                if (valid) {
                    int neighbourOffset = y1 * width + x1;
                    output[connNo] = points[neighbourOffset];

                }
                connNo++;
            }
        }
        // TEST
//        for (int i = 0; i < nbNeighbours; i++) {
//            assert (output[i] != null);
//        }
        return output;
    }
    
    public int[] getNeighbourColorList(int x, int y, int[] colors) {
        int offset = y * width + x;
        int nbNeighbours = connexitySum[offset];
        if (nbNeighbours == 0) {
            return new int[0];
        }

        byte c = connexity[offset];
        int[] output = new int[8];

        int k = 0;
        byte connNo = 0;
//        for (int y1 = y - 1; y1 <= y + 1; y1 = y1 + 1) {
//            for (int x1 = x - 1; x1 <= x + 1; x1 = x1 + 1) {
        for (int y1 = y - precision; y1 <= y + precision; y1 = y1 + precision) {
            for (int x1 = x - precision; x1 <= x + precision; x1 = x1 + precision) {
                if (x1 == x && y1 == y) {
                    continue;
                }

                int direction = 1 << connNo;
                boolean valid = (c & direction) > 0;

                if (valid) {
                    int neighbourOffset = y1 * width + x1;
                    output[connNo] = colors[neighbourOffset];

                }
                connNo++;
            }
        }
        // TEST
//        for (int i = 0; i < nbNeighbours; i++) {
//            assert (output[i] != null);
//        }
        return output;
    }

    void computeAll() {
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                compute(x, y);
            }
        }
    }

    void compute(int x, int y) {
        // Connexity map 
        //  0 1 2 
        //  3 x 4
        //  5 6 7
        // Todo: Unroll these for loops for optimisation...
        int currentOffset = y * width + x;

        if (points[currentOffset] == null){
            // We do not care about validity for Normal computing ?!
//                ||  !DepthAnalysis.isValidPoint(points[currentOffset])) {
            connexity[currentOffset] = 0;
            connexitySum[currentOffset] = 0;
            return;
        }

        byte sum = 0;
        byte type = 0;

        byte connNo = 0;
        for (int y1 = y - precision; y1 <= y + precision; y1 = y1 + precision) {
            for (int x1 = x - precision; x1 <= x + precision; x1 = x1 + precision) {

                // Do not try the current point
                if (x1 == x && y1 == y) {
                    continue;
                }

                // If the point is not in image
                if (y1 >= height || y1 < 0 || x1 < 0 || x1 >= width) {
                    connNo++;
                    continue;
                }

                int offset = y1 * width + x1;
//                if (kinectPoints[currentOffset].distanceTo(kinectPoints[offset]) < connexityDist) {
//                    type = type | (1 << connNo);
//                }
                if (points[offset] != null
                        && points[offset] != DepthAnalysis.INVALID_POINT
                        && points[currentOffset].distanceTo(points[offset]) < connexityDist) {
                    type = (byte) (type | 1 << connNo);
                    sum++;
                }

                connNo++;
            }
        }

        connexity[currentOffset] = type;
        connexitySum[currentOffset] = sum;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
        this.connexityDist = DEFAULT_CONNEXITY_DIST * precision;
    }

    public void setConnexityDist(float dist) {
        this.connexityDist = dist;
    }

}
