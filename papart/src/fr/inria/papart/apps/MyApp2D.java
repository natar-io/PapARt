package fr.inria.papart.apps;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;

public class MyApp2D extends PaperScreen {

    public MyApp2D(){
        super();
    }
    
    protected void setup() {
        setDrawingSize(297, 210);
//        loadMarkerBoard(Papart.markerFolder + "big.cfg", 297, 210);
        loadMarkerBoard(Papart.markerFolder + "dlink.png", 140,140);
    }

    public void draw() {
//        this.getLocation().print();
        beginDraw2D();
        background(100, 0, 0);
        fill(200, 100, 20);
        rect(10, 10, 100, 30);
        endDraw();
    }
}
