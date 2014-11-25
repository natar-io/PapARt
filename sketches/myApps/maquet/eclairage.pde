
public class Eclairage extends PaperTouchScreen{

    PImage acceuilECL, menuCamera, cameraSetupECL, cameraContrasteECL, cameraZoomECL, coupoleECL;
    Button cameraButtonECL, zoomButton, contrasteButton, setupButton, homeButton;


    static final int MENU = 0;
    static final int CAMERA1 = 1;
    static final int CAMERA2 = 2;
    static final int CAMERA3 = 3;

    int currentMode = MENU;

    PImage currentCameraImage; 

    void setup(){
	setDrawingSize(297, 210);
	setResolution(boardResolution);
	loadMarkerBoard(sketchPath + "/eclairage/eclairage.cfg", 297, 210);


	acceuilECL = loadImage("eclairage/acceuil.png");
	cameraSetupECL = loadImage("eclairage/menu camera setup.png");
	cameraContrasteECL = loadImage("eclairage/menu camera contrast.png");
	cameraZoomECL = loadImage("eclairage/menu camera Zoom.png");
	coupoleECL = loadImage("eclairage/menu coupole1.png");
	menuCamera = loadImage("eclairage/menu camera.png");

	cameraButtonECL = new Button(loadImage("eclairage/bouton eclairage.png"),
	//	cameraButtonECL = new Button("Eclairage",
				     10 + 70/2 ,
				     155 - 35 / 2,
				     70,
				     35);

	contrasteButton = new Button( loadImage("eclairage/contraste-no.png"),
				      loadImage("eclairage/contraste-select.png"),
				      70 + 50 /2, 
				      70 - 24 /2, 
				      50, 
				      24);

	setupButton = new Button( loadImage("eclairage/setup-no.png"),
				      loadImage("eclairage/setup-select.png"),
				      70 + 50 /2, 
				      102 - 24 /2, 
				      50, 
				      24);

	zoomButton = new Button( loadImage("eclairage/loupe-no.png"),
				 loadImage("eclairage/loupe-select.png"),
				 70 + 50 /2, 
				 132 - 24 /2, 
				 50, 
				 24);

	homeButton = new Button( loadImage("eclairage/home.png"), 
				 //	homeButton = new Button( "Home", 
				 252 + 44 /2, 
				 70 - 24 /2,
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



    void draw(){


	beginDraw2D();
	
	clear();
	
	PGraphicsOpenGL g = getGraphics();

	PImage toDisplay = acceuilECL;
	if(currentMode == MENU){
	    toDisplay = acceuilECL;
	}
	if(currentMode == CAMERA1){
	    toDisplay = currentCameraImage;
	}

	

	imageMode(CORNER);	
	image(toDisplay, 0, 0, (int) drawingSize.x, (int) drawingSize.y);


	drawButtons();
        for (Touch t : touchList) {
            if (!t.is3D) {
		ellipse(t.position.x, t.position.y, 10, 10);
	    }
        }
	
	cameraButtonECL.drawSelf(g);
	
	g.endDraw();
	
    }

    
    
    
    
}
