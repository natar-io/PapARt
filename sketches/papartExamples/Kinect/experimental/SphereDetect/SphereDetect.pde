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


float radius1 = 100f;

void draw(){

    background(100);

    fill(255, 0, 0);
    ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());

    //     for(TouchPoint tp : touchs3D){

    if(touchs3D.isEmpty())
	return;

	TouchPoint tp = touchs3D.get(0);

    	ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();

	// First point is the most forward. 
	Vec3D center1 = depthDataElements.get(0).projectedPoint;

	center1.x *= frameSizeX;
	center1.y *= frameSizeY;
	//	center1.y -= radius1;

	fill(200, 0, 0);
	ellipse(center1.x, center1.y, 12, 12);

	center1.y -= radius1;
	ellipse(center1.x, center1.y, 12, 12);

    	for(DepthDataElement dde : depthDataElements){
    	    Vec3D v = dde.projectedPoint;
    	    noStroke();

	    if(dde.normal == null)
		continue;

	    Vec3D normal = dde.normal.invert().scaleSelf(radius1 );


	    Vec3D center2 = new Vec3D(normal.x + v.x * frameSizeX, 
				      normal.y + v.y * frameSizeY, 
				      normal.z + v.z);
				      
	       
    	    //	    setColor(dde.pointColor, 100);
	    // setNormalColor(dde.normal);
	    float dist = center1.distanceTo(center2);

	    if(dist > 300) 
		return;

	    fill(0, dist, 0);
    	    ellipse(v.x * frameSizeX + normal.x,
    	    	    v.y * frameSizeY + normal.y,
    	    	    4, 4);

	    fill(0, 0, 200);
    	    ellipse(v.x * frameSizeX,
    		    v.y * frameSizeY,
    		    4, 4);


    	}


    	// PVector pos = tp.getPosition();
    	// ellipse(pos.x * frameSizeX,
    	// 	pos.y * frameSizeY, 40, 40);
	// //    }



}

void setNormalColor(Vec3D normal){
    colorMode(RGB, 1.0);
    if(normal != null){
	fill(normal.x, normal.y, normal.z);
    } else {
	fill(0, 1, 0);
    } 
}

void setColor(int rgb, float intens){
    colorMode(RGB, 255);
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



