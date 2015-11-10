// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.procam.display.*;

import fr.inria.papart.multitouch.*;
import fr.inria.papart.depthcam.*;

import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import fr.inria.guimodes.*;

Papart papart;


boolean useProjector = true;
boolean noCameraMode = false;

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 1;  // 3 pixels / mm
float renderQuality = 1.5f;

void setup() {

    if(noCameraMode){

	int frameSizeX = 1400;
	int frameSizeY = 800;
	size(frameSizeX, frameSizeY, OPENGL);

	papart = new Papart(this);
	papart.initNoCamera(1);
	
	return;
    }

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
    if (!noCameraMode)
	papart.startTracking();
}



void draw() {
}

boolean test = false;

void keyPressed() {

  if (key == 't') {
    test = !test;
  }

  if (key == '1') {
    Mode.set("raw");
  }
  if (key == '2') {
    Mode.set("relax");
  }
  if (key == '3') {
    Mode.set("vision");
  }

  if (key == '4') {
    SecondMode.set("waves");
  }
  if (key == '5') {
    SecondMode.set("pixelate");
  }
  if (key == '6') {
    SecondMode.set("noise");
  }
  if (key == '7') {
    SecondMode.set("clear");
  }

  //     SecondMode.add("clear");
  //   SecondMode.add("waves");
  //   SecondMode.add("pixelate");
  //   SecondMode.add("noise");
  //   SecondMode.set("waves");
}
