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
