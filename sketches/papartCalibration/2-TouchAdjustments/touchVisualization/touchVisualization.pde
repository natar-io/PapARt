import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
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

CameraOpenKinect cameraKinect;
KinectProcessing kinect;
PointCloudKinect pointCloud;

HomographyCreator homographyCreator;
HomographyCalibration homographyCalibration;
PlaneCalibration planeCalibration;
PlaneAndProjectionCalibration planeProjCalibration;
    
int precision = 3;

void setup(){
    
    size(800, 600, OPENGL);

    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;

    Papart papart = new Papart(this);
    
    //      papart.initKinectCamera(1);
     // ARDisplay kinectDisplay = papart.getARDisplay();
     // kinectDisplay.manualMode();

     // cameraKinect = (CameraOpenKinect) papart.getCameraTracking();
     
    cameraKinect = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    cameraKinect.setParent(this);
    cameraKinect.setCalibration(Papart.kinectRGBCalib);
    cameraKinect.getDepthCamera().setCalibration(Papart.kinectIRCalib);
    cameraKinect.start();

    
    try{
	planeProjCalibration = new  PlaneAndProjectionCalibration();
	planeProjCalibration.loadFrom(this, Papart.planeAndProjectionCalib);
	planeCalibration = planeProjCalibration.getPlaneCalibration();
    }catch(NullPointerException e){
	die("Impossible to load the plane calibration...");
    }

    kinect = new KinectProcessing(this, cameraKinect);

     kinect.setStereoCalibration(Papart.kinectStereoCalib);
    
    pointCloud = new PointCloudKinect(this, 1);


  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(100);
  cam.setMaximumDistance(5000);
  cam.setActive(true);

  touchDetection = new TouchDetectionSimple2D(Kinect.SIZE);

  touchCalibration = new PlanarTouchCalibration();
  touchCalibration.loadFrom(this, Papart.touchCalib);
  touchDetection.setCalibration(touchCalibration);

  initGui();

  frameRate(200);

}


// Inteface values
float maxDistance, minHeight;
float planeHeight;
int searchDepth, recursion, minCompoSize, forgetTime;
float trackingMaxDistance;

TouchDetectionSimple2D touchDetection;
PlanarTouchCalibration touchCalibration;


Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
ArrayList<TouchPoint> globalTouchList = new ArrayList<TouchPoint>();


void draw(){
    background(0);
    //    println("Framerate " + frameRate);
    
    try{
    	cameraKinect.grab();
    }catch(Exception e){
    	println("Could not grab frame..." +e );
    	return;
    }
    kinectImg = cameraKinect.getIplImage();
    kinectImgDepth = cameraKinect.getDepthCamera().getIplImage();
    if(kinectImg == null || kinectImgDepth == null){
    	println("null images..");
    	return;
    }

    touchCalibration.setMaximumDistance(maxDistance);
    touchCalibration.setMinimumHeight(minHeight);

    touchCalibration.setMinimumComponentSize((int)minCompoSize);
    touchCalibration.setMaximumRecursion((int) recursion);
    touchCalibration.setSearchDepth((int) searchDepth);

    touchCalibration.setTrackingForgetTime((int)forgetTime);
    touchCalibration.setTrackingMaxDistance(trackingMaxDistance);
    
    touchCalibration.setPrecision(precision);
    planeCalibration.setHeight(planeHeight);

    
    kinect.updateMT(kinectImgDepth, kinectImg,  planeProjCalibration,
		    precision);

     //    kinect.update(kinectImgDepth, kinectImg, planeCalibration, precision);
    draw3DPointCloud();
    



    cam.beginHUD();

    text("'m' to stop the camera", 10,  30);
    cp5.draw();
    cam.endHUD(); // always!
}


void draw3DPointCloud(){
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);

    lights();
    stroke(200);
    fill(200);

    KinectDepthData depthData = kinect.getDepthData();
    ArrayList<TouchPoint> touchs = touchDetection.compute(depthData);
    TouchPointTracker.trackPoints(globalTouchList, touchs, millis());

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


boolean isMouseControl = true;

void keyPressed() {

    if(key =='m'){
	isMouseControl = !isMouseControl;
	cam.setMouseControlled(isMouseControl);
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
