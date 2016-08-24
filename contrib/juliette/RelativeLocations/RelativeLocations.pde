// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.papart.tracking.*;

Papart papart;
void settings() {
  fullScreen(P3D);
}

void setup() {
  papart = Papart.seeThrough(this);
  papart.loadSketches() ;
  papart.startTracking() ;
  rectMode(CENTER);
}

void draw() {

  PVector v1 = obj1.getScreenPos();
  PVector v2 = obj2.getScreenPos();

  stroke(255);
  strokeWeight(3);

  line(v1.x, v1.y, 
    v2.x, v2.y);
}