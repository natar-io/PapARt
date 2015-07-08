

// ProjectiveDeviceP projectorDevice, kinectDevice;
// int frameWidth, frameHeight;

// HomographyCreator homographyCreator;
// PMatrix3D cameraKinectTransform;


// boolean computeScreenPaperIntersection(){

    
    
//     kinectDevice = cameraKinect.getProjectiveDevice();
	
//     projectorDevice = projector.getProjectiveDeviceP();
//     frameWidth = projectorDevice.getWidth();
//     frameHeight = projectorDevice.getHeight();

//     // compute Camera -> Kinect transformation
//     cameraKinectTransform = cameraPaperTransform.get();
//     cameraKinectTransform.invert();
//     cameraKinectTransform.preApply(kinectPaperTransform);


//     // generate coordinates...
//     float step = 0.5f;
//     nbPoints = (int) ((1 + 1f / step) * (1 + 1f / step));
//     homographyCreator = new HomographyCreator(3, 2, nbPoints);
//     homographyCalibration = new HomographyCalibration();

//     int k = 0;
//     for (float i = 0; i <= 1.0; i += step) {
// 	for (float j = 0; j <= 1.0; j += step, k++) {

// 	    PVector screenPoint = new PVector(i, j);
// 	    PVector kinectPoint = new PVector();

// 	    // where the point is on the table. 
// 	    PVector inter = computeIntersection(i, j);

// 	    if(inter == null)
// 		return false;
	    
// 	    // where the point on the table is seen by the Kinect. 
// 	    cameraKinectTransform.mult(inter, kinectPoint);

// 	    // add the point...
// 	    homographyCreator.addPoint(kinectPoint, screenPoint);
// 	}
//     }
    
//     homographyCalibration = homographyCreator.getHomography();
    
//     // PVector inter00 = computeIntersection(0, 0);
//     // PVector interX0 = computeIntersection(1, 0);

//     // if(inter00 == null || interX0 == null)
//     // 	return;
    
//     // println("dist "+ inter00.dist(interX0) + " Intersection " + inter00 + ", " + interX0); 


//     // PVector out1 = new PVector();
//     // cameraKinectTransform.mult(inter00, out1);

//     // PVector out2 = new PVector();
//     // cameraKinectTransform.mult(interX0, out2);

//     // PVector px = kinectDevice.worldToPixel(out1, false);
//     // println("Pixels..." + px);

//     // px = kinectDevice.worldToPixel(out2, false);
//     // println("Pixels 2..." + px);
//     return true;
// }



// PVector computeIntersection(float px, float py){

//     // Create ray from the projector (origin / viewed pixel)
//     // Intersect this ray with the piece of paper. 
//     // Compute the Two points for the ray          
//     PVector originP = new PVector(0, 0, 0);
//     PVector viewedPtP = projectorDevice.pixelToWorldNormP((int) (px * frameWidth), (int) (py * frameHeight));
    
//     // Pass it to the camera point of view (origin)
//     PMatrix3D extr = projector.getExtrinsicsInv();
//     PVector originC = new PVector();
//     PVector viewedPtC = new PVector();
//     extr.mult(originP, originC);
//     extr.mult(viewedPtP, viewedPtC);
    
//     // Second argument is a direction
//     viewedPtC.sub(originC);
    
//     Ray3D ray
// 	= new Ray3D(new Vec3D(originC.x,
// 			      originC.y,
// 			      originC.z),
// 		    new Vec3D(viewedPtC.x,
// 			      viewedPtC.y,
// 			      viewedPtC.z));
    
//     // Intersect ray with Plane 
//     ReadonlyVec3D inter = planeCalibCam.getPlane().getIntersectionWithRay(ray);
    
//     if(inter == null){
// 	println("No intersection :( check stuff");
// 	return null;
//     }
	
//     return new PVector(inter.x(), inter.y(), inter.z());
// }



