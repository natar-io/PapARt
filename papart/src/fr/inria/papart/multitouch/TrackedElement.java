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

import fr.inria.papart.multitouch.detection.TouchDetection;
import fr.inria.papart.depthcam.devices.KinectDepthData;
import fr.inria.papart.depthcam.DepthDataElementKinect;
import fr.inria.papart.depthcam.DepthPoint;
import java.util.ArrayList;
import processing.core.PVector;
import toxi.geom.Vec3D;

// TODO: Filtered TouchPoint ...

/** 
 * TouchPoint, touch events go through this class. 
 * TODO: add event handling !
 * @author Jeremy Laviole
 */
public class TrackedElement  {

    public static int count = 0;

    // protected PVector position... in DepthPoint
    protected PVector position = new PVector();
    protected PVector previousPosition = new PVector();
    protected PVector speed = new PVector();


    // TODO: become this:
    protected Object source;
//    private ArrayList<DepthDataElementKinect> depthDataElements = new ArrayList<DepthDataElementKinect>();
    public int attachedValue = -1;
    public Object attachedObject;

    
    protected float confidence;
//    public float size;
    
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


    
    // Element it originates from.
    protected TouchDetection detection;

    // Element it goes to 
    protected Touch touch;
    
// filtering 
    private OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.2f;
    public static float filterBeta = 8.000f;
    public static final int NO_TIME = -1;
    private int NUMBER_OF_FILTERS = 3;

    public TrackedElement(int id) {
        this();
        this.id = id;
    }

    public TrackedElement() {
        try {
            filters = new OneEuroFilter[NUMBER_OF_FILTERS];
            for (int i = 0; i < NUMBER_OF_FILTERS; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta);
            }
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }
    }

    /**
     * DistanceTo is used to compare Points !
     * @param newTp
     * @return 
     */
    public float distanceTo(TrackedElement newTp) {
        return this.position.dist(newTp.position);
    }

      
    public void setPosition(Vec3D pos) {
        this.position.set(pos.x, pos.y, pos.z);
        setPreviousPosition();
    }

    public void setPosition(PVector pos) {
        this.position.set(pos);
        setPreviousPosition();
    }
    
    public PVector getPosition(){
        return this.position;
    }

    public Vec3D getPositionVec3D(){
        return new Vec3D(position.x, position.y, position.z);
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

    public boolean updateWith(TrackedElement tp) {
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

        // TODO: Call  son methods ?
//        updateDepthPoints(tp);

        checkAndSetID();
        filter();
        return true;
    }
    
    public void updateAlone() {
        updatePosition(this);
        // TODO: check performance ?!
        filter();
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

    private void updatePosition(TrackedElement tp) {
        // save the previous position
        previousPosition = position.get();

        this.position.set(tp.position);
        this.confidence = tp.confidence;

        speed.set(this.position);
        speed.sub(this.previousPosition);
    }
    
    protected void updateAdditionalElements(TrackedElement tp){
        
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

    public void delete(int time) {
        this.toDelete = true;
        TrackedElement.count--;
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
        return "Tracked Element:  position: " + position + ", confidence: " + confidence + ". \n";
    }


    public boolean hasTouch() {
        return touch != null;
    }

    public void createTouch() {
        touch = new Touch();
        touch.id = this.id;
        // 
        touch.trackedSource = this;
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

    /**
     * TODO: Find the use of this?
     * @param detection 
     */
    public void setDetection(TouchDetection detection) {
        this.detection = detection;
    }


}
