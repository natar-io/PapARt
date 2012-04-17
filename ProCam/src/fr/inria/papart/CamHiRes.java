/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.papart;

/**
 *
 * @author jeremylaviole
 */

import com.googlecode.javacv.CameraDevice;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.processing.ARTagDetector;

import com.googlecode.javacv.processing.Utils;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PMatrix3D;
import processing.core.PVector;


public class CamHiRes{

    protected int vw, vh, imgW, imgH;

    public ARTagDetector art;
    protected CameraDevice cameraDevice;
				       
    protected PMatrix3D camIntrinsicsP3D;

    public PApplet parent;
    
    public float offscreenScale;
    PVector offscreenSize; 

    PaperSheet paperL, paperR;
    PaperSheet[] sheets; 

    public CamHiRes(PApplet parent, int camNo, 
		    int vw, int vh,
		    int imgW, int imgH,
		    PVector paperSize,
		    String calibrationYAML, String calibrationData,
                    String[] boards){


	//////// Augmented Reality initialization ///////////
	try {
	    com.googlecode.javacv.processing.Utils.
		convertARParam(parent, calibrationYAML, calibrationData, vw, vh);
	}catch (Exception e){
//	    println("Conversion error. " + e);
	}
	
	art = new ARTagDetector(camNo, vw, vh, 60, calibrationYAML,
				calibrationData,
				boards);
	// TODO: only 2 boards

	paperL = new PaperSheet(paperSize.x, paperSize.y, imgW, imgH, vw, vh); 
	sheets = new PaperSheet[1];
	sheets[0] = paperL;


	// Load the camera parameters. 
	try{
	    CameraDevice[] camDev = CameraDevice.read(calibrationYAML);
	    if (camDev.length > 0) 
		cameraDevice = camDev[0];

	    double[] camMat = cameraDevice.cameraMatrix.get();
	    camIntrinsicsP3D = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
					      (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
					      (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
					      0, 0, 0, 1);

	}  catch(Exception e){ 
//	    die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
	}
    

//	println("HiResCam init OK: " + camNo);
    }



    public CamHiRes(PApplet parent, String videoFile, 
		    int vw, int vh,
		    int imgW, int imgH,
		    PVector paperSize,
		    String calibrationYAML, String calibrationData,
                    String[] boards){


	//////// Augmented Reality initialization ///////////
	try {
	    com.googlecode.javacv.processing.Utils.
		convertARParam(parent, calibrationYAML, calibrationData, vw, vh);
	}catch (Exception e){
//	    println("Conversion error. " + e);
	}
	
	art = new ARTagDetector(videoFile, vw, vh, 60, calibrationYAML,
				calibrationData,
				boards);
	// TODO: only 2 boards

	paperL = new PaperSheet(paperSize.x, paperSize.y, imgW, imgH, vw, vh); 
	sheets = new PaperSheet[1];
	sheets[0] = paperL;


	// Load the camera parameters. 
	try{
	    CameraDevice[] camDev = CameraDevice.read(calibrationYAML);
	    if (camDev.length > 0) 
		cameraDevice = camDev[0];

	    double[] camMat = cameraDevice.cameraMatrix.get();
	    camIntrinsicsP3D = new PMatrix3D((float) camMat[0], (float) camMat[1], (float) camMat[2], 0,
					      (float) camMat[3], (float) camMat[4], (float) camMat[5], 0,
					      (float) camMat[6], (float) camMat[7], (float) camMat[8], 0,
					      0, 0, 0, 1);

	}  catch(Exception e){ 
//	    die("Error reading the calibration file : " + calibrationYAML + " \n" + e);
	}
    

//	println("HiResCam init OK: " + videoFile);
    }



    public PVector getCamViewPoint(PVector pt){
      PVector tmp = new PVector();
      camIntrinsicsP3D.mult(new PVector(pt.x, pt.y, pt.z), tmp);
      return new PVector(tmp.x / tmp.z, tmp.y / tmp.z);
    }

    public boolean isReady(){
	return true;
    }

    public void grab(){
	art.grab();
    }

 

    public PImage[] getPaperView(){
	float[][] allPos = art.findMultiMarkers(true, false);

	// TODO: optim here ?
	PImage[] ret = new PImage[2];

	int k=0;
	for(PaperSheet ps : sheets){
	    ps.setPos(allPos[k]);
	    ps.computeCorners(this);
	    ret[k] = ps.getImage(art.getImageIpl());
	    k++;
	}
	
	return ret;
    }


    public PImage getPaperView(int id){
	float[][] allPos = art.findMultiMarkers(true, false);
	int k=0;
	PaperSheet ps = sheets[id];
	ps.setPos(allPos[id]);
	ps.computeCorners(this);
	return ps.getImage(art.getImageIpl());
    }

    public PImage getLastPaperView(int id){
	return sheets[id].img;
    }

    public void close(){
      
    }

}

class PaperSheet{

    PImage img = null;
    IplImage tmpImg = null;
    PMatrix3D pos;
    PVector[] cornerPos;
    PVector size;
    PVector[] screenP = new PVector[4];
    PVector[] outScreenP = new PVector[4];

    public PaperSheet(float sheetW, float sheetH, int w, int h, int vw, int vh){
	size = new PVector(sheetW, sheetH);

	img = new PImage(w, h, PApplet.RGB);
	cornerPos = new PVector[4];
	screenP = new PVector[4];
	outScreenP = new PVector[4];

	for(int i =0; i < 4; i++)
	    cornerPos[i] = new PVector();

	outScreenP[0] = new PVector(0, vh); 
	outScreenP[1] = new PVector(vw, vh);
	outScreenP[2] = new PVector(vw, 0);
	outScreenP[3] = new PVector(0, 0);
    }

    public void setPos(float[] pos3D){
	pos = new PMatrix3D(pos3D[0], pos3D[1], pos3D[2], pos3D[3], 
			    pos3D[4], pos3D[5], pos3D[6], pos3D[7], 
			    pos3D[8], pos3D[9], pos3D[10],pos3D[11],
			    0, 0, 0, 1);

	//	println("Pos found ? : " );
	//	pos.print();
    }

    public void computeCorners(CamHiRes cam){
	PMatrix3D newPos = pos.get();

	cornerPos[0].x = newPos.m03;
	cornerPos[0].y = newPos.m13;
	cornerPos[0].z = newPos.m23;

	PMatrix3D tmp = new PMatrix3D();
	tmp.apply(pos);

	tmp.translate(size.x, 0, 0);
	cornerPos[1].x = tmp.m03;
	cornerPos[1].y = tmp.m13;
	cornerPos[1].z = tmp.m23;

	tmp.translate(0, size.y, 0);
	cornerPos[2].x = tmp.m03;
	cornerPos[2].y = tmp.m13;
	cornerPos[2].z = tmp.m23;

	tmp.translate(-size.x, 0, 0);
	cornerPos[3].x = tmp.m03;
	cornerPos[3].y = tmp.m13;
	cornerPos[3].z = tmp.m23;

        for(int i= 0; i < 4; i++)
	    screenP[i] = cam.getCamViewPoint(cornerPos[i]);
    }

    public PImage getImage(IplImage iplImg){
	if(tmpImg == null)
	    tmpImg = Utils.createImageFrom(iplImg, img);

	Utils.remapImage(screenP, outScreenP, iplImg, tmpImg, img);
	return img;
    }

}
