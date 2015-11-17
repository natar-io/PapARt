// PapARt library

import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;


Papart papart;

boolean useProjector = true;
float planetScale = 2f / 20000f;


PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 1;  // 3 pixels / mm
float renderQuality = 1.5f;

void setup() {

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


void draw() {
}

