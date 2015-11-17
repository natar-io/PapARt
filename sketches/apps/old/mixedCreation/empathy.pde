
// http://www.openprocessing.org/sketch/1182

class Empathy extends SubSketch{


    int n = 5000; // number of cells
    float bd = 37; // base line length
    float sp = 0.004; // rotation speed step
    float sl = .97; // slow down rate
    
    Cell[] all = new Cell[n];
    
    class Cell{
	int x, y;
	float s = 0; // spin velocity
	float c = 0; // current angle
	Cell(int x, int y) {
	    this.x=x;
	    this.y=y;
	}
	void sense(){
	    if(pmouseX != 0 || pmouseY != 0)
		s += sp * det(x, y, pmouseX, pmouseY, mouseX, mouseY) / (dist(x, y, mouseX, mouseY) + 1);
	    s *= sl;
	    c += s;
	    float d = bd * s + .001;
	    line(x, y, x + d * cos(c), y + d * sin(c));
	}
    }
    
    float det(int x1, int y1, int x2, int y2, int x3, int y3) {
	return (float) ((x2-x1)*(y3-y1) - (x3-x1)*(y2-y1));
    }


    public void setup(PApplet parent){
	
	// size in Pixels 
	this.width = 300;
	this.height = 300;

	// display size in millimeters 
	this.displayWidth = 100;
	this.displayHeight = 100;

	// initalize the sketch
	this.initSketch(parent);

	//////////// Std processing code ////////


	for(int i = 0; i < n; i++){
	    float a = i + random(0, PI / 9);
	    float r = ((i / (float) n) * (width / 2) * (((n-i) / (float) n) * 3.3)) + random(-3,3) + 3;
	    all[i] = new Cell(int(r*cos(a)) + (width/2), int(r*sin(a)) + (height/2));
	}

    }

    public void draw(){
	
	updateInputTouch(drawingTouch, drawingBoardSize);

	clear(0, 0);
	//	g.background(0);

	noFill();
	stroke(255, 200);
	for(int i = 0; i < n; i++)
	    all[i].sense();
    }

}
