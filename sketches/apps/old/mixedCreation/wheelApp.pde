PImage wheelImg = null;

class CarWheel extends SubSketch{

    public void setup(PApplet parent){

	// display size in millimeters 
	this.displayWidth = 30;
	this.displayHeight = 30;

	// size in Pixels 
	this.width = (int) (displayWidth * screenResolution);
	this.height = (int) (displayHeight * screenResolution);

	// initalize the sketch
	this.initSketch(parent);

	//////////// Std processing code ////////
	if(wheelImg == null)
	    wheelImg = loadImage(sketchPath + "/wheel.png");

    }

    public void draw(){
	clear(0, 0);
	scale(screenResolution);
	translate(displayWidth / 2, displayHeight /2, 0);
	rotate((float)millis() / 500f);
	translate(-displayWidth / 2, -displayHeight /2, 0);
	image(wheelImg, 0, 0, (int) displayWidth, (int) displayHeight);
    }

}
