/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import fr.inria.papart.kinect.KinectCst;
import fr.inria.papart.kinect.KinectScreenCalibration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import processing.core.PApplet;
import toxi.geom.Matrix4x4;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class Touch3D {

    public static ArrayList<Integer> findNeighbours3D(int currentPoint, int halfNeigh,
            ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D[] projPoints,
            boolean[] isValidPoints,
            boolean[] readPoints,
            Set<Integer> toVisit, int skip) {

        // TODO: optimisations here ?

        int x = currentPoint % KinectCst.w;
        int y = currentPoint / KinectCst.w;

        if (toVisit.contains(currentPoint)) {
            toVisit.remove(currentPoint);
        }

        ArrayList<Integer> ret = new ArrayList<Integer>();

        int max = KinectCst.w * KinectCst.h;

        for (int j = -halfNeigh; j < halfNeigh + skip; j += skip) {
            for (int i = -halfNeigh; i < halfNeigh + skip; i += skip) {

                int offset = (x + i) + (y + j) * KinectCst.w;

                // Avoid getting ouside the limits
                if (!(offset >= max
                        || (x + i) != PApplet.constrain(x + i, 0, KinectCst.w) || // to big or small in X
                        (y + j) != PApplet.constrain(y + j, 0, KinectCst.h) || // to big or small in Y
                        readPoints[offset] || // already parsed point
                        !isValidPoints[offset]
                        || projPoints[offset] == null
                        || !Touch.isInside(projPoints[offset], 0.f, 1.f))) {

                    readPoints[offset] = true;
                    ret.add((Integer) offset);

                    // if is is on a border
                    if (PApplet.abs(i) == halfNeigh - skip
                            || PApplet.abs(j) == halfNeigh - skip) {

                        // add to the list to examine
                        toVisit.add(offset);
                        readPoints[offset] = false;
                    } // if it is a border
                } // if is ValidPoint

            } // for j
        } // for i


        return ret;
    }

    public static ArrayList<ArrayList<Integer>> allNeighbourhood3D(ArrayList<Integer> validPoints,
            Vec3D points[], Vec3D projPoints[], boolean[] isValidPoints, int skip) {

        if (validPoints == null || validPoints.isEmpty()) {
            return null;
        }

        // TODO: Magic Numbers !!!!
        int searchDepth = 3 * skip; // on each direction
        int searchDepth2 = 2 * skip; // on each direction

        ////  Each detected Point is going to be parsed.
        boolean readPoints[] = new boolean[KinectCst.w * KinectCst.h];
        Set<Integer> toVisit = new HashSet<Integer>();

        ArrayList<ArrayList<Integer>> allNeighbourhood = new ArrayList<ArrayList<Integer>>();

        // all points are "valid"  i.e. detected in the right zone
        for (Integer v : validPoints) {
            if (!readPoints[v]) {

                ArrayList<Integer> n1 = findNeighbours3D(v, searchDepth, validPoints,
                        points, projPoints, isValidPoints, readPoints, toVisit, skip);

                while (toVisit.size() > 0) {
                    int visiting = toVisit.iterator().next();
                    n1.addAll(findNeighbours3D(visiting, searchDepth2, validPoints,
                            points, projPoints, isValidPoints, readPoints, toVisit, skip));
                }

                if (n1.isEmpty()) {
                    continue;
                }

                allNeighbourhood.add(n1);
            }
        }
        return allNeighbourhood;
    }

    public static ArrayList<TouchPoint> find3D(ArrayList<Integer> validPoints,
            Vec3D[] points, Vec3D[] projPoints, boolean[] isValidPoints,
            KinectScreenCalibration calibration, int skip) {

        return find3D(validPoints,
                points, projPoints, isValidPoints,
                calibration, skip,
                1);
    }

    public static ArrayList<TouchPoint> find3D(ArrayList<Integer> validPoints,
            Vec3D[] points, Vec3D[] projPoints,
            boolean[] isValidPoints,
            KinectScreenCalibration calibration, int skip,
            float height3D) {

        if (validPoints == null || validPoints.isEmpty()) {
            return null;
        }

        ArrayList<ArrayList<Integer>> allNeighbourhood = allNeighbourhood3D(validPoints,
                points, projPoints, isValidPoints, skip);

        ArrayList<TouchPoint> allTouchPoints = new ArrayList<TouchPoint>();

        // TODO: Magic Numbers !!!
        int minSize = 30; // in pixels

//        ClosestComparator cc = new ClosestComparator(projPoints);
//        ClosestComparatorY cch = new ClosestComparatorY(projPoints);
        ClosestComparatorHeight cch = new ClosestComparatorHeight(points, calibration);

        // remove too small elements
        for (ArrayList<Integer> vint : allNeighbourhood) {
            if (vint.size() < minSize) {
                continue;
            }

            TouchPoint tp = new TouchPoint();
            tp.is3D = true;
            tp.confidence = vint.size();

            Collections.sort(vint, cch);

            Vec3D mean = new Vec3D(0, 0, 0);
            Vec3D mean2 = new Vec3D(0, 0, 0);
            int k = 0;
            Vec3D min = points[vint.get(0)];

            for (int offset : vint) {
                if (points[offset].distanceTo(min) < 0.03) {
                    mean2.addSelf(points[offset]);

                    mean.addSelf(projPoints[offset]);
                    k++;
                }
            }
            //  println(k + "points found");

            mean.scaleSelf(1.0f / k);
            mean2.scaleSelf(1.0f / k);

            tp.v = mean;
            tp.v.z = calibration.plane().distanceTo(mean2) / height3D;
//            tp.v.z = planeSelection.distanceTo(mean2) / height3D;
            tp.vKinect = mean2;
            //      tp.isCloseToPlane = planeSelection.distanceTo(mean) < 0.1 ;
            //      println("distance : " +  planeSelection.distanceTo(mean));

            allTouchPoints.add(tp);
        }

        TouchPointComparator tpc = new TouchPointComparator();
        Collections.sort(allTouchPoints, tpc);

        return allTouchPoints;
    }
}
