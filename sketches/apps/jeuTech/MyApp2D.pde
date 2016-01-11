public class MyAppDessin  extends PaperScreen {

    PImage dessin;

    void settings(){
	setDrawingSize(297, 210);
	setResolution(3);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
    }

    void setup() {
	dessin = loadImage("dessin1.png");
        setLocation(61.4, 44.4, 0);
    }

    void drawOnPaper() {
	image(dessin, 0, 0, drawingSize.x, drawingSize.y);
    }
}
