 import fr.inria.papart.procam.*;
import fr.inria.papart.tools.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacv.*;
import toxi.geom.*;
import peasy.*;


int framePosX = 0;
int framePosY = 200;

// Undecorated frame 
public void init() {
  frame.removeNotify(); 
  frame.setUndecorated(true); 
  frame.addNotify(); 
  super.init();
}

KinectTouchInput touchInput;

int frameSizeX = 1280;
int frameSizeY = 800;

void setup(){
    size(frameSizeX, frameSizeY, OPENGL);

    Papart papart = new Papart(this);

    // arguments are 2D and 3D precision.
    papart.loadTouchInputKinectOnly(4, 0);
    touchInput = (KinectTouchInput) papart.getTouchInput();
}


void draw(){

    println("Framerate "+ frameRate);
    background(100);

    fill(50, 50, 255);

    // Get a copy, as the arrayList is constantly modified
    ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
    for(TouchPoint tp : touchs2D){
	fill(50, 50, 255);
	PVector pos = tp.getPosition();
	ellipse(pos.x * frameSizeX,
		pos.y * frameSizeY, 20, 20);
    }
	
    fill(255, 0, 0);
    ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
    for(TouchPoint tp : touchs3D){

	PVector pos = tp.getPosition();
	ellipse(pos.x * frameSizeX,
		pos.y * frameSizeY, 40, 40);
    }
}

void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}



