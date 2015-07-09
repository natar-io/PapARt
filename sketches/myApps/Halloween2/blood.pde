
int numDrips = 0;
int maxPointsPerTouch = 3;
int leap = 18; //how far the point travels each iteration also controls opacity
int sizeMax = 80; //how far the point travels each iteration also controls opacity

ArrayList<Drop> drips = new ArrayList<Drop>();

void initBlood(){

    bloodGraphics = (PGraphicsOpenGL) createGraphics(width, height, OPENGL);
    bloodGraphics.beginDraw();
    bloodGraphics.background(0);
    bloodGraphics.endDraw();
}

int lastShot = 0;
int shotDuration = 7000;

void drawBlood(){

    KinectDepthData depthData = kinect.getDepthData();

    Vec3D[] projPoints = depthData.projectedPoints;
    boolean[] mask2D = depthData.validPointsMask;
    boolean[] mask3D = depthData.validPointsMask3D;

    //    g.beginDraw();
    
   // g.colorMode(RGB, 255);
   // g.strokeWeight(precision);

    stroke(100);
    fill(255);

    boolean newImage =  millis() >  lastShot + shotDuration;

    if(newImage){

	lastShot = millis();

	background(0);

	if(perCentChance(10)){
	    g.stroke(255);
	    g.fill(255);
	}	    
	
	for(int y = 0; y < Kinect.HEIGHT; y += precision){
	    for(int x = 0; x < Kinect.WIDTH; x += precision){
		int i = x + y * Kinect.WIDTH;
		
		Vec3D p = projPoints[i];
		if(p == null)
		    continue;
		
		// if(!mask3D[i]) 
		//     continue; 
		
		if(perCentChance(50)){
		    
		    int intensity = (int) (50 + random(200));    
		    PVector screenP = new PVector(p.x * width, 
						  p.y * height);
		    
		    // if(p.z > 1000) {
		    // 	g.stroke(255, 100);
		    // 	g.fill(255, 100);
		    // } else {
			g.stroke(intensity, 30, 30, 100 + random(100));
			g.fill(intensity, 30, 30, 100 + random(100));
			// }
		    


		    int splat = round(random(0,20)); 
		    // ellipse(screenP.x, screenP.y, 10, 10);
		    // point(screenP.x, screenP.y);
		    
		    g.textFont(font, random(sizeMax));
		    g.text(splat, (int) screenP.x, (int) screenP.y); 
		    
		    if(perCentChance(8)){
			drips.add(new Drop((int) screenP.x,
					   (int) screenP.y,
					   (int) (100 + random(100))));
			
		    }
		}
		
	    }
	}
	
    }

    for (Iterator<Drop> it = drips.iterator(); it.hasNext();) {
    	Drop drop = it.next();
    	drop.drip();
    	drop.show((PGraphicsOpenGL) g);
    	drop.tryStop();
    	if(!drop.isMoving){
    	    it.remove();
    	}
    }

    // g.endDraw();

}
