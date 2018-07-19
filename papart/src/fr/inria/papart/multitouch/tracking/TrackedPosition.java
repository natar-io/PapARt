/*
 * Part of the PapARt project - htpts://project.inria.fr/papart/
 *
 * Copyright (C) 2018 RealityTech
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

import fr.inria.papart.multitouch.OneEuroFilter;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.detection.TouchDetection;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author Jeremy Laviole
 */
public class TrackedPosition implements Trackable{
    
    
    /**
     * Global counter of tracked elements.
     */
    public static int count = 0;

    // protected PVector position... in DepthPoint
    protected PVector position = new PVector();
    protected PVector previousPosition = new PVector();
    protected PVector speed = new PVector();

    // Tracking related variables
    public static final int NO_ID = -10;
    private static int globalID = 1;
//    private static int globalIDTemp = Integer.MIN_VALUE;
    protected int id = NO_ID;

    // time management
    protected int updateTime;
    protected int deletionTime;
    protected int createTime = -1;

    protected boolean toDelete = false;
    public boolean isUpdated = false;

// filtering 
    private OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.02f;
    public static float filterBeta = 0.2000f;
    public static final int NO_TIME = -1;
    protected int NUMBER_OF_FILTERS = 3;
    protected int forgetTime;
    protected float maxDistance;

    /**
     * Create a trackedElement and force an ID to it.
     * @param id 
     */
    public TrackedPosition(int id) {
        this();
        this.id = id;
    }

    /**
     * Create a TrackedElement, a new ID will be assigned to it.
     */
    public TrackedPosition() {
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
    @Override
    public void setPosition(PVector pos) {
        this.position.set(pos);
        setPreviousPosition();
    }

    /**
     * Get the current position of this element.
     *
     * @return
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    public boolean updateWith(TrackedPosition tp) {
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
        
        
        if (tp instanceof TrackedDepthPoint) {
            ((TrackedDepthPoint) this).updateAdditionalElements((TrackedDepthPoint) tp);
        }
        updatePosition(tp);

        // WARNING FILTERING IS NOW DONE OUTSIDE
        return true;
    }

    /**
     * Update the element without an external one. This is called when no good
     * candidate is found. The filtering may set a new position and speed.
     */
    @Override
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
    void checkAndSetID() {
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
    private void updatePosition(TrackedPosition tp) {
        // save the previous position
        previousPosition = position.get();
        this.position.set(tp.position);
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
    @Override
    public boolean isObselete(int currentTime) {
        return (currentTime - updateTime) > forgetTime;
    }
    
    public void setForgetTime(int duration){
        this.forgetTime = duration;
    }

    /**
     * Return true if the element is flagged to be deleted. A trackedElement
     * like this is a "ghost": still here but ready to be removed.
     *
     * @param currentTime
     * @param duration
     * @return
     */
    @Override
    public boolean isToRemove(int currentTime, int duration) {
        return (currentTime - deletionTime) > duration;
    }

    /**
     * Get the current ID.
     *
     * @return the id or NO_ID if its invalid.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void setCreationTime(int timeStamp) {
        this.createTime = timeStamp;
        this.updateTime = timeStamp;
    }

    @Override
    public PVector getSpeed() {
        return this.speed;
    }

    /**
     *
     * @param updated
     */
    public void setUpdated(boolean updated) {
        this.isUpdated = updated;
    }

    /**
     *
     * @return
     */
    public boolean isUpdated() {
        return isUpdated;
    }

    /**
     *
     * @param time
     */
    @Override
    public void delete(int time) {
        this.toDelete = true;
        TrackedElement.teCount--;
        this.deletionTime = time;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isToDelete() {
        return this.toDelete;
    }

    @Override
    public int lastUpdate() {
        return this.updateTime;
    }

    @Override
    public String toString() {
        return "Tracked Element:  position: " + position + ".";
    }

    @Override
    public float distanceTo(Trackable newTp) {
        return distanceTo((TrackedElement) newTp);
    }

    @Override
    public boolean updateWith(Trackable tp) {
        return updateWith((TrackedElement)(tp));
    }

    @Override
    public float getTrackingMaxDistance() {
        return this.maxDistance;
    }
 
    public void setMaxDistance(float dist){
        this.maxDistance = dist;
    }
    
}
