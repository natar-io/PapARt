MyBoard board;

public class MyBoard  extends PaperTouchScreen {

    int width = 390;
    int height = 270;
    
    void setup() {
	setDrawingSize(width, height);
	//	loadMarkerBoard(sketchPath + "/data/A3-small1.cfg", 297, 210);
	loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);


	Player p1 = new Player();
	Player p2 = new Player();
	Player p3 = new Player();
	Player p4 = new Player();
	currentPlayer = p1;
	board = this;	
    }

    void draw() {

	if(noCamera){
	    setLocation(100, 100, 0);
	}
	
	beginDraw2D();

	colorMode(RGB, 255);

	clear();

	noFill();
	stroke(200);
	strokeWeight(3);
	rectMode(CORNER);
	rect(0, 0, drawingSize.x , drawingSize.y);
	
	// for reasons...
	updateTouch();
	
	drawPlayers();

	// TODO: not always
	countPoints();
	
	checkTouch();
    
	endDraw();

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
	rectMode(CENTER);
	noStroke();
	for(Player player : playerList){
	    for(PVector tokenPos : player.tokens){
		fill(player.getColor());
		rect(tokenPos.x, tokenPos.y, player.HP, player.HP);
	    }
	}
    }


    int NEW_TOUCH = -1;

    void checkTouch(){

	TouchList touch2D = touchList.get2DTouchs();
	for (Touch t : touch2D) {

	    // draw the touch. 
	    PVector p = t.position;

	    // draw the elements of the Touch

	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }

	    if(tp.attachedValue == NEW_TOUCH){
		fill(200);
		ellipse(p.x, p.y, 10, 10);

		if(tp.getAge(millis()) > 1000){
		    if(currentPlayer.tryAddToken(p)){
			tp.attachedValue = currentPlayer.id;
		    }
		}
	    }

	    
	}



    }
    
}
