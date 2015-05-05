// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;




Papart papart;

public void setup() {

    papart = Papart.createDefaultSeeThrough(this);
    
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
