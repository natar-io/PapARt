import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.ColorDetection;
import fr.inria.papart.procam.Utils;

Game game;

PVector gameBoardSize = new PVector(297, 90);
float gameOffsetX = -17;
float gameOffsetY = -20;


PImage cameraImage;

int colorDistDrawing = 10;
int colorDistObject = 10;
int colorNbObject = 5;

// GREEN
public class Game  extends PaperTouchScreen {

    KinectTouchInput kTouchInput;

    ColorDetection[] colorDetections = new ColorDetection[3];
    ColorDetection colorDrawingDetection;
    ColorDetection drawingDetection;

    PVector offset = new PVector(gameOffsetX, gameOffsetY);
    Border topBorder;
    Border botBorder;

    // DEBUG values 
    int miniatureSize = 20;
    PImage miniature;

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/game.cfg",
			playerBoardSize.x, playerBoardSize.y);
	
	PFont font = loadFont("AccanthisADFStd-Bold-48.vlw");
	Button.setFont(font);
	Button.setFontSize(20);

	createCaptures();

	if(DEBUG_TOUCH){
	    cameraImage = createImage((int) cameraTracking.width(),
				      (int) cameraTracking.height(), RGB);
	    miniature = createImage(miniatureSize, miniatureSize, RGB);
	}
	topBorder = new Border(0, 0, 800, 3);
	botBorder = new Border(0, -350, 800, 3);
	game = this;
    }

    float drawingCaptureWidth = 300;
    float drawingCaptureHeight = 305;
    int outputCaptureWidth = 128; 
    int outputCaptureHeight = 128; 

    void createCaptures(){
	colorDetections[0] = new ColorDetection(this, new PVector(80, 60));
	colorDetections[0].setCaptureOffset(offset);
	colorDetections[0].initialize();

	colorDetections[1] = new ColorDetection(this, new PVector(120, 60));
	colorDetections[1].setCaptureOffset(offset);
	colorDetections[1].initialize();

	colorDetections[2] = new ColorDetection(this, new PVector(160, 60));
	colorDetections[2].setCaptureOffset(offset);
	colorDetections[2].initialize();

	// colorDetections[3] = new ColorDetection(this, new PVector(160, 40));
	// colorDetections[3].setCaptureOffset(offset);
	// colorDetections[3].initialize();

	colorDrawingDetection = new ColorDetection(this, new PVector(200, 60));
	colorDrawingDetection.setCaptureOffset(offset);
	colorDrawingDetection.initialize();

	drawingDetection = new ColorDetection(this, new PVector(0, -drawingCaptureHeight));
	drawingDetection.setCaptureOffset(offset);
	drawingDetection.setCaptureSize(drawingCaptureWidth, drawingCaptureHeight);
	drawingDetection.setPicSize(outputCaptureWidth,  outputCaptureHeight);  // pixels 
	drawingDetection.initialize();

    }
    

    public boolean checkBounds(PVector v){
	return (v.x < - 300 || v.x > 550);
    }

    public int getObjectColor(int id){ 
	return colorDetections[id].getColor();
    }
 

    // Todo check if this is useful 
    public void resetPos(){
	screen.resetPos();
    }


    public void draw(){
	if(DEBUG_TOUCH){
	    cameraTracking.getPImageCopyTo(cameraImage);
	}

	if(fixCastles){
	    markerBoard.blockUpdate(cameraTracking, trackingFixDuration);
	}

	setLocation(gameOffsetX, gameOffsetY, 0);

	// We must always step through time!
	box2d.step();
	beginDraw3D();

	clear();

	player1.drawCastle(currentGraphics);
	player2.drawCastle(currentGraphics);
	
	touchList.removeGhosts();

	drawColorDetection();
	findTouchColors();

	updateAttractors();
	updateMissiles();

	// WWALLS DISABLED
	updateWalls();

	doColorAnalysis();
        drawColorAnalysis();
	
	drawPlayerInfos();
	displayBorders();
	noStroke();
	endDraw();
    }

    ArrayList<PVector> drawnZone = new ArrayList<PVector>();
    void doColorAnalysis(){

	if(canDoColorAnalysis()){
	    drawnZone.clear();
	    PImage battleZone = drawingDetection.getImage();
	    if(battleZone != null){
		battleZone.loadPixels();
		int[] px=  battleZone.pixels;

		int colorToFind = colorDrawingDetection.getColor();
		for(int y = 0; y < outputCaptureHeight; y++){
		    for(int x = 0; x < outputCaptureWidth; x++){
			int offset = y * outputCaptureWidth + x;
			if(Utils.colorDist(px[offset], colorToFind, colorDistDrawing)){
			    PVector p = pxToMM(x, y);
			    drawnZone.add(p);
			}
		    }
		}
	    }
	}
    }

    boolean canDoColorAnalysis(){
	if(millis() > colorAnalysisCreationEvent + lastColorAnalysisCreation){
	    lastColorAnalysisCreation = millis();
	    return true;
	}
	return false;
    }

    int lastColorAnalysisCreation = 0;
    int colorAnalysisCreationEvent = 500;

    void drawColorAnalysis(){
	fill(40);
	noStroke();
	for (Touch t : touchList) {
	    if(isAttractor(t)){
		for(PVector p : drawnZone){
		    // Debug
		    // if(PVector.dist(p, t.position) < attractorDistance){
		    // 	fill(180);
		    // }  else {
		    // 	fill(40);
		    // }
		    ellipse(p.x, p.y, drawingDistance *2, drawingDistance *2);
		}
	    }
	}
    }

    PVector pxToMM(int x, int y){
	float outX = (float) x / (float) outputCaptureWidth * drawingCaptureWidth;
	float outY = (1f - ((float) y / (float) outputCaptureHeight)) * drawingCaptureHeight - drawingCaptureHeight;
	return new PVector(outX, outY);
    }



    void updateAttractors(){
	pushMatrix();
	colorMode(RGB, 255);
	translate(0, 0, -3);

	for (Touch t : touchList) {
	    if(isAttractor(t) && isInMiddle(t.position)){

		noFill();
		stroke(getObjectColor(t.touchPoint.attachedValue));
		strokeWeight(2);
		ellipse(t.position.x, t.position.y, attractorDistance *2, attractorDistance*2);

		for(Missile m: missiles){
		    float distToObject = m.getScreenPos().dist(t.position);

		    if(distToObject < attractorDistance){
			for(PVector p : drawnZone){
			    float distToDrawing = m.getScreenPos().dist(p);
			    
			    if(distToDrawing < drawingDistance){
				noFill();
				stroke(getObjectColor(1));
				strokeWeight(3);
				ellipse(t.position.x, t.position.y, 10, 10);
				push(t, m);
			    }
			}
			
		    }
		}
	    }
	}
	popMatrix();
    }
    

    void findTouchColors(){
	for (Touch t : touchList) {
            if (t.is3D) {
		continue;
            } 
	    if(t.position.y > 0) 
		continue;
	    TouchPoint tp = t.touchPoint; 

	    if(DEBUG_TOUCH){
		PVector imCoord = getCameraViewOf(t);
		PImage im = getImageFrom(imCoord, cameraImage, miniature, miniatureSize);
		pushMatrix();
		translate(t.position.x, t.position.y);
		image(im, 40, 40, miniatureSize, miniatureSize);
		popMatrix();
	    }

	    if(tp.attachedValue == -1){
		checkTypeOfTouch(t);
	    }
	    if(isInMiddle(t.position)){
		int v = tp.attachedValue;
		if(v>= 0){
		    float size = 10;
		    fill(colorDetections[v].getColor());
		    ellipse(t.position.x, t.position.y, size, size);
		}
	    }
	}
    }
    
    boolean isInMiddle(PVector v){
	return v.x > 0 && v.x < 300;
    }
    

    
    // TODO: update with all the new detections ?!
    // TODO: tweak these values online with sliders.
    void checkTypeOfTouch(Touch t){
	TouchPoint tp = t.touchPoint; 

	// check only young ones. 
	if(tp.getAge(millis()) < 2000){

	    int[] scores = new int[colorDetections.length];
	    int c = 0;

	    for(ColorDetection colorDetection : colorDetections){
		int col = colorDetection.getColor();
		PVector imCoord = getCameraViewOf(t);
		int k = getColorOccurencesFrom(imCoord, miniatureSize, col, colorDistObject);
		scores[c] = k;
		c++;
	    }

	    int maxIndex = maxIndexOf(scores);
	    if(scores[maxIndex] >= colorNbObject){
		tp.attachedValue = maxIndex;
	    }
	}
    }

    int maxIndexOf(int[] array){
	int maxIndex = 0;
	int maxValue = 0;
	for(int i = 0; i < array.length; i++){
	    int value = array[i];
	    if(value > maxValue){
		maxValue = value;
		maxIndex = i;
	    }
	}
	return maxIndex;
    }




    void updateMissiles(){
	for (int i = missiles.size()-1; i >= 0; i--) {
	    Missile m = missiles.get(i);
	    m.display(currentGraphics);
	    m.update();
	    
	    // Missiles that leave the screen, we delete them
	    // (note they have to be deleted from both the box2d world and our list
	    if (m.done()) {
		missiles.remove(i);
	    }
	}
    }

    void deleteMissiles(){
	for (int i = missiles.size()-1; i >= 0; i--) {
	    Missile m = missiles.get(i);
	    m.forceDeath();
	    missiles.remove(i);
	}
    }


    void updateWalls(){

	// for (Touch t : touchList) {
	//     if(isSpeedUp(t)){
	// 	Wall wall = new Wall(t.position.x, t.position.y, 15);
	// 	walls.add(wall);
	//     }
	// }
	//	Look at all Walls
	for (int i = walls.size()-1; i >= 0; i--) {
	    Wall w = walls.get(i);
	    w.display(currentGraphics);
	    // w.update();
	    // TODO: find a way to remove them...
	    if (w.done()) {
		walls.remove(i);
	    }
	}
	
	if(canCreateWalls()){
	    kTouchInput = (KinectTouchInput) touchInput;
	    kTouchInput.computeOutsiders(true);
	    noStroke();
	    for (Touch t : touchList) {
		if (!t.is3D) {
		    if(isAttractor(t)){
			TouchPoint tp = t.touchPoint;
			ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();
			for(DepthDataElement dde :  depthDataElements){
			    PVector v = kTouchInput.projectPointToScreen(screen, display, dde);
			    
			    if(v != null) {//  && random(1) < 0.01f ){
				//				if(v.x > 50 && v.x < 200 && v.y  < 0){
				    
				    if( PVector.dist(v, t.position) < 20){
					
					// ellipse(v.x, v.y, 15, 15);
					Wall wall = new Wall(v.x, v.y, 4);
					walls.add(wall);
				    }
				    //}   
			    }
			}
		    }	
		}
	    }
	    kTouchInput.computeOutsiders(false);
	}
    }

    boolean canCreateWalls(){
	if(millis() > wallCreationEvent + lastWallCreation){
	    lastWallCreation = millis();
	    return true;
	}
	return false;
    }

    int lastWallCreation = 0;
    int wallCreationEvent = 2000;


    void drawPlayerInfos(){
	pushMatrix();
	translate(-74, 10, 0);
	drawRectangle(1);
	translate(305 + 85, 00, 0);
	drawRectangle(2);
	popMatrix();
    }

    void drawRectangle(int id){
	noFill();
	strokeWeight(3);
	stroke(255);

	pushMatrix();
	rect(0, 0, 61, 76);
	
	stroke(colorDetections[id].getColor());
	translate(2.5f, 2.5f);
	strokeWeight(7);
	rect(0, 0, 56, 71);

	translate(2.5f, 2.5f);
	stroke(255);
	strokeWeight(3);
	rect(0, 0, 51, 66);
	popMatrix();
    }

    void drawColorDetection(){

	pushMatrix();
	noStroke();	
	translate(120, 12, 0);
	for(int i = 0; i < colorDetections.length; i++){
	    colorDetections[i].computeColor();
	    int c =colorDetections[i].getColor();
	    fill(c);
	    rect(0, 0, 10, 10);
	    translate(10, 0, 0);
	}

	colorDrawingDetection.computeColor();
	int c = colorDrawingDetection.getColor();
	fill(c);
	rect(0, 0, 10, 10);
	translate(10, 0, 0);

	popMatrix();
	
	for(ColorDetection colorDetection : colorDetections){
	    // colorDetection.drawSelf();
	    colorDetection.drawCaptureZone();
	}
	colorDrawingDetection.drawCaptureZone();
    }



    void displayBorders(){
	topBorder.display(getGraphics());
	botBorder.display(getGraphics());
    }

}
