// PapARt library
import fr.inria.papart.procam.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;


int framePosX = 0;
int framePosY = 0;
int frameSizeX = 640;
int frameSizeY = 480;
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
    size(640, 480, OPENGL);
  }

  if (frame != null) {
    frame.setResizable(true);
  }


  Papart papart = new Papart(this);

  String cameraNo = "2";
  if (useProjector) {
    papart.initProjectorCamera(cameraNo, Camera.Type.OPENCV);
  } else {
    papart.initCamera(cameraNo, Camera.Type.OPENCV);
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
