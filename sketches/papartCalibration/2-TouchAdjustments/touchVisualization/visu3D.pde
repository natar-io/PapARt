void draw3DPointCloud(){
    pointCloud.updateWith(kinect);
    pointCloud.drawSelf((PGraphicsOpenGL) g);

    lights();
    stroke(200);
    fill(200);

    KinectDepthData depthData = kinect.getDepthData();

    ArrayList<TouchPoint> touchs = touchDetection.compute(depthData);

    TouchPointTracker.trackPoints(globalTouchList, touchs, millis());

    colorMode(HSB, 20, 100, 100);
    for(TouchPoint touchPoint : globalTouchList){

    	Vec3D position = touchPoint.getPositionKinect();
    	pushMatrix();
    	translate(position.x, position.y, -position.z);

    	fill(touchPoint.getID() % 20, 100, 100);	
    	sphere(3);
    	popMatrix();
    }
    
}
