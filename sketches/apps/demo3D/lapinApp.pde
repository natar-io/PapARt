import fr.inria.papart.multitouch.metaphors.*;
import processing.opengl.PGL;
import processing.opengl.PGraphicsOpenGL;
import com.jogamp.opengl.GL;
import fr.inria.guimodes.Mode;

public class LapinApp extends PaperTouchScreen{

    PVector userPos = new PVector(0, 200, 500);
    PShape rabbit;
    PShape sphereM;
    PGraphicsOpenGL pg;
    Button b;
    PVector lightPos = new PVector(0, 0, 600);
    TwoFingersRST rst;

    void settings(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath() + "/data/markers/A3-small1.cfg", 297, 210);
    }

    void setup(){
	rabbit = loadShape("models/bun_zipper.obj");
	sphereM = loadShape("models/sphere/sphere1.obj");

	b = new Button("pos", 30, 20, 30, 20);
	buttons.add(b);

	rst = new TwoFingersRST(new PVector());
        rst.setFiltering(false);
	rst.setDisabledYZone(50);

        setDrawingFilter(3);
    }

    public void resetPos(){
	screen.resetPos();
    }

    void drawOnPaper(){
        userPos = new PVector(0, 400, 900);

        //   screen.setDrawing(true);
        updateTouch();

	if(Mode.is("light")){
	    updateLightPos();
	}

	if(Mode.is("lock")){

	}

	if(Mode.is("rotate")){

            float d = markerBoard.lastMovementDistance(cameraTracking);
            println("dist " + d);
            if(d < 3){
                try{
                    rst.update(touchList, millis());
                }catch(Exception e){ e.printStackTrace(); }
            }
        }

	background(0);
	noStroke();
	fill(140);



	if(test){
	    resetPos();
	    println("Reset Pos");
	    test = false;
	}

	drawMono();
        // drawAnaglyph();

// 	drawTouch(3);

	screen.endDrawPerspective();

    }

    void drawMono(){
	pushMatrix();
	screen.initDraw(cameraTracking, userPos, 20, 5000, false, false, true);
	pointLight(255, 255, 255,
		      -lightPos.x, -lightPos.y, -lightPos.z);

	drawScene();
	popMatrix();
    }

    void drawAnaglyph(){
	/////////////// Analglyph ////////////////////
	pushMatrix();
	screen.initDraw(cameraTracking, userPos, 20, 5000, true, false, false);

	pointLight(255, 255, 255,
		   -lightPos.x, -lightPos.y, lightPos.z);

	// pushMatrix();
	// translate(-lightPos.x, -lightPos.y, lightPos.z);
	// pointLight(255, 255, 255, 0, 0, 0);
	// popMatrix();


	PGL pgl = getGraphics().pgl;
	pgl.colorMask(true,false,false,true);

	drawScene();

	// Clear the depth buffer, and set the other mask
	pgl.clear( GL.GL_DEPTH_BUFFER_BIT);

	screen.initDraw(cameraTracking, userPos, 20, 5000, true, true, false);

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

        drawTouchPoints();
        drawRabbit();
    }

    private void drawTouchPoints(){
        TouchList touchList2D = touchList.get2DTouchs();
        fill(255);
        noStroke();

        pushMatrix();
	translate(-drawingSize.x /2,
		  -drawingSize.y /2,
		  0);

        for(Touch touch : touchList2D){
            ellipse(touch.position.x, touch.position.y, 10, 10);
        }


        rect(100, 100, 100, 100);

        popMatrix();
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

        scale(resolution);
        rst.applyTransformationTo(this);
        scale(1f / resolution);

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
