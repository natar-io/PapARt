import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.depthcam.devices.*;

import fr.inria.papart.calibration.HomographyCalibration;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.freenect;
import toxi.geom.*;
import peasy.*;

import fr.inria.skatolo.*;
import fr.inria.skatolo.events.*;
import fr.inria.skatolo.gui.controllers.*;
import fr.inria.skatolo.gui.group.*;


Camera cameraRGB, cameraIR, cameraDepth;
KinectDevice kinectDevice;
KinectPointCloud pointCloud;
KinectProcessing kinectAnalysis;

PMatrix3D stereoCalib;

Skatolo skatolo;
PeasyCam cam;

int skip = 1;
float translationX = 15;
float translationY = 15;

void settings(){
  size(800, 600, OPENGL);
}


void setup(){

    kinectDevice = Papart.loadDefaultKinectDevice(this);

    kinectDevice.setStereoCalibration(Papart.kinectStereoCalib);

    cameraRGB = kinectDevice.getCameraRGB();
    cameraDepth = kinectDevice.getCameraDepth();
// cameraIR = kinectDevice.getCameraIR();

    kinectAnalysis = new KinectProcessing(this, kinectDevice);
    pointCloud = new KinectPointCloud(this, kinectAnalysis, skip);

    try{
        stereoCalib = HomographyCalibration.getMatFrom(this, Papart.kinectStereoCalib);
        translationX = stereoCalib.m03;
        translationY = stereoCalib.m13;
    } catch(Exception e){
        println("File invalid or not found, load default values. " + e);
        stereoCalib = new PMatrix3D(1, 0, 0, 15,
                                    0, 1, 0, 0,
                                    0, 0, 1, 0,
                                    0, 0, 0, 1);
    }

    // Set the virtual camera
    cam = new PeasyCam(this, 0, 0, -800, 800);
    cam.setMinimumDistance(0);
    cam.setMaximumDistance(5000);
    cam.setActive(true);

    // GUI
    skatolo = new Skatolo(this);
    skatolo.addSlider("translationX")
        .setPosition(30, 50)
        .setValue(stereoCalib.m03)
        .setRange(-80, 50)
        .setSize(400, 12);
    // Manual draw.

    skatolo.addSlider("translationY")
        .setPosition(30, 70)
        .setValue(stereoCalib.m13)
        .setRange(-50, 50)
        .setSize(400, 12);

    skatolo.setAutoDraw(false);
    textFont(createFont("",15));
}




void draw(){
    background(100);
   // retreive the camera image.

    try {
	cameraRGB.grab();
        cameraDepth.grab();
// cameraIR.grab();
    } catch(Exception e){
	println("Could not grab the image " + e);
    }

    IplImage colourImg = cameraRGB.getIplImage();
    IplImage depthImg = cameraDepth.getIplImage();

    stereoCalib.m03 = translationX;
    stereoCalib.m13 = translationY;
    kinectDevice.setStereoCalibration(stereoCalib);
    kinectAnalysis.update(depthImg, colourImg, skip);

    pointCloud.updateWith(kinectAnalysis);
    pointCloud.drawSelf((PGraphicsOpenGL) g);

    cam.beginHUD();
    text("'m' to stop the camera, 's' to save the calibration.", 10, 30);
    skatolo.draw();
    cam.endHUD(); // always!

}

boolean isMouseControl = true;

void keyPressed(){

    if(key =='m'){
	isMouseControl = !isMouseControl;
	cam.setMouseControlled(isMouseControl);
    }

    if(key == 's'){
	HomographyCalibration.saveMatTo(this, stereoCalib, Papart.kinectStereoCalib);
	println("Default stereo calibration saved.");
    }
}

void close(){
    try{
	kinectDevice.close();
    }catch(Exception e){
    }
}
