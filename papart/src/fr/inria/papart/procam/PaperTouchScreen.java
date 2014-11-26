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
package fr.inria.papart.procam;

import com.sun.jna.platform.win32.WinError;
import fr.inria.papart.procam.display.BaseDisplay;
import processing.opengl.PGraphicsOpenGL;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.multitouch.TouchList;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class PaperTouchScreen extends PaperScreen {

    protected TouchList touchList = new TouchList();
    protected TouchInput touchInput;
    protected ArrayList<Button> buttons = new ArrayList<Button>();
    public boolean isTranslated = false;

    /**
     * Zero arguments can be invoked only when a Papart object was created.
     */
    public PaperTouchScreen() {
        // the super is implicity, however it has to be called.
        // For some reasons, this is never called !
        super();
    }

    public PaperTouchScreen(Papart papart) {
        this(papart.getCameraTracking(),
                papart.getDisplay(),
                papart.getTouchInput());
    }

    public PaperTouchScreen(Camera cam, BaseDisplay proj, TouchInput touchinput) {
        super(cam, proj);

        this.touchInput = touchinput;
    }

    ///// Load ressources ////////
    @Override
    public void pre() {
        super.pre();

        if (this.touchInput == null) {
            this.touchInput = Papart.getPapart().getTouchInput();
        }

        updateTouch();
    }

    @Override
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    @Override
    public void setLocation(float x, float y, float z) {
        super.setLocation(x, y, z);
        updateTouch();
    }

    public void updateTouch() {
        if (!screen.isDrawing()) {
            return;
        }
        screen.computeScreenPosTransform();
        touchList = touchInput.projectTouchToScreen(screen, display);
        touchList.sortAlongYAxis();

        if (touchInput instanceof KinectTouchInput) {
            if (((KinectTouchInput) (touchInput)).useRawDepth()) {
                touchList.invertY(drawingSize);
            } else {
                touchList.scaleBy(drawingSize);
            }
        }

        if (!buttons.isEmpty()) {
            updateButtons();
        }
    }

    public void updateButtons() {
        if (!touchList.isEmpty()) {
            for (Touch t : touchList) {
                if (t.is3D) {
                    continue;
                }
                PVector p = t.position;
                checkButtons(p.x, p.y);
            }
        }
    }

    protected void checkButtons(float x, float y) {
        for (Button b : buttons) {
            if (b.isSelected(x, y, null)) {
                return;
            }
        }
    }

    protected void drawButtons() {
        for (Button b : buttons) {
            b.drawSelf(getGraphics());
        }
    }

    // Example Draw.
    @Override
    public void draw() {
        beginDraw2D();
        clear();
        background(0, 200, 100);
        drawTouch(10);
        endDraw();
    }

    static private final int DEFAULT_TOUCH_SIZE = 50;

    protected void drawTouch() {
        drawTouch(DEFAULT_TOUCH_SIZE);
    }

    protected void drawTouch(int ellipseSize) {
        for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
            } else {
                fill(58, 71, 198);
            }
            ellipse(t.position.x, t.position.y, ellipseSize, ellipseSize);

//            ellipse(t.pposition.x, t.pposition.y, ellipseSize /2 , ellipseSize /2);
//            pushMatrix();
//            translate(t.position.x, t.position.y);
//            ellipse(0, 0, ellipseSize, ellipseSize);
//            line(0, 0, t.speed.x * 4, t.speed.y *4);
//            popMatrix();
        }
    }
    
    protected void drawFullTouch(int ellipseSize) {
        for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
            } else {
                fill(58, 71, 198);
            }
            ellipse(t.pposition.x, t.pposition.y, ellipseSize /2 , ellipseSize /2);
            pushMatrix();
            translate(t.position.x, t.position.y);
            ellipse(0, 0, ellipseSize, ellipseSize);
            line(0, 0, t.speed.x * 4, t.speed.y *4);
            popMatrix();
        }
    }

    protected void drawTouchSpeed() {
        for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
            } else {
                fill(58, 71, 198);
            }
            ellipse(t.position.x, t.position.y, t.speed.x * 3, t.speed.y * 3);
        }
    }

    public TouchList getTouchList() {
        return touchList;
    }

    public TouchInput getTouchInput() {
        return touchInput;
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }

    public boolean isIsTranslated() {
        return isTranslated;
    }
}
