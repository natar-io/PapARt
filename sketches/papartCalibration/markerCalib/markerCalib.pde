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
KinectOpenCV kinectOpenCV;
PointCloudKinect pointCloud;

MarkerBoard markerBoard;
PlaneCalibration planeCalibration;
HomographyCalibration homographyCalibration;

int precision = 1;

PVector[] screenPoints;
int nbPoints = 10;
int currentPoint = 0;

PVector paperSize = new PVector(297, 210);

void setup(){
    
    size(Kinect.WIDTH, Kinect.HEIGHT, OPENGL); 
    
    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;

    camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    camera.setParent(this);
    camera.setCalibration(Papart.kinectIRCalib);
    camera.setDepthFormat(depthFormat);
    Camera.convertARParams(this, Papart.kinectIRCalib, "Kinect.cal");
    camera.initMarkerDetection("Kinect.cal");
    camera.start();

    planeCalibration = new PlaneCalibration();
    reset();

    kinect = new KinectProcessing(this,
				  Papart.kinectIRCalib,
				  Papart.kinectRGBCalib,
				  kinectFormat);
    
    kinectOpenCV = new KinectOpenCV(this,
				    Papart.kinectIRCalib,
				    Papart.kinectRGBCalib,
				    kinectFormat);
    
  pointCloud = new PointCloudKinect(this, precision);

  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);

  markerBoard = new MarkerBoard(sketchPath + "/data/A3-small1.cfg", paperSize.x, paperSize.y);
  camera.trackMarkerBoard(markerBoard);


  println("Press R to reset.");
}



Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
  
void draw(){
    background(0);

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    kinectOpenCV.update(kinectImgDepth, kinectImg);

    markerBoard.updatePosition(camera, kinectOpenCV.getColouredDepthImage());
    PMatrix3D mat = markerBoard.getTransfoMat(camera);


    // planeCalibration = Utils.CreatePlaneCalibrationFrom(mat, paperSize);
    // homographyCalibration = Utils.CreateHomographyCalibrationFrom(mat, paperSize);

    //    planeCalibration = Utils.CreatePlaneCalibrationFrom(mat, paperSize);
    homographyCalibration.setMatrix(mat);


    if(homographyCalibration.isValid()){
    	draw3DPointCloud();
    } 
    // else {
    // 	draw2DSelection();
    // }
}


void draw3DPointCloud(){
    kinect.update(kinectImgDepth, kinectImg, homographyCalibration, precision);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);
}

void reset(){
    println("Reset");
    homographyCalibration = new HomographyCalibration();
    planeCalibration = new PlaneCalibration();
    currentPoint = 0;
}



void keyPressed() {

    if(key == 'r'){
	reset();
    }

    if(key == 's'){
	save();
    }

}


void save(){
    homographyCalibration.saveTo(this, Papart.homographyCalib);
    planeCalibration.saveTo(this, Papart.homographyCalib);
    println("All is saved.");
}


