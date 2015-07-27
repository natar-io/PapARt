/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
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
