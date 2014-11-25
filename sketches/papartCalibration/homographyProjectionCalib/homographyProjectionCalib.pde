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


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

PeasyCam cam;

CameraOpenKinect camera;
KinectProcessing kinect;
PointCloudKinect pointCloud;


HomographyCreator homographyCreator;
HomographyCalibration homographyCalibration;
PlaneCalibration planeCalibration;
PlaneAndProjectionCalibration planeProjCalibration;

int precision = 1;
int ellipseSize = 30;


PVector[] screenPoints;
int nbPoints;
int currentPoint = 0;

float defaultNormalMovementIncrement = 20f;

int frameSizeX = 1280;
int frameSizeY = 800;
int framePosX = 0;
int framePosY = 200;


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
	planeCalibration = new PlaneCalibration();
	planeCalibration.loadFrom(this, Papart.planeCalib);
	planeCalibration.moveAlongNormal(defaultNormalMovementIncrement);
    }catch(NullPointerException e){
	die("Impossible to load the plane calibration...");
    }

    init2DDestinationPoints();
    reset();

    kinect = new KinectProcessing(this,
				  Papart.kinectIRCalib,
				  Papart.kinectRGBCalib,
				  kinectFormat);
    
  pointCloud = new PointCloudKinect(this, precision);

  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);

  println("Press R to reset.");
}


void init2DDestinationPoints(){
    float step = 0.5f;
    nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
    println("NbPoints " + nbPoints);
    screenPoints = new PVector[nbPoints];
    int k = 0;
    for (float i = 0; i <= 1.0; i += step) {
	for (float j = 0; j <= 1.0; j += step, k++) {
	    screenPoints[k] = new PVector(i, j);
	}
    }
    currentPoint = 0;
}

Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
PImage goodDepthImg;  

void draw(){
    background(0);

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    if(isProjectPoint){
	kinect.update(kinectImgDepth, kinectImg, planeCalibration, precision);
	depthPoints = kinect.getDepthPoints();
	goodDepthImg = kinect.getColouredDepthImage();
	drawCurrentPoint();
	checkProjection();
    }

    if(isSelectPoint){
    	drawSelectPoint();
    }
    
    if(planeProjCalibration.isValid()){
    	draw3DPointCloud();
    }

}




void drawCurrentPoint(){
    cam.beginHUD();
    fill(255);
    noStroke();
    ellipse(screenPoints[currentPoint].x * frameSizeX, 
	    screenPoints[currentPoint].y * frameSizeY,
	    ellipseSize,
	    ellipseSize);
    cam.endHUD();
}

void draw3DPointCloud(){
    kinect.update(kinectImgDepth, kinectImg, planeProjCalibration, precision);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);
}

void drawSelectPoint(){
    cam.beginHUD();
    image(goodDepthImg, 0, 0, Kinect.WIDTH, Kinect.HEIGHT);
    cam.endHUD();
}

void reset(){
    println("Reset");

    planeProjCalibration = new  PlaneAndProjectionCalibration();
    homographyCreator = new HomographyCreator(3, 2, nbPoints);
    homographyCalibration = new HomographyCalibration();
    currentPoint = 0;
}



void keyPressed() {

    // Placed here, bug if it is placed in setup().
    if(key == ' '){
	frame.setLocation(framePosX, framePosY);
    }

    if(key == 'r'){
	reset();
    }

    if(key == 'p'){
	if(planeProjCalibration.isValid()){
	    reset();
	}else {
	    reProject();
	}
    }

    if(key == 's'){
	save();
    }
    if(key == 'u'){
	planeCalibration.moveAlongNormal(1f);
    }
    if(key == 'd'){
	planeCalibration.moveAlongNormal(-1f);
    }

	//     planeCalibration.moveAlongNormal(-defaultNormalMovementIncrement);


    if(key == 't'){
	// if(moved)
	//     planeCalibration.moveAlongNormal(-defaultNormalMovementIncrement);
	// else 
	//     planeCalibration.moveAlongNormal(defaultNormalMovementIncrement);

	// moved = !moved;
    }

}


void save(){

    homographyCalibration = homographyCreator.getHomography();
    homographyCalibration.saveTo(this, Papart.homographyCalib);
    println("Homography saved.");


    //    planeCalibration.moveAlongNormal(-defaultNormalMovementIncrement);

    planeProjCalibration.setPlane(planeCalibration);
    planeProjCalibration.setHomography(homographyCalibration);
    planeProjCalibration.saveTo(this, Papart.planeAndProjectionCalib);

    //    planeCalibration.moveAlongNormal(defaultNormalMovementIncrement);

    println("PlaneProj saved.");
}



void mousePressed(){
    if(homographyCalibration.isValid()){
	return;
    }

    if(isSelectPoint){
	pointSelected();
    }
}




void pointSelected(){
   
    if(mouseX > Kinect.WIDTH || mouseY > Kinect.HEIGHT)
	return;

   int offset = (int) mouseY * camera.width() +(int) mouseX;
   if(depthPoints[offset] != null){

       addPoint(offset);
       currentPoint++;
       setProjection();
       
       if(homographyCreator.isComputed()){
	    validateHomography();
       }

    }
}


void addPoint(int offset){
    Vec3D depth = depthPoints[offset];
    PVector depthPVector = new PVector(depth.x, depth.y, depth.z);
    homographyCreator.addPoint(depthPVector, screenPoints[currentPoint]);
    println("Point " + currentPoint  + "  added. Out of " + nbPoints + ".");
}

void validateHomography(){
    setEnded();
 
    planeCalibration.moveAlongNormal(-defaultNormalMovementIncrement);

   save();
    println("Press t to switch between plan height. (No effect on saving)");
}
