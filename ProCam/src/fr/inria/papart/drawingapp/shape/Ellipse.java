/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp.shape;

import fr.inria.papart.drawingapp.Descriptor;
import fr.inria.papart.drawingapp.PositionDescriptor;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class Ellipse extends Shape {

    PVector p1, p2, rot;

    public Ellipse(PVector pos, int scale) {
        super(pos);
        descriptors = new PositionDescriptor[3];

        descriptors[0] = new PositionDescriptor(center, new PVector(0, 0), "Center");
        descriptors[1] = new PositionDescriptor(center, new PVector(50 * scale, 50 * scale), "Ellipse");
        descriptors[2] = new PositionDescriptor(center, new PVector(0 * scale, 50 * scale), "Rotation");
        p1 = ((PositionDescriptor) descriptors[0]).getPosition();
        p2 = ((PositionDescriptor) descriptors[1]).getPosition();
        rot = ((PositionDescriptor) descriptors[2]).getPosition();

    }

    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        super.drawSelf(graphics);
        graphics.pushMatrix();

        graphics.ellipseMode(PApplet.CENTER);
        graphics.translate(center.x + p1.x, center.y + p1.y);
        graphics.stroke(strokeColor);
        graphics.strokeWeight(strokeWeight);
        graphics.noFill();
        graphics.rotate(rot.x / 200f);
        graphics.ellipse(0, 0, p2.x, p2.y);
        graphics.popMatrix();

        if (isSelected) {
            for (Descriptor desc : descriptors) {
                ((PositionDescriptor) desc).drawSelf(graphics);
            }
        }
    }
}
