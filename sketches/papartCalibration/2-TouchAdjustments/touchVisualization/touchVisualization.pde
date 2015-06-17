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

import fr.inria.controlP5.*;
import fr.inria.controlP5.events.*;
import fr.inria.controlP5.gui.controllers.*;
import fr.inria.controlP5.gui.group.*;

import peasy.*;

ControlP5 cp5;
PeasyCam cam;

CameraOpenKinect camera;
KinectProcessing kinect;
PointCloudKinect pointCloud;


HomographyCreator homographyCreator;
HomographyCalibration homographyCalibration;
PlaneCalibration planeCalibration;
PlaneAndProjectionCalibration planeProjCalibration;
    
int precision = 2;



void setup(){
    
    size(800, 600, OPENGL);
    
    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;
    
    camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    camera.setParent(this);
    camera.setCalibration(Papart.kinectRGBCalib);
    camera.getDepthCamera().setDepthFormat(depthFormat);
    camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
    camera.start();

    try{
	planeProjCalibration = new  PlaneAndProjectionCalibration();
	planeProjCalibration.loadFrom(this, Papart.planeAndProjectionCalib);
	planeCalibration = planeProjCalibration.getPlaneCalibration();
    }catch(NullPointerException e){
	die("Impossible to load the plane calibration...");
    }

    kinect = new KinectProcessing(this,camera);

    //  pointCloud = new PointCloudKinect(this, precision);
    pointCloud = new PointCloudKinect(this);



  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(100);
  cam.setMaximumDistance(5000);
  cam.setActive(true);

  touchDetection = new TouchDetectionSimple2D(Kinect.SIZE);
  touchDetection3D = new TouchDetectionSimple3D(Kinect.SIZE);

  touchCalibration = new PlanarTouchCalibration();
  touchCalibration.loadFrom(this, Papart.touchCalib);
  touchDetection.setCalibration(touchCalibration);
  
  initGui();
  
}


// Inteface values
float maxDistance, minHeight;
float planeHeight;
int searchDepth, recursion, minCompoSize, forgetTime;
float trackingMaxDistance;

TouchDetectionSimple2D touchDetection;
TouchDetectionSimple3D touchDetection3D;
PlanarTouchCalibration touchCalibration;


Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
PImage goodDepthImg;  
ArrayList<TouchPoint> globalTouchList = new ArrayList<TouchPoint>();


void draw(){
    background(0);

    try{
    camera.grab();
    }catch(Exception e){
	println("Could not grab frame..." +e );
    }
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthCamera().getIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	println("null images..");
	return;
    }

    //    kinect.updateMT(kinectImgDepth, planeProjCalibration, precision, precision);
    //    kinect.updateTest(kinectImgDepth, kinectImg, planeProjCalibration, precision);


    touchCalibration.setMaximumDistance(maxDistance);
    touchCalibration.setMinimumHeight(minHeight);

    touchCalibration.setMinimumComponentSize((int)minCompoSize);
    touchCalibration.setMaximumRecursion((int) recursion);
    touchCalibration.setSearchDepth((int) searchDepth);

    touchCalibration.setTrackingForgetTime((int)forgetTime);
    touchCalibration.setTrackingMaxDistance(trackingMaxDistance);
    
    touchCalibration.setPrecision(precision);
    planeCalibration.setHeight(planeHeight);
    

    kinect.update(kinectImgDepth, kinectImg, planeProjCalibration, precision);
    //    kinect.update(kinectImgDepth, kinectImg, planeCalibration, precision);
    draw3DPointCloud();
    
    cam.beginHUD();
    text("'m' to stop the camera", 10,  30);
    cp5.draw();
    cam.endHUD(); // always!
}




boolean isMouseControl = true;
boolean draw3D = true;

void keyPressed() {

    if(key =='m'){
	isMouseControl = !isMouseControl;
	cam.setMouseControlled(isMouseControl);
    }

    
    if(key == 't'){
	draw3D = !draw3D;
    }

    if(key == 's')
	save();

    if(key == 'S')
	save3D();

}

void save3D(){
    println("TouchCalibration3D saved.");    
    touchCalibration.saveTo(this, Papart.touchCalib3D);
}

void save(){
    planeProjCalibration.saveTo(this, Papart.planeAndProjectionCalib);
    // println("PlaneProj saved.");

    println("TouchCalibration2D saved.");    
    touchCalibration.saveTo(this, Papart.touchCalib);
}
