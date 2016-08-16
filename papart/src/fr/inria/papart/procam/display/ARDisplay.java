/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2014-2016 Inria
 * Copyright (C) 2011-2013 Bordeaux University
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; If not, see
 * <http://www.gnu.org/licenses/>.
 */
package fr.inria.papart.procam.display;

import fr.inria.papart.calibration.PlaneCalibration;
import processing.opengl.PGraphicsOpenGL;
import org.bytedeco.javacv.ProjectiveDevice;
import fr.inria.papart.drawingapp.DrawUtils;
import fr.inria.papart.multitouch.TouchInput;
import fr.inria.papart.multitouch.TouchPoint;
import fr.inria.papart.procam.HasCamera;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.HasExtrinsics;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.procam.ProjectiveDeviceP;
import fr.inria.papart.procam.Screen;
import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PShader;
import toxi.geom.Plane;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremy
 */
public class ARDisplay extends BaseDisplay implements HasExtrinsics{

//    public PGraphicsOpenGL graphicsUndist;
    private PImage mapImg;
    // Projector information
    protected ProjectiveDevice projectiveDevice;
    protected ProjectiveDeviceP projectiveDeviceP;
    protected PMatrix3D intrinsics, extrinsics, extrinsicsInv;
    protected boolean hasExtrinsics;
// OpenGL information
    protected float[] projectionMatrixGL = new float[16];
    protected PMatrix3D projectionInit = new PMatrix3D();
    // TODO...
    protected PShader lensFilter;
    protected PMatrix3D invProjModelView;
    protected Camera camera = null;

    protected float zNear = 20, zFar = 10000;

    private boolean distort = false;

    public ARDisplay(PApplet parent, String calibrationYAML) {
        super(parent);
        loadCalibration(calibrationYAML);
    }

    public ARDisplay(PApplet parent, Camera camera) {
        super(parent);
        this.camera = camera;
        this.hasCamera = true;
        setCalibration(camera.getProjectiveDevice());
    }

    @Override
    public void init() {
        this.graphics = (PGraphicsOpenGL) parent.createGraphics((int) (frameWidth * quality),
                (int) (frameHeight * quality), PApplet.OPENGL);
//        screens = new ArrayList<Screen>();
        updateIntrinsicsRendering();

        if (distort) {
            initDistortMap();
        }
        automaticMode();
    }

    protected void loadCalibration(String calibrationYAML) {
// Load the camera parameters.
        try {
//            pdp = ProjectiveDeviceP.loadProjectiveDevice(calibrationYAML, 0);
            projectiveDeviceP = ProjectiveDeviceP.loadCameraDevice(parent, calibrationYAML, 0);
            setCalibration(projectiveDeviceP);
        } catch (Exception e) {
            System.out.println("ARDisplay, Error at loading internals !!" + e);
        }
    }

    protected void setCalibration(ProjectiveDeviceP pdp) {
        // Load the camera parameters.
//            pdp = ProjectiveDeviceP.loadProjectiveDevice(calibrationYAML, 0);

        this.setIntrinsics(pdp.getIntrinsics());
        if (pdp.hasExtrinsics()) {
            this.setExtrinsics(pdp.getExtrinsics());
        }
        this.projectiveDeviceP = pdp;
        this.projectiveDevice = pdp.getDevice();
        this.frameWidth = pdp.getWidth();
        this.frameHeight = pdp.getHeight();
        this.drawingSizeX = frameWidth;
        this.drawingSizeY = frameHeight;
        
        // TODO: no more distorsions Processing3 test 
        // this.setDistort(pdp.handleDistorsions());
        this.setDistort(false);
    }

    public void updateIntrinsicsRendering() {
        float p00, p11, p02, p12;

        // ----------- OPENGL --------------
        // Reusing the internal projective parameters for the scene rendering.
        p00 = 2 * intrinsics.m00 / frameWidth;
        p11 = 2 * intrinsics.m11 / frameHeight;

        // Inverted because a camera is pointing towards a negative z...
        p02 = -(intrinsics.m02 / frameWidth * 2 - 1);
        p12 = -(intrinsics.m12 / frameHeight * 2 - 1);

        this.graphics.beginDraw();

        this.graphics.frustum(0, 0, 0, 0, zNear, zFar);

        this.graphics.projection.m00 = p00;
        this.graphics.projection.m11 = p11;
        this.graphics.projection.m02 = p02;
        this.graphics.projection.m12 = p12;

        // Save these good parameters
        projectionInit.set(this.graphics.projection);
        this.graphics.endDraw();
    }

    /**
     * Called in automatic mode.
     */
    @Override
    public void pre() {
        this.clear();
    }

