/* 
 * Copyright (C) 2014 Jeremy Laviole <jeremy.laviole@inria.fr>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.inria.papart.procam;

import static fr.inria.papart.procam.Utils.toVec;
import fr.inria.papart.tools.Homography;
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
public class Screen {

    private PApplet parent;
    // The current graphics
    private PGraphicsOpenGL thisGraphics;
    // Position holding...
    private PMatrix3D initPosM = null;

    // Either one, or the other is unique to this object. 
    // The other one is unique to the camera/markerboard couple. 
    private float[] posFloat;
    private PMatrix3D transformation = new PMatrix3D(); // init to avoid nullPointerExceptions 

    private boolean isFloatArrayUpdating;

    ////////////
    private PVector size;
    private float scale;
    protected Plane plane = new Plane();
    private static final int nbPaperPosRender = 4;
    private final PVector[] paperPosCorners3D = new PVector[nbPaperPosRender];
    protected Homography homography;
    protected Matrix4x4 transformationProjPaper;
    public float halfEyeDist = 10; // 2cm
    private boolean isDrawing = true;
    private boolean isOpenGL = false;

    public Screen(PApplet parent, PVector size, float scale) {
        this(parent, size, scale, false, 1);
    }

    public Screen(PApplet parent, PVector size, float scale, boolean useAA, int AAValue) {
        // AA not available anymore
        // Now it is loading when use, to save memory (for PaperScreens)
//        thisGraphics = (PGraphicsOpenGL) parent.createGraphics((int) (size.x * scale), (int) (size.y * scale), PApplet.OPENGL);

//        thisGraphics = new PGraphicsOpenGL(); 
//        thisGraphics.setPrimary(false);
//        thisGraphics.setSize((int) (size.x * scale), (int) (size.y * scale));
        this.size = size.get();
        this.scale = scale;
        this.parent = parent;
        initHomography();
//        initImageGetter();
    }

    // Get the texture to display...
    public PGraphicsOpenGL getTexture() {
        return getGraphics();
    }

    public PGraphicsOpenGL getGraphics() {
        if (thisGraphics == null) {
            thisGraphics = (PGraphicsOpenGL) parent.createGraphics((int) (size.x * scale), (int) (size.y * scale), PApplet.OPENGL);
        }

        return thisGraphics;
    }

    public void setPos(PMatrix3D position) {
        position = position.get();
    }

    // The board must be registered with the camera. 
    public void setAutoUpdatePos(Camera camera, MarkerBoard board) {
        if (!camera.tracks(board)) {
            camera.trackMarkerBoard(board);
        }

        isFloatArrayUpdating = board.useFloatArray();
        if (this.isFloatArrayUpdating) {
            posFloat = board.getTransfo(camera);
            transformation = new PMatrix3D();
        } else {
//            System.out.println("Getting the original transfo");

            transformation = board.getTransfoMat(camera);
            posFloat = new float[12];
        }

    }

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

    private PMatrix3D transfo = null;

    /**
     * Set an additional transformation to the current location.
     *
     * @param tr
     */
    public void setTransformation(PMatrix3D tr) {
        if (transfo == null) {
            this.transfo = new PMatrix3D(tr);
        } else {
            this.transfo.set(tr);
        }
    }

    public void setTranslation(PVector tr) {
        setTranslation(tr.x, tr.y, tr.z);
    }

    // TODO: replace transfo == null
    public void setTranslation(float x, float y, float z) {
        if (transfo == null) {
            transfo = new PMatrix3D();
        }
        transfo.reset();
        transfo.translate(x, y, z);
    }

    // TODO clearer on this method
    public PMatrix3D getPosition() {
        if (transfo == null) {
            return transformation;
        } else {
            PMatrix3D tmp = transformation.get();
            tmp.apply(transfo);
            return tmp;
        }
    }

    public float getScale() {
        return this.scale;
    }

    /**
     * update the internals of the screen to match the tracking.
     */
    public void updatePos() {

//        System.out.println("Screen: updatePos" + posPMatrix);
        if (this.isFloatArrayUpdating) {
            transformation.set(posFloat[0], posFloat[1], posFloat[2], posFloat[3],
                    posFloat[4], posFloat[5], posFloat[6], posFloat[7],
                    posFloat[8], posFloat[9], posFloat[10], posFloat[11],
                    0, 0, 0, 1);
        }

    }

    // Never Used  ?
    // Never Used ?
