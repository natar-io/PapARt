package fr.inria.papart;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import com.googlecode.javacv.ProjectorDevice;
import java.util.ArrayList;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.processing.ToxiclibsSupport;

public class Projector {

    private PApplet parent;
    public GLGraphicsOffScreen graphics;
    public ArrayList<Screen> screens;
    // TODO: this has to be useless.
    protected GLTexture finalImage;
    // Projector information
    protected ProjectorDevice proj;
    protected PMatrix3D projIntrinsicsP3D, projExtrinsicsP3D, projExtrinsicsP3DInv;
    // Resolution
    protected int frameWidth, frameHeight;
    // OpenGL information
    private float[] projectionMatrixGL = new float[16];
    protected GLTexture myMap;
    protected PMatrix3D projectionInit;
    protected GLTextureFilter lensFilter;
    private GL gl = null;
    private PMatrix3D invProjModelView;
    protected float znear;
    protected float zfar;
    // Temporary ?
    public ToxiclibsSupport toxi;

    /**
     * Projector allows the use of a projector for Spatial Augmented reality
     * setup. This class creates an OpenGL context which allows 3D projection.
     *
     * @param parent
     * @param calibrationYAML calibration file : OpenCV format
     * @param width resolution X
     * @param height resolution Y
     * @param near OpenGL near plane (in mm) or the units used for calibration.
     * @param far OpenGL far plane (in mm) or the units used for calibration.
     */
    public Projector(PApplet parent, String calibrationYAML,
            int width, int height,
            float near, float far) {
        this(parent, calibrationYAML, width, height, near, far, 0);
    }

    public Projector(PApplet parent, String calibrationYAML,
            int width, int height, float near, float far, int AA) {

        frameWidth = width;
        frameHeight = height;
        this.parent = parent;
        this.znear = near;
        this.zfar = far;

        // create the offscreen rendering for this projector.
        if (AA > 0) {
            this.graphics = new GLGraphicsOffScreen(parent, width, height, true, AA);
        } else {
            this.graphics = new GLGraphicsOffScreen(parent, width, height);
        }

        screens = new ArrayList<Screen>();
        toxi = new ToxiclibsSupport(parent, this.graphics);
    }

    public PMatrix3D getExtrinsics() {
        return projExtrinsicsP3D;
    }

    // Actual GLGraphics BUG :  projection has to be loaded directly into OpenGL.
    public void loadProjection() {
    }

    public void unLoadProjection() {
    }

    // TODO: un truc genre hasTouch // classe héritant
    /**
     * For all the screens computes the transformation from 3D space to screen
     * space
     */
    public void loadTouch() {
        for (Screen screen : screens) {
            screen.computeScreenPosTransform();
        }
    }

    public void drawScreens() {

        ////////  3D PROJECTION  //////////////
        this.graphics.beginDraw();

        // clear the screen
        this.graphics.clear(0);

        // load the projector parameters into OpenGL
        for (Screen screen : screens) {
            this.graphics.pushMatrix();

            // Draw the screen image
            this.graphics.image(screen.getTexture(),
                    screen.position.x, screen.position.x,
                    screen.getSize().x, screen.getSize().y);
            this.graphics.popMatrix();
        }

        this.graphics.endDraw();
    }

    /**
     * Distort (or not) the image and returns it.
     *
     * @return
     */
    public PImage distort(boolean distort) {
        return this.graphics.getTexture();
    }

    public PVector projectPointer(Screen screen, float px, float py) {

        // FIXME: ERRORS HERE
        
        return new PVector(
                    (px - screen.position.x) / screen.getDrawSizeX(),
                 (py - screen.position.y) / screen.getDrawSizeY());
    }

    public void addScreen(Screen s) {
        screens.add(s);
    }

    public GLGraphicsOffScreen getGraphics() {
        return this.graphics;
    }

    // TODO: public or protected ?
//    public PMatrix3D getModelview1() {
//        return this.modelview1;
//    }
    public PMatrix3D getProjectionInit() {
        return this.projectionInit;
    }

    public ProjectorDevice getProjectorDevice() {
        return this.proj;
    }

    public int getWidth() {
        return frameWidth;
    }

    public int getHeight() {
        return frameHeight;
    }
}
