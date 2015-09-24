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
ARDisplay arDisplay;

// "Cameras"
Camera camera;
ProjectorAsCamera  projectorAsCamera;

// Papart Objects.
Papart papart;
MyApp app;
MyApp appAR;
MarkerBoard board;

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


    Mode.add("None");
    initProjectorAsCamera();
    checkKinectVersion();
    initCorners();

    // Kinect version should be known before the control frame is created.
    controlFrame = new ControlFrame();
    projectorCorners = new ProjectorCorners();
    projectorCorners.getSurface().setVisible(false);

    activateKinect();


    // CameraView cameraView = new CameraView();
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



public void draw(){

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

        PImage cameraImage = camera.getPImage();
        if(cameraImage != null){
            image(cameraImage, 0, 0, width, height);
        }
    }

    if(Mode.is("ProjMarker")){
        PImage cameraImage = camera.getPImage();
        if(cameraImage != null){
            image(cameraImage, 0, 0, width, height);

            controlFrame.setText(projBoard());
        }

    }


    if(Mode.is("KinectManual")){
        PImage kinectImg = cameraKinect.getPImage();
        if(kinectImg != null){
            image(kinectImg, 0, 0, width, height);
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
    }

}
