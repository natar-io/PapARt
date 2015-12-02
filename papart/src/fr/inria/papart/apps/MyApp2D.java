package fr.inria.papart.apps;

import fr.inria.papart.procam.Papart;
import fr.inria.papart.procam.PaperScreen;
import fr.inria.papart.procam.PaperTouchScreen;

public class MyApp2D extends PaperScreen {

    @Override
    public void settings() {
        setDrawingSize(297, 210);
        loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
    }

    @Override
    public void setup() {

    }

    @Override
    public void drawOnPaper() {
        background(100, 0, 0);
        fill(200, 100, 20);
        rect(10, 10, 100, 30);
    }
}
