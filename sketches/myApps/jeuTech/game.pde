 import fr.inria.papart.depthcam.*;

Game game;

PVector gameBoardSize = new PVector(297, 90);
float gameOffsetX = -17;
float gameOffsetY = -20;

// GREEN
public class Game  extends PaperTouchScreen {

    KinectTouchInput kTouchInput;

    ColorDetection[] colorDetections = new ColorDetection[4];

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/game.cfg",
			playerBoardSize.x, playerBoardSize.y);

	colorDetections[0] = new ColorDetection(new PVector(70, 55));
	colorDetections[1] = new ColorDetection(new PVector(70 + 30, 55));
	colorDetections[2] = new ColorDetection(new PVector(70 + 100, 55));
	colorDetections[3] = new ColorDetection(new PVector(70 + 100 + 30, 55));

	game = this;
    }

    public int getObjectColor(int id){ 
	return colorDetections[id].getColor();
    }
 

    // Todo check if this is useful 
    public void resetPos(){
	screen.resetPos();
    }

    // void updateCameraImage(){
    // 	cameraTracking.getPImageCopyTo(cameraImage);
    // }

    public void draw(){
	
	//	updateCameraImage();

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


	// if(!fixCastles){
	//     fill(100);
	//     stroke(120);
	//     rect(50, 0, 200, 1000);
	// }

	updateMissiles();
	drawRectColors();
	 updateWalls();

	endDraw();
    }


    void drawRectColors(){

	for(ColorDetection colorDetection : colorDetections){
	    colorDetection.drawSelf();
	}
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

    void updateWalls(){
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

	// kTouchInput = (KinectTouchInput) touchInput;
	// kTouchInput.computeOutsiders(true);
	// noStroke();
	// for (Touch t : touchList) {
	//     if (t.is3D) {
	// 	fill(185, 142, 62);
	//     }
	//     ellipse(t.position.x,
	// 	    t.position.y,
	// 	    10, 10);
	//     TouchPoint tp = t.touchPoint;
	//     ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();
	//     for(DepthDataElement dde :  depthDataElements){
	// 	PVector v = kTouchInput.projectPointToScreen(screen, display, dde);
	// 	if(v != null && random(1) < 0.1f ){
	// 	    if(v.x > 50 && v.x < 200){
	// 		ellipse(v.x, v.y, 15, 15);
	// 		Wall wall = new Wall(v.x, v.y, 10);
	// 		walls.add(wall);
	// 	    }
		    
	// 	}
	//     }
	// }
	//		kTouchInput.computeOutsiders(false);

    }


    class ColorDetection {
	
	PVector size = new PVector(10, 10);
	PVector pos;
	int picSize = 8; // Works better with power  of 2
	int pxNb = picSize * picSize;
	TrackedView boardView;
	int col;
	
	public ColorDetection(PVector pos){
	    this.pos = pos;
	    
	    boardView = new TrackedView(markerBoard, 
					new PVector(pos.x + gameOffsetX, 
						    pos.y + gameOffsetY),
					size,
					picSize, picSize);
	    cameraTracking.addTrackedView(boardView);
	}

	public void update(){
	    computeColor();
	}

	public void drawSelf(){
	    computeColor();

	    pushMatrix();
	    translate(pos.x,
		      pos.y, 1);
	    
	    drawCaptureZone();
	    drawCapturedColor();
	    //	    drawCapturedImage();
	    popMatrix();
	}

	private void drawCapturedImage(){
	    PImage out = cameraTracking.getPView(boardView);
	    if(out != null){
	    	image(out, 0, -picSize -5, picSize, picSize);
	    }
	}

	private void drawCapturedColor(){
	    fill(this.col);
	    noStroke();
	    ellipse(0, -picSize -5, picSize , picSize);
	}

	private void drawCaptureZone(){
	    strokeWeight(4);
	    noFill();
	    stroke(80);
	    rect(0, 0, size.x, size.y);
	}

	private void computeColor(){
	    PImage out = cameraTracking.getPView(boardView);
	    if(out == null)
		return;

	    out.loadPixels();
	    int avgRed = 0;
	    int avgGreen = 0;
	    int avgBlue = 0;
	    for(int k = 0; k < pxNb ; k++){
		int c = out.pixels[k];
		avgRed   += c >> 16 & 0xFF;
		avgGreen += c >> 8 & 0xFF;
		avgBlue  += c >> 0 & 0xFF;
	    }

	    avgRed = (avgRed / pxNb) << 16;
	    avgGreen =  (avgGreen / pxNb) << 8;
	    avgBlue /= pxNb;
	    this.col = 255 << 24 | avgRed | avgGreen | avgBlue;
	}
	public int getColor(){
	    return this.col;
	}

	
    }



}





	// for(int x = 0; x < playerBoardSize.x; x += 50){
	//     for(int y = 0; y < playerBoardSize.y; y += 50){
	// 	PVector v = getCoordFrom(player1, new PVector(x, y));
	// 	if(v != INVALID_VECTOR){
	// 	    line(0, 0, v.x, v.y);
	// 	    //		    ellipse((int) v.x, (int) v.y, 2, 2);
	// 	}
	//     }
	// }
