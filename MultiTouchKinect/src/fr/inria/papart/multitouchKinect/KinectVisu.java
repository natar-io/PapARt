/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import java.util.ArrayList;
import processing.core.PApplet;
import processing.xml.XMLElement;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class KinectVisu {

    public int minDepthKinectVisu = 400;
    public int maxDepthKinectVisu = 800;
    public int minXKinectVisu = 0;
    public int maxXKinectVisu = KinectCst.w;
    public int minYKinectVisu = 0;
    public int maxYKinectVisu = KinectCst.h;
    public int kinectVisuSkip = 4;
    public boolean active = true;
    float depthDiff = 5;
    boolean isDeletingBackground = false;
    boolean isBackgroundDeleted = false;
    boolean[] backgroundValidPoints = new boolean[KinectCst.w * KinectCst.h];
    boolean[] validPoints = new boolean[KinectCst.w * KinectCst.h];

    public KinectVisu(int minD, int maxD,
            int minX, int maxX,
            int minY, int maxY,
            int skip) {
        minDepthKinectVisu = minD;
        maxDepthKinectVisu = maxD;
        minXKinectVisu = minX;
        maxXKinectVisu = maxX;
        minYKinectVisu = minY;
        maxYKinectVisu = maxY;
        kinectVisuSkip = skip;
    }
    String PlaneParametersfilename = "../data/PlaneParameters.txt";

    public KinectVisu(String PlaneParametersfilename) {

        String[] lines = KinectCst.pa.loadStrings(PlaneParametersfilename);
        if (lines != null && lines.length != 0) {

            minDepthKinectVisu = Integer.parseInt(lines[0]);
            maxDepthKinectVisu = Integer.parseInt(lines[1]);
            minXKinectVisu = Integer.parseInt(lines[2]);
            maxXKinectVisu = Integer.parseInt(lines[3]);
            minYKinectVisu = Integer.parseInt(lines[4]);
            maxYKinectVisu = Integer.parseInt(lines[5]);
            kinectVisuSkip = 1;
        } else {
            throw new RuntimeException("The plane parameters file is unreachable.");
        }
    }

    public KinectVisu(XMLElement parametersXML) {
        // TO finish ?? 
//        XMLElement minDepth = parametersXML.getChild("MinDepth");
//        XMLElement maxDepth = parametersXML.getChild("MaxDepth");
//
//        minDepthKinectVisu = Integer.parseInt(minDepth.getContent());
//        maxDepthKinectVisu = Integer.parseInt(maxDepth.getContent());
    }

    // TODO: replace Img3DVec by offset only ?
    public ArrayList<Integer> view(boolean[] validPoints,
            Vec3D[] points,
            Vec3D[] projPoints,
            int[] depth,
            PlaneSelection planeSelection,
            float distanceZ,
            boolean withBackground,
            Matrix4x4 transform) {

        if (!planeSelection.isValid()) {
            return null;
        }
        //gfx.plane(plane.plane, 0.30);

        if (active) {
            for (int y = minYKinectVisu, i = 0; y < maxYKinectVisu; y += kinectVisuSkip) {
                for (int x = minXKinectVisu; x < maxXKinectVisu; x += kinectVisuSkip) {
                    int offset = (x + y * KinectCst.w);
                    boolean good = isGoodDepth(depth[x + y * KinectCst.w]);
                    validPoints[offset] = good;
                    if (good) {
                        points[offset] = KinectCst.depthToWorld(x, y, depth[offset]);
                    }
                }
            }


            ArrayList<Integer> allPoints = new ArrayList<Integer>();

            if (withBackground && isBackgroundDeleted) {

                for (int y = minYKinectVisu + kinectVisuSkip; y < maxYKinectVisu; y += kinectVisuSkip) {
                    for (int x = minXKinectVisu + kinectVisuSkip; x < maxXKinectVisu; x += kinectVisuSkip) {
                        int offset = x + y * KinectCst.w;

                        if (validPoints[offset] && backgroundValidPoints[offset]
                                && PApplet.abs(planeSelection.distanceTo(points[offset])) < distanceZ
                                && planeSelection.orientation(points[offset])) {

                            //		  gfx.poInteger.parseInt(points[offset]);

                            Vec3D tr = transform.applyTo(planeSelection.plane.getProjectedPoint(points[offset]));
                            tr.x /= tr.z;
                            tr.y /= tr.z;
                            projPoints[offset] = tr;
                            allPoints.add(offset);
                            validPoints[offset] = true;
                        } else {
                            validPoints[offset] = false;
                        }
                    }
                }
            } else {
                if (isDeletingBackground) {
                    for (int y = minYKinectVisu + kinectVisuSkip; y < maxYKinectVisu; y += kinectVisuSkip) {
                        for (int x = minXKinectVisu + kinectVisuSkip; x < maxXKinectVisu; x += kinectVisuSkip) {
                            int offset = x + y * KinectCst.w;

                            if (validPoints[offset]
                                    && PApplet.abs(planeSelection.distanceTo(points[offset])) < distanceZ
                                    && planeSelection.orientation(points[offset])) {
                                backgroundValidPoints[offset] = false;
                                //		    gfx.poInteger.parseInt(points[offset]);

                                Vec3D tr = transform.applyTo(planeSelection.plane.getProjectedPoint(points[offset]));
                                tr.x /= tr.z;
                                tr.y /= tr.z;
                                projPoints[offset] = tr;
                                allPoints.add(offset);

                            } else {
                                validPoints[offset] = false;
                            }
                        }
                    }
                } else {

                    // General case

                    for (int y = minYKinectVisu + kinectVisuSkip; y < maxYKinectVisu; y += kinectVisuSkip) {
                        for (int x = minXKinectVisu + kinectVisuSkip; x < maxXKinectVisu; x += kinectVisuSkip) {
                            int offset = x + y * KinectCst.w;
                            if (validPoints[offset]
                                    && PApplet.abs(planeSelection.distanceTo(points[offset])) < distanceZ
                                    && planeSelection.orientation(points[offset])) {
                                // if(test)
                                //   println(plane.distanceTo(points[offset]));

                                //		    gfx.poInteger.parseInt(points[offset]);
                                Vec3D tr = transform.applyTo(planeSelection.plane.getProjectedPoint(points[offset]));
                                tr.x /= tr.z;
                                tr.y /= tr.z;

                                // TODO: optimisation : distance table ? 
                                tr.z = planeSelection.plane.distanceTo(points[offset]);
                                projPoints[offset] = tr;
                                allPoints.add(offset);
                            } else {
                                validPoints[offset] = false;
                            }

                        }
                    }
                }
            }


            return allPoints;
        }
        return null;  // inactive
    }

    private int ROISize() {
        return (maxXKinectVisu - minXKinectVisu) * (maxYKinectVisu - minYKinectVisu);
    }

    private boolean isGoodDepth(int rawDepth) {
        return (rawDepth >= minDepthKinectVisu && rawDepth < maxDepthKinectVisu);
    }
   
    
}
