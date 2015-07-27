/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.panel;

import processing.core.PApplet;
import processing.opengl.*;

/**
 * TEST CODE
 * @author jiii
 */
public class SketchTest extends PApplet {

    @Override
    public void setup() {
        size(200, 200);
        stroke(155, 0, 0);
    }

    @Override
    public void draw() {
        background(0);
        line(mouseX, mouseY, width / 2, height / 2);
    }

    public static void main(String args[]) {
//        PApplet.main(new String[]{"--present", "fr.inria.papart.panel.SketchTest"});
        PApplet.main(new String[]{"fr.inria.papart.panel.SketchTest"});
    }

}
