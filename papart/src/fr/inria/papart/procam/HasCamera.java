/*
 *
 * Copyright Inria, Bordeaux University.
 * Author : Jeremy Laviole. 
 *
 * No licence yet.
 */
package fr.inria.papart.procam;

import fr.inria.papart.procam.camera.Camera;
/**
 *
 * @author jiii
 */
public interface HasCamera {
    
    public boolean hasCamera();
    public Camera getCamera();
    
}
