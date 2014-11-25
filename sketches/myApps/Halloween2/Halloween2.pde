import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.depthcam.calibration.*;

import toxi.geom.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.*;
import java.nio.IntBuffer;

import peasy.*;
import java.util.Iterator;
import java.util.ArrayList;


// Undecorated frame 
public void init() {
    frame.removeNotify(); 
    frame.setUndecorated(true); 
    frame.addNotify(); 
    super.init();
}

int framePosX = 0;
int framePosY = 120;
int frameSizeX = 1920;
int frameSizeY = 1080;

int precision = 4;

PFont font;
PGraphicsOpenGL bloodGraphics;

PlaneAndProjectionCalibration planeProjCalibration;
CameraOpenKinect camera;
KinectProcessing kinect;

TouchDetectionSimple3D touchDetection3D;

void setup(){
    size(frameSizeX, frameSizeY, OPENGL);

    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;

    camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    camera.setParent(this);
    camera.setCalibration(Papart.kinectRGBCalib);
    camera.setDepthFormat(depthFormat);
    camera.start();

   try{
	planeProjCalibration = new  PlaneAndProjectionCalibration();
	planeProjCalibration.loadFrom(this, Papart.planeAndProjectionCalib);
    }catch(NullPointerException e){
	die("Impossible to load the plane calibration...");
    }


   kinect = new KinectProcessing(this,
				 Papart.kinectIRCalib,
				 Papart.kinectRGBCalib,
				 kinectFormat);
   
   touchDetection3D = new TouchDetectionSimple3D(Kinect.SIZE);

   //     initBlood();

    background(0);
    font = loadFont("WCRhesusBBta-48.vlw"); //load the font stored in the data file

}



IplImage kinectImg;
IplImage kinectImgDepth;

void draw(){

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    kinect.updateMT(kinectImgDepth, planeProjCalibration, precision, precision);
    // kinect.updateTest(kinectImgDepth, kinectImg, planeProjCalibration, precision);
    // kinect.update(kinectImgDepth, kinectImg, planeProjCalibration, precision);

    drawBlood();
    //image(bloodGraphics, 0, 0);
}


boolean perCentChance(float value){
    return random(1) <  (value / 100f);
}

void keyPressed() {

    // Placed here, bug if it is placed in setup().
    if(key == ' '){
	frame.setLocation(framePosX, framePosY);

	// bloodGraphics.beginDraw();
	// bloodGraphics.background(0);
	// bloodGraphics.endDraw();
    }
}



