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

public class PaperScreen {

    protected Screen screen;
    protected MarkerBoard board;
    protected PVector drawingSize;
    protected Camera cameraTracking;
    protected Projector projector;
    protected float resolution;
    protected PApplet parent;

    public PaperScreen(PApplet parent, 
            MarkerBoard board,
            PVector size, 
            float resolution,
            Camera cam,
            Projector proj) {

        this.parent = parent;
        this.board = board;
        this.drawingSize = size.get();
        this.cameraTracking = cam;
        this.projector = proj;
        this.resolution = resolution;

        this.screen = new Screen(parent, size, resolution);
        projector.addScreen(screen);

        if(!cam.tracks(board))
          cam.trackMarkerBoard(board);
        
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

    // Example Draw... to check ? Or put it as begin / end ...
    public void draw() {

        PGraphicsOpenGL g = screen.getGraphics();
        g.beginDraw();
        // T
//        g.clear(0, 0);
        g.scale(resolution);
        g.background(0, 100, 200);
        g.endDraw();

    }

    
}
