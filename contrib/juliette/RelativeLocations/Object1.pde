PaperScreen obj1;
PVector origin1;

public class Object1 extends PaperScreen {

  public void settings() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath() + "/data/firstObject.svg", 297, 210);
    setDrawOnPaper();
    origin1 = new PVector(50, 50);
    obj1 = this;
  }

  public void setup() {
  }

  public void drawOnPaper() {
    background(100, 100);

    fill(255, 0, 0);
    rect(origin1.x, origin1.y, 50, 50);
  }
}