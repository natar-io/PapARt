import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;


public class MyApp extends PaperTouchScreen {

    void settings(){
	setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
    }

    void setup() {
    }

    void drawOnPaper(){

	clear();
	background(0);

	float ellipseSize = 5;

	for (Touch t : touchList) {

	    // draw the touch.
	    PVector p = t.position;
	    fill(200);
	    ellipse(p.x, p.y, ellipseSize, ellipseSize);

	    // draw the elements of the Touch

	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }

	    ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();

	    for(DepthDataElementKinect dde : depthDataElements){
		try{
		    Vec3D projPt = dde.projectedPoint;
		    PVector v = touchInput.project(screen, getDisplay(), projPt.x, projPt.y);
		    noStroke();
		    fill(red(dde.pointColor), green(dde.pointColor), blue(dde.pointColor));

		    ellipse(v.x * drawingSize.x,
			    v.y * drawingSize.y,
			    ellipseSize, ellipseSize);
		}catch(Exception e){
		    println("Exception No Intersection " + e);
		}
	    }
	}
    }
}
