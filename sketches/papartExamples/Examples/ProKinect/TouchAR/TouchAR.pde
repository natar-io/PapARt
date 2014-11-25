import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacpp.*;
import org.reflections.*;
import toxi.geom.*;

// Frame location. 
int framePosX = 0;
int framePosY = 120;
int frameSizeX = 1280;
int frameSizeY = 800;

boolean useProjector;
 
// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

void setup(){
    useProjector = true;

    if(!useProjector) {
	frameSizeX = 640 * 2;
	frameSizeY = 480 * 2;
    }

    size(frameSizeX, frameSizeY, OPENGL);
    Papart papart = new Papart(this);

    if(useProjector){
	papart.initProjectorCamera("0", Camera.Type.OPENCV);
	papart.loadTouchInput(2, 5);
    } else {
	papart.initKinectCamera(2);
	papart.loadTouchInputKinectOnly(2, 5);
    }

    papart.getARDisplay().setDistort(true);

    papart.loadSketches();
    papart.startTracking();
}

ProjectiveDeviceP pdp;

void draw(){

}


void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}
