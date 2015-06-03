import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
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

CameraOpenKinect cameraKinect;
KinectProcessing kinect;
KinectOpenCV kinectOpenCV;
PointCloudKinect pointCloud;

MarkerBoard markerBoard;


PlaneAndProjectionCalibration planeProjCalib;

int precision = 1;
ProjectorDisplay projector;
    
PVector[] screenPoints;
int nbPoints = 10;
int currentPoint = 0;

Papart papart;
PVector trackingSize = new PVector(297, 210);

void setup(){
    
    size(Kinect.WIDTH, Kinect.HEIGHT, OPENGL); 
    
    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;

    papart = new Papart(this);
    
    papart.initProjectorCamera();
    projector = papart.getProjectorDisplay();

    // no automatic drawing. 
    projector.manualMode();

    
    cameraKinect = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    cameraKinect.setParent(this);
    cameraKinect.setCalibration(Papart.kinectRGBCalib);
    cameraKinect.getDepthCamera().setCalibration(Papart.kinectIRCalib);

    cameraKinect.convertARParams(this, Papart.kinectRGBCalib, "Kinect.cal");
    cameraKinect.initMarkerDetection("Kinect.cal");
    cameraKinect.start();

    planeProjCalib = new PlaneAndProjectionCalibration();
    
    kinect = new KinectProcessing(this,
				  Papart.kinectIRCalib,
				  Papart.kinectRGBCalib,
				  kinectFormat);

    kinect.setStereoCalibration(Papart.kinectStereoCalib);
    
    kinectOpenCV = new KinectOpenCV(this,
				    Papart.kinectIRCalib,
				    Papart.kinectRGBCalib,
				    kinectFormat);
    
    kinectOpenCV.setStereoCalibration(Papart.kinectStereoCalib);

    
  pointCloud = new PointCloudKinect(this, precision);

  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);
  
  markerBoard = new MarkerBoard(sketchPath + "/data/big.cfg", trackingSize.x, trackingSize.y);
  cameraKinect.trackMarkerBoard(markerBoard);

  

}



Vec3D[] depthPoints;
IplImage kinectImg;
IplImage kinectImgDepth;
IplImage cameraImg;
PlaneCalibration planeCalibKinect, planeCalibCam;
PMatrix3D kinectPaperTransform;
PMatrix3D cameraPaperTransform;


void draw(){
    background(0);

    cameraKinect.grab();
    kinectImg = cameraKinect.getIplImage();
    kinectImgDepth = cameraKinect.getDepthCamera().getIplImage();

    
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    kinectOpenCV.update(kinectImgDepth, kinectImg);

    // markerBoard.updatePosition(camera, kinectOpenCV.getColouredDepthImage());
    markerBoard.updatePosition(cameraKinect,   kinectImg);

    kinectPaperTransform = markerBoard.getTransfoMat(cameraKinect);

     // Not so usefull... ? To try with different parameters. 
    // PMatrix3D kinectExtr = kinect.getStereoCalibration();
    // kinectExtr.invert();
    // kinectPaperTransform.preApply(kinectExtr);

    
    planeCalibKinect =  PlaneCalibration.CreatePlaneCalibrationFrom(kinectPaperTransform, trackingSize);
    
    planeCalibKinect.flipNormal();
    planeCalibKinect.moveAlongNormal(-15f);

    HomographyCalibration homographyCalib = HomographyCalibration.CreateHomographyCalibrationFrom(kinectPaperTransform, trackingSize);
    
    // kinectPaperTransform.scale(1f / trackingSize.x,
    // 			       1f / trackingSize.y, 1);
    // homographyCalibration.setMatrix(new PMatrix3D());
    
    planeProjCalib.setPlane(planeCalibKinect);
    planeProjCalib.setHomography(homographyCalib);

    kinect.update(kinectImgDepth, kinectImg,  planeProjCalib, precision);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);
}




void draw3DPointCloud(){
}



boolean test = false;
void keyPressed() {

    if(key == 't')
	test = !test;

    if(key == 's'){
	save();
    }

}


void save(){

   planeProjCalib.saveTo(this, Papart.planeAndProjectionCalib);

   println("All is saved.");
}


