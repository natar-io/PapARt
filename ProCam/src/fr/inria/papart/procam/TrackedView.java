/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole
 */
public class TrackedView {

    private PImage img = null;
    private IplImage tmpImg = null;
    private PMatrix3D pos;
    private PVector[] cornerPos;
    private MarkerBoard board;
    private PVector[] screenP = new PVector[4];
    private PVector[] outScreenP = new PVector[4];
    private Camera camera;
    private PVector botLeft, sizeCapture;

    public TrackedView(MarkerBoard board, int outWidth, int outHeight) {
        this(board, null, outWidth, outHeight);
    }

    // Public constructor for capturing the whole markerboard 
    public TrackedView(MarkerBoard board, Camera cam, int outWidth, int outHeight) {
        this.board = board;
        this.camera = cam;

        // TODO: check if this img can be removed
        img = new PImage(outWidth, outHeight, PApplet.RGB);
        cornerPos = new PVector[4];
        screenP = new PVector[4];
        outScreenP = new PVector[4];

        for (int i = 0; i < 4; i++) {
            cornerPos[i] = new PVector();
        }

//        outScreenP[0] = new PVector(outWidth, outHeight);
//        outScreenP[1] = new PVector(0, outHeight);
//        outScreenP[2] = new PVector(0, 0);
//        outScreenP[3] = new PVector(outWidth, outHeight);
        outScreenP[0] = new PVector(0, 0);
        outScreenP[1] = new PVector(outWidth, 0);
        outScreenP[2] = new PVector(outWidth, outHeight);
        outScreenP[3] = new PVector(0, outHeight);

        this.botLeft = new PVector(0, 0);
        this.sizeCapture = new PVector(this.board.width, this.board.height);
    }

    // Public constructor for capturing part of a markerboard (or oustide it)
    public TrackedView(MarkerBoard board, Camera cam, PVector botLeft, PVector sizeCapture, int outWidth, int outHeight) {
        this.board = board;
        this.camera = cam;

        // TODO: check if this img can be removed
        img = new PImage(outWidth, outHeight, PApplet.RGB);

        screenP = new PVector[4];
        cornerPos = new PVector[4];
        outScreenP = new PVector[4];

        // 3D positions of the borders
        for (int i = 0; i < 4; i++) {
            cornerPos[i] = new PVector();
        }

        // Borders to remap to the final image 
//        outScreenP[0] = new PVector(0, 0);
//        outScreenP[1] = new PVector(outWidth, 0);
//        outScreenP[2] = new PVector(outWidth, outHeight);
//        outScreenP[3] = new PVector(0, outHeight);

        outScreenP[0] = new PVector(0, outHeight);
        outScreenP[1] = new PVector(outWidth, outHeight);
        outScreenP[2] = new PVector(outWidth, 0);
        outScreenP[3] = new PVector(0, 0);

        this.botLeft = botLeft.get();
        this.sizeCapture = sizeCapture.get();
    }
    
    public PVector getResolution(){
        return new PVector(img.width, img.height);
    }
    
    public PVector getPosition(){
        return botLeft.get();
    }

    public PVector getSize(){
        return sizeCapture.get();
    }

    public void setObservedLocation(PVector botLeft, PVector sizeCapture) {
        this.botLeft = botLeft.get();
        this.sizeCapture = sizeCapture.get();
    }

    protected void setPos(float[] pos3D) {
        pos = new PMatrix3D(pos3D[0], pos3D[1], pos3D[2], pos3D[3],
                pos3D[4], pos3D[5], pos3D[6], pos3D[7],
                pos3D[8], pos3D[9], pos3D[10], pos3D[11],
                0, 0, 0, 1);

        //	println("Pos found ? : " );
        //	pos.print();
    }

    protected void computeCorners(Camera cam) {
        Camera tmp = this.camera;
        this.camera = cam;
        computeCorners();
        this.camera = tmp;
    }

