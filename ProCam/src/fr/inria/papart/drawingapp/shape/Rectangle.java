/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
public class Rectangle extends Shape {

    PVector p1, p2, rot;

    public Rectangle(PVector pos, int scale) {
        super(pos);
        descriptors = new PositionDescriptor[3];

        descriptors[0] = new PositionDescriptor(center, new PVector(0, 0), "Center");
        descriptors[1] = new PositionDescriptor(center, new PVector(50 * scale, 50 * scale), "Rectangle");
        descriptors[2] = new PositionDescriptor(center, new PVector(50 * scale, 0 * scale), "Rotation");
        p1 = ((PositionDescriptor) descriptors[0]).getPosition();
        p2 = ((PositionDescriptor) descriptors[1]).getPosition();
        rot = ((PositionDescriptor) descriptors[2]).getPosition();

    }

    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        super.drawSelf(graphics);
        graphics.pushMatrix();

        graphics.rectMode(PApplet.CENTER);
        
        graphics.translate(center.x + p1.x, center.y + p1.y);
        graphics.stroke(strokeColor);
        graphics.strokeWeight(strokeWeight);
        graphics.noFill();
        graphics.rotate(rot.x / 200f);
        graphics.rect(0, 0, p2.x, p2.y);
        graphics.popMatrix();

        if (isSelected) {
            for (Descriptor desc : descriptors) {
                ((PositionDescriptor) desc).drawSelf(graphics);
            }
        }
    }
}
