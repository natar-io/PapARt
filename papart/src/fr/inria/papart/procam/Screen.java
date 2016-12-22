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
package fr.inria.papart.procam;

import fr.inria.papart.tracking.MarkerBoardInvalid;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.calibration.HomographyCalibration;
import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.utils.MathUtils;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.procam.display.ProjectorDisplay;
import static fr.inria.papart.utils.MathUtils.toVec;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import processing.opengl.PGraphicsOpenGL;
import toxi.geom.*;

/**
 * This class implements a virtual screen. The position of the screen has to be
 * passed. It no longers handles a camera.
 *
 * @author jeremylaviole
 */
public class Screen implements HasExtrinsics {

    private PApplet parent;
    // The current graphics
    private PGraphicsOpenGL thisGraphics;
    // Position holding...
    private PMatrix3D initPosM = null;

    // Either one, or the other is unique to this object. 
    // The other one is unique to the camera/markerboard couple. 
//    private PMatrix3D transformation = new PMatrix3D(); // init to avoid nullPointerExceptions 
    private PMatrix3D extrinsics = new PMatrix3D();
    private MarkerBoard markerBoard = MarkerBoardInvalid.board;

    ////////////
    private PVector size = new PVector(200, 200);
    private float scale = 1;
    protected Plane plane = new Plane();

    private static final int nbPaperPosRender = 4;
    private final PVector[] paperPosCorners3D = new PVector[nbPaperPosRender];

    // TODO: update this again
    private HomographyCreator homography;
    protected HomographyCalibration worldToScreen;
    public float halfEyeDist = 10; // 2cm
    private boolean isDrawing = true;
    private boolean isOpenGL = false;

    public Screen(PApplet parent) {
        this.parent = parent;
        initHomography();
    }

    public void setSize(PVector size) {
        this.size = size;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }


    public void linkTo(MarkerBoard board) {
        this.markerBoard = board;
    }

    public boolean hasMarkerBoard() {
        return this.markerBoard == MarkerBoardInvalid.board;
    }

    public MarkerBoard getMarkerBoard() {
        if (!this.hasMarkerBoard()) {
            System.err.println("The screen " + this + " does not a markerboard...");
        }
        return this.markerBoard;
    }

    // Get the texture to display...
    public PGraphicsOpenGL getTexture() {
        return getGraphics();
    }

    public PGraphicsOpenGL getGraphics() {
        if (thisGraphics == null) {
            thisGraphics = (PGraphicsOpenGL) parent.createGraphics((int) (size.x * scale), (int) (size.y * scale), PApplet.P3D);
        }

        return thisGraphics;
    }

    // The board must be registered with the camera. 
    /**
     * **
     * @deprecated
     *
     */
//    public void setAutoUpdatePos(Camera camera, MarkerBoard board) {
//        if (!camera.tracks(board)) {
//            camera.trackMarkerBoard(board);
//        }
//
//        isFloatArrayUpdating = board.useFloatArray();
//        if (this.isFloatArrayUpdating) {
//            posFloat = board.getTransfo(camera);
//            transformation = new PMatrix3D();
//        } else {
////            System.out.println("Getting the original transfo");
//
//            transformation = board.getTransfoMat(camera);
//            posFloat = new float[12];
//        }
//    }
    public boolean isOpenGL() {
        return isOpenGL;
    }

    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
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

    /**
     * Set a second transformation applied after tracking transform.
     *
     * @param tr
     */
    public void setTransformation(PMatrix3D tr) {
        if (extrinsics == null) {
            this.extrinsics = new PMatrix3D(tr);
        } else {
            this.extrinsics.set(tr);
        }
    }

    /**
     * Set an additional translation (replace the second transformation)
     *
     * @param tr
     */
    public void setTranslation(PVector tr) {
        setTranslation(tr.x, tr.y, tr.z);
    }

    /**
     * Set an additional translation (replace the second transformation)
     *
     * @param x
     * @param y
     * @param z
     */
    public void setTranslation(float x, float y, float z) {
        if (extrinsics == null) {
            extrinsics = new PMatrix3D();
        }
        extrinsics.reset();
        extrinsics.translate(x, y, z);
    }

    /**
     * Get a copy of the overall transform (after tracking and second
     * transform).
     *
     * @param camera
     * @return
     */
    public PMatrix3D getLocation(Camera camera) {
        if (!markerBoard.isTrackedBy(camera)) {
            return extrinsics.get();
        }

        PMatrix3D combinedTransfos = getMainLocation(camera);
        combinedTransfos.apply(extrinsics);
        return combinedTransfos;
    }
    
    protected PMatrix3D getMainLocation(Camera camera){
        return markerBoard.getTransfoMat(camera).get();
    }

    /**
     * Get a copy of the overall transform (after tracking and second
     * transform).
     *
     * @return
     */
    public PMatrix3D getLocation(PMatrix3D trackedLocation) {
        PMatrix3D combinedTransfos = trackedLocation.get();
        combinedTransfos.apply(extrinsics);
        return combinedTransfos;
    }

    public float getScale() {
        return this.scale;
    }

    /**
     * update the internals of the screen to match the tracking.
     */
    @Deprecated
    public void updatePos(Camera camera, MarkerBoard board) {
        System.err.println("ERROR Depracted call updatePos. ");
//        transformation.set(board.getTransfoMat(camera));
    }

    protected void updatePos() {

    }

