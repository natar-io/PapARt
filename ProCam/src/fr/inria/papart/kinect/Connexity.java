/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class Connexity {

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
    private final int width, height;
    private float connexityDist = 10;
    public byte[] connexity;  // TODO: check for Byte instead of int
    public byte[] connexitySum;  // TODO: check for Byte instead of int
    private final PointCloudElement[] points;

    public Connexity(PointCloudElement[] points, int w, int h) {
        this.width = w;
        this.height = h;
        this.points = points;
        connexity = new byte[w * h];
        connexitySum = new byte[w * h];
    }

    public void setConnexityDist(float dist) {
        this.connexityDist = dist;
    }

    public byte[] get() {
        return this.connexity;
    }

    public byte[] getSum() {
        return this.connexitySum;
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

        if (points[currentOffset] == null
                || points[currentOffset].point == null) {
            connexity[currentOffset] = 0;
            connexitySum[currentOffset] = 0;
            return;
        }

        byte sum = 0;
        byte type = 0;

//        for (int y1 = y - skip, connNo = 0; y1 <= y + skip; y1 = y1 + skip) {
//            for (int x1 = x - skip; x1 <= x + skip; x1 = x1 + skip) {
        byte connNo = 0;
        for (int y1 = y - 1; y1 <= y + 1; y1 = y1 + 1) {
            for (int x1 = x - 1; x1 <= x + 1; x1 = x1 + 1) {

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
                        && points[offset].point != null
                        && PVector.dist(points[currentOffset].point,
                                points[offset].point) < connexityDist) {
                    type = (byte) (type | 1 << connNo);
                    sum++;
                }

                connNo++;
            }
        }

        connexity[currentOffset] = type;
        connexitySum[currentOffset] = sum;
    }

}
