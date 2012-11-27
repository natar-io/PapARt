/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class Kinect {

    public static PApplet parent;
    public float closeThreshold = 300f, farThreshold = 800f;

    private Vec3D[] kinectPoints;
    private int[] colorPoints;
    private boolean[] validPoints;
    private PImage validPointsPImage;
    private byte[] depthRaw;
    private byte[] colorRaw;
    private byte[] validPointsRaw;
    private IplImage validPointsIpl;
    private int id;

//  Kinect with the standard calibration
    public Kinect(PApplet parent, int id) {
        this.parent = parent;
        KinectCst.init(parent);
        init(id);
    }

    // Kinect with advanced calibration 
    // Not ready yet
//    public Kinect(PApplet parent, int id, String calibrationFile) {
//        init(id);
//    }

    private void init(int id) {
        this.id = id;
        
        kinectPoints = new Vec3D[KinectCst.w * KinectCst.h];
        validPoints = new boolean[KinectCst.w * KinectCst.h];

        colorRaw = new byte[KinectCst.w * KinectCst.h * 3];
        depthRaw = new byte[KinectCst.w * KinectCst.h * 2];

        
        // For Processing output
        colorPoints = new int[KinectCst.w * KinectCst.h];
        validPointsPImage = parent.createImage(KinectCst.w, KinectCst.h, PConstants.RGB);

        // For OpenCV Image output
        validPointsIpl = IplImage.create(new CvSize(KinectCst.w, KinectCst.h), opencv_core.IPL_DEPTH_8U, 3);
        validPointsRaw = new byte[KinectCst.w * KinectCst.h * 3];
    }
    
    public int getId(){
        return this.id;
    }

    // TODO: use Calibration files etc...
    public PImage updateP(IplImage depth, IplImage color) {

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        depthBuff.get(depthRaw);
        colorBuff.get(colorRaw);

        validPointsPImage.loadPixels();

        int off = 0;
        for (int y = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {

                int offset = off++;

                int d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                boolean good = isGoodDepth(d);
                validPoints[offset] = good;

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);

                if (good) {
                    kinectPoints[offset] = KinectCst.depthToWorld(x, y, d);
                    colorPoints[offset] = KinectCst.WorldToColor(x, y, kinectPoints[offset]);

                    int colorOffset = colorPoints[offset] * 3;
                    int c = (colorRaw[colorOffset + 2] & 0xFF) << 16
                            | (colorRaw[colorOffset + 1] & 0xFF) << 8
                            | (colorRaw[colorOffset + 0] & 0xFF);

                    validPointsPImage.pixels[offset] = c;
                }

            }
        }

        validPointsPImage.updatePixels();

        return validPointsPImage;
    }

    public IplImage updateIpl(IplImage depth, IplImage color) {

        ByteBuffer depthBuff = depth.getByteBuffer();
        ByteBuffer colorBuff = color.getByteBuffer();

        ByteBuffer outputBuff = validPointsIpl.getByteBuffer();

        depthBuff.get(depthRaw);
        outputBuff.get(validPointsRaw);
        colorBuff.get(colorRaw);

        int off = 0;
        for (int y = 0; y < KinectCst.h; y++) {
            for (int x = 0; x < KinectCst.w; x++) {

                int offset = off++;
                int outputOffset = offset * 3;

                int d = (depthRaw[offset * 2] & 0xFF) << 8
                        | (depthRaw[offset * 2 + 1] & 0xFF);

                boolean good = isGoodDepth(d);
                validPoints[offset] = good;

                validPointsPImage.pixels[offset] = parent.color(0, 0, 255);

                if (good) {
                    kinectPoints[offset] = KinectCst.depthToWorld(x, y, d);
                    colorPoints[offset] = KinectCst.WorldToColor(x, y, kinectPoints[offset]);

                    int colorOffset = colorPoints[offset] * 3;


                    validPointsRaw[outputOffset + 2] = colorRaw[colorOffset + 2];
                    validPointsRaw[outputOffset + 1] = colorRaw[colorOffset + 1];
                    validPointsRaw[outputOffset + 0] = colorRaw[colorOffset + 0];

                } else {
                    validPointsRaw[outputOffset + 2] = 0;
                    validPointsRaw[outputOffset + 1] = 0;
                    validPointsRaw[outputOffset + 0] = 0;

                }
            }
        }

        outputBuff = (ByteBuffer) outputBuff.rewind();
        outputBuff.put(validPointsRaw);

        return validPointsIpl;
    }

    public PImage getDepthColor() {
        return validPointsPImage;
    }

    public boolean[] getValidPoints() {
        return validPoints;
    }

    public Vec3D[] getDepthPoints() {
        return kinectPoints;
    }

    private boolean isGoodDepth(int rawDepth) {
        return (rawDepth >= closeThreshold && rawDepth < farThreshold);
    }
}
