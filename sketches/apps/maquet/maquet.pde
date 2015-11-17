import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;

import org.reflections.*;
import org.bytedeco.javacpp.*;
import toxi.geom.*;
import fr.inria.skatolo.Skatolo;

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 3;  // 3 pixels / mm

float renderQuality = 1.5f;
boolean useProjector = true;
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
