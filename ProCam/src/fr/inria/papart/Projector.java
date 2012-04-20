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

public class Projector {

    private PApplet parent;
    public GLGraphicsOffScreen graphics;
    public ArrayList<Screen> screens;
    // TODO: this has to be useless.
//    protected GLTexture finalImage;
    // Projector information
    protected ProjectorDevice proj;
    protected PMatrix3D projIntrinsicsP3D, projExtrinsicsP3D, projExtrinsicsP3DInv;
    // Resolution
    protected int frameWidth, frameHeight;
    // OpenGL information
    private float[] projectionMatrixGL = new float[16];
    protected GLTexture myMap;
    public PMatrix3D modelview1;
    protected PMatrix3D projectionInit;
    protected GLTextureFilter lensFilter;
    private GL gl = null;

    /**
     * Projector allows the use of a projector for Spatial Augmented reality setup. 
     * This class creates an OpenGL context which allows 3D projection.
     * 
     * @param parent
     * @param calibrationYAML calibration file : OpenCV format
     * @param width  resolution X
     * @param height resolution Y
     * @param near   OpenGL near plane (in mm) or the units used for calibration.
     * @param far    OpenGL far plane  (in mm) or the units used for calibration.
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

        // create the offscreen rendering for this projector.
        if (AA > 0) {
            graphics = new GLGraphicsOffScreen(parent, width, height, true, AA);
        } else {
            graphics = new GLGraphicsOffScreen(parent, width, height);
        }

        screens = new ArrayList<Screen>();
        loadInternalParams(calibrationYAML);
        initProjection(near, far);
        initModelView();
        initDistortMap(proj);
    }

    private void loadInternalParams(String calibrationYAML) {
        // Load the camera parameters.
        try {

            ProjectorDevice[] p = ProjectorDevice.read(calibrationYAML);
            if (p.length > 0) {
                proj = p[0];
            }

            double[] projMat = proj.cameraMatrix.get();
            double[] projR = proj.R.get();
            double[] projT = proj.T.get();
            projIntrinsicsP3D = new PMatrix3D((float) projMat[0], (float) projMat[1], (float) projMat[2], 0,
                    (float) projMat[3], (float) projMat[4], (float) projMat[5], 0,
                    (float) projMat[6], (float) projMat[7], (float) projMat[8], 0,
                    0, 0, 0, 1);
            projExtrinsicsP3D = new PMatrix3D((float) projR[0], (float) projR[1], (float) projR[2], (float) projT[0],
                    (float) projR[3], (float) projR[4], (float) projR[5], (float) projT[1],
                    (float) projR[6], (float) projR[7], (float) projR[8], (float) projT[2],
                    0, 0, 0, 1);

            projExtrinsicsP3DInv = projExtrinsicsP3D.get();
            projExtrinsicsP3DInv.invert();

        } catch (Exception e) {
            // TODO: Exception creation !!
            System.out.println("Error !!!!!");
            System.err.println("Error reading the calibration file : " + calibrationYAML + " \n" + e);
        }
    }

    private void initProjection(float near, float far) {
        float p00, p11, p02, p12;

        // ----------- OPENGL --------------
        // Reusing the internal projector parameters for the scene rendering.

        PMatrix3D oldProj = graphics.projection.get();

        // Working params
        p00 = 2 * projIntrinsicsP3D.m00 / frameWidth;
        p11 = 2 * projIntrinsicsP3D.m11 / frameHeight;
        p02 = -(2 * projIntrinsicsP3D.m02 / frameWidth - 1);
        p12 = -(2 * projIntrinsicsP3D.m12 / frameHeight - 1);

        graphics.beginDraw();

        // TODO: magic numbers !!!
        graphics.frustum(0, 0, 0, 0, near, far);
        graphics.projection.m00 = p00;
        graphics.projection.m11 = p11;
        graphics.projection.m02 = p02;
        graphics.projection.m12 = p12;

        // Save these good parameters
        projectionInit = graphics.projection.get();

        graphics.projection.transpose();
        graphics.projection.get(projectionMatrixGL);

        graphics.projection.set(oldProj);
        graphics.endDraw();
    }

    private void initModelView() {
        graphics.beginDraw();
        graphics.resetMatrix();
//        graphics.scale(1, 1, -1);
        modelview1 = graphics.modelview.get();
        graphics.endDraw();
    }

    /**
     * graphics.modelview.apply(projExtrinsicsP3D);
     * @return
     */
    public PMatrix3D getExtrinsics() {
        return projExtrinsicsP3D;
    }

