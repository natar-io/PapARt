import fr.inria.papart.procam.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.kinect.*;
import fr.inria.papart.multitouchKinect.*;

// Loading javaCV and javaCPP
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
//import com.googlecode.javacv.processing.*;

import javax.media.opengl.GL;
import processing.opengl.*;

import codeanticode.glgraphics.*;
import codeanticode.gsvideo.*;

MarkerBoard markerBoard;
PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
Camera cameraTracking, cameraCapture;
Projector projector;
Camera camera;
Screen screen;
float screenResolution = 3;
GLGraphicsOffScreen screenGraphics;


PVector viewSize = new PVector(600, 600);

// TrackedView boardView; -- legacy 
TrackedView viewRef, viewA1, viewA2, viewA3, viewRes;
AnimationImage animRef, anim1, anim2, anim3;


PImage projection1 = null;


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

PVector captureSize = new PVector(75, 75);

PVector resultPos = new PVector(20 + captureSize.x + 20, 20);
PVector debugPos = new PVector(resultPos.x + 20 + captureSize.x, 20);

PVector a1Pos =  new PVector(20, 20 + captureSize.y + 30);
PVector a2Pos =  new PVector(a1Pos.x + captureSize.x + 20, a1Pos.y);
PVector a3Pos =  new PVector(a2Pos.x + captureSize.x + 20, a1Pos.y);
PVector refPos = new PVector(20, 20);

PImage composition1, composition2, composition3;
IplImage compositionIpl1, compositionIpl2, compositionIpl3;

void setup(){

    size(frameSizeX + 400, frameSizeY, GLConstants.GLGRAPHICS); 

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

    String markerBoardFile = sketchPath + "/data/markers/a3/small/A3-small1.cfg";
    markerBoard = new MarkerBoard(markerBoardFile, "board1", (int) boardSize.x, (int)boardSize.y); 
    MarkerBoard[] boards = new MarkerBoard[1];
    boards[0] = markerBoard;



    //////////////////////////////////////////////
    ////////////// Tracking camera ///////////////
    //////////////////////////////////////////////

    calibrationFile = sketchPath +"/data/calibration/calibration-p1.yaml";
    calibrationARToolKitFile = sketchPath + "/data/calibration/calib1-art.dat";
    Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, cameraX, cameraY);
    cameraTracking = new Camera(this, cameraNo, cameraX, cameraY, calibrationFile, cameraType);
    cameraTracking.initMarkerDetection(this, calibrationARToolKitFile, boards);
    // The camera view is handled in another thread;
    cameraTracking.setThread();
    // The boards will be detected at every frame.
    cameraTracking.setAutoUpdate(true);


    //////////////////////////////////////////////
    ///////////// Capture camera /////////////////
    //////////////////////////////////////////////

    calibrationFile = sketchPath + "/data/calibration/calibration-1600-2.yaml";
    calibrationARToolKitFile = sketchPath + "/data/calibration/calib2-art.dat";
    Camera.convertARParams(this, calibrationFile, calibrationARToolKitFile, camera2X, camera2Y);
    cameraCapture = new Camera(this, camera2No , camera2X, camera2Y, calibrationFile, camera2Type);
    cameraCapture.initMarkerDetection(this, calibrationARToolKitFile, boards);
    // The camera view is handled in another thread;
    cameraCapture.setThread(true);
    cameraCapture.setAutoUpdate(true);


    //////// Set the tracked view camera /////// 
    //    boardView = new TrackedView(markerBoard, cameraCapture, capturePos, captureSize, camera2X, camera2Y);


    // Legacy 
    // boardView = new TrackedView(markerBoard, cameraCapture, capturePos, captureSize, (int) 600, (int) 600);
    // cameraCapture.addTrackedView(boardView);

    viewRef = new TrackedView(markerBoard, cameraCapture, 
			      refPos,
			      captureSize, 
			      (int) viewSize.x, (int) viewSize.y);
    cameraCapture.addTrackedView(viewRef);

    viewA1 = new TrackedView(markerBoard, cameraCapture, 
			     a1Pos,
			     captureSize, 
			     (int) viewSize.x, (int) viewSize.y);
    cameraCapture.addTrackedView(viewA1);

    viewA2 = new TrackedView(markerBoard, cameraCapture, 
			     a2Pos,
			     captureSize, 
			     (int) viewSize.x, (int) viewSize.y);
    cameraCapture.addTrackedView(viewA2);

    viewA3 = new TrackedView(markerBoard, cameraCapture, 
			     a3Pos,
			     captureSize, 
			     (int) viewSize.x, (int) viewSize.y);
    cameraCapture.addTrackedView(viewA3);

    animRef = new AnimationImage(viewRef, "ref");
    anim1 = new AnimationImage(viewA1, "a1");
    anim2 = new AnimationImage(viewA2, "a2");
    anim3 = new AnimationImage(viewA3, "a3");

    composition1 = createImage((int) viewSize.x, (int) viewSize.y, RGB);
    compositionIpl1 = null;
    composition2 = createImage((int) viewSize.x, (int) viewSize.y, RGB);
    compositionIpl2 = null;
    composition3 = createImage((int) viewSize.x, (int) viewSize.y, RGB);
    compositionIpl3 = null;

    //////////////////////////////////////////////
    ////////////// Virtual Screen ////////////////
    //////////////////////////////////////////////

    screen = new Screen(this, boardSize, screenResolution);   
    screen.setAutoUpdatePos(cameraTracking, markerBoard);
    projector.addScreen(screen);


    ///////////////////////////////////////////////
    ///////////// Kinect init /////////////////////
    ///////////////////////////////////////////////

    initKinect();
    kinectCalibFile = "data/calibration/KinectScreenCalibration.txt";
    touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, false, 3, 5);
 
    ///////////////////////////////////////////////
    ///////////////// Init subsketches ////////////
    ///////////////////////////////////////////////

    //    initEmpathy();


    screenGraphics = screen.getGraphics();
}

