import fr.inria.papart.drawingapp.DrawUtils;

import fr.inria.skatolo.*;
// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import processing.video.*;

Papart papart;
ProjectorDisplay projector;

public void setup() {

    papart = Papart.projection(this);
    papart.loadTouchInput();

    papart.loadSketches();
    papart.startTracking();

    projector = papart.getProjectorDisplay();
    // projector.manualMode();
}


void settings(){
    fullScreen(P3D);
}

void draw() {
//    background(0);
    // projector.clear();
    // projector.drawScreensOver();
    // System.out.println("main Draw");
    // stroke(100);
    // DrawUtils.drawImage((PGraphicsOpenGL)this.g,
    //                     projector.render(),
    //                     0, 0, width, height);
}
