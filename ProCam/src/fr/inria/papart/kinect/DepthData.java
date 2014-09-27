/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import java.util.ArrayList;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class DepthData {

    /**
     * 3D points viewed by the kinects
     */
    public Vec3D[] kinectPoints;

    /**
     * Normalized version of the 3D points
     */
    public Vec3D[] projectedPoints;

    /**
     * Mask of valid Points
     */
    public boolean[] validPointsMask;
    public boolean[] validPointsMask3D;

    /**
     * Not sure if used...
     */
    public int[] colorPoints;

    /**
     * List of valid points
     */
    public ArrayList<Integer> validPointsList;
    public ArrayList<Integer> validPointsList3D;

    public DepthData(int size) {
        this(size, true);
    }

    public DepthData(int size, boolean is3D) {
        kinectPoints = new Vec3D[size];
        projectedPoints = new Vec3D[size];
        validPointsMask = new boolean[size];
        colorPoints = new int[size];
        validPointsList = new ArrayList();
        if (is3D) {
            validPointsMask3D = new boolean[size];
            validPointsList3D = new ArrayList();
        }
    }

}
