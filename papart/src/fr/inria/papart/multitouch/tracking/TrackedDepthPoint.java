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

import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.multitouch.ConnectedComponent;
import fr.inria.papart.multitouch.detection.DepthElementList;
import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import java.util.ArrayList;
import toxi.geom.Vec3D;

/**
 * TouchPoint, touch events go through this class. TODO: add event handling !
 *
 * @author Jeremy Laviole
 */
public class TrackedDepthPoint extends TrackedElement {

    private Vec3D positionDepthCam;
    private Vec3D previousPositionDepthCam;

    private DepthElementList depthDataElements = new DepthElementList();
    private DepthElementList selectDataElements = new DepthElementList();
    int pointColor;

    private boolean is3D;
    private boolean isCloseToPlane;

    private static final int NO_HAND = -1;
    private int attachedHandID = NO_HAND;
    private boolean isHand = false;

    
    private TrackedDepthPoint parent;

    public void setParent(TrackedDepthPoint p){
        parent = p;
    }
    public TrackedDepthPoint getParent(){
        return parent;
    }
    
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

    public void setDepthDataElements(ProjectedDepthData depthData, DepthElementList list) {
        depthDataElements.clear();
        depthDataElements.addAll(list);
    }

    public DepthElementList getDepthDataElements() {
        return this.depthDataElements;
    }

    public ConnectedComponent getDepthDataAsConnectedComponent() {
        return this.depthDataElements.toConnectedComponent();
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

}
