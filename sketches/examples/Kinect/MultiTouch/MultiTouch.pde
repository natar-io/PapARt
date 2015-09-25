import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacv.*;
import toxi.geom.*;


KinectTouchInput touchInput;

void settings(){
    fullScreen(P3D);
}

void setup(){
    Papart papart = Papart.projection2D(this);

    // arguments are 2D and 3D precision.
    papart.loadTouchInputKinectOnly();
    touchInput = (KinectTouchInput) papart.getTouchInput();
    frameRate(200);
}

void draw(){

    println("Framerate "+ frameRate);
    background(100);

    fill(50, 50, 255);
	
    fill(255, 0, 0);
    ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
    for(TouchPoint tp : touchs3D){

    	PVector pos = tp.getPosition();
    	ellipse(pos.x * width,
    		pos.y * height, 40, 40);
    }



    // Get a copy, as the arrayList is constantly modified
    ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
    for(TouchPoint tp : touchs2D){
	fill(50, 50, 255);
	PVector pos = tp.getPosition();
	ellipse(pos.x * width,
		pos.y * height, 20, 20);
    }

}



