import fr.inria.papart.multitouch.*;

public class MyApp  extends PaperTouchScreen {

    void settings(){
	setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
    }

    void setup() {
    }

    void drawOnPaper() {
	setLocation(mouseX, mouseY,0 );

	background(19, 30, 30);

	colorMode(HSB, 20, 100, 100);

	for(Touch touch : touchList){
	    fill(touch.id, 100, 100);
	    ellipse(touch.position.x, touch.position.y, 5, 5);
	}

	fill(200, 100, 20);
	rect(10, 10, 100, 30);
    }
}
