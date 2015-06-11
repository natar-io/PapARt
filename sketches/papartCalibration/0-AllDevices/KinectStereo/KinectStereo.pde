import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.depthcam.*;

import fr.inria.papart.calibration.HomographyCalibration;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.freenect;
import toxi.geom.*;
import peasy.*;

import fr.inria.controlP5.*;
import fr.inria.controlP5.events.*;
import fr.inria.controlP5.gui.controllers.*;
import fr.inria.controlP5.gui.group.*;



ControlP5 cp5;
PeasyCam cam;

PointCloudKinect pointCloud;

CameraOpenKinect camera;
KinectProcessing kinect;
PMatrix3D stereoCalib;

int depthFormat = freenect.FREENECT_DEPTH_MM;
int kinectFormat = Kinect.KINECT_MM;

int skip = 2;

float translation = 15;

void setup(){

  size(800, 600, OPENGL); 

  
  camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
  camera.setParent(this);
  camera.setCalibration(Papart.kinectRGBCalib);
  camera.getDepthCamera().setDepthFormat(depthFormat);
  camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
  camera.start();

  kinect = new KinectProcessing(this, camera);

  try{
      stereoCalib = HomographyCalibration.getMatFrom(this, Papart.kinectStereoCalib);
      translation = stereoCalib.m03;
  } catch(Exception e){
      println("File invalid or not found, load default values. " + e);
      stereoCalib = new PMatrix3D(1, 0, 0, 15,
				  0, 1, 0, 0,
				  0, 0, 1, 0,
				  0, 0, 0, 1);
  }


  cp5 = new ControlP5(this);
  cp5.addSlider("translation")
      .setPosition(30, 50)
      .setValue(stereoCalib.m03)
      .setRange(-15, 30)
      .setSize(200, 12);

  // Manual draw. 
  cp5.setAutoDraw(false);

  textFont(createFont("",15));

  pointCloud = new PointCloudKinect(this, skip);


  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);
}




void draw(){
    background(100);

    // retreive the camera image.
    camera.grab();

    IplImage colourImg = camera.getIplImage();
    IplImage depthImg = camera.getDepthCamera().getIplImage();

    stereoCalib.m03 = translation;
    kinect.setStereoCalibration(stereoCalib);
    kinect.update(depthImg, colourImg, skip);

    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);

    cam.beginHUD();
    text("'m' to stop the camera, 's' to save the calibration.", 10, 30);
    cp5.draw();
    cam.endHUD(); // always!

}

boolean isMouseControl = true;

void keyPressed(){
    
    if(key =='m'){
	isMouseControl = !isMouseControl;
	cam.setMouseControlled(isMouseControl);
    }

    if(key == 's'){
	HomographyCalibration.saveMatTo(this, stereoCalib, Papart.kinectStereoCalib);
	println("Default stereo calibration saved.");
    }
}

void close(){
    try{
	camera.close();
    }catch(Exception e){
    }
}
