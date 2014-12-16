import fr.inria.papart.drawingapp.Button;
import fr.inria.papart.drawingapp.DrawUtils;

int towerPowerUpThreshold = 40;
float levelPixelRatio = 15;

class MissileLauncher implements TouchPointEventHandler{

    int lastCreation = 0;
    float creationTimeout = 1000;
    float power = 700;
    float size = 1.5f;

    float launchAmount = 0;
    float maxLaunchAmount;
    float minLaunchAmount = 5;

    TouchPoint touchPoint;
    Player1 player;
    float drawingSizeY;

    int level = 0;
    //    Button powerButton, sizeButton, launchButton;


    PVector position;

    //    ColorDetection[] colorDetections = new ColorDetection[4];
    ColorDetection colorDetection, colorDetection2;


    MissileLauncher(Player1 player, Touch touch){
	this.touchPoint = touch.touchPoint;
	this.player = player;
	this.position = touch.position.get();
	
	// powerButton = new Button("power", 
	// 		      (int) position.x, 
	// 		      (int) position.y + 40 ,
	// 		      40, 20);
	// sizeButton = new Button("size", 
	// 		      (int) position.x, 
	// 		      (int) position.y + 80 ,
	// 		      40, 20);

	// launchButton = new Button("launch", 
	// 		      (int) position.x, 
	// 		      (int) position.y + 100 ,
	// 		      40, 20);

	// player.getButtons().add(powerButton);
	// player.getButtons().add(sizeButton);
	// player.getButtons().add(launchButton);



	// colorDetection = new ColorDetection(player, 
	// 				    new PVector((int) position.x + 60, 
	// 						(int) position.y + 40));

	// colorDetection.setCaptureOffset(playerPaperOffset);
	// colorDetection.setInvY(true);
	// colorDetection.initialize();

	colorDetection2 = new ColorDetection(player, 
					    new PVector((int) position.x + 20, 
							(int) position.y + 40));
	colorDetection2.setCaptureOffset(playerPaperOffset);
	colorDetection2.setInvY(true);
	colorDetection2.setCaptureSize(80, 10);
	colorDetection2.initialize();


	drawingSizeY = player.getDrawingSize().y;
    }


    void tryLaunch(Touch startTouch){

	power += colorDetection2.computeOccurencesOfColor(game.getObjectColor(2), towerPowerUpThreshold) / 10;
	creationTimeout -= colorDetection2.computeOccurencesOfColor(game.getObjectColor(1), towerPowerUpThreshold) / 1000;

	int nb1 = 5 + colorDetection2.computeOccurencesOfColor(game.getObjectColor(1), towerPowerUpThreshold);

	level =  (int) (nb1 / levelPixelRatio);
	if(level >= 3)
	    level = 3;

	if(!canCreateMissile())
	    return;
	
	Missile missile = createMissile(startTouch);
	launchForward(missile, startTouch);
	lastCreation = millis();
    }

    void checkButtons(Touch touch){
	// this.position.set(touch.position);
	// powerButton.setPosition(touch.position.x, 
	// 			touch.position.y + 40);
	// sizeButton.setPosition(touch.position.x, 
	// 		       touch.position.y + 80);
	// launchButton.setPosition(touch.position.x, 
	// 			 touch.position.y + 100);
	// if(powerButton.isTouched()){
	//     power += 2;
	// }
	// if(sizeButton.isTouched()){
	//     size += 0.2;
	// }
	// if(launchButton.isTouched()){
	//     launchAmount += 0.5;
	// }
	// updateMaxLaunchAmount();
	// 	updateMinLaunchAmount();
    }

    void updateMaxLaunchAmount(){
	maxLaunchAmount = size * power / 1000;
    }

    void updateMinLaunchAmount(){
	maxLaunchAmount = size * power / 10000;
    }

    private boolean canCreateMissile(){
	//	boolean hasMissileLeft = player.nbMissiles < player.maxMissiles;
	boolean isTimeOK = lastCreation + creationTimeout < millis();
	//	boolean hasEnough = launchAmount >= minLaunchAmount;

	return isTimeOK;

	//	return hasMissileLeft && isTimeOK && hasEnough;
    }
    
    Missile createMissile(Touch touch){
	touch.invertY(drawingSizeY);
	PVector localPos = touch.position;
	PVector posFromGame = player.gameCoord(localPos);
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
	localPos.add(new PVector(0, 20));
	PVector posFromGame = player.gameCoord(localPos);
	touch.invertY(drawingSizeY);

	// TODO: Set power...
//	missile.setGoal(posFromGame, power);
	missile.setGoal(posFromGame, power);  
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
	colorDetection2.drawCaptureZone();
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

	if(level == 0){
	    g.stroke(#17CB60);
	}
	if(level == 1){
	    g.stroke(#585BE0);
	}
	if(level == 2){
	    g.stroke(#9361EA);
	}
	if(level == 3){
	    g.stroke(#ED9851);
	}
	if(level >= 4){
	    g.stroke(#F932FA);
	}

	g.ellipse(position.x,
		  position.y,
		  size, size);


    }

    public void delete(){
 	// player.getButtons().remove(powerButton);
	// player.getButtons().remove(sizeButton);
	// player.getButtons().remove(launchButton);
   }
}
