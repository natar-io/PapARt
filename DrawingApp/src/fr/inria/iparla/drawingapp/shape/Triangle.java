/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.iparla.drawingapp.shape;

import fr.inria.iparla.drawingapp.Descriptor;
import fr.inria.iparla.drawingapp.PositionDescriptor;
import processing.core.PGraphics3D;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class Triangle extends Shape {

    PVector p1, p2, p3;

    public Triangle(PVector pos, int scale) {
        super(pos);
        descriptors = new PositionDescriptor[3];
        descriptors[0] = new PositionDescriptor(center, new PVector(-50 * scale,  50 * scale), "p1");
        descriptors[1] = new PositionDescriptor(center, new PVector( 50 * scale, -50 * scale), "p2");
        descriptors[2] = new PositionDescriptor(center, new PVector(-50 * scale, -50 * scale), "p3");


        p1 = ((PositionDescriptor) descriptors[0]).getPosition();
        p2 = ((PositionDescriptor) descriptors[1]).getPosition();
        p3 = ((PositionDescriptor) descriptors[2]).getPosition();

    }

    @Override
    public void drawSelf(PGraphics3D graphics) {
        super.drawSelf(graphics);
        graphics.pushMatrix();

        graphics.translate(center.x, center.y);
        graphics.stroke(strokeColor);
        graphics.strokeWeight(4);
        graphics.noFill();
        graphics.triangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
        graphics.popMatrix();

        if (isSelected) {
            for (Descriptor desc : descriptors) {
                ((PositionDescriptor) desc).drawSelf(graphics);
            }
        }
    }
}
