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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * This class is detecting elements close to a predifined plane.
 *
 * @author jeremy
 */
@Deprecated
public class TouchDetectionLegacy {

    public float maxDistance = 10f;    // in mm

    public float DEFAULT_MAX_DISTANCE = 10f;    // in mm

    public float MINIMUM_COMPONENT_SIZE = 5;   // in px
    public float MINIMUM_COMPONENT_SIZE_3D = 50; // in px

    private int MAX_REC = 500;

    // created here
    private boolean[] assignedPoints = null;
    private byte[] connectedComponentImage = null;
    private final byte STARTING_CONNECTED_COMPONENT = 1;
    private final byte NO_CONNECTED_COMPONENT = 0;
    private byte currentCompo = STARTING_CONNECTED_COMPONENT;

// set by calling function
    private boolean[] validPoints;
    private DepthData depthData;
    private int precision;
    private int searchDepth;

    private ArrayList<HashSet<Integer>> connectedComponentGraph = new ArrayList<HashSet<Integer>>();

    private HashSet<Integer> toVisit;
    private PointValidityCondition currentPointValidityCondition;

    public interface PointValidityCondition {

        public boolean checkPoint(int offset, int currentPoint);
    }

    public ConnectedComponent findNeighboursRec(
            int currentPoint,
            int recLevel) {

        // TODO: optimisations here ?
        int x = currentPoint % Kinect.WIDTH;
        int y = currentPoint / Kinect.WIDTH;
        ConnectedComponent neighbourList = new ConnectedComponent();
        ArrayList<Integer> visitNext = new ArrayList<Integer>();

        if (recLevel == MAX_REC) {
            return neighbourList;
        }

        int minX = PApplet.constrain(x - searchDepth, 0, Kinect.WIDTH - 1);
        int maxX = PApplet.constrain(x + searchDepth, 0, Kinect.WIDTH - 1);
        int minY = PApplet.constrain(y - searchDepth, 0, Kinect.HEIGHT - 1);
        int maxY = PApplet.constrain(y + searchDepth, 0, Kinect.HEIGHT - 1);

        for (int j = minY; j <= maxY; j += precision) {
            for (int i = minX; i <= maxX; i += precision) {

                int offset = j * Kinect.WIDTH + i;

                // Avoid getting ouside the limits
                if (currentPointValidityCondition.checkPoint(offset, currentPoint)) {

                    assignedPoints[offset] = true;
                    connectedComponentImage[offset] = currentCompo;

                    // Remove If present -> it might not be the case often. 
                    toVisit.remove(offset);
                    neighbourList.add((Integer) offset);
                    visitNext.add(offset);

                } // if is ValidPoint
            } // for j
        } // for i

        for (int offset : visitNext) {
            neighbourList.addAll(findNeighboursRec(
                    offset,
                    recLevel + 1));
        }

        return neighbourList;
    }

    public class CheckOverPlane implements PointValidityCondition {

        private static final float MAX_HAND_SIZE = 200f; // 20 cm 
        private final Vec3D firstPoint;

        public CheckOverPlane(int offset) {
            firstPoint = depthData.kinectPoints[offset];
        }

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToFirst = firstPoint.distanceTo(depthData.kinectPoints[currentPoint]);
            return !assignedPoints[offset] // not assigned  
                    && depthData.touchAttributes[offset].isOverTouch() // is a «Touch» point
                    && (depthData.kinectPoints[offset] != Kinect.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToFirst < MAX_HAND_SIZE;
        }
    }

    public class CheckTouchPoint implements PointValidityCondition {

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToCurrent = depthData.kinectPoints[offset].distanceTo(depthData.kinectPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && depthData.touchAttributes[offset].isInTouch() // is a «Touch» point
                    && (depthData.kinectPoints[offset] != Kinect.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToCurrent < maxDistance;
        }
    }

    public class CheckSimpleTouch implements PointValidityCondition {

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToCurrent = depthData.kinectPoints[offset].distanceTo(depthData.kinectPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && depthData.touchAttributes[offset].isOverTouch() // is over the plane
                    && (depthData.kinectPoints[offset] != Kinect.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToCurrent < maxDistance;
        }
    }

