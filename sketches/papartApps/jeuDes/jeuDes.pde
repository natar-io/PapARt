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

void setup(){

    if(useProjector){
	papart = Papart.projection(this);
	papart.loadTouchInput(2, 0);
    } else {
	size((int) (Kinect.WIDTH * renderQuality),
	     (int) (Kinect.HEIGHT * renderQuality),
	     OPENGL);

	papart = new Papart(this);

	papart.initKinectCamera(renderQuality);
	papart.loadTouchInputKinectOnly(1, 1);
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

    if(key == 'B')
	board.saveLocationTo("board.xml");
    if(key == 'b')
	board.loadLocationFrom("board.xml");

    if(key == 'C')
	counter.saveLocationTo("counter.xml");
    if(key == 'c')
	counter.loadLocationFrom("counter.xml");

    
    if(key == 't')
	test = !test;

    if(key == 'r')
	removeLastToken();
    
    if(key == 'n')
	nextPlayer();
    
}


