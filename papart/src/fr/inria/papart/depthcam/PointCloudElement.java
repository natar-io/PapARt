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
