 // import java.util.*;

float paperOffsetX = -22;
float paperOffsetY = -30;
// float paperOffsetX = -0;
// float paperOffsetY = -0;

PVector playerBoardSize = new PVector(297, 210); // A4 landscape. 
Player1 player1;

PVector playerPaperOffset = new PVector(paperOffsetX, paperOffsetY);


// GREEN
public class Player1  extends PaperTouchScreen {
   
    int castleSize = 55;
    int castleX = 120;
    int castleY = 85;
    Castle castle;

    int maxMissiles = 4;
    int nbMissiles = 0;

    Player1 ennemi = null;
    int playerColor;

    boolean needReset = true;
    int picSize = 24;

   public ArrayList<PVector> look = new ArrayList<PVector>();


    ColorDetection shootLookColorDetection;
    

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player1-big.cfg",
			playerBoardSize.x, playerBoardSize.y);

	player1 = this;
	playerColor = #FA1414; //color(159, 168, 143);
	prepare();
    }



    void prepare(){
	// shootLookColorDetection = new ColorDetection(this, new PVector(300, 130 + 75));
	// shootLookColorDetection.setCaptureOffset(playerPaperOffset);
	// shootLookColorDetection.setInvY(true);
	// shootLookColorDetection.setCaptureSize(75, 75); // millimeters
	// shootLookColorDetection.setPicSize(picSize, picSize);  // pixels 
	// shootLookColorDetection.initialize();

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
	    castle.hp = MAX_HP;
	    castle.hpChanged = true;
	    game.deleteMissiles();

	    // background(ennemi.playerColor);
	    // return;
	}

	beginDraw2D();
	clear();

	setLocation(paperOffsetX, paperOffsetY, 0);

        if(noCameraMode){
         //background(100);
         if(this == player1)
             setLocation(100, height/2 - 80, 0); 
             else 
             setLocation(800, height/2 - 80, 0);   
        }

	checkTouch();
	drawButtons();

	endDraw();
    }

 
    // void updateMissileShape(){
    // 	look.clear();
    // 	float sizeMult = 4.9;
    // 	PImage shootLook = shootLookColorDetection.getImage();
    // 	if(shootLook != null){
    // 	    shootLook.loadPixels();
    // 	    int[] px=  shootLook.pixels;
    // 	    for(int y = 0; y < picSize; y++){
    // 		for(int x = 0; x < picSize; x++){
    // 		    int offset = y * picSize + x;
    // 		    if(brightness(px[offset]) < 104){
    // 			px[offset] = color(255);
    // 			look.add(new PVector((float) x / picSize * sizeMult, sizeMult * (float)y / picSize));
    // 		    } else {
    // 			px[offset] = color(0);
    // 		    }
    // 		}
    // 	    }
    // 	    shootLook.updatePixels();
    // 	    // DEBUG COMMENTED
    // 	    image(shootLook, 100, 100, 50, 50);
    // 	}
    // 	if(look.isEmpty()){
    // 	    look.add(new PVector(picSize /2, picSize /2));
    // 	}
    // }

    
    void drawCastle(PGraphicsOpenGL g){
	if(castle == null) 
	    return;
	castle.display(g);
    }

    Touch aimTouch;
    float ellipseSize = 15;


    void checkTouch(){

	// PImage cameraImage = cameraTracking.getPImage();
	// cameraImage.loadPixels();
	// cameraImage.updatePixels();

	noFill();
	strokeWeight(2);

	for (Touch t : touchList) {
            if (t.is3D) {
		continue;
            } 

	    TouchPoint tp = t.touchPoint; 
	    if(tp == null)
		continue;
	    
	    if(isMissileTower(tp)){
		missileTowerAction(t);
	    } else {
		stroke(100, 100, 100);
		ellipse(t.position.x,
			t.position.y,
			2, 2);
		
	    }
	}

    }

    private boolean isMissileTower(TouchPoint tp){
	return tp.attachedValue == TOWER;
    }
    

    // TODO: action without TouchPoint... 
    // Or with a fake one ?
    private void missileTowerAction(Touch t){

	if(!noCameraMode){

	    // Nothing with too young touches....
	    if(t.touchPoint.getAge(millis()) < 500){
		return;
	    }
	}

	MissileLauncher launcher;
	if(t.touchPoint.attachedObject == null){
	    launcher = new MissileLauncher(this, t);
	    t.touchPoint.attachedObject = launcher;
	}  else {
	    launcher = (MissileLauncher) (t.touchPoint.attachedObject);
	}

	launcher.tryLaunch(t);
	launcher.drawSelf(getGraphics());
    }

    public PVector gameCoord(PVector v){
	return game.getCoordFrom(this, v);
    }

    // TODO: get a list of targets. 
    // Get a target for aiming...
    public PVector getTargetLocation(){
	return castle.getPosPxGame();
    }

}
