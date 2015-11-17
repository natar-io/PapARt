import fr.inria.papart.drawingapp.ButtonListener;


class PlacementInterface extends PaperInterface{


    public PlacementInterface(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }

    public void draw(){

	GLGraphicsOffScreen g = screen.getGraphics();
	g.beginDraw();
	g.clear(0, 0);
	g.scale(resolution);
	g.background(0, 255, 0);
	g.endDraw();

    }


}
