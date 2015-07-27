/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.drawingapp;

import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jeremylaviole
 */
public class Descriptor implements Drawable{
    public boolean isHidden;
    public String description;
    protected InteractiveZone zone;

    @Override
    public void show() {
       isHidden = false;
    }

    @Override
    public void hide() {
       isHidden = true;
    }


    // TODO: draw the text ? 
    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        // nothing...
    }

    public InteractiveZone getInteractiveZone(){
        return zone;
    }
}
