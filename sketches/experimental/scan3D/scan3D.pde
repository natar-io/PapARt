import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.scanner.*;
import fr.inria.papart.calibration.*;


import static org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import fr.inria.guimodes.Mode;
import fr.inria.papart.scanner.GrayCode;
import fr.inria.papart.scanner.*;


Papart papart;
DecodedCode decodedCode;
Camera cameraTracking;
MarkerBoard markerBoard;
PMatrix3D cameraPaperTransform;
ProjectorDisplay projector;
ProjectiveDeviceP projectorDevice, cameraDevice;


PImage cameraImg;
PImage cameraImg2;
IplImage cameraImgIpl;


PlaneCalibration planeCalibCam;
Scanner3D scanner;

// size does not matter, I think.
PVector paperSize = new PVector(297, 210);

public void setup(){

    // TODO: Kinect ?
    size(800, 600, OPENGL);
    // Camera

    papart = new Papart(this);

    cameraTracking = CameraFactory.createCamera(papart.cameraConfiguration.getCameraType(),
						       papart.cameraConfiguration.getCameraName());
    cameraTracking.setParent(this);
    cameraTracking.setCalibration(papart.proCamCalib);
    
    projector = new ProjectorDisplay(this, papart.proCamCalib);
    decodedCode = DecodedCode.loadFrom(this, "../../../papartCalibration/1-ProCam/grayCode/capture/scan0");


    scanner = new Scanner3D(cameraTracking.getProjectiveDevice(), projector);

    scanner.compute3DPos(decodedCode, 2);
    scanner.savePoints(this, "points.obj");
    println("OK");
    exit();
}



void draw(){

}  

