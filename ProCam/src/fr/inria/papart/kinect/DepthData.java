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

    /**
     * Not sure if used...
     */
    public int[] colorPoints;

    /**
     * List of valid points
     */
    public ArrayList<Integer> validPointsList;

    public DepthData(int size) {
        kinectPoints = new Vec3D[size];
        projectedPoints = new Vec3D[size];
        validPointsMask = new boolean[size];
        colorPoints = new int[size];
        validPointsList = new ArrayList();
    }

}
