/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import codeanticode.glgraphics.GLModel;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class PointCloudKinect {

    private GLModel model;
    private Kinect kinect;
    private PApplet parent;
    private int nbToDraw = 0;

    public PointCloudKinect(PApplet parent, Kinect kinect) {
        this.kinect = kinect;
        this.parent = parent;

        // TODO: try pointSprites ?
        model = new GLModel(parent, KinectCst.size, GLModel.POINT_SPRITES, GLModel.STREAM);
        model.initColors();
    }

    public void updateColorsProcessing(Kinect kinect) {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();

        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < KinectCst.size; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
            }
        }
        model.endUpdateVertices();

        if (colors != null) {
            colors.loadPixels();
            model.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < KinectCst.size; i++) {
                if (valid[i]) {
                    int c = colors.pixels[i];

                    model.updateColor(k++,
                            (c >> 16) & 0xFF,
                            (c >> 8) & 0xFF,
                            c & 0xFF);
                }
            }
            model.endUpdateColors();
        }
    }

    public void update(Kinect kinect, KinectScreenCalibration calib) {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();

        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < KinectCst.size; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
//                if (plane.orientation(p)) {
//                if (calib.plane().hasGoodOrientationAndDistance(p)) {
//                    if (isInside(calib.project(p), 0.f, 1.f, 0.05f)) {
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
//                    }

//                } else {
//                    valid[i] = false;
//                }
            }
        }
        model.endUpdateVertices();

        if (colors != null) {
            colors.loadPixels();
            model.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < KinectCst.size; i++) {
                if (valid[i]) {
                    int c = colors.pixels[i];

                    model.updateColor(k++,
                            (c >> 16) & 0xFF,
                            (c >> 8) & 0xFF,
                            c & 0xFF);
                }
            }
            model.endUpdateColors();
        }
    }

    public void updateMultiTouch() {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();

        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < KinectCst.size; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
            }
        }
        model.endUpdateVertices();

        if (colors != null) {
            colors.loadPixels();
            model.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < KinectCst.size; i++) {
                if (valid[i]) {
                    int c = colors.pixels[i];

                    model.updateColor(k++,
                            (c >> 16) & 0xFF,
                            (c >> 8) & 0xFF,
                            c & 0xFF);
                }
            }
            model.endUpdateColors();
        }

    }

    public void updateMultiTouch(Vec3D[] projectedPoints) {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();

        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < KinectCst.size; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
            }
        }
        model.endUpdateVertices();

        if (colors != null) {
            colors.loadPixels();
            model.beginUpdateColors();
            int k = 0;
            for (int i = 0; i < KinectCst.size; i++) {
                if (valid[i]) {

                    int r = (int) (projectedPoints[i].x * 255f);
                    int g = (int) (projectedPoints[i].y * 255f);
                    int b = (int) (projectedPoints[i].z * 255f);

                    model.updateColor(k++, r, g, b);
                }
            }
            model.endUpdateColors();
        }

    }

    public void drawSelf(PGraphicsOpenGL graphics) {
//        System.out.println("Trying to draw " + nbToDraw);
        model.render(0, nbToDraw);
    }

    public static boolean isInside(Vec3D v, float min, float max, float sideError) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
