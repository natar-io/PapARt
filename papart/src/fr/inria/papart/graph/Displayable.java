/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graph;

import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jiii
 */
public interface Displayable {

    public abstract void prepareToDisplayOn(PApplet display);

    public abstract boolean canBeDisplayedOn(PApplet display);
    
    public abstract PImage getDisplayedOn(PApplet display);
    
}
