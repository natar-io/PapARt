
import fr.inria.papart.procam.*;
//import fr.inria.papart.tools.*;
import fr.inria.papart.kinect.*;
import fr.inria.papart.multitouchKinect.*;


import javax.media.opengl.GL;
import processing.opengl.*;
import codeanticode.glgraphics.*;
import codeanticode.gsvideo.*;

// Loading javaCV and javaCPP
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


// The libraries are using Toxiclibs
import toxi.processing.*;
import toxi.geom.*;
import toxi.math.*;
import toxi.geom.mesh.*;

import java.nio.*;

// Jtablet library
import cello.tablet.*;


String userName = "Toto";
// EXPE TYPE : 0 au choix
// EXPE TYPE : 1 juste projection
int expeType = 0;


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


MarkerBoard markerBoardDraw, markerBoardInterface;
PVector drawSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector interfaceSize = new PVector(180, 160);  

// To get images from the camera
// TrackedView trackedView;

Projector projector;
ARDisplay cameraDisplay;
TrackedView boardView;

Camera camera, camera2;
Screen screenDraw;

float screenResolution = 2;
boolean useAA = true;
int antiAliasing = 2;
boolean useMultiTouch = true;

boolean useTablet = false;
boolean useSecondCamera = false;

PImage captureImage, projectImg;


public void setup(){

  /////////////////// Tablet init - 1 input ////////////////
  if(useTablet){
      initTabletHandler(this);
  }

  size(frameSizeX, frameSizeY, GLConstants.GLGRAPHICS); 

  noCursor();
  captureImage = loadImage(sketchPath + "/images/projOff.png");
  projectImg = loadImage(sketchPath + "/images/project.png");

  String calibrationFile = sketchPath + "/data/calibration/calibration-p1.yaml";
  String calibrationARToolKitFile = sketchPath + "/data/calibration/calib1-art.dat";

  String calibrationFile2 = sketchPath + "/data/calibration/calibration-1080p.yaml";
  String calibrationARToolKitFile2 = sketchPath + "/data/calibration/calib1-1080p.dat";

  //my_markerboardmini2.cfg
    //  String markerBoardInterfaceFile = sketchPath + "/data/my_markerboardmini3.cfg";
  String markerBoardInterfaceFile = sketchPath + "/data/markers/nouveaux/2285.cfg";
  String markerBoardFile = sketchPath + "/data/markers/a3/small/A3-small1.cfg";


  // TODO: only sometimes ?
  Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, 640, 480);

  projector = new Projector(this,  calibrationFile,
			    frameSizeX, frameSizeY, 200, 2000);

 
  // Camera AR...
  cameraDisplay = new ARDisplay(this, calibrationFile, cameraX, cameraY, 20, 2000, 0);

  markerBoardDraw = new MarkerBoard(markerBoardFile, "Drawing board", (int) drawSize.x, (int) drawSize.y); 
  markerBoardInterface = new MarkerBoard(markerBoardInterfaceFile, "Interface board", (int) interfaceSize.x, (int) interfaceSize.y);

  MarkerBoard[] boards = new MarkerBoard[2];
  boards[0] = markerBoardDraw;
  boards[1] = markerBoardInterface;

  
  camera = new Camera(this, cameraNo, cameraX, cameraY, calibrationFile, cameraType);
  camera.initMarkerDetection(this, calibrationARToolKitFile, boards);
  camera.setThread();
  camera.setAutoUpdate(true);


  if(useSecondCamera){
      camera2 = new Camera(this, camera2No, camera2X, camera2Y, calibrationFile2, camera2Type);
 
      Camera.convertARParams(this, calibrationFile2, calibrationARToolKitFile2, camera2X, camera2Y);

      MarkerBoard[] boards2 = new MarkerBoard[1];
      boards2[0] = markerBoardDraw;

      camera2.initMarkerDetection(this, calibrationARToolKitFile2, boards2);
      camera2.setThread();
      camera2.setAutoUpdate(true);   

      //      camera2.setPhotoCapture();

      boardView = new TrackedView(markerBoardDraw, camera2, camera2X / 2, camera2Y / 2);
  }



  // Create a virtual screen :   210 * 297 mm, 2 pixels per mm. 
  screenDraw = new Screen(this, drawSize, screenResolution, useAA, antiAliasing);   


  // The position of the screen is automatically updated with the camera view of this markerboard
  screenDraw.setAutoUpdatePos(camera, markerBoardDraw);

  // The projector will display this screen
  projector.addScreen(screenDraw);


  // Set filtering for the markerboards. 


  markerBoardDraw.setDrawingMode(camera, true, 10);
  markerBoardInterface.setDrawingMode(camera, true, 10);

  markerBoardDraw.setFiltering(camera, 30, 4);
  markerBoardInterface.setFiltering(camera, 30, 4);


  //////////// Kinect initialization //////////
  if(useMultiTouch){
      initKinect();
      kinectCalibFile = "data/calibration/KinectScreenCalibration.txt";

      if(use3DSelection)
	  touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, true, 2, 4);
      else
	  touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, true, 2, 0);
  }



  ////// INIT ORDER IS IMPORTANT ///////

  ////////////// Modes init ///////////////
  initFreeLinesMode();
  //  initPerspLinesMode();
  initGridMode();
  initCharacterMode();


  ///////////// Tablet init - 2 Data ///////////
  if(useTablet)
      initTablet();



  ////////////// NumPad init ///////////////
  initNumPad();


  // TouchPoint.filterFreq = 30f;
  // TouchPoint.filterCut = 1f;
  // TouchPoint.filterBeta = 8.00f;

  setNoActiveKey();
  frameRate(30);
}

