
import fr.inria.papart.procam.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.kinect.*;
import fr.inria.papart.multitouchKinect.*;
import fr.inria.papart.drawingapp.*;

import javax.media.opengl.GL;
import processing.opengl.*;
import codeanticode.glgraphics.*;
import codeanticode.gsvideo.*;

// Loading javaCV and javaCPP
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
//import com.googlecode.javacv.processing.*;

// The libraries are using Toxiclibs
import toxi.processing.*;
import toxi.geom.*;
import toxi.math.*;
import toxi.geom.mesh.*;

import saito.objloader.*;


MarkerBoard markerBoardDraw, markerBoardInterface;
PVector drawSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector interfaceSize = new PVector(180, 160);  

// To get images from the camera
// TrackedView trackedView;

Projector projector;
Camera camera, camera2;
Screen screenDraw, screenInterface;
GLTexture lastDrawnImage;

//PVector userPos = new PVector( -280.20816,  -477.78003, 1000);
//PVector userPos = new PVector(-140, -400, 800);
PVector userPos = new PVector(0, -600, 500);

PVector nearFar = new PVector(20, 3000);
PVector nearFarCam2 = new PVector(200, 3000);

float screenResolution = 4;
boolean useTexture = false;
boolean useAA = true;
int antiAliasing = 2;

boolean useHorse = false;
boolean useStereo = true;

boolean isDrawing = true;

//// Secondary Camera ////

boolean useSecondCam = false;
ARDisplay camera2Display;


//PVector userPos = new PVector(0, 700, 400);

public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


void setup(){ 

    if(useSecondCam)
   	size(frameSizeX + camera2X, frameSizeY, GLConstants.GLGRAPHICS); 
    else
	size(frameSizeX, frameSizeY, GLConstants.GLGRAPHICS); 


  String calibrationFile = sketchPath + "/data/calibration/calibration-p1.yaml";
  String calibrationCamera2File = sketchPath + "/data/calibration/calibration-p1.yaml";

  String calibrationARToolKitFile = sketchPath + "/data/calibration/calib1-art.txt";
  //  String markerBoardFile = sketchPath + "/data/markers/a3/A3p2.cfg";
  String markerBoardFile = sketchPath + "/data/markers/a3/small/A3-small1.cfg";
  String markerBoardInterfaceFile = sketchPath + "/data/markers/my_markerboardmini2.cfg";

  // TODO: only sometimes ?
  Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, cameraX, cameraY);

  projector = new Projector(this,  calibrationFile,
			    frameSizeX, frameSizeY, 20, 2000);

  markerBoardDraw = new MarkerBoard(markerBoardFile, "Drawing board", (int) drawSize.x, (int) drawSize.y); 
  markerBoardInterface = new MarkerBoard(markerBoardInterfaceFile, "Interface board", (int) interfaceSize.x, (int) interfaceSize.y);

  MarkerBoard[] boards = new MarkerBoard[2];
  boards[0] = markerBoardDraw;
  boards[1] = markerBoardInterface;


  //  camera = new Camera(this, "/dev/video1", cameraX, cameraY, calibrationFile, Camera.GSTREAMER_VIDEO);
  camera = new Camera(this, cameraNo, cameraX, cameraY, calibrationFile, cameraType);
  camera.initMarkerDetection(this, calibrationARToolKitFile, boards);
  camera.setThread();
  camera.setAutoUpdate(true);

  if(useSecondCam){

      String calibrationARToolKitFile2 = sketchPath + "/data/calibration/calib2-art.dat";

      camera2 = new Camera(this, camera2No, camera2X, camera2Y, calibrationCamera2File, camera2Type);
      camera2.initMarkerDetection(this, calibrationARToolKitFile2, boards);
      camera2.setThread();
      camera2.setAutoUpdate(true);

      camera2Display =  new ARDisplay(this,  calibrationCamera2File,
				      camera2X, camera2Y,
				      nearFarCam2.x, nearFarCam2.y, 0);

  }


  screenDraw = new Screen(this, drawSize, screenResolution, useAA, antiAliasing);   
  screenInterface = new Screen(this, interfaceSize, screenResolution);

  // The position of the screen is automatically updated with the camera view of this markerboard
  screenDraw.setAutoUpdatePos(camera, markerBoardDraw);
  screenInterface.setAutoUpdatePos(camera, markerBoardInterface);

  // The projector will display this screen
  projector.addScreen(screenDraw);
  projector.addScreen(screenInterface);


  // Filtering 

  markerBoardDraw.setFiltering(camera, 30, 4);
  markerBoardInterface.setFiltering(camera, 30, 4);

  markerBoardDraw.setDrawingMode(camera, true, 4);
  markerBoardInterface.setDrawingMode(camera, true, 4);


  //////////// Kinect initialization //////////
  initKinect();
 
  kinectCalibFile = "data/calibration/KinectScreenCalibration.txt";
  touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, true, 3, 5);

  initInterface();
  //  initScene3D(this, "data/bordeaux/tile_x59y80.obj");
  initScene3D(this, "useless");

  // For drawing and image saving.
  lastDrawnImage = new GLTexture(this, (int) (drawSize.x *screenResolution),(int) (drawSize.y *screenResolution));

  // more than 60fps (default) framerate, usefull ?
  frameRate(120);
}



