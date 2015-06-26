// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import processing.video.*;

import processing.app.Base;

boolean useProjector = true;
boolean useCamera = false;

Papart papart;
ProjectorDisplay projector;

public void setup() {

    if(useCamera){
	if(useProjector)
	    papart = Papart.projection(this);
	else 
	    papart = Papart.seeThrough(this);

    }else {
	
	papart = Papart.projection2D(this);

	projector = new ProjectorDisplay(this, Papart.proCamCalib);
        projector.setZNearFar(10, 6000);
        projector.setQuality(1);
        projector.init();

        papart.setDisplay(projector);
	papart.setNoTrackingCamera();

    }

    papart.loadSketches();

    if(useCamera)
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
