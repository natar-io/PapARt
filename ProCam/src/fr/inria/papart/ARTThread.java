/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

import com.googlecode.javacv.processing.ARTagDetector;
import processing.core.PMatrix3D;
import processing.core.PVector;
import toxi.geom.Vec3D;

/**
 *
 * @author jeremylaviole
 */

class ARTThread extends Thread{

    private ARTagDetector art;
    private ProjCam projCam;
    private boolean useGStreamer = false;
//    private GSCapture video;
    private Screen[] screens = null;

    public boolean stop;
    
    public ARTThread(ARTagDetector art, ProjCam projCam, Screen[] screens){
	this.art = art;
	this.projCam = projCam;
	this.screens = screens;
	stop = false;
    }

    public ARTThread(ARTagDetector art, ProjCam projCam){
	this.art = art;
	this.projCam = projCam;
	stop = false;
    }

    
    // TODO: support GSCapture ? 
//    public ARTThread(ARTagDetector art, ProjCam projCam, GSCapture video){
// 	this(art, projCam);
//	this.useGStreamer = true;
//	this.video = video;
//    }
    
    public void run(){
	while(!stop){
	    compute();
	    // if(useGStreamer){
	    // 	if (!video.available()) {
	    // 	    return;
	    // 	}
	    // 	video.read();
	    // 	computeGS();
	    // }else{
	    // 	compute();
	    // }
	}
    }

    // public void computeGS(){

    // 	projCam.pos3D = art.findMarkers(video);
    // 	projCam.pos = new PMatrix3D(projCam.pos3D[0], projCam.pos3D[1], projCam.pos3D[2], projCam.pos3D[3], 
    // 				    projCam.pos3D[4], projCam.pos3D[5], projCam.pos3D[6], projCam.pos3D[7], 
    // 				    projCam.pos3D[8], projCam.pos3D[9], projCam.pos3D[10],projCam.pos3D[11],
    // 				    0, 0, 0, 1);
    // 	projCam.posPaper = new Vec3D(projCam.pos3D[3], projCam.pos3D[7], projCam.pos3D[11]);
    // 	projCam.posPaperP = new PVector(projCam.pos3D[3], projCam.pos3D[7], projCam.pos3D[11]);
    // }

    
    public void compute(){
	float[][] allPos = art.findMultiMarkers(true, false);
	
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

