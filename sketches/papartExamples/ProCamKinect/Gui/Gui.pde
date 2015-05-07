// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

Papart papart;

public void setup() {

    papart = Papart.projection(this);
    papart.loadTouchInput(2, 5);
    
    papart.loadSketches();
    papart.startTracking();
}

void draw() {
}


