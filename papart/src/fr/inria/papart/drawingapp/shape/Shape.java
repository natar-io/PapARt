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

import fr.inria.papart.drawingapp.BBox;
import fr.inria.papart.drawingapp.Descriptor;
import fr.inria.papart.drawingapp.Drawable;
import fr.inria.papart.drawingapp.PositionDescriptor;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class Shape implements Drawable {

    protected Descriptor[] descriptors;
    protected BBox bbox;
    protected boolean isHidden = false;
    protected boolean isSelected = false;
    protected boolean isMovable = false;
    protected Descriptor centerDescriptor;
    protected PVector center;
    public int strokeWeight = 2;
    public int strokeColor = 255;

    public Shape(PVector pos) {
        centerDescriptor = new PositionDescriptor(new PVector(), pos, 50, 50, "cross.png");
        center = ((PositionDescriptor )centerDescriptor).getPosition();
    }

    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        if (isMovable) {
            centerDescriptor.drawSelf(graphics);
        }
    }

    public Descriptor[] getDescriptors() {
        return descriptors;
    }

    public BBox getBBox() {
        return bbox;
    }

   
    @Override
    public void show() {
        isHidden = false;
    }

    @Override
    public void hide() {
        isHidden = true;
    }
}
