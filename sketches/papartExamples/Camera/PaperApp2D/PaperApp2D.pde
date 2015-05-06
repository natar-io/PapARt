// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import processing.video.*;

boolean useProjector = false;

// Undecorated frame 
public void init() {
    if(useProjector){
	frame.removeNotify(); 
	frame.setUndecorated(true); 
	frame.addNotify(); 
    }
    super.init();
}

Papart papart;

public void setup() {

    if(useProjector)
	papart = Papart.projection(this);
    else 
	papart = Papart.seeThrough(this);

    if (frame != null) {
	frame.setResizable(true);
    }
    
    papart.loadSketches();
    papart.startTracking();
    frameRate(200);
}

void draw() {
}


void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if (key == ' ')
    	papart.defaultFrameLocation();
}
