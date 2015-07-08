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

void draw() {
}


void keyPressed() {

    if(key == 's'){
	app.saveLocationTo("loc.xml");
	println("Position saved");
    }

    if(key == 'l'){
	app.loadLocationFrom("loc.xml");
    }

    // Move again
    if(key == 'm'){
	app.useManualLocation(false);
    }

}
