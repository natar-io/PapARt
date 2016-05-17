import fr.inria.papart.procam.*;
import fr.inria.papart.procam.Utils;
import fr.inria.papart.tools.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.scanner.*;

import org.bytedeco.javacpp.opencv_core.IplImage;

// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


BackgroundRemover backgroundRemover;
Scanner3D scanner;

PImage imageOut;
Camera cameraTracking;
Projector projector;

int frameSizeX = 1280;
int frameSizeY = 800;
int framePosX = 0;
int framePosY = 200;
int cameraX = 640;
int cameraY = 480;

public void setup(){
    size(frameSizeX + cameraX, frameSizeY, OPENGL);

    Papart papart = new Papart(this);
    papart.initProjectorCamera(1,  "0", Camera.OPENCV_VIDEO);

    cameraTracking = papart.getCameraTracking();
    projector = (Projector) papart.getDisplay();
    projector.manualMode();
    
    backgroundRemover = new BackgroundRemover(cameraX, cameraY);
    scanner = new Scanner3D(cameraTracking, projector);

    imageOut = createImage(cameraX, cameraY, RGB);

    noCursor();
}

PVector currentPoint = new PVector(-300, -400);
int step = 5;
int nbPointsX = 100;
int nbPointsY = 100;
int lastScan = 0;

ArrayList<PVector> scannedPoints = new ArrayList<PVector>();


void draw(){

    IplImage imageIpl = cameraTracking.getIplImage();

    PVector detectedPoint = null;

    try{
    	if(imageIpl != null){

    	    // Try to remove the background...

    	    // The background is completely set !
    	    if(backgroundRemover.isProjSet()){

    		// Try to find one pixel different. 
    		// Might be null !
    		// detectedPoint = backgroundRemover.findSinglePixel(imageIpl);

		// Same with debug
    		IplImage px = backgroundRemover.applyTo(imageIpl);
		Utils.IplImageToPImage(px, true, diffImg);
		detectedPoint = backgroundRemover.findPos(px);

    	    } else {
    		removeBackground(imageIpl);
    	    }


    	    // Display the video...
    	    if(test) {
    		Utils.IplImageToPImage(imageIpl, true, imageOut); 
    		image(imageOut, frameSizeX, 0, 400, 300);

		if(background != null){
		    image(background, frameSizeX, 300, 400, 300);
		}

		if(projZone != null){
		    image(projZone, frameSizeX + 400, 0 , 400, 300);
		}

		if(diffImg != null){
		    image(diffImg, frameSizeX + 400, 300, 400, 300);
		}

    	    }
    	}
	
    }catch(Exception e){ println("Exception " + e );
    	e.printStackTrace(); 
    }

    if(backgroundRemover.isBackgroundSet() && backgroundRemover.isProjSet() && scan) {

    	displayPoint(currentPoint);
    
    	// if after more delay it is not found, skip it
    	if(lastScan +  delayNext < millis()){
    	    nextPoint();
    	    println("Point skipped");
    	}

    }


    if(detectedPoint != null) {
    	if(!scan || lastScan + delay > millis()){
    	    return;
    	} 

    	PVector pt = scanner.compute3DPoint(currentPoint, detectedPoint);
    	if(pt != null) {
    	    scannedPoints.add(pt);
    	    println("added Point " + pt);
    	    nextPoint();
    	}

    }

}


PImage background = null;
PImage projZone = null;
PImage diffImg = null;

void removeBackground(IplImage imageIpl){

    // at first black projected
    if(backgroundRemover.isBackgroundSet()){
	background(255);
    } else {
	background(0);
    }


    if (captureKeyPressed){
	captureKeyPressed = false;	    
	
	// Nothing is set, we set the background
	if(!backgroundRemover.isBackgroundSet()){
	    backgroundRemover.setBackground(imageIpl);
	    println("Background set");

	    background = createImage(cameraX, cameraY, RGB);
	    Utils.IplImageToPImage(backgroundRemover.getBackground(), false, background);
	}
	else { // else set the proj zone
	    backgroundRemover.setProjZone(imageIpl);
	    println("projZone set");

	    projZone = createImage(cameraX, cameraY, RGB);
	    Utils.IplImageToPImage(backgroundRemover.getProjZone(), true, projZone);

	    diffImg = createImage(cameraX, cameraY, RGB);
	}
    }
}


int delay = 600;  // ms 
int delaySuper = 600;  // ms 
int delayNext = delaySuper + delay * 2;  // ms 



void displayPoint(PVector point) {
    int ptSize = 5;
    if(lastScan  + delaySuper < millis())
	ptSize = 10;
    
    point.z = scanner.focalDistance();

    PGraphicsOpenGL pg = projector.beginDraw();
    pg.background(0);

    pg.pushMatrix();
    pg.translate(point.x, point.y, point.z);

    pg.fill(255);
    pg.ellipseMode(CENTER);
    pg.ellipse(0, 0, ptSize, ptSize);
    pg.popMatrix();
    projector.endDraw();
    
    PImage p = projector.distort(false);
    noStroke();
    DrawUtils.drawImage((PGraphicsOpenGL) g, p, 0, 0, frameSizeX, frameSizeY);
    
}



void nextPoint(){
    lastScan = millis();
    currentPoint.x += step;
    
    if(currentPoint.x > nbPointsX){
	currentPoint.y += step;
	currentPoint.x = 0;
    }
    
    if(currentPoint.y > nbPointsY){
	// END 
	savePoints();
	exit();
    }
}


void savePoints() {

    String[] vertices = new String[scannedPoints.size()];
    int k = 0;
    for(PVector v : scannedPoints){
	vertices[k++] = ("v " + v.x + " " + v.y + " " + v.z);
    }

    // Writes the strings to a file, each on a separate line
    saveStrings("scanned.obj", vertices);
    println(scannedPoints.size() + " points saved");
}


boolean scan = true;
boolean test = false;
boolean captureKeyPressed = false;

void keyPressed() {

    if(key == 'p') {
	scan = !scan;
	if(scan)
	    lastScan = millis();
    }
    
    if(key == 's') 
	savePoints();
    
    if(key == 't')
	test = !test;
    
    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);
    
}


void keyReleased(){

    if(key == 'c') 
	captureKeyPressed = true;

}
