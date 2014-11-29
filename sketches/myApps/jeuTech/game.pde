Game game;

// GREEN
public class Game  extends PaperTouchScreen {

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

	PVector v1 = getCoordFrom(player1, new PVector(50, 10));

	if(v1 != INVALID_VECTOR){

	    println("v1 " + v1);
	    fill(0, 200, 0);
	    ellipse((int) v1.x, (int) v1.y, 30, 30);

	    stroke(255);
	    strokeWeight(2);
	    line(100, 100, v1.x, v1.y);

	    if (random(1) < 0.05) {
		float sz = random(2,4);
		Missile p = new Missile(100, 100,sz);
		p.setGoal(v1, 30);
		missiles.add(p);
	    }

	}

	// Look at all missiles
	for (int i = missiles.size()-1; i >= 0; i--) {
	    Missile p = missiles.get(i);

	    p.display(currentGraphics);

	if(v1 != INVALID_VECTOR){
	    p.setGoal(v1, 2);
	    
	    // Missiles that leave the screen, we delete them
	    // (note they have to be deleted from both the box2d world and our list
	    if (p.done(v1)) {
		missiles.remove(i);
	    }
	}
	}
	
	endDraw();
    }

}
