import fr.inria.papart.procam.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.procam.display.*;
import org.bytedeco.javacpp.*;
import org.reflections.*;
import TUIO.*;
import toxi.geom.*;

import skatolo.*;

Papart papart;
boolean useProjector; 

// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}
void setup(){

    useProjector = false;
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
	papart.loadTouchInput(2, 2);
    } else {
	papart.initKinectCamera(2f);
	papart.loadTouchInputKinectOnly(2, 2);
	BaseDisplay display = papart.getDisplay();
	display.setDrawingSize(width, height);
    }

    papart.loadSketches() ;
    papart.startTracking();

   Skatolo skatolo = new Skatolo(this);
    skatolo.addSlider("hValue")
	.setPosition(100,50)
	.setRange(0,180)
	;

    skatolo.addSlider("sValue")
	.setPosition(100,70)
	.setRange(0,100)
	;

    skatolo.addSlider("bValue")
	.setPosition(100,90)
	.setRange(0,100)
	;
    

}

int hValue = 50;
int sValue = 50;
int bValue = 50;


void draw(){
}

// Frame location. 
int framePosX = 0;
int framePosY = 200;


boolean test = false;
void keyPressed(){

    if(key == 't')
	test = !test;

  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}

