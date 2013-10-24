/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.drawingapp.shape;

import fr.inria.papart.drawingapp.Descriptor;
import fr.inria.papart.drawingapp.PositionDescriptor;
import processing.opengl.PGraphics3D;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class Bezier extends Shape {

    PVector p1, p2, c1, c2;

    public Bezier(PVector pos, int scale) {
        super(pos);
        descriptors = new PositionDescriptor[4];

        descriptors[0] = new PositionDescriptor(center, new PVector(-50 * scale, -50 * scale), "Pos 1");
        descriptors[1] = new PositionDescriptor(center, new PVector(-50 * scale, -50 * scale), "Control 1");
        descriptors[2] = new PositionDescriptor(center, new PVector(50 * scale, 50 * scale), "Pos 2");
        descriptors[3] = new PositionDescriptor(center, new PVector(50 * scale, 50 * scale), "Control 2");

        p1 = ((PositionDescriptor) descriptors[0]).getPosition();
        c1 = ((PositionDescriptor) descriptors[1]).getPosition();
        p2 = ((PositionDescriptor) descriptors[2]).getPosition();
        c2 = ((PositionDescriptor) descriptors[3]).getPosition();

    }

    @Override
    public void drawSelf(PGraphics3D graphics) {
        super.drawSelf(graphics);
        graphics.pushMatrix();

        graphics.translate(center.x, center.y);
        graphics.stroke(strokeColor);
        graphics.strokeWeight(4);
        graphics.noFill();
        graphics.bezier(p1.x, p1.y,
                c1.x, c1.y,
                c2.x, c2.y,
                p2.x, p2.y);
        graphics.popMatrix();


        if (isSelected) {
            for (Descriptor desc : descriptors) {
                ((PositionDescriptor) desc).drawSelf(graphics);
            }
        }
    }

}
