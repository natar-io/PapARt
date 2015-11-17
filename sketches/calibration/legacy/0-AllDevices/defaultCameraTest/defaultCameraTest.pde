// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

Papart papart;

public void settings(){
    size(200, 200, P3D);
}

public void setup() {

    papart = Papart.seeThrough(this);
    
    papart.loadSketches();
    papart.startTracking();

}

void draw() {
}
