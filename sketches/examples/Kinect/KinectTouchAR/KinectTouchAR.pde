import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

void settings(){
    size(640, 480, P3D);
}

void setup(){
    Papart papart = new Papart(this);
    papart.initKinectCamera(1);
    papart.loadTouchInputKinectOnly();

    papart.loadSketches() ;
    papart.startTracking();
}


void draw(){
}