/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouchKinect;

import processing.core.PVector;

/**
 *
 * @author jiii
 */
public class Touch {
    public boolean is3D;
    public PVector p;
    public PVector speed;
    public TouchPoint touchPoint;
    
    @Override
    public String toString(){
        return  "Location " + p + " Speed " + speed + " Touch info " + touchPoint;
    }
}