    public ArrayList<TouchPoint> findMultiTouch2D(DepthData dData, int skip) {
        initWith(dData, skip);

        ArrayList<TouchPoint> touchPointFounds = new ArrayList<TouchPoint>();
        validPoints = depthData.validPointsMask;
        toVisit = new HashSet<Integer>();
        toVisit.addAll(depthData.validPointsList);

        if (toVisit.isEmpty()) {
            return touchPointFounds;
        }

        ArrayList<ConnectedComponent> connectedComponents = findTouchComponentsSimpleTouch();
        for (ConnectedComponent connectedComponent : connectedComponents) {
            if (connectedComponent.size() < MINIMUM_COMPONENT_SIZE) {
                continue;
            }
            TouchPoint tp = createTouchPoint(connectedComponent);
            touchPointFounds.add(tp);
        }

        return touchPointFounds;
    }

    public ArrayList<TouchPoint> findRichMultiTouch(DepthData dData, int skip) {
        initWith(dData, skip);

        ArrayList<TouchPoint> touchPointFounds = new ArrayList<TouchPoint>();
        validPoints = depthData.validPointsMask;
        HashSet<Integer> toVisit = new HashSet<Integer>();
        toVisit.addAll(depthData.validPointsList);

        if (toVisit.isEmpty()) {
            return touchPointFounds;
        }

        // Touch Connected component
        ArrayList<ConnectedComponent> connectedComponents = findTouchComponents(toVisit);

        // For each touch connected component, 
        // - find the greater component. 
        // - find if it touches the table. 
//        connectedComponentGraph 
        for (ConnectedComponent connectedComponent : connectedComponents) {

            HashSet<Integer> connectionIDs = new HashSet<Integer>();
            findGreaterComponent(connectedComponent);
        }

        for (ConnectedComponent connectedComponent : connectedComponents) {
            if (connectedComponent.size() < MINIMUM_COMPONENT_SIZE) {
                continue;
            }

            TouchPoint tp = createTouchPoint(connectedComponent);
            touchPointFounds.add(tp);
        }

        return touchPointFounds;
    }

    private void initWith(DepthData dData, int skip) {
        depthData = dData;
        precision = skip;
        int nbPoints = depthData.kinectPoints.length;
        checkMemoryAllocation(nbPoints);
    }

    private TouchPoint createTouchPoint(ConnectedComponent connectedComponent) {
        Vec3D meanProj = connectedComponent.getMean(depthData.projectedPoints);
        Vec3D meanKinect = connectedComponent.getMean(depthData.kinectPoints);
        TouchPoint tp = new TouchPoint();
        tp.setPosition(meanProj);
        tp.setPositionKinect(meanKinect);
        tp.setCreationTime(depthData.timeStamp);
        tp.set3D(false);
        tp.setConfidence(connectedComponent.size() / MINIMUM_COMPONENT_SIZE);
        return tp;
    }

    private ConnectedComponent findGreaterComponent(ConnectedComponent connectedComponent) {

        clearMemory();
        int startingPoint = connectedComponent.get(0);
        currentPointValidityCondition = new CheckOverPlane(startingPoint);

        ConnectedComponent neighbours = findNeighboursRec(
                startingPoint,
                0);
        return neighbours;
    }

    private ArrayList<ConnectedComponent> findTouchComponents(Set<Integer> toVisit) {
        clearMemory();
        setSearchParameters(toVisit);
        currentPointValidityCondition = new CheckTouchPoint();

        ArrayList<ConnectedComponent> connectedComponents = computeComponentsTouch(toVisit);
        return connectedComponents;
    }

    private ArrayList<ConnectedComponent> computeComponentsTouch(Set<Integer> toVisit) {

        ArrayList<ConnectedComponent> connectedComponents = new ArrayList<ConnectedComponent>();
        // recursive search for each component. 
        while (toVisit.size() > 0) {
            int startingPoint = toVisit.iterator().next();
            ConnectedComponent neighbours = findNeighboursRec(
                    startingPoint,
                    0);
            neighbours.setId(currentCompo);

            connectedComponents.add(neighbours);
            currentCompo++;
        }
        return connectedComponents;
    }

