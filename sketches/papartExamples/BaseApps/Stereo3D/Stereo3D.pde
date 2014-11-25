
public class LapinApp extends PaperTouchScreen{
    
    PVector userPos = new PVector(0, 500, 500);
    PShape rabbit;
    PShape sphereM;
    PGraphicsOpenGL pg;
    Button b;
    PVector lightPos = new PVector(0, 0, 600);

    void setup(){

	drawingSize(297, 210);
	markerBoard(sketchPath + "/data/markers/lapin.cfg", 297, 210);

	rabbit = loadShape("models/bun_zipper.obj");
	sphereM = loadShape("models/sphere/sphere1.obj");

	b = new Button("pos", 8, 15, 30, 20);
	buttons.add(b);
    }

    
    
    public void draw(){
        screen.setDrawing(true);
	updateLightPos();

	pg = screen.getGraphics();
	pg.beginDraw();

	pg.background(0);
	pg.noStroke();
	pg.fill(140);

	pg.pushMatrix();
	pg.translate(-drawingSize.y /2f, -drawingSize.y /2f);
	drawTouch(pg, 3);
	b.drawSelf(pg);
	pg.popMatrix();


	// pg.rect(-60, -60, 120, 120);

	pg.stroke(200);
	for(float y = -drawingSize.y ; y < drawingSize.y; y+= 20){
	    pg.line(-drawingSize.x /2, y, drawingSize.x /2, y);
	}
	for(float x = -drawingSize.x; x < drawingSize.x; x+= 20){
	    pg.line(x, -drawingSize.y /2, x, drawingSize.y /2);
	}

	if(b.isTouched()){
	    pg.background(100);
	    // Set the starting screen location
	    screen.resetPos();
	}

	PGL pgl;
	pgl = pg.beginPGL();

	/////////////// Analglyph ////////////////////
	screen.initDraw(userPos, 20, 5000, true, false, false);

	pg.pointLight(255, 255, 255, 
		      lightPos.x, lightPos.y, lightPos.z + 50);

	pgl.colorMask(true,false,false,true); 
	drawScene();
	
	// Clear the depth buffer, and set the other mask
	pgl.clear( GL.GL_DEPTH_BUFFER_BIT); 
	
	screen.initDraw(userPos, 20, 5000, true, true, false);

	pgl.colorMask(false, true, true, true); 
	drawScene();
	
	// Put the color mask back to normal. 
	pgl.colorMask(true, true, true, true);
	// /////////////// Analglyph -- END ////////////////////
	
	pg.endPGL();

	pg.endDraw();
    }

    protected void drawScene(){

	// The first call is ignored ?!...
	pg.rect(0, 0, 10, 10);

	pg.pushMatrix();
	pg.translate(0, 0, 10);
	pg.fill(200, 200, 200);
	pg.rect(30, 30, 20, 20);
	pg.popMatrix();

	pg.pushMatrix();
	pg.translate(lightPos.x, lightPos.y, lightPos.z);
	pg.scale(5);
	pg.shape(sphereM);
	pg.popMatrix();


	pg.pushMatrix();
	pg.translate(0, 0, 7);
	pg.rotateX(HALF_PI);
	pg.rotateY(PI);
	
	pg.scale(400);	
	pg.shape(rabbit);
	pg.popMatrix();

    }

    void updateLightPos(){
	for (Touch t : touchList) {
	    if(!t.is3D) 
		continue;

	    PVector v = t.p;
	    
	    if(v.x > 0.1 && v.x < 0.9 &&
	       v.y > 0.1 && v.y < 0.9){
		lightPos.x = v.x * drawingSize.x - drawingSize.x / 2f;
		lightPos.y = v.y * drawingSize.y - drawingSize.y / 2f;
		lightPos.z = v.z;
	    }
	    
	    break;
	}
    }

}
