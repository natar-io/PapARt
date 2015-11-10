import fr.inria.papart.procam.*;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.tuio.*;
import fr.inria.papart.multitouch.*;

import oscP5.*;
import org.bytedeco.javacv.*;
import toxi.geom.*;
import peasy.*;

TuioServer server;
OpenKinectFrameGrabber openKinectGrabber = null;
KinectTouchInput touchInput;

int frameSizeX = 1280;
int frameSizeY = 800;

void settings(){
    size(800, 600, P3D);
}

void setup(){

    Papart papart = new Papart(this);

    // arguments are 2D and 2D precising in that order.
    papart.loadTouchInputKinectOnly();
    touchInput = (KinectTouchInput) papart.getTouchInput();
    server = new TuioServer(this, 12000, "127.0.0.1", 3333);
}


void draw(){

    background(100);
    ArrayList<TouchPoint> touchs2D = new ArrayList<TouchPoint>(touchInput.getTouchPoints2D());
    server.send2D(touchs2D);

    for(TouchPoint tp : touchs2D){
	fill(50, 50, 255);
	PVector pos = tp.getPosition();
	ellipse(pos.x * width,
		pos.y * height, 20, 20);
    }
    // TODO: text for nb points ?
}
