// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import processing.video.*;

Papart papart;

public void setup() {

    papart = Papart.projection(this);


    PMatrix3D extr = papart.loadCalibration("camProjExtrinsics.xml");
     papart.getProjectorDisplay().setExtrinsics(extr);
    
    papart.loadSketches();
    papart.startTracking();

    
}

void draw() {
}



