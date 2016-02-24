import fr.inria.papart.depthcam.*;
import fr.inria.papart.procam.ColorDetection;
import fr.inria.papart.procam.Utils;

import java.awt.Robot;

Game game;

PVector gameBoardSize = new PVector(297, 90);
float gameOffsetX = 0;
float gameOffsetY = 0;

float colorCaptureY = 55;

PImage cameraImage;

int colorDistDrawing = 10;
int colorDistObject = 10;
int colorNbObject = 5;

// GREEN
public class Game  extends PaperTouchScreen {

    ColorDetection[] colorDetections = new ColorDetection[3];
    ColorDetection colorDrawingDetection;
    ColorDetection drawingDetection;

    PVector offset = new PVector(gameOffsetX, gameOffsetY);
    Border topBorder;
    Border botBorder;

    // DEBUG values
    int miniatureSize = 20;
    PImage miniature;

    Robot robot;

    void settings(){
	setResolution(3);
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/game.svg",
			playerBoardSize.x, playerBoardSize.y);
        setDrawAroundPaper();
    }

    void setup(){

	PFont font = loadFont("AccanthisADFStd-Bold-48.vlw");
	Button.setFont(font);
	Button.setFontSize(20);

	createCaptures();

	if(DEBUG_TOUCH){
	    cameraImage = createImage((int) cameraTracking.width(),
				      (int) cameraTracking.height(), RGB);
	    miniature = createImage(miniatureSize, miniatureSize, RGB);
	}
	topBorder = new Border(-400, 0, 2000, 3);
	botBorder = new Border(-400, -350, 2000, 3);
	game = this;


	if (!noCameraMode) {
	    markerBoard.setDrawingMode(cameraTracking, false, 20);
	    markerBoard.setFiltering(cameraTracking, 44, 1.6);

	} else {

	    createEnnemies();
	}

	try{
	    robot = new Robot();
	}catch(Exception e){}

    }


    ArrayList<Ennemi> ennemies = new ArrayList<Ennemi>();
    void createEnnemies(){
	Ennemi ennemi = new Ennemi(new PVector(250, -200));
	ennemies.add(ennemi);
    }

    float drawingCaptureWidth = 300;
    float drawingCaptureHeight = 305;
    int outputCaptureWidth = 128;
    int outputCaptureHeight = 128;

    void createCaptures(){
	colorDetections[0] = new ColorDetection(this);
	colorDetections[0].setPosition(new PVector(80, colorCaptureY));
	colorDetections[0].setCaptureOffset(offset);
	colorDetections[0].initialize();

	colorDetections[1] = new ColorDetection(this);
	colorDetections[1].setPosition(new PVector(120, colorCaptureY));
	colorDetections[1].setCaptureOffset(offset);
	colorDetections[1].initialize();

	colorDetections[2] = new ColorDetection(this);
	colorDetections[2].setPosition(new PVector(160, colorCaptureY));
	colorDetections[2].setCaptureOffset(offset);
	colorDetections[2].initialize();

	// colorDetections[3] = new ColorDetection(this, new PVector(160, 40));
	// colorDetections[3].setCaptureOffset(offset);
	// colorDetections[3].initialize();

	colorDrawingDetection = new ColorDetection(this);
	colorDrawingDetection.setPosition(new PVector(200, colorCaptureY));
	colorDrawingDetection.setCaptureOffset(offset);
	colorDrawingDetection.initialize();

	drawingDetection = new ColorDetection(this);
	drawingDetection.setPosition(new PVector(0, -drawingCaptureHeight));
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


    public void drawAroundPaper(){

	// if(random(1000) < 1){
	//     robot.mouseMove( (int) random(100), (int) random(100));
	// }

	if(DEBUG_TOUCH){
	    cameraTracking.getPImageCopyTo(cameraImage);
	}

	if(fixBoards || fixGame){
	    markerBoard.blockUpdate(cameraTracking, trackingFixDuration);
	}

	setLocation(gameOffsetX, gameOffsetY, 0);


	PVector noCameraLocation = new PVector(width/2, height - 200);
        if(noCameraMode){
	    setLocation(noCameraLocation.x,
			noCameraLocation.y,
			0);
        }

	// We must always step through time!
	box2d.step();

//	beginDraw3D();
	// clear();
//	background(0);

	if(noCameraMode){
	    rect(0, 0, 20, 30);

	    if(Mode.is("wall")){
		if(mousePressed){
		    Wall w = new Wall(mouseX - noCameraLocation.x,
				      mouseY - noCameraLocation.y,
				      10);
		    w.lifeTime = 1000000;
		    walls.add(w);
		}
	    }


	} else{
	    //	    touchList.removeGhosts();
	}

	drawColorDetection();
	findTouchColors();


	// TODO: something with attractors ?!
	//	updateAttractors();
	updateMissiles();


	if(!noCameraMode){
	    updateWalls();
	    doColorAnalysis();
	}

        drawDrawnZone();
	updateDrawnZone();

	drawWalls();
	drawEnnemies();

	//	drawPlayerInfos();
	//	displayBorders();
	noStroke();
//	endDraw();
    }

    void addTouch(){

    }


    void drawEnnemies(){
	for (int i = ennemies.size()-1; i >= 0; i--) {
	    Ennemi ennemi = ennemies.get(i);
	    ennemi.display(currentGraphics);

	    if (ennemi.done()) {
		ennemies.remove(i);
	    }
	}
    }


    ArrayList<PVector> drawnZone = new ArrayList<PVector>();
    ArrayList<PVector> drawnZone2 = new ArrayList<PVector>();

    void doColorAnalysis(){


        if(canDoColorAnalysis()){
	    drawnZone.clear();
	    drawnZone2.clear();



	    PImage battleZone = drawingDetection.getImage();
	    if(battleZone != null){
		battleZone.loadPixels();
		int[] px=  battleZone.pixels;


		int colorToFind = colorDrawingDetection.getColor();
		int colorToFind2 = colorDetections[2].getColor();

		for(int y = 0; y < outputCaptureHeight; y++){
		    for(int x = 0; x < outputCaptureWidth; x++){
			int offset = y * outputCaptureWidth + x;

                        if(Utils.colorDist(px[offset], colorToFind, colorDistDrawing)){
                            PVector p = pxToMM(x, y);
			    drawnZone.add(p);
			}

			if(Utils.colorDist(px[offset], colorToFind2, colorDistDrawing)){
			    PVector p = pxToMM(x, y);
			    drawnZone2.add(p);
			}

		    }
		}
	    }
	}
    }

    boolean canDoColorAnalysis(){
        if(doColorAnalysis){

	if(millis() > colorAnalysisCreationEvent + lastColorAnalysisCreation){
	    lastColorAnalysisCreation = millis();
	    return true;
	}
      }
	return false;
    }

    int lastColorAnalysisCreation = 0;
    int colorAnalysisCreationEvent = 500;

    // void drawColorAnalysis(){
    // 	noStroke();
    // 	for (Touch t : touchList) {
    // 	    if(isAttractor(t)){
    // 		for(PVector p : drawnZone){
    // 		    // Debug
    // 		    // if(PVector.dist(p, t.position) < attractorDistance){
    // 		    // 	fill(180);
    // 		    // }  else {
    // 		    // 	fill(40);
    // 		    // }
    // 		    ellipse(p.x, p.y, drawingDistance *2, drawingDistance *2);
    // 		}
    // 	    }
    // 	}
    // }

    PVector pxToMM(int x, int y){
	float outX = (float) x / (float) outputCaptureWidth * drawingCaptureWidth;
	float outY = (1f - ((float) y / (float) outputCaptureHeight)) * drawingCaptureHeight - drawingCaptureHeight;
	return new PVector(outX, outY);
    }


    void updateDrawnZone(){

	for(PVector p : drawnZone){
	    for(Missile m: missiles){
		float distToDrawing = m.getScreenPos().dist(p);
		if(distToDrawing < drawingDistance){
		    pushUpDown(m, true);
		}
	    }
	}

	for(PVector p : drawnZone2){
	    for(Missile m: missiles){
		float distToDrawing = m.getScreenPos().dist(p);
		if(distToDrawing < drawingDistance){
		    pushUpDown(m, false);
		}
	    }
	}

    }

    void drawDrawnZone(){
	pushMatrix();
	translate(0, 0, -3);
	noStroke();
	for(PVector p : drawnZone){
	    if(test){
		fill(255, 10, 10);
	    }
	    else{
		fill(20);
	    }
	    ellipse(p.x, p.y, 10, 10);
	}

	for(PVector p : drawnZone2){
	    if(test){
		fill(0, 255, 180);
	    }
	    else{
		fill(20);
	    }
	    ellipse(p.x, p.y, 10, 10);
	}
	popMatrix();
    }


    void updateAttractors(){
	pushMatrix();
	colorMode(RGB, 255);
	translate(0, 0, -3);

	//	println("Update attractors");
	for (Touch t : touchList) {
	    //	    println("touch " + t + " "  + t.touchPoint);

	    if(isAttractor(t) && isInMiddle(t.position)){
		noFill();

		if(!noCameraMode)
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

	    if(!noCameraMode && t.position.y > 0)
		continue;
	    TouchPoint tp = t.touchPoint;

	    if(tp == null){
		println("Null tp in game " );
		continue;
	    }

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


	    line(towerLine, 0, towerLine, 200);
	    if(isTower(t) && isInTowerZone(t.position)){
		tryAddEnnemi(t);
	    }

	    // Debug ellipse color detection ?!
	    if(isInMiddle(t.position)){
	    	int v = tp.attachedValue;
	    	if(v>= 0 && v < 2){
	    	    float size = 10;
	    	    fill(colorDetections[v].getColor());
	    	    ellipse(t.position.x, t.position.y, size, size);
	    	}
	    }
	}
    }


    void tryAddEnnemi(Touch t){
	if(ennemies.size() == 0){
	    Ennemi ennemi = new Ennemi(t.position);
	    t.touchPoint.attachedValue = ENNEMI;
	    t.touchPoint.attachedObject = ennemi;
	    ennemies.add(ennemi);
	}
    }

    int towerLine = 250;

    boolean isInMiddle(PVector v){
	return v.x > 0 && v.x < towerLine;
    }

    boolean isInTowerZone(PVector v){
	return v.x >= towerLine;
    }



    // TODO: update with all the new detections ?!
    // TODO: tweak these values online with sliders.
    void checkTypeOfTouch(Touch t){

	if(noCameraMode){
	    TouchPoint tp = t.touchPoint;
	    int id = tp.getID();

	    if(id < 8)
		tp.attachedValue =  TOWER;
	    else
		if(id < 15)
		    tp.attachedValue =  PUSH_UP;
		else
		    if(id < 22)
			tp.attachedValue =  PUSH_DOWN;

	} else {
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


    void drawWalls(){
	for (int i = walls.size()-1; i >= 0; i--) {
	    Wall w = walls.get(i);
	    w.display(currentGraphics);
	    // w.update();
	    // TODO: find a way to remove them...
	    if (w.done()) {
		walls.remove(i);
	    }
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

	// TODO: create something for the NoCameraMode...
	if(canCreateWalls() && !noCameraMode){
	    KinectTouchInput kTouchInput = (KinectTouchInput) touchInput;
	    kTouchInput.computeOutsiders(true);
	    noStroke();
	    for (Touch t : touchList) {
		if(!t.is3D && isAttractor(t)){
		    if(noCameraMode){
			// TODO: no camera mode...
		    } else {
			TouchPoint tp = t.touchPoint;
			ArrayList<DepthDataElementKinect> depthDataElements = tp.getDepthDataElements();
			for(DepthDataElementKinect dde :  depthDataElements){
			    PVector v = kTouchInput.projectPointToScreen(screen, getDisplay(), dde);

			    if(v != null && isInMiddle(v)) {

				// Hack
				if(PVector.dist(v, t.position) < 15){
				    Wall wall = new Wall(v.x, v.y, 3);
				    walls.add(wall);
				}
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
    int wallCreationEvent = 100;


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
