package fr.inria.iparla.drawingapp;

import multitouch.laviole.name.TouchPoint;
import processing.core.PApplet;
import processing.core.PGraphics3D;
import processing.core.PVector;

public class InteractiveZone implements Drawable {

    static final int INTERACTIVE_COOLDOWN = 100;
    protected float width = 40;
    protected float height = 40;
    protected int lastPressedTime = 0;
    protected TouchPoint currentTP = null;
    public boolean isActive = false;
    public boolean isSelected = false;
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
            isSelected = false;
            return false;
        }
        if (x == PApplet.constrain(x,
                position.x - (this.width / 2) - Button.BUTTON_ERROR,
                position.x + (this.height / 2) + Button.BUTTON_ERROR)
                && y == PApplet.constrain(y,
                position.y - (this.width / 2) - Button.BUTTON_ERROR,
                position.y + (this.height / 2) + Button.BUTTON_ERROR)) {

            if (isCooldownDone) {
                isActive = !isActive;
                currentTP = tp;
                isCooldownDone = false;
            }
            isSelected = true;
            lastPressedTime = DrawUtils.applet.millis();
            return true;
        } else {
            isSelected = false;
        }

        return false;
    }

    @Override
    public void drawSelf(PGraphics3D pgraphics3d) {
        if (isHidden) {
            System.out.println("Drawing hidden button...");
            return;
        }
        if ((DrawUtils.applet.millis() - lastPressedTime) > INTERACTIVE_COOLDOWN) {
            isCooldownDone = true;
            currentTP = null;
        }

        if (isActive) {
            pgraphics3d.fill(DrawUtils.applet.red(255));
        } else {
            pgraphics3d.fill(DrawUtils.applet.green(255));
        }

        pgraphics3d.rectMode(PApplet.CORNER);
        pgraphics3d.rect(position.x, position.y, width, height);
    }

    public void setPosition(PVector pos) {
        position.x = pos.x;
        position.y = pos.y;
    }

    public PVector getPosition() {
        return position;
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
