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
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.calibration.PlaneCalibration;
import fr.inria.papart.depthcam.devices.KinectDepthData;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.TrackedDepthPoint;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import java.util.Comparator;
import processing.core.PApplet;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole laviole@rea.lity.tech
 */
public abstract class TouchDetectionDepth extends TouchDetection {

// set by calling function
    protected KinectDepthData depthData;

    public KinectDepthData getDepthData() {
        return depthData;
    }

    public void setDepthData(KinectDepthData depthData) {
        this.depthData = depthData;
    }

    public TouchDetectionDepth(WithSize imgSize) {
        super(imgSize);
    }

    public abstract ArrayList<TrackedDepthPoint> compute(KinectDepthData dData);

    protected boolean hasCCToFind() {
        return !depthData.validPointsList.isEmpty();
    }

//    protected abstract void setSearchParameters();
    protected ArrayList<TrackedDepthPoint> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TrackedDepthPoint> touchPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

            float height = connectedComponent.getHeight(depthData.projectedPoints);
            if (connectedComponent.size() < calib.getMinimumComponentSize()
                    || height < calib.getMinimumHeight()) {

                continue;
            }

            TrackedDepthPoint tp = createTouchPoint(connectedComponent);
            touchPoints.add(tp);
        }
        return touchPoints;
    }

    @Override
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {
        Vec3D meanProj = connectedComponent.getMean(depthData.projectedPoints);
        Vec3D meanKinect = connectedComponent.getMean(depthData.depthPoints);
        TrackedDepthPoint tp = new TrackedDepthPoint();
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

    public class CheckTouchPoint implements PointValidityCondition {

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
//            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            return !assignedPoints[offset] // not assigned  
                    && depthData.validPointsMask[offset] // is valid
                    && DepthAnalysis.isValidPoint(depthData.depthPoints[offset])
                    && depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance();
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
            if (pos1.y == pos2.y) {
                return 0;
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
            if (d1 < d2) {
                return 1;
            }
            if (d1 == d2) {
                return 0;
            }

            return -1;
        }
    }

}