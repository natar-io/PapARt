package fr.inria.papart.procam;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import fr.inria.papart.tools.Homography;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.*;
import toxi.processing.ToxiclibsSupport;

/**
 * This class implements a virtual screen. The position of the screen has to be
 * passed. It no longers handles a camera.
 *
 * @author jeremylaviole
 */
public class Screen {

    //       private PVector userPos = new PVector(-paperSheetWidth/2, -paperSheetHeight/2 +500, 300);
    //       private PVector userPos = new PVector(paperSheetWidth/2, paperSheetHeight/2, 500);
    //    public PVector userPos = new PVector(0, -700, 1300);
    private PApplet parent;
    // The current graphics
    public GLGraphicsOffScreen thisGraphics;
    // Position holding...
    private PVector initPos = null;
    private PMatrix3D initPosM = null;
    private float[] pos3D;
    private Vec3D posPaper;
    private PVector posPaperP;
    private PMatrix3D pos;
    private PVector size;
    private float scale;
    protected Plane plane = new Plane();
    private static final int nbPaperPosRender = 4;
    private PVector[] paperPosCorners3D = new PVector[nbPaperPosRender];
    protected Homography homography;
    protected Matrix4x4 transformationProjPaper;
    private float halfEyeDist = 20; // 2cm
    private boolean isDrawing = true;
    private OneEuroFilter[] filters = null;

    public Screen(PApplet parent, PVector size, float scale) {
        this(parent, size, scale, false, 1);
    }

    public Screen(PApplet parent, PVector size, float scale, boolean useAA, int AAValue) {
        thisGraphics = new GLGraphicsOffScreen(parent, (int) (size.x * scale), (int) (size.y * scale), useAA, AAValue);
        this.size = size.get();
        this.scale = scale;
        this.parent = parent;
        pos = new PMatrix3D();
        posPaper = new Vec3D();
        posPaperP = new PVector();
        initHomography();
//        initImageGetter();
    }

    public void setFiltering(double freq, double minCutOff) {
        if (filters == null) {
            initFilters(freq);
        }
        try {
            for (int i = 0; i < 12; i++) {
                filters[i].setFrequency(freq);
                filters[i].setMinCutoff(minCutOff);
            }
        } catch (Exception e) {
        }

    }

    private void initFilters(double freq) {
        try {
            for (int i = 0; i < 12; i++) {
                filters[i] = new OneEuroFilter(freq);
            }
        } catch (Exception e) {
        }
    }

    ////////////////// 3D SPACE TO PAPER HOMOGRAPHY ///////////////
    private void initHomography() {
        homography = new Homography(parent, 3, 3, 4);
        homography.setPoint(false, 0, new PVector(0, 0, 0));
        homography.setPoint(false, 1, new PVector(1, 0, 0));
        homography.setPoint(false, 2, new PVector(1, 1, 0));
        homography.setPoint(false, 3, new PVector(0, 1, 0));
    }

    public GLTexture getTexture() {
        return thisGraphics.getTexture();
    }

    public void computeScreenPosTransform() {

        ///////////////////// PLANE COMPUTATION  //////////////////
        PMatrix3D mat = pos.get();

        PVector origin = new PVector(mat.m03, mat.m13, mat.m23);

        // got a little higher for the normal.

        mat.translate(0, 0, -20);
        PVector normal = new PVector(mat.m03, mat.m13, mat.m23);

        plane.set(new Vec3D(origin.x, origin.y, origin.z));
        plane.normal.set(new Vec3D(normal.x, normal.y, normal.z));

        // go back to the paper place
        mat.translate(0, 0, 20);

        paperPosCorners3D[0] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(size.x, 0, 0);
        paperPosCorners3D[1] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(0, size.y, 0);
        paperPosCorners3D[2] = new PVector(mat.m03, mat.m13, mat.m23);
        mat.translate(-size.x, 0, 0);
        paperPosCorners3D[3] = new PVector(mat.m03, mat.m13, mat.m23);

        for (int i = 0; i < 4; i++) {
            homography.setPoint(true, i, paperPosCorners3D[i]);
        }
        homography.compute();
        transformationProjPaper = homography.getTransformation();
    }

