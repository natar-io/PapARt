MyStatus myStatus;


public class MyStatus  extends PaperTouchScreen {

  int width = 400;
  int height = 50;

  PFont font;
  void setup() {
    setDrawingSize(width, height);
    //	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
    loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);

    font = loadFont("DejaVuSansMono-Bold-25.vlw");
    myStatus = this;
  }

  void draw() {

    if (noCamera) {
      setLocation(100, 10, 0);
    }


    beginDraw2D();
    colorMode(RGB, 255);
    clear();

background(100);
    // stroke(255);
    // noFill();
    // rect(0, 0, width, height);
    noStroke();


    fill(currentPlayer.getColor());
    drawPlayer(currentPlayer.id);

    fill(100, 100, 100);
    drawDisabledPlayers();


    drawMode();
    
    fill(255);
    stroke(255);
    int textWidth = 41;

    int px = 44;
    int py = 47;
    textFont(font, 10);
    text("Points ", 3, py);

    translate(px, 0, 0);
    for (Player player : playerList) {

      text("J" + player.id + ":" + player.nbPoints +" ", 0, py); 
      translate(textWidth, 0, 0);
    }
    endDraw();
  }


    void drawMode(){
	if(Mode.is("PlaceDice")){
	    fill(0, 100, 0);
	    rect(1, 14, 69, 23);
	}
	
	if(Mode.is("ChooseAction")){
	    fill(100, 100, 176);
	    rect(98, 14, 60, 10);
	}
		
	if(Mode.is("AddTower")){
	    fill(0, 0, 100);
	    rect(78, 26, 43, 11);
	}
	
	if(Mode.is("SpecialAttack")){
	    fill(0, 100, 1);
	    rect(115, 22, 50, 12);
	}
    }
    
//  int beginningOfNumbers = 60;
//  int playerNoWidth = 10;
//  int playerHighlightWidth = 7;

  void drawPlayer(int nb) {

      int beginningOfNumbers = 68;
  int playerNoWidth = 10;
  int playerHighlightWidth = 7;
  
      rect( beginningOfNumbers + nb * playerNoWidth, 
	    4, 
	    playerHighlightWidth, playerHighlightWidth);
  }


  void drawDisabledPlayers() {
    for (int i = playerList.size (); i <  MAX_PLAYERS; i++) {
      drawPlayer(i);
    }
  }

}

