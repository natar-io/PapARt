/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import java.util.ArrayList;
import processing.core.PVector;

/**
 * Public (Processing) API for a touch point. Touches are for PaperScreens
 *
 * @author jiii
 */
public class Touch {

    public boolean is3D;
    public PVector position;
    public PVector pposition;
    public PVector speed;
    public TouchPoint touchPoint;
    
    public PVector size;

    // TODO: implementation of this. 
    public boolean isObject;
    public int id;
    
    public void setPosition(PVector v){
        setPosition(v.x, v.y, v.z);
    }
    public void setPosition(float x, float y, float z){
        if(this.position == null){
            this.position = new PVector();
        }
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }
    
    @Override
    public String toString() {
        return "Position " + position + " Speed " + speed + " Touch info " + touchPoint;
    }

    public void setPrevPos(PVector prevPosition) {
        pposition = prevPosition;
        speed = PVector.sub(prevPosition,position);
    }
    
    public void defaultPrevPos() {
        pposition = position.get();
        speed = new PVector();
    }

    public void scaleBy(PVector scales) {
        position.x *= scales.x;
        position.y *= scales.y;
        position.z *= scales.z;

        pposition.x *= scales.x;
        pposition.y *= scales.y;
        pposition.z *= scales.z;

        speed.x *= scales.x;
        speed.y *= scales.y;
        speed.z *= scales.z;
    }
}
