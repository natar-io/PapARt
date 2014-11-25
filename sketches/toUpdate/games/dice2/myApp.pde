
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

       	pg.background(0);

	pg.colorMode(RGB, 255);

	pg.stroke(200);
	pg.strokeWeight(2);

	pg.translate(trOffset.x, trOffset.y);
	//	pg.fill(100);
	for(int i = 0; i <= NB_LINES  ; i++){
	    pg.line(i * SQUARE_SIZE, 0,
		    i * SQUARE_SIZE, SQUARE_SIZE * NB_LINES);


	    pg.line(0, i * SQUARE_SIZE,
		    SQUARE_SIZE * NB_LINES, i * SQUARE_SIZE);

	    //	    pg.line(0, SQUARE_SIZE * NB_LINES, i, i);	
	}

	if (!touch.position2D.isEmpty()) {
	    for (int j = 0; j < touch.position2D.size() ; j++) {
		PVector v  = touch.position2D.get(j);

		float x = (v.x * drawingSize.x) - trOffset.x;
		float y = (v.y * drawingSize.y) - trOffset.y;

		int squareX = (int) (x / SQUARE_SIZE);
		int squareY = (int) (y / SQUARE_SIZE);
		
		drawSquare(pg, squareX, squareY);
		println(x + " " + y);

	    }
	}
	
	pg.endDraw();
    }



void drawSquare(PGraphicsOpenGL pg, int x, int y){

    pg.noStroke();

    //  À voir !

    pg.colorMode(HSB, NB_LINES, 10 + NB_LINES, 100);

    pg.fill(x , 10 + y , 80);
    // pg.fill();

    pg.rect(x *  SQUARE_SIZE, 
	    y *  SQUARE_SIZE, 
	    SQUARE_SIZE, 
	    SQUARE_SIZE);

}

}
