import fr.inria.papart.multitouch.metaphors.*;

import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import com.jogamp.opengl.GL;

public class LapinApp extends PaperTouchScreen{
    
    PVector userPos = new PVector(0, 200, 500);
    PShape rabbit;
    PShape sphereM;
    PGraphicsOpenGL pg;
    Button b;
    PVector lightPos = new PVector(0, 0, 600);
    TwoFingersRST3D rst;

    void setup(){

	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath() + "/data/markers/A3-small1.cfg", 297, 210);

	rabbit = loadShape("models/bun_zipper.obj");
	sphereM = loadShape("models/sphere/sphere1.obj");

	b = new Button("pos", 30, 20, 30, 20);
	buttons.add(b);

	rst = new TwoFingersRST3D(new PVector());
	rst.setDisabledYZone(50);

    }
    
    public void resetPos(){
	screen.resetPos();
    }

    void draw(){

	userPos = new PVector(0, 400, 900);


	//   screen.setDrawing(true);
	 updateTouch();
	
	if(isLightMode()){
	    updateLightPos();
	}

	if(isLockMode()){

	}

	//	if(isRotateMode()){
	//rst.update(touchList, millis());	    
	//	}


	beginDraw3DProjected();
	background(0);
	noStroke();
	fill(140);

	//drawTouch(3);
	// drawButtons();
	
	if(test){
	    resetPos();
	    println("Reset Pos");
	    test = false;
	}
	    
	// if(b.isTouched()){
	//     resetPos();
	// }

	// drawMono();
        scale(resolution);
        drawAnaglyph();
	/////////////// Analglyph ////////////////////

	screen.endDrawPerspective();
	endDraw();
    }

    void drawMono(){
	pushMatrix();
	screen.initDraw(userPos, 20, 5000, false, false, true);
	pointLight(255, 255, 255, 
		      lightPos.x, lightPos.y, lightPos.z);

	drawScene();
	popMatrix();
    }

    void drawAnaglyph(){
	/////////////// Analglyph ////////////////////
	pushMatrix();
	screen.initDraw(userPos, 20, 5000, true, false, false);

	pointLight(255, 255, 255, 
		      lightPos.x, lightPos.y, lightPos.z);


	PGL pgl = getGraphics().pgl;
	pgl.colorMask(true,false,false,true); 

	drawScene();

	// Clear the depth buffer, and set the other mask
	pgl.clear( GL.GL_DEPTH_BUFFER_BIT); 

	screen.initDraw(userPos, 20, 5000, true, true, false);

	pgl.colorMask(false, true, true, true); 
	drawScene();
	
	// Put the color mask back to normal. 
	pgl.colorMask(true, true, true, true);
	// // /////////////// Analglyph -- END ////////////////////
	popMatrix();
    }

    protected void drawScene(){
	// The first call is ignored ?!...
	rect(0, 0, 10, 10);
	
	drawLight();
	drawLines();
	drawRabbit();
    }

    private void drawLight(){
	pushMatrix();
	translate(lightPos.x, lightPos.y, lightPos.z);
	scale(5);
	shape(sphereM);
	popMatrix();
    }

    private void drawRabbit(){
	pushMatrix();

	//	scale(resolution);
	// rst.applyTransformationTo(this);
	// scale(1f / resolution);
	translate(0, 0, 0);
	rotateX(HALF_PI);
	rotateY(PI);
	scale(400);	
	shape(rabbit);
	popMatrix();
    }

    private void drawLines(){
	stroke(120);
	int lineSpace = 20;
	pushMatrix();
	translate(-drawingSize.x /2, 
		  -drawingSize.y /2, 
		  0);
	for(float y = 0 ; y < drawingSize.y; y+= lineSpace){
	    line(0, y, drawingSize.x, y);
	}
	for(float x = 0; x < drawingSize.x; x+= lineSpace){
	    line(x, 0, x, drawingSize.y);
	}
	popMatrix();
    }


    float estimatedDistanceToTable = 20;
    void updateLightPos(){
	for (Touch t : touchList) {
	    if(!t.is3D) 
		continue;
	    PVector v = t.position;
		lightPos.x = v.x - drawingSize.x / 2f;
		lightPos.y = v.y - drawingSize.y / 2f;
		lightPos.z = v.z - estimatedDistanceToTable;
	}
    }

}
