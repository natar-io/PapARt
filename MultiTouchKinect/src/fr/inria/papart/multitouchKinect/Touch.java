/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import fr.inria.papart.kinect.KinectCst;
import fr.inria.papart.kinect.KinectScreenCalibration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import processing.core.PApplet;
import toxi.geom.Matrix4x4;
import toxi.geom.Vec3D;

/**
 * This class is detecting elements close to a predifined plane.
 *
 * @author jeremy
 */
public class Touch {

    public static float maxDistance = 0.2f;

    public static ArrayList<Integer> findNeighbours(int currentPoint, int halfNeigh,
            ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D[] projPoints,
            boolean[] isValidPoints,
            boolean[] readPoints, int recLevel,
            Set<Integer> toVisit, int skip) {

        // TODO: optimisations here ?

        int x = currentPoint % KinectCst.w;
        int y = currentPoint / KinectCst.w;

        readPoints[currentPoint] = true;

        if (toVisit.contains(currentPoint)) {
            toVisit.remove(currentPoint);
        }

        ArrayList<Integer> ret = new ArrayList<Integer>();

        int minX = PApplet.constrain(x - halfNeigh, 0, KinectCst.w - 1);
        int maxX = PApplet.constrain(x + halfNeigh, 0, KinectCst.w - 1);
        int minY = PApplet.constrain(y - halfNeigh, 0, KinectCst.h - 1);
        int maxY = PApplet.constrain(y + halfNeigh, 0, KinectCst.h - 1);

        for (int j = minY; j <= maxY; j += skip) {
            for (int i = minX; i <= maxX; i += skip) {

                int offset = j * KinectCst.w + i;

                // Avoid getting ouside the limits
                if (!(readPoints[offset] // already parsed point 
                        || !isValidPoints[offset]
                        || !isInside(projPoints[offset], 0.f, 1.f))) {

                    readPoints[offset] = true;

                    // we add it to the neighbour list
                    ret.add((Integer) offset);

                    toVisit.add((Integer) offset);
//                    // if is is on a border ??
//                    if (PApplet.abs(i) == halfNeigh - skip
//                            || PApplet.abs(j) == halfNeigh - skip) {
//
//                        // add to the list to examine
//                        toVisit.add(offset);
//                        readPoints[offset] = false;
//
//                    } // if it is a border


                } // if is ValidPoint

            } // for j
        } // for i

        return ret;
    }

    public static ArrayList<ArrayList<Integer>> allNeighbourhood(ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D[] projPoints, boolean[] isValidPoints, int skip) {

        if (validPoints == null || validPoints.isEmpty()) {
            return null;
        }

        int searchDepth = 4 * skip; // on each direction
        int searchDepth2 = 4 * skip; // on each direction

        ////  Each detected Point is going to be parsed.
        boolean readPoints[] = new boolean[KinectCst.w * KinectCst.h];
        Set<Integer> toVisit = new HashSet<Integer>();


//  currentColor = 0;
        ArrayList<ArrayList<Integer>> allNeighbourhood = new ArrayList<ArrayList<Integer>>();

        // all points are "valid"  i.e. detected in the right zone

        for (Integer p : validPoints) {
            if (!readPoints[p]) {

                ArrayList<Integer> n1 = findNeighbours(p, searchDepth, validPoints,
                        points, projPoints, isValidPoints, readPoints, 0, toVisit, skip);

                while (toVisit.size() > 0) {

//                    System.out.println("Building compo..." + n1.size());

                    int visiting = toVisit.iterator().next();
                    n1.addAll(findNeighbours(visiting, searchDepth2, validPoints,
                            points, projPoints, isValidPoints, readPoints, 3, toVisit, skip));
                }

                if (n1.isEmpty()) {
                    continue;
                }

                allNeighbourhood.add(n1);
            }
        }
        return allNeighbourhood;
    }

    public static ArrayList<TouchPoint> findMultiTouch(ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D[] projPoints, boolean[] isValidPoints,
            KinectScreenCalibration calib, int skip) {

        if (validPoints == null || validPoints.isEmpty()) {
            return null;
        }

        ArrayList<ArrayList<Integer>> allNeighbourhood = allNeighbourhood(validPoints,
                points, projPoints, isValidPoints, skip);

        ArrayList<TouchPoint> allTouchPoints = new ArrayList<TouchPoint>();

//        int minSize = 50 / (skip * skip); // in pixels

        // TODO: Magic numbers ...
        int minSize = 10;
        float closeDistance = calib.plane().getHeight() / 5f;   // valeur indiquée dans calib * 0.05

        ClosestComparatorHeight cch = new ClosestComparatorHeight(points, calib);

        // DEBUG:  remove all valid points.
//        Arrays.fill(isValidPoints, false);

        // remove too small elements
        for (ArrayList<Integer> vint : allNeighbourhood) {

            if (vint.size() < minSize) {
                continue;
            }

            System.out.println("Size " + vint.size());
            // sort all points
            Collections.sort(vint, cch);

            TouchPoint tp = new TouchPoint();
            tp.is3D = false;
            tp.confidence = vint.size();

            Vec3D mean = new Vec3D(0, 0, 0);
            Vec3D closeMean = new Vec3D(0, 0, 0);
            int nbClose = 0;

            // select only the closest 
            for (int k = 0; k < minSize; k++) {
                int offset = vint.get(k);
                mean.addSelf(points[offset]);

                // DEBUG: select only the valid points
//                isValidPoints[offset] = true;
            }

            mean.scaleSelf(1.0f / minSize);
            tp.v = mean;
            tp.vKinect = tp.v.copy();

//            tp.isCloseToPlane = planeSelection.distanceTo(mean) < closeDistance;
            tp.isCloseToPlane = calib.plane().distanceTo(mean) < closeDistance;

//            System.out.println("size "+ vint.size() + " distance : " + planeSelection.distanceTo(mean) + " Confidence " + tp.confidence);
            tp.vKinect = tp.v.copy();

            tp.v = calib.project(tp.v);
            tp.v.z = calib.plane().distanceTo(mean);

            allTouchPoints.add(tp);
            //tp.draw();
        }

        return allTouchPoints;
    }
    public static float sideError = 0.2f;

    public static boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