    public void computeScreenPosTransform(Camera camera) {

        ///////////////////// PLANE COMPUTATION  //////////////////
        PMatrix3D mat = this.getLocation(camera);

        paperPosCorners3D[0] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(size.x, 0, 0);
        paperPosCorners3D[1] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(0, size.y, 0);
        paperPosCorners3D[2] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(-size.x, 0, 0);
        paperPosCorners3D[3] = new PVector(mat.m03, mat.m13, mat.m23);

        plane = new Plane(new Triangle3D(MathUtils.toVec(paperPosCorners3D[0]), MathUtils.toVec(paperPosCorners3D[1]), MathUtils.toVec(paperPosCorners3D[2])));

        homography.addPoint(paperPosCorners3D[0], new PVector(0, 0));
        homography.addPoint(paperPosCorners3D[1], new PVector(1, 0));
        homography.addPoint(paperPosCorners3D[2], new PVector(1, 1));
        homography.addPoint(paperPosCorners3D[3], new PVector(0, 1));
        worldToScreen = homography.getHomography();
    }

    public PVector[] getCornerPos(Camera camera) {
        computeScreenPosTransform(camera);
        return paperPosCorners3D;
    }

    ////////////////// 3D SPACE TO PAPER HOMOGRAPHY ///////////////
    // Version 2.0 :  (0,0) is the top-left corner.
    private void initHomography() {
        homography = new HomographyCreator(3, 2, 4);

    }

    public void initDraw(Camera cam, PVector userPos) {
        initDraw(cam, userPos, 40, 5000);
    }

    public void initDraw(Camera cam, PVector userPos, float nearPlane, float farPlane) {
        initDraw(cam, userPos, nearPlane, farPlane, false, false, true);
    }
    // TODO: optionnal args.

    public void initDraw(Camera cam, PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {

        PGraphicsOpenGL graphics = getGraphics();

        if (initPosM == null) {
            this.isOpenGL = true;
            // Transformation  Camera -> Marker

            initPosM = this.getLocation(cam);

            initPosM.translate(this.getDrawSizeX() / 2, this.getDrawSizeY() / 2);
            // All is relative to the paper's center. not the corner. 
            initPosM.scale(-1, 1, -1);

        }

        // get the current transformation... 
        PMatrix3D newPos = this.getLocation(cam);

        newPos.translate(this.getDrawSizeX() / 2, this.getDrawSizeY() / 2);
        newPos.scale(-1, 1, -1);

        newPos.invert();
        newPos.apply(initPosM);

        PVector user = new PVector();

        if (isAnaglyph && isLeft) {
            userPos.add(-halfEyeDist * 2, 0, 0);
        }
        newPos.mult(userPos, user);
        PVector paperCameraPos = user;

        // Camera must look perpendicular to the screen. 
        graphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
                paperCameraPos.x, paperCameraPos.y, 0,
                0, 1, 0);

        // http://www.gamedev.net/topic/597564-view-and-projection-matrices-for-vr-window-using-head-tracking/
        float nearFactor = nearPlane / paperCameraPos.z;

        float left = nearFactor * (-size.x / 2f - paperCameraPos.x);
        float right = nearFactor * (size.x / 2f - paperCameraPos.x);
        float top = nearFactor * (size.y / 2f - paperCameraPos.y);
        float bottom = nearFactor * (-size.y / 2f - paperCameraPos.y);

        graphics.frustum(left, right, bottom, top, nearPlane, farPlane);
        graphics.projection.m11 = -graphics.projection.m11;

        // No detection?
        PMatrix3D transformation = this.getLocation(cam);
        if (transformation.m03 == 0 && transformation.m13 == 0 && transformation.m23 == 0) {
            resetPos();
        }
    }

    public void endDrawPerspective() {
        PGraphicsOpenGL graphics = getGraphics();
        graphics.perspective();
        graphics.camera();
        graphics.projection.m11 = -graphics.projection.m11;
    }

    public void resetPos() {
        initPosM = null;
    }

    public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
    }

    public Plane getPlane() {
        return plane;
    }

    public HomographyCalibration getWorldToScreen() {
        return worldToScreen;
    }

    /**
     * Set the main position (override tracking system). Use only after the call
     * of paperScreen.useManualLocation(false);
     * Lock for 10hours. 
     * @param position
     * @param cam
     */
    public void setMainLocation(PMatrix3D position, Camera cam) {
        this.setFakeLocation(cam, position);
    }
    
//    /**
//     * Set the main position by the tracking system. 
//     * @param cam
//     */
//    public void setTrackedLocation(Camera cam) {
//        this.blockUpdate(cam, 0); // ms
//    }

    public void forceUpdate(Camera camera, int time) {
        markerBoard.forceUpdate(camera, time);
    }

    public void blockUpdate(Camera camera, int time) {
        markerBoard.blockUpdate(camera, time);
    }

    public void setFiltering(Camera camera, double freq, double minCutOff) {
        markerBoard.setFiltering(camera, freq, minCutOff);
    }

    public void setDrawingMode(Camera camera, boolean dm) {
        markerBoard.setDrawingMode(camera, dm);
    }

    public void setDrawingMode(Camera camera, boolean dm, float dist) {
        markerBoard.setDrawingMode(camera, dm, dist);
    }

    public void setFakeLocation(Camera camera, PMatrix3D location) {
        markerBoard.setFakeLocation(camera, location);
    }

    public boolean isMoving(Camera camera) {
        return markerBoard.isMoving(camera);
    }

    public boolean isSeenBy(Camera camera, ProjectorDisplay projector, float error) {
        return markerBoard.isSeenBy(camera, projector, error);
    }

    @Override
    public boolean hasExtrinsics() {
        return true;
    }

    @Override
    public PMatrix3D getExtrinsics() {
        return this.extrinsics;
    }

}
