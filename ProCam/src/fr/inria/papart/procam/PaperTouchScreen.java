/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.opengl.PGraphicsOpenGL;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.multitouchKinect.TouchElement;
import fr.inria.papart.multitouchKinect.TouchInput;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class PaperTouchScreen extends PaperScreen {

    protected TouchElement touch;
    protected TouchInput touchInput;
    protected ArrayList<Button> buttons;
    public boolean isTranslated = false;

    public PaperTouchScreen(PApplet parent,
            MarkerBoard board,
            PVector size,
            float resolution,
            Camera cam,
            ARDisplay proj,
            TouchInput touchinput) {

        super(parent, board, size,
                resolution,
                cam, proj);

        this.touchInput = touchinput;
        this.buttons = new ArrayList<Button>();
    }

    ///// Load ressources ////////
    @Override
    public void init() {
    }

    @Override
    public void pre() {
        super.pre();

        if (!isTranslated && screen.isDrawing()) {
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
        isTranslated = true;

        if (screen.isDrawing()) {
            updateTouch();
        }
    }

    public void updateTouch() {

        if (!screen.isDrawing()) {
            System.err.println("UpdateTouch on disabled screen.");
            return;
        }
        
        screen.computeScreenPosTransform();
        touch = touchInput.projectTouchToScreen(screen, projector,
                true, true);

        if (!touch.position2D.isEmpty()) {
            for (PVector v : touch.position2D) {
                checkButtons(v.x * drawingSize.x,
                        v.y * drawingSize.y);
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

    // Example Draw... to check ? Or put it as begin / end ...
    @Override
    public void draw() {

        PGraphicsOpenGL g = screen.getGraphics();
        g.beginDraw();

        g.clear();
        g.scale(resolution);
        g.background(0, 200, 100);

        drawTouch(g, 10);
        g.endDraw();
    }

    protected void drawTouch(PGraphicsOpenGL g, int ellipseSize) {

        if (!touch.position2D.isEmpty()) {
            for (PVector v : touch.position2D) {
                PVector p1 = new PVector(v.x * drawingSize.x,
                        v.y * drawingSize.y);
                g.ellipse(p1.x, p1.y, ellipseSize, ellipseSize);
            }
        }

    }
}
