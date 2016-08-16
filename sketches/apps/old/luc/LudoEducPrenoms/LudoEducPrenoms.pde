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

MarkerBoard markerBoardMain;
MarkerBoard markerBoardInterface;

// All boards have the same size.
PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm

final static int INTERFACE_SIZE_X = 180;
final static int INTERFACE_SIZE_Y = 160;

PVector interfaceSize = new PVector(INTERFACE_SIZE_X, INTERFACE_SIZE_Y);

// To get images from the camera
// TrackedView trackedView;

Projector projector;
Camera cameraTracking;


PImage anaglyphImage;
TrackedView leftView, rightView;
static final int VIEW_RESOLUTION_H = 1200/4;
static final int VIEW_RESOLUTION_V = 800/4;


// Only one screen ?
Screen screenMain;
Screen screenInterface;
//Screen screenAna;
float screenResolution = 2;

int currentFont;
PFont buttonFont;
PFont[] fonts;
final int FONTS_COUNT = 5;

final static int MAIN_TEXT_FONT = VIEW_RESOLUTION_V / 2;
final static int MAIN_TEXT_SIZE = VIEW_RESOLUTION_H * 4;

final static int INTERFACE_TOTAL_H = VIEW_RESOLUTION_H;
final static int INTERFACE_TOTAL_V = VIEW_RESOLUTION_V;
final static int INTERFACE_SEPARATION_H = INTERFACE_TOTAL_H / 20;
final static int INTERFACE_AVAILABLE_H = INTERFACE_TOTAL_H - 2 * INTERFACE_SEPARATION_H;

final static int INTERFACE_MAIN_ELEMENT_SIZE_H = INTERFACE_AVAILABLE_H;
final static int INTERFACE_MAIN_FONT_SIZE = INTERFACE_AVAILABLE_H / 4;
final static int INTERFACE_SECONDARY_ELEMENT_SIZE_H = INTERFACE_AVAILABLE_H / 4;
final static int INTERFACE_SECONDARY_FONT_SIZE = INTERFACE_SECONDARY_ELEMENT_SIZE_H;

final static int INTERFACE_LEFT_ELEMENT_POS_X = INTERFACE_TOTAL_H/2;
final static int INTERFACE_MAIN_ELEMENT_POS_X = INTERFACE_TOTAL_H/6;
final static int INTERFACE_RIGHT_ELEMENT_POS_X = INTERFACE_TOTAL_H/2;

final static int INTERFACE_MAIN_ELEMENT_POS_Y = -INTERFACE_TOTAL_V/2 ;
final static int INTERFACE_TOP_ELEMENT_POS_Y = - INTERFACE_TOTAL_V - INTERFACE_SECONDARY_FONT_SIZE / 2;
final static int INTERFACE_BOTTOM_ELEMENT_POS_Y = 0;


final static PVector INTERFACE_PREVIOUS_ELEMENT_POSITION = new PVector(INTERFACE_LEFT_ELEMENT_POS_X, INTERFACE_TOP_ELEMENT_POS_Y);
final static PVector INTERFACE_MAIN_ELEMENT_POSITION = new PVector(INTERFACE_MAIN_ELEMENT_POS_X, INTERFACE_MAIN_ELEMENT_POS_Y);
final static PVector INTERFACE_NEXT_ELEMENT_POSITION = new PVector(INTERFACE_RIGHT_ELEMENT_POS_X, INTERFACE_BOTTOM_ELEMENT_POS_Y);


Button[] buttons;
final int BUTTONS_COUNT = 4;
ArrayList<InteractiveZone> interfaceZones = new ArrayList<InteractiveZone>();
ArrayList<Drawable> interfaceDrawables = new ArrayList<Drawable>();

final static String[] LETTERS = {
  "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
};
final static String[] NUMBERS = {
  "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
};



final static String[] NAMES = {
  "Amandine","Clémentine","Calissandre","BASILE","basile","Pierre","ESTEBAN","Esteban","Milo","MILO","Timothé"
  //"Luc","Jeremy","Alison","Renaud","Thomas","Martin"
};

final static int NAMES_COUNT = 11;

int currentLetter = 0; 
int currentNumber = 0;
int currentName = 0;

boolean letterMode = true;

Button previousButton, nextButton, currentModeButton, otherModeButton;

public void addInteractiveZone(InteractiveZone z) {
  interfaceZones.add(z);
  interfaceDrawables.add(z);
}

public void init() {
  super.init();

  frame.removeNotify(); 
  frame.setUndecorated(true);
  frame.addNotify();
}