GLTexture viewTexture = null;


int MODE_NONE = -1;
int NONE = -1;
int EDIT = 222;

int globalMode = MODE_NONE;

void draw(){

    background(0);
    
    //    trySaveStats();
    
    screenDraw.updatePos();
    screenDraw.computeScreenPosTransform();
    
    TouchElement touchElement = null;
    if(useMultiTouch){
	
	if(use3DSelection){
	    touchElement = touchInput.projectTouchToScreen(screenDraw, projector, 
							   true, true, true, true); 
	}else{
	    touchElement = touchInput.projectTouchToScreen(screenDraw, projector, 
							   true, false, true, false); 
	    
	}
	touchGrid(touchElement);
	touchFreeLines(touchElement);
	//      touchPerspLines(touchElement);
	touchCharacter(touchElement);
    }
    
    if(useTablet){
	updateTablet();
    }
    
    
    GLGraphicsOffScreen projGraphics = projector.getGraphics();
    projGraphics.beginDraw();
    projGraphics.clear(0);
    projGraphics.endDraw();
    
    
    GLGraphicsOffScreen screenGraphics = screenDraw.getGraphics();
    screenGraphics.beginDraw();
    screenGraphics.clear(0,0);
    screenGraphics.scale(screenResolution);
    
    
    ///////////// Draw Multi - Touch /////////////
    
    if(useMultiTouch){
	int k = 0;
	
	// TODO: CheckCapture functions...
	
	for(PVector p : touchElement.position2D){
	    lastTouchMouse = millis();
	    
	    screenGraphics.ellipse(p.x * screenDraw.getSize().x,
				   p.y * screenDraw.getSize().y + yTouchOffset, 2, 2);
	}
	
	
	if(use3DSelection){
	    for(PVector p : touchElement.position3D){
		//	  lastTouchMouse = millis();
		
		if(activeLine == null)  {
		    screenGraphics.ellipse(p.x * screenDraw.getSize().x,
					   p.y * screenDraw.getSize().y + yTouchOffset * 2, 8, 8);
		    
		    
		    if(globalMode == FREE_LINES_MODE){
			
			
			PVector p1 = p.get();
			p1.x *= drawSize.x;
			p1.y =  p1.y * drawSize.y + yTouchOffset * 2;
			for(Line l : lines){
			    
			    for (PVector v : l.snapPoints){
				
				float d = v.dist(p1);
				
				if(d < 100){
				    screenGraphics.ellipse(v.x, v.y, d /2f, d /2f );
				}
				// if(d < 30){
				//     activeLine = l;
				//     l.setActive(v);
				// }
				
			    }
			}
		    }
		}
	    }
	}
	
	
      // If there is no touch, we force the update of the sheet's position
	if(touchElement.position2D.size() == 0)
	    markerBoardDraw.forceUpdate(camera, 200);
	
	
    }
    
    checkCapture();
    
    

    ///////////// Draw Construction lines /////////////
    if(isProjectImage) {
	drawGrid(screenGraphics);
	//      drawPerspLines(screenGraphics);
	drawFreeLines(screenGraphics);
	drawCharacter(screenGraphics);

	if(useTablet){
	    drawTablet(screenGraphics);
	    drawCursor(screenGraphics, 10);
	}

	screenGraphics.endDraw();
    } else{

	screenGraphics.endDraw();
	drawCaptureImage();
    }

    drawNumPadProjector();
  

    // imageMode(CORNER);
    // if(isProjectImage)
    // 	  image(projector.distort(true), screenSizeX, 0, frameSizeX, frameSizeY);
    // imageMode(CENTER);
    //   pushMatrix();
    // 	translate(screenSizeX /2, screenSizeY / 2);
    // 	scale(1, -1);
    // 	IplImage out = camera2.getView(boardView);
    // 	if(out != null){
    // 	    if(viewTexture == null)
    // 		viewTexture = Utils.createTextureFrom(this, out);
    // 	    Utils.updateTexture(out, viewTexture);
    // 	}
    // 	if(viewTexture != null)
    // 	    image(viewTexture, 0, 0, screenSizeX, screenSizeY);
    // 	image(screenGraphics.getTexture(), 0, 0, screenSizeX, screenSizeY);
    // 	popMatrix();

    image(projector.distort(true), 0, 0, frameSizeX, frameSizeY);


}




