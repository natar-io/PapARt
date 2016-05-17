import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.procam.Utils;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.scanner.*;

import javax.media.opengl.GL;

// import org.bytedeco.javacpp.opencv_core.IplImage;

// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

PVector detectedPoint = null;

GrayCode grayCode;
Scanner3D scanner;
PImage[] grayCodesCaptures;

Camera cameraTracking;
ProjectorDisplay projector;

int sc = 2;
int halfSc = sc/2;

int displayTime = 250;
int captureTime = 180;
int delay = 70;

int decodeValue = 120;

boolean decoded = false;


int frameSizeX = 1280;
int frameSizeY = 800;
int framePosX = 0;
int framePosY = 200;
int cameraX;
int cameraY;

int[] decodedX, decodedY;
boolean[] myMask;

PImage refImage = null;
PImage cameraImage;


ArrayList<PVector> scannedPoints = new ArrayList<PVector>();
ArrayList<PVector> scannedPointsColors = new ArrayList<PVector>();


public void setup(){
    size(frameSizeX, frameSizeY, OPENGL);

    Papart papart = new Papart(this);
    papart.initProjectorCamera("0", Camera.Type.OPENCV);

    cameraTracking = papart.getCameraTracking();
    cameraX = cameraTracking.width();
    cameraY = cameraTracking.height();

    println("Camera " + cameraX + " " + cameraY);

    projector = (ProjectorDisplay) papart.getDisplay();
    projector.manualMode();

    grayCode = new GrayCode(this, frameSizeX, frameSizeY, sc);
    nbCodes = grayCode.nbCodes();
    grayCodesCaptures = new PImage[nbCodes];

    scanner = new Scanner3D(cameraTracking.getProjectiveDevice(), projector);
    frameRate(200);
}





private boolean getCameraImage(){
    cameraImage = cameraTracking.getPImage();
    return cameraImage != null;
}


void decode(){
    if(!decoded){
	grayCode.decodeAbs(decodeValue);
	
	decodedX = grayCode.decodedX();
	decodedY = grayCode.decodedY();
	myMask = grayCode.mask();
	
	// TODO
	compute3DPos();
	decoded = true;
    }
}

// refimage is for colors !
void checkAndSetRefImage(){
    if(refImage == null){
	PImage ref = cameraTracking.getPImageCopy();
	refImage = ref;
	grayCode.setRefImage(ref);
    }
}


void draw(){

    background(0);

    if(checkIsStarted() == false)
	return;

    if(!getCameraImage()){
	println("No image from camera");
	return;
    }
    	    
    updateCodes();
    

    if(captureOK()){ 
	setNextCaptureTime();

	checkAndSetRefImage();
	
	PImage im =  cameraTracking.getPImageCopy();
	grayCodesCaptures[nbCaptured] = im;
	grayCode.addCapture(im, codeProjected);
	println("Setting " + nbCaptured + " with code " + codeProjected);
	
	nbCaptured++;
	println("Captured code: " + codeProjected + ", total " + nbCaptured);
    }
    
    grayCode.display((PGraphicsOpenGL) this.g, code);

    if(allCodesCaptured())
	decode();
}


int nbScan = 0;



void savePoints() {

    String[] vertices = new String[scannedPoints.size()];
    int k = 0;

    for(int i = 0; i < scannedPoints.size(); i++){

	PVector v = scannedPoints.get(i);
	PVector c = scannedPointsColors.get(i);
	vertices[k++] = ("v " + v.x + " " + v.y + " " + v.z + " " + c.x + " " +c.y + " " +c.z);
    // for(PVector v : scannedPoints){
    // 	vertices[k++] = ("v " + v.x + " " + v.y + " " + v.z );
    // }

    }

    println("Points saved " + scannedPoints.size());

    // Writes the strings to a file, each on a separate line
    saveStrings("scanned" + nbScan + ".obj", vertices);
    scannedPoints.clear();
    scannedPointsColors.clear();
}



// TODO: 
void nextScan(){
    println("Next Scan...");
    grayCode.reset();
    decoded = false;
    nbCaptured = 0;
    resetTime();
}




void keyPressed() {

    checkStart();

    if(key == 'p') 
	savePoints();

    if(key == 'n') 
	nextScan();
    
    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);
}

