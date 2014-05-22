/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.papart.scanner;

import fr.inria.papart.procam.Camera;
import fr.inria.papart.procam.Projector;

/**
 *
 * @author jiii
 */
public class GrayCodeScanner3D extends Scanner3D{
    
        // Time-related variables
    int startTime = 7000;
    int displayTime = 1500;
    int captureTime = 700;
    int delay = 200;
    int nextCapture = captureTime + startTime;

    int nbCaptured = 0;
    
    public GrayCodeScanner3D(Camera camera, Projector projector){
        super(camera, projector);
    }
    
}
