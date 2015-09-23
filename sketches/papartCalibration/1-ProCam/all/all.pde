import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.calibration.*;
import fr.inria.papart.drawingapp.*;
import org.bytedeco.javacpp.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.guimodes.Mode;

import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_64F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvConvertScale;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;


import org.bytedeco.javacpp.opencv_core.IplImage;


PApplet mainApplet;

// Display, for AR rendering.
ProjectorDisplay projector;

// "Cameras"
Camera camera;
ProjectorAsCamera  projectorAsCamera;

// Papart Objects.
Papart papart;
MyApp app;
MarkerBoard board;

// view of the projector
TrackedView projectorView;

// set in cameraView.pde
PVector[] corners = new PVector[4];

// User interface
ControlFrame controlFrame;


public void settings(){
    fullScreen(P3D);
}

public void setup() {
    mainApplet = this;

    Papart.projection(this);

    papart =  Papart.getPapart();
    projector = papart.getProjectorDisplay();
    camera = papart.getCameraTracking();

    app = new MyApp();
    app.pre(); // -> forces the initialization.

    board = app.getBoard();

    initProjectorAsCamera();

    controlFrame = new ControlFrame();

    Mode.add("KinectOne");
    Mode.add("Kinect360");
    Mode.add("None");

    CameraView cameraView = new CameraView();

    papart.startTracking();
}

private void initProjectorAsCamera(){
    projectorView = new TrackedView();
    projectorView.setImageWidthPx(projector.getWidth());
    projectorView.setImageHeightPx(projector.getHeight());
    projectorView.init();

    projectorAsCamera = new ProjectorAsCamera();
    projectorAsCamera.setCalibration(Papart.projectorCalib);
    projectorAsCamera.setParent(this);

    String ARToolkitCalibFile =  sketchPath() + "/data/projector.cal";
    ProjectorAsCamera.convertARProjParams(this, projectorAsCamera.getCalibrationFile(),
                                          ARToolkitCalibFile);
    projectorAsCamera.initMarkerDetection(ARToolkitCalibFile);
    projectorAsCamera.trackMarkerBoard(board);

}

// No modes for now...

boolean isKinectOne = false;
boolean isKinect360 = false;

void setKinectOne(boolean useKinectOne){
    if(useKinectOne)
        activateKinectOne();
    else
        deActivateKinectOne();

}

void activateKinectOne(){
    controlFrame.hideKinectButtons();
    Mode.set("KinectOne");
    println("Kinect One activation");
    isKinectOne = true;
    initKinectOne();
}

void deActivateKinectOne(){
    Mode.set("None");
    isKinectOne = false;
    stopKinectOne();
}


void setKinect360(boolean useKinect360){
    if(useKinect360)
        activateKinect360();
    else
        deActivateKinect360();
}

void activateKinect360(){
    controlFrame.hideKinectButtons();

    Mode.set("Kinect360");
    println("Kinect 360 activation");
    isKinect360 = true;
    initKinect360();
}



void deActivateKinect360(){


    Mode.set("None");
    isKinect360 = false;
    stopKinect360();
}

// void setProjection(){
//     if(Mode.is("Projection"))
//         return;
//     Mode.set("Projection");
//     projector.automaticMode();
//     papart.forceProjectorSize();
// }





boolean isCalibrated = false;

// required :
// Position of the Markerboard from the camera.
// Position of the Markerboard from the projector
public void calibrate(){

    PMatrix3D camPaper = camBoard().get();
    PMatrix3D projPaper = projBoard().get();

    camPaper.print();
    projPaper.print();

    projPaper.invert();
    projPaper.preApply(camPaper);
    projPaper.print();
    projPaper.invert();

    papart.saveCalibration(Papart.cameraProjExtrinsics, projPaper);
    // projPaper.print();
    projector.setExtrinsics(projPaper);


    if(isKinectOne){
        calibrateKinectOne();
    }

    if(isKinect360){
        calibrateKinect360();
    }

}

