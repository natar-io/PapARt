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
            int width, int height, float near, float far, int res) {

        super(parent, calibrationYAML, width, height, near, far, res);
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
            this.hasExtrinsics = true;

        } catch (Exception e) {
            System.out.println("Error !!" + e);
        }

    }

    // TODO: test & validate this. !
    public void pre() {
        this.clear();
        this.graphics.background(0);
    }

    public void draw() {
        drawScreensOver();
        parent.noStroke();
        DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                this.distort(true),
                0, 0, this.frameWidth, this.frameHeight);
    }

    //   @deprecated
    public PGraphicsOpenGL beginDrawOnBoard(Camera camera, MarkerBoard board) {

        this.beginDraw();

        ///////// New version /////////////
        // Get the markerboard viewed by the camera
        PMatrix3D camBoard = board.getTransfoMat(camera);
        camBoard.preApply(getExtrinsics());
        this.graphics.applyMatrix(camBoard);

        ////////// old Version  //////////////
//        // Place the projector to his projection respective to the origin (camera here)
//         this.graphics.modelview.apply(getExtrinsics());
//
//        // Goto to the screen position
//        this.graphics.modelview.apply(board.getTransfoMat(camera));
        return this.graphics;
    }
//   @deprecated

    public void drawOnBoard(Camera camera, MarkerBoard board) {
        loadModelView();
        PMatrix3D camBoard = board.getTransfoMat(camera);
        camBoard.preApply(getExtrinsics());
        this.graphics.applyMatrix(camBoard);
    }

    @Override
    public void drawScreens() {
        this.beginDraw();
        this.graphics.clear();
        drawScreensProjection();
        this.endDraw();
    }

    public void drawScreensLegacy() {
        this.beginDraw();
        this.graphics.clear();
        drawScreensProjectionLegacy();
        this.endDraw();
    }

    @Override
    public void drawScreensOver() {
        this.beginDraw();
        drawScreensProjection();
        this.endDraw();
    }

    public void drawScreensProjection() {
        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.applyMatrix(getExtrinsics());

        super.renderScreens();
    }

    private void drawScreensProjectionLegacy() {

        // Place the projector to his projection respective to the origin (camera here)
        this.graphics.applyMatrix(getExtrinsics());

        for (Screen screen : screens) {
            if (!screen.isDrawing()) {
                continue;
            }
            this.graphics.pushMatrix();

            // Goto to the screen position
//            this.graphics.modelview.apply(screen.getPos());
            this.graphics.applyMatrix(screen.getPos());
            // Draw the screen image

            this.graphics.image(screen.getTexture(), 0, 0, screen.getSize().x, screen.getSize().y);

            this.graphics.popMatrix();
        }
    }

    // *** Projects the pixels viewed by the projector to the screen.
    // px and py are normalized -> [0, 1] in screen space
    @Override
    public PVector projectPointer(Screen screen, float px, float py) {

        // Create ray from the projector (origin / viewed pixel)
        // Intersect this ray with the piece of paper. 
        // Compute the Two points for the ray          
        PVector originP = new PVector(0, 0, 0);
        PVector viewedPtP = pdp.pixelToWorldNormP((int) (px * frameWidth), (int) (py * frameHeight));

        // Pass it to the camera point of view (origin)
        PMatrix3D extr = projExtrinsicsP3DInv;
        PVector originC = new PVector();
        PVector viewedPtC = new PVector();
        extr.mult(originP, originC);
        extr.mult(viewedPtP, viewedPtC);

        // Second argument is a direction
        viewedPtC.sub(originC);

        Ray3D ray
                = new Ray3D(new Vec3D(originC.x,
                                originC.y,
                                originC.z),
                        new Vec3D(viewedPtC.x,
                                viewedPtC.y,
                                viewedPtC.z));

        // Intersect ray with Plane 
        ReadonlyVec3D inter = screen.plane.getIntersectionWithRay(ray);

        // It may not intersect.
        if (inter == null) {
            return null;
        }
        // Check the error of the ray casting -- Debug only  
//        PVector inter1P = new PVector();
//        projExtrinsicsP3D.mult(interP, inter1P);
//        PVector px2 = pdp.worldToPixel(inter1P, false);
//        px2.sub(px * frameWidth, py * frameHeight, 0);
//        System.out.println("Error " + px2.mag());

        // Get the normalized coordinates in Paper coordinates
        Vec3D res = screen.transformationProjPaper.applyTo(inter);
//        PVector out = new PVector(res.x(), res.y(), res.z());
        PVector out = new PVector(res.x() / res.z(),
                res.y() / res.z(), 1);
        out.y = 1 - out.y;

        // Second possiblity... (WORKING)  Use directly the 3D location instead of the plane.
//     PVector interP = new PVector(inter.x(), inter.y(), inter.z());
//        PVector out3 = new PVector();
//        PMatrix3D posInv = screen.getPos().get();
//        posInv.invert();
//        posInv.mult(interP, out3);
//        out3.x /= screen.getSize().x;
//        out3.y /= screen.getSize().y;
//
//        PVector diff = PVector.sub(out, out3);
//        System.out.println("Diff " + diff);
//        out3.y = 1 - out3.y;
        return out;
    }

}
