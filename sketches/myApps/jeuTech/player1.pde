float paperOffsetX = -20;
float paperOffsetY = -7;

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

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player1.cfg",
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

	if(castle.hp <= 0){
	    background(ennemi.playerColor);
	    return;
	}

	beginDraw2D();
	clear();

	//	setLocation(-20, -7, 0);

	checkTouch();
	endDraw();
    }
    
    void drawCastle(PGraphicsOpenGL g){
	if(castle == null) 
	    return;
	castle.display(g);
    }


    float ellipseSize = 5;
    void checkTouch(){

	boolean created = false;
	for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
            } else {
                fill(58, 71, 198);
            }

            ellipse(t.position.x,
		    t.position.y,
		    ellipseSize,
		    ellipseSize);
	    
	    if(!t.is3D){
		t.invertY(drawingSize.y);

		if(canCreateMissile()){
		    createMissile(t.position);
		    created = true;
		}
	    }
	}

	if(created) {
	    lastCreation = millis();
	}
    }

    int lastCreation = 0;
    int creationTimeout = 200;
    
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