// IplImage out = null;
// GLTexture tex = null;

boolean displayAugmentation = false;

void draw(){

    ///////// Screen position for rendering ////////////
    screen.updatePos();

    //////// Touch input ////////////////
    screen.computeScreenPosTransform();
    TouchElement te = touchInput.projectTouchToScreen(screen, projector, 
						      true, true); 
    
    /////// Image input //////////////

    animRef.updateImage(this, cameraCapture);
    anim1.updateImage(this, cameraCapture);
    anim2.updateImage(this, cameraCapture);
    anim3.updateImage(this, cameraCapture);

    // out = cameraCapture.getView(viewRef);
    // if(out != null){
    // 	if(tex == null)
    // 	    tex = Utils.createTextureFrom(this, out);

    // 	Utils.updateTexture(out, tex);
    // 	//	image(tex, 0, 0, (int) (cameraX * camRatio) , (int) (cameraY * camRatio));
    // 	//	image(tex, 100, 0, 600, 600);
    // }


    //////// Rendering /////////////

    // Draw inside the virtual screen.
    screenGraphics.beginDraw();
    screenGraphics.clear(0);
    screenGraphics.scale(screenResolution);
    //  screenGraphics.camera();

    screenGraphics.strokeWeight(3);
    screenGraphics.stroke(255);

    screenGraphics.noFill();
    //    screenGraphics.fill(180, 0, 200);
    //  screenGraphics.ellipse(100, 100, 20, 30);
    
    screenGraphics.rectMode(CENTER);

    
    if(project){

	PVector capturePos = refPos;
	screenGraphics.rect(capturePos.x + captureSize.x / 2 , 
			    capturePos.y + captureSize.y / 2 , 
			    captureSize.x + 5, 
			    captureSize.y + 5);

	capturePos = a1Pos;
	screenGraphics.rect(capturePos.x + captureSize.x / 2 , 
			    capturePos.y + captureSize.y / 2 , 
			    captureSize.x + 5, 
			    captureSize.y + 5);
	capturePos = a2Pos;
	screenGraphics.rect(capturePos.x + captureSize.x / 2 , 
			    capturePos.y + captureSize.y / 2 , 
			    captureSize.x + 5, 
			    captureSize.y + 5);

	capturePos = a3Pos;
	screenGraphics.rect(capturePos.x + captureSize.x / 2 , 
			    capturePos.y + captureSize.y / 2 , 
			    captureSize.x + 5, 
			    captureSize.y + 5);


	capturePos = resultPos;
	screenGraphics.rect(capturePos.x + captureSize.x / 2 , 
			    capturePos.y + captureSize.y / 2 , 
			    captureSize.x + 5, 
			    captureSize.y + 5);

	capturePos = debugPos;
	screenGraphics.rect(capturePos.x + captureSize.x / 2 , 
			    capturePos.y + captureSize.y / 2 , 
			    captureSize.x + 5, 
			    captureSize.y + 5);


	screenGraphics.imageMode(CENTER);

	/////////////////////// Reference projection //////////////

	if(animRef.hasImage()){
	    DrawUtils.drawImageGL(screenGraphics, animRef.getImage(), 
				  (int) (a1Pos.x + captureSize.x / 2) , 
				  (int) (a1Pos.y + captureSize.y / 2) , 
				  (int) (captureSize.x), 
				  (int) (captureSize.y));
	    DrawUtils.drawImageGL(screenGraphics, animRef.getImage(), 
				  (int) (a2Pos.x + captureSize.x / 2) , 
				  (int) (a2Pos.y + captureSize.y / 2) , 
				  (int) (captureSize.x), 
				  (int) (captureSize.y));
	    DrawUtils.drawImageGL(screenGraphics, animRef.getImage(), 
				  (int) (a3Pos.x + captureSize.x / 2) , 
				  (int) (a3Pos.y + captureSize.y / 2) , 
				  (int) (captureSize.x), 
				  (int) (captureSize.y));
	}



	///////////////////// Image composition ////////////////
    
	if(animRef.hasImage() && anim1.hasImage() && anim2.hasImage() && anim3.hasImage() ){

	    if(changed){

		IplImage ref = animRef.getIplImage();
		IplImage a1 = anim1.getIplImage();
		IplImage a2 = anim2.getIplImage();
		IplImage a3 = anim3.getIplImage();
		
		if(compositionIpl1 == null)
		    compositionIpl1 = a1.clone();
		if(compositionIpl2 == null)
		    compositionIpl2 = a2.clone();
		if(compositionIpl3 == null)
		    compositionIpl3 = a3.clone();
		
		cvAdd(ref, a1, compositionIpl1, null);
		cvAdd(ref, a2, compositionIpl2, null);
		cvAdd(ref, a3, compositionIpl3, null);
		
		cvNot(a1, a1);

		Utils.IplImageToPImage(compositionIpl1, false, composition1);
		Utils.IplImageToPImage(compositionIpl2, false, composition2);
		Utils.IplImageToPImage(compositionIpl3, false, composition3);
		changed = false;
	    }
	    
	    // 500 ms Implementation...
	    int t = millis() % 1500;

	    if(t < 500){
	    DrawUtils.drawImageGL(screenGraphics, composition1,
				  (int) (resultPos.x + captureSize.x / 2) , 
	    			  (int) (resultPos.y + captureSize.y / 2) , 
	    			  (int) (captureSize.x), 
	    			  (int) (captureSize.y));
	    } else {
		if(t < 1000){
	    DrawUtils.drawImageGL(screenGraphics, composition2,
				  (int) (resultPos.x + captureSize.x / 2) , 
	    			  (int) (resultPos.y + captureSize.y / 2) , 
	    			  (int) (captureSize.x), 
	    			  (int) (captureSize.y));

		} else {
	    DrawUtils.drawImageGL(screenGraphics, composition3,
				  (int) (resultPos.x + captureSize.x / 2) , 
	    			  (int) (resultPos.y + captureSize.y / 2) , 
	    			  (int) (captureSize.x), 
	    			  (int) (captureSize.y));

		}
	    }

	    // DrawUtils.drawImageGL(screenGraphics, anim1.getImage(), 
	    // 			  (int) (resultPos.x + captureSize.x / 2) , 
	    // 			  (int) (resultPos.y + captureSize.y / 2) , 
	    // 			  (int) (captureSize.x), 
	    // 			  (int) (captureSize.y));

	}


    }

    ///////////////////////////////////// Empathy //////////////
    // screenGraphics.pushMatrix();
    // screenGraphics.scale( 1f / 300f * 20);  // 2cm empathy
    // for(int i = 0; i < n; i++)
    // 	all[i].sense(screenGraphics);
    // screenGraphics.popMatrix();
    ///////////////////////////////////// -- Empathy //////////////
	
    // if(projection1 != null && displayAugmentation){
    // 	screenGraphics.imageMode(CENTER);
	
    // 	DrawUtils.drawImageGL(screenGraphics, projection1, 
    // 			      (int) (capturePos.x + captureSize.x / 2) , 
    // 			      (int) (capturePos.y + captureSize.y / 2) , 
    // 			      (int) (captureSize.x), 
    // 			      (int) (captureSize.y));

    // 	// screenGraphics.image(projection1, capturePos.x + captureSize.x / 2 , 
    // 	// 		     capturePos.y + captureSize.y / 2 , 
    // 	// 		     captureSize.x, 
    // 	// 		     captureSize.y);
    // }

    // //////////// Display the touch ///////////////////
    // if(te.position2D != null){
    // 	for(PVector p : te.position2D){
    // 	    screenGraphics.ellipseMode(CENTER);
    // 	    screenGraphics.ellipse(p.x * screen.getSize().x,
    // 				   p.y * screen.getSize().y, 15, 15);
    // 	}
    // }

    // if(tex != null)
    // 	screenGraphics.image(tex, 0, 0, 100, 100);
    screenGraphics.endDraw();
    projector.drawScreens();

    // Still problems with the distosions
    image(projector.distort(true), 0, 0, frameSizeX, frameSizeY);


    // if(animRef.hasImage()){
    // 	//	image(animRef.getImage(), frameSizeX, 0, 400, 400); 
    // 	if(animRef.hasBackground()){
    // 	    image(animRef.computeDiff(), frameSizeX, 0, 400, 400); 
    // 	}
    // }


}


