/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.drawingapp;

import fr.inria.papart.multitouchKinect.TouchPoint;
import processing.core.PApplet;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

public class Slider extends InteractiveZone{

    private PVector previous = new PVector();
    private InteractiveZone activationZone;

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
