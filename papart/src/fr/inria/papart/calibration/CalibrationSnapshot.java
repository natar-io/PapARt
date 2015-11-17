/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.calibration;

import processing.core.PMatrix3D;

/**
 *
 * @author Jérémy Laviole - jeremy.laviole@inria.fr
 */
public class CalibrationSnapshot {

    PMatrix3D cameraPaper = null;
    PMatrix3D projectorPaper = null;
    PMatrix3D kinectPaper = null;

    public CalibrationSnapshot(PMatrix3D cameraPaperCalibration,
            PMatrix3D projectorPaperCalibration,
            PMatrix3D kinectPaperCalibration) {
        cameraPaper = cameraPaperCalibration.get();
        projectorPaper = projectorPaperCalibration.get();
        if (kinectPaperCalibration != null) {
            kinectPaper = kinectPaperCalibration.get();
        }
    }

}
