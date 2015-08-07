
class APP extends SubSketch{


    public void setup(PApplet parent){
	

	// display size in millimeters 
	this.displayWidth = 40;
	this.displayHeight = 40;

	// size in Pixels 
	this.width = (int) (displayWidth * screenResolution);
	this.height = (int) (displayHeight * screenResolution);

	// initalize the sketch
	this.initSketch(parent);

	//////////// Std processing code ////////

    }

    public void draw(){

	
	updateInputTouch(drawingTouch, drawingBoardSize);

	textFont(font, 8);
	clear(0, 0);
	//	background(0, 200,0 );

	fill(255);
	stroke(255, 200);
	
	ellipse(mouseX, mouseY, 10, 10);
	
	scale(screenResolution);

	float mx = mouseX / screenResolution;
	float my = mouseY / screenResolution;


	PVector rectSize = new PVector(27, 27);
	PVector rectPos = PVector.mult(rectSize, 0.5);

	if( mx >= rectPos.x && mx <= rectPos.x + rectSize.x && 
	    my >= rectPos.y && my <= rectPos.y + rectSize.y)
	    {
		text("Snow", 0, 45, 100, 100);
		//		background(100, 100, 100);
		useSnow = true;
	    }
	else {
	    useSnow = false;
	    text("No Snow", 0, 45, 100, 100);
	    rect(rectPos.x, rectPos.y, rectSize.x / 3, rectSize.y / 3);
	}


	//	if(mouseX > 100)

	//	translate(width /2, height/ 2, 0);

    }

}
