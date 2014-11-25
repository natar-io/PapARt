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

PeasyCam cam;

CameraOpenKinect camera;
KinectProcessing kinect;
PointCloudKinect pointCloud;


HomographyCreator homographyCreator;
HomographyCalibration homographyCalibration;
PlaneCalibration planeCalibration;
PlaneAndProjectionCalibration planeProjCalibration;

int precision = 2;


int framePosX = 0;
int framePosY = 200;

void setup(){
    
    size(Kinect.WIDTH, Kinect.HEIGHT, OPENGL);
    
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
    
  pointCloud = new PointCloudKinect(this, precision);

  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);

  touchDetection = new TouchDetectionSimple2D(Kinect.SIZE);
  touchDetection3D = new TouchDetectionSimple3D(Kinect.SIZE);
}

TouchDetectionSimple2D touchDetection;
TouchDetectionSimple3D touchDetection3D;
Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
PImage goodDepthImg;  

boolean draw3D = true;

void draw(){
    background(0);

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    //    kinect.updateMT(kinectImgDepth, planeProjCalibration, precision, precision);
    //    kinect.updateTest(kinectImgDepth, kinectImg, planeProjCalibration, precision);
    kinect.update(kinectImgDepth, kinectImg, planeProjCalibration, precision);


    if(draw3D){
    	draw3DPointCloud();
    } else {
	draw2DPoints();
    }
}

ArrayList<TouchPoint> globalTouchList = new ArrayList<TouchPoint>();

void draw3DPointCloud(){
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);

    lights();
    stroke(200);
    fill(200);

    DepthData depthData = kinect.getDepthData();

    // ArrayList<TouchPoint> touchs = touchDetection.findRichMultiTouch(depthData, precision);
    ArrayList<TouchPoint> touchs = touchDetection.compute(depthData, precision);

    TouchPointTracker.trackPoints(globalTouchList, touchs, millis(), KinectTouchInput.trackNearDist);

    colorMode(HSB, 20, 100, 100);
    for(TouchPoint touchPoint : globalTouchList){

	Vec3D position = touchPoint.getPositionKinect();
	pushMatrix();
	translate(position.x, position.y, -position.z);

	fill(touchPoint.getID() % 20, 100, 100);	
	sphere(3);
	popMatrix();
    }
    
}

void draw2DPoints(){
    cam.beginHUD();
    
    DepthData depthData = kinect.getDepthData();

    drawProjectedPoints(depthData);
    drawTouchPoints(depthData);
 
    cam.endHUD();
}

void drawProjectedPoints(DepthData depthData){
    Vec3D[] projPoints = depthData.projectedPoints;
    boolean[] mask2D = depthData.validPointsMask;
    boolean[] mask3D = depthData.validPointsMask3D;
    
    colorMode(RGB, 255);
    
    for(int i = 0; i < Kinect.SIZE; i++){
	Vec3D p = projPoints[i];
	if(p == null)
	    continue;
	int green = mask2D[i] ? 100 : 0;
	int blue = mask3D[i] ? 100 : 0;
	stroke(0, green, blue);
	point(p.x * Kinect.WIDTH, p.y * Kinect.HEIGHT);
    }
}

void drawTouchPoints(DepthData depthData){

    ArrayList<TouchPoint> touchs = touchDetection.compute(depthData, precision);
    TouchPointTracker.trackPoints(globalTouchList, touchs, millis(), KinectTouchInput.trackNearDist);

    colorMode(HSB, 20, 100, 100);
    for(TouchPoint touchPoint : globalTouchList){
	PVector position = touchPoint.getPosition();
	fill(touchPoint.getID() % 20, 100, 100);
	ellipse(position.x * Kinect.WIDTH, position.y  * Kinect.HEIGHT, 5, 5);
    }

    ArrayList<TouchPoint> touchs3D = touchDetection3D.compute(depthData, precision);
    colorMode(RGB, 255);
    fill(180, 200, 20);
    stroke(180, 200, 20);
    strokeWeight(precision);
    for(TouchPoint touchPoint : touchs3D){
	PVector position = touchPoint.getPosition();
	ellipse(position.x * Kinect.WIDTH, position.y  * Kinect.HEIGHT, 4, 4);
    }

}


void keyPressed() {

    // if(key == 'u'){
    // 	planeProjCalibration.moveAlongNormal(1f);
    // }
    // if(key == 'd'){
    // 	planeProjCalibration.moveAlongNormal(-1f);
    // }

    if(key == 't'){
	draw3D = !draw3D;
    }

}

void save(){
    planeProjCalibration.saveTo(this, Papart.planeAndProjectionCalib);
    println("PlaneProj saved.");
}
