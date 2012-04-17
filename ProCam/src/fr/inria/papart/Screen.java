package fr.inria.papart;

import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Matrix4x4;
import toxi.geom.Plane;
import toxi.geom.Ray3D;
import toxi.geom.ReadonlyVec3D;
import toxi.geom.Vec3D;

/** 
 * This class implements a virtual screen.
 * The position of the screen has to be passed. It no longers handles a camera. 
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
    private Plane plane = new Plane();
    private static final int nbPaperPosRender = 4;
    
    private PVector[] paperPosScreen = new PVector[nbPaperPosRender];
    private PVector[] paperPosRender1 = new PVector[nbPaperPosRender];
    protected Homography homography;

    protected Matrix4x4 transformationProjPaper;
    
    private float halfEyeDist = 20; // 2cm

  
    /**
     * 
     * @param parent
     * @param projcam
     * @param size
     * @param scale 
     */
    public Screen(PApplet parent, 
            ProjCam projcam, PVector size, float scale, boolean useAA, int AAValue) {
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

    ////////////////// 3D SPACE TO PAPER HOMOGRAPHY ///////////////
    private void initHomography() {
        homography = new Homography(parent, 3, 3, 4);
        homography.setPoint(false, 0, new PVector(0, 0, 0));
        homography.setPoint(false, 1, new PVector(1, 0, 0));
        homography.setPoint(false, 2, new PVector(1, 1, 0));
        homography.setPoint(false, 3, new PVector(0, 1, 0));
    }

        
//    PVector[] screenP, outScreenP;
 
//    private void initImageGetter() {
//        screenP = new PVector[4];
//        outScreenP = new PVector[4];
//
//        // TODO:  Magic numbers !!!
//        outScreenP[0] = new PVector(0, 480);
//        outScreenP[1] = new PVector(640, 480);
//        outScreenP[2] = new PVector(640, 0);
//        outScreenP[3] = new PVector(0, 0);
//    }

    public GLTexture getTexture() {
        return thisGraphics.getTexture();
    }

    public void initTouch(Projector proj) {
        computePlane(proj);
        computeHomography(proj);
    }

    
    public GLGraphicsOffScreen getGraphics(){
        return thisGraphics;
    }
    
    public void initDraw(PVector userPos){
        initDraw(userPos, 40, 5000);
    }
    
    public void initDraw(PVector userPos, float nearPlane, float farPlane){
        initDraw(userPos, nearPlane, farPlane, false, false, true);
    }
    
    // TODO: optionnal args.
    public void initDraw(PVector userPos, float nearPlane, float farPlane, boolean isAnaglyph, boolean isLeft, boolean isOnly) {
        if (initPos == null) {
            initPos = posPaperP.get();
            initPosM = pos.get();
        }

        if(isOnly){
        thisGraphics.beginDraw();
        thisGraphics.clear(0);
        }
        
//	float nearPlane = 10;
//	float farPlane = 2000 * scale;
        PVector paperCameraPos = new PVector();

        // get the position at the start of the program.
        PVector tmp = initPos.get();
        tmp.sub(posPaperP); //  tmp =  currentPos - initPos   (Position)

        // Get the current paperSheet position
        PMatrix3D newPos = pos.get();

        newPos.invert();
        newPos.m03 = 0;
        newPos.m13 = 0;
        newPos.m23 = 0;   // inverse of the Transformation (without position)

        PVector tmp2 = userPos.get();

        if (isAnaglyph) {
            tmp2.add(isLeft ? -halfEyeDist : halfEyeDist, 0, 0);
        }
        tmp2.mult(-scale);
        tmp2.add(tmp);

        newPos.mult(tmp2, paperCameraPos);

        // http://www.gamedev.net/topic/597564-view-and-projection-matrices-for-vr-window-using-head-tracking/
        thisGraphics.camera(paperCameraPos.x, paperCameraPos.y, paperCameraPos.z,
                paperCameraPos.x, paperCameraPos.y, 0,
                0, 1, 0);

        float nearFactor = nearPlane / paperCameraPos.z;

        float left = nearFactor * (-scale * size.x / 2f - paperCameraPos.x);
        float right = nearFactor * (scale * size.x / 2f - paperCameraPos.x);
        float top = nearFactor * (scale * size.y / 2f - paperCameraPos.y);
        float bottom = nearFactor * (-scale * size.y / 2f - paperCameraPos.y);

        thisGraphics.frustum(left, right, bottom, top, nearPlane, farPlane);
    }

    protected void computeScreenPosition(Projector projector) {

        GLGraphicsOffScreen projGraphics = projector.getGraphics();
        projGraphics.pushMatrix();
        projGraphics.modelview.apply(projector.projExtrinsicsP3DInv); // camera view - instead of projector view
        projGraphics.pushMatrix();
        projGraphics.modelview.apply(pos);    // Go te the paper position

        paperPosRender1[0] = posPaperP.get();

        projGraphics.translate(size.x, 0, 0);
        paperPosRender1[1] = new PVector(projGraphics.modelview.m03,
                projGraphics.modelview.m13,
                -projGraphics.modelview.m23);

        projGraphics.translate(0, size.y, 0);
        paperPosRender1[2] = new PVector(projGraphics.modelview.m03,
                projGraphics.modelview.m13,
                -projGraphics.modelview.m23);

        projGraphics.translate(-size.x, 0, 0);
        paperPosRender1[3] = new PVector(projGraphics.modelview.m03,
                projGraphics.modelview.m13,
                -projGraphics.modelview.m23);
        projGraphics.popMatrix();

        // ScreenX from camera view
        for (int i = 0; i < nbPaperPosRender; i++) {
            paperPosScreen[i] =
                    new PVector(projGraphics.screenX(paperPosRender1[i].x, paperPosRender1[i].y, -paperPosRender1[i].z),
                    projGraphics.screenY(paperPosRender1[i].x, paperPosRender1[i].y, -paperPosRender1[i].z),
                    projGraphics.screenZ(paperPosRender1[i].x, paperPosRender1[i].y, -paperPosRender1[i].z));
        }
        projGraphics.popMatrix();
    }

    protected void computeHomography(Projector pc) {
        computeScreenPosition(pc);
        for (int i = 0; i < 4; i++) {
            homography.setPoint(true, i, paperPosRender1[i]);
        }
        homography.compute();
        transformationProjPaper = homography.getTransformation();
    }

    public Vec3D applyProjPaper(ReadonlyVec3D v) {
        return transformationProjPaper.applyTo(v);
    }

    ///////////////////// PLANE COMPUTATION  ////////////////
    private Plane computePlane(Projector projector) {
        GLGraphicsOffScreen projGraphics = projector.getGraphics();
        projGraphics.pushMatrix();
        projGraphics.modelview.apply(pos);    // Go te the paper position
        projGraphics.translate(0, 0, 10);

        // Do the TWO INVERT operations,  invert Z again and apply the inverse of the projExtrinsics
        PMatrix3D mv = projGraphics.modelview;
        PVector p1 = new PVector(mv.m03, mv.m13, -mv.m23);  // get the current Point
        PVector normale = new PVector();
        projector.projExtrinsicsP3DInv.mult(p1, normale);   // move the currentPoint 
        plane.set(posPaper);
        plane.normal.set(new Vec3D(normale.x, normale.y, normale.z));
        //    screenGFX.plane(plane, 100);
        projGraphics.popMatrix();

        return plane;
    }

    ///////////////////// POINTER PROJECTION  ////////////////
    // GluUnproject
    // TODO: not working ???
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

    // TODO: more doc...
    /**
     * Projects the position of a pointer in normalized screen space.
     * 
     * 
     * @param px  Normalized x position (0,1)
     * @param py  Normalized y position (0,1)
     * @param width   real screen width (resolution)
     * @param height  real screen height (resolution)
     * @return Position of the pointer.
     */
    public ReadonlyVec3D projectPointer(Projector projector, float px, float py, int width, int height) {
        
        PMatrix3D projMat = projector.getProjectionInit().get();
        PMatrix3D modvw = projector.getModelview1();
        //	PMatrix3D modvw = graphics.modelview.get();

        double[] pointerDist = projector.getProjectorDevice().undistort(px * width, py * height);
        float x = 2 * (float) pointerDist[0] / (float) width - 1;
        float y = 2 * (float) pointerDist[1] / (float) height - 1;

        PVector vect = new PVector(x, y, 0);
        PVector transformVect = new PVector();
        PVector transformVect2 = new PVector();
        projMat.apply(modvw);
        projMat.invert();
        projMat.mult(vect, transformVect);
        vect.z = (float) 0.85;
        projMat.mult(vect, transformVect2);

        Ray3D ray = new Ray3D(new Vec3D(transformVect.x, transformVect.y, transformVect.z),
                new Vec3D(transformVect2.x, transformVect2.y, transformVect2.z));

        ReadonlyVec3D res = plane.getIntersectionWithRay(ray);
        return res;
    }
    
    
      public float getHalfEyeDist() {
        return halfEyeDist;
    }

    public void setHalfEyeDist(float halfEyeDist) {
        this.halfEyeDist = halfEyeDist;
    }
    
    public PVector getSize(){
        return size;
    }
    
    public PMatrix3D getPos(){
        return pos;
    }

    public void setPos(float pos3D[]) {
        this.pos3D = pos3D;
        pos.m00 = pos3D[0];
        pos.m01 = pos3D[1];
        pos.m02 = pos3D[2];
        pos.m03 = pos3D[3];
        pos.m10 = pos3D[4];
        pos.m11 = pos3D[5];
        pos.m12 = pos3D[6];
        pos.m13 = pos3D[7];
        pos.m20 = pos3D[8];
        pos.m12 = pos3D[9];
        pos.m22 = pos3D[10];
        pos.m23 = pos3D[11];


        posPaper.x = pos3D[3];
        posPaper.y = pos3D[7];
        posPaper.z = pos3D[11];
        
        posPaperP.x = pos3D[3];
        posPaperP.y = pos3D[7];
        posPaperP.z = pos3D[11];
        
    }
    
}
