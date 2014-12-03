float paperOffsetX = -22;
float paperOffsetY = -30;
// float paperOffsetX = -0;
// float paperOffsetY = -0;

PVector playerBoardSize = new PVector(297, 210); // A4 landscape. 
Player1 player1;

// GREEN
public class Player1  extends PaperTouchScreen {
   
    int castleSize = 55;
    int castleX = 120;
    int castleY = 85;
    Castle castle;

    Player1 ennemi = null;
    int playerColor;

    boolean needReset = true;

    PImage view = createImage(100, 100, RGB);

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player1-big.cfg",
			playerBoardSize.x, playerBoardSize.y);

	player1 = this;
	playerColor = color(159, 168, 143);

    }

    
    public void reset(){
	castle = new Castle(this);	
	needReset = false;
    }

    public void checkEnnemi(){
	if(this == player1)
	    ennemi = player2;
	if(this == player2)
	    ennemi = player1;
    }


    public void updateInternals(){
	if(needReset)  
	    reset();
	checkEnnemi();
	castle.update();
    }

    public void draw(){

	updateInternals();
	if(fixCastles){
	    markerBoard.blockUpdate(cameraTracking, trackingFixDuration);
	}

	if(castle.hp <= 0){
	    background(ennemi.playerColor);
	    return;
	}

	beginDraw2D();
	clear();
	//	background(100);

	setLocation(paperOffsetX, paperOffsetY, 0);

	pushMatrix();
	fill(0, 150, 0, 100);
	translate(120  + 55 / 2, playerBoardSize.y - 20 - 55 /2 , 0);
	ellipseMode(CENTER);
	ellipse(0, 0, 55, 55);
	popMatrix();

	checkTouch();
	endDraw();
    }
    
    void drawCastle(PGraphicsOpenGL g){
	if(castle == null) 
	    return;
	castle.display(g);
    }


    int redThing = 10;
    int nothing = 10;

    float ellipseSize = 5;
    void checkTouch(){

	boolean created = false;

	KinectTouchInput kTouchInput = (KinectTouchInput) touchInput;
	ProjectorDisplay projector = (ProjectorDisplay) display;

	// PImage cameraImage = cameraTracking.getPImage();
	// cameraImage.loadPixels();
	// cameraImage.updatePixels();


	for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
		continue;
            } 
	    
	    TouchPoint tp = t.touchPoint; 
	    if(isMissileTower(tp)){
		created = missileTowerAction(t);
	    } else {
		checkTypeOfTouch(t);
	    }
		
	    fill(255);
	    ellipse(t.position.x,
		    t.position.y,
		    ellipseSize,
		    ellipseSize);
	}
	
	if(created) {
	    lastCreation = millis();
	}
    }

    private boolean isMissileTower(TouchPoint tp){
	return tp.attachedValue == redThing;
    }

    private boolean missileTowerAction(Touch t){
	boolean created = false;
	noFill();
	//  	t.invertY(drawingSize.y);
	if(canCreateMissile()){
	    createMissile(t.position);
	    created = true;
	}
	
	stroke(255);
	ellipse(t.position.x,
		t.position.y,
		40,
		40);
	return created;
    }


    private void checkTypeOfTouch(Touch t){

	TouchPoint tp = t.touchPoint; 
	if(tp.getAge(millis()) < 2000){

	    // TODO: color adjustment, saving & loading ?
	    // Red object. 
	    int col = game.getObjectColor(0);
	    PVector imCoord = getCameraViewOf(t);
	    int k = getColorOccurencesFrom(imCoord, 15, col, 40);
	    
	    if(k >= 10){
		tp.attachedValue = redThing;
	    }
	}
    }


    int lastCreation = 0;
    int creationTimeout = 1000;
    
    public boolean canCreateMissile(){
	return lastCreation + creationTimeout < millis();
    }
    
    public void createMissile(PVector localPos){
	PVector posFromGame = gameCoord(localPos);
	Missile missile = new Missile(this, ennemi, posFromGame);

	PVector ennemiCastle = ennemi.getTargetLocation();
	missile.setGoal(ennemiCastle, random(30, 60));
	missiles.add(missile);
    }

    public PVector gameCoord(PVector v){
	return game.getCoordFrom(this, v);
    }

    // TODO: get a list of targets. 
    // Get a target for aiming...
    public PVector getTargetLocation(){
	return castle.getPos();
    }

}
