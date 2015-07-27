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
 * @author jeremy
 */
public interface Drawable {

    public void show();
    public void hide();
    public void drawSelf(PGraphicsOpenGL graphics);

}
