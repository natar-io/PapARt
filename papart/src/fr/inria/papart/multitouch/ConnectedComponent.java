/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.multitouch;

import java.util.ArrayList;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class ConnectedComponent extends ArrayList<Integer> {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
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
    
    public float getHeight(Vec3D[] array) {
        float min = Float.MAX_VALUE;
        float max = 0;
        for (int offset : this) {
            float z = array[offset].z;
            if (z < min) {
                min = z;
            }
            if (z > max) {
                max = z;
            }
        }
        return max - min;
    }

}
