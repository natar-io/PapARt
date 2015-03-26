// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;


int framePosX = 0;
int framePosY = 120;
int frameSizeX = 1280;
int frameSizeY = 1024;
boolean useProjector = false;


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

  if (frame != null) {
    frame.setResizable(true);
  }


  Papart papart = new Papart(this);

  String cameraNo = "0";
  if (useProjector) {
    papart.initProjectorCamera(cameraNo, Camera.Type.OPENCV);
  } else {
    papart.initCamera(cameraNo, Camera.Type.FLY_CAPTURE);
  }
  papart.loadSketches();
  papart.startTracking();
  frameRate(200);
}

void draw() {
}


void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if (key == ' ')
    frame.setLocation(framePosX, framePosY);
}
