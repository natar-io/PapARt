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
package fr.inria.papart.multitouch.tracking;

import fr.inria.papart.depthcam.devices.ProjectedDepthData;
import fr.inria.papart.depthcam.DepthDataElementProjected;
import fr.inria.papart.multitouch.ConnectedComponent;
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

    private ArrayList<DepthDataElementProjected> depthDataElements = new ArrayList<DepthDataElementProjected>();
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

    public float distanceTo(TrackedDepthPoint tp) {
        return this.positionDepthCam.distanceTo(tp.positionDepthCam);
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

    protected void updateAdditionalElements(TrackedDepthPoint tp) {
        assert (tp.is3D == this.is3D);
        previousPositionDepthCam = positionDepthCam.copy();

        this.positionDepthCam.set(tp.positionDepthCam);
        this.confidence = tp.confidence;
        this.isCloseToPlane = tp.isCloseToPlane;

        this.depthDataElements = tp.getDepthDataElements();
    }

    public void setDepthDataElements(ProjectedDepthData depthData, ConnectedComponent connectedComponent) {
        depthDataElements.clear();
        for (Integer i : connectedComponent) {
            depthDataElements.add(depthData.getElementKinect(i));
        }
    }

    public ArrayList<DepthDataElementProjected> getDepthDataElements() {
        return this.depthDataElements;
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
    public void addFinger(int id){
        fingerIDs.add(id);
    }
    
    public void clearFingers(){
        fingerIDs.clear();
    }
    public ArrayList<Integer> getFingers(){
        return fingerIDs;
    }

    @Override
    public String toString() {
        return "Touch Point, depth: " + positionDepthCam + " , proj: " + position + "confidence " + confidence + " ,close to Plane : " + isCloseToPlane;
    }

    public boolean mainFinger = false;
    public void setMainFinger() {
        this.mainFinger = true;
    }

}
