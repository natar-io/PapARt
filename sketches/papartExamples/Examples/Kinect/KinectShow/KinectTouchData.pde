import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;


public class MyApp extends PaperTouchScreen {
    
    void setup() {
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
    }
    
    void draw(){
	beginDraw3D();
	clear();
	noStroke();

	//	ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData((ARDisplay )display, screen);
	//ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData2D((ARDisplay )display, screen);
	ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData3D((ARDisplay )display, screen);


	int k = 0;
	for(DepthPoint depthPoint : points) {
	    PVector pos = depthPoint.getPosition();

	    PVector p1 = new PVector( pos.x,
	    			      pos.y,
				      pos.z);


	    if(p1.x < 0 
	       || p1.x >= drawingSize.x
	       || p1.y < 0 
	       || p1.y >= drawingSize.y)
	    	continue;

	    float ellipseSize = 3;
	    int c = depthPoint.getColor();
	    if(c == Kinect.INVALID_COLOR){
	    	fill(0, 0, 200);
	    } else {
	    	fill(red(c), green(c), blue(c));
	    }
	    
	    pushMatrix();
	    translate(p1.x, p1.y, p1.z);
	    ellipse(0, 0, ellipseSize, ellipseSize);
	    popMatrix();
	}

	endDraw();
    }
}





