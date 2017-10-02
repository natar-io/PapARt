/*
 * Part of the PapARt project - htpts://project.inria.fr/papart/
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
 * <htpt://www.gnu.org/licenses/>.
 */
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.depthcam.DepthData.DepthSelection;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.Touch;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import toxi.geom.Plane;
import toxi.geom.Vec3D;

/**
 * TouchPoint, touch events go through this class. TODO: add event handling !
 *
 * @author Jeremy Laviole
 */
public class TrackedDepthPoint extends TrackedElement {

    private Vec3D positionDepthCam;
    private Vec3D previousPositionDepthCam;

    private ArrayList<DepthDataElementProjected> depthDataElements = new ArrayList<DepthDataElementProjected>();
    private ArrayList<DepthDataElementProjected> selectDataElements = new ArrayList<DepthDataElementProjected>();
    int pointColor;

    private boolean is3D;
    private boolean isCloseToPlane;

    private static final int NO_HAND = -1;
    private int attachedHandID = NO_HAND;
    private boolean isHand = false;

    public TrackedDepthPoint(int id) {
        super(id);
    }

    public TrackedDepthPoint() {
        super();
    }

    public float distanceTo(TrackedDepthPoint pt) {
        return this.positionDepthCam.distanceTo(pt.positionDepthCam);
    }

    public void setPositionKinect(Vec3D pos) {
        this.positionDepthCam = new Vec3D(pos);
        this.previousPositionDepthCam = new Vec3D(pos);
    }

    public Vec3D getPositionKinect() {
        return this.positionDepthCam;
    }

    public Vec3D getPreviousPositionKinect() {
        return this.previousPositionDepthCam;
    }

    public void setPositionDepthCam(Vec3D pos) {
        this.positionDepthCam = new Vec3D(pos);
        this.previousPositionDepthCam = new Vec3D(pos);
    }

    public Vec3D getPositionDepthCam() {
        return this.positionDepthCam;
    }

    public Vec3D getPreviousPositionDepthCam() {
        return this.previousPositionDepthCam;
    }

    protected void updateAdditionalElements(TrackedDepthPoint pt) {
        assert (pt.is3D == this.is3D);
        previousPositionDepthCam = positionDepthCam.copy();

        this.positionDepthCam.set(pt.positionDepthCam);
        this.confidence = pt.confidence;
        this.isCloseToPlane = pt.isCloseToPlane;

        this.depthDataElements = pt.getDepthDataElements();
    }

    public static int numberOfCommonElements(TrackedDepthPoint first,
            TrackedDepthPoint second) {

        ArrayList<Integer> firstOffsets = new ArrayList<>();
        for (DepthDataElementProjected depthPoint : first.depthDataElements) {
            int offset = depthPoint.offset;
            firstOffsets.add(offset);
        }

        ArrayList<Integer> secondOffsets = new ArrayList<>();
        for (DepthDataElementProjected depthPoint : second.depthDataElements) {
            int offset = depthPoint.offset;
            secondOffsets.add(offset);
        }

        secondOffsets.retainAll(firstOffsets);
        return secondOffsets.size();
    }

    public void setDepthDataElements(ProjectedDepthData depthData, ConnectedComponent connectedComponent) {
        depthDataElements.clear();
        for (Integer i : connectedComponent) {
            depthDataElements.add(depthData.getDepthElement(i));
        }
    }

    public ArrayList<DepthDataElementProjected> getDepthDataElements() {
        return this.depthDataElements;
    }

    public ConnectedComponent getDepthDataAsConnectedComponent() {
        return ListToCC(this.depthDataElements);
    }

    public static ConnectedComponent ListToCC(List<DepthDataElementProjected> list) {
        ConnectedComponent cc = new ConnectedComponent();
        for (DepthDataElementProjected depthPoint : list) {
            cc.add(depthPoint.offset);
        }
        return cc;
    }

    public boolean is3D() {
        return is3D;
    }

    public void set3D(boolean is3D) {
        this.is3D = is3D;
    }

    public boolean isCloseToPlane() {
        return isCloseToPlane;
    }

    public void setCloseToPlane(boolean isCloseToPlane) {
        this.isCloseToPlane = isCloseToPlane;
    }

    public int getColor() {
        return pointColor;
    }

    public void setColor(int pointColor) {
        this.pointColor = pointColor;
    }

    public int getAttachedHandID() {
        return attachedHandID;
    }

    public void setAttachedHandID(int attachedHandID) {
        this.attachedHandID = attachedHandID;
    }

    public boolean isHand() {
        return isHand;
    }

    public void setHand(boolean isHand) {
        this.isHand = isHand;
    }

    private ArrayList<Integer> fingerIDs = new ArrayList<>();

    public void addFinger(int id) {
        fingerIDs.add(id);
    }

    public void clearFingers() {
        fingerIDs.clear();
    }

    public ArrayList<Integer> getFingers() {
        return fingerIDs;
    }

    @Override
    public String toString() {
        return "Touch Point, depth: " + positionDepthCam + " , proj: " + position + "confidence " + confidence + " ,close to Plane : " + isCloseToPlane;
    }

