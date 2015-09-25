import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.scanner.*;

// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

int sc = 2;

int displayTime = 200;
int captureTime = 180;
int delay = 70;

int decodeValue = 120;


PImage imageOut;
GrayCode grayCode;
PImage[] grayCodesCaptures;

Camera cameraTracking;
ProjectorDisplay projector;

int frameSizeX = 1280;
int frameSizeY = 800;
int framePosX = 0;
int framePosY = 200;
int cameraX; 
int cameraY;


public void setup(){
    size(frameSizeX, frameSizeY, OPENGL);
    
    Papart papart = new Papart(this);
    papart.initProjectorCamera("1", Camera.Type.OPENCV);
    
    cameraTracking = papart.getCameraTracking();
    cameraX = cameraTracking.width();
    cameraY = cameraTracking.height();

    projector = (ProjectorDisplay) papart.getDisplay();
    projector.manualMode();
    
    grayCode = new GrayCode(this, frameSizeX, frameSizeY, sc);
    nbCodes = grayCode.nbCodes();

    grayCodesCaptures = new PImage[nbCodes];
    imageOut = createImage(cameraX, cameraY, RGB);
 
    frameRate(100);
}




PImage cameraImage;


private boolean getCameraImage(){
    cameraImage = cameraTracking.getPImage();
    return cameraImage != null;
}


void draw(){
    
    if(getCameraImage()){
	image(cameraImage, 0, 0, cameraX, cameraY);
    }

    if(checkIsStarted() == false)
	return;

    // wtf 
    if(currentTime() < 0){
	grayCode.display((PGraphicsOpenGL) this.g, 0);
	return;
    }

    if(getCameraImage()){
	updateCodes();

	if(allCodesCaptured()){

	    if(grayCode.isDecoded()){
		drawDecoded();
	    } else { 
		// grayCode.decodeRef(50);
		 grayCode.decodeAbs(decodeValue);
	    }

	    // Draw each captured image bone by one.
	    image(grayCodesCaptures[code], 640, 0, 400, 300);

	} else {

	    tryCaptureImage();

	    // Display the gray code to capture
	    // if((int) random(2) == 0)
	    // 	image(grayCodes[code], 0, 0, frameSizeX, frameSizeY);
	    // else
	    grayCode.display((PGraphicsOpenGL) this.g, code);

	}
    }
}  



void drawDecoded(){
    int[] decodedX = grayCode.decodedX();
    int[] decodedY = grayCode.decodedY();
    boolean[] mask = grayCode.mask(); 
    
    background(0);
    colorMode(RGB, frameSizeX, frameSizeY, 255);
    for(int y = 0 ; y < cameraY; y+= 1) {
	for(int x = 0; x < cameraX; x+= 1) {
	    int offset = x + y* cameraX;
	    if(!mask[offset]){
		stroke(255, 255, 255);
		stroke((int) random(255));
		point(x,y);
		continue;
	    }
	    stroke(decodedX[offset], decodedY[offset], 100);
	    point(x,y);
	}
    }
}

void tryCaptureImage(){
    if(captureOK()){

    	setNextCaptureTime();

    	// Create an image
    	PImage im = cameraTracking.getPImageCopy();
	
    	if(grayCodesCaptures[codeProjected] == null){		
    	    grayCode.addCapture(im, codeProjected);
    	    grayCodesCaptures[codeProjected] = im;
    	    nbCaptured++;
    	    println("Captured code: " + code + ", total: " + nbCaptured);
    	}
    }
}



boolean test = false;

void keyPressed() {
  
  if(key == 't')
    test = !test;

  checkStart();

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);

}
