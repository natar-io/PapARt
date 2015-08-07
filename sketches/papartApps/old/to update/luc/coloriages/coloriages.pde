import fr.inria.papart.procam.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.tools.*;
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
//import com.googlecode.javacv.processing.*;

// The libraries are using Toxiclibs
import toxi.processing.*;
import toxi.geom.*;
import toxi.math.*;
import toxi.geom.mesh.*;


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


MarkerBoard markerBoardLeft;

// All boards have the same size.
PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm

// To get images from the camera
// TrackedView trackedView;

Projector projector;
Camera cameraTracking;

PImage anaglyphImage;

// Only one screen ?
Screen screenLeft;
//Screen screenAna;
float screenResolution = 8;


PImage[] images;
int currentImage = 0;
static final int IMAGES_COUNT = 7;

public void setup(){

  size(frameSizeX, frameSizeY, GLConstants.GLGRAPHICS); 
  

  String calibrationCamTrackingProj = sketchPath + "/data/calibration-p4.yaml";
  String calibrationARToolKitFileTracking = sketchPath + "/data/calib1-art.dat";
  //  String markerBoardFileLeft = sketchPath + "/data/my_markerboardmini.cfg";
  String markerBoardFileLeft = sketchPath + "/data/my_markerboarda3v1.cfg";

  // TODO: only sometimes ?
  Camera.convertARParams(this, calibrationCamTrackingProj, calibrationARToolKitFileTracking, cameraX, cameraY);

  projector = new Projector(this, calibrationCamTrackingProj,
			    frameSizeX, frameSizeY, 20, 2000);

  markerBoardLeft =  new MarkerBoard(markerBoardFileLeft, "Left image", 
				     (int) boardSize.x, (int)boardSize.y); 


  MarkerBoard[] boards = new MarkerBoard[1];
  boards[0] = markerBoardLeft;

  // Camera with id 0 .,  640x480 resolution
  cameraTracking = new Camera(this, cameraNo, cameraX, cameraY, calibrationCamTrackingProj, cameraType);
  // camera = new Camera(this, "1", cameraX, cameraY, calibrationFile, Camera.OPENCV_VIDEO);

  cameraTracking.initMarkerDetection(this, calibrationARToolKitFileTracking, boards);

  // The camera view is handled in another thread;
  cameraTracking.setThread();
  // The boards will be detected at every frame.
  cameraTracking.setAutoUpdate(true);

  markerBoardLeft.setDrawingMode(cameraTracking, true, 20);

  // One screen
  screenLeft = new Screen(this, boardSize, screenResolution);   

  // The position of the screen is automatically updated with the camera view of this markerboard
  screenLeft.setAutoUpdatePos(cameraTracking, markerBoardLeft);

  // The projector will display this screen
  projector.addScreen(screenLeft);

  images = new PImage[IMAGES_COUNT];
  
  images[0] = loadImage("chat.gif");
  images[1] = loadImage("cheval.gif");
  images[2] = loadImage("chien.gif");
  images[3] = loadImage("coquillage.gif");
  images[4] = loadImage("escargot.gif");
  images[5] = loadImage("fraise.gif");
  images[6] = loadImage("lapin.gif");
}



void draw(){

  screenLeft.updatePos();

  //  screenLeft.getPos().print();

  drawImage(screenLeft, images[currentImage]);

  projector.drawScreens();

  image(projector.distort(test), 0, 0, frameSizeX, frameSizeY);
  //  image(projector.getGraphics().getTexture(), 0, 0, frameSizeX, frameSizeY);
  println("FrameRate " + frameRate);

}


boolean test = true;
int tintIntens = 200;

void keyPressed() {

  if(key == 't')
    test = !test;

  if(key == 'r')
    tintIntens += 20;

  if(key == 'R')
    tintIntens -= 20;
    
  if (key == 'i') {
    currentImage = (currentImage + 1) % IMAGES_COUNT;
  }

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);

  System.gc();
}


   
   
void drawImage(Screen screen, PImage image){
  GLGraphicsOffScreen screenGraphics = screen.getGraphics();

  // Draw inside the virtual screen.
   screenGraphics.beginDraw();
   screenGraphics.scale(screenResolution);
   screenGraphics.imageMode(CENTER);
   
   screenGraphics.tint(tintIntens);

   DrawUtils.drawImage(screenGraphics, image, (int) (boardSize.x /2f), (int)(boardSize.y /2f),
		       (int) boardSize.x, (int) boardSize.y);

   screenGraphics.endDraw();
}

