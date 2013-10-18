package fr.inria.papart.imanalysis;

import processing.core.PImage;

/**
 *
 * @author jiii
 */
public interface IPixel extends IHasPosition{

    // Color management
    public float hue();

    public float saturation();

    public float brightness();

    public float red();

    public float green();

    public float blue();
    
}
