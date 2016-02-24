// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;
import fr.inria.skatolo.Skatolo;

import fr.inria.guimodes.*;

Papart papart;

boolean useProjector = true;
boolean noCameraMode = false;

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 1;  // 3 pixels / mm
int frameSizeX = 640 * 2;
int frameSizeY = 480 * 2;


void settings(){

    if (noCameraMode) {
	size(frameSizeX, frameSizeY, P3D);
    }

    fullScreen(P3D);
}

PApplet mainApplet;

void setup(){
    mainApplet = this;

    if (noCameraMode) {
	papart = new Papart(this);
	papart.initNoCamera(1);
	papart.loadTouchInputTUIO();
	papart.getTouchInput().computeOutsiders(true);

    } else {
	if (useProjector) {

	    papart = Papart.projection(this);
	    papart.loadTouchInput();

	} else {
//	    size(frameSizeX, frameSizeY, OPENGL);
	    papart = new Papart(this);
	    papart.initKinectCamera(2f);
	    papart.loadTouchInputKinectOnly();
	    BaseDisplay display = papart.getDisplay();
	    display.setDrawingSize(width, height);
	}
    }

    // see Physics.pde
    initPhysics();

    papart.loadSketches();

    if (!noCameraMode) {
	papart.startTracking();
    }else {
	Mode.add("wall");
	Mode.add("touch");
	Mode.set("wall");
    }

}

void draw() {
    // println("FRAMERATE " + frameRate);
}

boolean fixBoards = false;
boolean fixGame = false;
int trackingFixDuration = 2000;
boolean test = false;

boolean DEBUG_TOUCH = false;
boolean doColorAnalysis = true;

void keyPressed() {


    if(key =='a'){
       doColorAnalysis = !doColorAnalysis;
    }

    if (key == 'f') {
	fixBoards = !fixBoards;
	papart.getCameraTracking().trackSheets(!fixBoards);
    }

    if (key == 'b') {
	fixGame = !fixGame;
    }

    if (key == 't') {
	test = !test;
    }

    if (key == '1') {
	Mode.set("wall");
    }
    if (key == '2') {
	Mode.set("touch");
    }


    println("Test " + test);
}
