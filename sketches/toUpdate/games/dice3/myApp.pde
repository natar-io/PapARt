
class MyApp  extends PaperTouchScreen {



    public MyApp(PApplet parent, 
		    MarkerBoard board, 
		    PVector size, 
		    float resolution, 
		    Camera cam, 
		    Projector proj, 
		    TouchInput touchinput) {

	super(parent, board, size, 
	      resolution, 
	      cam, proj, touchinput);

    }


    int NB_LINES = 10;
    int SQUARE_SIZE = 20;
    PVector trOffset = new PVector(5, 5);

    public void draw(){

	PGraphicsOpenGL pg = screen.getGraphics();
	pg.beginDraw();
	pg.scale(resolution);

       	pg.background(50);

	pg.colorMode(RGB, 255);

	// pg.stroke(200);
	pg.strokeWeight(2);

	pg.noStroke();
	pg.fill(20, 130, 255);
	pg.rect(0, 0, drawingSize.x, 0);


	float s1 = sin((float) millis() / 800f);
	float s2 = sin(((float) millis() + 200f) / 600f);

	float x = drawingSize.x / 4f +  (s1 / PI) * (drawingSize.x / 4f);
	float y = drawingSize.y / 4f +  (s2/PI)* (drawingSize.y / 4f);

	PVector p1 = new PVector(x, y);

	pg.fill(20, 255, 50);
	pg.ellipse(x, y, 30, 30);

	PVector hot = null;
	PVector cold = null;
	float heatDist = 50;
	float heatSize = 100;

	if (!touch.position2D.isEmpty()) {
	    for (int j = 0; j < touch.position2D.size() ; j++) {

		PVector v  = touch.position2D.get(j);

		pg.fill(255);
		if(PVector.dist(new PVector(v.x * drawingSize.x, 
					    v.y * drawingSize.y), 
				p1) < (50 + 30) / 2)
		    pg.fill(0, 255, 0);

		pg.ellipse(v.x * drawingSize.x, 
			   v.y * drawingSize.y,
				   50, 50);
	    }
	}
	
	pg.endDraw();
    }





}
