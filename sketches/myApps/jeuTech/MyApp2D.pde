public class MyAppDessin  extends PaperScreen {

    PImage dessin;

    void setup() {
	setDrawingSize(297, 210);
	setResolution(3);
	loadMarkerBoard(sketchPath() + "/data/A3-small1.cfg", 297, 210);

	dessin = loadImage("dessin1.png");
    }

    void draw() {
	beginDraw2D();
	background(100, 0, 0);

	// if(test){
	//     dessin = loadImage("dessin.png");
	// }

	// TODO: null dessin exception -> not
	image(dessin, 0, 0, drawingSize.x, drawingSize.y);
	endDraw();
    }
}
