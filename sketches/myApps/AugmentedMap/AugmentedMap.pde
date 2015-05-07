import fr.inria.papart.procam.*;
import fr.inria.papart.drawingapp.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.multitouch.*;
import fr.inria.papart.drawingapp.Button; 
import org.bytedeco.javacpp.*;
import javax.media.opengl.GL;
import processing.opengl.*;
import org.reflections.*;
import toxi.geom.*;
import java.util.Vector;


final int TANGIBLE = 0;
final int TACTILE = 1;
final int MOVEMENT = 2;

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector A4BoardSize = new PVector(297, 210);   //  21 * 29.7 cm
PVector MenuSize = new PVector(130, 130);
PVector ZoomSize = new PVector(120, 210);
float boardResolution = 3;  // 3 pixels / 

// Frame location. 
int framePosX = 0;
int framePosY = 200;

int physicalZoomLevel;
int zoomType = TANGIBLE;

boolean useProjector;

PFont mainFont = createFont("Arial", 20);

//Buttons for the menu
final int CAPTURE = 0;
final int HOME = 1;
Vector<YButton> yButtons = new Vector<YButton>();
 
// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}


void setup(){
    physicalZoomLevel = 12;
    useProjector = true;
    int frameSizeX = 1280;
    int frameSizeY = 800;

    if(!useProjector) {
	frameSizeX = 640 * 2;
	frameSizeY = 480 * 2;
    }

    size(frameSizeX, frameSizeY, OPENGL);
    //size((int) A4BoardSize.x, (int) A4BoardSize.y, OPENGL);
    Papart papart = new Papart(this);

    if(useProjector){
	papart.initProjectorCamera("1", Camera.Type.OPENCV);
	papart.loadTouchInput(2, 5);
    } else {
	papart.initKinectCamera(2);
	papart.loadTouchInputKinectOnly(2, 5);
    }

    papart.loadSketches();
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
