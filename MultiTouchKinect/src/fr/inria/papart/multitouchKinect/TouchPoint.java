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

    public Vec3D prevV;
    public Vec3D v;
    public Vec3D oldV;
    public Vec3D vKinect;
    float distanceMin;
    public int confidence;
    public boolean is3D;
    public boolean isCloseToPlane;
    public boolean toDelete = false;
    public boolean isUpdated = false;
    protected int id;
    public int idPressed;
    private static int globalID = 0;
    protected int updateTime = 0;
    private OneEuroFilter[] filters;

    
    public static float filterFreq = 30f;
    public static float filterCut = 1.0f;
    public static float filterBeta = 8.000f;
    
    public TouchPoint() {
        id = globalID++;
        toDelete = false;
        try {
            filters = new OneEuroFilter[3];
            for (int i = 0; i < 3; i++) {
                filters[i] = new OneEuroFilter(filterFreq, filterCut, filterBeta);
            }
        } catch (Exception e) {
            System.out.println("OneEuro Exception. Pay now." + e);
        }
    }

//    public void init() {
//        try {
//            v.x = (float) filters[0].filter(v.x);
//            v.y = (float) filters[1].filter(v.y);
//            v.z = (float) filters[2].filter(v.z);
//        } catch (Exception e) {
//            System.out.println("OneEuro init Exception. Pay now.");
//        }
//    }
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

        // Implementation 1 --  Half smooth with the previous
//    v.addSelf(tp.v);
//    v.scaleSelf(0.5f);
//    isCloseToPlane = tp.isCloseToPlane;


//        try {
//            v.x = (float) filters[0].filter(v.x);
//            v.y = (float) filters[1].filter(v.y);
//            v.z = (float) filters[2].filter(v.z);
//        } catch (Exception e) {
//            System.out.println("OneEuro init Exception. Pay now.");
//        }

        // Implementation 2 --  Replace the ArrayList
        oldV = v;
        v = tp.v;

        vKinect = tp.v;
        confidence = tp.confidence;
        isCloseToPlane = tp.isCloseToPlane;

        filter();
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

//  void draw(){
//    pushStyle();
//    noStroke();
//
//    //    if(isCloseToPlane)
//      fill(50, 255, 50);
//    // else
//    //   fill(255, 50, 50);
//
//      //    gfx.sphere(new Sphere(v, 0.01), 8, true);
//    popStyle();
//  }
    public String toString() {
        return "Touch Point : \n Vec3D " + v + "\n" + "Close to Plane : " + isCloseToPlane + " \n";
    }
}
