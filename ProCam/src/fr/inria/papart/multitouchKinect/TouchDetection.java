/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import fr.inria.papart.kinect.Kinect;
import fr.inria.papart.kinect.KinectScreenCalibration;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 * This class is detecting elements close to a predifined plane.
 *
 * @author jeremy
 */
public class TouchDetection {

    public static float currentMaxDistance;
    public static float maxDistance = 8f;    // in mm
    public static float maxDistance3D = 20f;    // in mm
    public static int MAX_REC = 500;

    public static ArrayList<Integer> findNeighboursRec(int currentPoint, int halfNeigh,
            ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D[] projPoints,
            boolean[] isValidPoints,
            boolean[] readPoints, Set<Integer> toVisit,
            int skip, int recLevel) {

        // TODO: optimisations here ?
        int x = currentPoint % Kinect.KINECT_WIDTH;
        int y = currentPoint / Kinect.KINECT_WIDTH;

        ArrayList<Integer> ret = new ArrayList<Integer>();
        ArrayList<Integer> visitNext = new ArrayList<Integer>();

        if(recLevel == MAX_REC)
            return ret;
        
        int minX = PApplet.constrain(x - halfNeigh, 0, Kinect.KINECT_WIDTH - 1);
        int maxX = PApplet.constrain(x + halfNeigh, 0, Kinect.KINECT_WIDTH - 1);
        int minY = PApplet.constrain(y - halfNeigh, 0, Kinect.KINECT_HEIGHT - 1);
        int maxY = PApplet.constrain(y + halfNeigh, 0, Kinect.KINECT_HEIGHT - 1);

        for (int j = minY; j <= maxY; j += skip) {
            for (int i = minX; i <= maxX; i += skip) {

                int offset = j * Kinect.KINECT_WIDTH + i;

                // Avoid getting ouside the limits
                if (!(readPoints[offset] // already parsed point 
                        || !isValidPoints[offset]
                        //                        || !isInside(projPoints[offset], 0.f, 1.f))) {
                        || !isInside(projPoints[offset], 0.f, 1.f)
                        || points[offset].distanceTo(points[currentPoint]) > maxDistance)) {

                    readPoints[offset] = true;

                    toVisit.remove(offset);
                    // we add it to the neighbour list
                    ret.add((Integer) offset);

                    Kinect.connectedComponent[offset] = Kinect.currentCompo;

//                    // if is is on a border ??
//                    if (i == minX || j == minX || i >= maxX - skip || j >= maxY - skip) {
                    visitNext.add(offset);
//                    } // if it is a border

                } // if is ValidPoint

            } // for j
        } // for i

        for (int offset : visitNext) {
            ret.addAll(findNeighboursRec(offset,
                    halfNeigh,
                    validPoints,
                    points, projPoints,
                    isValidPoints,
                    readPoints, toVisit, skip, recLevel +1));
        }

        return ret;
    }

    public static ArrayList<TouchPoint> findMultiTouch(ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D[] projPoints, boolean[] isValidPoints, boolean[] readPoints,
            KinectScreenCalibration calib, boolean is3D, int skip) {

        if (validPoints == null || validPoints.isEmpty()) {
            return null;
        }

        currentMaxDistance = is3D ? maxDistance3D : maxDistance;

        // Debug purposes
        Arrays.fill(Kinect.connectedComponent, (byte) 0);
        Kinect.currentCompo = 1;

//        int searchDepth = 1 * skip; // on each direction
        int searchDepth = 1 * skip; // on each direction

        Arrays.fill(readPoints, false);
        Set<Integer> toVisit = new HashSet<Integer>();

        ArrayList<ArrayList<Integer>> allNeighbourhood = new ArrayList<ArrayList<Integer>>();

        // New method, recursive way. 
        toVisit.addAll(validPoints);

        while (toVisit.size() > 0) {
            int p = toVisit.iterator().next();
            allNeighbourhood.add(findNeighboursRec(p, searchDepth, validPoints,
                    points, projPoints, isValidPoints, readPoints, toVisit, skip, 0));
            Kinect.currentCompo++;
        }

        ArrayList<TouchPoint> allTouchPoints = new ArrayList<TouchPoint>();

//        int minSize = 50 / (skip * skip); // in pixels
        // TODO: Magic numbers ...
        int minSize = 5;
        int nbPoints3D = 15;
        if (is3D) {
            minSize = 60;
        }

        float goodPointsDist = 0.03f;

        float closeDistance = calib.plane().getHeight();   // valeur indiquée dans calib * 0.05

        ClosestComparatorHeight cch = new ClosestComparatorHeight(points, calib);

        // remove too small elements
        for (ArrayList<Integer> vint : allNeighbourhood) {

            if (vint.size() < minSize) {
                continue;
            }

            // sort all points
            Collections.sort(vint, cch);

            Vec3D mean = new Vec3D(0, 0, 0);

            Vec3D min = new Vec3D(projPoints[vint.get(0)]);
            Vec3D max = new Vec3D(projPoints[vint.get(0)]);

            if (is3D) {
                // select only the closest 
                for (int k = 0; k < nbPoints3D; k++) {

//                    Vec3D p = projPoints[vint.get(k)];
//                    if (p.x < min.x) {
//                        min.x = p.x;
//                    }
//                    if (p.y < min.y) {
//                        min.y = p.y;
//                    }
//                    if (p.z < min.z) {
//                        min.z = p.z;
//                    }
//                    if (p.x > max.x) {
//                        max.x = p.x;
//                    }
//                    if (p.y > max.y) {
//                        max.y = p.y;
//                    }
//                    if (p.z > max.z) {
//                        max.z = p.z;
//                    }

                    mean.addSelf(points[vint.get(k)]);
                }
                mean.scaleSelf(1.0f / nbPoints3D);
            } else {

                // REAL MEAN
                for (int offset : vint) {

//                    Vec3D p = points[offset];
//                    if (p.x < min.x) {
//                        min.x = p.x;
//                    }
//                    if (p.y < min.y) {
//                        min.y = p.y;
//                    }
//                    if (p.z < min.z) {
//                        min.z = p.z;
//                    }
//                    if (p.x > max.x) {
//                        max.x = p.x;
//                    }
//                    if (p.y > max.y) {
//                        max.y = p.y;
//                    }
//                    if (p.z > max.z) {
//                        max.z = p.z;
//                    }

                    mean.addSelf(points[offset]);
                }
                mean.scaleSelf(1.0f / vint.size());
//                for (int k = 0; k < vint.size() / 2; k++) {
//                    mean.addSelf(points[vint.get(k)]);
//                }
            }

            TouchPoint tp = new TouchPoint();

            tp.is3D = is3D;
            tp.confidence = vint.size();
//            tp.size = min.distanceTo(max);

            tp.v = mean;
            tp.vKinect = tp.v.copy();
            tp.isCloseToPlane = calib.plane().distanceTo(mean) < closeDistance;
            tp.v = calib.project(tp.v);
            tp.v.z = calib.plane().distanceTo(mean);

            allTouchPoints.add(tp);
        }

        return allTouchPoints;
    }
    public static float sideError = 0.2f;

    public static boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public static boolean isInside(PVector v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
