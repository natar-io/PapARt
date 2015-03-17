// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;



Camera camera;
int resX = 1280;
int resY = 720;

public void setup() {


  size(resX, resY, OPENGL);

  if (frame != null) {
    frame.setResizable(true);
  }

  camera = CameraFactory.createCamera(Camera.Type.FLY_CAPTURE, 0);
  camera.setParent(this);
  camera.setSize(resX, resY);
  ((CameraFlyCapture) camera).setBayerDecode(true);
  camera.start();
  camera.setThread();

}

void draw() {
    PImage im = camera.getPImage();
    if(im != null)
	image(im, 0, 0, width, height);

}


