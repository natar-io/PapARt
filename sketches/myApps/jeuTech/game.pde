import fr.inria.papart.depthcam.*;

Game game;

// GREEN
public class Game  extends PaperTouchScreen {

    KinectTouchInput kTouchInput;

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/game.cfg",
			playerBoardSize.x, playerBoardSize.y);


	game = this;
    }
 

    // Todo check if this is useful 
    public void resetPos(){
	screen.resetPos();
    }

    public void draw(){

	// We must always step through time!
	box2d.step();

	beginDraw3D();

	clear();

	setLocation(-20, -7, 0);
	fill(159, 168, 143);
	rect(120, 85, 55, 55);

	pushMatrix();
	goTo(player1);
	fill(200, 0, 0);
	translate(30, 10);
	rect(40, 10, 20, 10);
	popMatrix();

	player1.drawCastle(currentGraphics);
	player2.drawCastle(currentGraphics);

	PVector v1 = getCoordFrom(player1, new PVector(50, 10));

	if(v1 != INVALID_VECTOR){

	    fill(0, 200, 0);
	    ellipse((int) v1.x, (int) v1.y, 30, 30);
	    // stroke(255);
	    // strokeWeight(2);
	    // line(100, 100, v1.x, v1.y);

	    // if (random(1) < 0.05) {
	    // 	float sz = random(2,4);
	    // 	Missile p = new Missile(100, 100,sz);
	    // 	p.setGoal(v1, 30);
	    // 	missiles.add(p);
	    // }

	}

	//	Look at all missiles
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

	

	kTouchInput = (KinectTouchInput) touchInput;
	kTouchInput.computeOutsiders(true);
	noStroke();
	for (Touch t : touchList) {
            if (t.is3D) {
                fill(185, 142, 62);
            }

            ellipse(t.position.x,
		    t.position.y,
		    10, 10);

	    TouchPoint tp = t.touchPoint;
	    ArrayList<DepthDataElement> depthDataElements = tp.getDepthDataElements();

	    for(DepthDataElement dde :  depthDataElements){

		PVector v = kTouchInput.projectPointToScreen(screen, display, dde);
		if(v != null && random(1) < 0.1f ){
		    if(v.x > 0 && v.x < 200){
			ellipse(v.x, v.y, 15, 15);
			Wall wall = new Wall(v.x, v.y, 10);
			walls.add(wall);
		    }

		}
	    }

	}
	//	kTouchInput.computeOutsiders(false);

	endDraw();
    }

}
