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
package fr.inria.papart.drawingapp;

import processing.core.PApplet;
import processing.core.PFont;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class PositionDescriptor extends Descriptor {

    protected static int interfaceSize = 35;
    private ButtonWidget buttonWidget;
    private int strokeColor = 200;
    private int strokeAlpha = 10;

    public PositionDescriptor(PVector abs, PVector pos, String description) {
        this.description = description;
        buttonWidget = new ButtonWidget(description, abs, (int) pos.x, (int) pos.y);
        zone = buttonWidget;
        buttonWidget.show();
    }

    public PositionDescriptor(PVector abs, PVector pos, int width, int height, String description) {
        this.description = description;
        buttonWidget = new ButtonWidget(description, abs, (int) pos.x, (int) pos.y, width, height);
        zone = buttonWidget;
        buttonWidget.show();
    }

    @Override
    public void hide() {
        buttonWidget.hide();
    }

    @Override
    public void show() {
        buttonWidget.show();
    }

    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        if (isHidden) {
            return;
        }
        if (buttonWidget.getImage() == null) {

            graphics.pushMatrix();
            graphics.fill(255, 120);
            graphics.rectMode(PApplet.CORNER);
            graphics.translate(buttonWidget.getAbsPos().x + buttonWidget.getPosition().x,
                    buttonWidget.getAbsPos().y + buttonWidget.getPosition().y);

//            graphics.strokeWeight(3);
//            graphics.stroke(strokeColor, strokeAlpha);
//            graphics.line(-interfaceSize, 0, interfaceSize, 0);
//            graphics.line(0, -interfaceSize, 0, interfaceSize);

            graphics.noStroke();
            graphics.rect(-interfaceSize / 4f, -interfaceSize / 4f,
                    2 * interfaceSize / 4f, 2 * interfaceSize / 4f);
            graphics.popMatrix();
        }
        buttonWidget.drawSelf(graphics);
    }

    public PVector getPosition() {
        return buttonWidget.getPosition();
    }

    public PVector getAbsPosition() {
        return PVector.add(buttonWidget.getPosition(), buttonWidget.getAbsPos());
    }
}
