import fr.inria.papart.procam.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.kinect.*;
import fr.inria.papart.multitouchKinect.*;
import fr.inria.papart.drawingapp.*;

// Loading javaCV and javaCPP
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
//import com.googlecode.javacv.processing.*;

import javax.media.opengl.GL;
import processing.opengl.*;

import codeanticode.glgraphics.*;
import codeanticode.gsvideo.*;


Camera cameraTracking;
Projector projector;
float screenResolution = 3;
PFont font;

boolean useBig = false;

MarkerBoard lightBoard, patientBoard, imageBoard;
MarkerBoard[] boards;


PVector captureSize = new PVector(75, 75);
PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm

PaperInterface[] paperInterfaces;
Eclairage eclairage;
Radio radio;
Patient patient;

// Undecorated frame 
public void init() {
    frame.removeNotify(); 
    frame.setUndecorated(true); 
    frame.addNotify(); 
    super.init();
}

void setup(){

    size(frameSizeX, frameSizeY, GLConstants.GLGRAPHICS);
 
    String calibrationFile, calibrationARToolKitFile;

    ///////////////////////////////////////////////
    //////////////// Projector ////////////////////
    ///////////////////////////////////////////////

    calibrationFile = sketchPath + "/data/calibration/calibration-p1.yaml";
    projector = new Projector(this,  calibrationFile,
			      frameSizeX, frameSizeY, 200, 2000);
    

    //////////////////////////////////////////////
    /////////// Marker Board ////////////////////
    ////////////////////////////////////////////


    if(useBig){

    lightBoard = new MarkerBoard(sketchPath + "/data/markers/a3/A3p1.cfg",
				 "light management", 
				 (int) boardSize.x, (int)boardSize.y); 

    patientBoard = new MarkerBoard(sketchPath + "/data/markers/a3/A3p2.cfg",
				 "patient management", 
				 (int) boardSize.x, (int)boardSize.y); 

    imageBoard = new MarkerBoard(sketchPath + "/data/markers/a3/A3p3.cfg",
				 "image management", 
				 (int) boardSize.x, (int)boardSize.y); 

    }else {

   lightBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2285.cfg",
				 "light management", 
				 (int) boardSize.x, (int)boardSize.y); 

    patientBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2290.cfg",
				 "patient management", 
				 (int) boardSize.x, (int)boardSize.y); 

    imageBoard = new MarkerBoard(sketchPath + "/data/markers/nouveaux/2300.cfg",
				 "image management", 
				 (int) boardSize.x, (int)boardSize.y); 

    }

    boards = new MarkerBoard[3];
    boards[0] = lightBoard;
    boards[1] = patientBoard;
    boards[2] = imageBoard;

    //////////////////////////////////////////////
    ////////////// Tracking camera ///////////////
    //////////////////////////////////////////////

    calibrationARToolKitFile = sketchPath + "/data/calibration/p1-art.dat";
    Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, cameraX, cameraY);

    cameraTracking = new Camera(this, cameraNo, cameraX, cameraY, calibrationFile, cameraType);

    cameraTracking.initMarkerDetection(this, calibrationARToolKitFile, boards);
    cameraTracking.setThread();
    cameraTracking.setAutoUpdate(true);


    //////// Set the tracked view camera /////// 
    //    boardView = new TrackedView(markerBoard, cameraCapture, capturePos, captureSize, camera2X, camera2Y);

    ///////////////////////////////////////////////
    ///////////// Kinect init /////////////////////
    ///////////////////////////////////////////////

    initKinect();
    kinectCalibFile = "data/calibration/KinectScreenCalibration.txt";
    touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, false, 3, 5);
 
    ////////////////////////////////////////////
    /////////// Init GUI ///////////////////////
    ////////////////////////////////////////////

    ////////// Set Button font //////////

    // Used in button management for time

    DrawUtils.applet = this;
    font = loadFont(sketchPath + "/data/Font/GentiumBookBasic-48.vlw");
    Button.setFont(font);
    Button.setFontSize(12);



    //////////////////////////////////////////////
    ////////////   Screens   /////////////////////
    //////////////////////////////////////////////

    eclairage = new Eclairage(this, lightBoard, 
			      boardSize, 
			      cameraTracking, 
			      screenResolution, projector);

    radio = new Radio(this, imageBoard, 
			      boardSize, 
			      cameraTracking, 
			      screenResolution, projector);

    patient = new Patient(this, patientBoard, 
			  boardSize, 
			  cameraTracking, 
			  screenResolution, projector);
    
    paperInterfaces = new PaperInterface[3];
    paperInterfaces[0] = eclairage;
    paperInterfaces[1] = radio;
    paperInterfaces[2] = patient;

  for(PaperInterface pi : paperInterfaces)
	pi.init();


}




void draw(){


    GLGraphicsOffScreen g;


    for(PaperInterface pi : paperInterfaces)
	pi.update();

    for(PaperInterface pi : paperInterfaces)
	pi.draw();



    projector.drawScreens();    

    // Still problems with the distosions
    image(projector.distort(true), 0, 0, frameSizeX, frameSizeY);

}
boolean capture = false;
boolean project = true;
boolean changed = false;
boolean test = false;

int dx = 0;
int dy = 0;

void keyPressed(){
    if(key == 't')
	capture = !capture;

    if(key == 'x'){
	dx +=  1;
    }
    if(key == 'X'){
	dx -=  1;
    }

    if(key == 'y'){
	dy +=  1;
    }
    if(key == 'Y'){
	dy -=  1;
    }

    println("Dx et dy " + dx + " " + dy);

    if(key == 't')
	test = !test;


    println("Test " + test);

    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);
}

