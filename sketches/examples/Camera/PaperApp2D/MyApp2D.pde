public class MyApp extends PaperScreen {

    void settings(){
        setDrawingSize(297, 210);
        loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
    }

    void setup() {
    }

    void drawOnPaper(){
        background(0, 200, 10);
        fill(0, 100, 0);
        rect(0, 0, drawingSize.x /2, drawingSize.y/2);
        fill(200, 100, 20);
        translate(0, 0, 1);
        rect(10, 10, 100, 30);
    }

}
