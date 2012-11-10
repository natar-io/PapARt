/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

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
    public static float filterFreq = 30f;
    public static float filterCut = 1.0f;
    public static float filterBeta = 8.000f;

    public TouchPoint() {
    }

    public void filter() {
    }

    public boolean isObselete(int currentTime, int duration) {
        return (currentTime - updateTime) > duration;
    }

    // TODO: speed etc..
    public boolean updateWith(TouchPoint tp, int currentTime) {

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