boolean test = false;
float dx = 0f;
float dy = 0f;
float dz = 0f;


void keyPressed() {

    inputTotal++;

    if(key == 'r'){
	resetStats();
    }

    if(key == 's'){
	saveStats(0);

    }


    lastTouchMouse = millis();
 
  // if(globalMode == PERSPECTIVE_LINES_MODE){
  //     //      keyPerspLines();
  // }
  
  if(globalMode == FREE_LINES_MODE){
      keyFreeLines();
  }

  if(globalMode == GRID_MODE){
      keyGrid();
  }

  if(globalMode == TABLET_MODE){
      keyTablet();
  }

  if(globalMode == CHARACTER_MODE){
      keyCharacter();
  }



  if(globalMode == MODE_NONE){

      if(key == '1')
	  enterMode(FREE_LINES_MODE);

      if(key == '2')
	  enterMode(GRID_MODE);

      // if(key == '4')
      // 	  enterMode(TABLET_MODE);

      if(key == '3'){
	  isProjectImage = !isProjectImage;

	  if(!isProjectImage){
	      numPadSetOneLineText("Proj OFF", 15);
	  } else {
	      numPadSetOneLineText("Proj ON", 15);
	  }
      }

  }



  if(key == '/'){

      // if(globalMode == PERSPECTIVE_LINES_MODE)
      // 	  leavePersLinesMode();

      if(globalMode == FREE_LINES_MODE)
	  leaveFreeLinesMode();

      if(globalMode == GRID_MODE)
	  leaveGridMode();

      if(globalMode == TABLET_MODE)
	  leaveTabletMode();

      if(globalMode == CHARACTER_MODE)
	  leaveCharacterMode();

      resetNumPad();
      numPadSetOneLineText(null);

      globalMode = MODE_NONE;
  }



  // if(key == 'c'){
  //     enterCapture();
  // }

  if(!isProjectImage){
      numPadSetOneLineText("Proj OFF", 15);
  } 


  if(key == 't')
      test = !test;

  if(key == 'a'){
      dx += 1f;
  }

  if(key == 'A'){
      dx -= 1f;
  }

  if(key == 'u'){
     dy += 1f;
  }
  if(key == 'U'){
      dy -= 1f;
  }

  if(key == 'i'){
     dz += 1f;
  }
  if(key == 'I'){
      dz -= 1f;
  }

  //  println("dx : " + dx + " dy : " + dy + " dz : " + dz);

  // println("test ?" + test);

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}



// Stats.... 


int creationPointLine = 0;
int creationStandardLine = 0;

int editPoint = 0;
int editStandard = 0;

int gridActions = 0;
int inputTotal = 0;

void resetStats(){

    println("Stats reset");
  
    creationPointLine = 0;
    creationStandardLine = 0;
    editPoint = 0;
    editStandard = 0;
    gridActions = 0;
    inputTotal = 0;
}

int lastSave = 0;
int timeToSave = 30000;

void trySaveStats(){

    if(millis() - lastSave > timeToSave){

	println("Autosave");
	saveStats(1);

	lastSave = millis();
    }

}

void saveStats(int auto){

    try{
	
	expeType = auto;
	
	PrintWriter writer = new PrintWriter(sketchPath + "/results/" + userName + "-" + expeType +"-" + millis() +  ".txt");
	
	writer.println(userName);
	writer.println(expeType);

	writer.println(creationPointLine);
	writer.println(creationStandardLine);
	writer.println(editPoint);
	writer.println(editStandard);
	writer.println(gridActions);
	writer.println(inputTotal);
	
	writer.flush();  // Writes the remaining data to the file
	writer.close();  // Finishes the file
	
	println("Results saved");
	
    }catch(Exception e){
	println("Impossible to write the file. :" + e);
	
    }

}


void enterMode(int mode){
    resetNumPad();
    globalMode = mode;

    if(mode ==  GRID_MODE)
	enterGridMode();

    if(mode ==  FREE_LINES_MODE)
	enterFreeLinesMode();

    // if(mode ==  PERSPECTIVE_LINES_MODE)
    // 	  enterPersLinesMode();

    if(mode == TABLET_MODE)
	enterTabletMode();

    if(mode ==  CHARACTER_MODE)
	enterCharacterMode();

}


void close(){
    try{
	openKinectGrabber.stop();
    }catch(Exception e){
    }
}


