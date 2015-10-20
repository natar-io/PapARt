public class MyAppDessin  extends PaperScreen {

    PImage dessin;

    void settings(){
	setDrawingSize(297, 210);
	setResolution(3);
	loadMarkerBoard(sketchPath() + "/data/A3-small1.cfg", 297, 210);
    }

    void setup() {
	dessin = loadImage("dessin1.png");
    }

    void drawOnPaper() {
	image(dessin, 0, 0, drawingSize.x, drawingSize.y);
    }
}
