/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.analysis;

import fr.inria.papart.calibration.files.PlaneAndProjectionCalibration;
import fr.inria.papart.depthcam.devices.ProjectedDepthData;

/**
 *
 * @author Jeremy Laviole
 */
public abstract class DepthRecognition {

    protected final DepthAnalysisImpl depthAnalysis;
    protected final ProjectedDepthData depthData;

    public DepthRecognition(DepthAnalysisImpl depthAnalysis) {
        this.depthAnalysis = depthAnalysis;
        this.depthData = depthAnalysis.depthData;
    }
    
}
