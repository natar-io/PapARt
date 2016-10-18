PaperScreen obj2;
PVector origin2;
public class Object2 extends PaperScreen {

  public void settings() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath() + "/data/secondObject.svg", 297, 210);
    setDrawOnPaper();
    origin2 = new PVector(100, 100);
    obj2 = this;
  }

  public void setup() {
  }

  public void drawOnPaper() {
    background(150, 100);

    fill(0, 255, 0);
    rect(origin2.x, origin2.y, 50, 50);
  }
}