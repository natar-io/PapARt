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

ArrayList<CalibrationPoint> pointList;

// size does not matter, I think.
PVector paperSize = new PVector(297, 210);

public void setup(){

    // TODO: Kinect ?
    size(800, 600, OPENGL);
    // Camera

    papart = new Papart(this);
    decodedCode = DecodedCode.loadFrom(this, "scan0");


    cameraTracking = CameraFactory.createCamera(papart.cameraConfiguration.getCameraType(),
						       papart.cameraConfiguration.getCameraName());
    cameraTracking.setParent(this);
    cameraTracking.setCalibration(papart.proCamCalib);

    cameraTracking.convertARParams(this, papart.proCamCalib, papart.camCalibARtoolkit);
    cameraTracking.initMarkerDetection(papart.camCalibARtoolkit);
    
    markerBoard = new MarkerBoard(sketchPath + "/data/big.cfg", paperSize.x, paperSize.y); // the size does not matter here. 
    cameraTracking.trackMarkerBoard(markerBoard);

    // TODO: make this work without starting the camera...
    
    cameraTracking.start();
    // grab a few images...
    for(int i = 0; i < 10; i++)
	cameraTracking.grab();

    // cameraImg = decodedCode.getRefImage();
    // cameraImgIpl = decodedCode.getRefImageIpl();

    cameraImg = cameraTracking.getPImage();
    cameraImgIpl = cameraTracking.getIplImage();
    
    markerBoard.updatePosition(cameraTracking, cameraImgIpl);
    cameraPaperTransform = markerBoard.getTransfoMat(cameraTracking);

    println("Camera Paper...");
    cameraPaperTransform.print();
	
    planeCalibCam =  PlaneCalibration.CreatePlaneCalibrationFrom(cameraPaperTransform, paperSize);
    planeCalibCam.flipNormal();

    
    projector = new ProjectorDisplay(this, papart.proCamCalib);

    cameraDevice = cameraTracking.getProjectiveDevice();
    projectorDevice = projector.getProjectiveDeviceP();
    
    createPoints();

    // Now we have Camera Points, and Projector Points.
    // Projector Points are OK, for Camera we can reproject the plane
    // found from the Tracking... 
    projectCameraPoints();


    // Now we have the couples object/image.
    PVector imagePoints[] = new PVector[nbValidPoints];
    PVector objectPoints[] = new PVector[nbValidPoints];
    
    int k = 0;
    for(CalibrationPoint cp : pointList){
	if(!cp.isValid)
	    continue;
	
	imagePoints[k] = cp.imageProj;
	//	imagePoints[k] = cp.imageCam;
	objectPoints[k] = cp.object;
	k++;
    }
    if(k > 100){
	PMatrix3D orientation = projectorDevice.estimateOrientationRansac(objectPoints, imagePoints);

	println("Transformation found... " + k );
	orientation.print();

	// not sure why !
	papart.saveCalibration("camProjExtrinsics.xml", orientation);
    } else {
	println("Not enough points...");
    }

    image(cameraImg, 0, 0, width, height);

}

void createPoints(){
    
    boolean[] validPoints = decodedCode.getMask();
    int[] decodedX = decodedCode.getDecodedX();
    int[] decodedY = decodedCode.getDecodedY();

    pointList = new ArrayList<CalibrationPoint>(); 

    // check all the image for valid Points to create Pairs
    // iteration in cameraPoints. 
    int offset = 0;
    for(boolean isValid : validPoints){
	offset++;
	if(!isValid)
	    continue;


	int x = offset % cameraImg.width;
	int y = offset / cameraImg.width;

	CalibrationPoint cp = new CalibrationPoint();

	cp.imageCam.set(x, y);
	cp.imageProj.set(decodedX[offset], decodedY[offset]);

	//	println(cp.imageCam + " " + cp.imageProj);
	
	pointList.add(cp);
    }
}




// // size of the Projector's image
// int frameWidth, frameHeight;

int nbValidPoints;

void projectCameraPoints(){

    // frameWidth = projectorDevice.getWidth();
    // frameHeight = projectorDevice.getHeight();
    nbValidPoints = 0;
    
    for(CalibrationPoint cp : pointList){
	
	PVector intersection = computeIntersection((int) cp.imageCam.x, (int) cp.imageCam.y);
	if(intersection == null){
	    continue;
	}

	nbValidPoints++;
	cp.object.set(intersection);
	cp.isValid = true;
    }
}



// px and py in pixels... not homogeneous coordinates.

PVector computeIntersection(int px, int py){

    // Create a ray from the Camera, to intersect with the paper found.     
    
    PVector origin = new PVector(0, 0, 0);
    PVector viewedPt = cameraDevice.pixelToWorldNormP(px, py);
    
    Ray3D ray
	= new Ray3D(new Vec3D(origin.x,
			      origin.y,
			      origin.z),
		    new Vec3D(viewedPt.x,
			      viewedPt.y,
			      viewedPt.z));
    
    // Intersect ray with Plane 
    ReadonlyVec3D inter = planeCalibCam.getPlane().getIntersectionWithRay(ray);
    
    if(inter == null){
	// println("No intersection :( check stuff");
	return null;
    }
	
    return new PVector(inter.x(), inter.y(), inter.z());
}



class CalibrationPoint {
    public PVector imageProj = new PVector();
    public PVector imageCam = new PVector();
    public PVector object = new PVector();
    public boolean isValid = false;
    public CalibrationPoint(){
    }
}



void draw(){

}  

