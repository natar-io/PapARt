
class Eclairage extends PaperInterface{

    PImage acceuilECL, menuCamera, cameraSetupECL, cameraContrasteECL, cameraZoomECL, coupoleECL;
    Button cameraButtonECL, zoomButton, contrasteButton, setupButton, homeButton;


    static final int MENU = 0;
    static final int CAMERA1 = 1;
    static final int CAMERA2 = 2;
    static final int CAMERA3 = 3;

    int currentMode = MENU;

    PImage currentCameraImage; 


    public Eclairage(PApplet parent, MarkerBoard board, PVector size, Camera cam, float resolution, Projector proj){
	super(parent, board, size, cam, resolution, proj);
    }



    public void init(){

	acceuilECL = loadImage("eclairage/acceuil.png");
	cameraSetupECL = loadImage("eclairage/menu camera setup.png");
	cameraContrasteECL = loadImage("eclairage/menu camera contrast.png");
	cameraZoomECL = loadImage("eclairage/menu camera Zoom.png");
	coupoleECL = loadImage("eclairage/menu coupole1.png");
	menuCamera = loadImage("eclairage/menu camera.png");

	cameraButtonECL = new Button(loadImage("eclairage/bouton eclairage.png"),
				     10 + 70/2 ,
				     55 + 35 / 2,
				     70,
				     35);

	contrasteButton = new Button( loadImage("eclairage/contraste-no.png"),
				      loadImage("eclairage/contraste-select.png"),
				      70 + 50 /2, 
				      140 + 24 /2, 
				      50, 
				      24);

	setupButton = new Button( loadImage("eclairage/setup-no.png"),
				      loadImage("eclairage/setup-select.png"),
				      70 + 50 /2, 
				      108 + 24 /2, 
				      50, 
				      24);

	zoomButton = new Button( loadImage("eclairage/loupe-no.png"),
				 loadImage("eclairage/loupe-select.png"),
				 70 + 50 /2, 
				 78 + 24 /2, 
				 50, 
				 24);

	homeButton = new Button( loadImage("eclairage/home.png"), 
				 252 + 44 /2, 
				 140 + 24 /2,
				 44, 
				 24);



	cameraButtonECL.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    enterCameraMenu();
		}
		public void ButtonReleased(){
		}
	    });


	homeButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    leaveCameraMenu();
		    homeButton.reset();
		}
		public void ButtonReleased(){

		}
	    });	


	contrasteButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    selectCameraMenu(contrasteButton);
		}
		public void ButtonReleased(){
		    selectCameraMenu(null);
		}
	    });

	setupButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    selectCameraMenu(setupButton);
		}
		public void ButtonReleased(){
		    selectCameraMenu(null);
		}
	    });

	zoomButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    selectCameraMenu(zoomButton);
		}
		public void ButtonReleased(){
		    selectCameraMenu(null);
		}
	    });
	
	
	buttons.add(cameraButtonECL);
	buttons.add(zoomButton);
	buttons.add(contrasteButton);
	buttons.add(setupButton);
	buttons.add(homeButton);

	zoomButton.hide();
	contrasteButton.hide();
	setupButton.hide();
	currentCameraImage = menuCamera;
    }    


    void enterCameraMenu(){
	cameraButtonECL.reset();
	cameraButtonECL.hide();

	currentMode = CAMERA1;

	zoomButton.show();
	contrasteButton.show();
	setupButton.show();
    }

    void leaveCameraMenu(){
	cameraButtonECL.show();
	currentMode = MENU;

	zoomButton.hide();
	contrasteButton.hide();
	setupButton.hide();
    }




    void selectCameraMenu(Button b){

	if(b == null){
	    currentCameraImage = menuCamera;
	    return;
	}

	if(b == contrasteButton){
	    currentCameraImage = cameraContrasteECL;
	    zoomButton.reset();
	    setupButton.reset();
	}

	if(b == setupButton){
	    currentCameraImage = cameraSetupECL;
	    zoomButton.reset();
	contrasteButton.reset();
	}

	if(b == zoomButton){
	    currentCameraImage = cameraZoomECL;
	    setupButton.reset();
	contrasteButton.reset();
	}

    }



    public void draw(){

	GLGraphicsOffScreen g;
	
	/////////////////////////////////////////////////
	////////////// Light management /////////////////
	/////////////////////////////////////////////////
	
	g = screen.getGraphics();
	
	g.beginDraw();
	g.scale(screenResolution);
	
	g.imageMode(CENTER);
	
	
	// , cameraSetupECL, cameraContrasteECL, cameraZoomECL,


	switch(currentMode){
	case MENU:
	    DrawUtils.drawImage(g, acceuilECL,  (int) (boardSize.x /2f), (int)(boardSize.y /2f),
				(int) boardSize.x, (int) boardSize.y);
	    break;

	case CAMERA1:

	    DrawUtils.drawImage(g, currentCameraImage,  (int) (boardSize.x /2f), (int)(boardSize.y /2f),
				(int) boardSize.x, (int) boardSize.y);



	    break;

	}


	for(Button b : buttons){
	    b.drawSelf(g);
	}

        /////////// Draw Touch //////////
	g.noStroke();
	g.fill(180, 150);
	for(PVector p : touch.position2D){
	    
	    g.ellipse(p.x * boardSize.x, 
		      p.y * boardSize.y, 
		      8, 8);
	    
	}
	
	cameraButtonECL.drawSelf(g);
	
	
	g.endDraw();
	
    }

    
    
    
    
}
