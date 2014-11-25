import fr.inria.papart.multitouch.*;

public class MyApp  extends PaperTouchScreen {

  void setup() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
  }

  void draw() {
      setLocation(mouseX, mouseY,0 );
    beginDraw2D();

    background(100, 50, 0);

    for(Touch touch : touchList){
	println("Touch " + touch);
	ellipse(touch.position.x, touch.position.y, 5, 5);
    }

    fill(200, 100, 20);
    rect(10, 10, 100, 30);
    endDraw();
  }
}

