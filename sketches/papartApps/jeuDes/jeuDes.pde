import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import processing.video.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;

import fr.inria.guimodes.Mode;

boolean useProjector = true;
float renderQuality = 1.5f;
Papart papart;

KinectTouchInput touchInput;

boolean noCamera = false;

    
void setup(){


    if(noCamera) {
	size(800, 600, OPENGL);
	Papart papart = new Papart(this);
	papart.initNoCamera(1);
	papart.loadTouchInputTUIO();
	papart.loadSketches();
	return;
    }

    
    if(useProjector){
	papart = Papart.projection(this);
	papart.loadTouchInput();
    } else {
	size((int) (640 * renderQuality),
	     (int) (480 * renderQuality),
	     OPENGL);

	papart = new Papart(this);

	papart.initKinectCamera(renderQuality);
	papart.loadTouchInputKinectOnly();
	BaseDisplay display = papart.getDisplay();
	display.setDrawingSize(width, height);
    }

    touchInput = (KinectTouchInput) papart.getTouchInput();
    
    papart.loadSketches();
    papart.startTracking();
}


void draw(){
}

boolean test = false;

void keyPressed() {

    if(key == 'B')
	board.saveLocationTo("board.xml");
    if(key == 'b')
	board.loadLocationFrom("board.xml");

    if(key == 'C')
	counter.saveLocationTo("counter.xml");
    if(key == 'c')
	counter.loadLocationFrom("counter.xml");

    if(key == 'S')
	myStatus.saveLocationTo("myStatus.xml");
    if(key == 's')
	myStatus.loadLocationFrom("myStatus.xml");

    
    if(key == 't')
	test = !test;

    if(key == 'r')
	removeLastToken();
    
    if(key == 'n')
	nextPlayer();

    if(key == ' ')
	action();
    
}


