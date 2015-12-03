/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
import org.bytedeco.javacpp.opencv_core;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class MarkerBoardInvalid extends MarkerBoard {

    public static MarkerBoardInvalid board = new MarkerBoardInvalid();

    public MarkerBoardInvalid() {
        super();
    }

    @Override
    protected void addTrackerImpl(Camera camera) {
    }

    @Override
    protected void updatePositionImpl(int id, int currentTime, int endTime, int mode, Camera camera, opencv_core.IplImage img) {
    }
}
