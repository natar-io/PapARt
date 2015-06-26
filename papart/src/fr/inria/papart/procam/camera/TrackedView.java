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
package fr.inria.papart.procam.camera;

import fr.inria.papart.procam.MarkerBoard;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.Utils;
import fr.inria.papart.procam.camera.Camera;
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

    private PImage extractedImage = null;
    private IplImage extractedIplImage = null;

    // private data
    private PVector[] corner3DPos;
    private PVector[] screenPixelCoordinates;
    private PVector[] imagePixelCoordinates;

    // external information
    private MarkerBoard board = MarkerBoard.INVALID_MARKERBOARD;
    private PaperScreen paperScreen = null; // todo : invalid one...
    
    private boolean useBoardLocation = false;
    private boolean usePaperLocation = false;
    
    
    private PVector bottomLeftCorner = new PVector(0, 0), captureSizeMM = new PVector(100, 100);
    private int imageWidthPx = 128, imageHeightPx = 128;

    // temporary variables
    private CvMat homography;
    private IplImage mainImage;
    private Camera camera;

    // Public constructor for capturing the whole markerboard 
    public TrackedView(MarkerBoard board) {
        this.board = board;
        this.useBoardLocation = true;
//        this.setImageHeightPx((int) board.getHeight());
//        this.setImageWidthPx((int) board.getWidth());
        this.setCaptureSizeMM(new PVector(board.getWidth(), board.getHeight()));
    }
    
    public TrackedView(PaperScreen paperScreen) {
        this.paperScreen = paperScreen;
        this.usePaperLocation = true;
//        this.setImageHeightPx((int) board.getHeight());
//        this.setImageWidthPx((int) board.getWidth());
        this.setCaptureSizeMM(paperScreen.getDrawingSize());
    }
    
    

    public void init() {
        extractedImage = new PImage(imageWidthPx, imageHeightPx, PApplet.RGB);
        allocateMemory();
        initiateImageCoordinates();
    }

    private void allocateMemory() {
        screenPixelCoordinates = new PVector[4];
        corner3DPos = new PVector[4];
        imagePixelCoordinates = new PVector[4];
        for (int i = 0; i < 4; i++) {
            corner3DPos[i] = new PVector();
        }
    }

    private void initiateImageCoordinates() {
        imagePixelCoordinates[0] = new PVector(0, imageHeightPx);
        imagePixelCoordinates[1] = new PVector(imageWidthPx, imageHeightPx);
        imagePixelCoordinates[2] = new PVector(imageWidthPx, 0);
        imagePixelCoordinates[3] = new PVector(0, 0);
    }


    public PImage getViewOf(Camera camera) {
        if(extractedImage == null){
            System.err.println("You should init the TrackedView before getting the view.");
            return null;
        }
        if(camera.getIplImage() == null)
            return null;

        this.mainImage = camera.getIplImage();
        this.camera = camera;
        
        prepareHomography();
        Utils.remapImage(homography, camera.getIplImage(), extractedIplImage, extractedImage);
        return extractedImage;
    }
    
    public IplImage getIplViewOf(Camera camera) {
        if(camera.getIplImage() == null)
            return null;

        this.mainImage = camera.getIplImage();
        this.camera = camera;
        prepareHomography();
        Utils.remapImageIpl(homography, camera.getIplImage(), extractedIplImage);
        return extractedIplImage;
    }
    

    private void prepareHomography() {
        checkMemory();
        computeCorners();
        homography = Utils.createHomography(screenPixelCoordinates, imagePixelCoordinates);
    }


    private void checkMemory() {
        if (extractedIplImage == null) {
            extractedIplImage = Utils.createImageFrom(extractedImage);
        }
    }

    private void computeCorners() {

        PMatrix3D pos = null;
        
        if(usePaperLocation){
            pos = paperScreen.getLocation();
        }
        
        if(useBoardLocation){
            pos = board.getTransfoMat(camera).get();
        }
        
        if(pos == null){
            throw new RuntimeException("ERROR in TrackedView, report this.");
        }
        
        PMatrix3D tmp = new PMatrix3D();

        tmp.apply(pos);

        // bottom left
        tmp.translate(bottomLeftCorner.x, bottomLeftCorner.y);
        corner3DPos[0].x = tmp.m03;
        corner3DPos[0].y = tmp.m13;
        corner3DPos[0].z = tmp.m23;

        // bottom right
        tmp.translate(captureSizeMM.x, 0);
        corner3DPos[1].x = tmp.m03;
        corner3DPos[1].y = tmp.m13;
        corner3DPos[1].z = tmp.m23;

        // top right
        tmp.translate(0, captureSizeMM.y, 0);
        corner3DPos[2].x = tmp.m03;
        corner3DPos[2].y = tmp.m13;
        corner3DPos[2].z = tmp.m23;

        // top left
        tmp.translate(-captureSizeMM.x, 0, 0);
        corner3DPos[3].x = tmp.m03;
        corner3DPos[3].y = tmp.m13;
        corner3DPos[3].z = tmp.m23;

        for (int i = 0; i < 4; i++) {
            screenPixelCoordinates[i] = camera.pdp.worldToPixel(corner3DPos[i], true);
        }
    }

    public MarkerBoard getBoard() {
        return this.board;
    }

    public PVector getBottomLeftCorner() {
        return bottomLeftCorner;
    }

    public TrackedView setBottomLeftCorner(PVector bottomLeftCorner) {
        this.bottomLeftCorner = bottomLeftCorner;
        return this;
    }

    public PVector getCaptureSizeMM() {
        return captureSizeMM;
    }

    public TrackedView setCaptureSizeMM(PVector captureSizeMM) {
        this.captureSizeMM = captureSizeMM;
        return this;
    }

    public int getImageWidthPx() {
        return imageWidthPx;
    }

    public TrackedView setImageWidthPx(int imageWidthPx) {
        this.imageWidthPx = imageWidthPx;
        return this;
    }

    public int getImageHeightPx() {
        return imageHeightPx;
    }

    public TrackedView setImageHeightPx(int imageHeightPx) {
        this.imageHeightPx = imageHeightPx;
        return this;
    }

//    @Override
//    public void prepareToDisplayOn(PGraphicsOpenGL display) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void addDisplay(PGraphicsOpenGL display) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public PImage getDisplayedOn(PGraphicsOpenGL display) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
}
