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
PlaneCalibration planeCalibration;
HomographyCalibration homographyCalibration;
PlaneAndProjectionCalibration planeProjCalib = new PlaneAndProjectionCalibration();

int precision = 2;
ProjectorDisplay projector;
    
PVector[] screenPoints;
int nbPoints = 10;
int currentPoint = 0;

Papart papart;
Camera cameraTracking;
PVector paperSize = new PVector(297, 210);

void settings(){
    size(Kinect.WIDTH, Kinect.HEIGHT, P3D); 
}

void setup(){
    
    int depthFormat = freenect.FREENECT_DEPTH_MM;
    int kinectFormat = Kinect.KINECT_MM;

    papart = new Papart(this);
    
    papart.initProjectorCamera();
    projector = papart.getProjectorDisplay();

    // no automatic drawing. 
    projector.manualMode();

    cameraTracking = papart.getCameraTracking();
    
    cameraKinect = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    cameraKinect.setParent(this);
    cameraKinect.setCalibration(Papart.kinectRGBCalib);
    cameraKinect.getDepthCamera().setCalibration(Papart.kinectIRCalib);

    cameraKinect.convertARParams(this, Papart.kinectRGBCalib, "Kinect.cal");
    cameraKinect.initMarkerDetection("Kinect.cal");
    cameraKinect.start();

    planeCalibration = new PlaneCalibration();

    kinect = new KinectProcessing(this, cameraKinect);
    kinect.setStereoCalibration(Papart.kinectStereoCalib);
    
    kinectOpenCV = new KinectOpenCV(this, cameraKinect);
    kinectOpenCV.setStereoCalibration(Papart.kinectStereoCalib);

    
  pointCloud = new PointCloudKinect(this, precision);

  // Set the virtual camera
  cam = new PeasyCam(this, 0, 0, -800, 800);
  cam.setMinimumDistance(0);
  cam.setMaximumDistance(1200);
  cam.setActive(true);
  
  markerBoard = new MarkerBoard(Papart.markerFolder + "big.cfg", paperSize.x, paperSize.y);
  cameraKinect.trackMarkerBoard(markerBoard);
  cameraTracking.trackMarkerBoard(markerBoard);
  

  reset();
  println("Press R to reset.");
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

    try {
	cameraKinect.grab();
    } catch(Exception e){
	println("Could not grab the image " + e);
    }
    
    kinectImg = cameraKinect.getIplImage();
    kinectImgDepth = cameraKinect.getDepthCamera().getIplImage();
    cameraImg = cameraTracking.getIplImage();
    
    if(kinectImg == null || kinectImgDepth == null || cameraImg == null){
	return;
    }

    kinectOpenCV.update(kinectImgDepth, kinectImg);

    // markerBoard.updatePosition(camera, kinectOpenCV.getColouredDepthImage());
    markerBoard.updatePosition(cameraKinect,   kinectImg);
    markerBoard.updatePosition(cameraTracking, cameraImg);


    kinectPaperTransform = markerBoard.getTransfoMat(cameraKinect);
    cameraPaperTransform = markerBoard.getTransfoMat(cameraTracking);

    println("Kinect " );
    kinectPaperTransform.print();
    println("Camera");
    cameraPaperTransform.print();

     // Not so usefull... ? To try with different parameters. 
    // PMatrix3D kinectExtr = kinect.getStereoCalibration();
    // kinectExtr.invert();
    // kinectPaperTransform.preApply(kinectExtr);

    
    planeCalibKinect =  PlaneCalibration.CreatePlaneCalibrationFrom(kinectPaperTransform, paperSize);
    planeCalibCam =  PlaneCalibration.CreatePlaneCalibrationFrom(cameraPaperTransform, paperSize);
    planeCalibCam.flipNormal();
    
    boolean hasIntersection = computeScreenPaperIntersection();

    if(!hasIntersection)
	return;
    
    planeCalibKinect.flipNormal();
    planeCalibKinect.moveAlongNormal(-15f);

    planeProjCalib.setPlane(planeCalibKinect);
    planeProjCalib.setHomography(homographyCalibration);

    
    kinect.update(kinectImgDepth, kinectImg,  planeProjCalib, precision);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);

}




void draw3DPointCloud(){
}

void reset(){
    println("Reset");
    homographyCalibration = new HomographyCalibration();
    planeCalibration = new PlaneCalibration();
    currentPoint = 0;
}


boolean test = false;
void keyPressed() {

    if(key == 't')
	test = !test;

    if(key == 'r'){
	reset();
    }

    if(key == 's'){
	save();
    }

}


void save(){


   planeProjCalib.saveTo(this, Papart.planeAndProjectionCalib);

   papart.saveCalibration(Papart.kinectTrackingCalib, cameraKinectTransform);
   
   papart.setTableLocation(cameraPaperTransform);
   
   // homographyCalibration.saveTo(this, Papart.homographyCalib);
   // planeCalibration.saveTo(this, Papart.homographyCalib);
   println("All is saved.");
}


