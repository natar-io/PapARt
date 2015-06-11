import fr.inria.papart.procam.Utils;

int nbHearts = 0;
int nbAttack = 0;

MyCounter counter; 

int ACTION_TOWER = 1;
int ACTION_ATTACK = 2;
int ACTION_NEXT = 3;
int currentAction = 3;

public class MyCounter  extends PaperTouchScreen {

    int height = 250;
    int width = 95;

    ColorDetection[] heartColors = new ColorDetection[7];
    ColorDetection[] attackColors = new ColorDetection[7];

    ColorDetection[] chooser = new ColorDetection[4];

    PImage okRed, okGreen;

    int[] xOffsetValuesCap = new int[3];
    int yOffset = 20;
    int yStep = 29;

    void setup() {
	setDrawingSize(width, height);
	loadMarkerBoard(sketchPath + "/data/drawing.cfg", width, height);

	counter = this; 

	okRed = loadImage("ok-red.png");
	okGreen = loadImage("ok-green.png");
	xOffsetValuesCap[0] = 8;
	xOffsetValuesCap[1] = 45;
	xOffsetValuesCap[2] = 73;

	initColorDetections();
    }

    void initColorDetections(){

	int xOffset = xOffsetValuesCap[0];

	for(int i = 0; i < heartColors.length; i++){
	    heartColors[i] = new ColorDetection(this);
	    heartColors[i].setInvY(true);
	    heartColors[i].setCaptureSize(12, 12);
	    heartColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    heartColors[i].initialize();
	}

	// bot-left
	chooser[1] = new ColorDetection(this);
	chooser[1].setInvY(true);
	chooser[1].setCaptureSize(12, 12);
	chooser[1].setPosition(new PVector(xOffset, (yOffset + yStep * 7)));
	chooser[1].initialize();
	
	// ref 
	xOffset = xOffsetValuesCap[1];
	chooser[0] = new ColorDetection(this);
	chooser[0].setInvY(true);
	chooser[0].setCaptureSize(12, 12);
	chooser[0].setPosition(new PVector(xOffset, (yOffset)));
	chooser[0].initialize();
	
	// bot-mid
	chooser[2] = new ColorDetection(this);
	chooser[2].setInvY(true);
	chooser[2].setCaptureSize(12, 12);
	chooser[2].setPosition(new PVector(xOffset, (yOffset + yStep * 7)));
	chooser[2].initialize();

	
	xOffset = xOffsetValuesCap[2];

	// bot-right
	chooser[3] = new ColorDetection(this);
	chooser[3].setInvY(true);
	chooser[3].setCaptureSize(12, 12);
	chooser[3].setPosition(new PVector(xOffset, (yOffset + yStep  * 7)));
	chooser[3].initialize();
	
	for(int i = 0; i < attackColors.length; i++){
	    attackColors[i] = new ColorDetection(this);
	    attackColors[i].setInvY(true);
	    attackColors[i].setCaptureSize(12, 12);
	    attackColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    attackColors[i].initialize();
	}

	
    }
    
    void draw() {

	if(noCamera){
	    setLocation(600, 120,0);
	}


	xOffsetValuesCap[0] = 8;
	xOffsetValuesCap[1] = 43;
	xOffsetValuesCap[2] = 73;

	beginDraw2D();

	background(70);
	

	computeCaptures();
	computeAction();
	
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

    void computeAction(){
	int xOffset = xOffsetValuesCap[0];

	chooser[0].computeColor();

	int refC = chooser[0].getColor();
	for(int i = 1; i < 4; i++){

	    chooser[i].setPosition(new PVector(xOffsetValuesCap[i-1], (yOffset + yStep * 7)));

	    
	    chooser[i].computeColor();
	    int c1 = chooser[i].getColor();

	    ////// Debug
	    // pushMatrix();
	    // translate(xOffsetValuesCap[i-1], 40);
	    // chooser[i].drawCapturedImage();
	    //popMatrix();

	    if(Utils.colorDist(c1, refC, 35)){
		currentAction = i;
	    }
	}

	ellipse(3 + xOffsetValuesCap[currentAction-1], 235, 32, 23);
			
    }

    void computeCaptures(){
	nbHearts = 0;
	nbAttack = 0;

	int xOffset = xOffsetValuesCap[0];

	
	for(int i = 0; i < heartColors.length; i++){
	    heartColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    heartColors[i].computeColor();
	}

	xOffset = xOffsetValuesCap[2];
	for(int i = 0; i < attackColors.length; i++){
	    attackColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    attackColors[i].computeColor();
	}

	int refHeart = heartColors[0].getColor();
	for(int i = 1; i < heartColors.length; i++){
	    int c1 = heartColors[i].getColor();
	    if(Utils.colorDist(c1, refHeart, 35)){
		image(okGreen, 33, (8 + yStep * i) , 10, 10);
		nbHearts++;
	    }
	}

	int refAttack = attackColors[0].getColor();
	for(int i = 1; i < attackColors.length; i++){
	    int c1 = attackColors[i].getColor();
	    if(Utils.colorDist(c1, refAttack, 35)){
		image(okRed, 53, (8 + yStep * i) , 10, 10);
		nbAttack++;
	    }
	}
    }
    
    // Debug function :]
    void drawCaptures(){

	
	int xOffset = 8;
	int yOffset = 20;
	int yStep = 29;
	
	for(int i = 0; i < heartColors.length; i++){

	    heartColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    heartColors[i].computeColor();

	    ///// Debug :: The color Found
	    // fill(heartColors[i].getColor());
	    // rect(33, (8 + yStep * i) , 10, 10);

	    ///// Debug :: The image 
	    // pushMatrix();
	    // translate(33, (yStep * i));
	    // heartColors[i].drawCapturedImage();
	    // popMatrix();
	}

	xOffset = 73;

	for(int i = 0; i < attackColors.length; i++){
	    
	    attackColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    attackColors[i].computeColor();
	    
	    ///// Debug :: The color Found
	    // fill(attackColors[i].getColor());
	    // rect(53, (8 + yStep * i) , 10, 10);

	    ///// Debug :: The image 
	    // pushMatrix();
	    // translate(53, (yStep * i ));
	    // attackColors[i].drawCapturedImage();
	    // popMatrix();
	}

    }
    
    
}

