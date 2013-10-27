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

    TouchElement touch;
    TouchInput touchInput;
    ArrayList<Button> buttons;

    public PaperTouchScreen(PApplet parent,
            MarkerBoard board,
            PVector size,
            float resolution,
            Camera cam,
            Projector proj,
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
    public void update() {
        super.update();
        
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
        // T
//        g.clear(0, 0);
        g.scale(resolution);
        g.background(20, 20);
        
        drawTouch(g, 20);
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
