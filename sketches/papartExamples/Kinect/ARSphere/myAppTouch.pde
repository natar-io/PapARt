import fr.inria.papart.depthcam.*;
import toxi.geom.*;



public class MyApp  extends PaperTouchScreen {

    float radius1 = 38f;

  
    void setup(){
	setDrawingSize(297, 210);
	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
    }

    void draw(){
	beginDraw3D();

	KinectTouchInput kTouchInput = (KinectTouchInput) touchInput;


	ArrayList<TouchPoint> touchs3D = new ArrayList<TouchPoint>(kTouchInput.getTouchPoints3D());

	//     if(touchs3D.isEmpty())
	// 	return;
	// TouchPoint tp = touchs3D.get(0);


	for(TouchPoint tp : touchs3D){
	    
	    ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();

	    if(depthDataElements.size() == 0)
		continue;
	    //		return;

	    // First point is the most forward. 

	    // int k = 0;
	    // PVector pt1 = TouchInput.NO_INTERSECTION;
	    // while(pt1 == TouchInput.NO_INTERSECTION && k < depthDataElements.size()-3){
	    // 	DepthDataElement dde1 = depthDataElements.get(k++);
	    // 	pt1 = kTouchInput.projectPointToScreen(screen, display, dde1);
	    // }

	    // if(pt1 == TouchInput.NO_INTERSECTION)
	    // 	continue;

	    // fill(200, 0, 0);

	    // pushMatrix();
	    // translate(pt1.x, pt1.y, pt1.z);
	    // ellipse(0, 0, 15, 15);
	    // popMatrix(); 
	    
	    //	ellipse(center1.x, center1.y, 12, 12);

	    PVector mean = new PVector();
	    PVector normalMean = new PVector();
	    Vec3D kinectMean = new Vec3D();


	    int k = 0;
	    for(DepthDataElement dde : depthDataElements){
		Vec3D v = dde.projectedPoint;
		noStroke();
		

		PVector pt = kTouchInput.projectPointToScreen(screen, display, dde);
		int nbNeighbours = dde.neighbourSum;
		if(pt == TouchInput.NO_INTERSECTION)
		    continue;	

		if(isHand(dde.pointColor)){
		    continue;
		}

		mean.add(pt);
		k++;

		setColor(dde.pointColor, 255);

		//setNormalColor(dde.normal);
		// normalMean.add(new PVector(dde.normal.x,
		// 			   dde.normal.y,
		// 			   dde.normal.z));

		kinectMean.addSelf(dde.kinectPoint);


		// Vec3D normal = dde.normal.copy().scaleSelf(radius1 );
		// Vec3D center2 = new Vec3D(normal.x + pt.x, 
		// 			      normal.y + pt.y,
		// 			      normal.z + pt.z);
		
		// //	    setColor(dde.pointColor, 100);
		// // setNormalColor(dde.normal);
		// float dist = center1.distanceTo(center2);
		
		// if(dist > 300) 
		// 	return;

		    pushMatrix();
		    translate(pt.x, pt.y, pt.z);
		    ellipse(0, 0, 4, 4);
		    popMatrix();
	    }

	    mean.mult(1f/k);
	    normalMean.mult(1f/k);
	    kinectMean.scaleSelf(1f/k);

	    pushMatrix();
	    translate(mean.x,
		      mean.y, 
		      mean.z );
	    
	    translate(-kinectMean.x / 20f,
		      -kinectMean.y / 20f,
		      0f);
	    
	    fill(0, 255.0, 0);
	    //	    sphere(radius1);
	    popMatrix();

    }

	endDraw();
    }


    boolean isHand(int pxColor){

	int handColor = #4c1a16;

	colorMode(HSB, 180, 100, 100);

	float hueHand = hue(handColor);
	float satHand = brightness(handColor);
	float brightHand = hue(handColor);

	float huePx = hue(pxColor);
	float satPx = brightness(pxColor);
	float brightPx = hue(pxColor);
	
	float hueDist = hueDist(huePx, hueHand);
	float satDist = abs(satHand - satPx);
	float brightDist = abs(brightHand - brightPx);

	return  hueDist < hValue 
	    && satDist < sValue;
	    //	    && abs(brightHand - brightPx) < bValue;

    }

    float hueDist(float hue1, float hue2){
	float d1 = abs(hue1 - hue2); 
	float d2 = abs(abs(hue1 - 180) - hue2);
	float d3 = abs(abs(hue2 - 180) - hue1);

	return min(min(d1, d2), d3);
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
    
}
