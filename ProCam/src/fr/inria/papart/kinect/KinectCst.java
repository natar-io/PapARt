/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class KinectCst {

    public static final int w = 640;
    public static final int h = 480;
    public static final int size = w * h;
    static PApplet pa = null;

    static public void init(PApplet applet) {
        pa = applet;
    }

    static public PApplet get() {
        return pa;
    }

}