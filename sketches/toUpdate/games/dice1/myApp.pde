 
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


	PVector hot = null;
	PVector cold = null;
	float heatDist = 50;
	float heatSize = 100;

	if (!touch.position2D.isEmpty()) {
	    for (int j = 0; j < touch.position2D.size() ; j++) {

		PVector v  = touch.position2D.get(j);

		if(j == 0) 
		    pg.stroke(20, 20, 200);

		if(j == 1) 
		    pg.stroke(200, 20, 20);

		if(j < 2){
		    pg.noFill();
		    pg.ellipse(v.x * drawingSize.x, 
			       v.y * drawingSize.y,
			       heatSize, heatSize);
		    
		    if(j == 1){
			hot = new PVector(v.x * drawingSize.x, 
					  v.y * drawingSize.y);
		    } 
		    if(j == 0){
			cold = new PVector(v.x * drawingSize.x, 
					   v.y * drawingSize.y);
		    } 

		} else {
		    pg.noStroke();
		    PVector p =  new PVector(v.x * drawingSize.x, 
					   v.y * drawingSize.y);
		    
		    boolean drawn = false;
		    
		    if(hot != null){
			if(PVector.dist(p, hot) < heatDist){
			    pg.fill(200, 20, 20);
			    pg.ellipse(v.x * drawingSize.x, 
				       v.y * drawingSize.y,
				       50, 50);
			    drawn = true;
			}
		    }


		    if(cold != null){
		
			if(PVector.dist(p, cold) < heatDist){
			    pg.fill(20, 20, 200);
			    pg.ellipse(v.x * drawingSize.x, 
				       v.y * drawingSize.y,
				       50, 50);
			    drawn = true;
			}
		    }

		    if(!drawn){
			pg.fill(255);
			pg.ellipse(v.x * drawingSize.x, 
				   v.y * drawingSize.y,
				   50, 50);
		    }
		}
	    }
	}
	
	pg.endDraw();
    }





}
