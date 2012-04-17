/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.iparla.drawingapp;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics3D;
import processing.core.PImage;

/**
 *
 * @author jeremylaviole
 */
public class DrawUtils {

    public static PApplet applet;

    static public void drawImage(PGraphics3D pg3d, PImage img, int x, int y, int w, int h) {
        pg3d.pushMatrix();
        pg3d.translate(x, y);
        pg3d.scale(-1, 1, 1);
        pg3d.rotate(PApplet.PI);
        pg3d.image(img, 0, 0, w, h);
        pg3d.popMatrix();
    }

    static public void drawText(PGraphics3D pg3d, String text, PFont font, int x, int y) {
        pg3d.pushMatrix();
        pg3d.translate(x, y);
        pg3d.scale(-1, 1, 1);
        pg3d.rotate(PApplet.PI);
        pg3d.textMode(PApplet.MODEL);
        pg3d.textFont(font);
        pg3d.text(text, 0, 0);
        pg3d.popMatrix();
    }
}
