package fr.inria.papart.apps;

import fr.inria.papart.procam.PaperScreen;

public class MyApp2D extends PaperScreen {

    public MyApp2D(){
        super();
    }
    
    protected void setup() {
        setDrawingSize(297, 210);
        loadMarkerBoard(parent.sketchPath() + "/data/markers/big.cfg", 297, 210);
    }

    public void draw() {
        beginDraw2D();
        background(100, 0, 0);
        fill(200, 100, 20);
        rect(10, 10, 100, 30);
        endDraw();
    }
}
