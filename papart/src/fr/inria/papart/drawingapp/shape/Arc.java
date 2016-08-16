/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.drawingapp.shape;

import fr.inria.papart.drawingapp.Descriptor;
import fr.inria.papart.drawingapp.PositionDescriptor;
import processing.core.PVector;

/**
 *
 * @author jeremy
 */
public class Arc extends Shape {

    PVector p1, p2, p3, p4;

    public Arc(PVector pos, int scale) {
        super(pos);
        descriptors = new PositionDescriptor[4];
        descriptors[0] = new PositionDescriptor(center, new PVector(0,0), "Center");
        descriptors[1] = new PositionDescriptor(center, new PVector(40 * scale, 40 * scale), "Ellipse");
        descriptors[2] = new PositionDescriptor(center, new PVector(0 * scale, -50 * scale), "Begin");
        descriptors[3] = new PositionDescriptor(center, new PVector(0 * scale, -80 * scale), "End");


        p1 = ((PositionDescriptor) descriptors[0]).getPosition();
        p2 = ((PositionDescriptor) descriptors[1]).getPosition();
        p3 = ((PositionDescriptor) descriptors[2]).getPosition();
        p4 = ((PositionDescriptor) descriptors[3]).getPosition();

    }

//    @Override
//    public void drawSelf(PGraphics3D graphics) {
//        super.drawSelf(graphics);
//        graphics.pushMatrix();
//
//        graphics.translate(center.x, center.y);
//        graphics.stroke(strokeColor);
//        graphics.strokeWeight(strokeWeight);
//        graphics.noFill();
//        graphics.arc(p1.x, p1.y, p2.x, p2.y, p3.x / 50f, p4.x / 50f);
//        graphics.popMatrix();
//
//        if (isSelected) {
//            for (Descriptor desc : descriptors) {
//                ((PositionDescriptor) desc).drawSelf(graphics);
//            }
//        }
//    }
}
