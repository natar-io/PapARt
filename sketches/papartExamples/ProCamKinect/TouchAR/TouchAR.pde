import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import processing.video.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;


boolean useProjector = true;
float renderQuality = 1.5f;
Papart papart;

void setup(){

    if(useProjector){
	papart = Papart.projection(this);
	papart.loadTouchInput();
    } else {

	size((int) (Kinect.WIDTH * renderQuality),
	     (int) (Kinect.HEIGHT * renderQuality),
	     OPENGL);

	papart = new Papart(this);

	papart.initKinectCamera(renderQuality);
	papart.loadTouchInputKinectOnly();
	BaseDisplay display = papart.getDisplay();
	display.setDrawingSize(width, height);
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



