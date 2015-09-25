
PVector scenePos = new PVector(0, 0, 0);
float sceneScale = 1.0f;
float sceneRotate = 0f;
float sceneHeight = 0;
		      
float sceneRotateX = 0f;
float sceneRotateY = 0f;
float rotSpeed = 0.8f;
		      
void updateMultiTouch(Screen screen, TouchElement te){


    ArrayList<PVector> fingersPaper2D = te.position2D;
    ArrayList<PVector> fingersPaperSpeed2D = te.speed2D;

    ArrayList<PVector> fingersPaper3D = te.position3D;
    ArrayList<PVector> fingersPaperSpeed3D = te.speed3D;

    // if(currentSecondaryMode != MODE_SCENE3D_PLACEMENT && 
    //    !fingersPaperSpeed2D.isEmpty() && fingersPaperSpeed2D.size() == 2 
    //    && // and if LUMIERE mode and SpotMode
    //    !(currentSecondaryMode == MODE_SCENE3D_LUMIERE1 &&

    // 	 (currentScene.typeLum == Scene.L_SPOT) && 
    // 	 //	 (currentDrawMode == MODE_SCENE_SPOT) && 
    // 	 (fingersPaper3D.size() < 2))){

    // 	PVector v = fingersPaperSpeed2D.get(0);
    // 	PVector v2 = fingersPaperSpeed2D.get(1);

    // 	v.add(v2);
    // 	v.mult(0.5f);

    // 	v.x *= screen.getSize().x;
    // 	v.y *= screen.getSize().y;
		
    // 	v.x = constrain(v.x, -5, 5);
    // 	v.y = constrain(v.y, -5, 5);
    // 	scenePos.add(v);
    // 	scenePos.x = constrain(scenePos.x, -meshScale-5, meshScale+5);
    // 	scenePos.y = constrain(scenePos.y, -meshScale-15, meshScale+10);
    // } 
    

	    
  	    // Translate avg : 

    PVector sum = new PVector();
    for(PVector tr : fingersPaperSpeed2D){
	sum.add(tr);
    }
    
    sum.mult(1f / fingersPaperSpeed2D.size());
    
    //	    println(fingersPaper2D.get(0));
    
    sum.x *= screen.getSize().x;
    sum.y *= screen.getSize().y;
    
    sum.mult(0.5);
    
  	    
    if(currentInterfaceMode == MODE_DESSINER)
	return;



    switch (currentSecondaryMode){
    case MODE_SCENE3D_PLACEMENT:

      
	// if(!fingersPaperSpeed2D.isEmpty()){
	//     sum.mult(2);
	//     userPos.x = constrain(userPos.x + sum.x,
	// 			  - 500, 500);
	//     userPos.y = constrain(userPos.y - sum.y,
	// 			  - 500, 500);
	//     println(userPos);
	// }    

    	break;

    case MODE_SCENE3D_LUMIERE1:

	PVector currentLight = lightPos;
	
	if(!fingersPaper3D.isEmpty()){
	    
	    for(PVector v : fingersPaper3D){
		
		if(!(v.x < -0.2 || v.x > 1.2 || v.y < 0.0 || v.y > 1.2)){
		    currentLight.x = (v.x -0.5) * screen.getSize().x;
		    currentLight.y = (v.y -0.5) * screen.getSize().y;
		    currentLight.z = v.z;
		    break;
		}
	    }
	}
	      
    	break;

    default: 

    // ---------- RST -------------


	if(!fingersPaperSpeed2D.isEmpty() && fingersPaperSpeed2D.size() == 2 && 
	   !fingersPaper2D.isEmpty() && fingersPaper2D.size() == 2){


	    PVector finger1t0 = fingersPaper2D.get(0).get();
	    PVector finger1t1 = fingersPaper2D.get(0);

	    PVector finger2t0 = fingersPaper2D.get(1).get();
	    PVector finger2t1 = fingersPaper2D.get(1);

	    finger1t0.add(fingersPaperSpeed2D.get(0));  // previous position
	    finger2t0.add(fingersPaperSpeed2D.get(1));  // previous position


	    finger1t0.mult(drawSize);
	    finger1t1.mult(drawSize);
	    finger2t0.mult(drawSize);
	    finger2t1.mult(drawSize);


	    float distance = finger1t0.dist(finger2t0);
	    float distance2 = finger1t1.dist(finger2t1);

	    if(distance + distance2 > 80){
	    // Every values needs to be divided by 2... for some reason.

	    float rot =  computeRotation(finger1t0, finger1t1, finger2t0, finger2t1);
	    if(!Float.isNaN(rot)) // &&  abs(rot) > PI / 90f)
		sceneRotate += rot / 1.6f;

	    float scale = computeScale(finger1t0, finger1t1, finger2t0, finger2t1);

	    if(!Float.isNaN(scale)) //  &&  abs(scale) > 0.8)
		sceneScale *= (scale - 1f) / 2f + 1;

	    // PVector translate = computeTranslate(finger1t0, finger1t1, finger2t0, finger2t1);

	    // scenePos.x += translate.x / 2f ;
	    // scenePos.y += translate.y / 2f;
	    
	    // scenePos.add(translate);


	    sceneScale = constrain(sceneScale, 0.4, 3);
	    println(sceneScale);

	    scenePos.x = constrain(scenePos.x, -200, 200);
	    scenePos.y = constrain(scenePos.y, -200, 200);

	    }
	}

	// TODO: remove speed and use previous Position
	if(!fingersPaperSpeed2D.isEmpty()){
	    // PVector finger1t0 = fingersPaper2D.get(0).get();
	    // PVector finger1t1 = fingersPaper2D.get(0);
	    
	    // sceneRotateY += fingersPaperSpeed2D.get(0).x * rotSpeed;
	    // sceneRotateX -= fingersPaperSpeed2D.get(0).y * rotSpeed;

	    // 50 cm both ways 
	    scenePos.x = constrain(scenePos.x + sum.x, -200, 200);
	    scenePos.y = constrain(scenePos.y + sum.y, -200, 200);


	    // userPos.x += fingersPaperSpeed2D.get(0).x * drawSize.x;
	    // userPos.y -= fingersPaperSpeed2D.get(0).y * drawSize.y;
	    
	// ------------- FIN RST -------------
	}


    } // switch currentSecondaryInterfaceMode

}



