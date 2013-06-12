/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.graphics2D;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import fr.inria.papart.multitouchKinect.TouchElement;
import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.MarkerBoard;
import fr.inria.papart.procam.Utils;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
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

    
    public SubSketch() {
        
    }

    abstract public void setup(PApplet parent);

    protected void initSketch(PApplet parent) {

        // creation of a the object for manipulation
        this.layer = new Layer(parent,
                new PVector(width, height),
                new PVector(0, 0),
                new PVector(displayWidth, displayHeight));

        // get the buffer for rendering
        this.g = layer.getBuffer();
    }

    protected void updateInputTouch(TouchElement te, PVector screenSize) {

        if(te.position2D.isEmpty()){
            mouseX = -10;
            mouseY = -10;
            pmouseX = -10;
            pmouseY = -10;
            return;
        }
        
        // First check for inside Touch
        for (PVector p : te.position2D) {
            PVector p1 = new PVector(p.x * screenSize.x,
                    p.y * screenSize.y);
            p1 = layer.project(p1);
            p1 = layer.displayToImage(p1);

            // check if inside... 
            if (p1.x > 0 && p1.x < layer.width
                    && p1.y > 0 && p1.y < layer.height) {

                pmouseX = mouseX;
                pmouseY = mouseY;

                mouseX = (int) p1.x;
                mouseY = (int) (layer.height - p1.y);
                return;
            }
        }

        for (PVector p : te.position2D) {

            PVector p1 = new PVector(p.x * screenSize.x,
                    p.y * screenSize.y);
            p1 = layer.project(p1);
            p1 = layer.displayToImage(p1);

            pmouseX = mouseX;
            pmouseY = mouseY;

            mouseX = (int) p1.x;
            mouseY = (int) (layer.height - p1.y);
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
            mouseY = (int) (layer.height - p1.y);
            return;
        }
    }

    public void startDraw() {
        g.beginDraw();


        g.scale(-1, 1, 1);
        g.translate(0, height / 2f, 0);
        g.rotate(PApplet.PI);
        g.translate(0, -height / 2f, 0);
    }

    public void endDraw() {
        g.endDraw();
    }

    public void clear(float a, float b) {
        g.clear(a, b);
    }

    public void background(float r, float g, float b) {
        this.g.background(r, g, b);
    }

    public void background(float r) {
        this.g.background(r);
    }

    public void fill(int val) {
        g.fill(val);
    }

    public void fill(float v1, float v2) {
        g.fill(v1, v2);
    }

    public void noFill() {
        g.noFill();
    }

    public void stroke(int val) {
        g.stroke(val);
    }

    public void stroke(float val1, float val2) {
        g.stroke(val1, val2);
    }

    public void textFont(PFont val) {
        g.textFont(val);
    }

    public void textFont(PFont val, int size) {
        g.textFont(val, size);
    }

    public void text(String s, int a, int b, int c, int d) {
        g.text(s, a, b, c, d);
    }

    public void ellipse(float x, float y, float w, float h) {
        g.ellipse(x, y, w, h);
    }

    public void line(float x, float y, float w, float h) {
        g.line(x, y, w, h);
    }

    public void rect(float x, float y, float w, float h) {
        g.rect(x, y, w, h);
    }

    public void translate(float x, float y, float z) {
        g.translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        g.scale(x, y, z);
    }

    public void scale(float s) {
        g.scale(s);
    }
    public void rotate(float s) {
        g.rotate(s);
    }

    public void image(PImage img, float x, float y) {
        g.image(img, x, y);
    }
    
    public void image(PImage img, float x, float y, float w, float h) {
        g.image(img, x, y, w, h);
    }

    public void box(float x, float y, float z) {
        g.box(x, y, z);
    }

    abstract public void draw();
}
