import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;


public class MyApp extends PaperTouchScreen {

    PMatrix3D kinectProjector;
    void setup() {
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);

	kinectProjector = papart.loadCalibration(Papart.kinectTrackingCalib);
	kinectProjector.invert();
    }

    PVector pos = new PVector();
    
    void draw(){
	beginDraw3D();
	clear();
	background(0);
	
	float ellipseSize = 5;

	// in draw3D Mode the graphics here are the projector's graphics. 

	ProjectorDisplay projector = (ProjectorDisplay) display;
	projector.loadModelView();
	applyMatrix(projector.getExtrinsics());

	lights();
	pointLight(0, 100, 0, 0, 100, 0);

	noStroke();
	fill(255);


	// pushMatrix();
	// translate(0, 0, 850);
	// sphere(10);
	// popMatrix();

	
	for (Touch t : touchList) {

	    // draw the touch. 
	    PVector p = t.position;
	    // fill(200);
	    // ellipse(p.x, p.y, ellipseSize, ellipseSize);

	    // draw the elements of the Touch

	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }

	    //	    Vec3D depthPoint = tp.getPositionKinect();

	    ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();
	    for(DepthDataElementKinect dde : depthDataElements){
		    
	    	    Vec3D depthPoint = dde.depthPoint;
	    
		    kinectProjector.mult(new PVector(depthPoint.x,
						     depthPoint.y,
						     depthPoint.z),
					 pos);
		    pushMatrix();
		    translate(pos.x, pos.y , pos.z);
		    sphere(1);
		    popMatrix();
		    
	    }
	}
	endDraw();
    }
}





