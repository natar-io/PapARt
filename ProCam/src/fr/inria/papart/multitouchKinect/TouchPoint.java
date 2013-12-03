/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class TouchPoint {

    public Vec3D v;
    public Vec3D oldV = null;
    public Vec3D vKinect;
    public Vec3D oldvKinect = null;
    public int color;
    public int confidence;
//    public float size;
    public boolean is3D;
    public boolean isNew;
    public boolean isCloseToPlane;
    public boolean toDelete = false;
    public boolean isUpdated = false;
    protected int id;
    protected int updateTime = 0;
    private OneEuroFilter[] filters;
    
    private static int globalID = 0;
    public static float filterFreq = 30f;
    public static float filterCut = 0.2f;
//    public static float filterCut = 1.0f;
    public static float filterBeta = 8.000f;
    
    public TouchPoint() {
        id = globalID++;
        toDelete = false;
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

    public void filter() {
        try {
            v.x = (float) filters[0].filter(v.x);
            v.y = (float) filters[1].filter(v.y);
            v.z = (float) filters[2].filter(v.z);
        } catch (Exception e) {
            System.out.println("OneEuro init Exception. Pay now." + e);
        }
    }

    public boolean isObselete(int currentTime, int duration) {
        return (currentTime - updateTime) > duration;
    }

    public int getColor(){
        return this.color;
    }
    
    public int getID(){
        return this.id;
    }
    
    // TODO: speed etc..
    public boolean updateWith(TouchPoint tp, int currentTime) {

        if (isUpdated || tp.isUpdated) {
            return false;
        }

        if (tp == null) {
            // ...
        }

        this.setUpdated(true);
        tp.setUpdated(true);
        tp.updateTime = currentTime;
        this.updateTime = currentTime;
        tp.toDelete = true;

//        System.out.println("Update " + this.id + " with " + tp.id +" distance was " + this.v.distanceTo(tp.v)  );
        
        oldV = v.copy();
        oldvKinect = vKinect.copy();
        v = tp.v;

        vKinect = tp.vKinect;
        confidence = tp.confidence;
        isCloseToPlane = tp.isCloseToPlane;

         filter();
         isNew = false;
        return true;

    }

    public Vec3D getPosition() {
        return v;
    }

    protected void setUpdated(boolean updated) {
        this.isUpdated = updated;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    @Override
    public String toString() {
        return "Touch Point : \n Vec3D " + v + "\n" + "Close to Plane : " + isCloseToPlane + " \n";
    }
}
