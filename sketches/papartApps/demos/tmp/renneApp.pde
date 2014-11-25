
public class RenneApp extends PaperTouchScreen{


    PVector userPos = new PVector(0, 500, 500);
    PShape renne;
    PGraphicsOpenGL pg;
    PShader normalShader, lightShader;
    ArcBall arcball;

    float rx = 0, ry = 0;

    public RenneApp(PApplet parent,
            MarkerBoard board,
            PVector size,
            float resolution,
            Camera cam,
            Projector proj,
            TouchInput touchinput) {

        super(parent, board, size,
                resolution,
	      cam, proj, touchinput);

	renne = loadShape("models/renne/renne_90K.obj");

	normalShader = loadShader("data/shaders/processing/normal.frag", "data/shaders/processing/normal.vert"); 
	lightShader = loadShader("data/shaders/processing/light.frag", "data/shaders/processing/light.vert"); 

	arcball = new ArcBall(drawingSize.x, drawingSize.y);

    }


    public void resetPos(){
	screen.resetPos();
    }

    public void draw(){
        screen.setDrawing(true);
	// userPos.x = 20 *  (slider.arrayValue()[0] - 50);
	// userPos.y = 20 * (slider.arrayValue()[1] - 50);
	// userPos.z = 20 * (slider2.arrayValue()[0] );



	pg = screen.getGraphics();
	pg.beginDraw();
	
	pg.background(0);
	pg.noStroke();
	pg.fill(140);


	PGL pgl;
	pgl = pg.beginPGL();

	screen.initDraw(userPos, 20, 5000, false, false, false);
	drawScene();
	
	pg.endDraw();
    }

    int touchID = -1;

    protected void drawScene(){


	if(!lightOn){

	    for (Touch t : touchList) {
		if (t.is3D)
		    continue;

		PVector v = t.p;
		TouchPoint tp = t.touchPoint;
	    
		boolean isNew = false;
	    
		// Get the id of the first...
		int id = tp.getID();
		if(touchID < 0){
		    touchID = id;
		    isNew = true;
		} else {
		
		    // An ID is already set
		    
		    // if it is the same point... 
		    if (touchID == id){
			isNew = false;
		    } else { // if it is a new point
			isNew = true;
			touchID = id;
		    }
		}

		pg.noStroke();
		pg.fill(40, 200, 70);
		if(isNew){
		    pg.ellipse(v.x * drawingSize.x - drawingSize.x / 2 , v.y * drawingSize.y - drawingSize.y/2, 20, 20);
		} else {
		    pg.ellipse(v.x * drawingSize.x - drawingSize.x / 2 , v.y * drawingSize.y - drawingSize.y/2, 5, 5);
		    
		}
		
		
		if(v.x > 0.2 && v.x < 0.8 &&
		   v.y > 0.2 && v.y < 0.8){
		    arcball.customEvent(v.x * drawingSize.x,
					v.y * drawingSize.y,
					isNew);
		    break;
		}
		
	    } // For touch 
	} // if !lighton

	pg.pushMatrix();
	pg.translate(lightPos.x, lightPos.y, lightPos.z);
	pg.stroke(180);
	pg.fill(200);
	pg.sphere(lightSize);
	pg.popMatrix();
	
	// if(test)
	//     pg.shader(normalShader);
	// else 
	//     pg.shader(lightShader);
	

	updateLightPos();
	pg.ambient(50);
	pg.pointLight(255, 255, 255, 
		      lightPos.x, lightPos.y, lightPos.z);
	
	
	// TODO: check this...
	//    pg.rect(0, 0, 0, 0);
	
	pg.pushMatrix();
	{

	    arcball.update(pg);

	    pg.rotateX(HALF_PI);
	    pg.scale(0.8f);
	    pg.shape(renne);
	}
	pg.popMatrix();

    }



    PVector lightPos = new PVector(0, 0, 500);
    float lightSize = 10;

    void updateLightPos(){

	if(lightOn){
	    for (Touch t : touchList) {
		if (!t.is3D)
		    continue;

		PVector v = t.p;
		
		// for (PVector v : touch.position3D) {
		
		if(v.x > 0.1 && v.x < 0.9 &&
		   v.y > 0.1 && v.y < 0.9){
		    
		    lightPos.x = v.x * drawingSize.x - drawingSize.x / 2f;
		    lightPos.y = v.y * drawingSize.y - drawingSize.y / 2f;
		    lightPos.z = v.z;
		    break;
		}
	    }
	} 
    }

}
