import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.calibration.*;

import toxi.geom.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.*;
import java.nio.IntBuffer;


import peasy.*;

PeasyCam cam;

CameraOpenKinect camera;
KinectProcessing kinect;
PointCloudKinect pointCloud;

PlaneCalibration planeCalibration;
PlaneCreator planeCreator;

int precision = 1;
float normalMovementIncrement = 1f;
float defaultNormalMovementIncrement = -10f;


void setup(){
    
    size(640, 480, OPENGL);
    

    camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    camera.setParent(this);
    camera.setCalibration(Papart.kinectRGBCalib);
    camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
    camera.start();

    reset();


    kinect = new KinectProcessing(this, camera);

    kinect.setStereoCalibration(Papart.kinectStereoCalib);

  pointCloud = new PointCloudKinect(this, precision);

  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);

  println("Press r to reset.");
}

Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
  
void draw(){
    background(0);

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthCamera().getIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    if(planeCalibration.isValid()){
	draw3DPointCloud();
    } else {
	draw2DSelection();
    }
    
}


void draw3DPointCloud(){

    kinect.update(kinectImgDepth, kinectImg, planeCalibration, precision);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);
}

void draw2DSelection(){
    cam.beginHUD();
    kinect.update(kinectImgDepth, kinectImg);
    depthPoints = kinect.getDepthPoints();
    PImage goodDepthImg = kinect.getColouredDepthImage();
    image(goodDepthImg, 0, 0);
    cam.endHUD();
}

float defaultHeight = 15f;

void reset(){
    planeCalibration = new PlaneCalibration();
    planeCreator = new PlaneCreator();
    planeCreator.setHeight(defaultHeight);
}



void keyPressed() {

    if(key == 'r'){
	reset();
    }

    if(key == 'd'){
	planeCalibration.moveAlongNormal(normalMovementIncrement);
    }
    if(key == 'u'){
	planeCalibration.moveAlongNormal(-normalMovementIncrement);
    }

    if(key == 'h'){
	planeCalibration.setHeight(planeCalibration.getHeight() + 5);
    }
    if(key == 'H'){
	planeCalibration.setHeight(planeCalibration.getHeight() - 5);
    }

    
    if(key == 's'){
	save();
    }

}


void save(){
    planeCalibration.saveTo(this, Papart.planeCalib);
    println("Plane saved");
}


void mousePressed(){

    if(!planeCalibration.isValid()){
	checkPoint();
    }

}


void checkPoint(){

   int offset = (int) mouseY * camera.width() +(int) mouseX;

    if(depthPoints[offset] != null){
	Vec3D depth = depthPoints[offset];
	planeCreator.addPoint(depth);
	println("Point added");
	
	if(planeCreator.isComputed()){
	    planeCalibration = planeCreator.getPlaneCalibration();

	    planeCalibration.
		moveAlongNormal(defaultNormalMovementIncrement);
	    save();
	    println("You can now adjust the height of the plane with u and d keys.");
	    println("Press s to save the plane.");
	}
    }

}


