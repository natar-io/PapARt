public class MyApp  extends PaperScreen {

  void setup() {
    setDrawingSize(297, 210);
    //    loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
    loadMarkerBoard(sketchPath + "/data/frame5.png",
		    420, 297);

  }

  void draw() {
      //      getLocation().print();
    beginDraw2D();
    background(100, 0, 0);
    fill(200, 100, 20);
    rect(10, 10, 100, 30);
    endDraw();
  }
}
