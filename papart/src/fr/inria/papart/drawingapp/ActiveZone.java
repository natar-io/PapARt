/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.drawingapp;

import fr.inria.papart.multitouch.TouchPoint;
import processing.core.PApplet;
import processing.opengl.PGraphicsOpenGL;

public class ActiveZone extends Button {

    static final int ACTIVE_ZONE_ERROR = 5;

    public ActiveZone(String image, int x, int y, int width, int height) {
        super(image, x, y, width, height);
    }

    public ActiveZone(String image, int x, int y) {
        super(image, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public boolean isSelected(float x, float y, TouchPoint tp) {
        if (isHidden) {
            setNotTouched();
            return false;
        }

        if (x == PApplet.constrain(x,
                position.x - (this.width/2) - ACTIVE_ZONE_ERROR,
                position.x + (this.width/2) + ACTIVE_ZONE_ERROR)
                && y == PApplet.constrain(y,
                position.y - (this.height/2) - ACTIVE_ZONE_ERROR,
                position.y + (this.height/2) + ACTIVE_ZONE_ERROR)) {

            if (isCooldownDone) {
                isActive = !isActive;
                currentTP = tp;
                isCooldownDone = false;
            }
            setTouched();
            lastPressedTime = DrawUtils.applet.millis();
            return true;
        } else {
            setNotTouched();
        }
        return false;
    }

    @Override
    public void drawSelf(PGraphicsOpenGL pgraphics3d) {

        if (isHidden) {
            return;
        }

        if (isTouched()) {
            pgraphics3d.tint(DrawUtils.applet.color(100, 255, 100));
        } else {
            pgraphics3d.fill(DrawUtils.applet.color(UNSELECTED));
        }

        if (img != null) {
            DrawUtils.drawImage(pgraphics3d, img, (int) position.x, (int) position.y, (int) width, (int) height);
        } else {
            DrawUtils.drawText(pgraphics3d, name, buttonFont,
                    (int) position.x, (int) position.y); //, (int) width, (int) height);
        }
        pgraphics3d.noTint();

    }
}
