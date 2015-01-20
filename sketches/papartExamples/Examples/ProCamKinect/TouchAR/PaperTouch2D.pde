public class MyApp  extends PaperTouchScreen {

  void setup() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
  }

  void draw() {
    beginDraw2D();
    background(200, 200, 200);
    fill(200, 100, 20); 
    rect(10, 10, 100, 30);
    drawTouch(15);
    endDraw();
  }
}
