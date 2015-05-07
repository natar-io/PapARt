// PapARt library
import fr.inria.papart.procam.*;
import fr.inria.papart.procam.camera.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import oscP5.*;
import netP5.*;
  

Camera camera;
ARDisplay arDisplay;
Screen myScreen;

int resX = 1280;
int resY = 720;

OscP5 oscP5;
PMatrix3D screenPos;

public void setup() {

  String calibrationFile = dataPath("camera.yaml");


  camera = CameraFactory.createCamera(Camera.Type.FLY_CAPTURE, 0);
  camera.setParent(this);
  camera.setCalibration(calibrationFile);

  size(camera.width(), camera.height(), OPENGL);
  if (frame != null) {
      frame.setResizable(true);
  }

  ((CameraFlyCapture) camera).setBayerDecode(true);
  camera.start();
  camera.setThread();

  arDisplay = new ARDisplay(this, camera);
  arDisplay.setZNearFar(20, 3000); // mm 
  arDisplay.setQuality(1); //  width * quality * height * quality resolution.
  arDisplay.init();
  arDisplay.manualMode();

  // myScreen = new Screen(this, new PVector(300, 300), 1);
  // myScreen.getGraphics();
  // screenPos = myScreen.getPosition();
  // arDisplay.addScreen(myScreen);


  screenPos = new PMatrix3D();



  oscP5 = new OscP5(this, 3333);  

}

void draw() {

    background(100, 0, 0);
    if(camera.getPImage() != null)
	image(camera.getPImage(), 0, 0, width, height);

    PGraphicsOpenGL gl = arDisplay.beginDraw();
    gl.clear();
    gl.pushMatrix();
    gl.applyMatrix(screenPos);

    gl.box(100);
    gl.popMatrix();

    arDisplay.endDraw();

    //    arDisplay.drawScreens();

    noStroke();
    image(arDisplay.render(), 0, 0, (int) width, (int) height);


}

/* incoming osc message are forwarded to the oscEvent method. */
void oscEvent(OscMessage theOscMessage) {
    /* print the address pattern and the typetag of the received OscMessage */
    //  print("### SERVER 12000 received an osc message.");
    //  print(" addrpattern: "+theOscMessage.addrPattern());
    //  println(" typetag: "+theOscMessage.typetag());
	
    if(theOscMessage.checkAddrPattern("/pos")==true) {
	/* check if the typetag is the right one. */
	if(theOscMessage.checkTypetag("ffffffffffff")) {
	    /* parse theOscMessage and extract the values from the osc message arguments. */

	    screenPos.m00 = theOscMessage.get(0).floatValue();
	    screenPos.m01 = theOscMessage.get(1).floatValue();
	    screenPos.m02 = theOscMessage.get(2).floatValue();
	    screenPos.m03 = theOscMessage.get(3).floatValue();

	    screenPos.m10 = theOscMessage.get(4).floatValue();
	    screenPos.m11 = theOscMessage.get(5).floatValue();
	    screenPos.m12 = theOscMessage.get(6).floatValue();
	    screenPos.m13 = theOscMessage.get(7).floatValue();

	    screenPos.m20 = theOscMessage.get(8).floatValue();
	    screenPos.m21 = theOscMessage.get(9).floatValue();
	    screenPos.m22 = theOscMessage.get(10).floatValue();
	    screenPos.m23 = theOscMessage.get(11).floatValue();
	}  
    } 
	
}



