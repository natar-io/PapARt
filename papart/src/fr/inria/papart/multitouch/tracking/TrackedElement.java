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

import fr.inria.papart.multitouch.OneEuroFilter;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.detection.TouchDetection;
import fr.inria.papart.multitouch.detection.TouchDetectionDepth;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * Tracked element with a position and speed. The position can be filtered, the 
 * filtering must be called directly, it is not automatic.
 * @author Jeremy Laviole
 */
public class TrackedElement {

    /**
     * Global counter of tracked elements.
     */
    public static int count = 0;

    // protected PVector position... in DepthPoint
    protected PVector position = new PVector();
    protected PVector previousPosition = new PVector();
    protected PVector speed = new PVector();

    // TODO: become this:
//    protected Object source;

    /**
     * You can attach a value to a Tracked Element, it will be passed along tracking.
     */
    public int attachedValue = -1;
        /**
     * You can attach an object to a Tracked Element, it will be passed along tracking.
     */
    public Object attachedObject;

    protected float confidence;
//    public float size;

    // Tracking related variables
    public static final int NO_ID = -10;
    private static int globalID = 1;
//    private static int globalIDTemp = Integer.MIN_VALUE;
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
    protected final Touch touch;

// filtering 
    private OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.02f;
    public static float filterBeta = 0.2000f;
    public static final int NO_TIME = -1;
    private int NUMBER_OF_FILTERS = 3;

    /**
     * Create a trackedElement and force an ID to it.
     * @param id 
     */
    public TrackedElement(int id) {
        this();
        this.id = id;
        initTouch();
    }

