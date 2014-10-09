/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.multitouch;

import processing.core.PVector;

/**
 * Public (Processing) API for a touch point. 
 * Touches are for PaperScreens
 * @author jiii
 */
public class Touch {
    public boolean is3D;
    public PVector position;
    public PVector speed;
    public TouchPoint touchPoint;
    
    @Override
    public String toString(){
        return  "Position " + position + " Speed " + speed + " Touch info " + touchPoint;
    }
}