    /**
     * This function initializes the distorsion map used by the distorsion shader.
     * The texture is of the size of the projector resolution.
     * @param proj
     */
    private void initDistortMap(ProjectorDevice proj) {
        lensFilter = new GLTextureFilter(parent, "projDistort.xml");
//        finalImage = new GLTexture(parent, frameWidth, frameHeight);

        myMap = new GLTexture(parent, frameWidth, frameHeight, GLTexture.FLOAT);
        float[] mapTmp = new float[frameWidth * frameHeight * 3];
        int k = 0;
        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {

                double[] out = proj.undistort(x, y);
                mapTmp[k++] = (float) out[0] / frameWidth;
                mapTmp[k++] = (float) out[1] / frameHeight;
                mapTmp[k++] = 0;
            }
        }
        myMap.putBuffer(mapTmp, 3);
    }

    // Actual GLGraphics BUG :  projection has to be loaded directly into OpenGL.
    public void loadProjection() {
        gl = graphics.beginGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadMatrixf(projectionMatrixGL, 0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        graphics.endGL();
    }

    public void unLoadProjection() {
        gl = graphics.beginGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        graphics.endGL();
    }

    public void loadModelView() {
        graphics.modelview.set(getModelview1());
    }

    public GLGraphicsOffScreen loadGraphics() {

//	graphics.beginDraw();
//	graphics.clear(0);
//	graphics.endDraw();

        loadProjection();
        loadModelView();

        return graphics;
    }

    // TODO: un truc genre hasTouch // classe héritant
    public void loadTouch() {
        for (Screen screen : screens) {
            screen.initTouch(this);
        }
    }

    public void drawScreens() {

        ////////  3D PROJECTION  //////////////
        graphics.beginDraw();

        // clear the screen
        graphics.clear(0);

        // load the projector parameters into OpenGL
        loadProjection();

        // make the modelview matrix as the default matrix
        graphics.resetMatrix();

        // Setting the projector as a projector (inverse camera)
        graphics.scale(1, 1, -1);

        // Place the projector to his projection respective to the origin (camera here)
        graphics.modelview.apply(getExtrinsics());

        for (Screen screen : screens) {
            if(!screen.isDrawing())
                continue;

            graphics.pushMatrix();

            // Goto to the screen position
            graphics.modelview.apply(screen.getPos());

            // Draw the screen image
            graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);
            graphics.popMatrix();
        }

        // Put the projection matrix back to normal
        unLoadProjection();
        graphics.endDraw();
    }

    public PImage distort() {
        GLTexture off = graphics.getTexture();

        // TODO: BUG : works once, cannot be disabled and enabled
        off = graphics.getTexture();
        lensFilter.apply(new GLTexture[]{off, myMap}, off);

        return off;
//        parent.image(finalImage, posX, posY, frameWidth, frameHeight);
    }

    public void addScreen(Screen s) {
        screens.add(s);
    }

    public GLGraphicsOffScreen getGraphics() {
        return this.graphics;
    }

    // TODO: public or protected ?
    public PMatrix3D getModelview1() {
        return this.modelview1;
    }

    public PMatrix3D getProjectionInit() {
        return this.projectionInit;
    }

    public ProjectorDevice getProjectorDevice() {
        return this.proj;
    }

    //    /**
//     * TODO: find the use of this function ?? 
//     * 
//     * @param position
//     * @return 
//     */
//    public PVector computePosOnPaper(PVector position){
//	  graphics.pushMatrix();
//	  PVector ret = new PVector();   
//	  graphics.translate(position.x, position.y, position.z);
//	  projExtrinsicsP3DInv.mult(new PVector(graphics.modelview.m03, 
//						graphics.modelview.m13, 
//						-graphics.modelview.m23),
//				    ret);   
//	  graphics.popMatrix();
//	  return ret;
//    }
    public int getWidth() {
        return frameWidth;
    }

    public int getHeight() {
        return frameHeight;
    }
}
