/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import tech.lity.rea.nectar.tracking.MarkerBoard;
import static tech.lity.rea.nectar.tracking.MarkerBoardInvalid.board;
import processing.core.PVector;
import tech.lity.rea.nectar.camera.Camera;
import tech.lity.rea.nectar.camera.TrackedView;

/**
 *
 * @author ditrop
 */
public class TrackedViewPaper extends TrackedView {

    private boolean useBoardLocation = false;
    private boolean usePaperLocation = false;
    private PaperScreen paperScreen;
    private MarkerBoard board;
    private Camera camera;

    // Public constructor for capturing the whole markerboard 
    public TrackedViewPaper(MarkerBoard board, Camera cam) {
        this.board = board;
        this.camera = cam;
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
    public TrackedViewPaper(PaperScreen paperScreen) {
        this.paperScreen = paperScreen;
        this.usePaperLocation = true;
        setTopLeftCorner(new PVector(0, 0), 0);
        this.setImageHeightPx((int) paperScreen.getDrawingSize().x);
        this.setImageWidthPx((int) paperScreen.getDrawingSize().y);
        this.setCaptureSizeMM(paperScreen.getDrawingSize());
    }

    public void updateMainPosition() {
        if (usePaperLocation) {
            updateMainPosition(paperScreen.getLocation());
        }

        if (useBoardLocation) {
            updateMainPosition(board.getPosition(camera).get());
        }
    }

}
