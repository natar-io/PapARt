MyApp app;

public class MyApp extends PaperScreen {

  void setup() {
    setDrawingSize(100, 100);

    app = this;
  }

  void draw() {

    setLocation(0, 0, 0);
    beginDraw2D();
    background(30, 30, 255);
    endDraw();
  }
}

MyApp2 app2;

public class MyApp2 extends PaperScreen {

  int w = 50;
  int h = 50;
  PMatrix3D transform = new PMatrix3D();
  void setup() {
    setDrawingSize(w, h);
    app2 = this;
  }

  void draw() {


    transform.reset();
    // Go to a certain location 
   
    transform.translate(150, 80, 0);
    
    // and change the orientation of the drawing.
    transform.translate(w/2, h/2);
    transform.rotateZ(PI);
    transform.translate(- w/2, -h/2);

    setLocation(transform);

    beginDraw3D();
    // background(30, 255, 0);
    fill(50);
    rect(0,0, w, h);
    stroke(255);
    line(0, 0, 10, 10);

    endDraw();
  }
}

