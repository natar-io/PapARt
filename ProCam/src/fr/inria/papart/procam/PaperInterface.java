/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.multitouchKinect.TouchElement;
import fr.inria.papart.multitouchKinect.TouchInput;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class PaperInterface {

    Screen screen;
    MarkerBoard board;
    TouchElement touch;
    protected PVector drawingSize;
    Camera cameraTracking;
    Projector projector;
    float resolution;
    TouchInput touchInput;
    ArrayList<Button> buttons;
    PApplet parent;

    public PaperInterface(PApplet parent, MarkerBoard board,
            PVector size, Camera cam,
            float resolution, Projector proj,
            TouchInput touchinput) {

        this.parent = parent;
        this.board = board;
        this.drawingSize = size.get();
        this.cameraTracking = cam;
        this.projector = proj;
        this.resolution = resolution;
        this.touchInput = touchinput;

        this.buttons = new ArrayList<Button>();

        this.screen = new Screen(parent, size, resolution);
        screen.setAutoUpdatePos(cameraTracking, board);
        projector.addScreen(screen);

        board.setDrawingMode(cameraTracking, true, 25);
        board.setFiltering(cameraTracking, 30, 25);
    }

    ///// Load ressources ////////
    public void init() {
    }

    public void update() {
        screen.updatePos();
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
    public void draw() {

        GLGraphicsOffScreen g = screen.getGraphics();
        g.beginDraw();
        g.clear(0, 0);
        g.scale(resolution);
        g.background(20, 20);
        g.endDraw();

    }

    protected void drawTouch(GLGraphicsOffScreen g, int ellipseSize) {

        if (!touch.position2D.isEmpty()) {
            for (PVector v : touch.position2D) {
                PVector p1 = new PVector(v.x * drawingSize.x,
                        v.y * drawingSize.y);
                g.ellipse(p1.x, p1.y, ellipseSize, ellipseSize);
            }
        }

    }
}
