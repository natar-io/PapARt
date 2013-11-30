/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.opengl.PGraphicsOpenGL;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;

public class PaperScreen {

    protected Screen screen;
    protected MarkerBoard board;
    protected PVector drawingSize;
    protected Camera cameraTracking;
    protected ARDisplay projector;
    protected float resolution;
    protected PApplet parent;

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
        board.setDrawingMode(cameraTracking, true, 25);
        board.setFiltering(cameraTracking, 30, 25);
        parent.registerMethod("pre", this);
    }

    ///// Load ressources ////////
    public void init() {
    }

    public void pre() {
        screen.updatePos();
    }

    public PGraphicsOpenGL getGraphics() {
        return screen.getGraphics();
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

    public void keyPressed() {
    }

    public void keyReleased() {
    }
}
