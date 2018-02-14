/*
 * Part of the PapARt project - https://project.inria.fr/papart/
 *
 * Copyright (C) 2017 RealityTech
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
package fr.inria.papart.procam.camera;

import fr.inria.papart.calibration.HomographyCreator;
import fr.inria.papart.calibration.files.HomographyCalibration;
import fr.inria.papart.utils.ImageUtils;
import fr.inria.papart.tracking.MarkerBoard;
import fr.inria.papart.tracking.MarkerBoardInvalid;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.utils.ARToolkitPlusUtils;
import fr.inria.papart.procam.camera.Camera;
import fr.inria.papart.utils.WithSize;
import java.util.ArrayList;
import org.bytedeco.javacpp.opencv_core.CvMat;
import org.bytedeco.javacpp.opencv_core.IplImage;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;

/**
 *
 * @author jeremylaviole IDEA: can it inherit camera ?
 */
public class TrackedView implements WithSize {

    private PImage extractedPImage = null;
    private IplImage extractedIplImage = null;

    // private data
    private final PVector[] corner3DPos = new PVector[4];
//    private final PVector[] screenPixelCoordinates = new PVector[4];
//    private final PVector[] imagePixelCoordinates = new PVector[4];
    private final ArrayList<PVector> screenPixelCoordinates = new ArrayList<>(4);
    private final ArrayList<PVector> imagePixelCoordinates = new ArrayList<>(4);

    // external information
    private MarkerBoard board = MarkerBoardInvalid.board;
    private PaperScreen paperScreen = null; // todo : invalid one...

