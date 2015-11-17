import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.multitouch.*;

import org.bytedeco.javacv.*;
import toxi.geom.*;
import peasy.*;



KinectTouchInput touchInput;

void settings(){
    fullScreen(P3D);
}

void setup(){
    Papart.projection2D(this);

    Papart papart = new Papart(this);

    // arguments are 2D and 3D precision.
    papart.loadTouchInputKinectOnly();
    touchInput = (KinectTouchInput) papart.getTouchInput();
}


void draw(){

    background(100);

    fill(50, 50, 255);

    // Get a copy, as the arrayList is constantly modified
    ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
    for(TouchPoint tp : touchs2D){

	ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();
	for(DepthDataElementKinect dde : depthDataElements){
	    Vec3D v = dde.projectedPoint;
	    noStroke();
	    setColor(dde.pointColor, 255);
	    ellipse(v.x * width,
		    v.y * height,
		    10, 10);
	}

	fill(50, 50, 255);
	PVector pos = tp.getPosition();
	ellipse(pos.x * width,
		pos.y * height, 20, 20);
    }


    fill(255, 0, 0);
    ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(touchInput.getTouchPoints3D());
    for(TouchPoint tp : touchs3D){

        ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();

        for(DepthDataElementKinect dde : depthDataElements){
            Vec3D v = dde.projectedPoint;
            noStroke();
            setColor(dde.pointColor, 100);
            ellipse(v.x * width,
        	    v.y * height,
        	    10, 10);
        }

        PVector pos = tp.getPosition();
        ellipse(pos.x * width,
        	pos.y * height, 40, 40);
    }
}

void setColor(int rgb, float intens){
    int r = (rgb >> 16) & 0xFF;  // Faster way of getting red(argb)
    int g = (rgb >> 8) & 0xFF;   // Faster way of getting green(argb)
    int b = rgb & 0xFF;          // Faster way of getting blue(argb)
    fill(r, g, b, intens);
}
