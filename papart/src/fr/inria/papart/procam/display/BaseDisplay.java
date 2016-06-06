/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.display;

import fr.inria.papart.procam.HasCamera;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.Screen;
import fr.inria.papart.procam.camera.Camera;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;

/**
 *
 * @author jiii
 */
public class BaseDisplay implements HasCamera {

    Semaphore sem = new Semaphore(1);
    public PGraphicsOpenGL graphics;
    public ArrayList<Screen> screens = new ArrayList<Screen>();
//    public ArrayList<PaperScreen> paperScreens = new ArrayList<>();
    
    protected PApplet parent;
    //    public PGraphicsOpenGL graphicsUndist;
    protected static int DEFAULT_SIZE = 200;
    protected boolean registered = false;
    protected int frameWidth = DEFAULT_SIZE, frameHeight = DEFAULT_SIZE;
    protected int drawingSizeX = DEFAULT_SIZE, drawingSizeY = DEFAULT_SIZE;
    protected float quality = 1;
    protected boolean hasCamera = false;

    public BaseDisplay() {
        setParent(Papart.getPapart().getApplet());
    }

    public BaseDisplay(PApplet applet) {
        setParent(applet);
        this.setDrawingSize(applet.width, applet.height);
    }

    public BaseDisplay(PApplet applet, int quality) {
        setParent(applet);
        this.quality = quality;
    }

    public void init() {
        initGraphics();
        automaticMode();
    }

    /**
     * Called in init(). Do not call manually except if you know what yeu do.
     */
    protected void initGraphics() {
        this.graphics = (PGraphicsOpenGL) parent.createGraphics((int) (frameWidth * quality),
                (int) (frameHeight * quality), PApplet.OPENGL);
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
        try {
            sem.acquire();
            this.graphics.beginDraw();
            this.graphics.clear();
            this.graphics.endDraw();

        } catch (InterruptedException ex) {
            Logger.getLogger(BaseDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
        sem.release();
    }

    public PGraphicsOpenGL beginDraw() {
        try {
            sem.acquire();
            this.graphics.beginDraw();
            return this.graphics;
        } catch (InterruptedException ex) {
            Logger.getLogger(BaseDisplay.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public PGraphicsOpenGL beginDrawOnScreen(Screen screen) {
        PMatrix3D screenPos;

        if (this.hasCamera()) {
            screenPos = screen.getLocation(this.getCamera());
        } else {
            // Get the markerboard viewed by the camera
            screenPos = screen.getExtrinsics();
        }
        this.beginDraw();
        this.graphics.applyMatrix(screenPos);
        return this.graphics;
    }

    public void endDraw() {

        this.graphics.endDraw();
        sem.release();
    }

    /**
     * Called in Automatic mode to display the image.
     */
    public void draw() {
        parent.g.background(100, 0, 0);
        drawScreensOver();
        parent.noStroke();
        parent.g.image(this.render(), 0, 0, this.drawingSizeX, this.drawingSizeY);

//        pxCopy = getPixelsCopy();
    }

    public int[] pxCopy;

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
            this.graphics.noStroke();
            this.graphics.pushMatrix();
            this.graphics.applyMatrix(screen.getLocation(new PMatrix3D()));
            this.graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);
            this.graphics.popMatrix();
        }
    }

    public PGraphicsOpenGL getGraphics() {
        return this.graphics;
    }

    public int[] getPixelsCopy() {

        int[] out;
        try {
            sem.acquire();
            this.graphics.loadPixels();
            out = new int[graphics.pixels.length];

            System.arraycopy(graphics.pixels, 0, out, 0, graphics.pixels.length);
            sem.release();
            return out;
        } catch (InterruptedException ex) {
            Logger.getLogger(BaseDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Use with care !
     *
     * @param parent
     */
    protected void setParent(PApplet parent) {
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
    
//    public void addPaperScreen(PaperScreen s) {
//        paperScreens.add(s);
//    }

    @Override
    public boolean hasCamera() {
        return hasCamera;
    }

    @Override
    public Camera getCamera() {
        return Camera.INVALID_CAMERA;
    }

    public PVector projectPointer(Screen screen, float x, float y) {
        PMatrix3D screenMat = screen.getLocation(new PMatrix3D());
        screenMat.invert();
        PVector transformed = new PVector();
        screenMat.mult(new PVector(x * drawingSizeX, y * drawingSizeY), transformed);
        return transformed;
    }
}
