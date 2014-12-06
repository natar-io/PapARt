// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import fr.inria.guimodes.*;

Papart papart;

// Frame location. 
int framePosX = 0;
int framePosY = 200;

boolean useProjector;
 

// Undecorated frame 
public void init() {
    frame.removeNotify(); 
    frame.setUndecorated(true); 
    frame.addNotify(); 
    super.init();
}

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 1;  // 3 pixels / mm

void setup(){

    useProjector = true;
    int frameSizeX = 1280;
    int frameSizeY = 800;

    if(!useProjector) {
	frameSizeX = 640 * 2;
	frameSizeY = 480 * 2;
    }

    size(frameSizeX, frameSizeY, OPENGL);
    papart = new Papart(this);

    if(useProjector){
	papart.initProjectorCamera("0", Camera.Type.OPENCV);
	// papart.loadTouchInput(2, 5);
    } else {
	papart.initKinectCamera(2f);
	papart.loadTouchInputKinectOnly(1, 7);
	BaseDisplay display = papart.getDisplay();
	display.setDrawingSize(width, height);
    }


    // MarkerBoard miniTeegiVision; 
    // this.markerBoard = new MarkerBoard(configFile, width, height);

    papart.loadSketches();
    papart.startTracking();
}



void draw(){
}

boolean test = false;

void keyPressed() {


    // Placed here, bug if it is placed in setup().
    if(key == ' ')
	frame.setLocation(framePosX, framePosY);

    if(key == 't'){
	test = !test;
    }

    if(key == '1'){
	Mode.set("raw");
    }
    if(key == '2'){
	Mode.set("relax");
    }

    if(key == '3'){
	Mode.set("vision");
    }

}