    /**
     * Create a TrackedElement, a new ID will be assigned to it.
     */
    public TrackedElement() {
        try {
            filters = new OneEuroFilter[NUMBER_OF_FILTERS];
            for (int i = 0; i < NUMBER_OF_FILTERS; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta, 0.5f);
            }
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }
        
//        // In test: global ID also for temporary values
//        this.id = globalIDTemp++; 
//        if(globalIDTemp == NO_ID){
//            globalIDTemp = Integer.MIN_VALUE;
//        }
        
        
        touch = new Touch();
        initTouch();
    }

    private void initTouch() {
        touch.id = this.id;
        touch.trackedSource = this;
    }

    /**
     * DistanceTo is used to compare Points !
     *
     * @param newTp
     * @return
     */
    public float distanceTo(TrackedElement newTp) {
        return this.position.dist(newTp.position);
    }

    /**
     * Set a new position to this tracked element, it will save the past
     * location and compute the speed.
     *
     * @param pos
     */
    public void setPosition(Vec3D pos) {
        this.position.set(pos.x, pos.y, pos.z);
        setPreviousPosition();
    }

    /**
     * Set a new position to this tracked element, it will save the past
     * location and compute the speed.
     *
     * @param pos
     */
    public void setPosition(PVector pos) {
        this.position.set(pos);
        setPreviousPosition();
    }

    /**
     * Get the current position of this element.
     *
     * @return
     */
    public PVector getPosition() {
        return this.position;
    }

    /**
     * Get the current position of this element.
     *
     * @return
     */
    public Vec3D getPositionVec3D() {
        return new Vec3D(position.x, position.y, position.z);
    }

    private void setPreviousPosition() {
        previousPosition.set(this.position);
    }

    /**
     * Get the current previous position of this element.
     *
     * @return
     */
    public PVector getPreviousPosition() {
        return this.previousPosition;
    }

    /**
     * Get the current previous position of this element.
     *
     * @return
     */
    public Vec3D getPreviousPositionVec3D() {
        return new Vec3D(previousPosition.x, previousPosition.y, previousPosition.z);
    }

    /**
     * Use the OneEuroFilter to filter the position.
     */
    public void filter() {
        try {
            position.x = (float) filters[0].filter(position.x);
            position.y = (float) filters[1].filter(position.y);
            position.z = (float) filters[2].filter(position.z);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("OneEuro init Exception. Pay now." + e);
        }
    }

    /**
     * Use the OneEuroFilter to filter the position.
     *
     * @param updateTime Time from Processing.
     */
    public void filter(int updateTime) {
        try {
            position.x = (float) filters[0].filter(position.x, updateTime);
            position.y = (float) filters[1].filter(position.y, updateTime);
            position.z = (float) filters[2].filter(position.z, updateTime);
        } catch (Exception e) {
            System.out.println("OneEuro init Exception. Pay now." + e);
        }
    }

    /**
     * Update with an external element. This is used for tracking purposes. The
     * current element is updated with the data of the new one passed in
     * parameter. the new one is to be deleted afterwards.
     *
     * @param tp
     * @return
     */
    public boolean updateWith(TrackedElement tp) {
        if (isUpdated || tp.isUpdated) {
            return false;
        }

        assert (this.createTime <= tp.createTime);

        // these points are used for update. They will not be used again.
        this.setUpdated(true);
        tp.setUpdated(true);

        // mark the last update as the creation of the other point. 
        this.updateTime = tp.createTime;
        // not deleted soon, TODO: -> need better way
        this.deletionTime = tp.createTime;

        // delete the updating point (keep the existing one)
        tp.toDelete = true;

        checkAndSetID();
//        filter(tp.createTime);

        if (tp instanceof TrackedDepthPoint) {
            ((TrackedDepthPoint) this).updateAdditionalElements((TrackedDepthPoint) tp);
        }
        updatePosition(tp);

        // WARNING FILTERING IS NOW DONE OUTSIDE
//        filter();
        return true;
    }

    /**
     * Update the element without an external one. This is called when no good
     * candidate is found. The filtering may set a new position and speed.
     */
    public void updateAlone() {
//        this.setUpdated(true);
        updatePosition(this);
        checkAndSetID();
        // TODO: check performance ?!

//        filter();
    }

    /**
     * Find a proper ID for this tracked Element
     */
    private void checkAndSetID() {
        // The touchPoint gets an ID, it is a grown up now. 
        if (this.id == NO_ID) {
            if (count == 0) {
                globalID = 1;
            }
            this.id = globalID++;
            count++;
        }
    }

    /**
     * Update the position of this element according to the parameter. Updates
     * the position, previous position, confidence and speed.
     *
     * @param tp
     */
    private void updatePosition(TrackedElement tp) {
        // save the previous position
        previousPosition = position.get();
        this.position.set(tp.position);
        this.confidence = tp.confidence;

//        this.setSource(tp.source);

        speed.set(this.position);
        speed.sub(this.previousPosition);
    }

    protected void updateAdditionalElements(TrackedElement tp) {
    }

    /**
     * Find out if the current Element has not been updated for a long time. A
     * long time is given by the detection which created this element with the
     * method getTrackingForgetTime();
     *
     * @param currentTime
     * @return
     */
    public boolean isObselete(int currentTime) {
        return (currentTime - updateTime) > this.getDetection().getTrackingForgetTime();
    }

    /**
     * Return true if the element is flagged to be deleted. A trackedElement
     * like this is a "ghost": still here but ready to be removed.
     *
     * @param currentTime
     * @param duration
     * @return
     */
    public boolean isToRemove(int currentTime, int duration) {
        return (currentTime - deletionTime) > duration;
    }

    /**
     * Get the current ID.
     *
     * @return the id or NO_ID if its invalid.
     */
    public int getID() {
        return this.id;
    }

    /**
     * Force a given ID, to use when it comes from an external tracking.
     *
     * @param id
     */
    public void forceID(int id) {
        this.id = id;
    }

    /**
     * Time limit in ms for a "young" point
     */
    static final int SHORT_TIME_PERIOD = 200;  // in ms

    /**
     * Return true if the TrackedElement has been created recently. Currently a
     * young Element is less than 200ms old.
     *
     * @param currentTime
     * @return
     */
    public boolean isYoung(int currentTime) {
        int age = getAge(currentTime);
        return age == NO_TIME || age < SHORT_TIME_PERIOD;
    }

    /**
     * Get the age from creation in milliseconds.
     *
     * @param currentTime
     * @return
     */
    public int getAge(int currentTime) {
        if (this.createTime == NO_TIME) {
            return NO_TIME;
        } else {
            return currentTime - this.createTime;
        }
    }

    /**
     * Set the time of creation.
     *
     * @param timeStamp
     */
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

    // TODO: find a way to handle in a better way.
    public boolean hasTouch() {
        return touch != null;
    }

    public Touch getTouch() {
        touch.id = this.id;
        return touch;
    }
//
//    public Object getSource() {
//        return source;
//    }
//
//    public void setSource(Object source) {
//        this.source = source;
//    }

    /**
     * Get the detection object that created this tracked element.
     *
     * @return
     */
    public TouchDetection getDetection() {
        return detection;
    }

    /**
     * Set the detection object that created this tracked element.
     *
     * @param detection
     */
    public void setDetection(TouchDetection detection) {
        this.detection = detection;
    }

    public boolean mainFinger = false;

    public void setMainFinger() {
        this.mainFinger = true;
    }

}
