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

import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.Kinect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole <jeremy.laviole@inria.fr>
 */
public abstract class TouchDetection {

    protected float maxDistance = 10f;    // in mm
    protected float MINIMUM_COMPONENT_SIZE = 3;   // in px
    
    public static int MINIMUM_HEIGHT = 1; // mm
    protected int MAX_REC = 500;

    protected boolean[] assignedPoints = null;
    protected byte[] connectedComponentImage = null;

    protected final byte NO_CONNECTED_COMPONENT = 0;
    protected final byte STARTING_CONNECTED_COMPONENT = 1;

    protected byte currentCompo = STARTING_CONNECTED_COMPONENT;

// set by calling function
    protected DepthData depthData;
    protected int precision;
    protected int searchDepth;

    protected HashSet<Integer> toVisit;
    protected PointValidityCondition currentPointValidityCondition;

    public interface PointValidityCondition {

        public boolean checkPoint(int offset, int currentPoint);
    }

    public TouchDetection(int size) {
        allocateMemory(size);
    }

    public abstract ArrayList<TouchPoint> compute(DepthData dData, int skip);

    protected void allocateMemory(int size) {
        assignedPoints = new boolean[size];
        connectedComponentImage = new byte[size];
    }

    protected void clearMemory() {
        Arrays.fill(assignedPoints, false);
        Arrays.fill(connectedComponentImage, NO_CONNECTED_COMPONENT);
        currentCompo = STARTING_CONNECTED_COMPONENT;
    }

    protected boolean hasCCToFind() {
        return !depthData.validPointsList.isEmpty();
    }

    protected ArrayList<ConnectedComponent> findConnectedComponents() {
        clearMemory();
        setSearchParameters();
        ArrayList<ConnectedComponent> connectedComponents = computeAllConnectedComponents();
        return connectedComponents;
    }

    protected abstract void setSearchParameters();

    protected ArrayList<TouchPoint> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TouchPoint> touchPoints = new ArrayList<TouchPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < MINIMUM_COMPONENT_SIZE
                    || height < MINIMUM_HEIGHT) {

                continue;
            }

            TouchPoint tp = createTouchPoint(connectedComponent);
            touchPoints.add(tp);
        }
        return touchPoints;
    }

    protected ArrayList<ConnectedComponent> computeAllConnectedComponents() {

        ArrayList<ConnectedComponent> connectedComponents = new ArrayList<ConnectedComponent>();

        // recursive search for each component. 
        while (toVisit.size() > 0) {
            int startingPoint = toVisit.iterator().next();
            ConnectedComponent cc = findConnectedComponent(startingPoint);
            connectedComponents.add(cc);
        }
        return connectedComponents;
    }

    // TODO: chec if currentCompo ++ is relevent. 
    protected ConnectedComponent findConnectedComponent(int startingPoint) {
        ConnectedComponent cc = findNeighboursRec(startingPoint, 0);
        cc.setId(currentCompo);
        currentCompo++;
        return cc;
    }

    public ConnectedComponent findNeighboursRec(int currentPoint, int recLevel) {

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
            ConnectedComponent subNeighbours = findNeighboursRec(offset, recLevel + 1);
            neighbourList.addAll(subNeighbours);
        }

        return neighbourList;
    }

    // TODO: use another type here ?
    protected TouchPoint createTouchPoint(ConnectedComponent connectedComponent) {
        Vec3D meanProj = connectedComponent.getMean(depthData.projectedPoints);
        Vec3D meanKinect = connectedComponent.getMean(depthData.kinectPoints);
        TouchPoint tp = new TouchPoint();
        tp.setPosition(meanProj);
        tp.setPositionKinect(meanKinect);
        tp.setCreationTime(depthData.timeStamp);
        tp.set3D(false);

        tp.setConfidence(connectedComponent.size() / MINIMUM_COMPONENT_SIZE);

        // TODO: re-enable this one day ?
//        tp.setConnectedComponent(connectedComponent);
        tp.setDepthDataElements(depthData, connectedComponent);
        return tp;
    }

    public float ERROR_DISTANCE_MULTIPLIER = 1.3f;
    public float NOISE_ESTIMATION = 1.5f; // in millimeter. 

    protected void setPrecisionFrom(int firstPoint) {

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

    protected void setDistance(float distance) {
        maxDistance = (distance + NOISE_ESTIMATION) * ERROR_DISTANCE_MULTIPLIER;
    }

    public float sideError = 0.2f;

    public boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public boolean isInside(PVector v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    class ClosestComparator implements Comparator {

        public Vec3D[] projPoints;

        public ClosestComparator(Vec3D[] proj) {
            projPoints = proj;
        }

        public int compare(Object tp1, Object tp2) {

            Vec3D pos1 = projPoints[(Integer) tp1];
            Vec3D pos2 = projPoints[(Integer) tp2];
            if (pos1.z > pos2.z) {
                return 1;
            }
            return -1;
        }
    }

    class ClosestComparatorY implements Comparator {

        public Vec3D[] projPoints;

        public ClosestComparatorY(Vec3D[] proj) {
            projPoints = proj;
        }

        public int compare(Object tp1, Object tp2) {

            Vec3D pos1 = projPoints[(Integer) tp1];
            Vec3D pos2 = projPoints[(Integer) tp2];
            if (pos1.y < pos2.y) {
                return 1;
            }
            return -1;
        }
    }

    class ClosestComparatorHeight implements Comparator {

        public Vec3D[] points;
        PlaneCalibration calibration;

        public ClosestComparatorHeight(Vec3D points[],
                PlaneCalibration calib) {
            this.points = points;
            this.calibration = calib;
        }

        @Override
        public int compare(Object tp1, Object tp2) {

            float d1 = calibration.getPlane().distanceTo(points[(Integer) tp1]);
            float d2 = calibration.getPlane().distanceTo(points[(Integer) tp2]);
            if (d1 > d2) {
                return 1;
            }
            return -1;
        }
    }

}
