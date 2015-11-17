import fr.inria.papart.depthcam.DepthData;
import java.util.ArrayList;
import toxi.geom.Vec3D;

PVector boardSize = new PVector(297, 210);   //  21 * 29.7 cm
float boardResolution = 3;  // 3 pixels / mm


public class MyApp extends PaperTouchScreen {

    void settings(){
        setDrawAroundPaper();
        setDrawingSize(297, 210);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.cfg", 297, 210);
    }

    void setup() {
    }

    void drawAroundPaper(){
        clear();
	noStroke();

	ArrayList<DepthPoint> points = ((KinectTouchInput) touchInput).projectDepthData3D((ARDisplay ) getDisplay(), screen);

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
	    if(c == DepthAnalysis.INVALID_COLOR){
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
