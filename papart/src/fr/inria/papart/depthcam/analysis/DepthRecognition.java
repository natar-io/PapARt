/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.analysis;


/**
 * Handle the use of the depthAnalysis, and extend it by composition.
 * @author Jeremy Laviole
 */
public abstract class DepthRecognition {

    protected final DepthAnalysisImpl depthAnalysis;
//    protected ProjectedDepthData depthData;

    public DepthRecognition(DepthAnalysisImpl depthAnalysis) {
        this.depthAnalysis = depthAnalysis;
    }
    
}