    public GLGraphicsOffScreen getGraphics() {
        return thisGraphics;
    }

    public GLGraphicsOffScreen initDraw(PVector userPos) {
        return initDraw(userPos, 40, 5000);
    }

    public GLGraphicsOffScreen initDraw(PVector userPos, float nearPlane, float farPlane) {
        return initDraw(userPos, nearPlane, farPlane, false, false, true);
    }

    public GLGraphicsOffScreen initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {
        return initDraw(userPos, nearPlane, farPlane, isAnaglyph, isLeft, isOnly, thisGraphics);
    }
    // TODO: optionnal args.

    public GLGraphicsOffScreen initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly, GLGraphicsOffScreen graphics) {
        if (initPos == null) {
            System.out.println("InitPos ");
            pos.print();
            initPos = posPaperP.get();
            initPosM = pos.get();
        }

        if (isOnly) {
            graphics.beginDraw();
            graphics.clear(0, 0);
        }

        PVector paperCameraPos = new PVector();

        PVector virtualPos = userPos.get();
        if (isAnaglyph) {
            virtualPos.add(isLeft ? -halfEyeDist : halfEyeDist, 0, 0);
        }
        virtualPos.mult(-scale);
        // userPos * scale - posPaper - initPos 
        virtualPos.add(posPaperP);
        virtualPos.sub(initPos);

        // virtualPos.z = -virtualPos.z;
        
        // Get the current paperSheet position
        PMatrix3D rotationPaper = pos.get();
        rotationPaper.invert();
        rotationPaper.m03 = 0;
        rotationPaper.m13 = 0;
        rotationPaper.m23 = 0;   // inverse of the Transformation (without position)

        rotationPaper.mult(virtualPos, paperCameraPos);

        // http://www.gamedev.net/topic/597564-view-and-projection-matrices-for-vr-window-using-head-tracking/

//        graphics.camera(tmp2.x, tmp2.y, tmp2.z,
//                tmp2.x, tmp2.y, 0,
//                0, 1, 0);
//
//        float nearFactor = nearPlane / tmp2.z;
//
//        float left = nearFactor * (-scale * size.x / 2f - tmp2.x);
//        float right = nearFactor * (scale * size.x / 2f - tmp2.x);
//        float top = nearFactor * (scale * size.y / 2f - tmp2.y);
//        float bottom = nearFactor * (-scale * size.y / 2f - tmp2.y);
        
        graphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
                paperCameraPos.x, paperCameraPos.y, 0,
                0, 1, 0);

        float nearFactor = nearPlane / paperCameraPos.z;

        float left = nearFactor * (-scale * size.x / 2f - paperCameraPos.x);
        float right = nearFactor * (scale * size.x / 2f - paperCameraPos.x);
        float top = nearFactor * (scale * size.y / 2f - paperCameraPos.y);
        float bottom = nearFactor * (-scale * size.y / 2f - paperCameraPos.y);

        graphics.frustum(left, right, bottom, top, nearPlane, farPlane);

        return graphics;
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

        GLGraphicsOffScreen projGraphics = projector.getGraphics();
        PMatrix3D projMat = projector.projectionInit.get();
        PMatrix3D modvw = projGraphics.modelview.get();

        double[] mouseDist = projector.proj.undistort(mouseX, mouseY);
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

    public boolean setAutoUpdatePos(Camera camera, MarkerBoard board) {
        pos3D = camera.getPosPointer(board);
        return pos3D != null;
    }

