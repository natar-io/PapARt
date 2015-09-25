
ArrayList<InteractiveZone> interfaceZones = new ArrayList<InteractiveZone>();
ArrayList<Drawable> interfaceDrawables = new ArrayList<Drawable>();

PFont interfaceFont;

//////////////// INTERFACE MAIN BUTTONS ////////////////////
Button decouverteButton, copyrightButton, scene3DButton, testButton, dessinerButton, dessinerButton2, leftButton, rightButton;

//////////////// INTERFACE 3D SCENE BUTTONS  ////////////////////
Button lumiere1Button, placementButton, retourButton;

Button[] buttons;
Button[] scene3DButtons;

/////////// Interface Modes /////////////
static final int MODE_NONE = -1;
static final int MODE_DECOUVERTE = 0;
static final int MODE_DESSINER = 2;
static final int MODE_COPYRIGHT = 3;
static final int MODE_SCENE3D = 4;

static final int MODE_PARTICULES = 5;


/////////// Secondary Modes /////////////
static final int MODE_SCENE3D_PLACEMENT = 5;
static final int MODE_SCENE3D_LUMIERE1 = 6;

static final int MODE_ECLAIRAGE = 0;
static final int MODE_POSITION = 1;
static final int MODE_SCENE_SIMPLE = 0;


int currentInterfaceMode = MODE_NONE;
int currentSecondaryMode = MODE_NONE;
int currentDrawMode =  MODE_SCENE_SIMPLE;

float drawEndMinimum = 0;

public void initInterface(){

    DrawUtils.applet = this;
    interfaceFont = loadFont(sketchPath + "/data/Font/CenturySchL-Bold-20.vlw");
    Button.setFont(interfaceFont);

     //////// LOADING THE INTERFACE BUTTONS ///////////
    //////// LOADING THE INTERFACE BUTTONS ///////////
    decouverteButton = new Button(loadImage(sketchPath + "/images/decouverte.png"), 45,  70, 70, 25);
    copyrightButton = new Button(loadImage(sketchPath + "/images/credits.png"),   150, 70, 70, 25);
    scene3DButton = new Button(loadImage(sketchPath + "/images/scene3D.png"),       83, 130, 60, 25);

    dessinerButton = new Button(loadImage(sketchPath + "/images/dessiner.png"),     70, 20, 40, 17);
    dessinerButton2 = new Button(loadImage(sketchPath + "/images/dessiner.png"),     110, 20, 40, 17);

    leftButton = new Button(loadImage(sketchPath + "/images/left.png"),45,  70, 70, 25);
    rightButton = new Button(loadImage(sketchPath + "/images/right.png"),150, 70, 70, 25);

    //////// LOADING THE INTERFACE 3D SCENE BUTTONS ///////////
    // tailleButton = new Button("taille.png",      45,  70, 50, 25);
    // eclairageButton = new Button("eclairage.png",130, 70, 70, 25);
    // positionButton = new Button("position.png",    83, 20, 60, 25);

    placementButton = new Button(loadImage(sketchPath + "/images/placement.png"), 40, 90, 80, 25);
    lumiere1Button = new Button(loadImage(sketchPath + "/images/lumiere.png"),   130, 90, 70, 25);
    retourButton = new Button(loadImage(sketchPath + "/images/retour.png"), 90, 90, 70, 25);

    //    aButton = new Button("a.png",    83, 20, $60, 25);

    buttons = new Button[10];
    buttons[0] = decouverteButton;
    buttons[1] = copyrightButton;
    buttons[2] = scene3DButton;
    buttons[3] = dessinerButton;
    buttons[4] = placementButton;
    buttons[5] = lumiere1Button;
    buttons[6] = retourButton;
    buttons[7] = dessinerButton2;
    buttons[8] = leftButton;
    buttons[9] = rightButton;

    for(Button b : buttons){
	b.reset();
	addInteractiveZone(b);
    }

    // Set to the *** mode at the beginning
    // decouverteButton.isActive = true;
    scene3DButton.isActive = true;
    // copyrightButton.isActive = true;

}



void drawPaperInterface(Screen screenInterface, TouchElement te){
  
  GLGraphicsOffScreen interfaceScreen = screenInterface.getGraphics();
  
  interfaceScreen.beginDraw();
  interfaceScreen.scale(screenInterface.getScale());
  interfaceScreen.pushMatrix();
  
  PVector interfaceSize = screenInterface.getSize();

  interfaceScreen.clear(0);

  // interfaceScreen.translate(-interfaceSize.x/2f, -interfaceSize.y/2f, 0);
  // interfaceScreen.translate(-interfaceSize.x/2f, -interfaceSize.y/2f, 0);
  drawInterface(interfaceScreen);

    //////////////////////////// USER INTERFACE /////////////////////////////
  if(!te.position2D.isEmpty()){
      interfaceScreen.fill(180);
      interfaceScreen.noStroke();
      for(PVector v: te.position2D){
	  observeInput(v.x * interfaceSize.x , v.y * interfaceSize.y, null);
	  interfaceScreen.ellipse(v.x * interfaceSize.x , v.y * interfaceSize.y, 20, 20);
      }
  }else
    unSelect();

  //    interfaceScreen.image(image1, 0, 0, interfaceSize.x, interfaceSize.y);
  interfaceScreen.popMatrix();
  interfaceScreen.endDraw();
}


