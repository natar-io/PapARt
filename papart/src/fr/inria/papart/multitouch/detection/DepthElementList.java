/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch.detection;

import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 *
 * @author jiii
 */
public class DepthElementList extends ArrayList<DepthDataElementProjected> {

    public ConnectedComponent toConnectedComponent() {
        ConnectedComponent cc = new ConnectedComponent();
        for (DepthDataElementProjected depthPoint : this) {
            cc.add(depthPoint.offset);
        }
        return cc;
    }

    /**
     * Refactoring to do.
     *
     * @param depthData
     * @param dist
     * @param positionDepth
     * @return
     */
    public DepthElementList removeElementsAwayFromCenterDist(ProjectedDepthData depthData,
            float dist,
            Vec3D positionDepth) {
        //        DepthElementList noCenter = new DepthElementList();
        //        noCenter.addAll(getDepthDataElements());
        // X filtering.
        //        this.depthDataElements.sort(new DistanceComparator(this.getPositionDepthCam()));
        //        noCenter.sort(new DistanceComparator(this.getPositionDepthCam()));
        // Remove from a distance
        Iterator<DepthDataElementProjected> it = this.iterator();
        //        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.depthPoint.distanceTo(positionDepth) < dist) {
                it.remove();
            }
        }
        return null;
    }

    /**
     * Refactoring to do.
     *
     * @param depthData
     * @param depthSelection
     * @param dist
     * @param positionDepth
     * @return
     */
    public DepthElementList removeElementsAwayFromCenterDist(ProjectedDepthData depthData,
            DepthData.DepthSelection depthSelection,
            float dist, Vec3D positionDepth) {
        DepthElementList output = new DepthElementList();
        //        noCenter.addAll(getDepthDataElements());
        // X filtering.
        //        this.depthDataElements.sort(new DistanceComparator(this.getPositionDepthCam()));
        //        noCenter.sort(new DistanceComparator(this.getPositionDepthCam()));
        // Remove from a distance
        Iterator<DepthDataElementProjected> it = this.iterator();
        //        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.depthPoint.distanceTo(positionDepth) < dist) {
                // flag the point as invalid
                depthSelection.validPointsMask[dde.offset] = false;
                output.add(dde);
                //                it.remove();
            }
        }
        return output;
    }

    public DepthElementList getBoundaries(ProjectedDepthData depthData, TouchDetection detect) {
        DepthElementList out = new DepthElementList();
        // Remove from a distance
        Iterator<DepthDataElementProjected> it = this.iterator();
        //        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (detect.getBoundaries()[dde.offset]) {
                out.add(dde);
            }
        }
        return out;
    }

    /**
     * Select the points the most away from another tracked depth point.
     *
     * @param handPos
     * @param depthData
     * @param trackedDepthPoint
     */
    public void refineTouchWithHand(Vec3D handPos, ProjectedDepthData depthData, TrackedDepthPoint trackedDepthPoint) {
        DepthElementList copy = new DepthElementList();
        copy.addAll(this);
        copy.sort(new DistanceComparator(handPos));
        int size = 8;
        if (copy.size() < size) {
            size = copy.size();
        }
        // Keep the last 10 points.
        DepthElementList subList = (DepthElementList) copy.subList(0, size);
        ConnectedComponent connectedComponent = subList.toConnectedComponent();
        Vec3D meanProj;
        Vec3D meanDepthCam;
        meanProj = connectedComponent.getMean(depthData.projectedPoints);
        meanDepthCam = connectedComponent.getMean(depthData.depthPoints);
        trackedDepthPoint.setPosition(meanProj);
        trackedDepthPoint.setPositionKinect(meanDepthCam);
        trackedDepthPoint.setCreationTime(depthData.timeStamp);
        trackedDepthPoint.setDepthDataElements(depthData, connectedComponent);
        //        System.out.println("Points : " + connectedComponent.size());
    }

    /**
     * Refactoring to do.
     *
     * @param depthData
     * @param plane
     * @param amount
     * @param trackedDepthPoint
     */
    public void removeElementsAwayFromTable(ProjectedDepthData depthData, Plane plane, int amount, TrackedDepthPoint trackedDepthPoint) {
        DepthElementList copy = new DepthElementList();
        copy.addAll(this);
        copy.sort(new DistanceToPlaneComparator(plane));
        int size = amount;
        if (copy.size() < size) {
            size = copy.size();
        }
        try {
            DepthElementList subList = (DepthElementList) copy.subList(0, size);
            ConnectedComponent connectedComponent = subList.toConnectedComponent();
            trackedDepthPoint.setDepthDataElements(depthData, connectedComponent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Refactoring to do.
     *
     * @param depthData
     * @param detect
     * @param trackedDepthPoint
     */
    public void removeNonBoundaries(ProjectedDepthData depthData, TouchDetectionDepth detect, TrackedDepthPoint trackedDepthPoint) {
        // Remove from a distance
        Iterator<DepthDataElementProjected> it = trackedDepthPoint.getDepthDataElements().iterator();
        //        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (!detect.getBoundaries()[dde.offset]) {
                it.remove();
            }
        }
    }

    /**
     * Refactoring to do.
     *
     * @param depthData
     * @param trackedDepthPoint
     * @return
     */
    public boolean refineTouchAlongNormal(ProjectedDepthData depthData, TrackedDepthPoint trackedDepthPoint) {
        DepthElementList dx = new DepthElementList();
        dx.addAll(trackedDepthPoint.getDepthDataElements());
        // X filtering.
        float normalThreshold = 0.3F;
        int numberRequired = 6;
        Iterator<DepthDataElementProjected> it = dx.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.normal != null) {
                if (!(dde.normal.x() > normalThreshold || dde.normal.x() < -normalThreshold)) {
                    it.remove();
                }
            }
        }
        DepthElementList dy = new DepthElementList();
        dy.addAll(trackedDepthPoint.getDepthDataElements());
        // Y filtering.
        it = dy.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.normal != null) {
                if (!(dde.normal.y() > normalThreshold || dde.normal.y() < -normalThreshold)) {
                    it.remove();
                }
            }
            //            if (dde.normal == null
            //                    // TODO: MAGIC NUMBER
            //                    // the normal is the orientation relative to the underlying plane.
            //                    || dde.normal.y() < normalThreshold
            //                    || dde.normal.y() > normalThreshold) {
            //                it.remove();
            //            }
        }
        //        System.out.println("Copy size: " + copy.size());
        // WARNING: MAGIC NUMBER.
        if (dx.size() < numberRequired || dy.size() < numberRequired) {
            //            this.mainFinger = false;
            return false;
        }
        ConnectedComponent connectedComponent;
        if (dx.size() > dy.size()) {
            connectedComponent = dx.toConnectedComponent();
        } else {
            if (dy.size() > dx.size()) {
                connectedComponent = dy.toConnectedComponent();
            } else {
                connectedComponent = dx.toConnectedComponent();
                connectedComponent.addAll(dy.toConnectedComponent());
            }
        }
        //        this.mainFinger = true;
        Vec3D meanProj;
        Vec3D meanDepthCam;
        meanProj = connectedComponent.getMean(depthData.projectedPoints);
        meanDepthCam = connectedComponent.getMean(depthData.depthPoints);
        trackedDepthPoint.setPosition(meanProj);
        trackedDepthPoint.setPositionKinect(meanDepthCam);
        trackedDepthPoint.setCreationTime(depthData.timeStamp);
        trackedDepthPoint.setDepthDataElements(depthData, connectedComponent);
        //        System.out.println("Points : " + connectedComponent.size()); }
        return true;
    }

    /**
     * Refactoring to do.
     *
     * @param depthData
     * @param amount
     * @param trackedDepthPoint
     * @return
     */
    public DepthElementList removeElementsAwayFromCenterNumber(ProjectedDepthData depthData, int amount, TrackedDepthPoint trackedDepthPoint) {
        DepthElementList noCenter = new DepthElementList();
        noCenter.addAll(trackedDepthPoint.getDepthDataElements());
        // X filtering.
        trackedDepthPoint.getDepthDataElements().sort(new DistanceComparator(trackedDepthPoint.getPositionDepthCam()));
        //        noCenter.sort(new DistanceComparator(this.getPositionDepthCam()));
        int size = amount;
        if (trackedDepthPoint.getDepthDataElements().size() < size) {
            size = trackedDepthPoint.getDepthDataElements().size();
        }
        // X points closer
        //        List<DepthDataElementProjected> subList = noCenter.subList(0, size);

        DepthElementList subList = (DepthElementList) trackedDepthPoint.getDepthDataElements().subList(0, size);

        trackedDepthPoint.setDepthDataElements(depthData, subList.toConnectedComponent());

        //        ConnectedComponent connectedComponent = ListToCC(subList);
        //               setDepthDataElements(depthData, connectedComponent);
        // Remove from a distance
        //        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        //        while (it.hasNext()) {
        //            DepthDataElementProjected dde = it.next();
        //            if(dde.depthPoint.distanceTo(this.getPositionDepthCam()) < distance){
        //                    it.remove();
        //            }
        //        }
        return noCenter;
    }

    public void selectDark(ProjectedDepthData depthData) {

        Iterator<DepthDataElementProjected> it = this.iterator();
        //        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            
            int argb = depthData.pointColors[dde.offset];
            int r = (argb >> 16) & 0xFF;  // Faster way of getting red(argb)
            int g = (argb >> 8) & 0xFF;   // Faster way of getting green(argb)
            int b = argb & 0xFF;          // Faster way of getting blue(argb)
            int total = r + g + b;
            if (total != 0) {
                it.remove();
            }
        }
    }

    class DistanceComparator implements Comparator {

        private final Vec3D position;

        public DistanceComparator(Vec3D initialObject) {
            this.position = initialObject.copy();
        }

        @Override
        public int compare(Object pos0, Object pos1) {
            DepthDataElementProjected t0 = (DepthDataElementProjected) pos0;
            DepthDataElementProjected t1 = (DepthDataElementProjected) pos1;

            float d0 = t0.depthPoint.distanceTo(position);
            float d1 = t1.depthPoint.distanceTo(position);
            return Float.compare(d1, d0);
        }
    }

    class DistanceToPlaneComparator implements Comparator {

        private final Plane plane;

        public DistanceToPlaneComparator(Plane initialObject) {
            this.plane = initialObject;
        }

        @Override
        public int compare(Object pos0, Object pos1) {
            DepthDataElementProjected t0 = (DepthDataElementProjected) pos0;
            DepthDataElementProjected t1 = (DepthDataElementProjected) pos1;

            float d0 = plane.getDistanceToPoint(t0.depthPoint);
            float d1 = plane.getDistanceToPoint(t1.depthPoint);
            return Float.compare(d0, d1);
        }
    }

}
