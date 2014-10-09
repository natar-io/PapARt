/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam;

import processing.core.PVector;

/**
 * 
 * @author jiii
 */
public class PointCloudElement extends DepthPoint{

    public PVector error;

    public static void filterConnexity(PointCloudElement[] PointCloud,
            int w,
            int h,
            float dist, 
            int nbNeighbours) {
        
        Connexity connexity = new Connexity(PointCloud, w, h);
        connexity.setConnexityDist(dist);
        connexity.computeAll();
        byte[] connexSum = connexity.getSum();

        for (int i = 0; i < PointCloud.length; i++) {
            if (PointCloud[i] == null
                    || PointCloud[i].position == null) {
                continue;
            }

            if (connexSum[i] < nbNeighbours) {
                PointCloud[i] = null;
            }
        }
    }

}
