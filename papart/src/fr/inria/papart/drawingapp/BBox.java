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
package fr.inria.papart.drawingapp;

import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class BBox {

    public PVector min = new PVector();
    public PVector max = new PVector();
    public static float interfaceSize;
    private final ButtonWidget buttonWidget;


    public BBox(PVector center) {
//        this.center = center.get();
        buttonWidget = new ButtonWidget("Translate", new PVector(0,0), (int) center.x, (int)center.y);
        buttonWidget.show();
//        updateBBox();
    }

    void updateBBox() {
//        center.x = (max.x - min.x) / 2f + min.x;
//        center.y = (max.y - min.y) / 2f + min.y;
    }

    void drawSelf(PGraphicsOpenGL graphics) {
        graphics.pushMatrix();
        graphics.fill(255, 30);
        graphics.noStroke();
        graphics.rectMode(PApplet.CORNER);
        float w = max.x - min.x / 2f;
        float h = max.y - min.y / 2f;

//        graphics.translate(300, 300);
        graphics.translate(buttonWidget.getPosition().x, buttonWidget.getPosition().y);

        graphics.rect(-w, -h, w * 2, h * 2);
        graphics.popMatrix();

        buttonWidget.drawSelf(graphics);
    }


    public PVector getPosition(){
        return buttonWidget.getPosition();
    }

    @Override
    public String toString() {
        return "Bounding box : " + buttonWidget.getPosition() + " min : " + min + " max " + max + "\n";
    }
}
