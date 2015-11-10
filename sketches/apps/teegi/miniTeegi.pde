
public class MiniTeegi extends PaperScreen {


  ColorDetection meditationDetection, visionDetection, rawDetection;
  ColorDetection currentModeDetection;

  int colorOnPaperY = 90;
  PFont myFont;

  void setup() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath + "/data/mini-teegi.cfg", 297, 210);

    myFont = loadFont("AccanthisADFStd-Bold-48.vlw");

    initModes();
    initModeDetection();
  }

  void initModes() {
    Mode.add("vision");
    Mode.add("relax");
    Mode.add("raw");
    Mode.set("raw");
  }


    void initModeDetection() {
	meditationDetection=  new ColorDetection(this);
	meditationDetection.setPosition(new PVector(25, colorOnPaperY));
	
	visionDetection=  new ColorDetection(this);
	visionDetection.setPosition(new PVector(61, colorOnPaperY));
	
	rawDetection =  new ColorDetection(this);
	rawDetection.setPosition(new PVector(100, colorOnPaperY));

	currentModeDetection = new ColorDetection(this);
	currentModeDetection.setPosition(new PVector(180, 70));
      
      try{
	  meditationDetection.initialize(); 
	  visionDetection.initialize(); 
	  rawDetection.initialize(); 
	  currentModeDetection.initialize(); 
      }catch(Exception e){
	  println("e");
	  e.printStackTrace();
      }
  }

  void draw() {

    if (noCameraMode) {
      setLocation(26, 0, 0 );
    }
    beginDraw2D();

    resetShader();
    background(0);


    colorMode(RGB, 255);

    drawDetection(meditationDetection);
    drawDetection(visionDetection);
    drawDetection(rawDetection);
    drawDetection(currentModeDetection);

    int meditationColor = meditationDetection.getColor();
    int visionColor = visionDetection.getColor();
    int rawColor = rawDetection.getColor();
    int currentColor = currentModeDetection.getColor();


    if(!noCameraMode){
	if (sameColors(meditationColor, currentColor)) {
	    Mode.set("relax");
	}
	
	if (sameColors(visionColor, currentColor)) {
	    Mode.set("vision");
	}
	
	if (sameColors(rawColor, currentColor)) {
	    Mode.set("raw");
	}
    }

    fill(255);
    textFont(myFont, 25);

    translate(10, 180, 0);
    text(Mode.getCurrentName(), 20, 20);


    endDraw();
  }


  void drawDetection(ColorDetection colorDetection) {

    colorDetection.computeColor();

    fill(100);

    PVector pos = colorDetection.getPosition();
    pushMatrix();

    translate(pos.x, drawingSize.y - pos.y);

    ellipse(0, 0, 10, 10);

    //	colorDetection.drawCaptureZone();

    translate(0, 40);
    fill(colorDetection.getColor());
    rect(0, 0, 10, 10);


    popMatrix();
  }

  float epsilon = 20;

  boolean sameColors(int c1, int c2) {
    return  abs(red(c1) - red(c2)) < epsilon  
      && abs(green(c1) - green(c2)) < epsilon  
      && abs(blue(c1) - blue(c2)) < epsilon;
  }
}
