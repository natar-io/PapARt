public class MyApp  extends PaperScreen {

    public MyApp(){
        super();
    }

    public MyApp(Camera cam, BaseDisplay proj){
        super(cam, proj);
    }

    void settings(){
        setDrawingSize(297, 210);
//     loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
        loadMarkerBoard(Papart.markerFolder + "big-calib.cfg", 297, 210);
//    loadMarkerBoard(Papart.markerFolder + "mega-calib.cfg", 297, 210);
        // loadMarkerBoard(Papart.markerFolder + "rocks.jpg", 140, 200);

    }

    void setup() {
    }

  void draw() {
    beginDraw2D();
    background(100, 0, 0);
    fill(200, 100, 20);
    rect(10, 10, 100, 30);
    endDraw();
  }
}
