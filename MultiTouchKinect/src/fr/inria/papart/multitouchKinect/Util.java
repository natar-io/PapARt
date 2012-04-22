/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class Util {

    public static float sideError = 0.2f;

    public static boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