//    @Deprecated
//    public void updatePos(Camera camera, MarkerBoard board) {
//
//        posFloat = board.getTransfo(camera);
//
//        if (posPMatrix == null) {
//            posPMatrix = new PMatrix3D(posFloat[0], posFloat[1], posFloat[2], posFloat[3],
//                    posFloat[4], posFloat[5], posFloat[6], posFloat[7],
//                    posFloat[8], posFloat[9], posFloat[10], posFloat[11],
//                    0, 0, 0, 1);
//        }
//
//        posPMatrix.set(posFloat[0], posFloat[1], posFloat[2], posFloat[3],
//                posFloat[4], posFloat[5], posFloat[6], posFloat[7],
//                posFloat[8], posFloat[9], posFloat[10], posFloat[11],
//                0, 0, 0, 1);
//
//    }
    public void computeScreenPosTransform() {

        ///////////////////// PLANE COMPUTATION  //////////////////
        PMatrix3D mat = this.getPosition().get();

        paperPosCorners3D[0] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(size.x, 0, 0);
        paperPosCorners3D[1] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(0, size.y, 0);
        paperPosCorners3D[2] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(-size.x, 0, 0);
        paperPosCorners3D[3] = new PVector(mat.m03, mat.m13, mat.m23);

        plane = new Plane(new Triangle3D(toVec(paperPosCorners3D[0]), toVec(paperPosCorners3D[1]), toVec(paperPosCorners3D[2])));

        for (int i = 0; i < 4; i++) {
            homography.setPoint(true, i, paperPosCorners3D[i]);
        }
        homography.compute();
        transformationProjPaper = homography.getTransformation();
    }

    public PVector[] getCornerPos() {
        computeScreenPosTransform();
        return paperPosCorners3D;
    }

    ////////////////// 3D SPACE TO PAPER HOMOGRAPHY ///////////////
    // Version 2.0 :  (0,0) is the top-left corner.
    private void initHomography() {
        homography = new Homography(parent, 3, 2, 4);
        homography.setPoint(false, 0, new PVector(0, 0));
        homography.setPoint(false, 1, new PVector(1, 0));
        homography.setPoint(false, 2, new PVector(1, 1));
        homography.setPoint(false, 3, new PVector(0, 1));

//        homography.setPoint(false, 0, new PVector(0, 1));
//        homography.setPoint(false, 1, new PVector(1, 1));
//        homography.setPoint(false, 2, new PVector(1, 0));
//        homography.setPoint(false, 3, new PVector(0, 0));
    }

    public void initDraw(PVector userPos) {
        initDraw(userPos, 40, 5000);
    }

    public void initDraw(PVector userPos, float nearPlane, float farPlane) {
        initDraw(userPos, nearPlane, farPlane, false, false, true);
    }
//    public PGraphicsOpenGL initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {
//        return initDraw(userPos, nearPlane, farPlane, isAnaglyph, isLeft, isOnly);
//    }
    // TODO: optionnal args.
    private PVector userPosCam = null;

    // TODO:remettre ça...
//    public PGraphicsOpenGL initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {
//        return initDraw(userPos, nearPlane, farPlane, isAnaglyph, isLeft, isOnly);
//    }
    public void initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {

        PGraphicsOpenGL graphics = getGraphics();

        PVector userP = userPos.get();

        // Magic numbers...
        userP.x = -userP.x;
        userP.y = -userP.y;
        userP.add(20, -120, 0);

        if (initPosM == null) {
            this.isOpenGL = true;
            // Transformation  Camera -> Marker

            initPosM = this.getPosition().get();
            // No translation mode
//            initPosM.m03 = 0;
//            initPosM.m13 = 0;
//            initPosM.m23 = 0;

            initPosM.m00 = 1;
            initPosM.m11 = 1;
            initPosM.m22 = 1;
            initPosM.m01 = 0;
            initPosM.m02 = 0;
            initPosM.m10 = 0;
            initPosM.m12 = 0;
            initPosM.m20 = 0;
            initPosM.m21 = 0;

            // Transformation  Camera -> Center of screen
//            initPosM.preApply(tr1);
            // Maybe the Z axis is inverted ? Or any other axis ?
            initPosM.invert();

//            initPosM.print();
            // Now we have  Cam ->  new 3D origin
        }

        PMatrix3D newPos = transformation.get();
        // goto center...
//        newPos.preApply(tr1);

        // Compute the difference with the initial...
        newPos.preApply(initPosM);

        // No rotation 
        newPos.m00 = 1;
        newPos.m11 = 1;
        newPos.m22 = 1;
        newPos.m01 = 0;
        newPos.m02 = 0;
        newPos.m10 = 0;
        newPos.m12 = 0;
        newPos.m20 = 0;
        newPos.m21 = 0;

        // Compute the new transformation   
        PVector virtualPos = userP.get();

        if (isAnaglyph) {
            virtualPos.add(isLeft ? -halfEyeDist : halfEyeDist, 0, 0);
        }

        newPos.mult(virtualPos, virtualPos);

        PMatrix3D rotationPaper = this.getPosition().get();
        rotationPaper.invert();
        rotationPaper.m03 = 0; // newPos.m03;
        rotationPaper.m13 = 0; // newPos.m13;
        rotationPaper.m23 = 0; // newPos.m23;

        PVector paperCameraPos = new PVector();
        rotationPaper.mult(virtualPos, paperCameraPos);

        paperCameraPos.x = -paperCameraPos.x + newPos.m30;
        paperCameraPos.y = paperCameraPos.y - newPos.m31;
        paperCameraPos.z = -paperCameraPos.z + newPos.m32;

//
//        if (isOnly) {
//            graphics.beginDraw();
//            graphics.clear();
//        }
        // Camera must look perpendicular to the screen. 
        graphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
                paperCameraPos.x, paperCameraPos.y, 0,
                0, 1, 0);

