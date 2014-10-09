/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.opengl.PGraphicsOpenGL;
import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.multitouch.Touch;
import fr.inria.papart.multitouch.TouchInput;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;

public class PaperTouchScreen extends PaperScreen {

    // TODO: check -> never used. 
    protected ArrayList<Touch> touchList = new ArrayList<Touch>();
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

    public PaperTouchScreen(Camera cam, ARDisplay proj, TouchInput touchinput) {
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

    static private final int DEFAULT_TOUCH_SIZE = 10;

    protected void drawTouch() {
        drawTouch(currentGraphics, DEFAULT_TOUCH_SIZE);
    }

    protected void drawTouch(int ellipseSize) {
        drawTouch(currentGraphics, ellipseSize);
    }

    protected void drawTouch(PGraphicsOpenGL g, int ellipseSize) {

        if (!touchList.isEmpty()) {
            for (Touch t : touchList) {
                PVector p = t.position;
                float x = p.x * drawingSize.x;
                float y = p.y * drawingSize.y;
                if (t.is3D) {
                    g.fill(185, 142, 62);
                } else {
                    g.fill(58, 71, 198);
                }
                g.ellipse(x, y, ellipseSize, ellipseSize);
            }
        }
    }

    public ArrayList<Touch> getTouchList() {
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
