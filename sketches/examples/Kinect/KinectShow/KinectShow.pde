import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import processing.video.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.procam.camera.*;


boolean useProjector = false;
float renderQuality = 1.5f;
Papart papart;

void settings(){
    size(200, 200, P3D);
}

void setup(){

    if(useProjector){
	papart = Papart.projection(this);
	papart.loadTouchInput();
    } else {

        try{
        papart = new Papart(this);
	papart.initKinectCamera(renderQuality);
	papart.loadTouchInputKinectOnly();
        } catch(Exception e){
            println("Exception " + e);
            e.printStackTrace();
        }
    }

    papart.loadSketches();
    papart.startTracking();
}


void draw(){
}

boolean test = false;

void keyPressed() {
    if(key == 't')
	test = !test;
}
