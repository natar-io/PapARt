// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import processing.video.*;

Papart papart;

public void settings(){
    fullScreen(P3D);
}

public void setup() {
    papart = Papart.projection(this);
    
    computeCameraProjectorTransfo();
    
    PMatrix3D extr = papart.loadCalibration(Papart.cameraProjExtrinsics);
    papart.getProjectorDisplay().setExtrinsics(extr);
    
    papart.loadSketches();
    papart.startTracking();
}

void computeCameraProjectorTransfo(){
    PMatrix3D camPaper = papart.loadCalibration("cameraPaper.xml");
    PMatrix3D projPaper = papart.loadCalibration("projectorPaper.xml");

    camPaper.print();
    projPaper.print();

    projPaper.invert();
    projPaper.preApply(camPaper);
    projPaper.print();
    projPaper.invert();
    papart.saveCalibration(Papart.cameraProjExtrinsics, projPaper);
}


void draw() {
}



