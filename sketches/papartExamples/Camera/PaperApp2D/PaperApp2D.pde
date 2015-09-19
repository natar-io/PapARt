// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import processing.video.*;

boolean useProjector = true;
Papart papart;


public void setup() {

    if(useProjector)
     	papart = Papart.projection(this);
    else
	papart = Papart.seeThrough(this);

    papart.loadSketches();
    papart.startTracking();
}


void settings(){
    if(useProjector){
        fullScreen(P3D);
    }
    else {
        size(300, 300, P3D);
    }
}

void draw() {

}
