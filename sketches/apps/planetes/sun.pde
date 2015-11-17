PVector sunLocation = new PVector();
PMatrix3D sunLoc = new PMatrix3D();

PaperScreen sun;

// Public is very important :] 
public class Sun  extends PaperTouchScreen {

    PShape sunModel;
    PShader texlightShader;

    void setup(){
	setDrawingSize( (int) boardSize.x, (int)boardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/sun/sun.cfg",
			boardSize.x, boardSize.y);

	sunModel = loadShape("sphere2/sun.obj");
	texlightShader = loadShader("shaders/texlightfrag.glsl", "shaders/texlightvert.glsl");
	sun = this;
    }
 

    // Todo check if this is useful 
    public void resetPos(){
	screen.resetPos();
    }

    public void draw(){

	beginDraw3D();
	
	ambient(255);
	
	sunLocation = this.getLocationVector();
	sunLoc = this.getLocation();
	
	translate(160, 60, 0);
	rotateZ( (float) millis() / 50000f);
	
	// 	diamètre Soleil : 1 392 684 km
	scale(planetScale * 1392684 / 40f);
	scale(0.5f);
	sunModel.draw(getGraphics());
	
	endDraw();
    }

}
