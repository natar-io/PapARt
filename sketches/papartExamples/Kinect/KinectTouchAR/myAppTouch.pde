public class MyApp  extends PaperTouchScreen {

  
    void setup(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/big.cfg", 297, 210);
    }

    void draw(){
	beginDraw2D();
	background(100, 80);
	rect(20, 20, 50, 20);
	fill(0, 255, 20);
	ellipse(40, 40, 10, 10);
	drawTouch();
	endDraw();
    }

}
