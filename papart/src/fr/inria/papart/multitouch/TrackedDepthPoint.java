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
package fr.inria.papart.multitouch;

import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import fr.inria.papart.depthcam.devices.KinectDepthData;
import fr.inria.papart.depthcam.DepthDataElementKinect;
import fr.inria.papart.depthcam.DepthPoint;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

// TODO: TrackedTouchPoint ...
// TODO: Filtered TouchPoint ...
/**
 * TouchPoint, touch events go through this class. TODO: add event handling !
 *
 * @author Jeremy Laviole
 */
public class TrackedDepthPoint extends TrackedElement {

    private Vec3D positionKinect;
    private Vec3D previousPositionKinect;

    private ArrayList<DepthDataElementKinect> depthDataElements = new ArrayList<DepthDataElementKinect>();
    int pointColor;

    
    private boolean is3D;
    private boolean isCloseToPlane;

    public TrackedDepthPoint(int id) {
        super(id);
    }

    public TrackedDepthPoint() {
        super();
    }

    public float distanceTo(TrackedDepthPoint tp) {
        return this.positionKinect.distanceTo(tp.positionKinect);
    }

    public void setPositionKinect(Vec3D pos) {
        this.positionKinect = new Vec3D(pos);
        this.previousPositionKinect = new Vec3D(pos);
    }

    public Vec3D getPositionKinect() {
        return this.positionKinect;
    }

    public Vec3D getPreviousPositionKinect() {
        return this.previousPositionKinect;
    }

    protected void updateAdditionalElements(TrackedDepthPoint tp) {
        assert (tp.is3D == this.is3D);
        previousPositionKinect = positionKinect.copy();

        this.positionKinect.set(tp.positionKinect);
        this.confidence = tp.confidence;
        this.isCloseToPlane = tp.isCloseToPlane;

        this.depthDataElements = tp.getDepthDataElements();
    }

    public void setDepthDataElements(KinectDepthData depthData, ConnectedComponent connectedComponent) {
        depthDataElements.clear();
        for (Integer i : connectedComponent) {
            depthDataElements.add(depthData.getElementKinect(i));
        }
    }

    public ArrayList<DepthDataElementKinect> getDepthDataElements() {
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

    @Override
    public String toString() {
        return "Touch Point, kinect: " + positionKinect + " , proj: " + position + "confidence " + confidence + " ,close to Plane : " + isCloseToPlane;
    }


}
