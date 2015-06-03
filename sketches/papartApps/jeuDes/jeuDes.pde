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
TouchDetectionSimple2D touchDetection; 

    
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

    touchInput = (KinectTouchInput) papart.getTouchInput();
    touchDetection = touchInput.getTouchDetection2D();
    
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
    
    if(key == 'n' || key == ' ')
	nextPlayer();
    
}


