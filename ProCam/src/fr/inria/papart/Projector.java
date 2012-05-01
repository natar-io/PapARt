package fr.inria.papart;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import com.googlecode.javacv.ProjectorDevice;
import fr.inria.papart.multitouchKinect.TouchPoint;
import java.util.ArrayList;
import javax.media.opengl.GL;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Plane;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Sphere;
import toxi.geom.Vec3D;
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
            graphics = new GLGraphicsOffScreen(parent, width, height, true, AA);
        } else {
            graphics = new GLGraphicsOffScreen(parent, width, height);
        }

        screens = new ArrayList<Screen>();
        loadInternalParams(calibrationYAML);
        initProjection();
        computeInv();
        initDistortMap();
        toxi = new ToxiclibsSupport(parent, graphics);
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

    private void initProjection() {
        float p00, p11, p02, p12;

        // ----------- OPENGL --------------
        // Reusing the internal projector parameters for the scene rendering.

        PMatrix3D oldProj = graphics.projection.get();

        // Working params
        p00 = 2 * projIntrinsicsP3D.m00 / frameWidth;
        p11 = 2 * projIntrinsicsP3D.m11 / frameHeight;
        p02 = -((projIntrinsicsP3D.m02 / frameWidth) * 2 - 1);
//        p02 = -(2 * projIntrinsicsP3D.m02 / frameWidth  - 1);
//        p12 = -(2 * projIntrinsicsP3D.m12 / frameHeight - 1);
        p12 = -((projIntrinsicsP3D.m12 / frameHeight) * 2 - 1);

        graphics.beginDraw();

        // TODO: magic numbers !!!
        graphics.frustum(0, 0, 0, 0, znear, zfar);
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

    private void computeInv() {
        invProjModelView = getProjectionInit().get();
        invProjModelView.scale(1, 1, -1);
        // Set to the origin, as the plane was computed from the origin
        invProjModelView.apply(getExtrinsics());

        // invert for the inverse projection
        invProjModelView.invert();
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
     * This function initializes the distorsion map used by the distorsion
     * shader. The texture is of the size of the projector resolution.
     *
     * @param proj
     */
    private void initDistortMap() {
        lensFilter = new GLTextureFilter(parent, "projDistort.xml");
        finalImage = new GLTexture(parent, frameWidth, frameHeight);

        myMap = new GLTexture(parent, frameWidth, frameHeight, GLTexture.FLOAT);
        float[] mapTmp = new float[frameWidth * frameHeight * 3];
        int k = 0;

        for (int y = 0; y < frameHeight; y++) {
            for (int x = 0; x < frameWidth; x++) {

                double[] out = proj.undistort(x, y);
//                double[] out = proj.distort(x, y);
                mapTmp[k++] = (float) (out[0] / frameWidth);
                mapTmp[k++] = (float) (out[1] / frameHeight);
                mapTmp[k++] = 0;
            }
        }


//        double[] coord = new double[frameWidth * frameHeight * 2];
//        k = 0;
//        for (int y = 0; y < frameHeight; y++) {
//            for (int x = 0; x < frameWidth; x++) {
//                coord[k++] = x;
//                coord[k++] = y;
//            }
//        }
//
//        int k2 = 0;
//        k=0;
//        double[] out = proj.undistort(coord);
//        for (int y = 0; y < frameHeight; y++) {
//            for (int x = 0; x < frameWidth; x++) {
//
//                mapTmp[k++] = (float) (out[k2++] / frameWidth);
//                mapTmp[k++] = (float) (out[k2++] / frameHeight);
//                mapTmp[k++] = 0;
//            }
//        }

        // linear mapping
//        k = 0;
//        for (int y = 0; y < frameHeight; y++) {
//            for (int x = 0; x < frameWidth; x++) {
//                mapTmp[k++] = (float )x / (float)frameWidth;
//                mapTmp[k++] = (float) y / (float) frameHeight;
//                mapTmp[k++] = 0;
//            }
//        }
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
            if (!screen.isDrawing()) {
                continue;
            }

            graphics.pushMatrix();

            // Goto to the screen position
            graphics.modelview.apply(screen.getPos());

            // Draw the screen image
            graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);

            graphics.popMatrix();

            graphics.strokeWeight(4);
            graphics.stroke(200);
        }

        // Put the projection matrix back to normal
        unLoadProjection();
        graphics.endDraw();
    }

    /**
     * Distort (or not) the image and returns it.
     *
     * @return
     */
    public PImage distort(boolean distort) {
//        return graphics.getTexture();

        if (!distort) {
            return graphics.getTexture();
        }

        // TODO: BUG : works once, cannot be disabled and enabled
        GLTexture off = graphics.getTexture();
//        lensFilter.apply(new GLTexture[]{off, myMap}, off);
        lensFilter.apply(new GLTexture[]{off, myMap}, finalImage);
//        lensFilter.apply(new GLTexture[]{off, myMap}, off);

        return finalImage;
    }

    private PMatrix3D createProjection(PVector nearFar) {

        PMatrix3D init = graphics.projection.get();
        graphics.beginDraw();

        // TODO: magic numbers !!!
        graphics.frustum(0, 0, 0, 0, nearFar.x, nearFar.y);

        graphics.projection.m00 = projectionInit.m00;
        graphics.projection.m11 = projectionInit.m11;
        graphics.projection.m02 = projectionInit.m02;
        graphics.projection.m12 = projectionInit.m12;

        PMatrix3D out = graphics.projection.get();

        graphics.endDraw();
        graphics.projection.set(init);

        return out;
    }

    // TODO: more doc...
    /**
     * Projects the position of a pointer in normalized screen space. If you
     * need to undistort the pointer, do so before passing px and py.
     *
     * @param px Normalized x position (0,1) in projector space
     * @param py Normalized y position (0,1) in projector space
     * @return Position of the pointer.
     */
    public PVector projectPointer(Screen screen, float px, float py) {

//        float x = px * 2 - 1;
//        float y = py * 2 - 1;
        double[] undist = proj.undistort(px * getWidth(), py * getHeight());

        float x = (float) undist[0] / getWidth() * 2 - 1;
        float y = (float) undist[1] / getHeight() * 2 - 1;

        // Not the cleaniest method...
        PMatrix3D invProjModelView = createProjection(screen.getZMinMax());
        invProjModelView.scale(1, 1, -1);
        invProjModelView.apply(getExtrinsics());
        invProjModelView.invert();

        PVector p1 = new PVector(x, y, -1f);
        PVector p2 = new PVector(x, y, 1f);
        PVector out1 = new PVector();
        PVector out2 = new PVector();

        // view of the point from the projector.
        Utils.mult(invProjModelView, p1, out1);
        Utils.mult(invProjModelView, p2, out2);

        Ray3D ray = new Ray3D(new Vec3D(out1.x, out1.y, out1.z),
                new Vec3D(out2.x, out2.y, out2.z));

        ReadonlyVec3D inter = screen.plane.getIntersectionWithRay(ray);
//        dist = screen.plane.intersectRayDistance(ray);

        if (inter == null) {
            return null;
        }

        Vec3D res = screen.transformationProjPaper.applyTo(inter);
        PVector out = new PVector(res.x() / res.z(),
                res.y() / res.z(), 1);
        return out;
    }

//    public PVector projectPointer(Screen screen, TouchPoint tp) {
//        PVector out = projectPointer(screen, tp.v.x, tp.v.y);
//        out.z = screen.plane.getDistanceToPoint(tp.vKinect);
//        return out;
//    }

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