    /**
     * Called in Automatic mode to display the image.
     */
    @Override
    public void draw() {
        drawScreensOver();
        parent.noStroke();

        if (camera != null && camera.getPImage() != null) {
            parent.image(camera.getPImage(), 0, 0, parent.width, parent.height);
//            ((PGraphicsOpenGL) (parent.g)).image(camera.getPImage(), 0, 0, frameWidth, frameHeight);
        }

        // TODO: Distorsion problems with higher image space distorisions (useless ?)
        DrawUtils.drawImage((PGraphicsOpenGL) parent.g,
                this.render(),
                0, 0, parent.width, parent.height);
    }

    /**
     * Draw an image.
     *
     * @param g graphics context to draw on.
     * @param image Image to be drawn
     * @param x location x
     * @param y location y
     * @param width
     * @param height
     */
    public void drawImage(PGraphicsOpenGL g, PImage image,
            int x, int y,
            int width, int height) {
        DrawUtils.drawImage(g,
                image,
                x, y, width, height);
    }

    @Override
    public void renderScreens() {
        this.graphics.noStroke();
        
        for (Screen screen : screens) {
            if (!screen.isDrawing()) {
                continue;
            }
            this.graphics.pushMatrix();

            // Goto to the screen position
            this.graphics.applyMatrix(screen.getLocation(this.getCamera()));
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

    /**
     * graphics.modelview.apply(projExtrinsicsP3D);
     *
     * @return
     */
    public PMatrix3D getIntrinsics() {
        return intrinsics;
    }

    /* *
     *  For hand-made calibration exercices. 
     */
    public void setIntrinsics(PMatrix3D intr) {
        intrinsics = intr;
    }

    /* *
     *  For custom calibration. 
     */
    public void setExtrinsics(PMatrix3D extr) {
        extrinsics = extr.get();
        extrinsicsInv = extr.get();
        extrinsicsInv.invert();
        this.hasExtrinsics = true;
    }

    /**
     * graphics.modelview.apply(projExtrinsicsP3D);
     *
     * @return
     */
    @Override
    public PMatrix3D getExtrinsics() {
        assert (hasExtrinsics());
        return extrinsics.get();
    }

    /**
     * @return
     */
    public PMatrix3D getExtrinsicsInv() {
        assert (hasExtrinsics());
        return extrinsicsInv.get();
    }

    /**
     * This function initializes the distorsion map used by the distorsion
     * shader. The texture is of the size of the projector resolution.
     *
     * @param proj
     */
    private void initDistortMap() {
        if (!projectiveDeviceP.handleDistorsions()) {
            return;
        }

//        lensFilter = parent.loadShader("fr/inria/papart/procam/distortFrag.glsl", "distortVert.glsl");
        lensFilter = parent.loadShader(
                ARDisplay.class.getResource("distortFrag.glsl").toString(),
                ARDisplay.class.getResource("distortVert.glsl").toString());
        mapImg = parent.createImage((int) (quality * frameWidth), (int) (quality * frameHeight), PApplet.RGB);
        mapImg.loadPixels();

        // Maximum disparity, in pixels
        float mag = 30;

        parent.colorMode(PApplet.RGB, 1.0f);
        int k = 0;
        for (int y = 0; y < mapImg.height; y++) {
            for (int x = 0; x < mapImg.width; x++) {

                // get the points without the scale
                int x1 = (int) ((float) x / quality);
                int y1 = (int) ((float) y / quality);

                double[] out = projectiveDevice.undistort(x1, y1);
//                double[] out = proj.distort(x, y);

                // get back at the rendering resolution
                out[0] *= quality;
                out[1] *= quality;

                float r = ((float) out[0] - x) / mag + 0.5f;/// frameWidth; 
                float g = ((float) out[1] - y) / mag + 0.5f;// / frameHeight; 

                mapImg.pixels[k++] = parent.color(r, g, parent.random(1f));
            }

        }
        mapImg.updatePixels();

        parent.colorMode(PApplet.RGB, 255);

        lensFilter.set("mapTex", mapImg);
        lensFilter.set("texture", this.graphics);
        lensFilter.set("resX", (int) (frameWidth * quality));
        lensFilter.set("resY", (int) (frameHeight * quality));
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
        return this.projectiveDevice;
    }

    public ProjectiveDeviceP getProjectiveDeviceP() {
        return this.projectiveDeviceP;
    }

    public void clear() {
        this.graphics.beginDraw();
        this.graphics.clear();
        this.graphics.endDraw();
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

    @Override
    public PGraphicsOpenGL beginDrawOnScreen(Screen screen) {
        PMatrix3D screenPos = screen.getLocation(this.camera);

        this.beginDraw();
        if (this.hasExtrinsics()) {
            screenPos.preApply(getExtrinsics());
        }
        this.graphics.applyMatrix(screenPos);
        return this.graphics;
    }

    /**
     * Warning advanced use. Load the modelview to render object from the
     * ARDisplay (camera or projector) 's point of view.
     */
    public void loadModelView() {
        // make the modelview matrix as the default matrix
        this.graphics.resetMatrix();

        // Setting the projector negative because ARToolkit provides neg Z values
        this.graphics.scale(1, 1, -1);

        // TODO: check !
        this.graphics.scale(1f / quality);

    }

    public void endDraw() {

        // Put the projection matrix back to normal
        unLoadProjection();
        this.graphics.endDraw();

    }

    public void setDistort(boolean distort) {
        this.distort = distort;
    }

    /**
     * Note: The distortions for the view are important for Projectors. For
     * cameras it is not necessary. And not desired if the rendering image is
     * scaled.
     *
     * @return graphics context
     */
    @Override
    public PGraphicsOpenGL render() {
        if (distort) {
            if (!this.projectiveDeviceP.handleDistorsions()) {
                System.err.println("I cannot distort the display, it is not in the calibration.");
                return this.graphics;
            }
            this.graphics.filter(lensFilter);

        }
        return this.graphics;
    }

    public void drawScreens() {
        this.beginDraw();
        this.graphics.clear();
        renderScreens();
        this.endDraw();
    }

    public void drawScreensOver() {
        this.beginDraw();
        renderScreens();
        this.endDraw();
    }

    // We consider px and py are normalized screen or subScreen space... 
    @Override
    public PVector projectPointer(Screen screen, float px, float py) {
//        double[] undist = proj.undistort(px * getWidth(), py * getHeight());
//
//        // go from screen coordinates to normalized coordinates  (-1, 1) 
//        float x = (float) undist[0] / getWidth() * 2 - 1;
//        float y = (float) undist[1] / getHeight() * 2 - 1;

        PVector originP = new PVector(0, 0, 0);
        PVector viewedPtP = projectiveDeviceP.pixelToWorldNormP((int) (px * frameWidth), (int) (py * frameHeight));

        Ray3D ray
                = new Ray3D(new Vec3D(originP.x,
                                originP.y,
                                originP.z),
                        new Vec3D(viewedPtP.x,
                                viewedPtP.y,
                                viewedPtP.z));

        // 3D intersection with the screen plane. 
        ReadonlyVec3D inter = screen.getPlane().getIntersectionWithRay(ray);
//        dist = screen.plane.intersectRayDistance(ray);

        if (inter == null) {
            return TouchInput.NO_INTERSECTION;
        }

        // 3D -> 2D transformation
        Vec3D res = screen.getWorldToScreen().applyTo(inter);
        PVector out = new PVector(res.x() / res.z(),
                1f - (res.y() / res.z()), 1);
        return out;
    }
            
     /**
     * Computes the 3D coordinates of a projected pixel in the tracking camera
     * coordinate system.
     *
     * @param planeCalibCam projection plane
     * @param px x axis in pixel coordinates
     * @param py x axis in pixel coordinates
     * @return
     */
    public PVector getProjectedPointOnPlane(PlaneCalibration planeCalibCam, float px, float py) {
        // Create ray from the projector (origin / viewed pixel)
        // Intersect this ray with the piece of paper.
        // Compute the Two points for the ray
        PVector originP = new PVector(0, 0, 0);
        PVector viewedPtP = getProjectiveDeviceP().pixelToWorldNormalized(px, py);

        // Pass it to the camera point of view (origin)
        PMatrix3D proCamExtrinsics = getExtrinsicsInv();
        PVector originC = new PVector();
        PVector viewedPtC = new PVector();
        proCamExtrinsics.mult(originP, originC);
        proCamExtrinsics.mult(viewedPtP, viewedPtC);

        // Second argument is a direction
        viewedPtC.sub(originC);

        Ray3D ray = new Ray3D(new Vec3D(originC.x,
                originC.y,
                originC.z),
                new Vec3D(viewedPtC.x,
                        viewedPtC.y,
                        viewedPtC.z));

        // Intersect ray with Plane
        ReadonlyVec3D inter = planeCalibCam.getPlane().getIntersectionWithRay(ray);

        if (inter == null) {
            return TouchInput.NO_INTERSECTION;
        }

        return new PVector(inter.x(), inter.y(), inter.z());
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

    public void setZNearFar(float near, float far) {
        this.zNear = near;
        this.zFar = far;
    }

    @Override
    public boolean hasExtrinsics() {
        return this.hasExtrinsics;
    }

    @Override
    public Camera getCamera() {
        return this.camera;
    }

}
