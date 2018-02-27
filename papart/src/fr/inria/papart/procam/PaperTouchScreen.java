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
package fr.inria.papart.procam;

import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.display.BaseDisplay;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.DepthTouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.tracking.TrackedDepthPoint;
import fr.inria.papart.multitouch.tracking.TrackedElement;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.procam.display.ProjectorDisplay;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class PaperTouchScreen extends PaperScreen {

    protected TouchList touchList = new TouchList();
    protected TouchInput touchInput;

    /**
     * Zero arguments can be invoked only when a Papart object was created.
     */
    public PaperTouchScreen() {
        // the super is implicity, however it has to be called.
        // For some reasons, this is never called !
        super();
    }

    /**
     * Create a touchScreen from a Papart object. It will use the default camera
     * for tracking, display and touchInput.
     *
     * @param papart
     */
    public PaperTouchScreen(Papart papart) {
        this(papart.getApplet(),
                papart.getPublicCameraTracking(),
                papart.getDisplay(),
                papart.getTouchInput());
    }

    /**
     * Manual instanciation without a Papart object.
     *
     * @param applet
     * @param cam
     * @param proj
     * @param touchinput
     */
    public PaperTouchScreen(PApplet applet, Camera cam, BaseDisplay proj, TouchInput touchinput) {
        super(applet, cam, proj);
        this.touchInput = touchinput;
    }

    /**
     * Called by Processing, do not call. This method prepares the display if
     * necessary and the start the touch input.
     */
    @Override
    public void pre() {
        super.pre();

        if (this.touchInput == null) {
            this.touchInput = Papart.getPapart().getTouchInput();
        }
        if (this.touchInput != null) {
            updateTouch();
        }
    }

    /**
     * Add a vector to the tracked location. Then updates the touch.
     *
     * @param v in millimeters
     */
    @Override
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    /**
     * Add a vector to the tracked location. Then updates the touch.
     *
     * @param x in millimeters
     * @param y in millimeters
     * @param z in millimeters
     */
    @Override
    public void setLocation(float x, float y, float z) {
        super.setLocation(x, y, z);
        this.computeWorldToScreenMat(cameraTracking);
        updateTouch();
    }

    private PVector touchOffset = new PVector();

    /**
     * Get the 2D offset to the touch.
     *
     */
    public PVector getTouchOffset() {
        return touchOffset.copy();
    }

    /**
     * Add a 2D offset to the touch.
     *
     * @param x
     * @param y
     */
    public void setTouchOffset(float x, float y) {
        this.touchOffset.set(new PVector(x, y));
    }

    /**
     * Add a 2D offset to the touch.
     *
     * @param touchOffset
     */
    public void setTouchOffset(PVector touchOffset) {
        this.touchOffset.set(touchOffset);
    }

    /**
     * Update the touch locations. Call this after setTouchOffset.
     */
    public void updateTouch() {
        if (!(touchInput instanceof TUIOTouchInput)) {
            if (!this.isDrawing()) {
                return;
            }
        }

        if (touchInput == null) {
            return;
        }

        // Warning TODO: Hack.. V_V 
        // Touch in 2D  mode has boundaries. 
        // Touch in 3D mode has no boundaries. 
        touchInput.computeOutsiders(!this.isDraw2D());

        if (touchInput instanceof TUIOTouchInput) {
            touchInput.computeOutsiders(true);
        }

        touchList = touchInput.projectTouchToScreen(this, getDisplay());
        touchList.sortAlongYAxis();

        touchList.addOffset(touchOffset);

        if (touchInput instanceof DepthTouchInput) {
            if (((DepthTouchInput) (touchInput)).isUseRawDepth()) {
                touchList.invertY(drawingSize);
            } else if (this.isDraw2D()) {
                touchList.invertY(drawingSize);
            } //                touchList.scaleBy(drawingSize);
        }

    }

    static private final int DEFAULT_TOUCH_SIZE = 15;

    /**
     * Draw the touch points, good for debug.
     */
    protected void drawTouch() {
        drawTouch(DEFAULT_TOUCH_SIZE);
    }

    /**
     * Draw the touch points at a given size.
     *
     * @param ellipseSize size of the points.
     */
    protected void drawTouch(int ellipseSize) {
        for (Touch t : touchList) {
            if (t.is3D) {
                // fill(185, 142, 62);
            } else {
            
                if (t.trackedSource != null && t.trackedSource.mainFinger) {
                       fill(58, 190, 52);
                    ellipse(t.position.x, t.position.y, ellipseSize*1.5f, ellipseSize*1.5f);
                } else {
                        fill(58, 71, 198);
                    ellipse(t.position.x, t.position.y, ellipseSize, ellipseSize);
                }
            }
        }
    }

    /**
     * Draw the touch, all the touch founds.
     *
     * @param ellipseSize
     */
    protected void drawFullTouch(int ellipseSize) {
        for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
            } else {
                fill(58, 71, 198);
            }
            ellipse(t.pposition.x, t.pposition.y, ellipseSize / 2, ellipseSize / 2);
            pushMatrix();
            translate(t.position.x, t.position.y);
            ellipse(0, 0, ellipseSize, ellipseSize);
            line(0, 0, t.speed.x * 4, t.speed.y * 4);
            popMatrix();
        }
    }

    /**
     * Draw the touch, with previous locations.
     */
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

    /**
     * List of the touch Points. See the TouchList class, it is a wrapper class
     * for ArrayList (Touch).
     *
     * @return list of Touch objects.
     */
    public TouchList getTouchList() {
        return touchList;
    }

    /**
     * The touchInput is the main element that gives touch elements. Ex: a
     * depthTouchInput for depth camera, or a colorTouchInput for colored
     * inputs.
     *
     * @return
     */
    public TouchInput getTouchInput() {
        return touchInput;
    }

}