//    public void setManualUpdatePos() {
//        pos3D = new float[16];
//    }
    public boolean isDrawing() {
        return isDrawing;
    }

    public void setDrawing(boolean isDrawing) {
        this.isDrawing = isDrawing;
    }

    public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
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

    public PMatrix3D getPos() {
        return pos;
    }

    public float getScale() {
        return this.scale;
    }

    // Available only if pos3D is being updated elsewhere...
    public void updatePos() {




        if (pos == null) {
            pos = new PMatrix3D(pos3D[0], pos3D[1], pos3D[2], pos3D[3],
                    pos3D[4], pos3D[5], pos3D[6], pos3D[7],
                    pos3D[8], pos3D[9], pos3D[10], pos3D[11],
                    0, 0, 0, 1);
        } else {

            if (filters != null) {
                try {
                    pos.m00 = (float) filters[0].filter(pos3D[0]);
                    pos.m01 = (float) filters[1].filter(pos3D[1]);
                    pos.m02 = (float) filters[2].filter(pos3D[2]);
                    pos.m03 = (float) filters[3].filter(pos3D[3]);
                    pos.m10 = (float) filters[4].filter(pos3D[4]);
                    pos.m11 = (float) filters[5].filter(pos3D[5]);
                    pos.m12 = (float) filters[6].filter(pos3D[6]);
                    pos.m13 = (float) filters[7].filter(pos3D[7]);
                    pos.m20 = (float) filters[8].filter(pos3D[8]);
                    pos.m12 = (float) filters[9].filter(pos3D[9]);
                    pos.m22 = (float) filters[10].filter(pos3D[10]);
                    pos.m23 = (float) filters[11].filter(pos3D[11]);
                } catch (Exception e) {
                }
            } else {
                pos.set(pos3D[0], pos3D[1], pos3D[2], pos3D[3],
                        pos3D[4], pos3D[5], pos3D[6], pos3D[7],
                        pos3D[8], pos3D[9], pos3D[10], pos3D[11],
                        0, 0, 0, 1);

            }
        }


//        pos = new PMatrix3D(pos3D[0], pos3D[1], pos3D[2], pos3D[3],
//                pos3D[4], pos3D[5], pos3D[6], pos3D[7],
//                pos3D[8], pos3D[9], pos3D[10], pos3D[11],
//                0, 0, 0, 1);

        posPaper.x = pos3D[3];
        posPaper.y = pos3D[7];
        posPaper.z = pos3D[11];

        posPaperP.x = pos3D[3];
        posPaperP.y = pos3D[7];
        posPaperP.z = pos3D[11];
    }

    public void setPos(PMatrix3D position) {
        pos = position.get();
    }

    // Available only if pos3D is being updated elsewhere...
    public void updatePos(Camera camera, MarkerBoard board) {

        pos3D = camera.getPosPointer(board);

        pos = new PMatrix3D(pos3D[0], pos3D[1], pos3D[2], pos3D[3],
                pos3D[4], pos3D[5], pos3D[6], pos3D[7],
                pos3D[8], pos3D[9], pos3D[10], pos3D[11],
                0, 0, 0, 1);
//        pos.m00 = pos3D[0];
//        pos.m01 = pos3D[1];
//        pos.m02 = pos3D[2];
//        pos.m03 = pos3D[3];
//        pos.m10 = pos3D[4];
//        pos.m11 = pos3D[5];
//        pos.m12 = pos3D[6];
//        pos.m13 = pos3D[7];
//        pos.m20 = pos3D[8];
//        pos.m12 = pos3D[9];
//        pos.m22 = pos3D[10];
//        pos.m23 = pos3D[11];

        posPaper.x = pos3D[3];
        posPaper.y = pos3D[7];
        posPaper.z = pos3D[11];

        posPaperP.x = pos3D[3];
        posPaperP.y = pos3D[7];
        posPaperP.z = pos3D[11];
    }
//    public void setPos(float pos3D[]) {
//
//        // TODO: not optimal, need to check the pos3D creation / deletion
//        this.pos3D = pos3D;
//        pos.m00 = pos3D[0];
//        pos.m01 = pos3D[1];
//        pos.m02 = pos3D[2];
//        pos.m03 = pos3D[3];
//        pos.m10 = pos3D[4];
//        pos.m11 = pos3D[5];
//        pos.m12 = pos3D[6];
//        pos.m13 = pos3D[7];
//        pos.m20 = pos3D[8];
//        pos.m12 = pos3D[9];
//        pos.m22 = pos3D[10];
//        pos.m23 = pos3D[11];
//
//        posPaper.x = pos3D[3];
//        posPaper.y = pos3D[7];
//        posPaper.z = pos3D[11];
//
//        posPaperP.x = pos3D[3];
//        posPaperP.y = pos3D[7];
//        posPaperP.z = pos3D[11];
//
//    }

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
}
