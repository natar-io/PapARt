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

import fr.inria.papart.calibration.PlanarTouchCalibration;
import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.depthcam.KinectDepthData;
import fr.inria.papart.depthcam.DepthAnalysis;
import fr.inria.papart.depthcam.DepthData;
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

    // Variable parameters... going to a specific class for saving.  
    protected PlanarTouchCalibration calib;

    protected boolean[] assignedPoints = null;
    protected byte[] connectedComponentImage = null;

    protected final byte NO_CONNECTED_COMPONENT = 0;
    protected final byte STARTING_CONNECTED_COMPONENT = 1;

    protected byte currentCompo = STARTING_CONNECTED_COMPONENT;

// set by calling function
    protected KinectDepthData depthData;

    protected final HashSet<Integer> toVisit = new HashSet<>();
    protected PointValidityCondition currentPointValidityCondition;

    public interface PointValidityCondition {

        public boolean checkPoint(int offset, int currentPoint);
    }

    public TouchDetection(int size) {
        allocateMemory(size);
    }

    public abstract ArrayList<TouchPoint> compute(KinectDepthData dData);

    protected void allocateMemory(int size) {
        assignedPoints = new boolean[size];
        connectedComponentImage = new byte[size];
        this.calib = new PlanarTouchCalibration();
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
            if (connectedComponent.size() < calib.getMinimumComponentSize()
                    || height < calib.getMinimumHeight()) {

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

    protected int searchDepth;
    protected int precision;

    // TODO: chec if currentCompo ++ is relevent. 
    protected ConnectedComponent findConnectedComponent(int startingPoint) {

        // searchDepth is by precision steps. 
        searchDepth = calib.getSearchDepth() * calib.getPrecision();
        precision = calib.getPrecision();

        ConnectedComponent cc = findNeighboursRec(startingPoint, 0);
               cc.setId(currentCompo);
        currentCompo++;
        return cc;
    }

    private void addPointInConnectedComponent(ConnectedComponent cc, int point) {
        assignedPoints[point] = true;
        connectedComponentImage[point] = currentCompo;
        toVisit.remove(point);
        cc.add(point);
    }

    public ConnectedComponent findNeighboursRec(int currentPoint, int recLevel) {

        // TODO: optimisations here ?
        int w = depthData.source.getWidth();
        int h = depthData.source.getHeight();

        int x = currentPoint % w;
        int y = currentPoint / w;

        ConnectedComponent neighbourList = new ConnectedComponent();
        ArrayList<Integer> visitNext = new ArrayList<Integer>();

        // At least one point in connected compo !
        if(recLevel == 0){
            addPointInConnectedComponent(neighbourList, currentPoint);
        }
            
        if (recLevel == calib.getMaximumRecursion()) {
            addPointInConnectedComponent(neighbourList, currentPoint);
            return neighbourList;
        }

        int minX = PApplet.constrain(x - searchDepth, 0, w - 1);
        int maxX = PApplet.constrain(x + searchDepth, 0, w - 1);
        int minY = PApplet.constrain(y - searchDepth, 0, h - 1);
        int maxY = PApplet.constrain(y + searchDepth, 0, h - 1);

        for (int j = minY; j <= maxY; j += precision) {
            for (int i = minX; i <= maxX; i += precision) {
                int offset = j * w + i;

                // Avoid getting ouside the limits
                if (currentPointValidityCondition.checkPoint(offset, currentPoint)) {

//                    assignedPoints[offset] = true;
//                    connectedComponentImage[offset] = currentCompo;

                    // Remove If present -> it might not be the case often. 
//                    toVisit.remove(offset);
                    
                    addPointInConnectedComponent(neighbourList, currentPoint);
                    
                    neighbourList.add((Integer) offset);
                    
                    // It will be visited, 
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
        Vec3D meanKinect = connectedComponent.getMean(depthData.depthPoints);
        TouchPoint tp = new TouchPoint();
        tp.setDetection(this);
        tp.setPosition(meanProj);
        tp.setPositionKinect(meanKinect);
        tp.setCreationTime(depthData.timeStamp);
        tp.set3D(false);
        tp.setConfidence(connectedComponent.size() / calib.getMinimumComponentSize());

        // TODO: re-enable this one day ?
//        tp.setConnectedComponent(connectedComponent);
        tp.setDepthDataElements(depthData, connectedComponent);
        return tp;
    }

    public float ERROR_DISTANCE_MULTIPLIER = 1.3f;
    public float NOISE_ESTIMATION = 1.5f; // in millimeter. 

    @Deprecated
    protected void setPrecisionFrom(int firstPoint) {

        Vec3D currentPoint = depthData.depthPoints[firstPoint];
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

    @Deprecated
    protected void setDistance(float distance) {
        calib.setMaximumDistance((distance + NOISE_ESTIMATION) * ERROR_DISTANCE_MULTIPLIER);
    }

    public float sideError = 0.2f;

    public boolean isInside(Vec3D v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public boolean isInside(PVector v, float min, float max) {
        return v.x > min - sideError && v.x < max + sideError && v.y < max + sideError && v.y > min - sideError;
    }

    public void setCalibration(PlanarTouchCalibration calibration) {
//        this.calib.setTo(calibration);
        this.calib = calibration;
    }

    public PlanarTouchCalibration getCalibration() {
        return this.calib;
    }

    public int getPrecision() {
        return calib.getPrecision();
    }

    public int getTrackingForgetTime() {
        return calib.getTrackingForgetTime();
    }

    float getTrackingMaxDistance() {
        return calib.getTrackingMaxDistance();
    }

    public class CheckTouchPoint implements PointValidityCondition {

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && depthData.validPointsMask[offset] // is valid
                    && (depthData.depthPoints[offset] != DepthAnalysis.INVALID_POINT) // not invalid point (invalid depth)
                    && distanceToCurrent < calib.getMaximumDistance();
        }
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

        @Override
        public int compare(Object tp1, Object tp2) {

            Vec3D pos1 = projPoints[(Integer) tp1];
            Vec3D pos2 = projPoints[(Integer) tp2];
            if (pos1.y < pos2.y) {
                return 1;
            }
            if(pos1.y == pos2.y)
                return 0;
            
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
            if(d1 == d2)
                return 0;
            
            return -1;
        }
    }

}
