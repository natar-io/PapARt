public class Earth  extends PaperTouchScreen {

    PShape earthModel;
    PShader texlightShader;

    void setup(){
	setDrawingSize( (int) boardSize.x, (int)boardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/earth/earth.cfg",
			boardSize.x, boardSize.y);
	earthModel = loadShape("sphere2/earth.obj");
	texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
    }

    void draw(){
	beginDraw3D();
	 
	ambientLight(50, 50, 50);
	pushMatrix();

	// EARTH location
	goTo(sun);
	for(int i = -2; i <= 4; i++){
	    pointLight(130, 130, 130, 0, 0, i * 10);
	}
	popMatrix();

	translate(160, 60, 0);

	rotateX(HALF_PI);
	rotateY( (float) millis() / 20000f);

	// diamètre de la terre : 12 742 kilomètres
	scale(planetScale * 12742f);
	earthModel.draw(getGraphics());
	    
	endDraw();
    }
}