void updateSecondaryInterface(){

    boolean changed = false;

    if(activeButton(retourButton)){
	currentSecondaryMode = MODE_NONE;
	retourButton.hide();
	changed = true;
    }


    if(activeButton(lumiere1Button)){
	currentSecondaryMode = MODE_SCENE3D_LUMIERE1;
	changed = true;
    }
    if(activeButton(placementButton)){

	changed = true;
	useStereo = !useStereo;

	currentInterfaceMode = MODE_SCENE3D;
	currentSecondaryMode = MODE_NONE;
	//	currentSecondaryMode = MODE_SCENE3D_PLACEMENT;
    }

    // if(currentInterfaceMode == MODE_DESSINER){
    //   if(leftButton.isActive){
    // 	drawingLeft = true;
    // 	leftButton.reset();
    //   }
    //   if(rightButton.isActive){
    // 	drawingLeft = false;
    // 	rightButton.reset();
    //   }
    // }


    if(dessinerButton2.isActive)
	dessinerButton2.reset();

    if(dessinerButton.isActive)
	dessinerButton.reset();


    // One DessinerButton are selected    
    if(drawEndMinimum < millis() && 
       dessinerButton.isSelected ||  dessinerButton2.isSelected){

	drawEndMinimum = millis() + 500; // 500ms 

	if(currentInterfaceMode == MODE_SCENE3D){
	    currentInterfaceMode = MODE_DESSINER;
	    hideAllButtons();
	    // leftButton.show();
	    // rightButton.show();
	    dessinerButton.show();
	    //	    dessinerButton2.show();
	}else{
	    changed = true;
	    currentInterfaceMode = MODE_SCENE3D;
	}

    }


    // Back to standard mode
    if(changed && currentInterfaceMode == MODE_SCENE3D && currentSecondaryMode == MODE_NONE){
	lumiere1Button.show();
	placementButton.show();
	//	scene3DButton.show();
	dessinerButton.show();
	//	dessinerButton2.show();
	retourButton.hide();
	leftButton.hide();
	rightButton.hide();

    }
}

void changeDrawMode(boolean next){

    // currentDrawMode += next? 1 : -1;
    // currentDrawMode = constrain(currentDrawMode, 0, allScenes.length-1 );

    println(currentDrawMode);
    // currentScene = allScenes[currentDrawMode];

    /* currentScene = new Scene((int) random(2.1), */
    /* 			     (int) random(2.1),  */
    /* 			     ((int) random(1.1)) == 0, */
    /* 			     lightEffect, lightEffect, lightColor, null); */
}

boolean activeButton(Button b){
    if(b.isActive){
	hideAllButtons();
	retourButton.show();
	//	b.show();
	b.isActive = false;
	return true;
    }
    return false;
}

void updateInterface(){

    boolean modeChanged = false;
    if(decouverteButton.isActive){
	currentInterfaceMode =  (currentInterfaceMode == MODE_DECOUVERTE)? MODE_NONE : MODE_DECOUVERTE;
	decouverteButton.isActive = false;
	modeChanged = true;
    }

    if(scene3DButton.isActive){
	if(currentInterfaceMode == MODE_SCENE3D){
	    // DEMO: scene 3D Button desactivated
	    currentInterfaceMode = MODE_SCENE3D;	    

	    //	    currentInterfaceMode = MODE_NONE;
	}else{
	    currentInterfaceMode = MODE_SCENE3D;	    
	}
	//	currentInterfaceMode =  (currentInterfaceMode == MODE_SCENE3D)? MODE_NONE : MODE_SCENE3D;
	scene3DButton.isActive = false;
	modeChanged = true;
    }


    if(copyrightButton.isActive){
	currentInterfaceMode =  (currentInterfaceMode == MODE_COPYRIGHT)? MODE_NONE : MODE_COPYRIGHT;
	copyrightButton.isActive = false;
	modeChanged = true;
    }

    if(!modeChanged)
	return;

    hideAllButtons();

    switch(currentInterfaceMode){
    case MODE_SCENE3D : showInterfaceScene3D();
      break;
    case MODE_NONE : showInterfaceNone();
      break;
    }
}



void showInterfaceNone(){
    hideAllButtons();
    decouverteButton.show();
    copyrightButton.show();
    //    scene3DButton.show();
}


void showInterfaceScene3D(){
    //    scene3DButton.show();
    lumiere1Button.show();
    placementButton.show();
    dessinerButton.show();
    //    dessinerButton2.show();
}

void showInterfaceCopyright(){
    copyrightButton.show();
}

void hideAllButtons(){
    for(Button b : buttons){
	// TODO: reset ?
	b.reset();
	b.hide();
    }
}



public void addInteractiveZone(InteractiveZone z) {
    interfaceZones.add(z);
    interfaceDrawables.add(z);
}

public void drawInterface(PGraphics3D graphics){

    updateInterface();
    updateSecondaryInterface();

    graphics.pushStyle();
    graphics.imageMode(CENTER);
    graphics.fill(200,0 ,150);
    //    graphics.stroke(200, 0, 0);

    // TODO: feedback sur les doigts ?

    for (Drawable d : interfaceDrawables)
	d.drawSelf(graphics);
    graphics.popStyle();
    graphics.noTint();
}

public void observeInput(float x, float y, TouchPoint tp) {
    for (InteractiveZone z : interfaceZones) {
	if (z.isSelected(x, y, tp)) {
	    return;
	}
    }
}

public void unSelect(){
  for (InteractiveZone z : interfaceZones) {
      z.isSelected = false;
  }
}