    /**
     * Select the points the most away from another tracked depth point
     *
     * @param handPos
     * @param depthData
     */
    public void refineTouchWithHand(Vec3D handPos, ProjectedDepthData depthData) {
        ArrayList<DepthDataElementProjected> copy = new ArrayList<>();
        copy.addAll(getDepthDataElements());
        copy.sort(new DistanceComparator(handPos));

        int size = 8;
        if (copy.size() < size) {
            size = copy.size();
        }
        // Keep the last 10 points.
        List<DepthDataElementProjected> subList = copy.subList(0, size);

        ConnectedComponent connectedComponent = ListToCC(subList);
        Vec3D meanProj, meanDepthCam;
        meanProj = connectedComponent.getMean(depthData.projectedPoints);
        meanDepthCam = connectedComponent.getMean(depthData.depthPoints);

        setPosition(meanProj);
        setPositionKinect(meanDepthCam);
        setCreationTime(depthData.timeStamp);

        setDepthDataElements(depthData, connectedComponent);
//        System.out.println("Points : " + connectedComponent.size());
    }

    public void removeElementsAwayFromTable(
            ProjectedDepthData depthData,
            Plane plane, int amount) {

        ArrayList<DepthDataElementProjected> copy = new ArrayList<>();
        copy.addAll(getDepthDataElements());
        copy.sort(new DistanceToPlaneComparator(plane));

        int size = amount;
        if (copy.size() < size) {
            size = copy.size();
        }
        try {
            List<DepthDataElementProjected> subList = copy.subList(0, size);
            ConnectedComponent connectedComponent = ListToCC(subList);
            setDepthDataElements(depthData, connectedComponent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<DepthDataElementProjected> removeElementsAwayFromCenterNumber(
            ProjectedDepthData depthData, int amount) {
        ArrayList<DepthDataElementProjected> noCenter = new ArrayList<>();
        noCenter.addAll(getDepthDataElements());
        // X filtering.

        this.depthDataElements.sort(new DistanceComparator(this.getPositionDepthCam()));
//        noCenter.sort(new DistanceComparator(this.getPositionDepthCam()));

        int size = amount;
        if (this.getDepthDataElements().size() < size) {
            size = this.getDepthDataElements().size();
        }
        // X points closer
//        List<DepthDataElementProjected> subList = noCenter.subList(0, size);
        setDepthDataElements(depthData, ListToCC(depthDataElements.subList(0, size)));
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

    public ArrayList<DepthDataElementProjected> removeElementsAwayFromCenterDist(
            ProjectedDepthData depthData,
            float dist) {
//        ArrayList<DepthDataElementProjected> noCenter = new ArrayList<>();
//        noCenter.addAll(getDepthDataElements());
        // X filtering.

//        this.depthDataElements.sort(new DistanceComparator(this.getPositionDepthCam()));
//        noCenter.sort(new DistanceComparator(this.getPositionDepthCam()));

        // Remove from a distance
        Iterator<DepthDataElementProjected> it = depthDataElements.iterator();
//        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.depthPoint.distanceTo(this.getPositionDepthCam()) < dist) {
                it.remove();
            }
        }
        return null;
    }
    
    public ArrayList<DepthDataElementProjected> removeElementsAwayFromCenterDist(
            ProjectedDepthData depthData,
            DepthSelection depthSelection,
            float dist) {
        ArrayList<DepthDataElementProjected> output = new ArrayList<>();
//        noCenter.addAll(getDepthDataElements());
        // X filtering.

//        this.depthDataElements.sort(new DistanceComparator(this.getPositionDepthCam()));
//        noCenter.sort(new DistanceComparator(this.getPositionDepthCam()));

        // Remove from a distance
        Iterator<DepthDataElementProjected> it = depthDataElements.iterator();
//        Iterator<DepthDataElementProjected> it = noCenter.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.depthPoint.distanceTo(this.getPositionDepthCam()) < dist) {
                // flag the point as invalid
                depthSelection.validPointsMask[dde.offset] = false;
                output.add(dde);
//                it.remove();
            }
        }
        return output;
    }

    public boolean refineTouchAlongNormal(ProjectedDepthData depthData) {
        ArrayList<DepthDataElementProjected> dx = new ArrayList<>();
        dx.addAll(getDepthDataElements());
        // X filtering.

        float normalThreshold = 0.3f;
        int numberRequired = 6;

        Iterator<DepthDataElementProjected> it = dx.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();
            if (dde.normal != null) {
                if (!(dde.normal.x() > normalThreshold
                        || dde.normal.x() < -normalThreshold)) {
                    it.remove();
                }
            }
        }

        ArrayList<DepthDataElementProjected> dy = new ArrayList<>();
        dy.addAll(getDepthDataElements());

        // Y filtering.
        it = dy.iterator();
        while (it.hasNext()) {
            DepthDataElementProjected dde = it.next();

            if (dde.normal != null) {
                if (!(dde.normal.y() > normalThreshold
                        || dde.normal.y() < -normalThreshold)) {
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
            connectedComponent = ListToCC(dx);
        } else {
            if (dy.size() > dx.size()) {
                connectedComponent = ListToCC(dy);
            } else {
                connectedComponent = ListToCC(dx);
                connectedComponent.addAll(ListToCC(dy));
            }
        }

//        this.mainFinger = true;
        Vec3D meanProj, meanDepthCam;
        meanProj = connectedComponent.getMean(depthData.projectedPoints);
        meanDepthCam = connectedComponent.getMean(depthData.depthPoints);
        setPosition(meanProj);
        setPositionKinect(meanDepthCam);
        setCreationTime(depthData.timeStamp);
        setDepthDataElements(depthData, connectedComponent);
//        System.out.println("Points : " + connectedComponent.size()); }
        return true;
    }

    public void setDepthDataElements(ProjectedDepthData depthData, 
            ArrayList<DepthDataElementProjected> removeElementsAwayFromCenterDist) {
        setDepthDataElements(depthData, ListToCC(depthDataElements));
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
