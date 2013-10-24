/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;

import processing.opengl.PGraphics3D;

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
    public void drawSelf(PGraphics3D graphics) {
        // nothing...
    }

    public InteractiveZone getInteractiveZone(){
        return zone;
    }
}
