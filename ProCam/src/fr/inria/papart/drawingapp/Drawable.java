/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp;

import processing.opengl.PGraphics3D;

/**
 *
 * @author jeremy
 */
public interface Drawable {

    public void show();
    public void hide();
    public void drawSelf(PGraphics3D graphics);

}
