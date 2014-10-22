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
