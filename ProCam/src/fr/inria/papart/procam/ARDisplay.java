/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.opengl.PGraphicsOpenGL;
import processing.opengl.Texture;
import com.googlecode.javacv.ProjectiveDevice;
import java.util.ArrayList;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGL;
import static processing.opengl.PGL.CLAMP_TO_EDGE;
import static processing.opengl.PGL.NEAREST;
import static processing.opengl.PGL.RGBA;
import static processing.opengl.PGL.TEXTURE_2D;
import processing.opengl.PShader;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class ARDisplay {

    protected PApplet parent;
    public PGraphicsOpenGL graphics;
//    public PGraphicsOpenGL graphicsUndist;
    public ArrayList<Screen> screens;
    private PImage mapImg;
    // Projector information
    protected ProjectiveDevice proj;
    protected PMatrix3D projIntrinsicsP3D, projExtrinsicsP3D, projExtrinsicsP3DInv;
    // Resolution
    protected int frameWidth, frameHeight;
    // OpenGL information
    protected float[] projectionMatrixGL = new float[16];
    protected PMatrix3D projectionInit;
    // TODO...
    protected PShader lensFilter;
    protected GL2 gl = null;
    protected PMatrix3D invProjModelView;
    protected float znear;
    protected float zfar;
    protected ProjectiveDeviceP pdp;
    protected float resolution = 1;

    public ARDisplay(PApplet parent, String calibrationYAML,
            int width, int height, float near, float far) {
        this(parent, calibrationYAML, width, height, near, far, 1);
    }

    public ARDisplay(PApplet parent, String calibrationYAML,
            int width, int height, float near, float far, float resolution) {

        frameWidth = width;
        frameHeight = height;
        this.parent = parent;
        this.znear = near;
        this.zfar = far;

        this.resolution = resolution;

        // TODO: BROKEN: No more AA in Processing2 ! 
        this.graphics = (PGraphicsOpenGL) parent.createGraphics((int) (width * resolution), (int) (height * resolution), PApplet.OPENGL);// true, AA);
//        this.graphics = (PGraphicsOpenGL) parent.createGraphics(width ,height, PApplet.OPENGL);// true, AA);
        screens = new ArrayList<Screen>();
        loadInternalParams(calibrationYAML);
        initProjection();

        initDistortMap();
    }

    protected void loadInternalParams(String calibrationYAML) {
        // Load the camera parameters.
        try {
//            pdp = ProjectiveDeviceP.loadProjectiveDevice(calibrationYAML, 0);
            pdp = ProjectiveDeviceP.loadCameraDevice(calibrationYAML, 0);

            projIntrinsicsP3D = pdp.getIntrinsics();
            projExtrinsicsP3D = pdp.getExtrinsics();
            if (projExtrinsicsP3D != null) {
                projExtrinsicsP3DInv = projExtrinsicsP3D.get();
                projExtrinsicsP3DInv.invert();
            }

            proj = pdp.getDevice();

        } catch (Exception e) {
            System.out.println("ARDisplay, Error !!" + e);
        }
    }

    private void initProjection() {
        float p00, p11, p02, p12;

        // ----------- OPENGL --------------
        // Reusing the internal projective parameters for the scene rendering.
//        PMatrix3D oldProj = this.graphics.projection.get();
        // Working params
        p00 = 2 * projIntrinsicsP3D.m00 / frameWidth;
        p11 = 2 * projIntrinsicsP3D.m11 / frameHeight;

        // Inverted because a camera is pointing towards a negative z...
        p02 = -(projIntrinsicsP3D.m02 / frameWidth * 2 - 1);
        p12 = -(projIntrinsicsP3D.m12 / frameHeight * 2 - 1);

        this.graphics.beginDraw();

        this.graphics.frustum(0, 0, 0, 0, znear, zfar);

        this.graphics.projection.m00 = p00;
        this.graphics.projection.m11 = p11;
        this.graphics.projection.m02 = p02;
        this.graphics.projection.m12 = p12;

        // Save these good parameters
        projectionInit = this.graphics.projection.get();

//        this.graphics.projection.transpose();
//        this.graphics.projection.get(projectionMatrixGL);
//
//        this.graphics.projection.set(oldProj);
        this.graphics.endDraw();
    }

    /**
     * graphics.modelview.apply(projExtrinsicsP3D);
     *
     * @return
     */
    public PMatrix3D getExtrinsics() {
        return projExtrinsicsP3D;
    }
    /**
     * graphics.modelview.apply(projExtrinsicsP3D);
     *
     * @return
     */
    public PMatrix3D getIntrinsics() {
        return projIntrinsicsP3D;
    }

    /* *
     *  For hand-made calibration exercices. 
     */
    public void setIntrinsics(PMatrix3D intr){

        projIntrinsicsP3D = intr;
        initProjection();
    }

    /**
     * This function initializes the distorsion map used by the distorsion
     * shader. The texture is of the size of the projector resolution.
     *
     * @param proj
     */
    private void initDistortMap() {
        lensFilter = parent.loadShader("distortFrag.glsl", "distortVert.glsl"); // projDistort.xml");

//        mapImg = parent.createImage(graphics.width, graphics.height, PApplet.RGB);
        mapImg = parent.createImage((int) (resolution * frameWidth), (int) (resolution * frameHeight), PApplet.RGB);

        mapImg.loadPixels();

        // Maximum disparity, in pixels
        float mag = 30;

        parent.colorMode(PApplet.RGB, 1.0f);
        int k = 0;
        for (int y = 0; y < mapImg.height; y++) {
            for (int x = 0; x < mapImg.width; x++) {

                // get the points without the scale
                int x1 = (int) ((float) x / resolution);
                int y1 = (int) ((float) y / resolution);

                double[] out = proj.undistort(x1, y1);
//                double[] out = proj.distort(x, y);

                // get back at the rendering resolution
                out[0] *= resolution;
                out[1] *= resolution;

                float r = ((float) out[0] - x) / mag + 0.5f;/// frameWidth; 
                float g = ((float) out[1] - y) / mag + 0.5f;// / frameHeight; 

                mapImg.pixels[k++] = parent.color(r, g, parent.random(1f));
            }

        }
        mapImg.updatePixels();

        parent.colorMode(PApplet.RGB, 255);

        lensFilter.set("mapTex", mapImg);
        lensFilter.set("texture", this.graphics);
        lensFilter.set("resX", (int) (frameWidth * resolution));
        lensFilter.set("resY", (int) (frameHeight * resolution));
        lensFilter.set("mag", mag);
    }

    public void loadProjection() {
        this.graphics.projection.set(projectionInit);
    }

    public void unLoadProjection() {
    }

    public PMatrix3D getProjectionInit() {
        return this.projectionInit;
    }

    public ProjectiveDevice getProjectiveDevice() {
        return this.proj;
    }

    public ProjectiveDeviceP getProjectiveDeviceP() {
        return this.pdp;
    }

    public PGraphicsOpenGL beginDraw() {

        ////////  3D PROJECTION  //////////////
        this.graphics.beginDraw();

        // clear the screen
        // this.graphics.clear(0, 0);
        // load the projector parameters into OpenGL
        loadProjection();

        loadModelView();

        return this.graphics;
    }

    protected void loadModelView() {
        // make the modelview matrix as the default matrix
        this.graphics.resetMatrix();

        // Setting the projector negative because ARToolkit provides neg Z values
        this.graphics.scale(1, 1, -1);

        // TODO: check !
        this.graphics.scale(1f / resolution);

    }

    public void endDraw() {

        // Put the projection matrix back to normal
        unLoadProjection();
        this.graphics.endDraw();

    }

    /**
     * Note: The distorsions for the view are important for Projectors. For
     * cameras it is not necessary. And not desired if the rendering image is
     * scaled.
     *
     * @param distort
     * @return
     */
    public PGraphicsOpenGL distort(boolean distort) {

        if (distort) {
            graphics.filter(lensFilter);
            return graphics;
        }
        return this.graphics;
    }

    public void drawScreens() {
        this.beginDraw();
        this.graphics.clear();
        renderScreens();
        this.endDraw();
    }

    public void renderScreens() {

        for (Screen screen : screens) {
            if (!screen.isDrawing()) {
                continue;
            }
            this.graphics.pushMatrix();

            // Goto to the screen position
            this.graphics.modelview.apply(screen.getPos());
            // Draw the screen image

            // If it is openGL renderer, use the standard  (0, 0) is bottom left
            if (screen.isOpenGL()) {
                this.graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);
            } else {
                float w = screen.getSize().x;
                float h = screen.getSize().y;

                this.graphics.textureMode(PApplet.NORMAL);
                this.graphics.beginShape(PApplet.QUADS);
                this.graphics.texture(screen.getTexture());
                this.graphics.vertex(0, 0, 0, 0, 1);
                this.graphics.vertex(0, h, 0, 0, 0);
                this.graphics.vertex(w, h, 0, 1, 0);
                this.graphics.vertex(w, 0, 0, 1, 1);
                this.graphics.endShape();
            }
            this.graphics.popMatrix();
        }
    }

 
    // We consider px and py are normalized screen or subScreen space... 
    public PVector projectPointer(Screen screen, float px, float py) {
        float x = px * 2 - 1;
        float y = py * 2 - 1;

//        double[] undist = proj.undistort(px * getWidth(), py * getHeight());
//
//        // go from screen coordinates to normalized coordinates  (-1, 1) 
//        float x = (float) undist[0] / getWidth() * 2 - 1;
//        float y = (float) undist[1] / getHeight() * 2 - 1;
        // Not the cleaniest method...
        PMatrix3D invProjModelView1 = createProjection(screen.getZMinMax());
        invProjModelView1.scale(1, 1, -1);
        invProjModelView1.invert();

        PVector p1 = new PVector(x, y, -1f);
        PVector p2 = new PVector(x, y, 1f);
        PVector out1 = new PVector();
        PVector out2 = new PVector();

        // view of the point from the display.
        Utils.mult(invProjModelView1, p1, out1);
        Utils.mult(invProjModelView1, p2, out2);

        Ray3D ray = new Ray3D(new Vec3D(out1.x, out1.y, out1.z),
                new Vec3D(out2.x, out2.y, out2.z));

        ReadonlyVec3D inter = screen.plane.getIntersectionWithRay(ray);
//        dist = screen.plane.intersectRayDistance(ray);

        if (inter == null) {
            return null;
        }

        Vec3D res = screen.transformationProjPaper.applyTo(inter);
        PVector out = new PVector(res.x() / res.z(),
                1f- (res.y() / res.z()), 1);
        return out;
    }

    protected PMatrix3D createProjection(PVector nearFar) {

        PMatrix3D init = this.graphics.projection.get();
        this.graphics.beginDraw();

        this.graphics.frustum(0, 0, 0, 0, nearFar.x, nearFar.y);

        this.graphics.projection.m00 = projectionInit.m00;
        this.graphics.projection.m11 = projectionInit.m11;
        this.graphics.projection.m02 = projectionInit.m02;
        this.graphics.projection.m12 = projectionInit.m12;

        PMatrix3D out = this.graphics.projection.get();

        this.graphics.endDraw();
        this.graphics.projection.set(init);

        return out;
    }

    public PGraphicsOpenGL getGraphics() {
        return this.graphics;
    }

    public int getWidth() {
        return frameWidth;
    }

    public int getHeight() {
        return frameHeight;
    }

    public void addScreen(Screen s) {
        screens.add(s);
    }
}
