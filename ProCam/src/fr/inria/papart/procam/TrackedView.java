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

import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.IplImage;
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
    private PVector botLeft, sizeCapture;


    // Public constructor for capturing the whole markerboard 
    public TrackedView(MarkerBoard board, int outWidth, int outHeight) {
        this.board = board;

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
    public TrackedView(MarkerBoard board, PVector botLeft, PVector sizeCapture, int outWidth, int outHeight) {
        this.board = board;

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


    protected void computeCorners(Camera camera) {

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
            screenP[i] = camera.pdp.worldToPixel(cornerPos[i], true);
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
