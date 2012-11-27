/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.kinect;

import codeanticode.glgraphics.GLModel;
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

    public void update(Kinect kinect, PlaneThreshold plane) {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colors = kinect.getDepthColor();

        model.beginUpdateVertices();
        nbToDraw = 0;
        for (int i = 0; i < KinectCst.size; i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                if (plane.orientation(p)) {

                    model.updateVertex(nbToDraw++, p.x, p.y, -p.z);
                } else {
                    valid[i] = false;
                }
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

    public void drawSelf(PGraphicsOpenGL graphics) {
//        System.out.println("Trying to draw " + nbToDraw);
        model.render(0, nbToDraw);
    }
}
