/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import fr.inria.papart.depthcam.DepthPoint;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 * TouchPoint class for multi-touch tracking.
 *
 * @author jeremy
 */
public class TouchPoint extends DepthPoint {

    // protected PVector position... in DepthPoint
    private PVector previousPosition = new PVector();
    private PVector speed = new PVector();

    private Vec3D positionKinect;
    private Vec3D previousPositionKinect;
//    private PVector speedKinect = new PVector();

    private float confidence;
//    public float size;
    private boolean is3D;
    private boolean isCloseToPlane;

    // Tracking related variables
    private static int globalID = 0;
    protected int id;
    protected int updateTime = 0;
    protected int createTime = -1;
    private boolean isNew;
    private boolean toDelete = false;

    public boolean isUpdated = false;

// filtering 
    private OneEuroFilter[] filters;
    public static float filterFreq = 30f;
    public static float filterCut = 0.2f;
    public static float filterBeta = 8.000f;
    public static final int NO_TIME = -1;

    public TouchPoint() {
        id = globalID++;
        isNew = true;
        try {
            filters = new OneEuroFilter[3];
            for (int i = 0; i < 3; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta);
            }
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }
    }

    @Override
    public void setPosition(Vec3D pos) {
        super.setPosition(pos);
        initPreviousPosition();
    }

    @Override
    public void setPosition(PVector pos) {
        super.setPosition(pos);
        initPreviousPosition();
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

    public Vec3D setPreviousPositionKinect() {
        return this.previousPositionKinect;
    }

    private void initPreviousPosition() {
        if (this.isNew) {
            previousPosition = new PVector();
            previousPosition.set(this.position);
        } else {
            throw new RuntimeException("TouchPoint: position can be set only once.");
        }
    }

    public PVector getPreviousPosition() {
        return this.previousPosition;
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

    public boolean updateWith(TouchPoint tp, int currentTime) {

        if (isUpdated || tp.isUpdated) {
            return false;
        }

        // these points are used for update
        this.setUpdated(true);
        tp.setUpdated(true);

        if (this.createTime == NO_TIME) {
            this.createTime = currentTime;
        }
        tp.updateTime = currentTime;
        this.updateTime = currentTime;

        // delete the updating point (keep the existing one)
        tp.toDelete = true;

        updatePosition(tp);

        filter();
        isNew = false;
        return true;
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

    public PVector getSpeed() {
        return this.speed;
    }

    public boolean isObselete(int currentTime, int duration) {
        return (currentTime - updateTime) > duration;
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

    public void setToDelete() {
        this.toDelete = true;
    }

    public boolean isToDelete() {
        return this.toDelete;
    }

    @Override
    public String toString() {
        return "Touch Point, kinect: " + positionKinect + " , proj: " + position + "confidence " + confidence + " ,close to Plane : " + isCloseToPlane;
    }

}
