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
import java.util.Iterator;
import java.util.ArrayList;


// Undecorated frame
public void init() {
    frame.removeNotify();
    frame.setUndecorated(true);
    frame.addNotify();
    super.init();
}


int precision = 4;

PFont font;
PGraphicsOpenGL bloodGraphics;

PlaneAndProjectionCalibration planeProjCalibration;
CameraOpenKinect camera;
KinectProcessing kinect;

TouchDetectionSimple3D touchDetection3D;

void setup(){

    Papart papart = Papart.projection2D(this);

    camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
    camera.setParent(this);
    camera.setCalibration(Papart.kinectRGBCalib);
    camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
    camera.start();

    try{
        planeProjCalibration = new  PlaneAndProjectionCalibration();
        planeProjCalibration.loadFrom (this, Papart.planeAndProjectionCalib);
    }
    catch (NullPointerException e) {
        die("Impossible to load the plane calibration...");
    }

    kinect = new KinectProcessing(this, camera);

    touchDetection3D = new TouchDetectionSimple3D(Kinect.SIZE);

     //     initBlood();

    background(0);
    font = loadFont("WCRhesusBBta-48.vlw"); //load the font stored in the data file





    IplImage kinectImg;
    IplImage} kinectImgDepth;

void draw(){

    camera.grab();
    kinectImg = camera.getIplImage();
    kinectImgDepth = camera.getDepthCamera().getIplImage();
    if(kinectImg == null || kinectImgDepth == null){
	return;
    }

    kinect.updateMT(kinectImgDepth, kinectImg, planeProjCalibration, precision);

    drawBlood();

}


boolean perCentChance(float value){
    return random(1) <  (value / 100f);
}
