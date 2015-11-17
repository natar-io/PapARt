import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import processing.video.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.skatolo.Skatolo;

boolean useProjector = true;
float renderQuality = 1.5f;
Papart papart;

void settings(){

    if(useProjector){
	fullScreen(P3D);
    }else{
    	size((int) (640 * renderQuality),
	     (int) (480 * renderQuality),
	     P3D);
    }
}

 void setup(){

    if(useProjector){
	papart = Papart.projection(this);
	papart.loadTouchInput();
    } else {

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
