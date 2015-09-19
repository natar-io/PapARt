import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.drawingapp.*;
import org.bytedeco.javacpp.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.guimodes.Mode;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_64F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvConvertScale;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_RGBA2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;


import org.bytedeco.javacpp.opencv_core.IplImage;

import fr.inria.skatolo.*;
import fr.inria.skatolo.gui.controllers.*;

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
PVector[] corners = new PVector[4];

// User interface
ControlFrame controlFrame;


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

    app = new MyApp();
    // force the initialization.
    app.pre();

    board = app.getBoard();

    initProjectorAsCamera();

    controlFrame = new ControlFrame();

    Mode.add("Corners");
    Mode.add("Projection");

    papart.startTracking();
}

private void initProjectorAsCamera(){
    projectorView = new TrackedView();
    projectorView.setImageWidthPx(projector.getWidth());
    projectorView.setImageHeightPx(projector.getHeight());
    projectorView.init();

    projectorAsCamera = new ProjectorAsCamera();
    projectorAsCamera.setCalibration(Papart.projectorCalib);

    String ARToolkitCalibFile =  sketchPath() + "/data/projector.cal";
    Camera.convertARParams(this, projectorAsCamera.getCalibrationFile(),
                           ARToolkitCalibFile);
    projectorAsCamera.initMarkerDetection(ARToolkitCalibFile);

    board.addTracker(this, projectorAsCamera);

    // Corners of the image of the projector
    corners[0] = new PVector(100, 100);
    corners[1] = new PVector(200, 100);
    corners[2] = new PVector(200, 200);
    corners[3] = new PVector(100, 200);
}

void setCorners(){
    if(Mode.is("Corners"))
        return;

    Mode.set("Corners");
    projector.manualMode();
    papart.forceCameraSize();
}

void setProjection(){

    if(Mode.is("Projection"))
        return;

    Mode.set("Projection");
    projector.automaticMode();
    papart.forceProjectorSize();
}


// required :
// Position of the Markerboard from the camera.
// Position of the Markerboard from the projector
void calibrate(){

    PMatrix3D camPaper = camBoard().get();
    PMatrix3D projPaper = projBoard().get();

    // BIM camera -> Projector.
    // camPaper.invert();
    // camPaper.preApply(projPaper);
    // projector.setExtrinsics(camPaper);

// BIM Projector ->  Camera
    projPaper.invert();
    projPaper.preApply(camPaper);
    projector.setExtrinsics(projPaper);

    // camPaper.print();
    // projPaper.print();


    // projPaper.invert();
    // projPaper.preApply(camPaper);

    // projPaper.invert();

    // // papart.saveCalibration(Papart.cameraProjExtrinsics, projPaper);
    // projPaper.print();
    // projector.setExtrinsics(projPaper);

}

PMatrix3D camBoard(){
    return app.getLocation();
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
    return grayImage;
}



public void draw(){

    if(test)
        calibrate();


    if(Mode.is("Projection"))
        return;

    PImage camImage = camera.getPImage();
    if(camImage == null)
        return;

    image(camImage, 0, 0, width, height);


    fill(0, 180,0, 100);
    quad(corners[0].x, corners[0].y,
         corners[1].x, corners[1].y,
         corners[2].x, corners[2].y,
         corners[3].x, corners[3].y);

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
