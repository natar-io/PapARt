/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitouch.laviole.name;

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
    
    public TouchPoint() {
        id = globalID++;
        toDelete = false;
    }

    public boolean isObselete(int currentTime, int duration){
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

        // Implementation 2 --  Replace the ArrayList
        oldV = v;
        v = tp.v;

        vKinect = tp.v;
        confidence = tp.confidence;
        isCloseToPlane = tp.isCloseToPlane;

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
