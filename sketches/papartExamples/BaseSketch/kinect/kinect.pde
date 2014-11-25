import fr.inria.papart.procam.*;
import org.bytedeco.javacv.*;
import toxi.geom.*;
import peasy.*;

void setup(){
    int cameraX = 640;
    int cameraY = 480;

    size(cameraX, cameraY, OPENGL);

    Papart papart = new Papart(this);
    papart.initKinectCamera(1);
    papart.loadSketches() ;
    papart.startTracking();
}



void draw(){

}