boolean capture = false;
boolean project = true;
boolean changed = false;

void keyPressed(){
    if(key == 't')
	capture = !capture;

    // println("Capture : " + capture );
    // cameraCapture.setAutoUpdate(capture);

    if(key == 'b') {	    
	println("Capturing background");
	animRef.captureBackground();
	anim1.captureBackground();
	anim2.captureBackground();
	anim3.captureBackground();
    }


    if(key == 's') {
	animRef.saveImage();
	anim1.saveImage();
	anim2.saveImage();
	anim3.saveImage();
    }

    if(key == 'c'){

	println("Capturing diff");
	animRef.computeDiff();
	anim1.computeDiff();
	anim2.computeDiff();
	anim3.computeDiff();
	changed = true;
    }

    if(key == 'p'){
	project = !project;
    }


    if(key == 'd'){
	displayAugmentation = !displayAugmentation;
    }

    if(key == 'l'){
	projection1 = loadImage("projection1.png");
    }

    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);
}



// //////////////////////// From empathy ///////////////////////
// ///////// /http://www.openprocessing.org/sketch/1182  ///////
// /////////////////////////////////////////////////////////////

// /**
// <p>Don't move too fast &mdash; you might scare it. Click to forgive and forget.</p>
// */
 
// int n = 5000; // number of cells
// float bd = 37; // base line length
// float sp = 0.004; // rotation speed step
// float sl = .97; // slow down rate
 
