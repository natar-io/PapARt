/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart.procam;

import processing.core.PMatrix3D;

/**
 *
 * @author jiii
 */
public interface HasExtrinsics {
    
    public boolean hasExtrinsics();
    public PMatrix3D getExtrinsics();
    
}
