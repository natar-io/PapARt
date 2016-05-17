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

    papart = Papart.projection(this);

    papart.loadSketches();
}

void draw() {
}


void keyPressed() {

    if(key == 'l'){

	app.getScreen().setMainLocation(papart.getTableLocationFromProjector());
	app2.getScreen().setMainLocation(papart.getTableLocationFromProjector());
    }

    // Move again
    if(key == 'm'){
	app.useManualLocation(false);
    }

}
