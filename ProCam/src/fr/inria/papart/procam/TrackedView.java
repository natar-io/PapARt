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

    public TrackedView(MarkerBoard board, int outWidth, int outHeight) {
        this.board = board;
        img = new PImage(outWidth, outHeight, PApplet.RGB);
        cornerPos = new PVector[4];
        screenP = new PVector[4];
        outScreenP = new PVector[4];

        for (int i = 0; i < 4; i++) {
            cornerPos[i] = new PVector();
        }

        outScreenP[0] = new PVector(0, 0);
        outScreenP[1] = new PVector(outWidth, 0);
        outScreenP[2] = new PVector(outWidth, outHeight);
        outScreenP[3] = new PVector(0, outHeight);

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
            screenP[i] = cam.getCamViewPoint(cornerPos[i]);
        }
    }

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
