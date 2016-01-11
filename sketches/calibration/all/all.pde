import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.tracking.*;
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
ARDisplay arDisplay;

// "Cameras"
Camera camera;
ProjectorAsCamera  projectorAsCamera;

// Papart Objects.
Papart papart;
MyApp app;
MarkerBoard board, kinectBoard;

// view of the projector
TrackedView projectorView;



// User interface
ControlFrame controlFrame;

// projection corners to select from the camera
ProjectorCorners projectorCorners;

public void settings(){
    //fullScreen(P3D);
    size(200, 200, P3D);
}



public void setup() {
    mainApplet = this;

    Papart.projection(this);

    papart =  Papart.getPapart();
    projector = papart.getProjectorDisplay();
    camera = papart.getCameraTracking();

    projector.manualMode();

    arDisplay = new ARDisplay(this, camera);
    arDisplay.init();
    arDisplay.manualMode();

    app = new MyApp();
    app.pre(); // -> forces the initialization.
    board = app.getBoard();

    arDisplay.addScreen(app.getScreen());

    initModes();

    initProjectorAsCamera();
    checkKinectVersion();
    initNames();

    initCorners();

    // Kinect version should be known before the control frame is created.
    controlFrame = new ControlFrame();
    projectorCorners = new ProjectorCorners();
    projectorCorners.getSurface().setVisible(false);

    activateKinect();
    papart.startTracking();
}

String cameraName, screenName, kinectName;
int screenWidth, screenHeight, screenOffsetX, screenOffsetY;

void initNames(){
    CameraConfiguration cameraConfig = Papart.getDefaultCameraConfiguration(this);
    cameraName = cameraConfig.getCameraType().name()+ " " + cameraConfig.getCameraName();

    ScreenConfiguration screenConfig = Papart.getDefaultScreenConfiguration(this);
    screenName = screenConfig.getProjectionScreenWidth() + "x" +
        screenConfig.getProjectionScreenHeight() + " at " +
        screenConfig.getProjectionScreenOffsetX() + "," +
        screenConfig.getProjectionScreenOffsetY() + ".";

    screenWidth = screenConfig.getProjectionScreenWidth();
    screenHeight = screenConfig.getProjectionScreenHeight();
    screenOffsetX = screenConfig.getProjectionScreenOffsetX();
    screenOffsetY = screenConfig.getProjectionScreenOffsetY();

    if(isKinectOne || isKinect360){
        CameraConfiguration config = Papart.getDefaultKinectConfiguration(this);
        kinectName = config.getCameraType().name()+ " " + config.getCameraName();
    } else{
        kinectName = "No Kinect";
    }

}

void initModes(){
    Mode.add("None");
    Mode.add("CamManual");
    Mode.add("CamView");
    Mode.add("CamMarker");
    Mode.add("ProjManual");
    Mode.add("ProjMarker");
    Mode.add("ProjView");
    Mode.add("KinectManual");
    Mode.add("KinectMarker");
    Mode.add("Kinect3D");
}

ARToolKitPlus.MultiTracker projectorTracker = null;

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
    projectorTracker = DetectedMarker.createDetector(projector.getWidth(), projector.getHeight());
}



public void draw(){
    Camera currentCamera = null;
    // ellipse(mouseX, mouseY, 10, 10);
    // Automatic modes...

    if(Mode.is("CamMarker")){
        controlFrame.setText(currentCamBoard());
    }

    if(Mode.is("KinectMarker")){
        controlFrame.setText(currentKinect360Board());
    }

    //background(0);

    if(Mode.is("None")){
        background(100);
        return;
    }

    if(Mode.is("CamView") ||
       Mode.is("CamManual")){

        currentCamera = camera;
        PImage cameraImage = camera.getPImage();
        if(cameraImage != null){
            image(cameraImage, 0, 0, width, height);
        }
    }

    if(Mode.is("ProjMarker")){
        PImage cameraImage = camera.getPImage();
        currentCamera = camera;
        if(cameraImage != null){
            image(cameraImage, 0, 0, width, height);
            controlFrame.setText(projBoard());
        }

    }


    if(Mode.is("KinectManual")){
        PImage kinectImg = cameraKinect.getPImage();
        currentCamera = cameraKinect;
        if(kinectImg != null){
            image(kinectImg, 0, 0, width, height);

        } else {
            println("No Kinect Image");
        }

    }


    if(Mode.is("ProjManual") || Mode.is("CamManual") || Mode.is("KinectManual")){
        draw3DCorners();
    }

    if(areCorners){
        fill(0, 180,0, 100);
        quad(corners[0].x, corners[0].y,
             corners[1].x, corners[1].y,
             corners[2].x, corners[2].y,
             corners[3].x, corners[3].y);

        noFill();
        rectMode(CENTER);
        stroke(255);
        strokeWeight(1);

        pushMatrix();
        translate(corners[currentCorner].x,
                  corners[currentCorner].y,
                  0);
        rect(0, 0,15, 15);
        popMatrix();

        if(showCornerZoom && currentCamera != null){
            PImage currentImage = currentCamera.getPImageCopy();
            int previewSize = 10;
            PImage cornerPreview = currentImage.get((int) (corners[currentCorner].x - previewSize),
                                                    (int) (corners[currentCorner].y - previewSize),
                                                    previewSize * 2,
                                                    previewSize * 2);
            int previewSizeVisu = 5;
            image(cornerPreview, 0 ,0, previewSize * 2 * previewSizeVisu, previewSize * 2 * previewSizeVisu);

            pushMatrix();
            translate(previewSize * previewSizeVisu, previewSize * previewSizeVisu, 0);

            stroke(255);
            strokeWeight(1);
            int crossSize = previewSize * previewSizeVisu / 2;
            line(-crossSize, 0, crossSize, 0);
            line(0, -crossSize, 0, crossSize);
            popMatrix();
        }

        noStroke();
    }


}
