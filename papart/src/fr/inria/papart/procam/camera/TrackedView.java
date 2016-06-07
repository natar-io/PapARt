/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam.camera;

import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.tracking.MarkerBoardInvalid;
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
    private final PVector[] corner3DPos = new PVector[4];
    private final PVector[] screenPixelCoordinates = new PVector[4];
    private final PVector[] imagePixelCoordinates = new PVector[4];

    // external information
    private MarkerBoard board = MarkerBoardInvalid.board;
    private PaperScreen paperScreen = null; // todo : invalid one...

    private boolean useBoardLocation = false;
    private boolean usePaperLocation = false;
    private boolean useManualConrers = false;

    private PVector bottomLeftCorner = new PVector(0, 0), captureSizeMM = new PVector(100, 100);
    private PVector topLeftCorner = new PVector(0, 0);
    private boolean isYUp = true;

    private int imageWidthPx = 128, imageHeightPx = 128;

    // temporary variables
    private Camera camera;
    private IplImage mainImage;

    // Public constructor for capturing the whole markerboard 
    public TrackedView(MarkerBoard board) {
        this.board = board;
        this.useBoardLocation = true;
        allocateMemory();
//        this.setImageHeightPx((int) board.getHeight());
//        this.setImageWidthPx((int) board.getWidth());
        this.setCaptureSizeMM(new PVector(board.getWidth(), board.getHeight()));
    }

    public TrackedView(PaperScreen paperScreen) {
        this.paperScreen = paperScreen;
        this.usePaperLocation = true;
        allocateMemory();
//        this.setImageHeightPx((int) board.getHeight());
//        this.setImageWidthPx((int) board.getWidth());
        this.setCaptureSizeMM(paperScreen.getDrawingSize());
    }

    public TrackedView() {
        this.useManualConrers = true;
        allocateMemory();
    }

    /**
     *
     * @param corners
     */
    public void setCorners(PVector[] corners) {
        if (corners.length == 4) {
            for (int i = 0; i < 4; i++) {
                screenPixelCoordinates[i] = corners[i];
            }
        }

    }

    public void init() {
        extractedImage = new PImage(imageWidthPx, imageHeightPx, PApplet.RGB);

        initiateImageCoordinates();
    }

    private void allocateMemory() {
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
        if (extractedImage == null) {
            System.err.println("You should init the TrackedView before getting the view.");
            return null;
        }
        if (camera.getIplImage() == null) {
            return null;
        }

        this.mainImage = camera.getIplImage();
        this.camera = camera;

        CvMat homography = computeHomography();
        Utils.remapImage(homography, camera.getIplImage(), extractedIplImage, extractedImage);
        return extractedImage;
    }

    public IplImage getIplViewOf(Camera camera) {
        if (camera.getIplImage() == null) {
            return null;
        }

        this.mainImage = camera.getIplImage();
        this.camera = camera;
        CvMat homography = computeHomography();
        Utils.remapImageIpl(homography, camera.getIplImage(), extractedIplImage);
        return extractedIplImage;
    }

    private CvMat computeHomography() {
        checkMemory();
        computeCorners();
        CvMat homography = Utils.createHomography(screenPixelCoordinates, imagePixelCoordinates);
        return homography;
    }

    private void checkMemory() {
        if (extractedIplImage == null) {
            extractedIplImage = Utils.createImageFrom(extractedImage);
        }
    }

    private void computeCorners() {

        PMatrix3D pos = null;

        if (useManualConrers) {
            return;
        }

        if (usePaperLocation) {
            pos = paperScreen.getLocation();
        }

        if (useBoardLocation) {
            pos = board.getTransfoMat(camera).get();
        }

        if (pos == null) {
            throw new RuntimeException("ERROR in TrackedView, report this.");
        }

        PMatrix3D tmp = new PMatrix3D();

        tmp.apply(pos);

        if (isYUp) {

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

        } else {

            // top left
            tmp.translate(topLeftCorner.x, paperScreen.getDrawingSize().y - topLeftCorner.y);
            corner3DPos[3].x = tmp.m03;
            corner3DPos[3].y = tmp.m13;
            corner3DPos[3].z = tmp.m23;

            // top right
            tmp.translate(captureSizeMM.x, 0);
            corner3DPos[2].x = tmp.m03;
            corner3DPos[2].y = tmp.m13;
            corner3DPos[2].z = tmp.m23;

            // bottom right
            tmp.translate(0, -captureSizeMM.y, 0);
            corner3DPos[1].x = tmp.m03;
            corner3DPos[1].y = tmp.m13;
            corner3DPos[1].z = tmp.m23;

            // bottom left
            tmp.translate(-captureSizeMM.x, 0, 0);
            corner3DPos[0].x = tmp.m03;
            corner3DPos[0].y = tmp.m13;
            corner3DPos[0].z = tmp.m23;
        }

        for (int i = 0; i < 4; i++) {
            screenPixelCoordinates[i] = camera.pdp.worldToPixel(corner3DPos[i], true);
        }
    }

    public MarkerBoard getBoard() {
        return this.board;
    }

    public PVector getBottomLeftCorner() {
        return bottomLeftCorner.get();
    }

    public PVector getTopLeftCorner() {
        return topLeftCorner.get();
    }

    /**
     * Use either TopLeftCorner OR BottomLeftCorner. Calling one will discard
     * the other.
     *
     * @param bottomLeftCorner
     */
    public void setBottomLeftCorner(PVector bottomLeftCorner) {
        this.bottomLeftCorner.set(bottomLeftCorner);
        this.isYUp = true;
    }

    /**
     * Use either TopLeftCorner OR BottomLeftCorner. Calling one will discard
     * the other.
     *
     * @param topLeftCorner
     */
    public void setTopLeftCorner(PVector topLeftCorner) {
        this.topLeftCorner.set(topLeftCorner);
        this.isYUp = false;
    }

    public PVector getCaptureSizeMM() {
        return captureSizeMM.copy();
    }

    public void setCaptureSizeMM(PVector captureSizeMM) {
        this.captureSizeMM.set(captureSizeMM);
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
