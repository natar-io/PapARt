/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.depthcam.DepthDataElementKinect;
import fr.inria.papart.depthcam.PointCloud;
import fr.inria.papart.depthcam.analysis.KinectProcessing;
import fr.inria.papart.depthcam.analysis.KinectDepthAnalysis;
import static fr.inria.papart.depthcam.PointCloud.javaToNativeARGB;
import fr.inria.papart.multitouch.TouchPoint;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 */
public class KinectPointCloud extends PointCloud implements PConstants {

    public KinectPointCloud(PApplet parent, KinectDepthAnalysis depthAnalysis, int skip) {
        super(parent, depthAnalysis.getDepthSize() / (skip * skip));
    }

    public KinectPointCloud(PApplet parent, KinectDepthAnalysis depthAnalysis) {
        this(parent, depthAnalysis, 1);
    }

    public void updateWith(KinectProcessing kinect) {
        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getColouredDepthImage();

        nbVertices = 0;
        nbColors = 0;

        int k = 0;
        for (int i = 0; i < kinect.getDepthSize(); i++) {

            if (valid[i]) {
                Vec3D p = points[i];
                int c = colorsImg.pixels[i];

                verticesJava[k++] = p.x;
                verticesJava[k++] = p.y;
                verticesJava[k++] = -p.z;
                verticesJava[k++] = 1;

                int c2 = javaToNativeARGB(c);

                nbVertices++;

                colorsJava[nbColors++] = c2;
                // Think about dividing the color intensity by 255 in the shader...
            }
        }
        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, nbVertices * 4);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

    public void updateWithFakeColors(KinectProcessing kinect, ArrayList<TouchPoint> touchs) {

        boolean[] valid = kinect.getValidPoints();
        Vec3D[] points = kinect.getDepthPoints();
        PImage colorsImg = kinect.getColouredDepthImage();

        nbVertices = 0;
        nbColors = 0;
        int k = 0;

        parentApplet.pushStyle();
        parentApplet.colorMode(HSB, 8, 100, 100);
        int id = 0;
        for (TouchPoint touch : touchs) {

            int c = this.parentApplet.color(id % 8, 100, 100);
            id++;
            int c2 = javaToNativeARGB(c);

            for (DepthDataElementKinect dde : touch.getDepthDataElements()) {
                Vec3D p = dde.depthPoint;
                verticesJava[k++] = p.x;
                verticesJava[k++] = p.y;
                verticesJava[k++] = -p.z;
                verticesJava[k++] = 1;

                nbVertices++;

                colorsJava[nbColors++] = c2;

            }
        }

        parentApplet.popStyle();
        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, nbVertices * 4);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

}
