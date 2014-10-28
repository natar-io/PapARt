/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.Kinect;
import fr.inria.papart.depthcam.calibration.KinectScreenCalibration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import processing.core.PApplet;
import static processing.core.PConstants.EPSILON;
import processing.core.PVector;
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

    public static float MINIMUM_COMPONENT_SIZE = 5;   // in px
    public static float MINIMUM_COMPONENT_SIZE_3D = 50; // in px

    public static int MAX_REC = 500;

    // created here
    protected static boolean[] assignedPoints = null;
    protected static byte[] connectedComponentImage = null;
    protected static byte currentCompo = 1;

// set by calling function
    protected static boolean[] validPoints;
    protected static DepthData depthData;
    protected static int precision;

    public static ConnectedComponent findNeighboursRec(
            int currentPoint,
            int halfNeigh,
            Set<Integer> toVisit,
            int recLevel) {

        // TODO: optimisations here ?
        int x = currentPoint % Kinect.WIDTH;
        int y = currentPoint / Kinect.WIDTH;
        ConnectedComponent neighbourList = new ConnectedComponent();
        ArrayList<Integer> visitNext = new ArrayList<Integer>();

        if (recLevel == MAX_REC) {
            return neighbourList;
        }

        int minX = PApplet.constrain(x - halfNeigh, 0, Kinect.WIDTH - 1);
        int maxX = PApplet.constrain(x + halfNeigh, 0, Kinect.WIDTH - 1);
        int minY = PApplet.constrain(y - halfNeigh, 0, Kinect.HEIGHT - 1);
        int maxY = PApplet.constrain(y + halfNeigh, 0, Kinect.HEIGHT - 1);

        for (int j = minY; j <= maxY; j += precision) {
            for (int i = minX; i <= maxX; i += precision) {

                int offset = j * Kinect.WIDTH + i;

                // Avoid getting ouside the limits
                if (isValidNeighbour(offset, currentPoint)) {

                    assignedPoints[offset] = true;
                    connectedComponentImage[offset] = currentCompo;

                    toVisit.remove(offset);
                    neighbourList.add((Integer) offset);

//                    // if is is on a border ??
//                    if (i == minX || j == minX || i >= maxX - skip || j >= maxY - skip) {
                    visitNext.add(offset);
//                    } // if it is a border

                } // if is ValidPoint

            } // for j
        } // for i

        for (int offset : visitNext) {
            neighbourList.addAll(findNeighboursRec(
                    offset,
                    halfNeigh,
                    toVisit,
                    recLevel + 1));
        }

        return neighbourList;
    }

    protected static boolean isValidNeighbour(int offset, int currentPoint) {
        float distanceToCurrent = depthData.kinectPoints[offset].distanceTo(depthData.kinectPoints[currentPoint]);

        return !assignedPoints[offset] // not assigned  
                && validPoints[offset] // valid point (in the research space)
                && (depthData.kinectPoints[offset] != Kinect.INVALID_POINT) // invalid point (invalid depth)
                && distanceToCurrent < maxDistance;
    }

    public static ArrayList<TouchPoint> findMultiTouch2D(DepthData dData, int skip) {
        initWith(dData, skip);

        validPoints = depthData.validPointsMask;
        Set<Integer> toVisit = new HashSet<Integer>();
        toVisit.addAll(depthData.validPointsList);

        ArrayList<TouchPoint> touchPointFounds = new ArrayList<TouchPoint>();
        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents(toVisit);

        for (ConnectedComponent connectedComponent : connectedComponents) {

            if (connectedComponent.size() < MINIMUM_COMPONENT_SIZE) {
                continue;
            }

            Vec3D meanProj = connectedComponent.getMean(depthData.projectedPoints);
            Vec3D meanKinect = connectedComponent.getMean(depthData.kinectPoints);
            TouchPoint tp = new TouchPoint();
            tp.setPosition(meanProj);
            tp.setPositionKinect(meanKinect);
            tp.set3D(false);
            tp.setConfidence(connectedComponent.size() / MINIMUM_COMPONENT_SIZE);

            touchPointFounds.add(tp);
        }

        return touchPointFounds;
    }

    public static ArrayList<TouchPoint> findMultiTouch3D(DepthData dData, int skip) {
        initWith(dData, skip);

        validPoints = depthData.validPointsMask3D;
        Set<Integer> toVisit = new HashSet<Integer>();
        toVisit.addAll(depthData.validPointsList3D);

        ArrayList<ConnectedComponent> connectedComponents = findConnectedComponents(toVisit);
        ArrayList<TouchPoint> touchPointFounds = new ArrayList<TouchPoint>();
        ClosestComparatorY closestComparator = new ClosestComparatorY(depthData.projectedPoints);

        for (ConnectedComponent connectedComponent : connectedComponents) {

            if (connectedComponent.size() < MINIMUM_COMPONENT_SIZE_3D) {
                continue;
            }

            // get a subset of the points.
            Collections.sort(connectedComponent, closestComparator);

            //  Get a sublist
            List<Integer> subList = connectedComponent.subList(0, 10);
            ConnectedComponent subCompo = new ConnectedComponent();
            subCompo.addAll(subList);

            Vec3D meanProj = subCompo.getMean(depthData.projectedPoints);
            Vec3D meanKinect = subCompo.getMean(depthData.kinectPoints);
            TouchPoint tp = new TouchPoint();
            tp.setPosition(meanProj);
            tp.setPositionKinect(meanKinect);
            tp.set3D(true);
            tp.setConfidence(connectedComponent.size() / MINIMUM_COMPONENT_SIZE_3D);
            touchPointFounds.add(tp);
        }

        return touchPointFounds;
    }

    protected static void initWith(DepthData dData, int skip) {
        depthData = dData;
        precision = skip;
        int nbPoints = depthData.kinectPoints.length;
        checkMemoryAllocation(nbPoints);
    }

    protected static ArrayList<ConnectedComponent> findConnectedComponents(Set<Integer> toVisit) {
        clearMemory();
        currentCompo = 1;
        ArrayList<ConnectedComponent> connectedComponents = new ArrayList<ConnectedComponent>();
        int searchDepth = precision; // on each direction
        // New method, recursive way. 
        while (toVisit.size() > 0) {
            int startingPoint = toVisit.iterator().next();
            ConnectedComponent neighbours = findNeighboursRec(
                    startingPoint,
                    searchDepth,
                    toVisit,
                    0);

            connectedComponents.add(neighbours);
            currentCompo++;
        }
        return connectedComponents;
    }

    protected static void checkMemoryAllocation(int size) {
        if (assignedPoints == null && connectedComponentImage == null) {
            assignedPoints = new boolean[size];
            connectedComponentImage = new byte[size];
        }
    }

    protected static void clearMemory() {
        Arrays.fill(assignedPoints, false);
        Arrays.fill(connectedComponentImage, (byte) 0);
    }

    public static float sideError = 0.2f;

    public static boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public static boolean isInside(PVector v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
