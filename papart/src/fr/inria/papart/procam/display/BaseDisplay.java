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
package fr.inria.papart.procam.display;

import fr.inria.papart.drawingapp.DrawUtils;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.Screen;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jiii
 */
public class BaseDisplay {

    public PGraphicsOpenGL graphics;
    public ArrayList<Screen> screens = new ArrayList<Screen>();
    protected PApplet parent;
    //    public PGraphicsOpenGL graphicsUndist;
    protected static int DEFAULT_SIZE = 200;
    protected boolean registered = false;
    protected int frameWidth = DEFAULT_SIZE, frameHeight = DEFAULT_SIZE;
    protected int drawingSizeX = DEFAULT_SIZE, drawingSizeY = DEFAULT_SIZE;
    protected float quality = 1;

    public BaseDisplay() {
        setParent(Papart.getPapart().getApplet());
    }

    public BaseDisplay(PApplet applet) {
        setParent(applet);
    }

    public BaseDisplay(PApplet applet, int quality) {
        setParent(applet);
        this.quality = quality;
    }

    public void init() {
        this.graphics = (PGraphicsOpenGL) parent.createGraphics((int) (frameWidth * quality),
                (int) (frameHeight * quality), PApplet.OPENGL);
        automaticMode();
    }

    public void automaticMode() {
        registered = true;
        // Before drawing. 
        parent.registerMethod("pre", this);
        // At the end of drawing.
        parent.registerMethod("draw", this);
    }

    public void manualMode() {
        registered = false;
        parent.unregisterMethod("draw", this);
        parent.unregisterMethod("pre", this);
    }

    public void registerAgain() {
        if (!registered) {
            return;
        }
        manualMode();
        automaticMode();
    }

    /**
     * Called in automatic mode.
     */
    public void pre() {
        this.clear();
    }

    public void clear() {
        this.graphics.beginDraw();
        this.graphics.clear();
        this.graphics.endDraw();
    }

    public PGraphicsOpenGL beginDraw() {
        this.graphics.beginDraw();
        return this.graphics;
    }

    public PGraphicsOpenGL beginDrawOnScreen(Screen screen) {
        // Get the markerboard viewed by the camera
        PMatrix3D screenPos = screen.getLocation();
        this.beginDraw();
        this.graphics.applyMatrix(screenPos);
        return this.graphics;
    }

    public void endDraw() {
        this.graphics.endDraw();

    }

    /**
     * Called in Automatic mode to display the image.
     */
    public void draw() {
        parent.g.background(100, 0, 0);
        drawScreensOver();
        parent.noStroke();
        parent.g.image(this.render(), 0, 0, this.drawingSizeX, this.drawingSizeY);
    }

    public PImage render() {
        return this.graphics;
    }

    public void drawScreens() {
        this.beginDraw();
        this.graphics.clear();
        renderScreens();
        this.endDraw();
    }

    public void drawScreensOver() {
        this.beginDraw();
        renderScreens();
        this.endDraw();
    }

    public void renderScreens() {
        for (Screen screen : screens) {
            if (!screen.isDrawing()) {
                continue;
            }
            this.graphics.pushMatrix();
            this.graphics.applyMatrix(screen.getLocation());
            this.graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);
            this.graphics.popMatrix();
        }
    }

    public PGraphicsOpenGL getGraphics() {
        return this.graphics;
    }

    private void setParent(PApplet parent) {
        this.parent = parent;
    }

    public float getQuality() {
        return quality;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void setFrameSize(int width, int height) {
        this.frameWidth = width;
        this.frameHeight = height;
    }

    public int getWidth() {
        return frameWidth;
    }

    public int getHeight() {
        return frameHeight;
    }

    public void setDrawingSize(int w, int h) {
        this.drawingSizeX = w;
        this.drawingSizeY = h;
    }

    public void addScreen(Screen s) {
        screens.add(s);
    }

    public PVector projectPointer(Screen screen, float x, float y) {

        PMatrix3D screenMat = screen.getLocation().get();
        screenMat.invert();
        PVector transformed = new PVector();
        screenMat.mult(new PVector(x * drawingSizeX, y * drawingSizeY), transformed);
        return transformed;
    }
}
