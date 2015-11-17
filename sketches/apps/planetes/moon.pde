public class Moon  extends PaperTouchScreen {

    PShape moonModel;
    PShader texlightShader;

    void setup(){
	setDrawingSize( (int) boardSize.x, (int)boardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/moon/moon.cfg",
			boardSize.x, boardSize.y);
	moonModel = loadShape("sphere2/moon.obj");
	texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
    }

    public void draw(){
	beginDraw3D();
	ambientLight(50, 50, 50);

	pushMatrix();
  	  goTo(sun);
	  for(int i = -2; i <= 4; i++){
	      pointLight(80, 80, 80, 0, 0, i * 10);
	  }
	popMatrix();

	translate(160, 60, 0);
	rotateZ( (float) millis() / 4000f);

	// Diamètre de la lune 3 474 km
	scale(planetScale * 3447f);
	moonModel.draw(getGraphics());
	endDraw();
    }
}
