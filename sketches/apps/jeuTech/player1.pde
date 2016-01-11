// import java.util.*;

float paperOffsetX = 0;
float paperOffsetY = 0;
// float paperOffsetX = -0;
// float paperOffsetY = -0;

PVector playerBoardSize = new PVector(297, 210); // A4 landscape.
Player1 player1;

PVector playerPaperOffset = new PVector(paperOffsetX, paperOffsetY);


// GREEN
public class Player1  extends PaperTouchScreen {

    int maxMissiles = 4;
    int nbMissiles = 0;

    Player1 ennemi = null;
    int playerColor;

    boolean needReset = true;
    int picSize = 24;

    public ArrayList<PVector> look = new ArrayList<PVector>();


    ColorDetection shootLookColorDetection;

    void settings(){
        setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/player1-big.svg",
			playerBoardSize.x, playerBoardSize.y);
    }

    void setup(){
	player1 = this;
	playerColor = #FA1414; //color(159, 168, 143);
	prepare();
    }

    void prepare(){
    }


    public void reset(){
	needReset = false;
    }

    public void checkEnnemi(){
	// if(this == player1)
	//     ennemi = player2;
	// if(this == player2)
	//     ennemi = player1;
    }


    public void updateInternals(){
	if(needReset)
	    reset();
	checkEnnemi();
    }

    public void drawOnPaper(){
	updateInternals();

	if(fixBoards){
	    markerBoard.blockUpdate(cameraTracking, trackingFixDuration);
	}

	clear();

	setLocation(paperOffsetX, paperOffsetY, 0);

	// stroke(255);
	// noFill();
	// rect(drawingSize.x /2 - 20 , drawingSize.y / 2,
	//      20, 30);

        if(noCameraMode){
	    noFill();
	    stroke(120);
	    strokeWeight(1);
	    rect(0, 0, drawingSize.x, drawingSize.y);
	    setLocation(20, height/2 - 120, 0);
        }

	checkTouch();
	drawButtons();

    }


    //   void drawCastle(PGraphicsOpenGL g){
    //	if(castle == null)
    //	    return;
    //	g.fill(playerColor);
    //       castle.display(g);
    //    }

    Touch aimTouch;
    float ellipseSize = 15;


    void checkTouch(){

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
		stroke(200, 100, 100);
		ellipse(t.position.x,
			t.position.y,
			15, 15);

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
    //    public PVector getTargetLocation(){
    //	return castle.getPosPxGame();
    //    }

}