void setTransformations(GLGraphicsOffScreen paperScreen){
    //    paperScreen.scale(sceneScale);

    paperScreen.translate(scenePos.x, scenePos.y, scenePos.z - sceneHeight);

    paperScreen.scale(sceneScale);


    // paperScreen.rotateX(sceneRotateX);
    // paperScreen.rotateY(sceneRotateY);

    //    paperScreen.rotateZ(sceneRotate);



}

float computeRotation(PVector f1p0, PVector f1p1, PVector f2p0, PVector f2p1){

    PVector previousDirection = PVector.sub(f1p0, f2p0);
    PVector currentDirection = PVector.sub(f1p1, f2p1);
    previousDirection.normalize();
    currentDirection.normalize();
    
    float cos = currentDirection.dot(previousDirection);
    float angle = acos(cos);

    PVector sin = currentDirection.cross(previousDirection);
    if( sin.z < 0 )
    	angle = -angle;
    return angle;
}

float computeScale(PVector f1p0, PVector f1p1, PVector f2p0, PVector f2p1){
    
    PVector tmp1 = f1p1.get();
    PVector tmp2 = f1p0.get();

    tmp1.sub(f2p1);
    tmp2.sub(f2p0);
    return tmp2.mag() / tmp1.mag() ;
}

PVector computeTranslate(PVector f1p0, PVector f1p1, PVector f2p0, PVector f2p1){

    PVector previousCenter = new PVector((f1p0.x + f2p0.x) / 2f,
					 (f1p0.y + f2p0.y) / 2f);

    PVector currentCenter = new PVector((f1p1.x + f2p1.x) / 2f,
					(f1p1.y + f2p1.y) / 2f);

    return PVector.sub(previousCenter, currentCenter);
}

