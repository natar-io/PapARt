import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.depthcam.*;

import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.freenect;
import toxi.geom.*;
import peasy.*;

PeasyCam cam;

PointCloudKinect pointCloud;

CameraOpenKinect camera;
KinectProcessing kinect;

int depthFormat = freenect.FREENECT_DEPTH_MM;
int kinectFormat = Kinect.KINECT_MM;

int skip = 2;

void setup(){

  size(800, 600, OPENGL); 

  camera = (CameraOpenKinect) CameraFactory.createCamera(Camera.Type.OPEN_KINECT, 0);
  camera.setParent(this);
  camera.setCalibration(Papart.kinectRGBCalib);
  camera.getDepthCamera().setDepthFormat(depthFormat);
  camera.getDepthCamera().setCalibration(Papart.kinectIRCalib);
  camera.start();

  kinect = new KinectProcessing(this, camera);
  kinect.setStereoCalibration(Papart.kinectStereoCalib);

  pointCloud = new PointCloudKinect(this, skip);

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
	camera.grab();
    } catch(Exception e){
	println("Could not grab the image " + e);
    }
    
    IplImage colourImg = camera.getIplImage();
    IplImage depthImg = camera.getDepthCamera().getIplImage();

    if(colourImg == null || depthImg == null)
	return;

    kinect.update(depthImg, colourImg, skip);
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);
}

void close(){
    try{
	camera.close();
    }catch(Exception e){
    }
}
