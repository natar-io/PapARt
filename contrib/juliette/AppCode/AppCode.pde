import fr.inria.papart.procam.*; 
import fr.inria.papart.multitouch.*;
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.opencv_core;
import org.reflections.*;
import toxi.geom.*;

Papart papart;

void settings() {

  fullScreen(P3D);
}

void setup() {

  papart = Papart.projection(this);
  papart.loadTouchInput();

  papart.loadSketches();
  papart.startTracking();
}

void draw() {
}