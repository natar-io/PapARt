public class MyApp  extends PaperScreen {

  void setup() {
    setDrawingSize(297, 210);
    loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
  }

  void draw() {
    beginDraw3D();
    //   background(100, 0, 0, 100);
    fill(200, 100, 20);
    rect(10, 10, 100, 30);

    // pointLight(200, 200, 255, 0, 0, 500);

    pushMatrix();
    translate(trackerPos.x, trackerPos.y, trackerPos.z);
    applyMatrix(quatMatrix);
    box(20);
    
    translate(0, 0, -135/2);
    box(70, 70, 135);
    popMatrix();

    endDraw();
  }
}