public void setup() {

  DrawUtils.applet = this;

/*
  previousButton = new Button("button.png", (int)(INTERFACE_PREVIOUS_ELEMENT_POSITION.x), (int)(INTERFACE_PREVIOUS_ELEMENT_POSITION.y), INTERFACE_SECONDARY_FONT_SIZE, INTERFACE_SECONDARY_FONT_SIZE);
  currentModeButton = new Button("button.png",(int)INTERFACE_MAIN_ELEMENT_POSITION.x,(int)INTERFACE_MAIN_ELEMENT_POSITION.y,INTERFACE_MAIN_FONT_SIZE,INTERFACE_MAIN_FONT_SIZE);
  nextButton = new Button("button.png",(int)INTERFACE_NEXT_ELEMENT_POSITION.x,(int)INTERFACE_NEXT_ELEMENT_POSITION.y,INTERFACE_SECONDARY_FONT_SIZE,INTERFACE_SECONDARY_FONT_SIZE);
  otherModeButton = new Button("button.png",(int)INTERFACE_MAIN_ELEMENT_POSITION.x,(int)INTERFACE_MAIN_ELEMENT_POSITION.y-200,INTERFACE_MAIN_FONT_SIZE,INTERFACE_MAIN_FONT_SIZE);

  buttons = new Button[BUTTONS_COUNT];
  buttons[0] = previousButton;
  buttons[1] = currentModeButton;
  buttons[2] = nextButton;
  buttons[3] = otherModeButton;
  

  for (Button currentButton : buttons) {
    currentButton.reset();
    addInteractiveZone(currentButton);
  }
  */
 
  fonts = new PFont[FONTS_COUNT];

  fonts[0] = loadFont("URWGothicL-Book-250.vlw");
  fonts[1] = loadFont("URWChanceryL-MediItal-200.vlw");
  fonts[2] = loadFont("URWBookmanL-LighItal-200.vlw");
    fonts[3] = loadFont("Garuda-150.vlw");
    fonts[4] = loadFont("NimbusMonL-Regu-150.vlw");
  

  currentFont = 0;

  size(frameSizeX, frameSizeY, GLConstants.GLGRAPHICS); 

  String calibrationCamTrackingProj = sketchPath + CALIBRATION_FILE;
  String calibrationARToolKitFileTracking = sketchPath + CALIBRATION_ARTOOLKIT_FILE;
  String markerBoardFileLeft = sketchPath + MARKERBOARD_FILE;
  String markerBoardInterfaceFile = sketchPath + MENUBOARD_FILE;

  // TODO: only sometimes ?
  Camera.convertARParams(this, calibrationCamTrackingProj, calibrationARToolKitFileTracking, cameraX, cameraY);

  projector = new Projector(this, calibrationCamTrackingProj, frameSizeX, frameSizeY, 20, 2000);

  markerBoardMain =  new MarkerBoard(markerBoardFileLeft, "Left image", (int) boardSize.x, (int)boardSize.y); 
  markerBoardInterface = new MarkerBoard(markerBoardInterfaceFile, "Interface board", (int) interfaceSize.x, (int) interfaceSize.y);

  MarkerBoard[] boards = new MarkerBoard[2];
  boards[0] = markerBoardMain;
  boards[1] = markerBoardInterface;

  cameraTracking = new Camera(this, cameraNo, cameraX, cameraY, calibrationCamTrackingProj, cameraType);

  cameraTracking.initMarkerDetection(this, calibrationARToolKitFileTracking, boards);

  cameraTracking.setThread();
  cameraTracking.setAutoUpdate(true);

  markerBoardMain.setDrawingMode(cameraTracking, true, 10);
  markerBoardInterface.setDrawingMode(cameraTracking, true, 4);

  initKinect();

  kinectCalibFile = "data/KinectScreenCalibration.txt";
  touchInput = new TouchInput(this, kinectCalibFile, kinect, openKinectGrabber, true, 2, 5);

  screenMain = new Screen(this, boardSize, screenResolution);
  screenInterface = new Screen(this, interfaceSize, screenResolution);  

  // The position of the screen is automatically updated with the camera view of this markerboard
  screenMain.setAutoUpdatePos(cameraTracking, markerBoardMain);
  screenInterface.setAutoUpdatePos(cameraTracking, markerBoardInterface);

  // The projector will display this screen
  projector.addScreen(screenMain);
  projector.addScreen(screenInterface);


  buttonFont = loadFont("URWGothicL-Book-48.vlw");
  //buttonFont = loadFont("URWGothicL-Book-96.vlw");
  
  Button.setFont(buttonFont);

  smooth();
  
  println("LudoEducatif::setup : previousElement : " + INTERFACE_PREVIOUS_ELEMENT_POSITION);
  println("LudoEducatif::setup : mainElement : " + INTERFACE_MAIN_ELEMENT_POSITION);
  println("LudoEducatif::setup : nextElement : " + INTERFACE_NEXT_ELEMENT_POSITION);
  println("LudoEducatif::setup : interfaceSecondaryFontSize : " + INTERFACE_SECONDARY_FONT_SIZE);
  println("LudoEducatif::setup : main element size h : " + INTERFACE_MAIN_ELEMENT_SIZE_H);
}

Boolean block = false;

