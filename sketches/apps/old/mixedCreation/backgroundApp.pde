// http://www.randybyers.net/?p=1154

PImage backgroundImg = null;

class BackgroundApp extends SubSketch{

    float imgDisplayWidth;
    float imgDisplayHeight;
    float pos1 = 0;
    float pos2 = 0;

    public void setup(PApplet parent){

	// display size in millimeters 
	this.displayWidth = 60;
	this.displayHeight = 80;

	// size in Pixels 
	this.width = (int) (displayWidth * screenResolution);
	this.height = (int) (displayHeight * screenResolution);

	// initalize the sketch
	this.initSketch(parent);

	//////////// Std processing code ////////
	if(backgroundImg == null)
	    backgroundImg = loadImage(sketchPath + "/background.jpg");

	imgDisplayHeight = this.displayHeight;
	imgDisplayWidth = (float)this.displayHeight *  (float)backgroundImg.width / (float)backgroundImg.height;

	pos1 = 0; 
	pos2 = -imgDisplayWidth;
    }

    float scrollSpeed = 0.2;

    public void draw(){
	clear(0, 0);
	scale(screenResolution);

	pos1 += scrollSpeed;
	pos2 += scrollSpeed;

	// out of the screen
	if(pos1 >= displayWidth)
	    pos1 -= imgDisplayWidth * 2;

	if(pos2 >= displayWidth)
	    pos2 -= imgDisplayWidth * 2;

	image(backgroundImg, pos1, 0, (int) imgDisplayWidth +4, (int) imgDisplayHeight);
	image(backgroundImg, pos2, 0, (int) imgDisplayWidth +4, (int) imgDisplayHeight);
    }

}
