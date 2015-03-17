import fr.inria.papart.exceptions.BoardNotDetectedException;

public class MyApp  extends PaperScreen {

    PShape rocketShape;
    
    void setup(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
	rocketShape = loadShape("rocket.obj");
    }

    void draw(){
	beginDraw3D();
	pushMatrix();
	scale(0.5f);
	rotateX(HALF_PI);
	rotateY((float) millis() / 1000f) ;
	shape(rocketShape);
	popMatrix();
	
	translate(100, 10, 0);
	box(50);
	
	endDraw();
    }

}
    
