/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.depthcam.analysis;

/**
 *
 * @author Jeremy Laviole
 */
public interface DepthComputation {

    public float findDepth(int offset, Object buffer);
}
