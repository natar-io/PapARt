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
    private Screen[] screens = null;
    private boolean undistort; 
    
    public boolean stop;
    
    public ARTThread(ARTagDetector art, Screen[] screens){
        this(art, screens, true);
    }
    
    public ARTThread(ARTagDetector art, Screen[] screens, boolean undistort){
        this.undistort = undistort;
	this.art = art;
	this.screens = screens;
	stop = false;
    }
    
    public void run(){
	while(!stop){
	    compute();
	}
    }

    public void compute(){
	float[][] allPos = art.findMultiMarkers(undistort, false);
	
	// TODO: assert same size !!
	int k=0;
	for(Screen screen : screens){
            screen.setPos(allPos[k++]);
	}
    }

    public void stopThread(){
	stop = true;
    }
}

