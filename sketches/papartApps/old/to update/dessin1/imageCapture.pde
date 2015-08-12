float captureInit;
boolean isProjectImage = true;
boolean captured = false;
boolean isCapturing = false;
float captureDelay = 0;
float captureTimeout = 800;

float lastCapture;
float captureInterval = 20000;

float touchMouseTimeout = 5000;
float lastTouchMouse = 0;

void enterCapture(){
    captureInit = millis();
    isCapturing = true;
    isProjectImage = false;
}


void checkCapture(){

    if(!useSecondCamera)
	return;

    /////////// Capture every captureInterval if no Tocuh ////////
    if(!isCapturing){

	if(millis() - lastTouchMouse > touchMouseTimeout){
	    float lastCapEllapsed = millis() - lastCapture;
	    if(lastCapEllapsed > captureInterval)
		enterCapture();
	}
	return;
    }

    ////////// If blackout long enougth ///////////
    float elapsedTime = millis() - captureInit;
    if(elapsedTime > captureTimeout){


	////////// Get the image ///////////
	IplImage out = camera2.getView(boardView);
	println("Getting the view...");

	if(out != null){
	    if(viewTexture == null)
		viewTexture = Utils.createTextureFrom(this, out);
	    Utils.updateTexture(out, viewTexture);
	    
	    viewTexture.updateTexture();
	    viewTexture.save("Photo.jpg");
	    println("Saved photo...");
	}else{
	    println("No View !");
	}

	endCapture();
    }

}

void endCapture(){
    lastCapture = millis();
    isProjectImage = true;
    isCapturing = false;
}




void drawCaptureImage(){

    GLGraphicsOffScreen projGraphics = projector.beginDrawOnBoard(camera, markerBoardDraw);
    
    projGraphics.textureMode(NORMAL);
    projGraphics.fill(200);
    projGraphics.noStroke();

    drawImage(projGraphics, 
	      new PVector(0, -60), 
	      new PVector(130, 32),
	      captureImage);

    drawImage(projGraphics, 
	      new PVector(150, -60), 
	      new PVector(130, 32),
	      captureImage);

    drawImage(projGraphics, 
	      new PVector(300, -60), 
	      new PVector(130, 32),
	      captureImage);

    projector.endDrawOnScreen();
    projector.drawScreensOver();


}

void drawImage(GLGraphicsOffScreen g, PVector p, PVector s, PImage image){

    g.pushMatrix();
    
    g.imageMode(CENTER);

    g.translate(p.x, p.y, p.z);
    g.scale(1, -1, 1);

    g.image(image, 0, 0, s.x, s.y);

    g.popMatrix();
}
