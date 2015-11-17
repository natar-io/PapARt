import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.drawingapp.DrawUtils;

int towerPowerUpThreshold = 40;
float levelPixelRatio = 15;

class MissileLauncher implements TouchPointEventHandler{

    float DEFAULT_CREATION_TIMEOUT = 3000;

    int lastCreation = 0;
    float creationTimeout = DEFAULT_CREATION_TIMEOUT;
    float power = 900;
    float size = 1.5f;

    float launchAmount = 0;
    float maxLaunchAmount;
    float minLaunchAmount = 5;

    Player1 player;
    float drawingSizeY;

    int level = 1;

    PVector position;
    //    ColorDetection colorDetection2;


    MissileLauncher(Player1 player, Touch touch){
	this.player = player;
	this.position = touch.position.get();
	
	// colorDetection2 = new ColorDetection(player, 
	// 				    new PVector((int) position.x + 20, 
	// 						(int) position.y + 40));
	// colorDetection2.setCaptureOffset(playerPaperOffset);
	// colorDetection2.setInvY(true);
	// colorDetection2.setCaptureSize(65, 5);
	// colorDetection2.initialize();


	drawingSizeY = player.getDrawingSize().y;
    }

    float speedUp;

    void tryLaunch(Touch startTouch){
	updatePos(startTouch);

	//	speedUp = colorDetection2.computeOccurencesOfColor(game.getObjectColor(2), towerPowerUpThreshold) * towerRateRatio;
	//	speedUp = constrain(speedUp, 0, DEFAULT_CREATION_TIMEOUT - 300);
	//	creationTimeout = DEFAULT_CREATION_TIMEOUT - speedUp;

	//	int nb1 = 5 + colorDetection2.computeOccurencesOfColor(game.getObjectColor(1), towerPowerUpThreshold);

	level = 1 ; // (int) ((millis() / 12000.)  % 3);

	//	level =  1 + (int) (nb1 / levelPixelRatio);

	if(!canCreateMissile())
	    return;
	
	Missile missile = createMissile(startTouch);
	launchForward(missile, startTouch);
	lastCreation = millis();
    }

    void updatePos(Touch touch){
	this.position = touch.position.get();
    }

    void updateMaxLaunchAmount(){
	maxLaunchAmount = size * power / 1000;
    }

    void updateMinLaunchAmount(){
	maxLaunchAmount = size * power / 10000;
    }

    private boolean canCreateMissile(){
	boolean hasMissileLeft = player.nbMissiles < player.maxMissiles;
	boolean isTimeOK = lastCreation + creationTimeout < millis();
	//	boolean hasEnough = launchAmount >= minLaunchAmount;

	return isTimeOK;

	//	return hasMissileLeft && isTimeOK && hasEnough;
    }
    
    Missile createMissile(Touch touch){

	if(!noCameraMode)
	    touch.invertY(drawingSizeY);
	PVector localPos = touch.position;
	PVector posFromGame = player.gameCoord(localPos);

	if(!noCameraMode)
	    touch.invertY(drawingSizeY);

	Missile missile = new Missile(player, player.ennemi, posFromGame, this.level);
	missile.setCreationPos(touch.position);
	missiles.add(missile);
	player.nbMissiles++;
	launchAmount = 0;
	return missile;

	// // Ennemy castle as target. 
	// PVector ennemiCastle = ennemi.getTargetLocation();
	// missile.setGoal(ennemiCastle, random(200, 250));
    }

    void launchForward(Missile missile, Touch touch){
	touch.invertY(drawingSizeY);
	PVector localPos = touch.position;

	// Forward direction

	if(noCameraMode){
	    localPos.add(new PVector(2000, 0));
	} else {
	    localPos.add(new PVector(0, 200));
	}

	PVector posFromGame = player.gameCoord(localPos);
	touch.invertY(drawingSizeY);

	missile.setGoal(posFromGame, power * level);  
    }

    void launch(Missile missile, Touch touch){
	touch.invertY(drawingSizeY);
	PVector localPos = touch.position;
	PVector posFromGame = player.gameCoord(localPos);
	touch.invertY(drawingSizeY);

	// TODO: Set power...
	missile.setGoal(posFromGame, power);
    }

    void drawSelf(PGraphicsOpenGL g){

	g.pushMatrix(); 
	g.translate(0,-10);
	//	colorDetection2.drawCaptureZone();
	g.popMatrix();

	// DEBUG
	// int nb1 = colorDetection2.computeOccurencesOfColor(game.getObjectColor(1), towerPowerUpThreshold);
	// int nb2 = colorDetection2.computeOccurencesOfColor(game.getObjectColor(2),  towerPowerUpThreshold);
	// g.ellipse(position.x,
	// 	  position.y,
	// 	  nb1 * 5,
	// 	  nb2 * 5);

	g.noFill();

	int size =  10 + level * 5;

	g.strokeWeight(level + 2);

	if(level == 1){
	    g.stroke(#17CB60);
	}
	if(level == 2){
	    g.stroke(#585BE0);
	}
	if(level == 3){
	    g.stroke(#9361EA);
	}
	if(level == 4){
	    g.stroke(#ED9851);
	}
	if(level >= 4){
	    g.stroke(#F932FA);
	}

	g.ellipseMode(CENTER);
	g.ellipse(position.x,
		  position.y,
		  size, size + speedUp / 60f);


    }

    public void delete(){

    }
}
