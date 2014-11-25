


void compute3DPos(){

    colorMode(RGB, 20, 20, 100);

    background(0);
    pushStyle();

    for(int y = halfSc; y < cameraY; y+= sc) {
    	for(int x = halfSc; x < cameraX; x+= sc) {


    // for(int y = 0; y < cameraY ; y++) {
    // 	for(int x = 0; x < cameraX; x++) {

	    int offset = x + y* cameraX;

	    if(!myMask[offset])
	    	continue;

	    PVector projectedPointProj = scanner.sceenTo3D(decodedX[offset], decodedY[offset]);
	    PVector intersection = scanner.compute3DPoint(projectedPointProj,
							  new PVector(x, y));

	    if(intersection == null) {
		continue;
	    }

	    // TODO: Error in configuration file 
	    if(intersection.z < 200 || intersection.z > 2000){
		//		println("Intersection too far");
	    	continue;
	    }


	    PVector error = scanner.lastError();
	    float errX = error.x;
	    float errY = error.y;
	    PVector p2 = scanner.projector2DViewOf(intersection);

	    if(errX > 20 || errY > 20)
		continue;


	    scannedPoints.add(intersection);
	    
	    // int c = refImage.pixels[offset];
	    // scannedPointsColors.add(new PVector( (float) red(c) / 255f,
	    // 					 (float) green(c) / 255f,
	    // 					 (float) blue(c) / 255f));
	    
	    scannedPointsColors.add(new PVector( errX * 255f / 20f, 
						 errY * 255f / 20f, 
						 0));
	    // noStroke();
	    // fill(errX *2, errY*2, 100);
	    // rect(p2.x, p2.y, sc, sc);
	    // fill(errX *2, errY*2, 50);
	    // rect(decodedX[offset], decodedY[offset], sc, sc);
	}
	
    }


    popStyle();

    savePoints();
}
