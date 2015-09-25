class Radio extends PaperInterface{


    PImage[] radioImages;
    int currentImage = 0;

    Button nextButton, prevButton, lockButton;

    public Radio(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }


    public void init(){

	radioImages = new PImage[5];

	radioImages[0] = loadImage("radio/radio1.png");
	radioImages[1] = loadImage("radio/radio2.png");
	radioImages[2] = loadImage("radio/radio3.jpg");
	radioImages[3] = loadImage("radio/radio4.jpg");
	radioImages[4] = loadImage("radio/radio5.jpg");

	nextButton = new Button("Next" , 
				275, 170, 
				40, 20);

	prevButton = new Button("Prev" , 
				275, 150, 
				40, 20);

	lockButton = new Button("Lock" , 
				275, 130, 
				40, 20,
				"UnLock");

	nextButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    currentImage = constrain(currentImage +1, 0, radioImages.length);
		    nextButton.reset();
		}
		public void ButtonReleased(){
		}
	    });
	
	prevButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    currentImage = constrain(currentImage -1, 0, radioImages.length);
		    prevButton.reset();
		}
		public void ButtonReleased(){
		}
	    });

	
	lockButton.addListener(new ButtonListener() {
		public void ButtonPressed(){

		}
		public void ButtonReleased(){
		}
	    });



	buttons.add(nextButton);
	buttons.add(prevButton);
	buttons.add(lockButton);

    }


    public void draw(){


	if(!lockButton.isActive)
	    updateMultiTouch(screen, touch);

	GLGraphicsOffScreen g;
	
	/////////////////////////////////////////////////
	////////////// Light management /////////////////
	/////////////////////////////////////////////////
	
	g = screen.getGraphics();
	
	g.beginDraw();
	g.scale(screenResolution);

	g.background(0);
	g.pushMatrix();

	setTransformations(g);

	g.imageMode(CENTER);

	DrawUtils.drawImage(g, radioImages[currentImage % radioImages.length],
			    0, 0,
			    (int) boardSize.x, (int) boardSize.y);
	
	g.popMatrix();

	for(Button b : buttons){
	    b.drawSelf(g);
	}


	g.endDraw();

    }






    PVector scenePos = new PVector(210/2, 297/2, 0);
    float sceneScale = 1.0f;
    float sceneRotate = 0f;
    float sceneHeight = 0;
		      
    float sceneRotateX = 0f;
    float sceneRotateY = 0f;
    float rotSpeed = 0.8f;
	
    void setTransformations(GLGraphicsOffScreen paperScreen){

	paperScreen.translate(scenePos.x, scenePos.y, scenePos.z - sceneHeight);


	//    paperScreen.scale(sceneScale);
	paperScreen.rotateX(sceneRotateX);
	paperScreen.rotateY(sceneRotateY);
	paperScreen.rotateZ(sceneRotate);

	paperScreen.scale(sceneScale);
    }

	      
    void updateMultiTouch(Screen screen, TouchElement te){


	ArrayList<PVector> fingersPaper2D = te.position2D;
	ArrayList<PVector> fingersPaperSpeed2D = te.speed2D;

	ArrayList<PVector> fingersPaper3D = te.position3D;
	ArrayList<PVector> fingersPaperSpeed3D = te.speed3D;



	///////////////////////////////////////////
	// Rotation / Scale / Translate  2 fingers
	///////////////////////////////////////////

	if(!fingersPaperSpeed2D.isEmpty() && fingersPaperSpeed2D.size() == 2 && 
	   !fingersPaper2D.isEmpty() && fingersPaper2D.size() == 2){

	    PVector finger1t0 = fingersPaper2D.get(0).get();
	    PVector finger1t1 = fingersPaper2D.get(0);

	    PVector finger2t0 = fingersPaper2D.get(1).get();
	    PVector finger2t1 = fingersPaper2D.get(1);

	    finger1t0.add(fingersPaperSpeed2D.get(0));  // previous position
	    finger2t0.add(fingersPaperSpeed2D.get(1));  // previous position

	    PVector screenSize  = screen.getSize();
	    finger1t0.mult(screenSize);
	    finger1t1.mult(screenSize);
	    finger2t0.mult(screenSize);
	    finger2t1.mult(screenSize);

	    // Every values needs to be divided by 2... for some reason.

	    float rot =  computeRotation(finger1t0, finger1t1, finger2t0, finger2t1);
	    if(!Float.isNaN(rot)) // &&  abs(rot) > PI / 90f)
		sceneRotate += rot / 3 ;

	    float scale = computeScale(finger1t0, finger1t1, finger2t0, finger2t1);

	    if(!Float.isNaN(scale)) //  &&  abs(scale) > 0.8)
		sceneScale *= (scale - 1f) / 2f + 1;


	    PVector translate = computeTranslate(finger1t0, finger1t1, finger2t0, finger2t1);

	    scenePos.x += translate.x / 2f ;
	    scenePos.y += translate.y / 2f;
	    
	} else {


	    //////////////////////////////////////////
	    //  1 finger movement 
	    //////////////////////////////////////////

	    if(te.position2D.size() == te.speed2D.size()){

		int k = 0;
		PVector sum = new PVector();

		for(int i = 0; i < te.position2D.size() ; i++) {
		    PVector pos = te.position2D.get(i);
		    PVector speed = te.speed2D.get(i);

		    if(pos.x < 0 || pos.x >= 0.8 ||
		       pos.y < 0 || pos.y >= 1)
			continue;
		    k++;
		    sum.add(speed);
		}
	    
		if(k > 0){
		    sum.mult(1f / (float)k);
		    sum.x *= screen.getSize().x;
		    sum.y *= screen.getSize().y;

		    scenePos.add(sum);
		}
	    }    

	}


	    //	    scenePos.add(translate);
	    //	    sceneScale = constrain(sceneScale, 0.1, 5);
	    // scenePos.x = constrain(scenePos.x, -100, 100);
	    // scenePos.y = constrain(scenePos.y, -100, 100);


    }



    float computeRotation(PVector f1p0, PVector f1p1, PVector f2p0, PVector f2p1){

	PVector previousDirection = PVector.sub(f1p0, f2p0);
	PVector currentDirection = PVector.sub(f1p1, f2p1);
	previousDirection.normalize();
	currentDirection.normalize();
    
	float cos = currentDirection.dot(previousDirection);
	float angle = acos(cos);

	PVector sin = currentDirection.cross(previousDirection);
	if( sin.z < 0 )
	    angle = -angle;
	return angle;
    }

    float computeScale(PVector f1p0, PVector f1p1, PVector f2p0, PVector f2p1){
    
	PVector tmp1 = f1p1.get();
	PVector tmp2 = f1p0.get();

	tmp1.sub(f2p1);
	tmp2.sub(f2p0);
	return tmp2.mag() / tmp1.mag() ;
    }

    PVector computeTranslate(PVector f1p0, PVector f1p1, PVector f2p0, PVector f2p1){

	PVector previousCenter = new PVector((f1p0.x + f2p0.x) / 2f,
					     (f1p0.y + f2p0.y) / 2f);

	PVector currentCenter = new PVector((f1p1.x + f2p1.x) / 2f,
					    (f1p1.y + f2p1.y) / 2f);

	return PVector.sub(previousCenter, currentCenter);
    }

}


