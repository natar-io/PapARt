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
import fr.inria.papart.multitouch.KinectTouchInput;
import fr.inria.papart.multitouch.TUIOTouchInput;
import fr.inria.papart.multitouch.TouchList;
import fr.inria.papart.multitouch.TrackedDepthPoint;
import fr.inria.papart.multitouch.TrackedElement;
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
        this(papart.getPublicCameraTracking(),
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
        if (this.touchInput != null) {
            updateTouch();
        }
    }

    @Override
    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    @Override
    public void setLocation(float x, float y, float z) {
        super.setLocation(x, y, z);
        screen.computeScreenPosTransform(cameraTracking);
        updateTouch();
    }
    
    private PVector touchOffset = new PVector();

    public PVector getTouchOffset() {
        return touchOffset.copy();
    }

    public void setTouchOffset(float x, float y) {
        this.touchOffset.set(new PVector(x,y));
    }
    public void setTouchOffset(PVector touchOffset) {
        this.touchOffset.set(touchOffset);
    }

    public void updateTouch() {
        if (!(touchInput instanceof TUIOTouchInput)) {
            if (!screen.isDrawing()) {
                return;
            }
        }
        // Warning TODO: Hack.. V_V 
        // Touch in 2D  mode has boundaries. 
        // Touch in 3D mode has no boundaries. 
        touchInput.computeOutsiders(!this.isDraw2D());

        if (touchInput instanceof TUIOTouchInput) {
            touchInput.computeOutsiders(true);
        }

        touchList = touchInput.projectTouchToScreen(screen, getDisplay());
        touchList.sortAlongYAxis();
        
        touchList.addOffset(touchOffset);

        if (touchInput instanceof KinectTouchInput) {
            if (((KinectTouchInput) (touchInput)).isUseRawDepth()) {
                touchList.invertY(drawingSize);
            } else if (this.isDraw2D()) {
                touchList.invertY(drawingSize);
            } //                touchList.scaleBy(drawingSize);
        }

    }

    // TODO: cleaning of this
    static private final int DEFAULT_TOUCH_SIZE = 15;

    protected void drawTouch() {
        drawTouch(DEFAULT_TOUCH_SIZE);
    }

    protected void drawTouch(int ellipseSize) {
        for (Touch t : touchList) {
            if (t.is3D) {
                // fill(185, 142, 62);
            } else {
                fill(58, 71, 198);
                ellipse(t.position.x, t.position.y, ellipseSize, ellipseSize);
            }
        }
    }

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

    public boolean isIsTranslated() {
        return isTranslated;
    }

    /**
     * Unsafe do not use unless you are sure.
     */
    public PVector getCameraViewOf(Touch t) {
        ProjectorDisplay projector = (ProjectorDisplay) getDisplay();

        TrackedElement tp = t.trackedSource;
        PVector screenPos = tp.getPosition();
        PVector tablePos = projector.projectPointer3D(screen, screenPos.x, screenPos.y);
        ProjectiveDeviceP pdp = cameraTracking.getProjectiveDevice();
        PVector coord = pdp.worldToPixelCoord(tablePos);
        return coord;
    }

    // TODO:  find a class for this ?!
    /**
     * Unsafe do not use unless you are sure.
     */
    public PImage getImageFrom(PVector coord, PImage src, PImage dst, int radius) {
        int x = (int) coord.x;
        int y = (int) coord.y;

        dst.copy(src,
                x - radius / 2,
                y - radius / 2,
                radius,
                radius,
                0, 0,
                radius,
                radius);
        return dst;
    }

    public int getColorAt(PVector coord) {
        int x = (int) coord.x;
        int y = (int) coord.y;
        ByteBuffer buff = cameraTracking.getIplImage().getByteBuffer();
        int offset = x + y * cameraTracking.width();
        return getColor(buff, offset);
    }

    private int getColor(ByteBuffer buff, int offset) {
        offset = offset * 3;
        return (buff.get(offset + 2) & 0xFF) << 16
                | (buff.get(offset + 1) & 0xFF) << 8
                | (buff.get(offset) & 0xFF);
    }

    public int getColorFrom3D(PVector point) {
        return getColorAt(getPxCoordinates(point));
    }

    /**
     * Unsafe do not use unless you are sure.
     */
    public PVector getPxCoordinates(PVector cameraTracking3DCoord) {
        ProjectiveDeviceP pdp = cameraTracking.getProjectiveDevice();
        PVector coord = pdp.worldToPixelCoord(cameraTracking3DCoord);
        return coord;
    }

    /**
     * Unsafe do not use unless you are sure.
     */
    public int[] getPixelsFrom(PVector coord, PImage cameraImage, int radius) {
        int[] px = new int[radius * radius];
        int x = (int) coord.x;
        int y = (int) coord.y;
        int minX = PApplet.constrain(x - radius, 0, cameraTracking.width() - 1);
        int maxX = PApplet.constrain(x + radius, 0, cameraTracking.width() - 1);
        int minY = PApplet.constrain(y - radius, 0, cameraTracking.height() - 1);
        int maxY = PApplet.constrain(y + radius, 0, cameraTracking.height() - 1);

        int k = 0;
        for (int j = minY; j <= maxY; j++) {
            for (int i = minX; i <= maxX; i++) {
                int offset = i + j * cameraTracking.width();
                px[k++] = cameraImage.pixels[offset];
            }
        }
        return px;
    }

}
