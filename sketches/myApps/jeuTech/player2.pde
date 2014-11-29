Player2 player2;

// BLUE
public class Player2  extends Player1 {

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player2.cfg",
			playerBoardSize.x, playerBoardSize.y);

	player2 = this;
    }
 

    // Todo check if this is useful 
    public void resetPos(){
	screen.resetPos();
    }

    public void draw(){

	beginDraw2D();
	clear();

	setLocation(-20, -7, 0);
	fill(143, 162, 186);
	rect(120, 85, 55, 55);

	endDraw();
    }

}
