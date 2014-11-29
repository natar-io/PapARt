
Player1 player1;

// GREEN
public class Player1  extends PaperTouchScreen {
   
    PVector castleLocation = new PVector(120 , 85); 
    Castle castle;
    Player1 ennemi = null;
    int playerColor;

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player1.cfg",
			playerBoardSize.x, playerBoardSize.y);

	player1 = this;

	castle = new Castle(castleLocation.x, castleLocation.y);

	playerColor = color(159, 168, 143);
    }
 
    public void checkEnnemi(){
	if(this == player1)
	    ennemi = player2;
	if(this == player2)
	    ennemi = player1;
    }


    public void updateInternals(){
	checkEnnemi();
    }

    public void draw(){

	updateInternals();
	
	beginDraw2D();
	clear();

	setLocation(-20, -7, 0);

	drawCastle();
	checkTouch();

	endDraw();
    }
    
    void drawCastle(){
	fill(playerColor);
	ellipseMode(CORNER);
	ellipse( castleLocation.x,
		 castleLocation.y,
		  55, 55);
    }


    float ellipseSize = 5;
    void checkTouch(){
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
	    
	    if(!t.is3D && t.touchPoint.isUpdated == false){
		t.invertY(drawingSize.y);
		createMissile(t.position);
	    }
	}
    }

    int lastCreation = 0;
    int creationTimeout = 500;
    
    public boolean canCreateMissile(){
	return lastCreation + creationTimeout < millis();
    }
    
    public void createMissile(PVector localPos){
	if(!canCreateMissile())
	    return;

	println("Missile creation");

	lastCreation = millis();
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
	return gameCoord(this.castleLocation);
    }

}
