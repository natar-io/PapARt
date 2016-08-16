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

import fr.inria.papart.multitouch.TouchPoint;
import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

public class Slider extends InteractiveZone{

    private final PVector previous = new PVector();
    private final InteractiveZone activationZone;

    public boolean isSliding = false;
//    public PVector translation = new PVector();

    public Slider(int x, int y, int width, int height, InteractiveZone activation){
	super(x, y, width, height);
	this.activationZone = activation;
    }

    @Override
   public boolean isSelected(float x, float y, TouchPoint tp){
       boolean selected = super.isSelected(x, y, tp);

       if(isTouched() && activationZone.isActive){
	   position.x = x;
	   //	   position.y = y;
	   isSliding = true;
	   previous.x = x;
	   previous.y = y;
       }

       return selected;
    }


    @Override
    public void drawSelf(PGraphicsOpenGL pgraphics3d){
	if(isHidden)
                return;

        if((DrawUtils.applet.millis() - lastPressedTime)  > INTERACTIVE_COOLDOWN){
	    isCooldownDone = true;
	    currentTP = null;
	}

       if(activationZone.isActive){
	    pgraphics3d.fill(0xAC135C);
	    pgraphics3d.rectMode(PApplet.CENTER);
	    pgraphics3d.rect(position.x, position.y, width, height);
	}
    }

}
