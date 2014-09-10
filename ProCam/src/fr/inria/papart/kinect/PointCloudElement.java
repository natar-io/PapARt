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
public class PointCloudElement {

    public PVector point;
    public PVector error;
    public int ptColor;
    public boolean hasColor = false;

    public static void filterConnexity(PointCloudElement[] pce,
            int w,
            int h,
            float dist, 
            int nbNeighbours) {
        
        Connexity connexity = new Connexity(pce, w, h);
        connexity.setConnexityDist(dist);
        connexity.computeAll();
        byte[] connexSum = connexity.getSum();

        for (int i = 0; i < pce.length; i++) {
            if (pce[i] == null
                    || pce[i].point == null) {
                continue;
            }

            if (connexSum[i] < nbNeighbours) {
                pce[i] = null;
            }
        }
    }

}
