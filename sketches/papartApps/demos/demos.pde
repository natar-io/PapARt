import fr.inria.papart.procam.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.drawingapp.Button; 
import org.bytedeco.javacpp.*;
import javax.media.opengl.GL;
import processing.opengl.*;
import org.reflections.*;

 
// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify();
   super.init();
}

float boardResolution = 4;   //  pixels / mm

// Frame location. 
int framePosX = 0;
int framePosY = 200;


boolean useProjector;

// check
PVector A4BoardSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector interfaceSize = new PVector(60, 40);   //  6 * 4 cm

MarkerBoard markerBoardDrawing ;

public void setup(){

    //////////////////////////////////////////////////////////
    useProjector = true;
    int frameSizeX = 1280;
    int frameSizeY = 800;

    if(!useProjector) {
	frameSizeX = 640 * 2;
	frameSizeY = 480 * 2;
    }

    size(frameSizeX, frameSizeY, OPENGL);
    Papart papart = new Papart(this);

    if(useProjector){
	papart.initProjectorCamera("2", Camera.Type.OPENCV, 1);
	papart.loadTouchInput(2, 5);
    } else {
	papart.initKinectCamera(2);
	papart.loadTouchInputKinectOnly(2, 5);
    }


    papart.loadSketches() ;
    papart.startTracking();
}

void draw(){
}

boolean test = false;
void keyPressed() {
    if(key == 't') 
	test = !test;

  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}
 
