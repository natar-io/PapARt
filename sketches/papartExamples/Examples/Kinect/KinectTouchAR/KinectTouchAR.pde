import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

void setup(){
    int cameraX = 640;
    int cameraY = 480;

    size(cameraX, cameraY, OPENGL);

    Papart papart = new Papart(this);
    papart.initKinectCamera(1);
    papart.loadTouchInputKinectOnly(2, 5);

    papart.loadSketches() ;
    papart.startTracking();
}


void draw(){
}

