/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremylaviole
 */

class ARTThread extends Thread{

    private ARTagDetector art;
    private MarkerBoard[] sheets = null;
    private boolean undistort; 
    private boolean compute;
    
    public boolean stop;
    
    public ARTThread(ARTagDetector art, MarkerBoard[] sheets){
        this(art, sheets, true);
    }
    
    public ARTThread(ARTagDetector art, MarkerBoard[] sheets, boolean undistort){
        this.undistort = undistort;
	this.art = art;
	this.sheets = sheets;
	stop = false;
    }
    
    public void run(){
	while(!stop){
            art.grab(undistort);
            if(compute)
                compute();
	}
    }

    public void compute(){
//	float[][] allPos = art.findMultiMarkers(undistort, false);
        for(MarkerBoard sheet : sheets){
           art.findMarkers(sheet);
        }
    }

    public boolean isCompute() {
        return compute;
    }

    public void setCompute(boolean compute) {
        this.compute = compute;
    }

    public void stopThread(){
	stop = true;
    }
}

