Button rotateButton, lockButton, lightButton;

public boolean isRotateMode(){
    return rotateButton.isActive();
}

public boolean isLockMode(){
    return lockButton.isActive();
}

public boolean isLightMode(){
    return lightButton.isActive();
}


public class LapinInterface extends PaperTouchScreen{

    //    Button rotateButton, lockButton, lightButton;
    PVector paperSize = new PVector(160, 160);
    
    void setup(){
	setDrawingSize((int) paperSize.x, (int) paperSize.y);
	loadMarkerBoard(sketchPath() + "/data/markers/lapinInterface.cfg", 
			paperSize.x, paperSize.y);

	createButtons();
    }
    
    void createButtons(){
	PImage glow = loadImage("glow.png");
	PImage glowDark = loadImage("glow-dark.png");

	int button2PosX = 87;
	int button2PosY = 100;
	int buttonSize = 64;

	rotateButton = new Button(glowDark, glow,
					 buttonSize / 2,
					 button2PosY + buttonSize / 2, 
					 buttonSize, 
					 buttonSize);

	rotateButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    rotateMode();
		}
		public void ButtonReleased(){
		}
	    });
	buttons.add(rotateButton);

	lockButton = new Button(glowDark, glow,
					 button2PosX + buttonSize / 2,
					 button2PosY + buttonSize / 2, 
					 buttonSize, 
					 buttonSize);

	lockButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    lockMode();
		}
		public void ButtonReleased(){
		}
	    });
	buttons.add(lockButton);


	lightButton = new Button(glowDark, glow,
					button2PosX + buttonSize / 2,
					0 + buttonSize / 2, 
					buttonSize,
					buttonSize);

	lightButton.addListener(new ButtonListener() {
		public void ButtonPressed(){
		    lightMode();
		}
		public void ButtonReleased(){
		}
	    });

	buttons.add(lightButton);
    }

    void rotateMode(){
	resetButtons();
	rotateButton.setActive();
    }

    void lightMode(){
	resetButtons();
	lightButton.setActive();
    }

    void lockMode(){
	resetButtons();
	lockButton.setActive();
    }

    void resetButtons(){
	for(Button b : buttons){
	    b.reset();
	}
    }

    public void resetPos(){
	screen.resetPos();
    }

    public void draw(){
	// setLocation(0, -90, 0);
	beginDraw2D();
	clear();
	background(0);

	noStroke();
	drawButtons();
	endDraw();
    }

}
