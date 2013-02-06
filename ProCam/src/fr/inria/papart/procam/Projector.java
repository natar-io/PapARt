package fr.inria.papart.procam;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.ProjectorDevice;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

public class Projector extends ARDisplay {

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

        super(parent, calibrationYAML, width, height, near, far, AA);
    }

    @Override
    protected void loadInternalParams(String calibrationYAML) {
        // Load the camera parameters.

        try {
            pdp = ProjectiveDeviceP.loadProjectorDevice(calibrationYAML, 0);

            projExtrinsicsP3D = pdp.getExtrinsics();
            projIntrinsicsP3D = pdp.getIntrinsics();
            projExtrinsicsP3DInv = projExtrinsicsP3D.get();
            projExtrinsicsP3DInv.invert();

            proj = pdp.getDevice();

        } catch (Exception e) {
            System.out.println("Error !!" + e);
        }

    }

    public void drawScreens() {

        ////////  3D PROJECTION  //////////////
        this.graphics.beginDraw();

        // clear the screen
        this.graphics.clear(0);

        // load the projector parameters into OpenGL
        loadProjection();

        // make the modelview matrix as the default matrix
        this.graphics.resetMatrix();

        // Setting the projector as a projector (inverse camera)
        this.graphics.scale(1, 1, -1);

        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.modelview.apply(getExtrinsics());

        for (Screen screen : screens) {
            if (!screen.isDrawing()) {
                continue;
            }
            this.graphics.pushMatrix();

            // Goto to the screen position
            this.graphics.modelview.apply(screen.getPos());

            // Draw the screen image
            this.graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);
            this.graphics.popMatrix();
        }

        // Put the projection matrix back to normal
        unLoadProjection();
        this.graphics.endDraw();
    }
    
    
    public void drawScreensOver() {

        ////////  3D PROJECTION  //////////////
        this.graphics.beginDraw();

        // load the projector parameters into OpenGL
        loadProjection();

        // make the modelview matrix as the default matrix
        this.graphics.resetMatrix();

        // Setting the projector as a projector (inverse camera)
        this.graphics.scale(1, 1, -1);

        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.modelview.apply(getExtrinsics());

        for (Screen screen : screens) {
            if (!screen.isDrawing()) {
                continue;
            }
            this.graphics.pushMatrix();

            // Goto to the screen position
            this.graphics.modelview.apply(screen.getPos());

            // Draw the screen image
            this.graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);
            this.graphics.popMatrix();
        }

        // Put the projection matrix back to normal
        unLoadProjection();
        this.graphics.endDraw();
    }

    public GLGraphicsOffScreen beginDrawOnScreen(Screen screen) {

        ////////  3D PROJECTION  //////////////
        this.graphics.beginDraw();

        // load the projector parameters into OpenGL
        loadProjection();

        // make the modelview matrix as the default matrix
        this.graphics.resetMatrix();

        // Setting the projector as a projector (inverse camera)
        this.graphics.scale(1, 1, -1);

        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.modelview.apply(getExtrinsics());

        // Goto to the screen position
        this.graphics.modelview.apply(screen.getPos());

        return this.graphics;
    }
    
    public GLGraphicsOffScreen beginDrawOnBoard(MarkerBoard board) {

        ////////  3D PROJECTION  //////////////
        this.graphics.beginDraw();

        // load the projector parameters into OpenGL
        loadProjection();

        // make the modelview matrix as the default matrix
        this.graphics.resetMatrix();

        // Setting the projector as a projector (inverse camera)
        this.graphics.scale(1, 1, -1);

        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.modelview.apply(getExtrinsics());

        // Goto to the screen position
        this.graphics.modelview.apply(board.getTransfoMat());

        return this.graphics;
    }

    public void endDrawOnScreen() {

        // Put the projection matrix back to normal
        unLoadProjection();
        this.graphics.endDraw();
    }

    private PMatrix3D createProjection(PVector nearFar) {

        PMatrix3D init = this.graphics.projection.get();
        this.graphics.beginDraw();

        // TODO: magic numbers !!!
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
}
