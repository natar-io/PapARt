import fr.inria.papart.depthcam.DepthData;
import fr.inria.papart.depthcam.*;
import java.util.ArrayList;
import toxi.geom.Vec3D;
import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.display.*;



public class MyApp extends PaperTouchScreen {

    void setup() {
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath() + "/data/A3-small1.cfg", 297, 210);
    }

    void draw(){
	beginDraw2D();
	clear();
	background(0);
	noStroke();

	ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData((ARDisplay ) getDisplay(), screen);
	// ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData2D((ARDisplay )display, screen);
	// ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData3D((ARDisplay )display, screen);

	for(DepthPoint depthPoint : points) {
	    PVector pos = depthPoint.getPosition();
	    PVector p1 = new PVector(pos.x,
				     drawingSize.y - pos.y);

	    float ellipseSize = 3;
	    int c = depthPoint.getColor();
	    if(c == DepthAnalysis.INVALID_COLOR){
		fill(0, 0, 200);
	    } else {
		fill(red(c), green(c), blue(c));
	    }
	    ellipse(p1.x, p1.y, ellipseSize, ellipseSize);
	}
	endDraw();
    }
}
