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
KinectProcessing kinect;

// int depthFormat = freenect.FREENECT_DEPTH_MM;
// int kinectFormat = Kinect.KINECT_MM;

int skip = 2;

void settings(){
  size(800, 600, P3D);
}


Camera cameraRGB, cameraIR, cameraDepth;
KinectOne kinectOne;

void setup(){

    kinectOne = new KinectOne(this);
    cameraRGB = kinectOne.getCameraRGB();
    cameraIR = kinectOne.getCameraIR();
    cameraDepth = kinectOne.getCameraDepth();

    kinect = new KinectProcessing(this, kinectOne);
  // kinect.setStereoCalibration(Papart.kinectStereoCalib);

    pointCloud = new KinectPointCloud(this, kinect, skip);

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
        cameraIR.grab();
        cameraDepth.grab();
    } catch(Exception e){
	println("Could not grab the image " + e);
    }

    IplImage colourImg = cameraRGB.getIplImage();
    IplImage depthImg = cameraDepth.getIplImage();

    if(colourImg == null || depthImg == null)
	return;

    try{
        kinect.update(depthImg, colourImg, skip);
        pointCloud.updateWith(kinect);
        pointCloud.drawSelf((PGraphicsOpenGL) g);
    }catch(Exception e){ e.printStackTrace(); }

    // debugVideo();
}

void debugVideo(){

    PImage im = cameraRGB.getPImage();
    if(im != null)
	image(im, 0, 0, 200, 200);
    im = cameraIR.getPImage();
    if(im != null)
	image(im, 200, 0, 200, 200);
    im = cameraDepth.getPImage();
    if(im != null)
	image(im, 0, 200, 200, 200);


}

void close(){
    try{
	cameraIR.close();
        cameraRGB.close();
        cameraDepth.close();
    }catch(Exception e){
    }
}
