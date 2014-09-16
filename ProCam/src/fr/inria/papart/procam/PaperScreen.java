/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.exceptions.BoardNotDetectedException;
import java.lang.reflect.AccessibleObject;
import processing.opengl.PGraphicsOpenGL;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.event.KeyEvent;

public class PaperScreen {

    protected Screen screen;
    protected MarkerBoard board;
    protected PVector drawingSize;
    protected Camera cameraTracking;
    protected ARDisplay projector;
    protected float resolution;
    protected PApplet parent;
    PGraphicsOpenGL currentGraphics;

    protected boolean isDrawingOnScreen;

    public PaperScreen(PApplet parent,
            MarkerBoard board,
            PVector size,
            float resolution,
            Camera cam,
            ARDisplay proj) {
        this.parent = parent;
        this.board = board;
        this.drawingSize = size.get();
        this.cameraTracking = cam;
        this.projector = proj;
        this.resolution = resolution;

        this.screen = new Screen(parent, size, resolution);
        projector.addScreen(screen);

        if (!cam.tracks(board)) {
            cam.trackMarkerBoard(board);
        }

        screen.setAutoUpdatePos(cam, board);
//        board.setDrawingMode(cameraTracking, true, 4);
//        board.setFiltering(cameraTracking, 30, 4);

        board.setDrawingMode(cameraTracking, true, 10);
        board.setFiltering(cameraTracking, 30, 4);

        parent.registerMethod("pre", this);
        parent.registerMethod("draw", this);
        projector.registerAgain();
    }

    ///// Load ressources -> to remove ?////////
    protected void init() {

    }

    public void pre() {
        screen.updatePos();
    }

    public PGraphicsOpenGL getGraphics() {
        return screen.getGraphics();
    }

    public Screen getScreen() {
        return this.screen;
    }

    public boolean isMoving() {
        return board.isMoving(cameraTracking);
    }

    public void setLocation(PVector v) {
        setLocation(v.x, v.y, v.z);
    }

    public void setLocation(float x, float y, float z) {
        PMatrix3D screenPos = screen.getPos();
        screenPos.translate(x, y, z);
    }

    public PVector getLocationVector() {
        PMatrix3D p = screen.getPos();
        return new PVector(p.m03, p.m13, p.m23);
    }

    public PMatrix3D getLocation() {
        return this.screen.getPos();
    }

    // TODO: check this !
    public PVector getScreenPos() {
        return board.getBoardLocation(cameraTracking, projector);
    }

    public void noDraw() {
        screen.setDrawing(false);
        PGraphicsOpenGL pg = screen.getGraphics();
        pg.beginDraw();
        pg.clear();
        pg.endDraw();
    }

    public PGraphicsOpenGL beginDraw2D() {
        screen.setDrawing(true);
        PGraphicsOpenGL g = screen.getGraphics();
        g.beginDraw();
        g.scale(resolution);
        this.isDrawingOnScreen = true;
        return g;
    }

    public PGraphicsOpenGL beginDraw3D() throws BoardNotDetectedException {
        screen.setDrawing(false);
        PGraphicsOpenGL g = projector.beginDrawOnScreen(this.screen);
        this.isDrawingOnScreen = false;
        this.currentGraphics = g;
//        this.screen.getPos().print();
        return g;
    }

    public PGraphicsOpenGL beginDraw3DProjected(PVector userPos) {
        screen.setDrawing(true);
        PGraphicsOpenGL g = screen.getGraphics();
        g.beginDraw();
        this.isDrawingOnScreen = true;
        screen.initDraw(userPos);
        return g;
    }

    // Example Draw... to check ? Or put it as begin / end ...
    public void draw() {
        screen.setDrawing(true);
        PGraphicsOpenGL g = screen.getGraphics();
        g.beginDraw();
        // T
//        g.clear(0, 0);
        g.scale(resolution);
        g.background(0, 100, 200);
        g.endDraw();

    }

    /**
     * *
     * Works only in 3D mode with beginDraw3D().
     *
     * @param ps PaperScreen to go to.
     */
    public void goTo(PaperScreen ps, PGraphicsOpenGL graphics) {

        if (this.isDrawingOnScreen == true) {
            throw new RuntimeException("Impossible to draw on another board. You need to drawi using beginDraw3D() to do so.");
        }

        if (this.currentGraphics != graphics) {
            throw new RuntimeException("The given graphics context is not valid. Use the one given by beginDraw3D().");
        }

        // get the location of this board...
        PMatrix3D loc = this.getLocation().get();
        loc.invert();
        loc.apply(ps.getLocation());

        // Sun POV
        currentGraphics.applyMatrix(loc);
    }
    
    
    public MarkerBoard getBoard() {
        return board;
    }

    public PVector getDrawingSize() {
        return drawingSize;
    }

    public Camera getCameraTracking() {
        return cameraTracking;
    }

    public ARDisplay getProjector() {
        return projector;
    }

    public float getResolution() {
        return resolution;
    }

    public boolean isIsDrawingOnScreen() {
        return isDrawingOnScreen;
    }
    

    public void keyEvent(KeyEvent e) {

    }
}
