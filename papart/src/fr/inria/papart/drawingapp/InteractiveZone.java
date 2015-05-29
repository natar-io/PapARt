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

import fr.inria.papart.multitouch.TouchPoint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

public class InteractiveZone implements Drawable, PConstants {

    static final int INTERACTIVE_COOLDOWN = 100;
    protected float width = 40;
    protected float height = 40;
    protected int lastPressedTime = 0;
    protected TouchPoint currentTP = null;
    protected boolean isActive = false;
    protected boolean isSelected = false;
    public boolean isCooldownDone = true;
    protected boolean isHidden = false;
    protected PVector position = new PVector();

    public InteractiveZone(int x, int y, int width, int height) {
        position.x = x;
        position.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isSelected(float x, float y, TouchPoint tp) {
        if (isHidden) {
            System.err.println("Error : inactive zone updated ");
            setNotTouched();
            return false;
        }
        if (x == PApplet.constrain(x,
                position.x - (this.width / 2) - Button.BUTTON_ERROR,
                position.x + (this.height / 2) + Button.BUTTON_ERROR)
                && y == PApplet.constrain(y,
                        position.y - (this.width / 2) - Button.BUTTON_ERROR,
                        position.y + (this.height / 2) + Button.BUTTON_ERROR)) {

            setTouched();
            if (isCooldownDone) {
                isActive = !isActive;
                currentTP = tp;
                isCooldownDone = false;
            }

            lastPressedTime = DrawUtils.applet.millis();
            return true;
        } 

        return false;
    }

    @Override
    public void drawSelf(PGraphicsOpenGL pgraphics3d) {
        if (isHidden) {
            System.out.println("Drawing hidden button...");
            return;
        }
        if ((DrawUtils.applet.millis() - lastPressedTime) > INTERACTIVE_COOLDOWN) {
            setNotTouched();
            isCooldownDone = true;
            currentTP = null;
        }

        if (isActive) {
            pgraphics3d.fill(DrawUtils.applet.red(255));
        } else {
            pgraphics3d.fill(DrawUtils.applet.green(255));
        }

        pgraphics3d.rectMode(PApplet.CENTER);
        pgraphics3d.rect(position.x, position.y, width, height);
    }

    protected void setTouched() {
        this.isSelected = true;
    }

    protected void setNotTouched() {
        this.isSelected = false;
    }

    public boolean isTouched() {
        return this.isSelected;
    }

    public void setPosition(PVector pos) {
        position.x = pos.x;
        position.y = pos.y;
    }
    public void setPosition(float x , float y ) {
        position.x = x;
        position.y = y;
    }

    public PVector getPosition() {
        return position;
    }
    
    public float getWidth(){
        return this.width;
    }

    public float getHeight(){
        return this.height;
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