public void observeInput(float x, float y) {
  
  if (block == false) {
  //println("x : " + x + " ; y : " + y);
        if (y > 0 && y < (INTERFACE_SIZE_Y/3)) {
             nextName();          
        }
        else if (y > (2*INTERFACE_SIZE_Y/3)) {
            previousName();
        }      
      }
      block = true;
}

public void unSelect() {
  println("LudoEducatif::unSelect");
  for (InteractiveZone z : interfaceZones) {
    z.isSelected = false;
  }
}

void draw() {

  screenMain.updatePos();
  screenMain.computeScreenPosTransform();
  screenInterface.updatePos();
  screenInterface.computeScreenPosTransform();

  drawMain();
  drawInterface();

  projector.drawScreens();

  image(projector.distort(false), 0, 0, frameSizeX, frameSizeY);

  //println("LudoEducatif::draw : Framerate : " + frameRate);
}

void drawText(Screen screen, String txt, int sizeX, int fontSize, PVector position, PFont txtFont) {
  GLGraphicsOffScreen screenGraphics = screen.getGraphics(); 

  screenGraphics.pushMatrix();
  screenGraphics.scale(1, -1, 1);
  screenGraphics.translate(0, -fontSize, 0);
  screenGraphics.fill(255);
  screenGraphics.tint(tintIntens);
  screenGraphics.textFont(txtFont, fontSize);
  screenGraphics.textAlign(CENTER);

  screenGraphics.text(txt, position.x, position.y, sizeX, fontSize);

  screenGraphics.popMatrix();
}

void drawMain() {
  //PVector textPosition = new PVector(100,25);
  PVector textPosition = new PVector(-300,-100);
  String element;

  element = NAMES[currentName];

  screenMain.getGraphics().beginDraw();
  screenMain.getGraphics().background(0);

  drawText(screenMain, element, MAIN_TEXT_SIZE, MAIN_TEXT_FONT, textPosition, fonts[currentFont]);

  screenMain.getGraphics().endDraw();
}

void drawInterface() {
  String element, nextElement, previousElement;
  
    element = NAMES[currentName];
    nextElement = ">";
    previousElement = "<";

  TouchElement te = touchInput.projectTouchToScreen(screenInterface, projector, true, true); 

  if (!te.position2D.isEmpty()) {
    for (PVector v: te.position2D) {
      observeInput(v.x * interfaceSize.x, v.y * interfaceSize.y);
    }
  }
  else {
    block = false;
  }

  screenInterface.getGraphics().beginDraw();
  screenInterface.getGraphics().background(0);

  drawText(screenInterface, element, INTERFACE_MAIN_ELEMENT_SIZE_H,INTERFACE_MAIN_FONT_SIZE, INTERFACE_MAIN_ELEMENT_POSITION,buttonFont);

  if (previousElement.compareTo("") !=  0) {
    drawText(screenInterface, previousElement, INTERFACE_SECONDARY_ELEMENT_SIZE_H,INTERFACE_SECONDARY_FONT_SIZE, INTERFACE_PREVIOUS_ELEMENT_POSITION,buttonFont);
  }
  if (nextElement.compareTo("") !=  0) {
    drawText(screenInterface, nextElement, INTERFACE_SECONDARY_ELEMENT_SIZE_H,INTERFACE_SECONDARY_FONT_SIZE, INTERFACE_NEXT_ELEMENT_POSITION,buttonFont);
  }

  screenInterface.getGraphics().endDraw();
}

boolean test = true;
int tintIntens = 200;

void previousLetter() {
  currentLetter = max(0, (currentLetter - 1)) % 25;
}

void nextLetter(){
  currentLetter = (currentLetter + 1) % 25;
}

void previousNumber() {
  currentNumber = max(0, (currentNumber - 1)) % 10;
}

void nextNumber() {
  currentNumber = (currentNumber + 1) % 10;
}

void previousName() {
  currentName = max(0, (currentName - 1)) % NAMES_COUNT;
}

void nextName() {
  currentName = (currentName + 1) % NAMES_COUNT;
}

void switchMode() {
  letterMode = !letterMode;
}

void keyPressed() {
  println("LudoEducatif::keyPressed");

  switch (key) {
  case 't' :
    test = !test;
    break;
  case 'r' :
    tintIntens += 20;
    break;
  case 'R' :
    tintIntens -= 20; 
    break;
  case 'l' :
    previousLetter();
    break;
  case 'L' :
    nextLetter();
    break;
  case 'n' :
    previousNumber();
    break;
  case 'N' :
    nextNumber();
    break;
  case 'p' :
    previousName();
    break;
  case 'P' :
    nextName();
    break;
   case 'F':
    currentFont = (currentFont + 1) % FONTS_COUNT;
    break;
   case 'f':
   currentFont = max(0, (currentFont - 1)) % FONTS_COUNT;
    break;
  case 'm' :
  case 'M' :
    switchMode();
    break;
  case ' ' :
    frame.setLocation(framePosX, framePosY);
    break;
  }
  //System.gc();
}

