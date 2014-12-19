Player2 player2;

// BLUE
public class Player2  extends Player1 {

    void setup(){
	setDrawingSize( (int) playerBoardSize.x, (int)playerBoardSize.y);
	loadMarkerBoard(sketchPath + "/data/markers/player2-big.cfg",
			playerBoardSize.x, playerBoardSize.y);

	player2 = this;
	playerColor = #2C42FF; // color(143, 162, 186);
	prepare();
    }

     public void reset(){
	castle = new Castle(this);	
	needReset = false;
    }


    // public void draw(){
    // 	updateInternals();
    // 	beginDraw2D();
    // 	clear();
    // 	setLocation(-20, -7, 0);
    // 	drawCastle();
    // 	endDraw();
    // }

}
