/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
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
