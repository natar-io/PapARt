int nbHearts = 0;
int nbAttack = 0;

MyCounter counter; 

ArrayList<PVector> heartCounter = new ArrayList<PVector>();
ArrayList<PVector> attackCounter = new ArrayList<PVector>();

public class MyCounter  extends PaperTouchScreen {

    int height = 250;
    int width = 110;

    ColorDetection[] colorDetections = new ColorDetection[1];
    
    void setup() {
	setDrawingSize(width, height);
	loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);

	counter = this; 

	colorDetections[0] = new ColorDetection(this, new PVector(40, 40));
	colorDetections[0].setCaptureSize(12, 12);
	colorDetections[0].initialize();

    }
    
    void draw() {

	if(noCamera){
	    setLocation(600, 120,0);
	}

	beginDraw2D();

	background(100);
	    
	// colorDetections[0].computeColor();

	// colorDetections[0].drawCaptureZone();

	// fill(colorDetections[0].getColor());

	colorDetections[0].drawSelf();

	// if(Mode.is("PlaceDice")){

	//     fill(100, 100, 100);
	//     noStroke();
	//     rect(0, 0, 40, 0, 215); 
	//     rect(70, 0, 40, 0, 215); 

	//     updateTouch();
	//     checkTouch();
	//     drawFoundDice();
	// } else {

	//     fill(100, 100, 100);
	//     noStroke();
	//     rect(0, 0, 40, 0, 215); 
	//     rect(70, 0, 40, 0, 215); 

	// }

	endDraw();
    }

    void drawFoundDice(){

	for(PVector p : heartCounter){
	    fill(0, 200, 0);
	    ellipse(p.x, p.y, 30, 30);
	}

	for(PVector p : attackCounter){
	    fill(200, 0, 0);
	    ellipse(p.x, p.y, 30, 30);
	}

    }


    ArrayList<PVector> currentList;
    boolean isHeart = false;

    void checkTouch(){
	TouchList touch2D = touchList.get2DTouchs();
	for (Touch t : touch2D) {


	    TouchPoint tp = t.touchPoint;
	    if(tp == null){
		println("TouchPoint null, this method only works with KinectTouchInput.");
		continue;
	    }


	    PVector p = t.position;

	    fill(200);
	    ellipse(p.x, p.y, 10, 10);
	    
	    if(tp.attachedValue == NEW_TOUCH){

		if(! checkHeartOrAttack(p))
		    continue;

		if(touchAlreadyFound(p))
		    continue;
		
		if(tp.getAge(millis()) > 3000){
		    currentList.add(new PVector(p.x, p.y));
		    println("Add !");
		    tp.attachedValue = currentPlayer.id;
		}
	    }
	}
    }

    boolean checkHeartOrAttack(PVector p){

	// lower 40mm heart, over 40 +30 attack, between nothing. 
	if(p.x < 55){
	    currentList = heartCounter;
	    isHeart = true;
	}else {
	    currentList = attackCounter;
	    isHeart = false;
	}
	return true;
    }

    

    // roughly...
    float diceDist = minTokenDist;

    boolean touchAlreadyFound(PVector touchPos){
	for(PVector pos : currentList){
	    if(PVector.dist(touchPos, pos) < diceDist){
		return true;
	    } 
	}
	return false;
    }
    
    
}

