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

  void setup() {
    setDrawingSize(50, 50);
    app2 = this;
  }

  void draw() {

    setLocation(150, 80, 0);
    beginDraw2D();
    background(30, 255, 0);
    endDraw();
  }
}

