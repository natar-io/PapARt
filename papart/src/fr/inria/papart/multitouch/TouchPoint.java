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
import fr.inria.papart.depthcam.DepthDataElement;
import fr.inria.papart.depthcam.DepthPoint;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

// TODO: TrackedTouchPoint ...
// TODO: Filtered TouchPoint ...
/**
 * TouchPoint class for multi-touch tracking.
 *
 * @author jeremy
 */
public class TouchPoint extends DepthPoint {

    public static int count = 0;

    // protected PVector position... in DepthPoint
    private PVector previousPosition = new PVector();
    private PVector speed = new PVector();

    // TODO: Remove this !
    private Vec3D positionKinect;
    private Vec3D previousPositionKinect;
//    private PVector speedKinect = new PVector();
    private ArrayList<DepthDataElement> depthDataElements = new ArrayList<DepthDataElement>();

    private float confidence;
//    public float size;
    private boolean is3D;
    private boolean isCloseToPlane;

    // Tracking related variables
    public static final int NO_ID = -10;
    private static int globalID = 1;
    protected int id = NO_ID;

    // time management
    private int updateTime;
    private int deletionTime;
    private int createTime = -1;

    private boolean toDelete = false;
    public boolean isUpdated = false;

    public int attachedValue = -1;
    public Object attachedObject;

    private TouchDetection detection;

// filtering 
    private OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.2f;
    public static float filterBeta = 8.000f;
    public static final int NO_TIME = -1;
    private int NUMBER_OF_FILTERS = 3;

    public TouchPoint(int id) {
        this();
        this.id = id;
    }

    public TouchPoint() {
        try {
            filters = new OneEuroFilter[NUMBER_OF_FILTERS];
            for (int i = 0; i < NUMBER_OF_FILTERS; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta);
            }
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }
    }

    @Override
    public void setPosition(Vec3D pos) {
        super.setPosition(pos);
        setPreviousPosition();
    }

    @Override
    public void setPosition(PVector pos) {
        super.setPosition(pos);
        setPreviousPosition();
    }

    public float distanceTo(TouchPoint tp) {
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

    private void setPreviousPosition() {
        previousPosition.set(this.position);
    }

    public PVector getPreviousPosition() {
        return this.previousPosition;
    }

    public Vec3D getPreviousPositionVec3D() {
        return new Vec3D(previousPosition.x, previousPosition.y, previousPosition.z);
    }

    public void filter() {
        try {
            position.x = (float) filters[0].filter(position.x);
            position.y = (float) filters[1].filter(position.y);
            position.z = (float) filters[2].filter(position.z);
        } catch (Exception e) {
            System.out.println("OneEuro init Exception. Pay now." + e);
        }
    }

    public boolean updateWith(TouchPoint tp) {
        if (isUpdated || tp.isUpdated) {
            return false;
        }

        assert (this.createTime < tp.createTime);

        // these points are used for update. They will not be used again.
        this.setUpdated(true);
        tp.setUpdated(true);

        // mark the last update as the creation of the other point. 
        this.updateTime = tp.createTime;
        // not deleted soon, TODO: -> need better way
        this.deletionTime = tp.createTime;

        // delete the updating point (keep the existing one)
        tp.toDelete = true;

        updatePosition(tp);

        // TODO: check performance ?!
        updateDepthPoints(tp);

        checkAndSetID();
        filter();
        return true;
    }

    private void checkAndSetID() {
        // The touchPoint gets an ID, it is a grown up now. 
        if (this.id == NO_ID) {
            if (count == 0) {
                globalID = 0;
            }
            this.id = globalID++;
            count++;
        }
    }

    private void updatePosition(TouchPoint tp) {
        // Error checking: never update 3D with non 3D !
        assert (tp.is3D == this.is3D);

        // save the previous position
        previousPosition = position.get();
        previousPositionKinect = positionKinect.copy();

        this.position.set(tp.position);
        this.positionKinect.set(tp.positionKinect);
        this.confidence = tp.confidence;
        this.isCloseToPlane = tp.isCloseToPlane;

        speed.set(this.position);
        speed.sub(this.previousPosition);
    }

    private void updateDepthPoints(TouchPoint tp) {
        this.depthDataElements = tp.getDepthDataElements();
    }

    public boolean isObselete(int currentTime) {
        return (currentTime - updateTime) > this.getDetection().getTrackingForgetTime();
    }

    public boolean isToRemove(int currentTime, int duration) {
        return (currentTime - deletionTime) > duration;
    }

    public int getID() {
        return this.id;
    }

    static final int SHORT_TIME_PERIOD = 200;  // in ms

    public boolean isYoung(int currentTime) {
        int age = getAge(currentTime);
        return age == NO_TIME || age < SHORT_TIME_PERIOD;
    }

    public int getAge(int currentTime) {
        if (this.createTime == NO_TIME) {
            return NO_TIME;
        } else {
            return currentTime - this.createTime;
        }
    }

    public void setCreationTime(int timeStamp) {
        this.createTime = timeStamp;
        this.updateTime = timeStamp;
    }

    public PVector getSpeed() {
        return this.speed;
    }

    public void setDepthDataElements(DepthData depthData, ConnectedComponent connectedComponent) {
        depthDataElements.clear();
        for (Integer i : connectedComponent) {
            depthDataElements.add(depthData.getElement(i));
        }
    }

    public ArrayList<DepthDataElement> getDepthDataElements() {
        return this.depthDataElements;
    }

    protected void setUpdated(boolean updated) {
        this.isUpdated = updated;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
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

    public void delete(int time) {
        this.toDelete = true;
        TouchPoint.count--;
        this.deletionTime = time;
        if (this.attachedObject != null) {
            if (this.attachedObject instanceof TouchPointEventHandler) {
                ((TouchPointEventHandler) this.attachedObject).delete();
            }
        }
    }

    public boolean isToDelete() {
        return this.toDelete;
    }

    public int lastUpdate() {
        return this.updateTime;
    }

    @Override
    public String toString() {
        return "Touch Point, kinect: " + positionKinect + " , proj: " + position + "confidence " + confidence + " ,close to Plane : " + isCloseToPlane;
    }

    Touch touch;

    public boolean hasTouch() {
        return touch != null;
    }

    public void createTouch() {
        touch = new Touch();
        touch.id = this.id;
        touch.touchPoint = this;
    }

    public Touch getTouch() {
        if (touch == null) {
            createTouch();
        }
        touch.id = this.id;
        return touch;
    }

    public void deleteTouch() {
        touch = null;
    }

    public TouchDetection getDetection() {
        return detection;
    }

    protected void setDetection(TouchDetection detection) {
        this.detection = detection;
    }

}
