
///// Warning, not used anymore. 

void draw2DPoints(){
    cam.beginHUD();
    
    KinectDepthData depthData = kinect.getDepthData();

    drawProjectedPoints(depthData);
    drawTouchPoints(depthData);
 
    cam.endHUD();
}

void drawProjectedPoints(KinectDepthData depthData){
    Vec3D[] projPoints = depthData.projectedPoints;
    boolean[] mask2D = depthData.validPointsMask;
    boolean[] mask3D = depthData.validPointsMask3D;
    
    colorMode(RGB, 255);
    
    for(int i = 0; i < Kinect.SIZE; i++){
	Vec3D p = projPoints[i];
	if(p == null)
	    continue;
	int green = mask2D[i] ? 100 : 0;
	int blue = mask3D[i] ? 100 : 0;
	stroke(0, green, blue);
	point(p.x * Kinect.WIDTH, p.y * Kinect.HEIGHT);
    }
}

void drawTouchPoints(KinectDepthData depthData){

    ArrayList<TouchPoint> touchs = touchDetection.compute(depthData);
    TouchPointTracker.trackPoints(globalTouchList, touchs, millis());

    colorMode(HSB, 20, 100, 100);
    for(TouchPoint touchPoint : globalTouchList){
	PVector position = touchPoint.getPosition();
	fill(touchPoint.getID() % 20, 100, 100);
	ellipse(position.x * Kinect.WIDTH, position.y  * Kinect.HEIGHT, 5, 5);
    }

    ArrayList<TouchPoint> touchs3D = touchDetection3D.compute(depthData);
    colorMode(RGB, 255);
    fill(180, 200, 20);
    stroke(180, 200, 20);
    strokeWeight(precision);
    for(TouchPoint touchPoint : touchs3D){
	PVector position = touchPoint.getPosition();
	ellipse(position.x * Kinect.WIDTH, position.y  * Kinect.HEIGHT, 4, 4);
    }

}