    private boolean useBoardLocation = false;
    private boolean usePaperLocation = false;
    private boolean useManualConrers = false;
    private boolean useListofPairs = false;

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
//        this.setImageHeightPx((int) board.getHeight());
//        this.setImageWidthPx((int) board.getWidth());
        this.setCaptureSizeMM(new PVector(board.getWidth(), board.getHeight()));
    }

    /**
     * Create a TrackedView of the size of the PaperScreen. The default capture
     * size in millimeters is the size of the paperScreen). The default pixel
     * size is 1px / millimeter, so identical to the capture size. You can
     * change these values before calling the init() method.
     *
     * @param paperScreen
     */
    public TrackedView(PaperScreen paperScreen) {
        this.paperScreen = paperScreen;
        this.usePaperLocation = true;
        setTopLeftCorner(new PVector(0, 0));
        this.setImageHeightPx((int) paperScreen.getDrawingSize().x);
        this.setImageWidthPx((int) paperScreen.getDrawingSize().y);
        this.setCaptureSizeMM(paperScreen.getDrawingSize());
    }

    public TrackedView() {
        this.useManualConrers = true;
    }

    private boolean cornersSet = false;

    public void setCorners(PVector[] corners) {
        screenPixelCoordinates.clear();
        for (int i = 0; i < corners.length; i++) {
            screenPixelCoordinates.add(corners[i]);
        }
        cornersSet = true;
    }

    public void addObjectImagePair(PVector object, PVector image) {
        screenPixelCoordinates.add(object);
        imagePixelCoordinates.add(image);
    }

    public int getNbPairs() {
        return screenPixelCoordinates.size();
    }

    public void useListOfPairs(boolean use) {
        useListofPairs = use;
    }

    public void clearObjectImagePairs() {
        screenPixelCoordinates.clear();
        imagePixelCoordinates.clear();
    }

    public void init() {
        init(PApplet.RGB);
    }

    public void init(int frameType) {
        extractedPImage = new PImage(imageWidthPx, imageHeightPx, frameType);
        initiateImageCoordinates();
    }

    private void initiateImageCoordinates() {
        imagePixelCoordinates.clear();
        imagePixelCoordinates.add(new PVector(0, imageHeightPx));
        imagePixelCoordinates.add(new PVector(imageWidthPx, imageHeightPx));
        imagePixelCoordinates.add(new PVector(imageWidthPx, 0));
        imagePixelCoordinates.add(new PVector(0, 0));
    }

    public PVector pixelsToMM(PVector p) {
        return pixelsToMM(p.x, p.y);
    }

    public PVector pixelsToMM(float x, float y) {

        float outX = (x / imageWidthPx) * captureSizeMM.x + topLeftCorner.x;
        float outY = (y / imageHeightPx) * captureSizeMM.y + topLeftCorner.y;
        return new PVector(outX, outY);
    }

    public PImage getViewOf(Camera camera) {
        camera = Camera.checkActingCamera(camera);

        IplImage img = camera.getIplImage();
        if (!isExtractionReady(img)) {
            return null;
        }

        this.mainImage = img;
        this.camera = camera;

        CvMat homography = computeHomography();

        boolean useRGB = camera.getPixelFormat() == Camera.PixelFormat.RGB;
        // Convert to the good type... 
        ImageUtils.remapImage(homography, img, extractedIplImage, extractedPImage, useRGB);
        return extractedPImage;
    }

    public PImage getViewOf(Camera camera, IplImage fakeImage) {
        camera = Camera.checkActingCamera(camera);

        IplImage img = fakeImage;
        if (!isExtractionReady(img)) {
            return null;
        }

        this.mainImage = img;
        this.camera = camera;

        CvMat homography = computeHomography();

        boolean useRGB = camera.getPixelFormat() == Camera.PixelFormat.RGB;
        // Convert to the good type... 
        ImageUtils.remapImage(homography, img, extractedIplImage, extractedPImage, useRGB);
        return extractedPImage;
    }

    public HomographyCalibration getHomographyOf(Camera camera) {
        camera = Camera.checkActingCamera(camera);

        IplImage img = camera.getIplImage();
        if (!isExtractionReady(img)) {
            return null;
        }
        this.mainImage = img;
        this.camera = camera;

        CvMat homography = computeHomography();
        double[] homoMat = homography.get();
        HomographyCalibration homoCalib = new HomographyCalibration();
        homoCalib.setMatrix(new PMatrix3D(
                (float) homoMat[0], (float) homoMat[1], 0, (float) homoMat[2],
                (float) homoMat[3], (float) homoMat[4], 0, (float) homoMat[5],
                0, 0, 1, (float) homoMat[8],
                0, 0, 0, 1));
//        homoCalib.setMatrix(new PMatrix3D(
//                (float) homoMat[0], (float) homoMat[1], (float) homoMat[2], 0,
//                (float) homoMat[3], (float) homoMat[4], (float) homoMat[5], 0,
//                (float) homoMat[6], (float) homoMat[7], (float) homoMat[8], 0,
//                0, 0, 0, 1));
        return homoCalib;
    }

    public IplImage getIplViewOf(Camera camera) {
        IplImage img = camera.getIplImage();
        if (!isExtractionReady(img)) {
            return null;
        }

        this.mainImage = img;
        this.camera = camera;
        CvMat homography = computeHomography();
        ImageUtils.remapImageIpl(homography, camera.getIplImage(), extractedIplImage);
        return extractedIplImage;
    }

    public IplImage getIplViewOf(Camera camera, IplImage img) {
        if (!isExtractionReady(img)) {
            return null;
        }

        this.mainImage = img;
        this.camera = camera;
        CvMat homography = computeHomography();
        ImageUtils.remapImageIpl(homography, img, extractedIplImage);
        return extractedIplImage;
    }

    private boolean isExtractionReady(IplImage img) {
        if (extractedPImage == null) {
            System.err.println("You should init the TrackedView before getting the view.");
            return false;
        }

        if (img != null) {
            checkMemory(img);
        }
        return img != null && (useListofPairs && imagePixelCoordinates.size() >= 3
                || !useManualConrers
                || (useManualConrers && cornersSet));
    }

    private CvMat computeHomography() {
        if (!this.useListofPairs) {
            computeCorners();
        }

//        System.out.println("ComputeHomograpy with these points: ");
//        int k = 0;
//        for (PVector screen : screenPixelCoordinates) {
//            PVector img = imagePixelCoordinates.get(k);
////            System.out.println("id: " + k + " scr: " + screen + " img: " + img);
//            k++;
//        }
        CvMat homography = ImageUtils.createHomography(screenPixelCoordinates, imagePixelCoordinates);
        return homography;
    }

    private void checkMemory(IplImage memory) {
        if (extractedIplImage == null) {
            extractedIplImage = ImageUtils.createNewSizeImageFrom(memory, imageWidthPx, imageHeightPx);

            if (extractedIplImage == null) {
                System.err.println("Impossible to create a View! " + this + " " + extractedPImage);
            }
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

        for (int i = 0; i < 4; i++) {
            corner3DPos[i] = new PVector();
        }
        if (isYUp) {

            // bottom left
            tmp.translate(topLeftCorner.x, topLeftCorner.y);
            corner3DPos[0].x = tmp.m03;
            corner3DPos[0].y = tmp.m13;
            corner3DPos[0].z = tmp.m23;

            // bottom right
            tmp.translate(captureSizeMM.x, 0);
            corner3DPos[1].x = tmp.m03;
            corner3DPos[1].y = tmp.m13;
            corner3DPos[1].z = tmp.m23;

            // top right
            tmp.translate(0, -captureSizeMM.y, 0);
            corner3DPos[2].x = tmp.m03;
            corner3DPos[2].y = tmp.m13;
            corner3DPos[2].z = tmp.m23;

            // top left
            tmp.translate(-captureSizeMM.x, 0, 0);
            corner3DPos[3].x = tmp.m03;
            corner3DPos[3].y = tmp.m13;
            corner3DPos[3].z = tmp.m23;

        } else {

            // TODO: use BottowLeftCorner here ?!! 
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

        screenPixelCoordinates.clear();
        for (int i = 0; i < 4; i++) {
            screenPixelCoordinates.add(camera.pdp.worldToPixel(corner3DPos[i], true));
        }
        cornersSet = true;
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

    public void forceYOrientation(boolean up) {
        this.isYUp = up;
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

    public void setScale(float scale) {
        this.imageWidthPx = (int) (captureSizeMM.x * scale);
        this.imageHeightPx = (int) (captureSizeMM.y * scale);
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

    /**
     * Get Pixel width.
     *
     * @return
     */
    public int getWidth() {
        return imageWidthPx;
    }

    /**
     * Get pixel height.
     *
     * @return
     */
    public int getHeight() {
        return imageHeightPx;
    }

    /**
     * Get pixel size.
     *
     * @return
     */
    public int getSize() {
        return getWidth() * getHeight();
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
