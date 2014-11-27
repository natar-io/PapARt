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
    papart.loadTouchInputKinectOnly(2, 2);
    touchInput = (KinectTouchInput) papart.getTouchInput();
}


void draw(){

    background(100);

    colorMode(RGB, 1.0);
    ArrayList<DepthDataElement> depthPoints =  touchInput.getDepthData();
    for(DepthDataElement p : depthPoints){

    	noStroke();

    	Vec3D pos = p.projectedPoint;
    	Vec3D normal = p.normal;
	
    	if(normal != null){
    	fill(normal.x, normal.y, normal.z);
    	} else {
    	    fill(0, 1, 0);
    	}
    	ellipse(pos.x * frameSizeX,
    		pos.y * frameSizeY, 5, 5);
    }


    // Get a copy, as the arrayList is constantly modified
    // ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
    // for(TouchPoint tp : touchs2D){

    // 	ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();
    // 	for(DepthDataElement dde : depthDataElements){
    // 	    Vec3D v = dde.projectedPoint;
    // 	    noStroke();
    // 	    colorMode(RGB, 1.0);
    // 	    setColor(dde.pointColor, 255);
    // 	    ellipse(v.x * frameSizeX,
    // 		    v.y * frameSizeY,
    // 		    10, 10);
    // 	}

    // 	fill(50, 50, 255);
    // 	PVector pos = tp.getPosition();
    // 	ellipse(pos.x * frameSizeX,
    // 		pos.y * frameSizeY, 20, 20);
    // }
	
    // fill(255, 0, 0);
    // ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
    // for(TouchPoint tp : touchs3D){

    // 	ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();
	
    // 	for(DepthDataElement dde : depthDataElements){
    // 	    Vec3D v = dde.projectedPoint;
    // 	    noStroke();
    // 	    //	    setColor(dde.pointColor, 100);


    // 	    Vec3D normal = dde.normal;

    // 	    colorMode(RGB, 1.0);
	    
    // 	    if(normal != null){
    // 		fill(normal.x, normal.y, normal.z);
    // 	    } else {
    // 		fill(0, 1, 0);
    // 	    } 
    // 	    ellipse(v.x * frameSizeX,
    // 		    v.y * frameSizeY,
    // 		    10, 10);
    // 	}

    // 	PVector pos = tp.getPosition();
    // 	ellipse(pos.x * frameSizeX,
    // 		pos.y * frameSizeY, 40, 40);
    // }


}

void setColor(int rgb, float intens){
    int r = (rgb >> 16) & 0xFF;  // Faster way of getting red(argb)
    int g = (rgb >> 8) & 0xFF;   // Faster way of getting green(argb)
    int b = rgb & 0xFF;          // Faster way of getting blue(argb)
    fill(r, g, b, intens);
}

void keyPressed() {

  // Placed here, bug if it is placed in setup().
  if(key == ' ')
    frame.setLocation(framePosX, framePosY);
}



