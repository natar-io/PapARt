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
    public static final float ifx_d = 1.0f / 589.03265f;
    public static final float ify_d = 1.0f / 587.64886f;
    public static final float cx_d = 319.35583f;
    public static final float cy_d = 253.5655f;
    public static final float fx_rgb = 525.2362f;
    public static final float fy_rgb = 525.1191f;
    public static final float cx_rgb = 317.96698f;
    public static final float cy_rgb = 244.86328f;
    // For debug purposes. 
    public static float dx = -15.175022f;
    public static float dy = 7.7f;


    static public void init(PApplet applet) {
        pa = applet;


    }

    static public PApplet get() {
        return pa;
    }



    public static Vec3D depthToWorld(int x, int y, float depthValue) {

        Vec3D result = new Vec3D();
        float depth = depthValue;
//        float depth = 1000 * depthLookUp[depthValue]; 
        result.x = (float) ((x - cx_d) * depth * ifx_d);
        result.y = (float) ((y - cy_d) * depth * ify_d);

        // TODO: put it back to -z somehow ?
        result.z = depth;
        return result;
    }

    public static int WorldToColor(int x, int y, Vec3D pt) {


        pt.addSelf(dx, dy, 0);

        // Reprojection 
        float invZ = 1.0f / pt.z();

        int px = PApplet.constrain(PApplet.round((pt.x() * invZ * fx_rgb) + cx_rgb), 0, w - 1);
        int py = PApplet.constrain(PApplet.round((pt.y() * invZ * fy_rgb) + cy_rgb), 0, h - 1);

        return (int) (py * KinectCst.w + px);
    }
}