private void calibrateKinectOne(){
    planeCalibCam = PlaneCalibration.CreatePlaneCalibrationFrom(camBoard().get(),
                                                                new PVector(297, 210));
    planeCalibCam.flipNormal();
    kinectCameraExtrinsics.reset();
    computeScreenPaperIntersection(planeCalibCam);

    // move the plane up a little.
    planeCalibCam.moveAlongNormal(-10f);

    saveKinectCalibration(planeCalibCam);
}

void saveKinectCalibration(PlaneCalibration planeCalib){
    planeProjCalib.setPlane(planeCalib);
    planeProjCalib.setHomography(homographyCalibration);

    planeProjCalib.saveTo(this, Papart.planeAndProjectionCalib);
    HomographyCalibration.saveMatTo(this,
                                    kinectCameraExtrinsics,
                                    Papart.kinectTrackingCalib);

    papart.setTableLocation(camBoard().get());
    println("Calibration OK");
    isCalibrated = true;
}

private void calibrateKinect360(){
    PVector paperSize = new PVector(297, 210);
    PMatrix3D kinectPaperTransform =  kinect360Board();

    PlaneCalibration planeCalibKinect =
        PlaneCalibration.CreatePlaneCalibrationFrom(kinectPaperTransform, paperSize);
    planeCalibCam = PlaneCalibration.CreatePlaneCalibrationFrom(camBoard().get(), paperSize);
    planeCalibCam.flipNormal();


    kinectCameraExtrinsics = camBoard().get();
    kinectCameraExtrinsics.invert();
    kinectCameraExtrinsics.preApply(kinectPaperTransform);
    println("Kinect - Camera extrinsics : ");
    kinectCameraExtrinsics.print();

    boolean inter = computeScreenPaperIntersection(planeCalibCam);
    if(!inter){
        println("No intersection");
        kinect360Board().print();
        return;
    }

    // move the plane up a little.
    planeCalibKinect.flipNormal();
    planeCalibKinect.moveAlongNormal(-15f);

    saveKinectCalibration(planeCalibKinect);
}

PMatrix3D camBoard(){
    return board.getTransfoMat(camera);
}

PMatrix3D kinect360Board(){
    assert(isKinect360Activated);
    return board.getTransfoMat(cameraKinect);
}

PMatrix3D projBoard(){
    IplImage projImage = projectorImage();
    if(projImage == null)
        return null;
    board.updatePosition(projectorAsCamera, projImage);
    return board.getTransfoMat(projectorAsCamera);
}

IplImage grayImage = null;

IplImage projectorImage(){

    projectorView.setCorners(corners);
    IplImage projImage = projectorView.getIplViewOf(camera);

    if(board.useARToolkit()){
        projImage =  greyProjectorImage(projImage);
    }

    return projImage;
}


IplImage greyProjectorImage(IplImage projImage){
    if(grayImage == null){
        grayImage = IplImage.create(projector.getWidth(),
                                    projector.getHeight(),
                                    IPL_DEPTH_8U, 1);
    }
    cvCvtColor(projImage, grayImage, CV_BGR2GRAY);

    if(test){
        cvSaveImage( sketchPath() + "/data/projImage.jpg", grayImage);
        cvSaveImage( sketchPath() + "/data/camImage.jpg", camera.getIplImage());
    }

    return grayImage;
}



public void draw(){
    if(test)
        calibrate();

    fill(255);
    rect(0,0, 5, 5);

    fill(255);
    rect(width-5,0, 5, 5);

    fill(255);
    rect(width-5,height-5, 5, 5);

    fill(255);
    rect(0, height-5, 5, 5);

}

boolean test = false;
boolean test2 = false;
public void keyPressed(){

    if(key == 't'){
        test = !test;
    }

    if(key == 's'){
        test2 = !test2;
    }
}


int currentCorner = 0;
void activeCorner(int value){
    if(value == -1)
        value = 0;
    currentCorner = value;
}

void mouseDragged() {
    if(Mode.is("Corners")){
        corners[currentCorner].set(mouseX, mouseY);
    }
}
