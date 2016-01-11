public class MyApp  extends PaperTouchScreen {


    void settings(){
	setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "big-calib.svg", 297, 210);
    }

    void setup(){
    }

    void drawOnPaper(){
	background(100, 80);
	rect(20, 20, 50, 20);
	fill(0, 255, 20);
	ellipse(40, 40, 10, 10);
	drawTouch();
    }

}