boolean test = false;
boolean saveImage = false;

void keyPressed(){
  if(key == 't')
    test = !test;

  if(key == 'd')
    isDrawing = !isDrawing;


  if(key == 's')
    saveImage = true;

  // if(key == 'u')
  //   scenePos.z += 5;
  // if(key == 'U')
  //   scenePos.z -= 5;

  screenDraw.halfEyeDist = 15;

  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}


void stop() {
  super.stop();
}


boolean isPaperUpdating = true;

void draw(){


    screenDraw.updatePos();
    screenDraw.computeScreenPosTransform();

    screenInterface.updatePos();
    screenInterface.computeScreenPosTransform();

    isPaperUpdating = markerBoardDraw.isMoving(camera);


    TouchElement te = touchInput.projectTouchToScreen(screenInterface, projector, true, false); 
    drawPaperInterface(screenInterface, te);

    te = touchInput.projectTouchToScreen(screenDraw, projector, true, true, true, true); 

    if(!isPaperUpdating)
	updateMultiTouch(screenDraw, te);


    if(isDrawing){
	draw3D(screenDraw, te);

	GLGraphicsOffScreen screenGraphics = screenDraw.getGraphics();
	screenGraphics.beginDraw();
	//  screenGraphics.clear(0,0);
	screenGraphics.scale(screenResolution);

	// TODO: CheckCapture functions...
	for(PVector p : te.position2D){

	    screenGraphics.ellipse(p.x * screenDraw.getSize().x,
				   p.y * screenDraw.getSize().y + 20, 2, 2);
	}

	screenGraphics.endDraw();
    }

    // switch(currentInterfaceMode){
    // case MODE_SCENE3D:
    //     draw3D(screenDraw, te);
      
    // case MODE_DESSINER:
    //     // if(isAnaglyph)
    //     //   draw2DImage(drawingLeft? leftImage : rightImage, screenDraw);
    //     // draw2DImage(lastDrawnImage, screenDraw);
    //     break;
      
    // case MODE_NONE : 
    // default:
    //     draw3D(screenDraw, te);
    //     //   drawNoneSelected(screenDraw);
    // }
  
  
    projector.drawScreens();
    image(projector.distort(true), 0, 0, frameSizeX, frameSizeY);


    if(useSecondCam){
      
	GLGraphicsOffScreen displayGraphics = camera2Display.beginDraw();
      
	PMatrix3D transfo = markerBoardDraw.getTransfoMat(camera2);
	PMatrix3D transfo2 = transfo.get();

	transfo2.invert();

	// Goto to the screen position
	displayGraphics.modelview.apply(transfo);


	// if(test){
	userPos.x = -transfo2.m03; 
	userPos.y = transfo2.m13;
	userPos.z = transfo2.m23;

	// }else{
	// lightPos.x = -transfo2.m03; 
	// lightPos.y = transfo2.m13;
	// lightPos.z = transfo2.m23;
	// }

	//      println(userPos);


	//      drawSize = new PVector(297, 210);   //  21 * 29.7 cm



	if(test)
	    displayGraphics.image(screenDraw.getGraphics().getTexture(), 0, 0, (int) drawSize.x, (int)drawSize.y);
	else{

	    GL gl = displayGraphics.beginGL();
	    //      displayGraphics.scale(screenDraw.getScale());
	    displayGraphics.translate(drawSize.x / 2f, drawSize.y / 2f, 0);

	    drawScene(displayGraphics, gl);

	    displayGraphics.translate(lightPos.x, lightPos.y, lightPos.z);
	    displayGraphics.scale(10);
	    sphereModel.render();

	    displayGraphics.endGL();
	}

	// displayGraphics.translate(0, 0, 0);
	// displayGraphics.box(10);
	camera2Display.endDraw();

	image(camera2.getPImage(), frameSizeX, 0, camera2X, camera2Y);
	noLights();
	image(camera2Display.distort(false), frameSizeX, 0, camera2X, camera2Y);
    }

}


