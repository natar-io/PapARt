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
public class Line extends Shape {

    PVector p1, p2;

    public Line(PVector pos, int scale) {
        super(pos);
        descriptors = new PositionDescriptor[2];
        descriptors[0] = new PositionDescriptor(center, new PVector(50 * scale, 50 * scale), "p1");
        descriptors[1] = new PositionDescriptor(center, new PVector(50 * scale, -50 * scale), "p2");


        p1 = ((PositionDescriptor) descriptors[0]).getPosition();
        p2 = ((PositionDescriptor) descriptors[1]).getPosition();

    }

    @Override
    public void drawSelf(PGraphics3D graphics) {
        super.drawSelf(graphics);
        graphics.pushMatrix();

        graphics.translate(center.x, center.y);
        graphics.stroke(strokeColor);
        graphics.strokeWeight(strokeWeight);
        graphics.noFill();
        graphics.line(p1.x, p1.y, p2.x, p2.y);
        graphics.popMatrix();

        if (isSelected) {
            for (Descriptor desc : descriptors) {
                ((PositionDescriptor) desc).drawSelf(graphics);
            }
        }
    }
}
