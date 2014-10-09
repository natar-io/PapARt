/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import java.util.ArrayList;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ConnectedComponent extends ArrayList<Integer> {

    public Vec3D getMean(Vec3D[] array) {
        Vec3D mean = new Vec3D(0, 0, 0);
        for (int offset : this) {
            mean.addSelf(array[offset]);
        }
        mean.scaleSelf(1.0f / this.size());
        return mean;
    }

    public float getMinZ(Vec3D[] array) {
        float min = Float.MAX_VALUE;
        for (int offset : this) {
            float z = array[offset].z;
            if (z < min) {
                min = z;
            }
        }
        return min;
    }

}