    protected void computeCorners() {

        if (camera == null) {
            System.err.println("TrackedView : Error, you must set a camera, or use computeCorners(ImageWithTags itw.");
            return;
        }
//                // Borders to remap to the final image 
//        outScreenP[0] = new PVector(0, 0);
//        outScreenP[1] = new PVector(outWidth, 0);
//        outScreenP[2] = new PVector(outWidth, outHeight);
//        outScreenP[3] = new PVector(0, outHeight);


        // TODO: test if .get() is necessary ? 
        pos = board.getTransfoMat(camera).get();

        PMatrix3D tmp = new PMatrix3D();

        tmp.apply(pos);

        // bottom left
        tmp.translate(botLeft.x, botLeft.y);
        cornerPos[0].x = tmp.m03;
        cornerPos[0].y = tmp.m13;
        cornerPos[0].z = tmp.m23;

        // bottom right
        tmp.translate(sizeCapture.x, 0);
        cornerPos[1].x = tmp.m03;
        cornerPos[1].y = tmp.m13;
        cornerPos[1].z = tmp.m23;

        // top right
        tmp.translate(0, sizeCapture.y, 0);
        cornerPos[2].x = tmp.m03;
        cornerPos[2].y = tmp.m13;
        cornerPos[2].z = tmp.m23;

        // top left
        tmp.translate(-sizeCapture.x, 0, 0);
        cornerPos[3].x = tmp.m03;
        cornerPos[3].y = tmp.m13;
        cornerPos[3].z = tmp.m23;

        for (int i = 0; i < 4; i++) {
            screenP[i] = camera.getCamViewPoint(cornerPos[i]);
        }
    }
//    protected void computeCorners() {
//
//        if (camera == null) {
//            System.err.println("TrackedView : Error, you must set a camera, or use computeCorners(ImageWithTags itw.");
//            return;
//        }
//
//        // TODO: test if .get() is necessary ? 
//        pos = board.getTransfoMat(camera).get();
//
//        cornerPos[0].x = pos.m03;
//        cornerPos[0].y = pos.m13;
//        cornerPos[0].z = pos.m23;
//
//        PMatrix3D tmp = new PMatrix3D();
//        tmp.apply(pos);
//
//        tmp.translate(board.width, 0, 0);
//        cornerPos[1].x = tmp.m03;
//        cornerPos[1].y = tmp.m13;
//        cornerPos[1].z = tmp.m23;
//
//        tmp.translate(0, board.height, 0);
//        cornerPos[2].x = tmp.m03;
//        cornerPos[2].y = tmp.m13;
//        cornerPos[2].z = tmp.m23;
//
//        tmp.translate(-board.width, 0, 0);
//        cornerPos[3].x = tmp.m03;
//        cornerPos[3].y = tmp.m13;
//        cornerPos[3].z = tmp.m23;
//
//        for (int i = 0; i < 4; i++) {
//            screenP[i] = camera.getCamViewPoint(cornerPos[i]);
//        }
//    }

    protected IplImage getImageIpl(IplImage iplImg) {
        if (tmpImg == null) {
            tmpImg = Utils.createImageFrom(iplImg, img);
        }

        CvMat homography = Utils.createHomography(screenP, outScreenP);
        Utils.remapImageIpl(homography, iplImg, tmpImg);
        return tmpImg;
    }

    protected PImage getImage(IplImage iplImg) {
        if (tmpImg == null) {
            tmpImg = Utils.createImageFrom(iplImg, img);
        }

        CvMat homography = Utils.createHomography(screenP, outScreenP);
        Utils.remapImage(homography, iplImg, tmpImg, img);
        return img;
    }

    public MarkerBoard getBoard() {
        return this.board;
    }

    protected void computeCorners(ImageWithTags itw) {
        PMatrix3D newPos = pos.get();

        cornerPos[0].x = newPos.m03;
        cornerPos[0].y = newPos.m13;
        cornerPos[0].z = newPos.m23;

        PMatrix3D tmp = new PMatrix3D();
        tmp.apply(pos);

        tmp.translate(board.width, 0, 0);
        cornerPos[1].x = tmp.m03;
        cornerPos[1].y = tmp.m13;
        cornerPos[1].z = tmp.m23;

        tmp.translate(0, board.height, 0);
        cornerPos[2].x = tmp.m03;
        cornerPos[2].y = tmp.m13;
        cornerPos[2].z = tmp.m23;

        tmp.translate(-board.width, 0, 0);
        cornerPos[3].x = tmp.m03;
        cornerPos[3].y = tmp.m13;
        cornerPos[3].z = tmp.m23;

        for (int i = 0; i < 4; i++) {
            screenP[i] = itw.getCamViewPoint(cornerPos[i]);
        }
    }
}
