/// Dimensions : 
// 
// Width 
// Border : 5mm
// Key : 12 mm
// interKey : 5mm 
// 
// Height 
// Border : 5mm
// Key : 15mm to 17mm
//


PImage[] numPadImages;
PImage tmpImage;

boolean[] activeNumPadArray;


PImage backImage;


void initNumPad(){

    tmpImage = loadImage(sketchPath + "/images/tmp.png");

    numPadImages = new PImage[11];
    activeNumPadArray = new boolean[11];

    backImage = loadImage(sketchPath + "/images/back.png");

    resetNumPad();
}

void resetNumPad(){
    for(int i = 0; i < numPadImages.length; i++){
	numPadImages[i] = tmpImage;
    }
    numPadImages[0] = lineSel;
    numPadImages[1] = vanishLeft;
    numPadImages[2] = gridImg;
    if(useTablet)
	numPadImages[3] = tabletImg;

    numPadImages[4] = charImg;
    numPadImages[5] = projectImg;

    numPadImages[9] = backImage;

    int[] keys = {0, 1, 2, 3, 4, 5, 9};
    setActiveKeys(keys);
}

void resetFullNumPad(){
    for(int i = 0; i < numPadImages.length; i++){
	numPadImages[i] = tmpImage;
    }
    numPadImages[9] = backImage;
}


void drawNumPadProjector(){

    GLGraphicsOffScreen projGraphics = projector.beginDrawOnBoard(camera, markerBoardInterface);
    
    projGraphics.textureMode(NORMAL);
    //    projGraphics.clear(0);
    projGraphics.fill(200);
    projGraphics.noStroke();
  
    drawNumPad(projGraphics);
 
    projector.endDrawOnScreen();
    projector.drawScreensOver();

}

void drawNumPadCamera(GLGraphicsOffScreen cameraGraphics){

    cameraGraphics.pushMatrix();
    cameraGraphics.modelview.apply(markerBoardInterface.getTransfoMat(camera));
    cameraGraphics.textureMode(NORMAL);
  
    drawNumPad(cameraGraphics);

    cameraGraphics.popMatrix();

}

void drawNumPad( GLGraphicsOffScreen projGraphics){
 
    //    GLGraphicsOffScreen projGraphics = projector.beginDrawOnScreen(screenInterface);

    
    // 1cm high  -- 85 cm on the right
    projGraphics.translate(86, 30, 0);
    
    projGraphics.translate(7.5, 7.5, 11);
      
    //  projGraphics.translate(0, 0, 11);

    // First row  (1, 2, 3)   
    drawKey(projGraphics, numPadImages[0], 0);

    projGraphics.pushMatrix();
      projGraphics.translate(19, 0, 0);
      drawKey(projGraphics, numPadImages[1], 1);
      projGraphics.translate(19, 0, 0);
      drawKey(projGraphics, numPadImages[2], 2);
    projGraphics.popMatrix();

    // Second row  (4, 5, 6)     
    projGraphics.translate(0, 19, 0);
    drawKey(projGraphics, numPadImages[3], 3);

      projGraphics.pushMatrix();
        projGraphics.translate(19, 0, 0);
	drawKey(projGraphics, numPadImages[4], 4);
	projGraphics.translate(19, 0, 0);
	drawKey(projGraphics, numPadImages[5], 5);
      projGraphics.popMatrix();

    // Third row  (7, 8, 9)     
      projGraphics.translate(0, 19, 0);
      drawKey(projGraphics, numPadImages[6], 6);

	projGraphics.pushMatrix();
    	  projGraphics.translate(19, 0, 0);
	  drawKey(projGraphics, numPadImages[7], 7);
	  projGraphics.translate(19, 0, 0);
	  drawKey(projGraphics, numPadImages[8], 8);
	projGraphics.popMatrix();

    // Third row  (/, *)     
      projGraphics.translate(19, 19, 0);
      drawKey(projGraphics, numPadImages[9], 9);

	projGraphics.translate(19, 0, 0);
	drawKey(projGraphics, numPadImages[10], 10);

	projGraphics.noTint();

	//    projGraphics.translate(dx, dy, dz);

}


void setNoActiveKey(){
    setAllActiveKeys(true);
}

void setActiveKey(int key){
    setAllActiveKeys(false);
    activeNumPadArray[key] = true;
}

void setActiveKeys(int[] valid){
    setActiveKeysValue(valid, false);
}

void setNotActiveKeys(int[] valid){
    setActiveKeysValue(valid, true);
}

void setActiveKeysValue(int[] valid, boolean b){
    setAllActiveKeys(b);
    for(int i = 0; i < valid.length; i++){
	activeNumPadArray[valid[i]] = !b;
    }
}

void setAllActiveKeys(boolean active){
    Arrays.fill(activeNumPadArray, active);
}



void drawKey(GLGraphicsOffScreen graphics, PImage image, int num){

    if(activeNumPadArray[num])
	graphics.tint(255, 255);
    else 
	graphics.tint(255, 128);

    graphics.beginShape();
    graphics.texture(image);
    graphics.vertex(-7.5, -7.5, 0, 0, 1);
    graphics.vertex( 7.5, -7.5, 0, 1, 1);
    graphics.vertex( 7.5,  7.5, 0, 1, 0);
    graphics.vertex(-7.5,  7.5, 0, 0, 0);
    graphics.endShape();
}
