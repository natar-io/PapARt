/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import fr.inria.papart.multitouchKinect.TouchElement;
import processing.core.PApplet;
import processing.core.PVector;

/**
 *
 * @author jiii
 */
public abstract class SubSketch {

    public Layer layer;
    public int width, height;
    public float displayWidth, displayHeight;
    public PApplet parent;
    public GLGraphicsOffScreen g;
    public int mouseX, mouseY, pmouseX, pmouseY;

    abstract public void setup(PApplet parent);

    protected void init(PApplet parent) {

        // creation of a the object for manipulation
        this.layer = new Layer(parent,
                new PVector(width, height),
                new PVector(0, 0),
                new PVector(displayWidth, displayHeight));

        // get the buffer for rendering
        this.g = layer.getBuffer();
    }

    protected void updateInputTouch(TouchElement te, PVector screenSize) {

        for (PVector p : te.position2D) {
            PVector p1 = new PVector(p.x * screenSize.x,
                    p.y * screenSize.y);
            p1 = layer.project(p1);
            p1 = layer.displayToImage(p1);
            
            pmouseX = mouseX;
            pmouseY = mouseY;

            mouseX = (int) p1.x;
            mouseY = (int) p1.y;
            return;
        }
    }
    
    protected void updateInputHover(TouchElement te, PVector screenSize) {

        for (PVector p : te.position3D) {
            PVector p1 = new PVector(p.x * screenSize.x,
                    p.y * screenSize.y);
            p1 = layer.project(p1);
            p1 = layer.displayToImage(p1);
            
            pmouseX = mouseX;
            pmouseY = mouseY;

            mouseX = (int) p1.x;
            mouseY = (int) p1.y;
            return;
        }

    }

    abstract public void draw();
}
