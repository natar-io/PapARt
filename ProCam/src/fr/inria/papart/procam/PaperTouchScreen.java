/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

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

    // TODO: check -> never used. 
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
            touchList.scaleBy(drawingSize);
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

    static private final int DEFAULT_TOUCH_SIZE = 10;

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
