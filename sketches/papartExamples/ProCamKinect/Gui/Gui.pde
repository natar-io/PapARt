// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;


int framePosX = 0;
int framePosY = 120;
int frameSizeX = 1920;
int frameSizeY = 1080;
boolean useProjector = true;


// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

public void setup() {

  if (useProjector) {
    size(frameSizeX, frameSizeY, OPENGL);
  } else {     
    size(1280, 1024, OPENGL);
  }

  Papart papart = new Papart(this);

  String cameraNo = "0";
  if (useProjector) {
    papart.initProjectorCamera(cameraNo, Camera.Type.FLY_CAPTURE);
    papart.loadTouchInput(2, 5);
  } else {
    papart.initCamera(cameraNo, Camera.Type.FLY_CAPTURE);
    papart.loadTouchInputKinectOnly(2, 5);
  }

  papart.loadSketches();
  papart.startTracking();
}

void draw() {
}


void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if (key == ' ')
    frame.setLocation(framePosX, framePosY);
}
