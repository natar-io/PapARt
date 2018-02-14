/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.depthcam.devices;

import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.PointCloud;
import fr.inria.papart.depthcam.analysis.DepthAnalysisPImageView;
import fr.inria.papart.depthcam.analysis.DepthAnalysisImpl;
import static fr.inria.papart.depthcam.PointCloud.javaToNativeARGB;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import toxi.geom.Vec3D;

/**
 *
 */
public class PointCloudForDepthAnalysis extends PointCloud implements PConstants {

    private DepthAnalysisImpl depthAnalysis;
    private int precision = 1;

    public PointCloudForDepthAnalysis(PApplet parent, DepthAnalysisImpl depthAnalysis, int skip) {
        super(parent, depthAnalysis.getSize() / (skip * skip));
        this.depthAnalysis = depthAnalysis;
        precision = skip;
    }

    public PointCloudForDepthAnalysis(PApplet parent, DepthAnalysisImpl depthAnalysis) {
        this(parent, depthAnalysis, 1);
        this.depthAnalysis = depthAnalysis;
    }

    /**
     * Warning: invalid points are also displayed.
     *
     * @param depthAnalysis
     */
    public void updateWith(DepthAnalysisPImageView depthAnalysis) {
//        boolean[] valid = depthAnalysis.getValidPoints();

        Vec3D[] points = depthAnalysis.getDepthPoints();
        PImage colorsImg = depthAnalysis.getColouredDepthImage();

        nbVertices = 0;
        nbColors = 0;

//        for (int i = 0; i < kinect.getDepthSize(); i++) {
        int k = 0; // k is the 3D point cloud memory.
        for (int y = 0; y < depthAnalysis.getHeight(); y += precision) {
            for (int x = 0; x < depthAnalysis.getWidth(); x += precision) {

                int i = x + y * depthAnalysis.getWidth();

//                if (valid[i]) {
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
//                }

            }
        }

        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, nbVertices * 4);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

    public void updateWithNormalColors(DepthAnalysisImpl depthAnalysis, ArrayList<TrackedDepthPoint> touchs) {
        Vec3D[] points = depthAnalysis.getDepthPoints();

        nbVertices = 0;
        nbColors = 0;
        int k = 0;

        parentApplet.pushStyle();
        //            ID Color
//        parentApplet.colorMode(HSB, 8, 100, 100);

        // Normal Color 
        parentApplet.colorMode(RGB, 1, 1, 1);
        int defaultColor = this.parentApplet.color(1, 1, 1);
        int defaultColor2 = javaToNativeARGB(defaultColor);
        for (TrackedDepthPoint touch : touchs) {

            for (DepthDataElementProjected dde : touch.getDepthDataElements()) {
                Vec3D p = dde.depthPoint;
                verticesJava[k++] = p.x;
                verticesJava[k++] = p.y;
                verticesJava[k++] = -p.z;
                verticesJava[k++] = 1;

                nbVertices++;

                if (dde.normal != null) {
                    int c = this.parentApplet.color(dde.normal.x, dde.normal.y, dde.normal.z);
                    int c2 = javaToNativeARGB(c);
                    colorsJava[nbColors++] = c2;
                } else {
                    colorsJava[nbColors++] = defaultColor2;
                }
            }
        }

        parentApplet.popStyle();
        verticesNative.rewind();
        verticesNative.put(verticesJava, 0, nbVertices * 4);

        colorsNative.rewind();
        colorsNative.put(colorsJava, 0, nbColors);
    }

    public void updateWithIDColors(DepthAnalysisImpl kinect, ArrayList<TrackedDepthPoint> touchs) {
        Vec3D[] points = kinect.getDepthPoints();

        nbVertices = 0;
        nbColors = 0;
        int k = 0;

        parentApplet.pushStyle();
        parentApplet.colorMode(HSB, 8, 100, 100);

        int id = 0;
        for (TrackedDepthPoint touch : touchs) {
            int c = this.parentApplet.color(id % 8, 100, 100);
            int c2 = javaToNativeARGB(c);
            id++;

            for (DepthDataElementProjected dde : touch.getDepthDataElements()) {
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

    public void updateWithCamColors(DepthAnalysisImpl analysis, ArrayList<TrackedDepthPoint> touchs) {
        Vec3D[] points = analysis.getDepthPoints();
        int[] pointColors = analysis.getDepthData().pointColors;
        nbVertices = 0;
        nbColors = 0;
        int k = 0;

        parentApplet.pushStyle();
        parentApplet.colorMode(HSB, 8, 100, 100);

        int id = 0;
        for (TrackedDepthPoint touch : touchs) {

            for (DepthDataElementProjected dde : touch.getDepthDataElements()) {
                int c = pointColors[dde.offset];
                int c2 = javaToNativeARGB(c);

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