// Cell[] all = new Cell[n];
 
// class Cell{
//   int x, y;
//   float s = 0; // spin velocity
//   float c = 0; // current angle
//   Cell(int x, int y) {
//     this.x=x;
//     this.y=y;
//   }
//   void sense(GLGraphicsOffScreen g) {
//     if(pmouseX != 0 || pmouseY != 0)
//       s += sp * det(x, y, pmouseX, pmouseY , mouseX, mouseY) / (dist(x, y, mouseX, mouseY) + 1);
//     s *= sl;
//     c += s;
//     float d = bd * s + .001;
//     g.line(x, y, x + d * cos(c), y + d * sin(c));
//   }
// }

// float det(int x1, int y1, int x2, int y2, int x3, int y3) {
//     return (float) ((x2-x1)*(y3-y1) - (x3-x1)*(y2-y1));
// }


// PVector empathySize = new PVector(300, 300);
// void initEmpathy(){
//   for(int i = 0; i < n; i++){
//     float a = i + random(0, PI / 9);
//     float r = ((i / (float) n) * (width / 2) * (((n-i) / (float) n) * 3.3)) + random(-3,3) + 3;
//     all[i] = new Cell(int(r*cos(a)) + (width/2), int(r*sin(a)) + (height/2));
//   }
// }

// void mousePressed() {
//   for(int i=0;i<n;i++)
//     all[i].c = 0;
// }
 
