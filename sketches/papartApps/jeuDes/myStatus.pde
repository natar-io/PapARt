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
    stroke(255);
    int textWidth = 73;

    int px = 84;
    int py = 42;
    textFont(font, 20);
    text("Points ", 0, py);

    translate(px, 0, 0);
    for (Player player : playerList) {

      text("J" + player.id + ":" + player.nbPoints +" ", 0, py); 
      translate(textWidth, 0, 0);
    }


    endDraw();
  }


  int beginningOfNumbers = 60;
  int playerNoWidth = 10;
  int playerHighlightWidth = 7;

  void drawPlayer(int nb) {
    rect( beginningOfNumbers + nb * playerNoWidth, 
    0, 
    playerHighlightWidth, playerHighlightWidth);
  }


  void drawDisabledPlayers() {
    for (int i = playerList.size (); i <  MAX_PLAYERS; i++) {
      drawPlayer(i);
    }
  }



  void drawThrowDice() {
    rect(34, 10, 45, 10);
  }

  void drawEndTurn() {
    rect(84, 10, 37, 10);
  }
}

