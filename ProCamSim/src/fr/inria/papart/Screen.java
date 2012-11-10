package fr.inria.papart;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import fr.inria.papart.tools.Homography;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.*;
import toxi.processing.ToxiclibsSupport;

/**
 * This class implements a virtual screen. The position of the screen has to be
 * passed. It no longers handles a camera.
 *
 * @author jeremylaviole
 */
public class Screen {

    //       private PVector userPos = new PVector(-paperSheetWidth/2, -paperSheetHeight/2 +500, 300);
    //       private PVector userPos = new PVector(paperSheetWidth/2, paperSheetHeight/2, 500);
    //    public PVector userPos = new PVector(0, -700, 1300);
    private PApplet parent;
    // The current graphics
    public GLGraphicsOffScreen thisGraphics;
    // Position holding...
    private float[] pos3D;
    private Vec3D posPaper;
    private PVector posPaperP;
    private PMatrix3D pos;
    private PVector size;
    private float scale;
    protected Plane plane = new Plane();
    private static final int nbPaperPosRender = 4;
    private PVector[] paperPosCorners3D = new PVector[nbPaperPosRender];
    protected Homography homography;
    protected Matrix4x4 transformationProjPaper;
    private float halfEyeDist = 20; // 2cm
    private boolean isDrawing = true;
    private OneEuroFilter[] filters = null;

    // Simulation API
    public PVector position;
    
    public Screen(PApplet parent, PVector size, float scale) {
        this(parent, size, scale, false, 1);
    }

    public Screen(PApplet parent, PVector size, float scale, boolean useAA, int AAValue) {
        thisGraphics = new GLGraphicsOffScreen(parent, (int) (size.x * scale), (int) (size.y * scale), useAA, AAValue);
        this.size = size.get();
        this.scale = scale;
        this.parent = parent;
        pos = new PMatrix3D();
        posPaper = new Vec3D();
        posPaperP = new PVector();
        
        // Simulation API
        this.position = new PVector(0, 0); 
//        initImageGetter();
    }
    
    
    // **** Simulation API
    public void setPositionSim(PVector pos){
        
        
    }
    
    

    public void setFiltering(double freq, double minCutOff) {
        if (filters == null) {
            initFilters(freq);
        }
        try {
            for (int i = 0; i < 12; i++) {
                filters[i].setFrequency(freq);
                filters[i].setMinCutoff(minCutOff);
            }
        } catch (Exception e) {
        }

    }

    private void initFilters(double freq) {
        try {
            for (int i = 0; i < 12; i++) {
                filters[i] = new OneEuroFilter(freq);
            }
        } catch (Exception e) {
        }
    }

    public GLTexture getTexture() {
        return thisGraphics.getTexture();
    }

    public void computeScreenPosTransform() {

    }

    public GLGraphicsOffScreen getGraphics() {
        return thisGraphics;
    }

    public GLGraphicsOffScreen initDraw(PVector userPos) {
        return initDraw(userPos, 40, 5000);
    }

    public GLGraphicsOffScreen initDraw(PVector userPos, float nearPlane, float farPlane) {
        return initDraw(userPos, nearPlane, farPlane, false, false, true);
    }

    public GLGraphicsOffScreen initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {
        return initDraw(userPos, nearPlane, farPlane, isAnaglyph, isLeft, isOnly, thisGraphics);
    }
    // TODO: optionnal args.

    public GLGraphicsOffScreen initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly, GLGraphicsOffScreen graphics) {
 
        // Nothing to do here... as a simulation
        
        return graphics;
    }

    ///////////////////// POINTER PROJECTION  ////////////////
    // GluUnproject
    // TODO: not working ???
    /**
     * UNSAFE DO NOT USE
     *
     * @param projector
     * @param mouseX
     * @param mouseY
     * @param width
     * @param height
     * @return
     */
    public ReadonlyVec3D projectMouse(Projector projector, int mouseX, int mouseY, int width, int height) {

return null;
    }

    public boolean setAutoUpdatePos(Camera camera, MarkerBoard board) {
       return true;
    }

//    public void setManualUpdatePos() {
//        pos3D = new float[16];
//    }
    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
    }

    public PVector getSize() {
        return size;
    }

    public int getDrawSizeX() {
        return (int) (size.x * scale);
    }

    public int getDrawSizeY() {
        return (int) (size.y * scale);
    }

    public PMatrix3D getPos() {
        return pos;
    }

    public float getScale() {
        return this.scale;
    }

    // Available only if pos3D is being updated elsewhere...
    public void updatePos() {

    }

    public void setPos(PMatrix3D position) {
        pos = position.get();
    }

    // Available only if pos3D is being updated elsewhere...
    public void updatePos(Camera camera, MarkerBoard board) {

    }

    public PVector getZMinMax() {
        return new PVector(200, 2000);
    }
    
}
