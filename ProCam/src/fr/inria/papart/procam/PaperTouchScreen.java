/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.opengl.PGraphicsOpenGL;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.multitouchKinect.Touch;
import fr.inria.papart.multitouchKinect.TouchInput;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class PaperTouchScreen extends PaperScreen {

    protected ArrayList<Touch> touchList;
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
        touchList = touchInput.projectTouchToScreen(screen, projector,
                true, true, true, true, true);

        if (!touchList.isEmpty()) {
            for (Touch t : touchList) {
                if (t.is3D) {
                    continue;
                }
                PVector p = t.p;
                checkButtons(p.x * drawingSize.x,
                        p.y * drawingSize.y);
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

        if (!touchList.isEmpty()) {
            for (Touch t : touchList) {
                PVector p = t.p;
                PVector p1 = new PVector(p.x * drawingSize.x,
                        p.y * drawingSize.y);
                if (t.is3D) {
                    g.fill(185, 142, 62);
                } else {
                    g.fill(58, 71, 198);
                }

                g.ellipse(p1.x, p1.y, ellipseSize, ellipseSize);
            }
        }
    }
}
