package fr.inria.papart.procam;

import com.googlecode.javacv.ProjectiveDevice;
import com.googlecode.javacv.ProjectorDevice;
import fr.inria.papart.drawingapp.DrawUtils;
import fr.inria.papart.multitouchKinect.TouchPoint;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
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
        this(parent, calibrationYAML, width, height, near, far, 1);
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

    public PGraphicsOpenGL beginDrawOnScreen(Screen screen) {

        this.beginDraw();

        ///////// New version /////////////
        // Get the markerboard viewed by the camera
        PMatrix3D camBoard = screen.getPos();
        camBoard.preApply(getExtrinsics());
        this.graphics.modelview.apply(camBoard);

//        // Place the projector to his projection respective to the origin (camera here)
//        this.graphics.modelview.apply(getExtrinsics());
//
//        // Goto to the screen position
//        this.graphics.modelview.apply(screen.getPos());

        return this.graphics;
    }

//   @deprecated
    public PGraphicsOpenGL beginDrawOnBoard(Camera camera, MarkerBoard board) {

        this.beginDraw();

        ///////// New version /////////////
        // Get the markerboard viewed by the camera
        PMatrix3D camBoard = board.getTransfoMat(camera);
        camBoard.preApply(getExtrinsics());
        this.graphics.modelview.apply(camBoard);

        ////////// old Version  //////////////
//        // Place the projector to his projection respective to the origin (camera here)
//         this.graphics.modelview.apply(getExtrinsics());
//
//        // Goto to the screen position
//        this.graphics.modelview.apply(board.getTransfoMat(camera));

        return this.graphics;
    }

    public void drawOnBoard(Camera camera, MarkerBoard board) {
        loadModelView();
        PMatrix3D camBoard = board.getTransfoMat(camera);
        camBoard.preApply(getExtrinsics());
        this.graphics.modelview.apply(camBoard);
    }

    public void endDrawOnScreen() {

        // Put the projection matrix back to normal
        unLoadProjection();
        this.graphics.endDraw();
    }

    @Override
    public void drawScreens() {
        this.beginDraw();
        this.graphics.clear();
        this.graphics.background(0);
        drawScreensProjection();
        this.endDraw();
    }

    public void drawScreensLegacy() {
        this.beginDraw();
        this.graphics.clear();
        this.graphics.background(0);
        drawScreensProjectionLegacy();
        this.endDraw();
    }

    public void drawScreensOver() {

        this.beginDraw();
        drawScreensProjection();
        this.endDraw();
    }

    public void drawScreensProjection() {
        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.modelview.apply(getExtrinsics());

        super.renderScreens();
    }

    private void drawScreensProjectionLegacy() {

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
    @Override
    public PVector projectPointer(Screen screen, float px, float py) {

//        float x = px * 2 - 1;
//        float y = py * 2 - 1;

        double[] undist = proj.undistort(px * getWidth(), py * getHeight());

        // go from screen coordinates to normalized coordinates  (-1, 1) 
        float x = (float) undist[0] / getWidth() * 2 - 1;
        float y = (float) undist[1] / getHeight() * 2 - 1;

        // Not the cleaniest method...
        PMatrix3D invProjModelView1 = createProjection(screen.getZMinMax());
        invProjModelView1.scale(1, 1, -1);
        invProjModelView1.apply(getExtrinsics());
        invProjModelView1.invert();

        PVector p1 = new PVector(x, y, -1f);
        PVector p2 = new PVector(x, y, 1f);
        PVector out1 = new PVector();
        PVector out2 = new PVector();

        // view of the point from the projector.
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
                res.y() / res.z(), 1);
        return out;
    }


}
