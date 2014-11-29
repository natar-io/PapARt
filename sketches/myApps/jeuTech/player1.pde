
Player1 player1;

// GREEN
public class Player1  extends PaperTouchScreen {
   
    PVector castleLocation = new PVector(120 + 55 / 2, 85 + 55 / 2); 
    Castle castle;

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player1.cfg",
			playerBoardSize.x, playerBoardSize.y);

	player1 = this;

	castle = new Castle(castleLocation.x, castleLocation.y);
    }
 

    // Todo check if this is useful 
    public void resetPos(){
	screen.resetPos();
    }

    public void draw(){

	beginDraw2D();
	clear();

	setLocation(-20, -7, 0);
	fill(159, 168, 143);
	
	ellipseMode(CENTER);
	ellipse(120, 85, 55, 55);
	
	endDraw();
    }

}