    private ArrayList<ConnectedComponent> findTouchComponentsSimpleTouch() {
        clearMemory();
        setSearchParameters(toVisit);
        currentPointValidityCondition = new CheckSimpleTouch();
        ArrayList<ConnectedComponent> connectedComponents = computeComponentsSimpleTouch(toVisit);
        return connectedComponents;
    }

    private void setSearchParameters(Set<Integer> toVisit) {
        int firstPoint = toVisit.iterator().next();
        setPrecisionFrom(firstPoint);
        searchDepth = precision * 7;// TODO: FIX this value !
        MAX_REC = 100; // TODO: fix this value.
    }

    private ArrayList<ConnectedComponent> computeComponentsSimpleTouch(Set<Integer> toVisit) {

        ArrayList<ConnectedComponent> connectedComponents = new ArrayList<ConnectedComponent>();

        // recursive search for each component. 
        while (toVisit.size() > 0) {
            int startingPoint = toVisit.iterator().next();

            ConnectedComponent neighbours = findNeighboursRec(
                    startingPoint,
                    0);
            connectedComponents.add(neighbours);
            neighbours.setId(currentCompo);

            currentCompo++;
        }
        return connectedComponents;
    }

    public float ERROR_DISTANCE_MULTIPLIER = 1.3f;
    public float NOISE_ESTIMATION = 1f; // in millimeter. 

    private void setPrecisionFrom(int firstPoint) {

        Vec3D currentPoint = depthData.kinectPoints[firstPoint];
        PVector coordinates = depthData.projectiveDevice.getCoordinates(firstPoint);

        // Find a point. 
        int x = (int) coordinates.x;
        int y = (int) coordinates.y;
        int minX = PApplet.constrain(x - precision, 0, depthData.projectiveDevice.getWidth() - 1);
        int maxX = PApplet.constrain(x + precision, 0, depthData.projectiveDevice.getWidth() - 1);
        int minY = PApplet.constrain(y - precision, 0, depthData.projectiveDevice.getHeight() - 1);
        int maxY = PApplet.constrain(y + precision, 0, depthData.projectiveDevice.getHeight() - 1);

        for (int j = minY; j <= maxY; j += precision) {
            for (int i = minX; i <= maxX; i += precision) {
                Vec3D nearbyPoint = depthData.projectiveDevice.pixelToWorld(i,
                        j, currentPoint.z);

                // Set the distance. 
                setDistance(currentPoint.distanceTo(nearbyPoint));
                return;
            }
        } // for i

    }

    // TODO: subclass for this, or a parameter...
    public ArrayList<TouchPoint> findMultiTouch3D(DepthData dData, int skip) {
        initWith(dData, skip);

        validPoints = depthData.validPointsMask3D;
        Set<Integer> toVisit = new HashSet<Integer>();
        toVisit.addAll(depthData.validPointsList3D);

        ArrayList<ConnectedComponent> connectedComponents = findTouchComponentsSimpleTouch();
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
            tp.setCreationTime(depthData.timeStamp);
            tp.setPosition(meanProj);
            tp.setPositionKinect(meanKinect);
            tp.set3D(true);
            tp.setConfidence(connectedComponent.size() / MINIMUM_COMPONENT_SIZE_3D);
            touchPointFounds.add(tp);
        }

        return touchPointFounds;
    }

    private void setDistance(float distance) {
        maxDistance = (distance + NOISE_ESTIMATION) * ERROR_DISTANCE_MULTIPLIER;
    }

    private void checkMemoryAllocation(int size) {
        if (assignedPoints == null && connectedComponentImage == null) {
            assignedPoints = new boolean[size];
            connectedComponentImage = new byte[size];
        }
    }

    private void clearMemory() {
        Arrays.fill(assignedPoints, false);
        Arrays.fill(connectedComponentImage, NO_CONNECTED_COMPONENT);
        currentCompo = STARTING_CONNECTED_COMPONENT;
    }

    public float sideError = 0.2f;

    public boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public boolean isInside(PVector v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }
}
