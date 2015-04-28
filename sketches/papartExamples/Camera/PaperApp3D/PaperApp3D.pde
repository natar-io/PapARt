// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;


void setup(){
    size(640, 480, OPENGL);
    Papart papart = new Papart(this);

    papart.initCamera("1", Camera.Type.OPENCV);
    papart.loadSketches() ;
    papart.startTracking() ;
}

void draw(){
}

