import fr.inria.papart.procam.Utils;

int nbHearts = 0;
int nbAttack = 0;

MyCounter counter;

int ACTION_TOWER = 1;
int ACTION_ATTACK = 2;
int ACTION_NEXT = 3;
int currentAction = 3;


public class MyCounter  extends PaperScreen {

    int height = 290;
    int width = 80;

    int refHeartColor = 0;
    int refAttackColor = 0;

    int orangeColor1, orangeColor2 = 0;

    ColorDetection[] heartColors = new ColorDetection[6];
    ColorDetection[] attackColors = new ColorDetection[6];

    ColorDetection[] chooser = new ColorDetection[2];

    PImage okRed, okGreen;

    int[] xOffsetValuesCap = new int[2];
    int yOffset = 20;
    int yStep = 38;

    void settings(){
        setDrawingSize(width, height);
        try{
        loadMarkerBoard(sketchPath()  + "/data/interface.svg", width, height);
        }catch(Exception e){
            e.printStackTrace();
        }
	counter = this;
    }

    void setup() {
        okRed = loadImage("ok-red.png");
        okGreen = loadImage("ok-green.png");

        xOffsetValuesCap[0] = 20;
        xOffsetValuesCap[1] = 58;

        initColorDetections();

    }

    void initColorDetections(){

        int xOffset = xOffsetValuesCap[0];

        for(int i = 0; i < attackColors.length; i++){
            attackColors[i] = new ColorDetection(this);
            attackColors[i].setInvY(true);
            attackColors[i].setCaptureSize(12, 12);
            attackColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
            attackColors[i].initialize();
        }

        chooser[0] = new ColorDetection(this);
        chooser[0].setInvY(true);
        chooser[0].setCaptureSize(12, 12);
        chooser[0].setPosition(new PVector(xOffset,
                                           (yOffset + yStep * attackColors.length)));
        chooser[0].initialize();


        xOffset = xOffsetValuesCap[1];

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
        chooser[1].setPosition(new PVector(xOffset,
                                           (yOffset + yStep *  heartColors.length)));
        chooser[1].initialize();
    }
    void drawOnPaper() {

        setLocation(61f, 2.7f, 0);
        if(noCamera){
            setLocation(600, 120,0);
        }

	xOffsetValuesCap[0] = 20;
        xOffsetValuesCap[1] = 58;
        yOffset = 44;
        yStep = 38;
	background(100);

	// Here is where we set the mode.
	// fill(0);
// stroke(200);
// strokeWeight(2);
// rect(0, 0, width, 200);
        computeHeartAttack();
        countAndDrawHeartAttack();
//	drawCapturesDebug();
        // fill(0);
        // stroke(200);
        // strokeWeight(2);
        // rect(0, 200, width, 80);
        computeAction();
    }
    void computeAction(){
        int xOffset = xOffsetValuesCap[0];

        chooser[0].computeColor();
        chooser[1].computeColor();
        if(saveDiceColors){
            refAttackColor = chooser[0].getColor();
            refHeartColor = chooser[1].getColor();
            saveDiceColors = false;
        }
        if(saveOrangeColor){
            orangeColor1 = chooser[0].getColor();
            orangeColor2 = chooser[1].getColor();
            saveOrangeColor = false;
        }
        int c1 = chooser[0].getColor();
        int c2 = chooser[1].getColor();
/////// DEBUG
        for(int i = 0; i < chooser.length; i++){
            chooser[i].setPosition(new PVector(xOffsetValuesCap[i],
                                               (yOffset + yStep * attackColors.length)));
            chooser[i].computeColor();
            // int c1 = chooser[i].getColor();
            ////// Debug
            pushMatrix();
            translate(xOffsetValuesCap[i], yOffset + yStep * attackColors.length -36);
            chooser[i].drawCapturedImage();
            popMatrix();
            // if(Utils.colorDist(c1, refC, 35)){
            //   currentAction = i;
            // }
        }

        if(Utils.colorDist(c1, orangeColor1, 50)){
            // println("Add TowerMode, orange1 found. ");
            Mode.set("AddTower");
            return;
        }
        if(Utils.colorDist(c2, orangeColor2, 50)){
            // println("Add TowerMode, orange1 found. ");
            Mode.set("SpecialAttack");
            return;
        }
        Mode.set("PlaceDice");

    }

    void computeHeartAttack(){
        int xOffset = xOffsetValuesCap[0];
        for(int i = 0; i < attackColors.length; i++){
            attackColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
            attackColors[i].computeColor();
        }

        xOffset = xOffsetValuesCap[1];

        for(int i = 0; i < heartColors.length; i++){
            heartColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
            heartColors[i].computeColor();
        }


    }

    void countAndDrawHeartAttack(){
	nbHearts = 0;
	nbAttack = 0;

	for(int i = 0; i < heartColors.length; i++){
	    int c1 = heartColors[i].getColor();
	    if(Utils.colorDist(c1, refHeartColor, 60)){
		image(okGreen, 40, (14 + yStep * i) , 10, 10);
		nbHearts++;
	    }
	}

	for(int i = 0; i < attackColors.length; i++){
	    int c1 = attackColors[i].getColor();
	    if(Utils.colorDist(c1, refAttackColor, 60)){
		image(okRed, 32, (14 + yStep * i) , 10, 10);
		nbAttack++;
	    }
	}
    }

    // Debug function :]
    void drawCapturesDebug(){


	int xOffset = xOffsetValuesCap[0];

	for(int i = 0; i < heartColors.length; i++){

	    heartColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    heartColors[i].computeColor();

	    ///// Debug :: The color Found
	    // fill(heartColors[i].getColor());
	    // rect(33, (8 + yStep * i) , 10, 10);

	    ///// Debug :: The image
	    pushMatrix();
	    translate(32, (yStep * i) + 39);
	    heartColors[i].drawCapturedImage();
	    popMatrix();

	}

	xOffset = xOffsetValuesCap[1];

	for(int i = 0; i < attackColors.length; i++){

	    attackColors[i].setPosition(new PVector(xOffset, (yOffset + yStep * i)));
	    attackColors[i].computeColor();

	    ///// Debug :: The color Found
	    // fill(attackColors[i].getColor());
	    // rect(53, (8 + yStep * i) , 10, 10);

	    ///// Debug :: The image
	    pushMatrix();
	    translate(53, (yStep * i ) + 40);
	    attackColors[i].drawCapturedImage();
	    popMatrix();
	}

    }


}
