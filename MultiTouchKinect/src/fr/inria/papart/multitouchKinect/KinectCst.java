/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

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
    static PApplet pa = null;
    public static float height3D = 15f;

    static void init(PApplet applet) {
        pa = applet;
    }

    static PApplet get() {
        return pa;
    }
    static float[] depthLookUp = null;
    static Matrix4x4 transform3;

    public static void initKinect() {

        if (depthLookUp == null) {
            depthLookUp = new float[2048];
            for (int i = 0; i < depthLookUp.length; i++) {
                depthLookUp[i] = rawDepthToMeters(i);
            }
        }

        Matrix4x4 rot;
        Matrix4x4 trans;
        rot = new Matrix4x4(
                9.9984628826577793e-01, 1.2635359098409581e-03, -1.7487233004436643e-02, 0,
                -1.4779096108364480e-03, 9.9992385683542895e-01, -1.2251380107679535e-02, 0,
                1.7470421412464927e-02, 1.2275341476520762e-02, 9.9977202419716948e-01, 0,
                0, 0, 0, 1);
        trans = new Matrix4x4(
                1, 0, 0, -1.9985242312092553e-02,
                0, 1, 0, 7.4423738761617583e-04,
                0, 0, 1, 1.0916736334336222e-02,
                0, 0, 0, 1);

        rot = rot.transpose();
        transform3 = rot.multiply(trans);

        System.out.println(transform3);
    }

    public static float rawDepthToMeters(int depthValue) {
        if (depthValue < 2047) {
            return (float) (1.0 / ((double) (depthValue) * -0.0030711016 + 3.3309495161));
        }
        return 0.0f;
    }

    public static Vec3D depthToWorld(int x, int y, int depthValue) {
        final double fx_d = 1.0 / 5.9421434211923247e+02;
        final double fy_d = 1.0 / 5.9104053696870778e+02;
        final double cx_d = 3.3930780975300314e+02;
        final double cy_d = 2.4273913761751615e+02;
        Vec3D result = new Vec3D();
        double depth = rawDepthToMeters(depthValue); //rawDepthToMeters(depthValue);
        result.x = (float) ((x - cx_d) * depth * fx_d);
        result.y = (float) ((y - cy_d) * depth * fy_d);
        result.z = -(float) (depth);
        return result;
    }
//    static Matrix4x4 transform = new Matrix4x4(
//            9.9984628826577793e-01f, -1.4779096108364480e-03f, 1.7470421412464927e-02f, -1.9985242312092553e-02f,
//            1.2635359098409581e-03f, 9.9992385683542895e-01f, 1.2275341476520762e-02f, 7.4423738761617583e-04f,
//            -1.7487233004436643e-02f, -1.2251380107679535e-02f, 9.9977202419716948e-01f, 1.0916736334336222e-02f,
//            0, 0, 0, 1);
    static Matrix4x4 transform = new Matrix4x4(
            9.9984628826577793e-01f, -1.4779096108364480e-03f, 1.7470421412464927e-02f, 0,
            1.2635359098409581e-03f, 9.9992385683542895e-01f, 1.2275341476520762e-02f, 0,
            -1.7487233004436643e-02f, -1.2251380107679535e-02f, 9.9977202419716948e-01f, 0,
            0, 0, 0, 1);
        static Matrix4x4 transform2 = new Matrix4x4(
            9.9984628826577793e-01f, -1.4779096108364480e-03f, 1.7470421412464927e-02f, -1.9985242312092553e-02f,
            1.2635359098409581e-03f, 9.9992385683542895e-01f, 1.2275341476520762e-02f, 7.4423738761617583e-04f,
            -1.7487233004436643e-02f, -1.2251380107679535e-02f, 9.9977202419716948e-01f, 1.0916736334336222e-02f,
            0, 0, 0, 1);
//    static PMatrix3D transform2 = new PMatrix3D(
//            9.9984628826577793e-01f, -1.4779096108364480e-03f, 1.7470421412464927e-02f, -1.9985242312092553e-02f,
//            1.2635359098409581e-03f, 9.9992385683542895e-01f, 1.2275341476520762e-02f, 7.4423738761617583e-04f,
//            -1.7487233004436643e-02f, -1.2251380107679535e-02f, 9.9977202419716948e-01f, 1.0916736334336222e-02f,
//            0, 0, 0, 1);

    public static int WorldToColor(Vec3D pt) {
        final double fx_rgb = 5.2921508098293293e+02;
        final double fy_rgb = 5.2556393630057437e+02;
        final double cx_rgb = 3.2894272028759258e+02;
        final double cy_rgb = 2.6748068171871557e+02;

//        PVector v = new PVector(pt.x, pt.y, -pt.z);
//        PVector v2 = transform2.mult(v, new PVector());
//        
//        Vec3D pt2 = transform.applyTo(ptOrig);
        Vec3D pt2 = transform3.applyTo(pt);

//        pt2 = pt2.add(1.9985242312092553e-02f, -7.4423738761617583e-04f, -1.0916736334336222e-02f);

//        System.out.println(pt2 + " " + pt3);

        double invZ = 1.0f / pt2.z();
        int px = PApplet.constrain(PApplet.round((float) ((pt2.x() * fx_rgb * invZ) + cx_rgb)), 0, 639);
        int py = PApplet.constrain(PApplet.round((float) ((pt2.y() * fy_rgb * invZ) + cy_rgb)), 0, 479);

        
        double invZorig = 1.0f / pt.z();
        int pxOrig = PApplet.constrain(PApplet.round((float) ((pt.x() * fx_rgb * invZ) + cx_rgb)) - 10, 0, 639);
        int pyOrig = PApplet.constrain(PApplet.round((float) ((pt.y() * fy_rgb * invZ) + cy_rgb)) - 28, 0, 479);

//        System.out.println((px - pxOrig) + " " + (py - pyOrig));
//        
        px = pxOrig ;
        py = pyOrig ;

        return py * KinectCst.w + px;
    }
}
