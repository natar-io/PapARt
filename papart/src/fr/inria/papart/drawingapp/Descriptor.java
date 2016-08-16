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

import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jeremylaviole
 */
public class Descriptor implements Drawable{
    public boolean isHidden;
    public String description;
    protected InteractiveZone zone;

    @Override
    public void show() {
       isHidden = false;
    }

    @Override
    public void hide() {
       isHidden = true;
    }


    // TODO: draw the text ? 
    @Override
    public void drawSelf(PGraphicsOpenGL graphics) {
        // nothing...
    }

    public InteractiveZone getInteractiveZone(){
        return zone;
    }
}