//        graphics.camera(userPos.x, userPos.y, userPos.z,
//                userPos.x, userPos.y, 0,
//                0, 1, 0);
//        graphics.modelview.preApply(newPos);
        ///////////////////////////////////////////////////////////
//        PMatrix3D currentTransfo = pos2.get();
        // No translation
//        currentTransfo.m03 = 0;
//        currentTransfo.m13 = 0;
//        currentTransfo.m23 = 0;
//         currentTransfo.invert();
//        currentTransfo.preApply(initPosM);
//        currentTransfo.mult(virtualPos, paperCameraPos);
        /////////// GOOOD ONE ////////////////////
//        virtualPos.mult(-1);
//
//        // Get the current paperSheet position
//        PMatrix3D rotationPaper = pos.get();
//        rotationPaper.invert();
//
//        rotationPaper.m03 = 0;
//        rotationPaper.m13 = 0;
//        rotationPaper.m23 = 0;   // inverse of the Transformation (without position)
//
//        rotationPaper.mult(virtualPos, paperCameraPos);
        /////////// GOOOD ONE ////////////////////
//        paperCameraPos.x -= this.size.x / 2;
//        paperCameraPos.y -= this.size.y / 2;
//        if (initPos == null) {
//            initPos = paperCameraPos.get();
//        } else {
//            paperCameraPos = initPos;
//        }
        // http://www.gamedev.net/topic/597564-view-and-projection-matrices-for-vr-window-using-head-tracking/
        float nearFactor = nearPlane / paperCameraPos.z;

        float left = nearFactor * (-size.x / 2f - paperCameraPos.x);
        float right = nearFactor * (size.x / 2f - paperCameraPos.x);
        float top = nearFactor * (size.y / 2f - paperCameraPos.y);
        float bottom = nearFactor * (-size.y / 2f - paperCameraPos.y);

//        graphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
//                paperCameraPos.x, paperCameraPos.y, 0,
//                0, 1, 0);
//
//        float nearFactor = nearPlane / paperCameraPos.z;
//
//        float left = nearFactor * (-size.x / 2f - paperCameraPos.x);
//        float right = nearFactor * (size.x / 2f - paperCameraPos.x);
//        float top = nearFactor * (size.y / 2f - paperCameraPos.y);
//        float bottom = nearFactor * (-size.y / 2f - paperCameraPos.y);
        graphics.frustum(left, right, bottom, top, nearPlane, farPlane);
        graphics.projection.m11 = -graphics.projection.m11;

        // No detection?
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

        PGraphicsOpenGL projGraphics = projector.getGraphics();
        PMatrix3D projMat = projector.projectionInit.get();
        PMatrix3D modvw = projGraphics.modelview.get();

        double[] mouseDist = projector.projectiveDevice.undistort(mouseX, mouseY);
        float x = 2 * (float) mouseDist[0] / (float) width - 1;
        float y = 2 * (float) mouseDist[1] / (float) height - 1;

        PVector vect = new PVector(x, y, 1);
        PVector transformVect = new PVector();
        PVector transformVect2 = new PVector();
        projMat.apply(modvw);
        projMat.invert();
        projMat.mult(vect, transformVect);
        vect.z = (float) 0.85;
        projMat.mult(vect, transformVect2);
        //    println(skip / 10f);
        Ray3D ray = new Ray3D(new Vec3D(transformVect.x, transformVect.y, transformVect.z),
                new Vec3D(transformVect2.x, transformVect2.y, transformVect2.z));

        ReadonlyVec3D res = plane.getIntersectionWithRay(ray);
        return res;
    }

    public PVector getZMinMax() {

        float znear = 300000;
        float zfar = 0;

        for (int i = 0; i < 4; i++) {

            if (paperPosCorners3D[i].z < znear) {
                znear = paperPosCorners3D[i].z;
            }
            if (paperPosCorners3D[i].z > zfar) {
                zfar = paperPosCorners3D[i].z;
            }

        }
        return new PVector(znear, zfar);
    }

    public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
    }

}
