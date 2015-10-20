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


HomographyCreator homographyCreator;
HomographyCalibration homographyCalibration;
PlaneCalibration planeCalibration;
PlaneAndProjectionCalibration planeProjCalibration;

int precision = 1;

PVector[] screenPoints;
int nbPoints;
int currentPoint = 0;

int scale = 1;

void setup(){
    
    size(Kinect.WIDTH * scale , Kinect.HEIGHT * scale, OPENGL); 
    
    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;

    camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    camera.setParent(this);
    camera.setCalibration(Papart.kinectRGBCalib);
    camera.getDepthCamera().setDepthFormat(depthFormat);
    camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
    camera.start();

    try{
	planeCalibration = new PlaneCalibration();
	planeCalibration.loadFrom(this, Papart.planeCalib);
    }catch(NullPointerException e){
	die("Impossible to load the plane calibration...");
    }

    init2DDestinationPoints();
    reset();

    kinect = new KinectProcessing(this, camera);
    kinect.setStereoCalibration(Papart.kinectStereoCalib);

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
  
void draw(){
    background(0);

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthCamera().getIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    if(planeProjCalibration.isValid()){
	draw3DPointCloud();
    } else {
	draw2DSelection();
    }
    
}


void draw3DPointCloud(){

    kinect.update(kinectImgDepth, kinectImg, planeProjCalibration, precision);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);
}

void draw2DSelection(){
    cam.beginHUD();
    //    kinect.update(kinectImgDepth, kinectImg, planeCalibration, precision);
    kinect.update(kinectImgDepth, kinectImg, precision);
    //    kinect.update(kinectImgDepth, kinectImg);
    depthPoints = kinect.getDepthPoints();
    PImage goodDepthImg = kinect.getColouredDepthImage();
    image(goodDepthImg, 0, 0, width, height);
    cam.endHUD();
}

void reset(){
    println("Reset");
    planeProjCalibration = new  PlaneAndProjectionCalibration();
    homographyCreator = new HomographyCreator(3, 2, nbPoints);
    homographyCalibration = new HomographyCalibration();
    currentPoint = 0;
}



float normalMovementIncrement = 1f;

void keyPressed() {

    if(key == 'r'){
	reset();
    }

    if(key == 's'){
	save();
    }

    if(key == 'd'){
	planeCalibration.moveAlongNormal(normalMovementIncrement);
    }
    if(key == 'u'){
	planeCalibration.moveAlongNormal(-normalMovementIncrement);
    }

}


void save(){

    homographyCalibration = homographyCreator.getHomography();
    homographyCalibration.saveTo(this, Papart.homographyCalib);
    println("Homography saved.");


    planeProjCalibration.setPlane(planeCalibration);
    planeProjCalibration.setHomography(homographyCalibration);
    planeProjCalibration.saveTo(this, Papart.planeAndProjectionCalib);

    println("PlaneProj saved.");
}


void mousePressed(){

    if(homographyCalibration.isValid()){
	return;
    }


    int offset = (int) (mouseY / scale)  * camera.width() +(int) (mouseX /scale);

    if(depthPoints[offset] != null){
	Vec3D depth = depthPoints[offset];
	PVector depthPVector = new PVector(depth.x, depth.y, depth.z);

	homographyCreator.addPoint(depthPVector, screenPoints[currentPoint]);
	currentPoint++;
	println("Point " + currentPoint  + "  added. Out of " + nbPoints + ".");
	
	if(homographyCreator.isComputed()){
	    save();
	    println("Press t to switch between plan height. (No effect on saving)");
	}

    }
}
