/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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

import fr.inria.papart.calibration.files.PlaneCalibration;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.analysis.DepthAnalysis;
import static fr.inria.papart.depthcam.analysis.DepthAnalysis.INVALID_POINT;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
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
    protected ProjectedDepthData depthData;

    public ProjectedDepthData getDepthData() {
        return depthData;
    }

    public void setDepthData(ProjectedDepthData depthData) {
        this.depthData = depthData;
    }

    public TouchDetectionDepth(WithSize imgSize) {
        super(imgSize);
    }

    public abstract ArrayList<TrackedDepthPoint> compute(ProjectedDepthData dData);

    protected boolean hasCCToFind() {
        return !depthData.validPointsList.isEmpty();
    }

    protected boolean checkNormalFinger(ConnectedComponent cc) {
        boolean highX = false;
        boolean highZ = false;
        boolean highY = false;

        float filter = 0.25f;

        int xOffset = 0;
        int yOffset = 0;
        int zOffset = 0;
        for (Integer offset : cc) {
            Vec3D normal = depthData.normals[offset];
            if (normal != null) {
                if (normal.x > filter) {
                    highX = true;
                    xOffset = offset;
                }
                if (normal.y > filter) {
                    highY = true;
                    yOffset = offset;

                }
                if (normal.z > filter) {
                    highZ = true;
                    zOffset = offset;
                }
            }
        }
        if (xOffset != 0 && yOffset != 0 && zOffset != 0
                && highX && highY && highZ) {
            float d = depthData.depthPoints[xOffset].distanceTo(depthData.depthPoints[zOffset]);
            return d < 20 && d > 3 && highX && highY && highZ;
//        System.out.println(depthData.depthPoints[xOffset].distanceTo(depthData.depthPoints[zOffset]));
        }
        return highX && highY && highZ;
    }

//    protected abstract void setSearchParameters();
    protected ArrayList<TrackedDepthPoint> createTouchPointsFrom(ArrayList<ConnectedComponent> connectedComponents) {
        ArrayList<TrackedDepthPoint> touchPoints = new ArrayList<TrackedDepthPoint>();
        for (ConnectedComponent connectedComponent : connectedComponents) {

            float height = connectedComponent.getHeight(depthData.projectedPoints);
            boolean isFinger = true;
//            boolean isFinger = checkNormalFinger(connectedComponent);
            if (connectedComponent.size() < calib.getMinimumComponentSize()
                    || height < calib.getMinimumHeight()
                    || !isFinger) {

                continue;
            }

            TrackedDepthPoint tp = createTouchPoint(connectedComponent);
            touchPoints.add(tp);
        }
        return touchPoints;
    }

    /**
     * Experimental
     */
    protected void filterTips(ConnectedComponent connectedComponent) {

        connectedComponent.getMean(depthData.projectedPoints);

        ConnectedComponent out = new ConnectedComponent();
        for (Integer i : connectedComponent) {
            Vec3D depthPoint = depthData.depthPoints[i];

            // Look left from the point  : 10mm
            Vec3D left = depthPoint.copy();
            left.add(-10, 0, 0);
            int offsetOut = depthData.projectiveDevice.worldToPixel(left);
//            int x = offsetOut % depthData.projectiveDevice.getWidth(); 
//            int y = offsetOut / depthData.projectiveDevice.getWidth();

            // TODO: check bounds.
            if (depthData.validPointsMask[offsetOut]) {
                out.add(i);
            }
        }
        connectedComponent.clear();
        connectedComponent.addAll(out);
    }

    @Override
    protected TrackedDepthPoint createTouchPoint(ConnectedComponent connectedComponent) {

//        filterTips(connectedComponent);
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

        private int inititalPoint;

        public void setInitalPoint(int offset) {
            this.inititalPoint = offset;
        }

        public ProjectedDepthData getData() {
            return depthData;
        }

        @Override
        public boolean checkPoint(int offset, int currentPoint) {
//            float distanceToCurrent = depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]);

            float dN = (depthData.planeAndProjectionCalibration.getPlane().normal).distanceToSquared(depthData.normals[currentPoint]);
            float d1 = (depthData.planeAndProjectionCalibration.getPlane().getDistanceToPoint(depthData.depthPoints[currentPoint]));
//            System.out.println("d1: " + d1 + " dN: " + dN);
//TODO: Magic numbers !!
            boolean goodNormal = (depthData.normals[offset] != null && dN > 3f) || (d1 > 8f);  // Higher  than Xmm
            return !assignedPoints[offset] // not assigned   
                    //                    && depthData.validPointsMask[offset] // is valid
                    //                    && depthData.depthPoints[offset] != INVALID_POINT // is valid
                    //                    && depthData.depthPoints[offset].distanceTo(INVALID_POINT) >= 0.01f //  TODO WHY invalidpoints here.
                    //                    && DepthAnalysis.isValidPoint(depthData.depthPoints[offset])

                    && depthData.depthPoints[inititalPoint].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistanceInit()
                    && depthData.depthPoints[offset].distanceTo(depthData.depthPoints[currentPoint]) < calib.getMaximumDistance()
                    && goodNormal;
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
