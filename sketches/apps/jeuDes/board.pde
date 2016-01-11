import fr.inria.guimodes.Mode;

MyBoard board;

int NEW_TOUCH = -1;


public class MyBoard  extends PaperTouchScreen {

    int width = 297;
    int height = 210;

    void settings(){
        setDrawingSize(width, height);
	loadMarkerBoard(Papart.markerFolder + "A3-small1.svg", 297, 210);
	board = this;
    }

    void setup() {
		Player p1 = new Player();
	Player p2 = new Player();
	Player p3 = new Player();
	Player p4 = new Player();

	currentPlayer = p1;


	// Modes are for placement of Attacks or Towers only.
	Mode.add("PlaceDice");
	Mode.add("AddTower");
	Mode.add("SpecialAttack");

	Mode.set("PlaceDice");

	// Mode.set("SpecialAttack");
        // Mode.set("ChooseAction");
        // Mode.set("AddTower");

    }

    int maxAmount = 255;
    float nextPlayerAnimationDuration = 1000;

    int lastAction = 0;
    int actionTimeout = 500;

    void drawOnPaper() {

	if(noCamera){
	    setLocation(100, 100, 0);
	}

        // background(120);

	if(millis() > lastAction + actionTimeout)  {
	    colorMode(RGB, 255);
	    clear();
	    noFill();
	    stroke(200);
	    strokeWeight(3);
	    rectMode(CORNER);

	    float animationAmount = ((float)millis() - lastNextPlayer) / nextPlayerAnimationDuration;

	    if(animationAmount < 1)
		fill(animationAmount * maxAmount);

            noFill();
            stroke(200);
            rect(0, 0, drawingSize.x , drawingSize.y);


	    rectMode(CENTER);
	    ellipseMode(CENTER);

	    // for reasons... touch needs to be updated -> was fixed ?
	    updateTouch();

	    drawAttacks();
	    drawPlayers();

	    // TODO: not always
	    countPoints();

	    if(Mode.is("AddTower"))
		checkAddTower();
	    if(Mode.is("SpecialAttack"))
		checkSpecialAttack();

	    lastAction = millis();

	}

    }


    void countPoints(){
	loadPixels();
	for(Player player : playerList){

	    player.nbPoints = 0;
	    int c1 = player.getColor();

	    PGraphicsOpenGL g = getGraphics();
	    int[] px = g.pixels;
	    for(int i = 0; i < px.length; i++){
		if(px[i] == c1){
		    player.nbPoints++;
		}
	    }

	    player.nbPoints /= screen.getDrawSizeX();
	}

    }

    void drawPlayers(){
	noStroke();
	for(Player player : playerList){
	    for(PVector tokenPos : player.tokens){
		drawTower(player, tokenPos);
	    }
	}
    }

    void drawAttacks(){
	noStroke();
	for(SpecialAttack attack : allAttacks){
	    drawAttack(attack.owner, attack);
	}
    }


    void drawTower(Player player, PVector pos){

	int offsetX = 7;
	int offsetY = -17;

	if(player == currentPlayer){

	    fill(player.getTempColor());
	    offsetX = 7;
	    offsetY = -17;
	    rect(pos.x + offsetX,
		 pos.y + offsetY,
		 player.HP + healAmount(),
		 player.HP + healAmount());

	    fill(player.getColor());
	    rect(pos.x + offsetX,
		 pos.y + offsetY,
		 player.HP, player.HP);

	} else {
	    fill(player.getColor());
	    rect(pos.x + offsetX,
		 pos.y + offsetY,
		 player.HP, player.HP);

	    fill(player.getTempColor());
	    offsetX = 7;
	    offsetY = -17;
	    rect(pos.x + offsetX,
		 pos.y + offsetY,
		 player.HP - damageAmount(),
		 player.HP - damageAmount());
	}
    }

    void drawAttack(Player player, PVector pos){

	int offsetX = 7;
	int offsetY = -17;


	if(player == currentPlayer){

	    fill(player.getTempColor());
	    offsetX = 7;
	    offsetY = -17;
	    ellipse(pos.x + offsetX,
		 pos.y + offsetY,
		 (player.HP* attackHPRatio) +  healAmount(),
		 (player.HP* attackHPRatio) +  healAmount());

	    fill(player.getColor());
	    ellipse(pos.x + offsetX,
		    pos.y + offsetY,
		    player.HP * attackHPRatio,
		    player.HP * attackHPRatio);


	} else {
	    fill(player.getTempColor());
	    offsetX = 7;
	    offsetY = -17;

	    fill(player.getColor());
	    ellipse(pos.x + offsetX,
		    pos.y + offsetY,
		    player.HP * attackHPRatio,
		    player.HP * attackHPRatio);

	    ellipse(pos.x + offsetX,
		    pos.y + offsetY,
		    (player.HP* attackHPRatio) - damageAmount(),
		    (player.HP* attackHPRatio) - damageAmount());



	}

    }


    void checkSpecialAttack(){

	TouchList touch2D = touchList.get2DTouchs();
	for (Touch t : touch2D) {
	    PVector p = t.position;
	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }

	    // new touch.
	    if(tp.attachedValue == NEW_TOUCH){
		if(isCloseToAToken(p))
		    continue;

		drawAttack(currentPlayer, p);

		// Bimanual !
		if(tryAddElement){
		    currentPlayer.addAttack(p);
		    tryAddElement = false;
		    addSpecialAttack();
		}
		return;
	    }
	}

    }

    void checkAddTower(){

	TouchList touch2D = touchList.get2DTouchs();
	for (Touch t : touch2D) {
	    PVector p = t.position;
	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }

	    if(Mode.is("AddTower")){
		if(tp.attachedValue == NEW_TOUCH){

		    if(isCloseToAToken(p))
			continue;

		    drawTower(currentPlayer, p);

		    if(tryAddElement){
			currentPlayer.addToken(p);
			tp.attachedValue = currentPlayer.id;
			tryAddElement = false;
			addTower();
		    }
		    return;
		}
	    }
	}
    }

}
