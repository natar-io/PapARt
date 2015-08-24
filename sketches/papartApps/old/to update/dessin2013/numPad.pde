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


int numPadActiveIntens = 255;
int numPadPassiveIntens = 40;

PFont numPadFont;
int numPadFontSize = 20;
PVector numPadTextOffset = new PVector();


String numPadTextLine1 = null;
String numPadTextLine2 = null;
String numPadText = null;
PImage numPadImage = null;

void initNumPad(){

    tmpImage = loadImage(sketchPath + "/images/tmp.png");

    numPadImages = new PImage[11];
    activeNumPadArray = new boolean[11];

    backImage = loadImage(sketchPath + "/images/back.png");
    numPadFont = loadFont(sketchPath + "/data/Font/FreeSans-60.vlw");
    numPadFontSize = 20;

    resetNumPad();
}

void resetNumPad(){
    for(int i = 0; i < numPadImages.length; i++){
	numPadImages[i] = tmpImage;
    }
    numPadImages[0] = lineSel;
    numPadImages[1] = gridImg;
    numPadImages[2] = projectImg;

    if(useTablet)
	numPadImages[3] = tabletImg;

    // numPadImages[4] = charImg;


    numPadImages[9] = backImage;

    int[] keys = {0, 1, 2, 9};
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
      


    projGraphics.pushMatrix();

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

    projGraphics.popMatrix();
    // Back to the start 

    if(numPadText != null){

	projGraphics.pushMatrix();
	projGraphics.translate(-2, 73, 0);
	projGraphics.scale(1, -1, 1);
	projGraphics.translate(numPadTextOffset.x,
			       numPadTextOffset.y,0);
	projGraphics.textFont(numPadFont, numPadFontSize);
	projGraphics.text(numPadText, 0, 0);
	projGraphics.popMatrix();
	//	  DecimalFormat decimalFormat = new DecimalFormat("#.##");
	
    }

    if(numPadImage != null){
	projGraphics.pushMatrix();
	projGraphics.translate(-2, 73, 0);
        projGraphics.translate(dx, dy, dz);

	projGraphics.image(numPadImage, 0, 0, 60, 20);
	projGraphics.popMatrix();
	//	  DecimalFormat decimalFormat = new DecimalFormat("#.##");
	
    }

    //        projGraphics.translate(dx, dy, dz);

}


void numPadSetNoText(){
    numPadText = null;
}

void numPadSetOneLineText(String s){
    numPadSetOneLineText(s, 20);
}

void numPadSetOneLineText(String s, int textSize){
    numPadTextOffset.y = 0;
    numPadFontSize = textSize;
    numPadText = s;
}


void numPadSetTwoLinesText(String s, String t){

    numPadTextLine1 = s;
    numPadTextLine2 = t;

    numPadTextOffset.y = -10;
    numPadFontSize = 10;
    numPadText = s + "\n" + t;
}

void numPadSetUpdateFistLine(String s){
    numPadSetTwoLinesText(s, numPadTextLine2);
}

void numPadSetUpdateSecondLine(String t){
    numPadSetTwoLinesText(numPadTextLine1, t);
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
	graphics.tint(255, numPadActiveIntens);
    else 
	graphics.tint(255, numPadPassiveIntens);

    graphics.beginShape();
    graphics.texture(image);
    graphics.vertex(-7.5, -7.5, 0, 0, 1);
    graphics.vertex( 7.5, -7.5, 0, 1, 1);
    graphics.vertex( 7.5,  7.5, 0, 1, 0);
    graphics.vertex(-7.5,  7.5, 0, 0, 0);
    graphics.endShape();
}
