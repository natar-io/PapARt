MyStatus myStatus;


public class MyStatus  extends PaperTouchScreen {

    int width = 250;
    int height = 40;
    
    void setup() {
	setDrawingSize(width, height);
	//	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
	loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);

	myStatus = this;	
    }

    void draw() {
	beginDraw2D();
	colorMode(RGB, 255);
	clear();

	// stroke(255);
	// noFill();
	// rect(0, 0, width, height);
	noStroke();
	

	fill(255);
	drawPlayer(currentPlayer.id);

	fill(100, 0, 0);
	drawDisabledPlayers();

	
	fill(0, 100, 0);
	drawThrowDice();
	fill(0, 0, 100);
	drawEndTurn();


	fill(255);
	
	int textWidth = 44;

	int px = 51;
	int py = 30;
	text("Points ", 2, py);
	
	for(Player player : playerList){

	    text("J " + player.id + ", " + player.nbPoints +" ",
		 textWidth + px * + player.id,
		 py); 
	}

	
	endDraw();

    }


    int beginningOfNumbers = 60;
    int playerNoWidth = 10;
    int playerHighlightWidth = 7;
    
    void drawPlayer(int nb){
	rect( beginningOfNumbers + nb * playerNoWidth,
	      0,
	      playerHighlightWidth, playerHighlightWidth); 
    }

    
    void drawDisabledPlayers(){
	for(int i = playerList.size(); i <  MAX_PLAYERS; i++){
	    drawPlayer(i);
	}
    }
    

    
    void drawThrowDice(){
	rect(34, 10, 45,10); 
    }

    void drawEndTurn(){
	rect(84, 10, 37,10); 
    }

}
