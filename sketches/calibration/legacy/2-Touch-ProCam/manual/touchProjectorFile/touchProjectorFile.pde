import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;

import fr.inria.papart.depthcam.devices.*;
import fr.inria.papart.calibration.*;

import toxi.geom.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.freenect;
import org.bytedeco.javacpp.opencv_core.*;
import java.nio.IntBuffer;

import peasy.*;

PeasyCam cam;

KinectDevice kinectDevice;
Camera kinectRGB, kinectDepth;
KinectProcessing kinect;
KinectOpenCV kinectOpenCV;
KinectPointCloud pointCloud;

PlaneCalibration planeCalibration;
HomographyCalibration homographyCalibration;
PlaneAndProjectionCalibration planeProjCalib = new PlaneAndProjectionCalibration();

int precision = 2;

PVector[] screenPoints;
int nbPoints = 10;
int currentPoint = 0;

Papart papart;
ProjectorDisplay projector;
Camera cameraTracking;
PVector paperSize = new PVector(297, 210);

boolean kinectOne = true;


void settings(){
    size(800, 600, P3D);
}


void setup(){

    papart = new Papart(this);
    projector = new ProjectorDisplay(this, Papart.projectorCalib);
    projector.init();
    projector.manualMode();

    cameraPaperTransform = papart.loadCalibration("cameraPaper.xml");

    if(kinectOne){
        kinectDevice = KinectDevice.createKinectOne(this);
        cameraTracking = kinectDevice.getCameraRGB();
        kinectPaperTransform =  papart.loadCalibration("cameraPaper.xml");
    } else {
        kinectPaperTransform =  papart.loadCalibration("kinectPaperForTouch.xml");
        kinectDevice = KinectDevice.createKinect360(this);
    }

    kinectRGB = kinectDevice.getCameraRGB();
    kinectDepth = kinectDevice.getCameraDepth();


    planeCalibration = new PlaneCalibration();

    kinect = new KinectProcessing(this, kinectDevice);
    kinectOpenCV = new KinectOpenCV(this, kinectDevice);

    pointCloud = new KinectPointCloud(this, kinectOpenCV, precision);

    // Set the virtual camera
    cam = new PeasyCam(this, 0, 0, -800, 800);
    cam.setMinimumDistance(0);
    cam.setMaximumDistance(1200);
    cam.setActive(true);

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
	kinectRGB.grab();
        kinectDepth.grab();
    } catch(Exception e){
	println("Could not grab the image " + e);
    }

    kinectImg = kinectRGB.getIplImage();
    kinectImgDepth = kinectDepth.getIplImage();
    cameraImg = cameraTracking.getIplImage();

    if(kinectImg == null || kinectImgDepth == null || cameraImg == null){
        println("No image !");
	return;
    }

     // Not so usefull... ? To try with different parameters.
    // PMatrix3D kinectExtr = kinect.getStereoCalibration();
    // kinectExtr.invert();
    // kinectPaperTransform.preApply(kinectExtr);

    if(kinectOne){

        planeCalibKinect =  PlaneCalibration.CreatePlaneCalibrationFrom(kinectPaperTransform, paperSize);
        planeCalibCam =  PlaneCalibration.CreatePlaneCalibrationFrom(cameraPaperTransform, paperSize);
        planeCalibCam.flipNormal();

        boolean hasIntersection = computeScreenPaperIntersection();

        if(!hasIntersection){
            println("No intersection..");
            return;
        }

        planeCalibKinect.flipNormal();
        planeCalibKinect.moveAlongNormal(-15f);

        planeProjCalib.setPlane(planeCalibKinect);
        planeProjCalib.setHomography(homographyCalibration);


    } else {

    planeCalibKinect =  PlaneCalibration.CreatePlaneCalibrationFrom(kinectPaperTransform, paperSize);
    planeCalibCam =  PlaneCalibration.CreatePlaneCalibrationFrom(cameraPaperTransform, paperSize);
    planeCalibCam.flipNormal();

    boolean hasIntersection = computeScreenPaperIntersection();

    if(!hasIntersection){
        println("No intersection..");
	return;
    }

    planeCalibKinect.flipNormal();
    planeCalibKinect.moveAlongNormal(-15f);

    planeProjCalib.setPlane(planeCalibKinect);
    planeProjCalib.setHomography(homographyCalibration);

    }

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

   HomographyCalibration.saveMatTo(this,
				    cameraKinectTransform,
				    Papart.kinectTrackingCalib);

   // homographyCalibration.saveTo(this, Papart.homographyCalib);
   // planeCalibration.saveTo(this, Papart.homographyCalib);
   println("All is saved.");
}
