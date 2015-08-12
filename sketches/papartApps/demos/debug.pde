 
PVector debugSize = new PVector(300, 180);   //  29.7 * 21 cm

import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;

public class Debug  extends PaperTouchScreen {

    void setup(){
	setDrawingSize((int) debugSize.x, (int) debugSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/debug.cfg", 
		    debugSize.x, debugSize.y);
    }

    void draw(){

	beginDraw2D();
	clear();
	background(80);

	fill(0);
	rect(0, 297, 80, 80);

	noStroke();


	//	drawTouch(10);

	fill(80, 80, 255, 180);
	stroke(255, 80);
	
	// try{
	//     ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData((ARDisplay )display, screen);
	//     colorMode(RGB, 255);
	//     for(DepthPoint depthPoint : points) {
	// 	PVector pos = depthPoint.getPosition();
	// 	PVector p1 = new PVector(pos.x * drawingSize.x, 
	// 			     pos.y * drawingSize.y);
	// 	float ellipseSize = 3;
	// 	// if(p.z < 80)
	// 	// 	ellipseSize +=  1f / (p.z + 10) * 500;
	// 	int c = depthPoint.getColor();
	// 	if(c == -1){
	// 	    fill(0, 0, 200);
	// 	} else {
	// 	    //println(c);
	// 	    fill(red(c), green(c), blue(c));
	// 	    stroke(red(c), green(c), blue(c));
	// 	}
	// 	ellipse(p1.x, p1.y, ellipseSize, ellipseSize);
	//     }
	// }catch(Exception e){println("Exception ?! " + e); }

	//////////////////

	// drawTouch();

	float ellipseSize = 5;

	for (Touch t : touchList) {

	    PVector p = t.position;
	    fill(200);
	    ellipse(p.x, p.y, ellipseSize, ellipseSize);

	    TouchPoint tp = t.touchPoint;
	    ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();

	    for(DepthDataElementKinect dde : depthDataElements){

		Vec3D projPt = dde.projectedPoint;
		try{
		    PVector v = touchInput.project(screen, display, projPt.x, projPt.y);
		    
		    noStroke();
		    fill(red(dde.pointColor), green(dde.pointColor), blue(dde.pointColor));
		    
		    ellipse(v.x * drawingSize.x, 
			    v.y * drawingSize.y, 
			    ellipseSize, ellipseSize);
		}catch(Exception e){
		    println("Exception " + e);
		    e.printStackTrace();
		}
	    }

	    // if(p.z < 80)
	    // 	    ellipseSize +=  1f / (p.z + 10) * 500;
	}

	endDraw();

    }

}
