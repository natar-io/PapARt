import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.depthcam.devices.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.freenect;
import toxi.geom.*;
import peasy.*;

PeasyCam cam;

KinectPointCloud pointCloud;
KinectProcessing kinectAnalysis;

int skip = 2;

void settings(){
  size(800, 600, P3D);
}


Camera cameraRGB, cameraIR, cameraDepth;
KinectDevice kinectDevice;

void setup(){

    kinectDevice = KinectDevice.createKinectOne(this);
    // kinectDevice = KinectDevice.createKinect360(this);

    kinectDevice.setStereoCalibration(Papart.kinectStereoCalib);

    cameraRGB = kinectDevice.getCameraRGB();
    cameraDepth = kinectDevice.getCameraDepth();
// cameraIR = kinectDevice.getCameraIR();

    kinectAnalysis = new KinectProcessing(this, kinectDevice);
    pointCloud = new KinectPointCloud(this, kinectAnalysis, skip);

  // Set the virtual camera
    cam = new PeasyCam(this, 0, 0, -800, 800);
    cam.setMinimumDistance(0);
    cam.setMaximumDistance(5000);
    cam.setActive(true);
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

    if(colourImg == null || depthImg == null)
	return;

    try{
        kinectAnalysis.update(depthImg, colourImg, skip);
        pointCloud.updateWith(kinectAnalysis);
        pointCloud.drawSelf((PGraphicsOpenGL) g);
    }catch(Exception e){ e.printStackTrace(); }

    // debugVideo();
}

void debugVideo(){

    PImage im = cameraRGB.getPImage();
    if(im != null)
	image(im, 0, 0, 200, 200);
    im = cameraDepth.getPImage();
    if(im != null)
	image(im, 0, 200, 200, 200);

    // im = cameraIR.getPImage();
    // if(im != null)
    //     image(im, 200, 0, 200, 200);
}

void close(){
    try{
//	cameraIR.close();
        cameraRGB.close();
        cameraDepth.close();
    }catch(Exception e){
    }
}
