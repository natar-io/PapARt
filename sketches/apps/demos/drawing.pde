public class DrawingApp  extends PaperTouchScreen {

    PImage toDraw;

    void setup(){
	setDrawingSize((int) A4BoardSize.x, (int) A4BoardSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/drawing.cfg",
		    (int) A4BoardSize.x, (int) A4BoardSize.y);

	toDraw = loadImage(sketchPath() + "/dessin.png");
    }

    public void draw(){
	beginDraw2D();
	background(0);
	//	tint(255, imageIntens);
	image(toDraw, 0, 0, drawingSize.x, drawingSize.y);
	endDraw();
    }


}
