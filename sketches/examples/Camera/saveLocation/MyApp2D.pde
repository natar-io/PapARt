MyApp app;

public class MyApp extends PaperScreen {

    void settings(){
        setDrawingSize(297, 210);
        loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
    }

    void setup() {
        app = this;
    }

    void drawOnPaper() {
        //      getLocation().print();
        background(100, 0, 0);
        fill(200, 100, 20);
        rect(10, 10, 100, 30);
    }

}
